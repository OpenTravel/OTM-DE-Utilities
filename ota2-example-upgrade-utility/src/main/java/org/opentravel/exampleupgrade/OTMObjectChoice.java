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

package org.opentravel.exampleupgrade;

import org.opentravel.schemacompiler.model.NamedEntity;

import javax.xml.namespace.QName;

/**
 * Wrapper class that encapsulates an OTM entity and its global XML element name as a selectable item in the visual
 * display.
 */
public class OTMObjectChoice {

    private NamedEntity otmObject;
    private QName otmObjectName;
    private String displayName;

    /**
     * Constructor that provides the OTM object.
     * 
     * @param otmObject the OTM object instance
     * @param otmObjectName the global element name of the OTM object
     */
    public OTMObjectChoice(NamedEntity otmObject, QName otmObjectName) {
        String prefix = otmObject.getOwningLibrary().getPrefix();

        this.otmObject = otmObject;
        this.otmObjectName = otmObjectName;
        this.displayName = ((prefix == null) ? "" : (prefix + ":")) + otmObjectName.getLocalPart();
    }

    /**
     * Returns the OTM object instance.
     *
     * @return NamedEntity
     */
    public NamedEntity getOtmObject() {
        return otmObject;
    }

    /**
     * Returns the global element name of the OTM object.
     *
     * @return QName
     */
    public QName getOtmObjectName() {
        return otmObjectName;
    }

    /**
     * Returns the display name for the OTM object in the combo-box.
     *
     * @return String
     */
    public String toString() {
        return displayName;
    }

}
