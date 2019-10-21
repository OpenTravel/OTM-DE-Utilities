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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ValidationUtils;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

public class NameChangeAction extends DexStringAction {
    private static Log log = LogFactory.getLog( NameChangeAction.class );

    private static final String VETO1 = "org.opentravel.schemacompiler.TLProperty.name.ELEMENT_REF_NAME_MISMATCH";
    // private static final String VETO2 = "org.opentravel.schemacompiler.TLAttribute.name.INVALID_REFERENCE_NAME";
    private static final String VETO2 = "";
    private static final String VETO3 = "org.opentravel.schemacompiler.TLProperty.name.PATTERN_MISMATCH";
    private static final String[] VETOKEYS = {VETO1, VETO2, VETO3};

    public static boolean isEnabled(OtmObject subject) {
        if (subject == null || !subject.isRenameable())
            return false;
        if (subject instanceof OtmTypeUser && ((OtmTypeUser) subject).getAssignedType() != null
            && ((OtmTypeUser) subject).getAssignedType().isNameControlled())
            return false;
        return subject.isEditable();
    }

    public NameChangeAction() {
        // constructor for reflection
    }

    @Override
    protected String get() {
        return otm.getName();
    }

    @Override
    protected void set(String value) {
        otm.setName( value );
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        otm = subject;
        return true;
    }


    @Override
    public String doIt(ObservableValue<? extends String> o, String oldName, String name) {
        log.debug( "Ready to set name to " + name + "  from: " + oldName + " on: " + otm.getClass().getSimpleName()
            + " " + ignore );

        // Apply naming rules
        String modifiedName = name;
        if (otm instanceof OtmObject)
            modifiedName = otm.fixName( name );

        super.doIt( o, oldName, modifiedName );

        // Status is quickly overriden, use warning when user settings created

        return get();
    }

    @Override
    public String undoIt() {
        ignore = true;
        super.undoIt();

        if (!isValid()) {
            // You will get a loop if the old name is not valid!
            otm.setName( "" );
            if (observable instanceof SimpleStringProperty)
                ((SimpleStringProperty) observable).set( "" );
        }
        ignore = false;
        return otm.getName();
    }

    @Override
    public ValidationFindings getVetoFindings() {
        return ValidationUtils.getRelevantFindings( VETOKEYS, otm.getFindings() );
    }

    @Override
    public boolean isValid() {
        if (otm == null)
            return false;

        // Validate the parent - naming could change validation status
        if (otm.getOwningMember() != null)
            otm.getOwningMember().isValid( true );
        return otm.isValid( true ) ? true
            : ValidationUtils.getRelevantFindings( VETOKEYS, otm.getFindings() ).isEmpty();
    }

    @Override
    public String toString() {
        return "Changed name from " + oldString + " to " + newString;
    }

}
