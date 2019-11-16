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

package org.opentravel.dex.controllers.resources;

import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.popup.StandaloneWindowControllerBase;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuItem;

/**
 * Manage the stand-alone resource window.
 * 
 * @author dmh
 *
 */
public class ResourcesWindowController extends StandaloneWindowControllerBase {

    public static final String LAYOUT_FILE = "/ResourceViews/ResourcesWindow.fxml";
    public static final String DIALOG_TITLE = "Resources";

    /**
     * Initialize this controller using FXML loader.
     */
    public static ResourcesWindowController init() {
        FXMLLoader loader = new FXMLLoader( ResourcesWindowController.class.getResource( LAYOUT_FILE ) );
        return (ResourcesWindowController) StandaloneWindowControllerBase.init( loader );
    }

    /** ******** Java FX Nodes this controller is dependent upon */
    @FXML
    private ResourcesTreeTableController resourcesTreeTableController;
    @FXML
    private ResourceDetailsController resourceDetailsController;
    @FXML
    private ResourceActionsTreeTableController resourceActionsTreeTableController;
    @FXML
    private ResourceErrorsTreeTableController resourceErrorsTreeTableController;

    public ResourcesWindowController() {
        // No-op
    }

    @Override
    public void configure(DexMainController mainController, MenuItem menuItem) {
        includedControllers.add( resourcesTreeTableController );
        includedControllers.add( resourceDetailsController );
        includedControllers.add( resourceActionsTreeTableController );
        includedControllers.add( resourceErrorsTreeTableController );
        super.configure( mainController, menuItem );
    }

    @Override
    public String getTitle() {
        return DIALOG_TITLE;
    }
}
