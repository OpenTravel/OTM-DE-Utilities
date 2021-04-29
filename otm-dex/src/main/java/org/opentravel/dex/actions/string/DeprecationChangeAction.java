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
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

public class DeprecationChangeAction extends DexStringAction {
    // private static Log log = LogFactory.getLog( DeprecationChangeAction.class );

    public static boolean isEnabled(OtmObject subject) {
        return subject.isEditable() && subject.getLibrary().isChainEditable();
    }

    // New OTM created in a minor version of the library
    private OtmLibraryMember newLibraryMember = null;

    public DeprecationChangeAction() {
        // Constructor for reflection
    }

    @Override
    protected String get() {
        return otm.getDeprecation();
    }

    @Override
    protected void set(String value) {
        // Create a minor version if the subject is in an older library in editable chain
        //
        OtmLibrary subjectLibrary = otm.getLibrary();
        if (subjectLibrary != null && !subjectLibrary.isEditable() && subjectLibrary.isChainEditable()) {
            // Get the latest library in the chain that is editable
            newLibraryMember =
                subjectLibrary.getVersionChain().getNewMinorLibraryMember( getSubject().getOwningMember() );
            if (newLibraryMember == null)
                return;
            otm = newLibraryMember;
            // FIXME - undo
        }

        otm.setDeprecation( value );
    }


    @Override
    public boolean setSubject(OtmObject subject) {
        otm = subject;
        return true;
    }

    @Override
    public String toString() {
        return "Changed deprecation to " + newString;
    }

}
