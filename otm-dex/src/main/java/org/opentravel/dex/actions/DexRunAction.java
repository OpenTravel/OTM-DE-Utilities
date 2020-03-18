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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Actions invoked to perform actions on an OtmObject from the model. Action must acquire its own data.
 * <p>
 * 
 * @author dmh
 *
 */
public abstract class DexRunAction extends DexActionBase implements DexAction<Object> {
    public static Log log = LogFactory.getLog( DexRunAction.class );

    public DexRunAction() {
        super();
    }

    // @Deprecated
    // public abstract Object doIt();
    //
    /**
     * Perform the action using supplied data.
     * 
     * @param data to apply to the action
     * @return the value from get(). If null, the action was canceled and not added to queue.
     */
    public abstract Object doIt(Object data);

    /**
     * Simply get the object's field value
     */
    protected abstract Object get();

    @Override
    public ObservableValue<? extends OtmObject> getObservable() {
        return null; // There is no observable for directly run actions
    }

    /**
     * Override if the action can veto successful run
     * 
     * @see org.opentravel.dex.actions.DexAction#getVetoFindings()
     */
    @Override
    public ValidationFindings getVetoFindings() {
        return null;
    }

    @Override
    public void removeChangeListener() {
        // run actions do not use listeners
    }

    @Override
    public void setChangeListener(ChangeListener<Object> changeListener, ObservableValue<? extends Object> o) {
        // run actions do not use listeners
    }

}
