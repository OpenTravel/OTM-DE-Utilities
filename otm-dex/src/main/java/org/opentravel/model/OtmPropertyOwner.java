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

import org.opentravel.dex.action.manager.DexActionManager;
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
     * Add the tl object (TLAttribute, TLIndicator or TLProperty) to this facade's underlying TLFacet. Then If the TL
     * object has a facade, add it to this parent; if not, create a OtmProperty to wrap the TL property setting this as
     * the parent.
     * <p>
     * To simply add to this facade, use {@link #add(OtmObject)}
     */
    public OtmProperty add(TLModelElement tlChild);

    /**
     * Return true if this owner can add properties of the passed type.
     * 
     * @param property
     * @return
     */
    public boolean canAdd(OtmProperty property);

    /**
     * {@inheritDoc} Property Owner is needed in member property row factory
     */
    @Override
    public DexActionManager getActionManager();

    /**
     * 
     * @return the library member that owns this property owner
     */
    @Override
    public OtmLibraryMember getOwningMember();

    /**
     * @return
     */
    @Override
    public boolean isInherited();

}
