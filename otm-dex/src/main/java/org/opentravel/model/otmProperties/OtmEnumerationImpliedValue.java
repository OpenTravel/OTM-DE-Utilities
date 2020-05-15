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

import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmFacets.OtmEnumerationOtherFacet;
import org.opentravel.schemacompiler.model.TLEnumValue;

import javafx.scene.control.Tooltip;

/**
 * The open enumeration has an implied "Other" value. It does not have a TL level equivalent.
 * 
 * @author dmh
 *
 */
public class OtmEnumerationImpliedValue extends OtmValueProperty {

    // private static Log log = LogFactory.getLog( OtmEnumerationImpliedValue.class );

    private OtmPropertyOwner parent;
    private static final String OTHER = "Other_";

    public OtmEnumerationImpliedValue(OtmEnumerationOtherFacet parent) {
        super( new TLEnumValue() ); // this TL value will never be added to TLModel.
        this.parent = parent;
        if (parent != null)
            parent.add( this );
    }

    @Override
    public TLEnumValue getTL() {
        return (TLEnumValue) tlObject;
    }

    @Override
    public String setName(String name) {
        return getName();
    }

    @Override
    public String getName() {
        return OTHER;
    }

    @Override
    public OtmPropertyOwner getParent() {
        return parent;
    }

    @Override
    public boolean isInherited() {
        return false;
    }

    /**
     * {@inheritDoc} Literal on open enum facet is not editable
     */
    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public OtmPropertyOwner setParent(OtmPropertyOwner parent) {
        return parent;
    }

    @Override
    public Tooltip getTooltip() {
        return new Tooltip( "Other value built into an open enumeration" );
    }

    @Override
    public String getValidationFindingsAsString() {
        return "";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isValid(boolean refresh) {
        return true;
    }

    @Override
    public void clone(OtmProperty property) {
        // NO-OP
    }
}
