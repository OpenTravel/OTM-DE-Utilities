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

import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmVersionChain;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLFacet;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javafx.beans.property.StringProperty;

/**
 * Interface implemented by all library members, including complex objects and contextual facets.
 * 
 * @author dmh
 *
 */
public interface OtmLibraryMember extends OtmChildrenOwner {

    /**
     * Add this facet alias to the appropriate TLAliasOwner
     * 
     * @param tla must be an alias on a tlFacet or a library member that is an alias owner.
     */
    public void addAlias(TLAlias tla);

    /**
     * Create new FX read only property containing the base type name.
     * 
     * @return new FX observable property containing base type name.
     */
    public StringProperty baseTypeProperty();

    /**
     * Build out the default configuration of a new library member.
     */
    public void build();

    /**
     * Add and remove the users library member to the where used list
     * 
     * @param userToRemove from where used list, can be null
     * @param userToAdd to the where used list, can be null
     */
    void changeWhereUsed(OtmLibraryMember userToRemove, OtmLibraryMember userToAdd);

    /**
     * @param o
     * @return true if object is direct child of member
     */
    boolean contains(OtmObject o);

    /**
     * Make and return a full copy of this library member. New member will not be in a library.
     * 
     * @return copy of member or null if it can not be copied
     */
    public OtmLibraryMember copy();

    /**
     * Create a minor version of this library member.
     * 
     * @param minorLibrary
     * @return new library member or null
     */
    OtmLibraryMember createMinorVersion(OtmLibrary minorLibrary);

    /**
     * Get the object that is extended by this object -- its base type.
     * 
     * @return an OtmObject that is extended by this object or null.
     */
    public OtmObject getBaseType();

    /**
     * @return the name of the base type object ({@link #getBaseType()}) or empty string
     */
    String getBaseTypeName();

    /**
     * @return new list of contributed facets or empty list
     */
    public Collection<OtmContributedFacet> getChildrenContributedFacets();

    /**
     * Get the facet of the specified type.
     * <p>
     * 
     * @param facet is the type of facet to match. Can't be contextual because members can have multiple contextual
     *        facets;
     * @return facet or null
     */
    OtmFacet<TLFacet> getFacet(OtmFacet<TLFacet> facet);

    /**
     * @return the name of the owning TL Library
     */
    public String getLibraryName();

    /**
     * Get a descendant type provider of the same class as the passed provider.
     * 
     * @param provider
     * @return match or null
     */
    public OtmTypeProvider getMatchingProvider(OtmTypeProvider provider);

    /**
     * Get the actual type users that use this library member or descendants as assigned type.
     * 
     * @return new map of user -> provider
     */
    Map<OtmTypeUser,OtmTypeProvider> getPropertiesWhereUsed();

    /**
     * TLContextualFacet or TLLibraryMember
     * <p>
     * TLLibaryMember and TLContextualFacet extend LibraryMember. Both are OtmLibraryMembers. This convenience method
     * makes it easy to get a LibraryMember regardless of which TL type hierarchy it belongs.
     * 
     * @return the TLObject cast to LibraryMember
     */
    public LibraryMember getTlLM();

    /**
     * @param provider
     * @return list of type user descendants assigned to the provider, or an empty list
     */
    public List<OtmTypeUser> getTypeUsers(OtmTypeProvider provider);

    /**
     * @return non-null, sorted list of type providers used by all descendants of this member.
     */
    public List<OtmTypeProvider> getUsedTypes();

    /**
     * @return number of type users for this member and its descendants.
     */
    public int getUsedTypesCount();

    /**
     * Get all type users and base-type extensions of this member.
     * <p>
     * Get all members that contain type users that use this member or any of its descendants as assigned types.
     * <p>
     * List includes all extensions of this object.
     *
     * @return list of where used or empty list
     */
    public List<OtmLibraryMember> getWhereUsed();

    /**
     * Get the lazy evaluated where used list.
     * <p>
     * When <i>forced</i> by type resolver or list is null, it will get users of the library member and all its type
     * provider descendants.
     * 
     * @param force will clear list and recompute users to add to existing list
     * @return
     */
    List<OtmLibraryMember> getWhereUsed(boolean force);

    /**
     * Is this member editable as a minor version See {@link #isEditable()}
     * 
     * @return true if member is in editable library or is latest in an editable chain
     */
    boolean isEditableMinor();

    /**
     * Is this member the latest in the version chain of its library?
     * <p>
     * 4/27/2021 - returns false if unmanaged (see {@link OtmVersionChain#isLatestChain()})
     * 
     * @return
     */
    boolean isLatestVersion();

    // /**
    // * Update or clear any cached values (string properties).
    // */
    // public void refresh();

    /**
     * @return a new FX property for library name
     */
    public StringProperty libraryProperty();

    /**
     * @return fx property for library prefix
     */
    public StringProperty prefixProperty();

    /**
     * @param otherMember
     * @return true if the namespace base is same
     */
    public boolean sameBaseNamespace(OtmLibraryMember otherMember);

    /**
     * Set the base type if supported.
     * 
     * @param baseObject
     * @return the object assigned as base type or null
     */
    public abstract OtmObject setBaseType(OtmObject baseObject);

    /**
     * Set or clear action manager that overrides action manager from library. Will only be returned by
     * {@link #getActionManager()} when library is null. Should only be used when member is being worked on in a wizard
     * or task.
     * 
     * @param actionManager
     */
    public void setNoLibraryActionManager(DexActionManager dexActionManager);

    /**
     * @return new FX string property containing library's version number
     */
    public StringProperty versionProperty();


}
