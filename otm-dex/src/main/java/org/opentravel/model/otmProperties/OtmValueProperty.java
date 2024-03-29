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

import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;

import javafx.beans.property.StringProperty;

/**
 * Abstract base class for properties that are literals for enumeration or role values.
 * 
 * @author dmh
 *
 */
public abstract class OtmValueProperty extends OtmModelElement<TLModelElement> implements OtmProperty {
    // private static Logger log = LogManager.getLogger( OtmValueProperty.class );

    public OtmValueProperty(TLModelElement tl) {
        super( tl );
    }

    @Override
    public StringProperty exampleProperty() {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * No rules applied
     */
    @Override
    public String fixName(String name) {
        return name;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.ENUMERATION_VALUE;
    }

    @Override
    public OtmLibraryMember getOwningMember() {
        return getParent() != null ? getParent().getOwningMember() : null;
    }

    @Override
    public OtmPropertyType getPropertyType() {
        return OtmPropertyType.ENUMVALUE;
    }

    @Override
    public boolean isManditory() {
        return false;
    }

    @Override
    public void setManditory(boolean value) {
        // No-op
    }

    @Override
    public OtmPropertyOwner setParent(OtmPropertyOwner parent) {
        // NO-OP
        return null;
    }
}
