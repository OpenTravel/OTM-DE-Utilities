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
import org.opentravel.model.resource.OtmParentRef;

public class SetParentRefParentAction extends DexStringAction {
    // private static Log log = LogFactory.getLog( SetAbstractAction.class );

    public SetParentRefParentAction(OtmParentRef otm) {
        super( otm );
    }

    @Override
    public OtmParentRef getSubject() {
        return (OtmParentRef) otm;
    }

    protected void set(String value) {
        getSubject().setParentResource( value );
    }

    protected String get() {
        return getSubject().getParentResourceName();
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
        if (subject instanceof OtmParentRef)
            return subject.isEditable();
        return false;
    }

}
