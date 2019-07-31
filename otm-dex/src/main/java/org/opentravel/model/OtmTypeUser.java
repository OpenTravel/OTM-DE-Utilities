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

import org.opentravel.schemacompiler.model.NamedEntity;

import javafx.beans.property.StringProperty;

/**
 * OtmTypeUser is an interface and utility class. It is not part of the type hierarchy. Therefore, it has several method
 * declarations that duplicate other interfaces.
 * 
 * @author dmh
 *
 */
public interface OtmTypeUser extends OtmObject {

    /**
     * FX Property with the type name. Adds prefix if the owner and type are in different libraries.
     * 
     * @return
     */
    public StringProperty assignedTypeProperty();

    public NamedEntity getAssignedTLType();

    /**
     * Get the assigned type from the listener on the assigned TL Type.
     * 
     * @return
     */
    public OtmTypeProvider getAssignedType();

    /**
     * Get the "typeName" field from the TL object. Should only be used as last resort if Otm and TL objects are not
     * available.
     */
    public String getTlAssignedTypeName();

    public NamedEntity setAssignedTLType(NamedEntity type);

    /**
     * Set the type assigned to this type user.
     * 
     * @param type
     * @return the type
     */
    public OtmTypeProvider setAssignedType(OtmTypeProvider type);

    /**
     * Should only be used as last resort if Otm and TL objects are not available. Sometimes, only the name is known
     * because the tl model does not have the type loaded.
     * <p>
     * <b>Warning:</b> The type is set to null so the name will be used in the compiler.
     * 
     * @param oldTLTypeName
     */
    public void setTLTypeName(String name);
}
