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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.dex.controllers.DexMainController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Manage the stand-alone repository window.
 * 
 * @author dmh
 *
 */
public abstract class StandaloneWindowControllerBase extends DexPopupControllerBase {
    private static Log log = LogFactory.getLog( StandaloneWindowControllerBase.class );

    protected static Stage dialogStage;
    protected List<DexIncludedController<?>> includedControllers = new ArrayList<>();

    /**
     * Initialize this controller using the passed FXML loader.
     * <p>
     * Note: This approach using a static stage and main controller hides the complexity from calling controller.
     * Otherwise, this code must migrate into the calling controller.
     * 
     */
    protected static StandaloneWindowControllerBase init(FXMLLoader loader) {
        // FXMLLoader loader = new FXMLLoader( StandaloneWindowControllerBase.class.getResource( LAYOUT_FILE ) );
        StandaloneWindowControllerBase controller = null;
        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            dialogStage.initModality( Modality.NONE );
            dialogStage.getScene().getStylesheets().add( "DavesViper.css" );

            // get the controller from loader.
            controller = loader.getController();
            if (!(controller instanceof StandaloneWindowControllerBase))
                throw new IllegalStateException( "Error creating resources window controller." );
        } catch (IOException e1) {
            throw new IllegalStateException(
                "Error loading search window. " + e1.getLocalizedMessage() + "\n" + e1.getCause().toString() );
        }
        positionStage( dialogStage );
        return controller;
    }


    public StandaloneWindowControllerBase() {
        log.debug( "Repository Window Controller constructed." );
    }

    @Override
    public void checkNodes() {
        if (includedControllers == null || includedControllers.isEmpty())
            throw new IllegalStateException( "Stand alone controller does not declare any included controllers." );
        includedControllers.forEach( c -> {
            if (c == null)
                throw new IllegalStateException( "Included controller not injected by FXML." );
        } );
    }

    public void configure(DexMainController mc, MenuItem menuItem) {
        launchedFromMenuItem = menuItem; // Remember so it can be enabled on close
        includedControllers.forEach( c -> mc.addIncludedController( c ) );
        mc.getEventSubscriptionManager().configureEventHandlers();
        // log.debug( "Stand alone window configured." );
    }

    @Override
    @FXML
    public void initialize() {
        // no-op
    }

    // public abstract String getTitle();

    @Override
    public void setup(String ignored) {
        super.setStage( getTitle(), dialogStage );
    }
}
