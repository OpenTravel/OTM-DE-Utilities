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

package org.opentravel.dex.actions.resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.dex.actions.DexBooleanAction;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmResource;

public class SetFirstClassAction extends DexBooleanAction {
    private static Logger log = LogManager.getLogger( SetFirstClassAction.class );

    /**
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        if (subject instanceof OtmResource)
            return subject.isEditable();
        return false;
    }

    public SetFirstClassAction() {
        // actionType = DexActions.SETFIRSTCLASS;
    }

    public boolean get() {
        return getSubject().isFirstClass();
    }

    @Override
    public OtmResource getSubject() {
        return (OtmResource) otm;
    }

    public void set(boolean value) {
        getSubject().setFirstClass( value );
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (!(subject instanceof OtmResource))
            return false;
        otm = subject;
        return true;
    }

    @Override
    public String toString() {
        return "First class set to " + get();
    }
}
