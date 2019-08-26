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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

/**
 * Actions are invoked by the view controllers to perform <i>actions</i> on the model.
 * <p>
 * They are designed to be set as listeners to FX Observable objects. When the observable value changes, the associated
 * action handler is fired.
 * 
 * 
 * @author dmh
 *
 */
public abstract class DexStringAction extends DexActionBase implements DexAction<String> {
    private static Log log = LogFactory.getLog( DexStringAction.class );

    protected String oldString;
    protected String newString;
    protected ObservableValue<? extends String> observable;

    /**
     * @param otm
     */
    public DexStringAction(OtmObject otm) {
        super( otm );
        oldString = get();
        if (oldString == null)
            oldString = "";
    }

    /**
     * Simply set the object's string field value
     * 
     * @param value
     */
    protected abstract void set(String value);

    /**
     * Simply get the object's string field value
     */
    protected abstract String get();

    @Override
    public void doIt(Object name) {
        // NO-OP
    }

    @Override
    public boolean isEnabled() {
        return getSubject().isEditable();
    }

    @Override
    public boolean isAllowed(String value) {
        return getSubject().isEditable();
    }

    @Override
    public ValidationFindings getVetoFindings() {
        return null;
    }

    public String doIt(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (newValue == null)
            return "";
        if (ignore)
            return "";
        ignore = true;

        this.observable = observable;
        this.newString = newValue;

        // Set value into model and observable
        set( newValue );
        if (observable instanceof SimpleStringProperty)
            ((SimpleStringProperty) observable).set( newValue );

        coreActionManager.push( this );

        log.debug( "Set to: " + newValue );
        ignore = false;
        return get();
    }

    @Override
    public String undo() {
        ignore = true;
        set( oldString );
        if (observable instanceof SimpleStringProperty)
            ((SimpleStringProperty) observable).set( oldString );

        log.debug( "Undo - restored base path to " + oldString );
        ignore = false;
        return oldString;
    }

    /**
     * @return true if change already made is valid for this object for this application and user.
     */
    public boolean isValid() {
        // Override if change can cause invalid objects
        return true;
    }


    //
    // // VETOable event??
    // /**
    // * @return true if change is enabled for this application and user.
    // */
    // public boolean isEnabled();
    //
    // /**
    // * @return true if the requested change is allowed for object in this application and user.
    // */
    // public boolean isAllowed(T value);
    //
    //
    // // /**
    // // * @return true if change is valid for this object for this application and user.
    // // */
    // // public boolean wouldBeValid(T value);
}
