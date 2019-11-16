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

package org.opentravel.dex.controllers.library;

import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.DexTabControllerBase;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Manage the Libraries tab.
 * 
 * @author dmh
 *
 */
public class LibrariesTabController extends DexTabControllerBase {

    /** FXML Java FX Nodes this controller is dependent upon */
    @FXML
    private LibrariesTreeTableController librariesTreeTableController;

    public LibrariesTabController() {
        // No-op
    }

    @Override
    public void configure(DexMainController mainController) {
        includedControllers.add( librariesTreeTableController );
        super.configure( mainController );
    }

    @Override
    public String getDialogTitle() {
        return null;
    }

    public void launchWindow(ActionEvent e) {
        // No-op
    }
}
