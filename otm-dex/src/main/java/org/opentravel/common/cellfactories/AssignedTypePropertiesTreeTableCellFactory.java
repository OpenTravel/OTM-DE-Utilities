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

package org.opentravel.common.cellfactories;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.member.properties.MemberPropertiesTreeTableController;
import org.opentravel.dex.controllers.member.properties.PropertiesDAO;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * @author dmh
 *
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class AssignedTypePropertiesTreeTableCellFactory extends TreeTableCell<PropertiesDAO,String> {
    private static Log log = LogFactory.getLog( AssignedTypePropertiesTreeTableCellFactory.class );
    private MemberPropertiesTreeTableController controller;
    private OtmTypeProvider assignedType = null;

    public AssignedTypePropertiesTreeTableCellFactory(MemberPropertiesTreeTableController controller) {
        this.controller = controller;
    }

    // TreeTableCell<PropertiesDAO, ImageView>() {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem( item, empty );
        if (!empty && getTreeTableRow() != null && getTreeTableRow().getItem() != null) {
            OtmObject element = getTreeTableRow().getItem().getValue();
            ImageView graphic = null;

            setText( getTreeTableRow().getItem().assignedTypeProperty().get() );

            if (element instanceof OtmTypeUser)
                assignedType = ((OtmTypeUser) element).getAssignedType();
            else
                assignedType = null;

            if (assignedType != null) {
                if (assignedType.getLibrary() == null)
                    setTooltip( new Tooltip( assignedType.getObjectTypeName() ) );
                else
                    setTooltip( new Tooltip( assignedType.getObjectTypeName() + " in "
                        + assignedType.getLibrary().getName() + " library.\n" + assignedType.getDescription() ) );
                // graphic = new ImageManager().get_OLD( assignedType );
                graphic = ImageManager.get( assignedType );
                if (graphic != null)
                    setGraphic( graphic );
                setOnMouseClicked( this::mouseClick );
            } else {
                setGraphic( null );
                setTooltip( null );
            }
        } else {
            setGraphic( null );
            setTooltip( null );
            setText( null );
        }
    }

    public void mouseClick(MouseEvent event) {
        OtmLibraryMember lm = null;
        // this fires after the member selection listener
        if (assignedType != null)
            lm = ((OtmModelElement<?>) assignedType).getOwningMember();
        if (controller != null && event.getButton().equals( MouseButton.PRIMARY ) && event.getClickCount() == 2) {
            log.debug( "Double click selection: " );
            if (lm != null)
                controller.fireEvent( new DexMemberSelectionEvent( lm ) );
            else
                controller.getMainController().postStatus( "Assigned type could not be found." );
        }
    }

}
