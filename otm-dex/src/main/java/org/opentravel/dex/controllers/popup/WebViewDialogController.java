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

package org.opentravel.dex.controllers.popup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.model.OtmModelManager;
import org.opentravel.objecteditor.UserSettings;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for creating a web view pop-up display.
 * <p>
 * Create the controller using the static {@link WebViewDialogController#init() } method.
 * 
 * @author dmh
 *
 */
public class WebViewDialogController extends DexPopupControllerBase {
    private static Logger log = LogManager.getLogger( WebViewDialogController.class );

    public static final String LAYOUT_FILE = "/Dialogs/WebViewDialog.fxml";

    protected static Stage dialogStage;
    private static String dialogTitle = "Repository Web Viewer";

    /**
     * Initialize this controller
     * 
     * @return dialog controller or null
     */
    public static WebViewDialogController init() {
        FXMLLoader loader = new FXMLLoader( WebViewDialogController.class.getResource( LAYOUT_FILE ) );
        WebViewDialogController controller = null;
        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            dialogStage.initModality( Modality.NONE );

            // get the controller from it.
            controller = loader.getController();
            if (!(controller instanceof WebViewDialogController))
                throw new IllegalStateException( "Error creating controller." );
        } catch (IOException e1) {
            throw new IllegalStateException(
                "Error loading dialog box. " + e1.getLocalizedMessage() + "\n" + e1.getCause().toString() );
        }
        positionStage( dialogStage );
        return controller;
    }

    @FXML
    WebView webView;

    // private UserSettings userSettings;

    private WebEngine webEngine;

    @Override
    public void checkNodes() {
        if (dialogStage == null)
            throw new IllegalStateException( "Missing stage." );

        if (webView == null)
            throw new IllegalStateException( "Missing injected web view." );
    }

    @Override
    public void clear() {
        // dialogHelp.getChildren().clear();
    }

    @Override
    public void show(String url) {
        webEngine = webView.getEngine();
        log.debug( "loading: " + url );
        webEngine.load( url );
        super.show( "" );
    }

    /**
     * 
     * @param manager used to create project
     * @param initialProjectFolder used in user file selection dialog
     */
    public void configure(OtmModelManager manager, UserSettings settings) {
        // this.userSettings = settings;
    }

    @Override
    protected void setup(String message) {
        super.setStage( dialogTitle, dialogStage );
        // checkNodes();

        webEngine = webView.getEngine();
    }
}
