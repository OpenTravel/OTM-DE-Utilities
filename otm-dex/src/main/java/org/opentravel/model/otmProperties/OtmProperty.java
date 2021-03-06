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

package org.opentravel.model.otmProperties;

import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import javafx.beans.property.StringProperty;

/**
 * Interface for all OTM properties.
 * 
 * @author Dave Hollander
 * 
 */
public interface OtmProperty extends OtmObject {


    /**
     * Copy content from property to this one.
     * 
     * @param oldProperty
     */
    public void clone(OtmProperty property);

    /**
     * @see DexActionManager#add(org.opentravel.dex.actions.DexActions, String, OtmObject)
     * @return FX string property with action handler for example value
     */
    public StringProperty exampleProperty();

    public String getName();

    public String getNamespace();

    public OtmLibraryMember getOwningMember();

    public OtmPropertyOwner getParent();

    /**
     * @return the property type enumeration for this property
     */
    public OtmPropertyType getPropertyType();

    public boolean isEditable();

    public boolean isInherited();

    /**
     * @return
     */
    public boolean isManditory();

    /**
     * @param value
     */
    public abstract void setManditory(boolean value);

    /**
     * Set the parent of the property.
     * 
     * @param parent
     * @return the parent
     */
    public OtmPropertyOwner setParent(OtmPropertyOwner parent);

    public String toString();

    /**
     * @return
     */
    public boolean isAssignedTypeInNamespace();
}
