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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.common.OtmTypeUserUtils;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmFacets.OtmListFacet;
import org.opentravel.model.otmFacets.OtmSummaryFacet;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLListFacet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * OTM Object Node for Core objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmCore extends OtmComplexObjects<TLCoreObject> implements OtmTypeUser {
    private static Log log = LogFactory.getLog( OtmCore.class );

    private StringProperty assignedTypeProperty;

    public OtmCore(TLCoreObject tlo, OtmModelManager mgr) {
        super( tlo, mgr );
    }

    public OtmCore(String name, OtmModelManager mgr) {
        super( new TLCoreObject(), mgr );
        setName( name );
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.CORE;
    }

    @Override
    public TLCoreObject getTL() {
        return (TLCoreObject) tlObject;
    }

    @Override
    public String setName(String name) {
        getTL().setName( name );
        isValid( true );
        return getName();
    }

    /**
     * {@inheritDoc} Creates facets to represent facets in the TL object.
     */
    @Override
    public void modelChildren() {
        super.modelChildren(); // Will model facets
        log.debug( "FIXME - Has " + children.size() + " children." );
        // Add
        // Role Enumeration - TLRoleEnumeration / TLRole
        children.add( new OtmRoleEnumeration( getTL().getRoleEnumeration(), this ) );
        // Simple Facet - TLSimpleFacet
        children.add( new OtmListFacet( getTL().getSimpleListFacet(), this ) );
        children.add( new OtmListFacet( getTL().getSummaryListFacet(), this ) );
        children.add( new OtmListFacet( getTL().getDetailListFacet(), this ) );
    }

    public OtmRoleEnumeration getRoles() {
        for (OtmObject c : getChildren())
            if (c instanceof OtmRoleEnumeration)
                return (OtmRoleEnumeration) c;
        return null;
    }

    public OtmListFacet getDetailList() {
        for (OtmObject c : getChildren())
            if (c instanceof OtmListFacet && ((TLListFacet) c.getTL()).getFacetType() == TLFacetType.DETAIL)
                return (OtmListFacet) c;
        return null;
    }

    public OtmListFacet getSummaryList() {
        for (OtmObject c : getChildren())
            if (c instanceof OtmListFacet && ((TLListFacet) c.getTL()).getFacetType() == TLFacetType.SUMMARY)
                return (OtmListFacet) c;
        return null;
    }

    public OtmListFacet getSimpleList() {
        for (OtmObject c : getChildren())
            if (c instanceof OtmListFacet && ((TLListFacet) c.getTL()).getFacetType() == TLFacetType.SIMPLE)
                return (OtmListFacet) c;
        return null;
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        Collection<OtmObject> ch = new ArrayList<>();
        children.forEach( c -> {
            // TODO - shouldn't simple be here too?
            if (c instanceof OtmSummaryFacet)
                ch.add( c );
            if (c instanceof OtmAlias)
                ch.add( c );
        } );

        // TODO - roles
        // TODO - lists
        return ch;
    }

    @Override
    public OtmTypeProvider getAssignedType() {
        return OtmTypeUserUtils.getAssignedType( this );
    }

    @Override
    public String getTlAssignedTypeName() {
        return getTL().getSimpleFacet().getSimpleTypeName();
    }

    @Override
    public StringProperty assignedTypeProperty() {
        if (assignedTypeProperty == null) {
            if (isEditable())
                assignedTypeProperty = new SimpleStringProperty( OtmTypeUserUtils.formatAssignedType( this ) );
            else
                assignedTypeProperty = new ReadOnlyStringWrapper( OtmTypeUserUtils.formatAssignedType( this ) );
        }
        return assignedTypeProperty;
    }

    @Override
    public NamedEntity getAssignedTLType() {
        NamedEntity tlType = getTL().getSimpleFacet().getSimpleType();
        return tlType instanceof NamedEntity ? (NamedEntity) tlType : null;
    }

    @Override
    public OtmTypeProvider setAssignedType(OtmTypeProvider type) {
        if (type != null && type.getTL() instanceof NamedEntity)
            setAssignedTLType( (NamedEntity) type.getTL() );
        return getAssignedType();
    }

    @Override
    public NamedEntity setAssignedTLType(NamedEntity type) {
        if (type instanceof NamedEntity)
            getTL().getSimpleFacet().setSimpleType( type );
        assignedTypeProperty = null;
        log.debug( "Set assigned TL type" );
        return getAssignedTLType();
    }

    @Override
    public void setTLTypeName(String name) {
        getTL().getSimpleFacet().setSimpleType( null );
        getTL().getSimpleFacet().setSimpleTypeName( name );
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        return Collections.emptyList(); // TODO
    }

    @Override
    public void modelInheritedChildren() {
        if (getTL().getExtension() != null)
            log.warn( "TODO - model inherited children" );
    }


    // extends FacetOwners
    // implements ExtensionOwner, AliasOwner, Sortable, ContextualFacetOwnerInterface, VersionedObjectInterface {

    // private static final Logger LOGGER = LoggerFactory.getLogger(BusinessObjectNode.class);
    // private ExtensionHandler extensionHandler = null;
    // private LibraryNode owningLibrary = null;
    //
    // public BusinessObjectNode(TLBusinessObject mbr) {
    // super(mbr);
    //
    // childrenHandler = new BusinessObjectChildrenHandler(this);
    // extensionHandler = new ExtensionHandler(this);
    // }
    //
    // /**
    // * Create a new business object using the core as a template and add to the same library as the core object.
    // *
    // * @param core
    // */
    // public BusinessObjectNode(CoreObjectNode core) {
    // this(new TLBusinessObject());
    // if (core == null)
    // return;
    // cloneAliases(core.getAliases());
    //
    // setName(core.getName());
    // core.getLibrary().addMember(this); // version managed library safe add
    // setDocumentation(core.getDocumentation());
    //
    // if (core.isDeleted())
    // return;
    // getFacet_Summary().copy(core.getFacet_Summary());
    // getFacet_Detail().copy(core.getFacet_Detail());
    //
    // // Assure business object has one and only one ID and it is in the ID facet.
    // fixIDs();
    // }
    //
    // public BusinessObjectNode(VWA_Node vwa) {
    // this(new TLBusinessObject());
    // if (vwa == null)
    // return;
    //
    // setName(vwa.getName());
    // vwa.getLibrary().addMember(this);
    // setDocumentation(vwa.getDocumentation());
    // if (vwa.isDeleted())
    // return;
    //
    // getFacet_Summary().copy(vwa.getFacet_Attributes());
    //
    // // Assure business object has one and only one ID and it is in the ID facet.
    // fixIDs();
    //
    // }
    //
    // @Override
    // public LibraryNode getLibrary() {
    // return owningLibrary;
    // }
    //
    // @Override
    // public void setLibrary(LibraryNode library) {
    // owningLibrary = library;
    // }
    //
    // @Override
    // public String getName() {
    // return emptyIfNull(getTLModelObject().getName());
    // }
    //
    // @Override
    // public TLBusinessObject getTLModelObject() {
    // return (TLBusinessObject) tlObj;
    // }
    //
    // @Override
    // public boolean isExtensibleObject() {
    // return true;
    // }
    //
    // @Override
    // public Node setExtensible(boolean extensible) {
    // if (isEditable_newToChain())
    // if (getTLModelObject() instanceof TLComplexTypeBase)
    // ((TLComplexTypeBase) getTLModelObject()).setNotExtendable(!extensible);
    // return this;
    // }
    //
    // @Override
    // public boolean hasChildren_TypeProviders() {
    // return true;
    // }
    //
    // @Override
    // public boolean isAssignedByReference() {
    // return true;
    // }
    //
    // @Override
    // public ComponentNodeType getComponentNodeType() {
    // return ComponentNodeType.BUSINESS;
    // }
    //
    // @Override
    // public ContributedFacetNode getContributedFacet(TLContextualFacet tlcf) {
    // ContributedFacetNode cfn = null;
    // for (TLModelElement tlo : getChildrenHandler().getChildren_TL())
    // if (tlo == tlcf)
    // if (Node.GetNode(tlo) instanceof ContextualFacetNode) {
    // ContextualFacetNode cxn = (ContextualFacetNode) Node.GetNode(tlo);
    // if (cxn != null) {
    // cfn = cxn.getWhereContributed();
    // break;
    // }
    // }
    // return cfn;
    // }
    //
    // @Override
    // public FacetProviderNode getFacet_Default() {
    // return getFacet_Summary();
    // }
    //
    // @Override
    // public Image getImage() {
    // return Images.getImageRegistry().get(Images.BusinessObject);
    // }
    //
    // @Override
    // public BaseNodeListener getNewListener() {
    // return new TypeProviderListener(this);
    // }
    //
    // @Override
    // public void remove(AliasNode alias) {
    // getTLModelObject().removeAlias(alias.getTLModelObject());
    // clearAllAliasHolders();
    // }
    //
    // @Override
    // public void addAlias(AliasNode alias) {
    // if (!getTLModelObject().getAliases().contains(alias.getTLModelObject()))
    // getTLModelObject().addAlias(alias.getTLModelObject());
    // clearAllAliasHolders();
    // }
    //
    // @Override
    // public AliasNode addAlias(String name) {
    // AliasNode alias = null;
    // if (this.isEditable_newToChain())
    // alias = new AliasNode(this, NodeNameUtils.fixBusinessObjectName(name));
    // return alias;
    // }
    //
    // @Override
    // public void cloneAliases(List<AliasNode> aliases) {
    // for (AliasNode a : aliases)
    // addAlias(a.getName());
    // }
    //
    // private void clearAllAliasHolders() {
    // for (Node child : getChildren())
    // if (child.getChildrenHandler() != null)
    // child.getChildrenHandler().clear();
    // getChildrenHandler().clear();
    // }
    //
    // /**
    // *
    // * New facets can only be added in unmanaged or head versions.
    // *
    // * @param name
    // * @param type
    // * @return the new contextual facet (not contributed)
    // */
    // // TODO - consider allowing them in minor and use createMinorVersionOfComponent()
    // @Override
    // public AbstractContextualFacet addFacet(String name, TLFacetType type) {
    // if (!isEditable_newToChain()) {
    // isEditable_newToChain();
    // throw new IllegalArgumentException("Not editable - Can not add facet to " + this);
    // }
    // TLContextualFacet tlCf = ContextualFacetNode.createTL(name, type);
    // AbstractContextualFacet cf = NodeFactory.createContextualFacet(tlCf);
    // cf.setOwner(this);
    // if (cf instanceof LibraryMemberInterface)
    // getLibrary().addMember((LibraryMemberInterface) cf);
    // cf.setName(NodeNameUtils.fixContextualFacetName(cf, name));
    //
    // if (OTM16Upgrade.otm16Enabled) {
    // assert cf.getParent() instanceof NavNode;
    // assert getChildren().contains(((ContextualFacetNode) cf).getWhereContributed());
    // } else {
    // assert cf.getParent() == this;
    // assert getChildren().contains(cf);
    // }
    // return cf;
    // }
    //
    // @Override
    // public boolean canOwn(AbstractContextualFacet targetCF) {
    // return canOwn(targetCF.getTLModelObject().getFacetType());
    // }
    //
    // @Override
    // public boolean canOwn(TLFacetType type) {
    // switch (type) {
    // case ID:
    // case SUMMARY:
    // case DETAIL:
    // case CUSTOM:
    // case QUERY:
    // case UPDATE:
    // return true;
    // default:
    // return false;
    // }
    // }
    //
    // @Override
    // public ComponentNode createMinorVersionComponent() {
    // TLBusinessObject tlMinor = (TLBusinessObject) createMinorTLVersion(this);
    // if (tlMinor != null)
    // return super.createMinorVersionComponent(new BusinessObjectNode(tlMinor));
    // return null;
    // }
    //
    // /**
    // * @return Custom Facets without inherited
    // */
    // public List<AbstractContextualFacet> getCustomFacets() {
    // ArrayList<AbstractContextualFacet> ret = new ArrayList<>();
    // for (INode f : getContextualFacets(false))
    // if (f instanceof CustomFacetNode)
    // ret.add((CustomFacetNode) f);
    // else if (f instanceof CustomFacet15Node)
    // ret.add((CustomFacet15Node) f);
    //
    // return ret;
    // }
    //
    // @Override
    // public NavNode getParent() {
    // return (NavNode) parent;
    // }
    //
    // // FIXME - make return abstractContextualFacet
    // public List<ComponentNode> getQueryFacets() {
    // ArrayList<ComponentNode> ret = new ArrayList<>();
    // for (AbstractContextualFacet f : getContextualFacets(false)) {
    // if (f instanceof QueryFacetNode)
    // ret.add(f);
    // if (f instanceof QueryFacet15Node)
    // ret.add(f);
    // }
    // return ret;
    // }
    //
    // @Override
    // public void delete() {
    // // Must delete the contextual facets separately because they are separate library members.
    // for (Node n : getChildren_New())
    // if (n instanceof ContextualFacetNode)
    // n.delete();
    // super.delete();
    // }
    //
    // /**
    // * Assure business object has one and only one ID and it is in the ID facet. Change extra IDs to attributes.
    // Create
    // * new ID if needed.
    // *
    // * @return
    // */
    // private IdNode fixIDs() {
    // IdNode finalID = null;
    // // Use from ID facet if found. if more than one found, change extras to attribute
    // for (Node n : getFacet_ID().getChildren())
    // if (n instanceof IdNode)
    // if (finalID == null) {
    // ((IdNode) n).moveProperty(getFacet_ID());
    // finalID = (IdNode) n;
    // } else
    // ((PropertyNode) n).changePropertyRole(PropertyNodeType.ATTRIBUTE);
    //
    // // Search for any ID types. Move 1st one to ID facet and make rest into attributes.
    // List<Node> properties = new ArrayList<>(getFacet_Summary().getChildren());
    // properties.addAll(getFacet_Detail().getChildren());
    // for (Node n : properties)
    // if (n instanceof IdNode)
    // if (finalID == null) {
    // ((IdNode) n).moveProperty(getFacet_ID());
    // finalID = (IdNode) n;
    // } else
    // ((PropertyNode) n).changePropertyRole(PropertyNodeType.ATTRIBUTE);
    //
    // // If none were found, make one
    // if (finalID == null)
    // finalID = new IdNode(getFacet_ID(), "newID"); // BO must have at least one ID facet property
    // return finalID;
    // }
    //
    // @Override
    // public INode.CommandType getAddCommand() {
    // return INode.CommandType.PROPERTY;
    // }
    //
    // @Override
    // public List<AliasNode> getAliases() {
    // List<AliasNode> aliases = new ArrayList<>();
    // for (Node c : getChildren())
    // if (c instanceof AliasNode)
    // aliases.add((AliasNode) c);
    // return aliases;
    // }
    //
    // @Override
    // public void setName(String name) {
    // getTLModelObject().setName(NodeNameUtils.fixBusinessObjectName(name));
    // updateNames(NodeNameUtils.fixBusinessObjectName(name));
    // }
    //
    // @Override
    // public void sort() {
    // getFacet_Summary().sort();
    // getFacet_Detail().sort();
    // for (ComponentNode f : getCustomFacets())
    // ((FacetOMNode) f).sort();
    // for (ComponentNode f : getQueryFacets())
    // ((FacetOMNode) f).sort();
    // }
    //
    // @Override
    // public void merge(Node source) {
    // if (!(source instanceof BusinessObjectNode)) {
    // throw new IllegalStateException("Can only merge objects with the same type");
    // }
    // BusinessObjectNode business = (BusinessObjectNode) source;
    // getFacet_ID().add(business.getFacet_ID().getProperties(), true);
    // getFacet_Summary().add(business.getFacet_Summary().getProperties(), true);
    // getFacet_Detail().add(business.getFacet_Detail().getProperties(), true);
    //
    // copyFacet(business.getContextualFacets(false));
    // getChildrenHandler().clear();
    // }
    //
    // private void copyFacet(List<AbstractContextualFacet> facets) {
    // // assert false;
    // // // FIXME
    // // }
    // //
    // // private void copyFacet(List<ComponentNode> facets) {
    // // FIXME
    // for (ComponentNode f : facets) {
    // FacetInterface facet = (FacetInterface) f;
    // if (!NodeUtils.checker((Node) facet).isInheritedFacet().get()) {
    // TLFacet tlFacet = (TLFacet) facet.getTLModelObject();
    // String name = "";
    // if (tlFacet instanceof TLContextualFacet)
    // name = ((TLContextualFacet) tlFacet).getName();
    // ComponentNode newFacet = addFacet(name, tlFacet.getFacetType());
    // ((FacetInterface) newFacet).add(facet.getProperties(), true);
    // }
    // }
    // }
    //
    // @Override
    // public boolean isMergeSupported() {
    // return true;
    // }
    //
    // // /////////////////////////////////////////////////////////////////
    // //
    // // Extension Owner implementations
    // //
    // @Override
    // public Node getExtensionBase() {
    // return extensionHandler != null ? extensionHandler.get() : null;
    // }
    //
    // @Override
    // public String getExtendsTypeNS() {
    // return getExtensionBase() != null ? getExtensionBase().getNamespace() : "";
    // }
    //
    // @Override
    // public void setExtension(final Node base) {
    // if (extensionHandler == null)
    // extensionHandler = new ExtensionHandler(this);
    // extensionHandler.set(base);
    // }
    //
    // @Override
    // public ExtensionHandler getExtensionHandler() {
    // return extensionHandler;
    // }
    //
}
