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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.model.otmFacets.OtmRoleEnumeration;
import org.opentravel.schemacompiler.model.TLRole;

/**
 * @author dmh
 *
 */
public class OtmRoleValue extends OtmValueProperty implements OtmProperty {
    private static Log log = LogFactory.getLog( OtmRoleValue.class );

    private OtmRoleEnumeration parent;

    public OtmRoleValue(TLRole value, OtmRoleEnumeration parent) {
        super( value );
        this.parent = parent;
        if (parent != null)
            parent.add( this );
    }

    @Override
    public TLRole getTL() {
        return (TLRole) tlObject;
    }

    @Override
    public String setName(String name) {
        getTL().setName( name );
        nameProperty().set( getName() ); // may not fire otm name change listener
        isValid( true );
        return getName();
    }

    @Override
    public String getName() {
        return getTL().getName();
    }

    @Override
    public OtmRoleEnumeration getParent() {
        return parent;
    }

    @Override
    public void clone(OtmProperty property) {
        TLRole newTL = new TLRole();
        newTL.setName( getTL().getName() );
        new OtmRoleValue( newTL, getParent() );
    }
}
