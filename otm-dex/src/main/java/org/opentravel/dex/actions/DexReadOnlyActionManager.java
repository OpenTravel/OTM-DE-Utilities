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

import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;

import javafx.beans.value.ObservableValue;

/**
 * Read Only action manager. All actions are disabled (isEnabled = false) and run commands ignored.
 * 
 * @author dmh
 *
 */
public class DexReadOnlyActionManager extends DexActionManagerBase {
    // private static Log log = LogFactory.getLog( DexReadOnlyActionManager.class );

    public DexReadOnlyActionManager() {
        super();
    }

    /**
     * {@inheritDoc} Does nothing and returns false.
     */
    @Override
    public boolean addAction(DexActions action, ObservableValue<? extends String> op, OtmModelElement<?> subject) {
        return false;
    }

    /**
     * {@inheritDoc} Does nothing and returns false.
     */
    @Override
    public boolean addAction(DexActions action, ObservableValue<? extends Boolean> property, OtmObject subject) {
        return false;
    }

    /**
     * {@inheritDoc} Does nothing and returns false.
     */
    @Override
    public void run(DexActions action, OtmObject subject, Object data) {
        // Do Nothing - READ ONLY!
    }

    @Override
    public boolean isEnabled(DexActions action, OtmObject subject) {
        return false;
    }
}
