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

package org.opentravel.dex.action.manager;

import org.opentravel.dex.actions.CopyLibraryMemberAction;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.OtmObject;

/**
 * Read Only action manager. All actions are disabled (isEnabled = false) and run commands ignored.
 * 
 * @author dmh
 *
 */
public class DexReadOnlyActionManager extends DexActionManagerBase {
    // private static Logger log = LogManager.getLogger( DexReadOnlyActionManager.class );

    public DexReadOnlyActionManager() {
        super();
    }

    @Override
    public boolean isEnabled(DexActions action, OtmObject subject) {
        // All copying content from any source.
        if (action == DexActions.COPYLIBRARYMEMBER)
            return CopyLibraryMemberAction.isEnabled( subject );
        return false;
    }
    // TODO - should this also account for chainEditable?
}
