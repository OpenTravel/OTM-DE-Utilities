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
import org.opentravel.dex.actions.string.DexStringAction;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObject;
import org.opentravel.schemacompiler.model.TLSimple;

public class SetConstraintPatternAction extends DexStringAction {
    // private static Log log = LogFactory.getLog( DescriptionChangeAction.class );

    private static final String VETO1 = "org.opentravel.schemacompiler.TLSimple.pattern.MUST_BE_NULL_OR_BLANK";
    private static final String[] VETOKEYS = {VETO1};
    private static final String TESTSTRING = "a-x";

    private OtmSimpleObject simple;

    /**
     * May be used as facet for:xs:anyURI, xs:base64Binary, xs:boolean, xs:byte, xs:date, xs:dateTime, xs:decimal,
     * xs:double, xs:duration, xs:ENTITY, xs:float, xs:gDay, xs:gMonth, xs:gMonthDay, xs:gYear, xs:gYearMonth,
     * xs:hexBinary, xs:ID, xs:IDREF, xs:int, xs:integer, xs:language, xs:long, xs:Name, xs:NCName, xs:negativeInteger,
     * xs:NMTOKEN, xs:nonNegativeInteger, xs:nonPositiveInteger, xs:normalizedString, xs:NOTATION, xs:positiveInteger,
     * xs:QName, xs:short, xs:string, xs:time, xs:token, xs:unsignedByte, xs:unsignedInt, xs:unsignedLong,
     * xs:unsignedShort
     * 
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        if (!subject.isEditable() || !(subject instanceof OtmSimpleObject) || !(subject.getTL() instanceof TLSimple))
            return false;

        // Force change and see it is vetoed
        String savedValue = ((TLSimple) subject.getTL()).getPattern();
        ((OtmSimpleObject) subject).getTL().setPattern( TESTSTRING );
        boolean veto = ValidationUtils.isVetoed( VETOKEYS, subject );
        ((OtmSimpleObject) subject).getTL().setPattern( savedValue );
        subject.isValid( true ); // fix the findings

        return !veto;
    }

    public SetConstraintPatternAction() {
        // Constructor for reflection
    }

    @Override
    public String get() {
        return simple.getTL().getPattern();
    }

    @Override
    public void set(String value) {
        simple.getTL().setPattern( value );
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
        return "Set pattern to " + get();
    }

}
