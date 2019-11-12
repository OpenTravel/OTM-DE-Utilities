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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.OtmEventSubscriptionManager;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.DexTabController;
import org.opentravel.dex.controllers.member.MemberDetailsController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Manage the properties tab.
 * 
 * @author dmh
 *
 */
public class MemberPropertiesTabController implements DexTabController {
    private static Log log = LogFactory.getLog( MemberPropertiesTabController.class );

    /**
     * FXML Java FX Nodes this controller is dependent upon
     */
    @FXML
    private MemberPropertiesTreeTableController memberPropertiesTreeTableController;
    @FXML
    private MemberDetailsController memberDetailsController;

    private DexMainController mainController;

    public MemberPropertiesTabController() {
        log.debug( "Member Properties Tab Controller constructed." );
    }


    @Override
    public void checkNodes() {
        if (!(memberPropertiesTreeTableController instanceof MemberPropertiesTreeTableController))
            throw new IllegalStateException( "Member Properties tree table controller not injected by FXML." );
    }

    @FXML
    @Override
    public void initialize() {
        // no-op
    }

    @Override
    public void configure(DexMainController parent) {
        this.mainController = parent;
        OtmEventSubscriptionManager eventManager = parent.getEventSubscriptionManager();

        parent.addIncludedController( memberPropertiesTreeTableController, eventManager );
        parent.addIncludedController( memberDetailsController, eventManager );
    }

    @Override
    public String getDialogTitle() {
        return MemberPropertiesWindowController.dialogTitle;
    }

    public void launchWindow(ActionEvent e) {
        MemberPropertiesWindowController w = MemberPropertiesWindowController.init();
        w.configure( mainController );
        w.show( MemberPropertiesWindowController.dialogTitle );
    }

}
