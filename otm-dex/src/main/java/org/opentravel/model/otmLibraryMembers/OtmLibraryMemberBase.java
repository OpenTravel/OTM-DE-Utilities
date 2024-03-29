/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.model.otmLibraryMembers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.common.ValidationUtils;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmLocalLibrary;
import org.opentravel.model.otmContainers.OtmVersionChain;
import org.opentravel.model.otmContainers.OtmVersionChainBase;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmFacets.OtmFacetFactory;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.Versioned;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Abstract OTM Library Member base class.
 * <p>
 * Note: implements children owner even though not all library members are children owners, but most are.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmLibraryMemberBase<T extends TLModelElement> extends OtmModelElement<TLModelElement>
    implements OtmLibraryMember, OtmTypeProvider, OtmChildrenOwner {
    private static Logger log = LogManager.getLogger( OtmLibraryMemberBase.class );

    protected OtmModelManager mgr = null;

    // A list of all descendants that are type providers. Created by getDescendantsTypeProviders
    protected List<OtmTypeProvider> membersProviders = null;

    // A list of all descendants that are type users. Created by getDescendantsTypeUsers.
    protected List<OtmTypeUser> memberTypeUsers = new ArrayList<>();

    // A list of all the types used by this member or its descendants
    // Computed in getUsedTypes(). Not managed by add/remove.
    protected List<OtmTypeProvider> typesUsed = null;

    // A list of all members that have a descendant type user that assigned to this member and its descendants.
    protected List<OtmLibraryMember> whereUsed = null;

    protected boolean editableMinor = false;

    private DexActionManager noLibraryActionManager;


    /**
     * Construct library member. Set its model manager, TL object and add a listener.
     */
    public OtmLibraryMemberBase(T tl, OtmModelManager mgr) {
        super( tl );
        this.mgr = mgr;
        this.noLibraryActionManager = getModelManager().getActionManager( false );
        setEditableMinor();
    }

    @Override
    public void addAlias(TLAlias tla) {
        if (tla.getOwningEntity() instanceof TLFacet) {
            String baseName = tla.getLocalName().substring( 0, tla.getName().lastIndexOf( '_' ) );
            children.forEach( c -> {
                if (c instanceof OtmAlias && c.getName().equals( baseName ))
                    ((OtmAlias) c).add( tla );
            } );
        } else if (getTL() instanceof TLAliasOwner) {
            try {
                ((TLAliasOwner) getTL()).addAlias( tla );
            } catch (UnsupportedOperationException e) {
                log.warn( "Add alias failed. " + e.getLocalizedMessage() );
                return;
            }
            new OtmAlias( tla, this );
        }
    }

    /**
     * Add user to passed list iff user's assigned type is not already in list.
     * 
     * @param user
     * @param list
     */
    private void addProvider(OtmTypeUser user, List<OtmTypeProvider> list) {
        if (user == null)
            return;
        OtmTypeProvider p = user.getAssignedType();
        if (p != null && !list.contains( p ))
            list.add( p );
    }

    @Override
    public StringProperty baseTypeProperty() {
        return new ReadOnlyStringWrapper( "" );
    }

    @Override
    public void build() {
        // NoOp
    }

    @Override
    public void changeWhereUsed(OtmLibraryMember oldUser, OtmLibraryMember newUser) {
        // 6/2/2021 - just recompute? Forces getWhereUsed() to recompute.
        // This solves the multiple users problem noted below.
        // Leave this in unless performance issues arise.
        // See junit TestTypeAssignmentAndWhereUsed
        whereUsed = null;
        // log.debug( "Cleared " + this + " whereUsed list." );
        // if (whereUsed == null)
        // whereUsed = new ArrayList<>();
        // // TODO - what if there are multiple users in the same owner?
        // if (oldUser != null)
        // whereUsed.remove( oldUser );
        // if (newUser != null && !whereUsed.contains( newUser ))
        // whereUsed.add( newUser );
        // log.debug( "Removed " + oldUser + " and added " + newUser + " from " + this );
    }

    /**
     * Return true if the list contains the child or the list member.getTL() contains the child.getTL()
     * 
     * @param list
     * @param child
     * @return
     */
    protected boolean contains(List<OtmObject> list, OtmObject child) {
        if (list.contains( child ))
            return true;
        if (child == null || child.getTL() == null)
            return false;

        ArrayList<OtmObject> localList = new ArrayList<>( list );
        for (OtmObject c : localList)
            if (c != null && c.getTL() != null && c.getTL() == child.getTL())
                return true;
        return false;
    }

    @Override
    public boolean contains(OtmObject o) {
        return children.contains( o );
    }

    @Override
    public OtmLibraryMember copy() {
        LibraryElement tlMember = null;
        try {
            tlMember = getTL().cloneElement();
        } catch (Exception e) {
            log.debug( "Error cloning." + getClass().getSimpleName() + " " + getName() );
        }
        if (tlMember instanceof LibraryMember)
            return OtmLibraryMemberFactory.create( (LibraryMember) tlMember, getModelManager() );
        return null;
    }

    @Override
    public OtmLibraryMember createMinorVersion(OtmLibrary minorLibrary) {
        OtmLibraryMember lm = null;
        Versioned v = null;
        Exception exception = null;
        String errMsg = null;
        if (!OtmVersionChainBase.isLaterVersion( getLibrary(), minorLibrary ))
            errMsg = minorLibrary + " is not later version of this library.";
        if (getLibrary() == minorLibrary)
            errMsg = "Same library.";
        if (!(minorLibrary.isMinorVersion()))
            errMsg = "Not a minor verion.";
        if (!(getTL() instanceof Versioned))
            errMsg = "Not a versioned object type.";
        if (!(minorLibrary.getTL() instanceof TLLibrary))
            errMsg = "Not a TL library.";

        //
        if (errMsg == null) {
            TLLibrary targetTLLib = (TLLibrary) minorLibrary.getTL();
            try {
                MinorVersionHelper helper = new MinorVersionHelper();
                v = helper.createNewMinorVersion( (Versioned) getTL(), targetTLLib );
                lm = OtmLibraryMemberFactory.create( (LibraryMember) v, getModelManager() );
            } catch (VersionSchemeException e) {
                errMsg = "Minor Version Error: " + this + " into library: " + targetTLLib.getPrefix() + ":"
                    + targetTLLib.getName();
                errMsg += "\n" + e.getLocalizedMessage();
                exception = e;
                lm = null;
            } catch (ValidationException e) {
                errMsg = "Minor Version Error: " + targetTLLib.getPrefix() + ":" + targetTLLib.getName();
                ValidationFindings findings = e.getFindings();
                log.debug( ValidationUtils.getMessagesAsString( findings ) );
                exception = e;
                lm = null;
            }
            if (errMsg != null) {
                getActionManager().postError( exception, errMsg );
                log.debug( errMsg );
            } else if (lm != null) {
                // Clone the aliases
                for (OtmObject c : getChildren()) {
                    if (c instanceof OtmAlias)
                        new OtmAlias( c.getName(), lm );
                }
            }
        }
        return lm;
    }


    /**
     * {@inheritDoc} Delete aliases. Sub-types MUST implement if any of their children are delete-able
     */
    @Override
    public void delete(OtmObject property) {
        if (property instanceof OtmAlias && getTL() instanceof TLAliasOwner) {
            ((TLAliasOwner) getTL()).removeAlias( (TLAlias) property.getTL() );
            children.remove( property );
            // TODO - what about where used?
        }
    }

    @Override
    public DexActionManager getActionManager() {
        return getLibrary() != null ? getLibrary().getActionManager( this ) : noLibraryActionManager;
    }

    @Override
    public OtmObject getBaseType() {
        if (getTL() instanceof TLExtensionOwner && ((TLExtensionOwner) getTL()).getExtension() != null)
            return OtmModelElement
                .get( (TLModelElement) ((TLExtensionOwner) getTL()).getExtension().getExtendsEntity() );
        return null;
    }

    @Override
    public String getBaseTypeName() {
        return getBaseType() != null ? getBaseType().getName() : "";
    }

    @Override
    public List<OtmObject> getChildren() {
        // Only let one thread model the children
        synchronized (this) {
            if (children != null && children.isEmpty())
                modelChildren();
        }
        return children != null ? children : Collections.emptyList();
    }

    @Override
    public synchronized Collection<OtmContributedFacet> getChildrenContributedFacets() {
        List<OtmObject> kids = new ArrayList<>( getChildren() );
        if (!kids.isEmpty()) {
            List<OtmContributedFacet> pChildren = new ArrayList<>();
            for (OtmObject child : kids)
                if (child instanceof OtmContributedFacet)
                    pChildren.add( (OtmContributedFacet) child );
            return pChildren;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        Collection<OtmObject> hierarchy = new HashSet<>();
        if (getInheritedChildren() != null)
            getInheritedChildren().forEach( hierarchy::add );
        getChildren().forEach( hierarchy::add );
        return hierarchy;
    }

    @Override
    public synchronized Collection<OtmTypeProvider> getChildrenTypeProviders() {
        List<OtmObject> kids = new ArrayList<>( getChildren() );
        if (!kids.isEmpty()) {
            List<OtmTypeProvider> pChildren = new ArrayList<>();
            for (OtmObject child : kids)
                if (child instanceof OtmTypeProvider)
                    pChildren.add( (OtmTypeProvider) child );
            return pChildren;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public synchronized Collection<OtmChildrenOwner> getDescendantsChildrenOwners() {
        List<OtmChildrenOwner> owners = new ArrayList<>();
        List<OtmObject> children = Collections.synchronizedList( new ArrayList<>( getChildren() ) );
        synchronized (children) {
            for (OtmObject child : children) {
                if (child instanceof OtmChildrenOwner) {
                    owners.add( (OtmChildrenOwner) child );
                    // Recurse
                    owners.addAll( ((OtmChildrenOwner) child).getDescendantsChildrenOwners() );
                }
            }
        }
        return owners;
    }

    @Override
    public synchronized Collection<OtmPropertyOwner> getDescendantsPropertyOwners() {
        List<OtmPropertyOwner> owners = new ArrayList<>();
        List<OtmObject> children = Collections.synchronizedList( new ArrayList<>( getChildren() ) );
        synchronized (children) {
            for (OtmObject child : children) {
                if (child instanceof OtmPropertyOwner) {
                    owners.add( (OtmPropertyOwner) child );
                    // Recurse
                    owners.addAll( ((OtmChildrenOwner) child).getDescendantsPropertyOwners() );
                }
            }
        }
        return owners;
    }

    @Override
    public synchronized List<OtmTypeProvider> getDescendantsTypeProviders() {
        if (membersProviders == null)
            if (getChildrenTypeProviders() != null) {
                membersProviders = new ArrayList<>();
                for (OtmTypeProvider p : getChildrenTypeProviders()) {
                    membersProviders.add( p );
                    // Recurse
                    if (p instanceof OtmChildrenOwner)
                        membersProviders.addAll( ((OtmChildrenOwner) p).getDescendantsTypeProviders() );
                }
            }
        return membersProviders;
    }

    @Override
    public synchronized Collection<OtmTypeUser> getDescendantsTypeUsers() {
        // TODO - should this be cached?
        memberTypeUsers.clear();
        List<OtmObject> children = Collections.synchronizedList( new ArrayList<>( getChildren() ) );
        synchronized (children) {
            for (OtmObject child : children) {
                if (child instanceof OtmTypeUser)
                    memberTypeUsers.add( (OtmTypeUser) child );
            }
        }
        // Recurse
        for (OtmChildrenOwner co : getDescendantsChildrenOwners()) {
            Collection<OtmTypeUser> u = co.getDescendantsTypeUsers();
            memberTypeUsers.addAll( u );
        }
        // log.debug("Users now has " + memberTypeUsers.size() + " items");
        return memberTypeUsers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public OtmFacet<TLFacet> getFacet(OtmFacet<TLFacet> facet) {
        if (facet != null) {
            TLFacetType type = facet.getTL().getFacetType();
            for (OtmObject kid : getChildren())
                if (kid instanceof OtmFacet && kid.getTL() instanceof TLFacet
                    && ((TLFacet) kid.getTL()).getFacetType() == type)
                    return (OtmFacet<TLFacet>) kid;
        }
        return null;
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        modelInheritedChildren();
        return inheritedChildren;
    }

    @Override
    public OtmLibrary getLibrary() {
        if (mgr == null)
            return null;

        // Debugging
        LibraryMember tllm = getTlLM();
        AbstractLibrary tllmOwingLib = getTlLM().getOwningLibrary();
        // During construction, the mgr does not have this object or it's TL
        // OtmLibrary owningLib = mgr.get( getTlLM().getOwningLibrary() );
        // OtmLibrary owningLib2 = mgr.get( tllmOwingLib );

        // Deleted members will have the tl library removed
        return getTlLM() != null && getTlLM().getOwningLibrary() != null ? mgr.get( getTlLM().getOwningLibrary() )
            : null;
        // return getTlLM() != null && getTlLM().getOwningLibrary() != null ? mgr.get( getTlLM().getOwningLibrary() )
        // : null;
    }

    @Override
    public String getLibraryName() {
        return getTlLM() != null && getTlLM().getOwningLibrary() != null ? getTlLM().getOwningLibrary().getName() : "";
    }

    @Override
    public OtmTypeProvider getMatchingProvider(OtmTypeProvider provider) {
        OtmTypeProvider match = null;
        if (this.getClass() == provider.getClass())
            match = this;
        else
            for (OtmTypeProvider p : getDescendantsTypeProviders())
                if (p.getClass() == provider.getClass()) {
                    match = p;
                    break;
                }
        return match;
    }

    @Override
    public OtmModelManager getModelManager() {
        return mgr;
    }

    @Override
    public String getNamespace() {
        return getTlLM().getNamespace();
    }

    @Override
    public String getObjectTypeName() {
        return OtmLibraryMemberFactory.getObjectName( this );
    }

    @Override
    public String getPrefix() {
        return getTlLM().getOwningLibrary() != null ? getTlLM().getOwningLibrary().getPrefix() : "";
    }

    @Override
    public Map<OtmTypeUser,OtmTypeProvider> getPropertiesWhereUsed() {
        Map<OtmTypeUser,OtmTypeProvider> users = new HashMap<>();

        List<OtmTypeProvider> thisProviders = getDescendantsTypeProviders();
        // List<OtmTypeUser> users = new ArrayList<>();
        for (OtmLibraryMember owner : getWhereUsed())
            for (OtmTypeUser user : owner.getDescendantsTypeUsers())
                if (user.getAssignedType() == this || thisProviders.contains( user.getAssignedType() ))
                    users.put( user, user.getAssignedType() );
        return users;

        // TODO - what if the owner is a type user?
    }

    @Override
    public LibraryMember getTlLM() {
        return (LibraryMember) getTL();
    }

    @Override
    public List<OtmTypeUser> getTypeUsers(OtmTypeProvider provider) {
        List<OtmTypeUser> users = new ArrayList<>();
        if (this instanceof OtmTypeUser && ((OtmTypeUser) this).getAssignedType() == provider)
            users.add( (OtmTypeUser) this );
        for (OtmTypeUser candidate : getDescendantsTypeUsers())
            if (candidate.getAssignedType() == provider)
                users.add( candidate );
        return users;
    }

    @Override
    public List<OtmTypeProvider> getUsedTypes() {
        // TODO - consider how to make this into lazy evaluated and persisted.
        // if (typesUsed == null) {
        // typesUsed = new ArrayList<>();
        typesUsed = Collections.synchronizedList( new ArrayList<>() );

        // Type used by this type users
        if (this instanceof OtmTypeUser)
            addProvider( (OtmTypeUser) this, typesUsed );

        // Types used by all descendant type users. Prevent concurrent modification.
        Collection<OtmTypeUser> descendants =
            Collections.synchronizedList( new ArrayList<>( getDescendantsTypeUsers() ) );
        synchronized (descendants) {
            descendants.forEach( d -> addProvider( d, typesUsed ) );
        }
        // DONE - 4/16/2021
        // Make typesUsed a synchronizedList and added synchronized() to prevent
        // Exception in thread "JavaFX Application Thread" java.util.ConcurrentModificationException
        synchronized (typesUsed) {
            typesUsed.sort(
                (OtmObject o1, OtmObject o2) -> o1.getNameWithPrefix().compareToIgnoreCase( o2.getNameWithPrefix() ) );
        }
        // }
        // log.debug( this + " typesUsed size = " + typesUsed.size() );
        return typesUsed;
    }

    @Override
    public int getUsedTypesCount() {
        return getUsedTypes().size();
    }

    @Override
    public List<OtmLibraryMember> getWhereUsed() {
        return getWhereUsed( false );
    }

    @Override
    public List<OtmLibraryMember> getWhereUsed(boolean force) {
        if (whereUsed == null) {
            whereUsed = new ArrayList<>();
            force = true;
        }
        if (force) {
            whereUsed.clear();
            whereUsed.addAll( mgr.findUsersOf( this ) ); // Relies on list from getUsedTypes()
            whereUsed.addAll( mgr.findSubtypesOf( this ) ); // base types
            // FIXME - get resources when they expose this library member
            // FIXME - aliases???

            // Will re-populate children using modelChildren()
            getDescendantsTypeProviders().forEach( p -> whereUsed.addAll( mgr.findUsersOf( p ) ) );
            // log.debug( "Created Where Used List " + whereUsed.size() + " for : " + this.getNameWithPrefix() );
        }
        return whereUsed;
    }

    /**
     * {@inheritDoc} Library members return the edit-ability of their library.
     * 
     * @see org.opentravel.model.OtmModelElement#isEditable()
     */
    @Override
    public boolean isEditable() {
        return getLibrary() != null && getLibrary().isEditable();
    }

    @Override
    public boolean isEditableMinor() {
        setEditableMinor();
        return editableMinor;
    }

    @Override
    public boolean isLatest() {
        if (getLibrary() instanceof OtmLocalLibrary)
            return true;
        if (getLibrary().getVersionChain().isLatestChain())
            return getLibrary().getVersionChain().isLatestVersion( this );
        else
            return false;
    }

    @Override
    public boolean isLatestVersion() {
        if (getLibrary().getVersionChain().isLatestChain())
            return getLibrary().getVersionChain().isLatestVersion( this );
        else
            return false;
    }

    @Override
    public boolean isValid(boolean force) {
        if (getLibrary() == null)
            return false; // Can't be valid if not in a library.
        if (force) {
            // prevent concurrent access - https://www.geeksforgeeks.org/synchronization-arraylist-java/
            List<OtmObject> kids = Collections.synchronizedList( new ArrayList<>( getChildren() ) );
            synchronized (kids) {
                kids.forEach( c -> {
                    if (c != this)
                        c.isValid( force );
                } );
                // synchronized (this) {
                // List<OtmObject> kids = getChildren();
                // kids.forEach( c -> {
                // if (c != this)
                // c.isValid( force );
                // } );
            }
        }
        return super.isValid( force );
    }

    // TODO - why is this editable?
    @Override
    public StringProperty libraryProperty() {
        if (getLibrary() == null)
            return new ReadOnlyStringWrapper( "" ); // deleted member
        if (isEditable())
            return new SimpleStringProperty( getPrefix() + "  " + getLibrary().getVersion() + "  " + getLibraryName() );
        return new ReadOnlyStringWrapper( getPrefix() + "  " + getLibrary().getVersion() + "  " + getLibraryName() );
    }

    /**
     * {@inheritDoc} Create aliases and facets to represent facets in the TL object.
     */
    @Override
    public void modelChildren() {
        assert children.isEmpty();
        if (!children.isEmpty())
            log.debug( "OOPS - model children already has children." );
        Collection<OtmObject> kids = new HashSet<>(); // assure no duplications or co-modification
        // Must do aliases first so facet aliases will have a parent
        // Aliases from contextual facets come from the member where injected (contributed)
        if (getTL() instanceof TLAliasOwner && !(this instanceof OtmContextualFacet)) {
            ((TLAliasOwner) getTL()).getAliases().forEach( t -> {
                OtmObject obj = OtmModelElement.get( t );
                if (obj == null)
                    new OtmAlias( t, this );
                else
                    kids.add( obj );
            } );
        }

        if (getTL() instanceof TLFacetOwner)
            for (TLFacet tlFacet : ((TLFacetOwner) getTL()).getAllFacets()) {
                OtmFacet<?> facet = OtmFacetFactory.create( tlFacet, this );
                if (facet != null) {
                    kids.add( facet );
                }
            }

        synchronized (children) {
            for (OtmObject k : kids)
                if (!children.contains( k ))
                    children.add( k );
        }
    }

    /**
     * {@inheritDoc} Recompute inherited custom, query, choice and update facets
     */
    @Override
    public void modelInheritedChildren() {
        if (inheritedChildren == null)
            inheritedChildren = new ArrayList<>();
        else
            inheritedChildren.clear(); // force re-compute

        OtmObject baseType = getBaseType();
        // AliasCodegenUtils - there is nothing in the utils about inheritance
        if (getTL() instanceof TLFacetOwner && baseType != null) {

            TLFacetOwner extendedOwner = (TLFacetOwner) getTL();
            List<TLContextualFacet> ghosts = FacetCodegenUtils.findGhostFacets( extendedOwner, TLFacetType.CUSTOM );
            ghosts.addAll( FacetCodegenUtils.findGhostFacets( extendedOwner, TLFacetType.QUERY ) );
            ghosts.addAll( FacetCodegenUtils.findGhostFacets( extendedOwner, TLFacetType.CHOICE ) );
            ghosts.addAll( FacetCodegenUtils.findGhostFacets( extendedOwner, TLFacetType.UPDATE ) );
            // Ghosts do NOT have any children! See OtmFacet.modelInheritedChildren()

            // Create a contributed facet for each ghost
            ghosts.forEach( g -> inheritedChildren.add( OtmFacetFactory.create( g, this ) ) );

            // Replace contributor in each contributed facet with one from the base
            inheritedChildren.forEach( i -> setContributor( i, baseType ) );

            // if (ghosts.size() > 0)
            // log.debug( "Found and modeled " + ghosts.size() + " ghost facets on " + this.getName() );
        }
    }

    @Override
    public StringProperty prefixProperty() {
        return new ReadOnlyStringWrapper( getPrefix() );
    }

    /**
     * {@inheritDoc} Null out member providers and where used lists.
     */
    @Override
    public void refresh() {
        super.refresh();
        membersProviders = null; // Created by getDescendantsTypeProviders
        typesUsed = null;
        getWhereUsed( true );
        setEditableMinor();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If a contextual facet is passed, it's whereContributed is removed.
     * 
     * @see org.opentravel.model.OtmChildrenOwner#remove(org.opentravel.model.OtmObject)
     */
    @Override
    public void remove(OtmObject child) {
        if (child instanceof OtmContextualFacet)
            child = ((OtmContextualFacet) child).getWhereContributed();
        children.remove( child );
    }

    @Override
    public boolean sameBaseNamespace(OtmLibraryMember otherMember) {
        if (getLibrary() == null || otherMember == null || otherMember.getLibrary() == null)
            return false;
        return getLibrary().sameBaseNamespace( otherMember.getLibrary() );
    }

    @Override
    public OtmObject setBaseType(OtmObject baseObj) {
        OtmLibraryMember oldBaseOwner = null;
        if (getBaseType() != null)
            oldBaseOwner = getBaseType().getOwningMember();
        if (baseObj != null) {
            if (this.getClass() == baseObj.getClass() && getTL() instanceof TLExtensionOwner) {
                TLExtension tlExt = ((TLExtensionOwner) getTL()).getExtension();
                if (tlExt == null)
                    tlExt = new TLExtension();
                tlExt.setExtendsEntity( (NamedEntity) baseObj.getTL() );
                ((TLExtensionOwner) getTL()).setExtension( tlExt );
            }
        } else {
            // Clear the extension
            if (getTL() instanceof TLExtensionOwner)
                ((TLExtensionOwner) getTL()).setExtension( null );
        }
        // Set the where used in case resolver is not run afterwards
        OtmLibraryMember newBaseOwner = null;
        if (getBaseType() != null)
            newBaseOwner = getBaseType().getOwningMember();
        if (newBaseOwner != null)
            newBaseOwner.changeWhereUsed( oldBaseOwner, getOwningMember() );
        return getBaseType();
    }

    private void setContributor(OtmObject i, OtmObject baseType) {
        OtmContributedFacet contributed = null;
        OtmLibraryMember base = null;
        if (i instanceof OtmContributedFacet)
            contributed = (OtmContributedFacet) i;
        if (baseType instanceof OtmLibraryMember)
            base = (OtmLibraryMember) baseType;

        // Find a contextual facet with the same name and use it as the contributor
        if (contributed != null && base != null)
            for (OtmObject child : base.getChildren())
                // TL names do not include owner
                if (child instanceof OtmContributedFacet
                    && ((TLContextualFacet) child.getTL()).getName().equals( contributed.getTL().getName() )) {
                    contributed.setContributor( ((OtmContributedFacet) child).getContributor() );
                    assert contributed.isInherited();
                    break;
                }
    }

    /**
     * this is an often used expensive operation
     */
    private void setEditableMinor() {
        editableMinor = false;
        if (getLibrary() == null)
            return;
        if (isEditable())
            editableMinor = true;
        else {
            OtmVersionChain chain = getLibrary().getVersionChain();
            if (chain != null)
                editableMinor = chain.isChainEditable() && chain.isLatestChain() && chain.isLatestVersion( this );
        }
    }

    /**
     * Used by actions to allow editing on incomplete members.
     */
    public void setNoLibraryActionManager(DexActionManager actionManager) {
        this.noLibraryActionManager = actionManager;
        if (actionManager == null)
            this.noLibraryActionManager = getModelManager().getActionManager( false );
    }

    // TODO - why is this editable?
    @Override
    public StringProperty versionProperty() {
        return getLibrary() != null ? new SimpleStringProperty( getLibrary().getVersion() )
            : new ReadOnlyStringWrapper( "" );
    }
}
