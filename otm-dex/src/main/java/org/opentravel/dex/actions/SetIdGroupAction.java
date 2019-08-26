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

package org.opentravel.dex.actions;

import org.opentravel.model.OtmObject;
import org.opentravel.model.resource.OtmParameterGroup;

public class SetIdGroupAction extends DexBooleanAction {
    // private static Log log = LogFactory.getLog( SetAbstractAction.class );

    public SetIdGroupAction(OtmParameterGroup otm) {
        super( otm );
    }

    @Override
    public OtmParameterGroup getSubject() {
        return (OtmParameterGroup) otm;
    }

    protected void set(boolean value) {
        getSubject().setIdGroup( value );
    }

    protected boolean get() {
        return getSubject().isIdGroup();
    }



    @Override
    public String toString() {
        return "ID Group set to " + get();
    }

    /**
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        if (subject instanceof OtmParameterGroup)
            return subject.isEditable();
        return false;
    }

}
