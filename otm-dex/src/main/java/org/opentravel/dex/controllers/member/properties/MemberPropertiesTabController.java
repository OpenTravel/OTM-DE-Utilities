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

package org.opentravel.dex.controllers.member.properties;

import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.DexTabControllerBase;
import org.opentravel.dex.controllers.member.MemberDetailsController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Manage the properties tab.
 * 
 * @author dmh
 *
 */
public class MemberPropertiesTabController extends DexTabControllerBase {

    /** FXML Java FX Nodes this controller is dependent upon */
    @FXML
    private MemberPropertiesTreeTableController memberPropertiesTreeTableController;
    @FXML
    private MemberDetailsController memberDetailsController;
    @FXML
    private PropertyDetailsController propertyDetailsController;

    public MemberPropertiesTabController() {
        // No-op
    }

    @Override
    public void configure(DexMainController mainController, int viewGroupId) {
        includedControllers.add( memberPropertiesTreeTableController );
        includedControllers.add( memberDetailsController );
        includedControllers.add( propertyDetailsController );
        super.configure( mainController, viewGroupId );
    }

    @Override
    public String getDialogTitle() {
        return MemberPropertiesWindowController.DIALOG_TITLE;
    }

    @Override
    public void launchWindow(ActionEvent e) {
        MemberPropertiesWindowController w = MemberPropertiesWindowController.init();
        super.launchWindow( e, w, getViewGroupId() + 100 );
    }

}
