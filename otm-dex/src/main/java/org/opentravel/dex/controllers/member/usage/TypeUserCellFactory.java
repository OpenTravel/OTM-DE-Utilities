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

import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.dex.controllers.member.properties.PropertiesDAO;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmElement;

import javafx.css.PseudoClass;
import javafx.scene.control.TreeCell;
import javafx.scene.image.ImageView;

/**
 * @author dmh
 *
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public final class TypeUserCellFactory extends TreeCell<PropertiesDAO> {
    // private static Logger log = LogManager.getLogger( TypeUserCellFactory.class );

    private static final PseudoClass EDITABLE = PseudoClass.getPseudoClass( "editable" );
    private static final PseudoClass DIVIDER = PseudoClass.getPseudoClass( "divider" );
    // private DexIncludedController<?> controller;

    // private final ContextMenu providerMenu = new ContextMenu();

    public TypeUserCellFactory(DexIncludedController<?> controller) {
        // this.controller = controller;
    }

    @Override
    public void updateItem(PropertiesDAO item, boolean empty) {
        super.updateItem( item, empty );
        if (empty) {
            setText( null );
            setGraphic( null );
            pseudoClassStateChanged( DIVIDER, false );
        } else {
            if (isEditing()) {
                setText( null );
            } else {
                // 9/1/2020 - changed from name with prefix name to cardinality with type
                String txt = item.getValue().getName();
                if (item.getValue() instanceof OtmLibraryMember)
                    txt = item.getValue().getNameWithPrefix();
                else if (item.getValue() instanceof OtmTypeUser) {
                    txt += "   [";
                    if (item.getValue() instanceof OtmElement<?>)
                        txt += (((OtmElement<?>) item.getValue()).getCardinality());
                    String typeName = "*missing*";
                    if (((OtmTypeUser) item.getValue()).getAssignedType() != null)
                        typeName = ((OtmTypeUser) item.getValue()).getAssignedType().getName();
                    txt += " " + typeName + " ]";
                }
                setText( txt );
                ImageView graphic = ImageManager.get( item.getValue() );
                setGraphic( graphic );
                // if (!getTreeItem().isLeaf() && getTreeItem().getParent() != null) {
                // setContextMenu( providerMenu );
                // }
                pseudoClassStateChanged( EDITABLE, item.getValue() != null && item.getValue().isEditable() );
                pseudoClassStateChanged( DIVIDER, item.getValue() instanceof OtmLibraryMember );
            }
        }
    }


}
