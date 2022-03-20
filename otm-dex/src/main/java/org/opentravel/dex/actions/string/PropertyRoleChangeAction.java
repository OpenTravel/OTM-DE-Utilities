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

package org.opentravel.dex.actions.string;

import org.opentravel.model.OtmObject;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyType;

public class PropertyRoleChangeAction extends DexStringAction {
    // private static Logger log = LogManager.getLogger( DescriptionChangeAction.class );

    private OtmProperty oldProperty = null;
    private OtmProperty newProperty = null;

    public static boolean isEnabled(OtmObject subject) {
        return subject instanceof OtmProperty && subject.isEditable();
    }

    /**
     * 
     * @param obj
     * @return role label for properties, empty string for others
     */
    public static String getCurrent(OtmObject obj) {
        String value = "";
        if (obj instanceof OtmProperty)
            value = ((OtmProperty) obj).getPropertyType().label();
        return value;
    }

    public PropertyRoleChangeAction() {
        // Constructor for reflection
    }

    @Override
    protected String get() {
        return getCurrent( otm );
    }

    @Override
    public void set(String value) {
        if (oldProperty == null)
            change( value );
        else
            undo();
    }

    public OtmProperty change(String value) {
        OtmPropertyType type = OtmPropertyType.getType( value );
        if (type != null && getSubject() instanceof OtmProperty && getSubject().getPropertyType() != type) {
            oldProperty = getSubject();
            newProperty = OtmPropertyType.build( type, oldProperty.getParent() );
            if (newProperty != null) {
                newProperty.clone( oldProperty );
                oldProperty.getParent().delete( oldProperty );
                otm = newProperty;
            }
        }
        return newProperty;
    }

    public OtmProperty undo() {
        if (newProperty != null && oldProperty != null) {
            newProperty.getParent().add( oldProperty.getTL() );
            newProperty.getParent().delete( newProperty );
        }
        return oldProperty;
    }

    @Override
    public OtmProperty getSubject() {
        return (OtmProperty) otm;
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (subject instanceof OtmProperty) {
            otm = subject;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Changed property role to " + newString;
    }

}
