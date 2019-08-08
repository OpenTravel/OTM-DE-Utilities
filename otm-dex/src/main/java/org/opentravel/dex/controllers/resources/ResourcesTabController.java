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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.OtmEventSubscriptionManager;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.DexTabController;

import javafx.fxml.FXML;

/**
 * Manage the resource tab.
 * 
 * @author dmh
 *
 */
public class ResourcesTabController implements DexTabController {
    private static Log log = LogFactory.getLog( ResourcesTabController.class );

    /**
     * FXML Java FX Nodes this controller is dependent upon
     */
    @FXML
    private ResourcesTreeTableController resourcesTreeTableController;
    @FXML
    private ResourceDetailsController resourceDetailsController;
    @FXML
    private ResourceActionsTreeTableController resourceActionsTreeTableController;
    @FXML
    private ResourceErrorsTreeTableController resourceErrorsTreeTableController;

    public ResourcesTabController() {
        log.debug( "Resource Tab Controller constructed." );
    }

    public void checkNodes() {
        if (!(resourcesTreeTableController instanceof ResourcesTreeTableController))
            throw new IllegalStateException( "Resource tree table controller not injected by FXML." );

        if (!(resourceDetailsController instanceof ResourceDetailsController))
            throw new IllegalStateException( "Resource child details controller not injected by FXML." );

        if (!(resourceActionsTreeTableController instanceof ResourceActionsTreeTableController))
            throw new IllegalStateException( "Resource Actions controller not injected by FXML." );

        if (!(resourceErrorsTreeTableController instanceof ResourceErrorsTreeTableController))
            throw new IllegalStateException( "Resource Errors controller not injected by FXML." );

    }

    @Override
    @FXML
    public void initialize() {
        // no-op
        checkNodes();
    }

    /**
     * @param primaryStage
     */
    @Override
    public void configure(DexMainController parent) {
        OtmEventSubscriptionManager eventManager = parent.getEventSubscriptionManager();
        parent.addIncludedController( resourcesTreeTableController, eventManager );
        parent.addIncludedController( resourceDetailsController, eventManager );
        parent.addIncludedController( resourceActionsTreeTableController, eventManager );
        parent.addIncludedController( resourceErrorsTreeTableController, eventManager );
        log.debug( "Repository Tab configured." );
    }
}
