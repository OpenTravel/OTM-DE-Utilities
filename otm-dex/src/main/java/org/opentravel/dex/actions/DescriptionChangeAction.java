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

public class DescriptionChangeAction extends DexStringAction {
    // private static Log log = LogFactory.getLog( DescriptionChangeAction.class );

    public static boolean isEnabled(OtmObject subject) {
        return subject.isEditable();
    }

    public DescriptionChangeAction() {
        // actionType = DexActions.DESCRIPTIONCHANGE;
    }

    @Override
    protected String get() {
        return otm.getDescription();
    }

    @Override
    protected void set(String value) {
        otm.setDescription( value );
    }


    @Override
    public boolean setSubject(OtmObject subject) {
        otm = subject;
        return true;
    }

    @Override
    public String toString() {
        return "Changed description to " + newString;
    }

}
