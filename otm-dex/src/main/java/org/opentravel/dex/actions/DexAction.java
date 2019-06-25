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
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Actions are invoked by the view controllers to perform <i>actions</i> on the model.
 * <p>
 * They are designed to be set as listeners to FX Observable objects. When the observable value changes, the associated
 * action handler is fired.
 * 
 * @param <T> is the data type consumed by the action to make the change to the object
 * 
 * @author dmh
 *
 */
public interface DexAction<T> {

    // All implementations must implement, but the signatures will be different.
    // public T doIt(ObservableValue<? extends T> observable, T oldValue, T newValue);

    /**
     * Use the stored values to redo the change.
     * 
     * @return
     */
    // public T redo();
    public T undo();

    // VETOable event??
    /**
     * Is the action enabled for this subject?
     * 
     * @return true if change is enabled for this application and user.
     */
    public boolean isEnabled();

    /**
     * @return true if the requested change is allowed for object in this application and user.
     */
    public boolean isAllowed(T value);

    /**
     * @return true if change already made is valid for this object for this application and user.
     */
    public boolean isValid();

    /**
     * @return
     */
    ValidationFindings getVetoFindings();

    /**
     * Get the OTM object that is or will be acted upon.
     * 
     * @return
     */
    public OtmObject getSubject();

    /**
     * Run the action.
     * 
     * @param data value to be applied in the action, must be of type defined in the generic parameter
     */
    public void doIt(Object data);

    // /**
    // * @return true if change is valid for this object for this application and user.
    // */
    // public boolean wouldBeValid(T value);
}