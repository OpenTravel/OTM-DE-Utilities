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

package org.opentravel.dex.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.popup.StandaloneWindowControllerBase;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

/**
 * Base class for tab controllers.
 * 
 * @author dmh
 *
 */
public abstract class DexTabControllerBase implements DexTabController {
    private static Log log = LogFactory.getLog( DexTabControllerBase.class );

    protected List<DexIncludedController<?>> includedControllers = new ArrayList<>();
    protected DexMainController mainController;

    public DexTabControllerBase() {
        // log.debug( "Repository Tab Controller constructed." );
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

    @Override
    @FXML
    public void initialize() {
        // no-op
    }

    /**
     * @param primaryStage
     */
    @Override
    public void configure(DexMainController mc) {
        this.mainController = mc;
        includedControllers.forEach( c -> mc.addIncludedController( c ) );
        // mainController.getEventSubscriptionManager().configureEventHandlers();
    }

    // @Override
    // public abstract String getDialogTitle();

    public void launchWindow(ActionEvent e, StandaloneWindowControllerBase wc) {
        if (wc == null)
            return;
        if (e.getSource() instanceof MenuItem) {
            ((MenuItem) e.getSource()).setDisable( true );
            wc.configure( mainController, (MenuItem) e.getSource() );
        }
        wc.configure( mainController, null );
        wc.show( wc.getTitle() );
    }

}
