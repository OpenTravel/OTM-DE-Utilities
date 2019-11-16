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

package org.opentravel.dex.controllers.member.usage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.DexTabController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Manage the properties tab. Just register the included controllers.
 * 
 * @author dmh
 *
 */
public class WhereUsedTabController implements DexTabController {
    private static Log log = LogFactory.getLog( WhereUsedTabController.class );

    /**
     * FXML Java FX Nodes this controller is dependent upon
     */
    @FXML
    private TypeUsersTreeController typeUsersTreeController;
    @FXML
    private UsersTreeController usersTreeController;
    @FXML
    private TypeProvidersTreeController typeProvidersTreeController;

    private DexMainController mainController;

    // Available but not used
    // @FXML
    // private Tab whereUsedTab;
    // @FXML
    // private VBox whereUsedTabVbox;

    public WhereUsedTabController() {
        log.debug( "Where Used Controller constructed." );
    }

    @Override
    public void checkNodes() {}


    @FXML
    @Override
    public void initialize() {
        // no-op
    }

    @Override
    public void configure(DexMainController mainController) {
        this.mainController = mainController;
        mainController.addIncludedController( typeUsersTreeController );
        mainController.addIncludedController( usersTreeController );
        mainController.addIncludedController( typeProvidersTreeController );
        // mainController.getEventSubscriptionManager().configureEventHandlers();
    }

    @Override
    public String getDialogTitle() {
        return null;
    }

    public void launchWindow(ActionEvent e) {
        // No-op
    }

}
