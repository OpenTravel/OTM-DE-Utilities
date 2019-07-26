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

import org.opentravel.dex.controllers.resources.ResourcesDAO;

import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.image.ImageView;

/**
 * @author dmh
 *
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class ValidationResourceTreeTableCellFactory extends TreeTableCell<ResourcesDAO,ImageView> {

    // TreeTableCell<PropertiesDAO, ImageView>() {
    @Override
    protected void updateItem(ImageView item, boolean empty) {
        super.updateItem( item, empty );
        // Provide imageView directly - does not update automatically as the observable property would
        // Provide tooltip showing validation results
        String name = "";
        if (!empty && getTreeTableRow() != null && getTreeTableRow().getItem() != null) {
            setGraphic( getTreeTableRow().getItem().getValue().validationImage() );
            name = getTreeTableRow().getItem().getValue().getValidationFindingsAsString();
            if (!name.isEmpty())
                setTooltip( new Tooltip( name ) );
        } else {
            setGraphic( null );
            setTooltip( null );
        }
    }
}
