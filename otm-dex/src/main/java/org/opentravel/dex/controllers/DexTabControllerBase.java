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
    // private static Logger log = LogManager.getLogger( DexTabControllerBase.class );

    protected List<DexIncludedController<?>> includedControllers = new ArrayList<>();
    protected DexMainController mainController;
    protected int viewGroupId = 0; // Assigned by main controller on configuration.

    public DexTabControllerBase() {
        // log.debug( "Tab Controller constructed." );
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

    // @Deprecated
    // @Override
    // public void configure(DexMainController mc) {
    // configure( mc, 0 );
    // // this.mainController = mc;
    // // includedControllers.forEach( mc::addIncludedController );
    // }

    @Override
    public void configure(DexMainController mc, int viewGroupId) {
        this.mainController = mc;
        for (DexIncludedController<?> ic : includedControllers)
            mc.addIncludedController( ic, viewGroupId );
        this.viewGroupId = viewGroupId;
    }

    public int getViewGroupId() {
        return viewGroupId;
    }

    public void launchWindow(ActionEvent e, StandaloneWindowControllerBase wc, int viewGroupId) {
        if (wc == null || e == null || !(e.getSource() instanceof MenuItem))
            return;
        ((MenuItem) e.getSource()).setDisable( true );
        wc.configure( mainController, (MenuItem) e.getSource(), viewGroupId );
        wc.show( wc.getTitle() );
    }

}
