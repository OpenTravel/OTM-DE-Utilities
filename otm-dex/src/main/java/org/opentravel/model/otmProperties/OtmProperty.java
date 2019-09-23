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

import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

/**
 * Abstract base class for all OTM properties.
 * 
 * @author Dave Hollander
 * 
 */
public interface OtmProperty extends OtmObject {


    public String getName();

    public String getNamespace();

    public OtmLibraryMember getOwningMember();

    public OtmPropertyOwner getParent();

    public OtmPropertyType getPropertyType();

    public String getRole();

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

    public String toString();

    /**
     * Copy content from property to this one.
     * 
     * @param oldProperty
     */
    public void clone(OtmProperty property);
}
