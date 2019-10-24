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

package org.opentravel.model;

import org.opentravel.model.otmProperties.OtmProperty;

import java.util.Collection;
import java.util.List;

/**
 * All owners of children must implement this interface.
 * 
 * @author dmh
 *
 */
public interface OtmChildrenOwner extends OtmObject {

    /**
     * Safely add the OtmObject to the owner if can be owned by the owner. Make sure the owner does not already contain
     * it. If the child reports it is inherited (isInherited() = true) then it is added to inherited children.
     * <p>
     * Does <b>not</b> change the underlying TL object. Does <b>not</b> change the added object.
     * <p>
     * Typically, the child will add itself when constructed unless parent is null.
     * 
     * @param child
     * @return
     */
    public OtmObject add(OtmObject child);

    /**
     * Delete the child from facade and underlying TL object. See also: {@link #remove(OtmProperty)}
     * 
     * @param child
     */
    public void delete(OtmObject property);

    /**
     * Simply remove the child from the children or inherited children list. See also: {@link #delete(OtmProperty)}
     * 
     * @param child
     */
    public void remove(OtmObject property);

    /**
     * Get list of all the children of this object. To allow lazy evaluation, implementations are expected to attempt to
     * model the children if the list is empty.
     * 
     * @return live list of children or empty list.
     */
    public List<OtmObject> getChildren();

    /**
     * Get a list of all the inherited children of this object. Because this list can not be inflated until after the
     * model is loaded, implementations must allow lazy evaluation--implementations are expected to attempt to model the
     * children if the list is null or empty.
     * 
     * @return list of inherited children or empty list.
     */
    public List<OtmObject> getInheritedChildren();

    /**
     * Get a list of children organized by inheritance. For example, a business object will only report out the ID facet
     * and the ID facet will include the summary facet in this list.
     * <p>
     * To allow lazy evaluation, implementations are expected to attempt to model the children if the list is empty.
     * <p>
     * Used in views such as Properties tab's view to give modeler a clearer understanding of target object structures.
     * 
     * @return list of children or empty list.
     */
    public Collection<OtmObject> getChildrenHierarchy();

    /**
     * Get a list of children that are type providers.
     * 
     * @return new list of children or empty list.
     */
    public Collection<OtmTypeProvider> getChildrenTypeProviders();

    /**
     * @return new list of all descendants that are children owners or empty list, never null
     */
    public Collection<OtmChildrenOwner> getDescendantsChildrenOwners();

    /**
     * Get a list of children and their descendants that are type providers.
     * 
     * @return list of children or empty list.
     */
    public Collection<OtmTypeProvider> getDescendantsTypeProviders();

    /**
     * @return
     */
    public Collection<OtmTypeUser> getDescendantsTypeUsers();

    /**
     * Model the children of this object from its' tlObject(s).
     */
    public void modelChildren();

    /**
     * Model the inherited children of this object from its' tlObject->extension tlObject.
     */
    public void modelInheritedChildren();

    /**
     * Should this owner be displayed with its children visible?
     * 
     * @return true if children should be visible.
     */
    public boolean isExpanded();
}
