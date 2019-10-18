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

import org.opentravel.dex.controllers.member.properties.PropertiesDAO;

import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.image.ImageView;

/**
 * Provide cell with graphic and tool tip showing validation results
 * 
 * @author dmh
 *
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class ValidationPropertiesTreeTableCellFactory extends TreeTableCell<PropertiesDAO,ImageView> {

    @Override
    protected void updateItem(ImageView item, boolean empty) {
        super.updateItem( item, empty );
        String tip = "";
        if (!empty && getTreeTableRow() != null && getTreeTableRow().getItem() != null
            && getTreeTableRow().getItem().getValue() != null) {
            if (getTreeTableRow().getItem().validationImageProperty() != null)
                setGraphic( getTreeTableRow().getItem().validationImageProperty().get() );
            else
                setGraphic( null );
            tip = getTreeTableRow().getItem().getValidationFindingsAsString();
            if (tip != null && !tip.isEmpty())
                setTooltip( new Tooltip( tip ) );
        } else {
            setGraphic( null );
            setTooltip( null );
        }
    }
}
