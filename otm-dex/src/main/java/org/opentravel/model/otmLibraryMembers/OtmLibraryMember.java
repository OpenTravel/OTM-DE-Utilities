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
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAlias;

import java.util.List;

import javafx.beans.property.StringProperty;

/**
 * Interface implemented by all library members, including complex objects and contextual facets.
 * 
 * @author dmh
 *
 */
public interface OtmLibraryMember extends OtmChildrenOwner {

    /**
     * Add this facet alias to the appropriate alias
     * 
     * @param tla must be an alias on a tlFacet
     */
    public void addAlias(TLAlias tla);

    /**
     * @return
     */
    public StringProperty baseTypeProperty();

    /**
     * @param o
     * @return true if object is direct child of member
     */
    boolean contains(OtmObject o);

    /**
     * Get the object that is extended by this object -- its base type.
     * 
     * @return an OtmObject that is extended by this object or null.
     */
    public OtmObject getBaseType();

    public String getLibraryName();

    /**
     * @return
     */
    @Override
    public String getObjectTypeName();

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
     * @return non-null, sorted list of type providers used by all descendants of this member.
     */
    public List<OtmTypeProvider> getUsedTypes();

    /**
     * Get all members that contain type users that use this member or any of its descendants as assigned types.
     * 
     * @return list of where used or empty list
     */
    public List<OtmLibraryMember> getWhereUsed();

    /**
     * @return fx property for library name
     */
    public StringProperty libraryProperty();

    /**
     * @return fx property for library prefix
     */
    public StringProperty prefixProperty();

    public StringProperty versionProperty();

    /**
     * @return the model manager for this object
     */
    OtmModelManager getModelManager();

    /**
     * Add the user library member to the where used list
     * 
     * @param oldUser to remove from where used list
     * @param newUser to add to the where used list
     */
    void addWhereUsed(OtmLibraryMember oldUser, OtmLibraryMember newUser);

    /**
     * Set or clear action manager that overrides action manager from library. Will only be returned by
     * {@link #getActionManager()} when library is null. Should only be used when member is being worked on in a wizard
     * or task.
     * 
     * @param actionManager
     */
    public void setNoLibraryActionManager(DexActionManager dexActionManager);


}
