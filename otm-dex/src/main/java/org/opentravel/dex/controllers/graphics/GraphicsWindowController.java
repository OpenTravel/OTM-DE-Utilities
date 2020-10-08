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

package org.opentravel.dex.controllers.graphics;

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
public class GraphicsWindowController extends StandaloneWindowControllerBase {

    public static final String LAYOUT_FILE = "/SearchViews/SearchWindow.fxml";
    public static final String DIALOG_TITLE = "Search";

    /** ********* FXML Java FX Nodes this controller is dependent upon */
    // @FXML
    // private RepositorySelectionController repositorySelectionController;
    // @FXML
    // private SearchQueryController searchQueryController;
    @FXML
    private GraphicsCanvasController graphicsCanvasController;

    /**
     * Initialize this controller using FXML loader.
     */
    protected static GraphicsWindowController init() {
        FXMLLoader loader = new FXMLLoader( GraphicsWindowController.class.getResource( LAYOUT_FILE ) );
        return (GraphicsWindowController) StandaloneWindowControllerBase.init( loader );
    }

    public GraphicsWindowController() {
        // No-op
    }

    @Override
    public void configure(DexMainController mc, MenuItem menuItem, int viewGroupId) {
        // includedControllers.add( repositorySelectionController );
        // includedControllers.add( searchQueryController );
        includedControllers.add( graphicsCanvasController );
        super.configure( mc, menuItem, viewGroupId );
    }

    @Override
    public String getTitle() {
        return DIALOG_TITLE;
    }

}
