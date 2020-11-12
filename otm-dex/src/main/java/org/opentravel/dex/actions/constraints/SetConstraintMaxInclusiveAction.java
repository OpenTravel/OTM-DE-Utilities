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

package org.opentravel.dex.actions.constraints;

import org.opentravel.common.ValidationUtils;
import org.opentravel.dex.actions.DexStringAction;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObject;
import org.opentravel.schemacompiler.model.TLSimple;

public class SetConstraintMaxInclusiveAction extends DexStringAction {
    // private static Log log = LogFactory.getLog( DescriptionChangeAction.class );

    private static final String VETO1 = "org.opentravel.schemacompiler.TLProperty.name.INVALID_RESTRICTION";
    private static final String VETO2 = "org.opentravel.schemacompiler.TLSimple.maxInclusive.MUST_BE_NULL_OR_BLANK";
    private static final String VETO3 = "org.opentravel.schemacompiler.TLProperty.name.RESTRICTION_NOT_APPLICABLE";
    private static final String[] VETOKEYS = {VETO1, VETO2, VETO3};
    private static final String TESTSTRING = "MAXIzzzaaazzzaaaqwert";

    private OtmSimpleObject simple;

    public static boolean isEnabled(OtmObject subject) {
        if (!subject.isEditable() || !(subject instanceof OtmSimpleObject) || !(subject.getTL() instanceof TLSimple))
            return false;

        // Force change and see it is vetoed
        String savedValue = ((TLSimple) subject.getTL()).getMaxInclusive();
        ((OtmSimpleObject) subject).getTL().setMaxInclusive( TESTSTRING );
        boolean veto = ValidationUtils.isVetoed( VETOKEYS, subject );
        ((OtmSimpleObject) subject).getTL().setMaxInclusive( savedValue );
        subject.isValid( true ); // fix the findings

        return !veto;
    }

    public SetConstraintMaxInclusiveAction() {
        // Constructor for reflection
    }

    @Override
    protected String get() {
        return simple.getTL().getMaxInclusive();
    }

    @Override
    protected void set(String value) {
        simple.getTL().setMaxInclusive( value );
    }


    @Override
    public boolean setSubject(OtmObject subject) {
        if (subject instanceof OtmSimpleObject) {
            simple = (OtmSimpleObject) subject;
            otm = simple;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Set Max Inclusive to " + get();
    }

}
