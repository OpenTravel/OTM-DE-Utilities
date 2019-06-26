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

import org.opentravel.dex.actions.DexActionManager;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * All owners of properties (elements, attributes, indicators...) must implement this interface.
 * 
 * @author dmh
 *
 */
public interface OtmPropertyOwner extends OtmChildrenOwner {

    /**
     * 
     * @return the library member that owns this property owner
     */
    @Override
    public OtmLibraryMember getOwningMember();

    /**
     * Add the OtmProperty to the owner if the owner does not already contain it. If the child reports it is inherited
     * (isInherited() = true) then it is added to inherited children.
     * <p>
     * Typically, the child will add itself when constructed with non-null parent.
     * 
     * @param child
     * @return
     */
    public OtmProperty<?> add(OtmProperty<?> child);

    /**
     * Add the passed TL property/attribute/indicator to this and then create OtmProperty
     * 
     * @return the new OtmProperty
     */
    public OtmProperty<?> add(TLModelElement newTL);

    /**
     * @return
     */
    @Override
    public DexActionManager getActionManager();

    /**
     * @return
     */
    @Override
    public boolean isInherited();

}
