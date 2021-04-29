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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.actions.DexAction;
import org.opentravel.dex.actions.DexActionBase;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
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


    protected ChangeListener<String> changeListener;

    public DexStringAction() {
        super();
    }

    @Override
    public void removeChangeListener() {
        observable.removeListener( changeListener );
    }

    @Override
    public ObservableValue<? extends String> getObservable() {
        return observable;
    }

    @Override
    public void setChangeListener(ChangeListener<String> listener, ObservableValue<? extends String> o) {
        // Remove any previously set listeners
        if (changeListener != null && observable != null)
            removeChangeListener();

        this.changeListener = listener;
        this.observable = o;

        if (observable != null && changeListener != null)
            observable.addListener( listener );
    }

    public String doIt(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (newValue == null)
            return "";
        if (ignore)
            return "";
        ignore = true;

        oldString = get();
        if (oldString == null)
            oldString = "";
        this.observable = observable;
        this.newString = newValue;

        // Set value into model and observable
        set( newValue );
        if (observable instanceof SimpleStringProperty)
            ((SimpleStringProperty) observable).set( newValue );

        // log.debug( "Set to: " + newValue );
        ignore = false;
        return get();
    }

    /**
     * Simply get the object's string field value
     */
    protected abstract String get();

    @Override
    public ValidationFindings getVetoFindings() {
        return null;
    }

    /**
     * @return true if change already made is valid for this object for this application and user.
     */
    public boolean isValid() {
        // Override if change can cause invalid objects
        return true;
    }

    /**
     * Simply set the object's string field value
     * 
     * @param value
     */
    protected abstract void set(String value);

    @Override
    public String undoIt() {
        ignore = true;
        set( oldString );
        if (observable instanceof SimpleStringProperty)
            ((SimpleStringProperty) observable).set( oldString );

        // log.debug( "Undo - " + toString() + " using " + oldString );
        ignore = false;
        return oldString;
    }
}
