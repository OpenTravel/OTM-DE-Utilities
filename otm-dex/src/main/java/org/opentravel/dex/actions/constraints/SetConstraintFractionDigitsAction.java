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
import org.opentravel.dex.actions.DexRunAction;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObject;
import org.opentravel.schemacompiler.model.TLSimple;

/**
 * Set the repeat count on a property.
 */
public class SetConstraintFractionDigitsAction extends DexRunAction {
    // private static Logger log = LogManager.getLogger( SetRepeatCountAction.class );

    private static final String VETO1 =
        "org.opentravel.schemacompiler.TLSimple.constraintFacet.RESTRICTION_NOT_APPLICABLE";
    private static final String VETO2 =
        "org.opentravel.schemacompiler.TLSimple.fractionDigits.MUST_BE_LESS_THAN_OR_EQUAL";
    private static final String[] VETOKEYS = {VETO1, VETO2};

    private static final int TESTINT = 207;


    /**
     * @param subject
     * @return true if editable property
     */
    public static boolean isEnabled(OtmObject subject) {
        if (!(subject instanceof OtmSimpleObject) || !subject.isEditable())
            return false;
        // Force change and see it is vetoed
        int savedValue = ((TLSimple) subject.getTL()).getFractionDigits();
        ((OtmSimpleObject) subject).getTL().setFractionDigits( TESTINT );
        boolean veto = ValidationUtils.isVetoed( VETOKEYS, subject );
        ((OtmSimpleObject) subject).getTL().setFractionDigits( savedValue );
        subject.isValid( true ); // fix the findings

        // log.debug( "Is " + subject + " vetoed for fraction digits? " + veto + " "
        // + ((TLSimple) subject.getTL()).getFractionDigits() );
        return !veto;

    }

    public static boolean isEnabled(OtmObject subject, OtmObject value) {
        return isEnabled( subject );
    }

    public static int get(OtmObject otm) {
        return otm instanceof OtmSimpleObject ? ((OtmSimpleObject) otm).getTL().getFractionDigits() : 0;
    }

    private int oldCount = 0;

    public SetConstraintFractionDigitsAction() {
        // Constructor for reflection
    }


    /**
     * {@inheritDoc} Set the library in the library member.
     * 
     * @return
     */
    @Override
    public Object doIt(Object data) {
        if (otm instanceof OtmSimpleObject && data instanceof Integer)
            return doIt( ((int) data) );
        return null;
    }

    /**
     * Add the member to the model and clear its no-library action
     * 
     * @param library
     * @return
     */
    public Integer doIt(int value) {
        oldCount = getSubject().getTL().getFractionDigits();
        getSubject().getTL().setFractionDigits( value );
        return get();
    }

    /**
     * Return the new member or null if none created.
     * 
     * @see org.opentravel.dex.actions.DexRunAction#get()
     */
    @Override
    public Integer get() {
        return getSubject().getTL().getFractionDigits();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (isEnabled( subject )) {
            otm = subject;
            return true;
        }
        return false;
    }

    @Override
    public OtmSimpleObject getSubject() {
        return (OtmSimpleObject) otm;
    }

    @Override
    public String toString() {
        return "Set repeat count to: " + get();
    }

    @Override
    public Integer undoIt() {
        getSubject().getTL().setFractionDigits( oldCount );
        return get();
    }
}
