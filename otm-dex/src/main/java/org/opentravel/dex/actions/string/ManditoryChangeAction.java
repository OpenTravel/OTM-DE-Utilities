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
import org.opentravel.model.otmProperties.OtmValueProperty;

public class ManditoryChangeAction extends DexStringAction {
    // private static Logger log = LogManager.getLogger( DescriptionChangeAction.class );
    public static final String REQUIRED = "Required";
    public static final String OPTIONAL = "Optional";

    public static boolean isEnabled(OtmObject subject) {
        return subject instanceof OtmProperty && subject.isEditable();
    }

    public static String getCurrent(OtmObject obj) {
        String value = "";
        if (obj instanceof OtmValueProperty)
            value = "";
        else if (obj instanceof OtmProperty) {
            value = OPTIONAL;
            if (((OtmProperty) obj).isManditory())
                value = REQUIRED;
        }
        return value;
    }

    public ManditoryChangeAction() {
        // Constructor for reflection
    }

    @Override
    protected String get() {
        return getCurrent( otm );
    }

    @Override
    protected void set(String value) {
        if (otm instanceof OtmProperty) {
            ((OtmProperty) otm).setManditory( value.equals( REQUIRED ) );
        }
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
        return "Changed manditory to " + newString;
    }

}
