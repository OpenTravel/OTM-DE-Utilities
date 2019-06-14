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

package org.opentravel.exampleupgrade;

import java.lang.ref.WeakReference;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;

/**
 * Extension of the <code>TreeCell</code> class that allows sub-classes to configure the visual style of the cell.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth") // Unavoidable since the base class is from core JavaFXx
public abstract class StyledTreeCell<T> extends TreeCell<T> {

    private HBox hbox;

    private WeakReference<TreeItem<T>> treeItemRef;

    private InvalidationListener treeItemGraphicListener = observable -> updateDisplay( getItem(), isEmpty() );

    private WeakInvalidationListener weakTreeItemGraphicListener =
        new WeakInvalidationListener( treeItemGraphicListener );

    private InvalidationListener treeItemListener = o -> {
        TreeItem<T> oldTreeItem = treeItemRef == null ? null : treeItemRef.get();
        TreeItem<T> newTreeItem = getTreeItem();

        if (oldTreeItem != null) {
            oldTreeItem.graphicProperty().removeListener( weakTreeItemGraphicListener );
        }

        if (newTreeItem != null) {
            newTreeItem.graphicProperty().addListener( weakTreeItemGraphicListener );
            treeItemRef = new WeakReference<>( newTreeItem );
        }
    };

    private WeakInvalidationListener weakTreeItemListener = new WeakInvalidationListener( treeItemListener );

    /**
     * Default constructor.
     */
    public StyledTreeCell() {
        treeItemProperty().addListener( weakTreeItemListener );

        if (getTreeItem() != null) {
            getTreeItem().graphicProperty().addListener( weakTreeItemGraphicListener );
        }
    }

    /**
     * Returns the list of all possible conditional style classes.
     * 
     * @return List&lt;String&gt;
     */
    protected abstract List<String> getConditionalStyleClasses();

    /**
     * Returns the style that should be applied to the cell.
     * 
     * @return String
     */
    protected abstract String getConditionalStyleClass();

    /**
     * Updates the display of the cell when the item has been modified.
     * 
     * @param item the tree item that has been modified
     * @param empty flag indicating whether the content of the cell is empty
     */
    private void updateDisplay(T item, boolean empty) {
        if ((item == null) || empty) {
            hbox = null;
            setText( null );
            setGraphic( null );

        } else {
            // Update the graphic if one is set in the TreeItem
            updateTreeItemGraphic( item );
        }
    }

    /**
     * Update the graphic if one is set in the associated TreeItem.
     * 
     * @param item item for which to update the graphic
     */
    private void updateTreeItemGraphic(T item) {
        TreeItem<T> treeItem = getTreeItem();
        Node graphic = treeItem == null ? null : treeItem.getGraphic();
        ObservableList<String> styles = getStyleClass();

        if (graphic != null) {
            if (item instanceof Node) {
                setText( null );

                // The item is a Node, and the graphic exists, so
                // we must insert both into an HBox and present that
                // to the user (see RT-15910)
                if (hbox == null) {
                    hbox = new HBox( 3 );
                }
                hbox.getChildren().setAll( graphic, (Node) item );
                setGraphic( hbox );

            } else {
                hbox = null;
                setText( item.toString() );
                setGraphic( graphic );
            }

        } else {
            hbox = null;

            if (item instanceof Node) {
                setText( null );
                setGraphic( (Node) item );

            } else {
                setText( item.toString() );
                setGraphic( null );
            }
        }
        styles.removeAll( getConditionalStyleClasses() );
        styles.add( getConditionalStyleClass() );
    }

    /**
     * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
     */
    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem( item, empty );
        updateDisplay( item, empty );
    }

}
