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

import org.opentravel.dex.actions.DexBooleanAction;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmResource;

public class SetAbstractAction extends DexBooleanAction {
    // private static Log log = LogFactory.getLog( SetAbstractAction.class );

    /**
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        if (subject instanceof OtmResource)
            return subject.isEditable();
        return false;
    }

    public SetAbstractAction() {
        // actionType = DexActions.SETABSTRACT;
    }

    public boolean get() {
        return getSubject().isAbstract();
    }

    @Override
    public OtmResource getSubject() {
        return (OtmResource) otm;
    }

    public void set(boolean value) {
        getSubject().setAbstract( value );
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
        return "Abstract set to " + get();
    }

}
