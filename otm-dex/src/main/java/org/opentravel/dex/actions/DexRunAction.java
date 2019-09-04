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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Actions invoked to perform actions on an OtmObject from the model. Action must acquire its own data.
 * <p>
 * 
 * @author dmh
 *
 */
public abstract class DexRunAction extends DexActionBase implements DexAction<OtmObject> {
    private static Log log = LogFactory.getLog( DexRunAction.class );

    public DexRunAction() {
        super();
    }

    // protected ChangeListener<String> changeListener;

    @Override
    public void removeChangeListener() {
        // observable.removeListener( changeListener );
    }

    @Override
    public void setChangeListener(ChangeListener<OtmObject> changeListener, ObservableValue<? extends OtmObject> o) {
        // this.changeListener = listener;
    }

    /**
     * @see org.opentravel.dex.actions.DexAction#getObservable()
     */
    @Override
    public ObservableValue<? extends OtmObject> getObservable() {
        return null; // There is no observable for directly run actions
    }

    /**
     * Simply set the action's field to value
     * 
     * @param value
     */
    protected abstract boolean set(OtmObject value);

    /**
     * Simply get the object's field value
     */
    protected abstract OtmObject get();

    // @Override
    // public boolean isEnabled() {
    // return getSubject().isEditable();
    // }

    // @Override
    // public boolean isAllowed(OtmObject value) {
    // return getSubject().isEditable();
    // }

    public abstract OtmObject doIt();

    /**
     * Perform the action using supplied data.
     * 
     * @param data to apply to the action
     */
    public abstract void doIt(Object data);

    // @Override
    // public ValidationFindings getVetoFindings() {
    // return null;
    // }
    //
    // public String doIt(ObservableValue<? extends String> observable, String oldValue, String newValue) {
    // if (newValue == null)
    // return "";
    // if (ignore)
    // return "";
    // ignore = true;
    //
    // oldString = get();
    // if (oldString == null)
    // oldString = "";
    // this.observable = observable;
    // this.newString = newValue;
    //
    // // Set value into model and observable
    // set( newValue );
    // if (observable instanceof SimpleStringProperty)
    // ((SimpleStringProperty) observable).set( newValue );
    //
    // log.debug( "Set to: " + newValue );
    // ignore = false;
    // return get();
    // }
    //
    // @Override
    // public String undoIt() {
    // ignore = true;
    // set( oldString );
    // if (observable instanceof SimpleStringProperty)
    // ((SimpleStringProperty) observable).set( oldString );
    //
    // log.debug( "Undo - " + toString() + " using " + oldString );
    // ignore = false;
    // return oldString;
    // }

    // /**
    // * @return true if change already made is valid for this object for this application and user.
    // */
    // public boolean isValid() {
    // // Override if change can cause invalid objects
    // return true;
    // }


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
