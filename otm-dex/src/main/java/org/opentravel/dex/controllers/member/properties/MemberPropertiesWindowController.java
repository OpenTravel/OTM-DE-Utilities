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
import org.opentravel.dex.controllers.member.MemberDetailsController;
import org.opentravel.dex.controllers.popup.StandaloneWindowControllerBase;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuItem;

/**
 * Manage the stand-alone member properties window.
 * 
 * @author dmh
 *
 */
public class MemberPropertiesWindowController extends StandaloneWindowControllerBase {

    public static final String LAYOUT_FILE = "/MemberViews/MemberPropertiesWindow.fxml";
    public static final String DIALOG_TITLE = "Member Properties";

    /**
     * Initialize this controller using FXML loader.
     */
    protected static MemberPropertiesWindowController init() {
        FXMLLoader loader = new FXMLLoader( MemberPropertiesWindowController.class.getResource( LAYOUT_FILE ) );
        return (MemberPropertiesWindowController) StandaloneWindowControllerBase.init( loader );
    }

    /**
     * ******* FXML Java FX Nodes this controller is dependent upon
     */
    @FXML
    private MemberDetailsController memberDetailsController;
    @FXML
    private MemberPropertiesTreeTableController memberPropertiesTreeTableController;
    @FXML
    private PropertyDetailsController propertyDetailsController;

    public MemberPropertiesWindowController() {
        // No-op
    }

    @Override
    public void configure(DexMainController mc, MenuItem menuItem) {
        includedControllers.add( memberDetailsController );
        includedControllers.add( memberPropertiesTreeTableController );
        includedControllers.add( propertyDetailsController );
        super.configure( mc, menuItem );
    }

    @Override
    public String getTitle() {
        return DIALOG_TITLE;
    }
}
