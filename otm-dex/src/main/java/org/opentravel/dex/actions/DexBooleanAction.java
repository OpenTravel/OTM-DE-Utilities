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
import org.opentravel.model.OtmObject;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

/**
 * Actions on boolean values.
 * 
 * @author dmh
 *
 */
public abstract class DexBooleanAction extends DexActionBase implements DexAction<Boolean> {
    private static Log log = LogFactory.getLog( DexBooleanAction.class );

    protected ObservableValue<? extends Boolean> observable;

    /**
     * @param otm
     */
    public DexBooleanAction(OtmObject otm) {
        super( otm );
    }

    /**
     * Simply set the object's string field value
     * 
     * @param value
     */
    protected abstract void set(boolean value);

    /**
     * Simply get the object's string field value
     */
    protected abstract boolean get();

    @Override
    public void doIt(Object name) {
        // NO-OP
    }

    @Override
    public boolean isEnabled() {
        return getSubject().isEditable();
    }

    @Override
    public boolean isAllowed(Boolean value) {
        return getSubject().isEditable();
    }

    @Override
    public ValidationFindings getVetoFindings() {
        return null;
    }

    public boolean doIt(ObservableValue<? extends Boolean> observable) {
        // if (newValue == null)
        // return "";
        if (ignore)
            return get();
        ignore = true;

        this.observable = observable;

        // Set value into model and observable
        set( !get() );
        // if (observable instanceof SimpleBooleanProperty)
        // ((SimpleBooleanProperty) observable).set( get() );

        log.debug( "Set to: " + get() );
        ignore = false;
        return get();
    }

    @Override
    public Boolean undoIt() {
        ignore = true;
        set( !get() );
        if (observable instanceof SimpleBooleanProperty)
            ((SimpleBooleanProperty) observable).set( !get() );

        log.debug( "Undo - restored base path to " + get() );
        ignore = false;
        return get();
    }

    /**
     * @return true if change already made is valid for this object for this application and user.
     */
    public boolean isValid() {
        // Override if change can cause invalid objects
        return true;
    }

}
