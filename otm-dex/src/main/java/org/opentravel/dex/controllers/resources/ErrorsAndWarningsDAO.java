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

import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexDAO;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.ValidationFinding;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

/**
 * The TreeItem properties for object findings. Simple Data Access Object that contains data and provides gui access.
 *
 * @author dmh
 * @param <T>
 *
 */
public class ErrorsAndWarningsDAO implements DexDAO<OtmObject> {
    // private static Log log = LogFactory.getLog( ErrorsAndWarningsDAO.class );

    protected OtmObject otmObject;
    protected ValidationFinding finding;

    public ErrorsAndWarningsDAO(ValidationFinding finding) {
        this.finding = finding;
        this.otmObject = OtmModelElement.get( (TLModelElement) finding.getSource() );
    }

    @Override
    public ImageView getIcon(ImageManager imageMgr) {
        return ImageManager.get( otmObject );
    }

    @Override
    public OtmObject getValue() {
        return otmObject;
    }

    public boolean isEditable() {
        return otmObject != null ? otmObject.isEditable() : false;
    }

    public StringProperty errorProperty() {
        return otmObject != null ? otmObject.validationProperty() : new ReadOnlyStringWrapper();
    }

    public ObjectProperty<ImageView> errorImageProperty() {
        return otmObject != null ? otmObject.validationImageProperty() : null;
    }


    public StringProperty nameProperty() {
        return otmObject != null ? otmObject.nameProperty() : new ReadOnlyStringWrapper();
    }


    public StringProperty descriptionProperty() {
        return finding != null
            ? new ReadOnlyStringWrapper( finding.getFormattedMessage( FindingMessageFormat.MESSAGE_ONLY_FORMAT ) )
            : new ReadOnlyStringWrapper();
    }

    public StringProperty levelProperty() {
        return finding != null ? new ReadOnlyStringWrapper( finding.getType().getDisplayName() )
            : new ReadOnlyStringWrapper();
    }


    @Override
    public String toString() {
        return otmObject != null ? otmObject.getPrefix() + ":" + otmObject.toString() : "";
    }

    /**
     * Create and add to tree with no conditional logic.
     * 
     * @return new tree item added to tree at the parent
     */
    public TreeItem<ErrorsAndWarningsDAO> createTreeItem(TreeItem<ErrorsAndWarningsDAO> parent) {
        TreeItem<ErrorsAndWarningsDAO> item = new TreeItem<>( this );
        item.setExpanded( false );
        if (parent != null)
            parent.getChildren().add( item );

        // Decorate if possible
        ImageView graphic = ImageManager.get( otmObject );
        if (graphic != null && otmObject != null) {
            item.setGraphic( graphic );
            Tooltip.install( graphic, new Tooltip( otmObject.getObjectTypeName() ) );
        }
        return item;
    }

}
