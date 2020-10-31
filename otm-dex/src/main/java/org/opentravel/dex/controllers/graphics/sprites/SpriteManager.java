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

package org.opentravel.dex.controllers.graphics.sprites;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.dex.controllers.graphics.sprites.connections.Connection;
import org.opentravel.dex.controllers.graphics.sprites.retangles.ColumnRectangle;
import org.opentravel.dex.events.DexEvent;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmChoiceObject;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmEnumeration;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObjects;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Manage a collection of Dex Sprites
 * 
 * @author dmh
 */
public class SpriteManager {
    private static Log log = LogFactory.getLog( SpriteManager.class );

    private SettingsManager settingsManager;
    private Pane spritePane;
    private DexIncludedController<?> parentController = null;
    private DexSprite draggedSprite = null;
    private GraphicsContext connectionsGC;
    private List<Connection> connections;
    private Canvas connectionsCanvas;
    private List<ColumnRectangle> columns;

    private Paint backgroundColor = Color.gray( 0.95 );


    /**
     * Initialize the sprite. Create connections canvas and add to pane. Set mouse click handler.
     * 
     * @param spritePane
     * @param owner
     * @param gc
     */
    public SpriteManager(DexIncludedController<?> owner, SettingsManager settingsManager) {
        parentController = owner;
        this.settingsManager = settingsManager;
        this.spritePane = settingsManager.getSpritePane();
        //
        connectionsCanvas = new Canvas( spritePane.getWidth(), spritePane.getHeight() );
        spritePane.getChildren().add( connectionsCanvas );
        connectionsCanvas.widthProperty().bind( spritePane.widthProperty() );
        connectionsCanvas.heightProperty().bind( spritePane.heightProperty() );
        connectionsGC = connectionsCanvas.getGraphicsContext2D();
        connectionsGC.setFill( backgroundColor );
        connectionsGC.fillRect( 0, 0, connectionsCanvas.getWidth(), connectionsCanvas.getHeight() );
        connections = new ArrayList<>();
        //
        createColumns( 3 );
        //
        spritePane.setOnMouseClicked( this::mouseClick );
    }

    // /**
    // * Add the sprite to the list and render into pane
    // *
    // * @param sprite
    // */
    // public void add(DexSprite<OtmLibraryMember> sprite, ColumnRectangle column) {
    // column.add( sprite );
    // // activeSprites.add( sprite );
    // // spritePane.getChildren().add( sprite.render() );
    // }


    /**
     * {@link #add(OtmLibraryMember, ColumnRectangle, boolean)} not collapsed.
     * 
     * @param member
     */
    public DexSprite add(OtmLibraryMember member, ColumnRectangle column) {
        return add( member, column, false );
    }

    /**
     * If the column does not contain the sprite, create it using {@link #factory(OtmLibraryMember)}.
     * 
     * @param member
     * @param column if null, use column 1
     * @param collapsed
     * @return
     */
    public MemberSprite<OtmLibraryMember> add(OtmLibraryMember member, ColumnRectangle column, boolean collapsed) {
        if (column == null)
            column = getColumn( 1 );

        MemberSprite<OtmLibraryMember> memberSprite = column.get( member );

        if (memberSprite == null) {
            memberSprite = factory( member );
            column.add( memberSprite );
        }
        if (memberSprite != null) {
            memberSprite.setCollapsed( collapsed );
        }
        return memberSprite;
    }

    /**
     * Add to connections list and redraw the connections canvas
     * 
     * @param c
     */
    public void addAndDraw(Connection c) {
        if (!connections.contains( c )) {
            connections.add( c );
            c.draw( connectionsGC );
        }
    }

    /**
     * Remove all sprites and their canvases. Remove all connections.
     */
    public void clear() {
        columns.forEach( ColumnRectangle::clear );
        eraseConnections();
        connections.clear();
        // should be done in columns, but just to be sure
        spritePane.getChildren().clear();
        spritePane.getChildren().add( connectionsCanvas );
    }

    // FIXME - this seems broken!
    @Deprecated
    public boolean contains(OtmLibraryMember member) {
        for (DexSprite s : getAllSprites())
            if (s.getMember() == member)
                return true;
        return false;
    }

    private void createColumns(int count) {
        if (columns == null)
            columns = new ArrayList<>();
        ColumnRectangle column = new ColumnRectangle( spritePane, null );
        int i = 0;
        do {
            columns.add( column );
            ColumnRectangle nc = new ColumnRectangle( spritePane, column );
            column.setNext( nc );
            column = nc;
        } while (i++ < count);
    }

    /**
     * clear the dragged sprite then post it at the new location
     */
    public void drag(MouseEvent e) {
        if (draggedSprite != null) {
            // log.debug( "Found Selected Sprite: " + draggedSprite.getMember() );
            draggedSprite.clear();
            draggedSprite.set( e.getX(), e.getY() );
            draggedSprite.render();
            // render updates connections updateConnections( draggedSprite );
        }
        // log.debug( "Dragging sprite." );
    }

    public void dragEnd(MouseEvent e) {
        if (draggedSprite != null)
            updateConnections();
        draggedSprite = null;
        // log.debug( "Drag end." );
    }

    public void dragStart(MouseEvent e) {
        // log.debug( "Drag start. x = " + e.getX() + " y = " + e.getY() );
        draggedSprite = findSprite( new Point2D( e.getX(), e.getY() ) );
        if (draggedSprite != null)
            draggedSprite.getCanvas().toFront();
    }

    /**
     * Draw a filled rectangle on the connections canvas
     */
    public void eraseConnections() {
        // connectionsGC.clearRect( 0, 0, connectionsCanvas.getWidth(), connectionsCanvas.getHeight() );
        connectionsGC.fillRect( 0, 0, connectionsCanvas.getWidth(), connectionsCanvas.getHeight() );
    }

    /**
     * Sprite factory.
     * 
     * @param member
     * @return Built sprite or null.
     */
    // sprite factory
    private MemberSprite<OtmLibraryMember> factory(OtmLibraryMember member) {
        MemberSprite<?> newSprite = null;
        if (member instanceof OtmBusinessObject)
            newSprite = new BusinessObjectSprite( (OtmBusinessObject) member, this );
        else if (member instanceof OtmChoiceObject)
            newSprite = new ChoiceObjectSprite( (OtmChoiceObject) member, this );
        else if (member instanceof OtmCore)
            newSprite = new CoreObjectSprite( (OtmCore) member, this );
        else if (member instanceof OtmValueWithAttributes)
            newSprite = new VWASprite( (OtmValueWithAttributes) member, this );
        else if (member instanceof OtmContextualFacet)
            newSprite = new ContextualFacetSprite( (OtmContextualFacet) member, this );
        else if (member instanceof OtmEnumeration)
            newSprite = new EnumerationSprite( (OtmEnumeration<?>) member, this );
        else if (member instanceof OtmSimpleObjects)
            newSprite = new SimpleSprite( (OtmSimpleObjects<?>) member, this );
        else if (member instanceof OtmResource)
            newSprite = new ResourceSprite( (OtmResource) member, this );
        // log.debug( "factory created: " + newSprite );
        return (MemberSprite<OtmLibraryMember>) newSprite;
    }

    public DexSprite findSprite(OtmLibraryMember member) {
        DexSprite selectedSprite = null;
        for (ColumnRectangle column : columns) {
            selectedSprite = column.get( member );
            if (selectedSprite != null)
                return selectedSprite;
        }
        return selectedSprite;
    }


    public DexSprite findSprite(Point2D point) {
        DexSprite selectedSprite = null;
        for (DexSprite sprite : getAllSprites())
            if (sprite.contains( point )) {
                return (sprite);
            }
        return selectedSprite;
    }

    public MemberSprite<OtmLibraryMember> get(OtmLibraryMember member) {
        for (DexSprite s : getAllSprites())
            if (s.getMember() == member && s instanceof MemberSprite)
                return (MemberSprite<OtmLibraryMember>) s;
        return null;
    }

    /**
     * 
     * @return new list of all sprites in all columns
     */
    public List<DexSprite> getAllSprites() {
        List<DexSprite> sprites = new ArrayList<>();
        columns.forEach( c -> sprites.addAll( c.getSprites() ) );
        return sprites;
    }

    public ColumnRectangle getColumn(int index) {
        for (ColumnRectangle c : columns)
            if (c.getIndex() == index)
                return c;
        return null;
    }

    public GraphicsContext getConnectionsGC() {
        return connectionsGC;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    private void mouseClick(MouseEvent e) {
        // log.debug( "Mouse click on at: " + e.getX() + " " + e.getY() );
        // The whole canvas is active, check boundaries
        // TODO - use find(point)
        DexSprite selected = null;
        for (DexSprite sprite : getAllSprites())
            if (sprite.contains( new Point2D( e.getX(), e.getY() ) )) {
                selected = sprite;
                break;
            }
        if (selected != null) {
            if (e.getButton() != MouseButton.SECONDARY && e.getClickCount() >= 2) {
                // log.debug( "Throw event: " + selected.getMember() );
                publishEvent( new DexMemberSelectionEvent( selected.getMember() ) );
            } else
                selected.findAndRunRectangle( e );
        }
    }

    protected void publishEvent(DexEvent event) {
        if (parentController != null)
            parentController.publishEvent( event );
    }

    public void refresh() {
        for (DexSprite sprite : getAllSprites()) {
            sprite.clear();
            sprite.render();
        }
        updateConnections();
    }

    public void remove(DexSprite sprite) {
        if (sprite != null) {
            // log.debug( "Removing sprite: " + sprite.getMember() );
            removeConnection( sprite );
            sprite.getColumn().remove( sprite );
            sprite.clear();
        }
    }

    public void remove(OtmLibraryMember member) {
        DexSprite sprite = findSprite( member );
        remove( sprite );
    }

    public void removeConnection(DexSprite sprite) {
        if (sprite != null) {
            List<Connection> list = new ArrayList<>( connections );
            for (Connection c : list)
                if (c.contains( sprite ))
                    connections.remove( c );
            updateConnections();
        }
    }

    public void setCollapsed(boolean collapsed) {
        getAllSprites().forEach( s -> s.setCollapsed( collapsed ) );
        refresh();
    }

    public void update(Color color) {
        settingsManager.update( color );
        getAllSprites().forEach( s -> s.setBackgroundColor( color ) );
        refresh();
    }

    /**
     * Update the size then redraw the sprites and connections.
     * 
     * @param size
     */
    public void update(int size) {
        if (settingsManager.updateSize( size )) {
            getAllSprites().forEach( s -> s.set( settingsManager.getFont() ) );
            refresh();
        }
    }

    public void updateConnections() {
        eraseConnections();
        for (Connection c : connections) {
            c.draw( connectionsGC );
        }
        // FIXME - rectangles may have changed if font changed.
    }

    public void updateConnections(DexSprite sprite) {
        List<Connection> toDelete = new ArrayList<>();
        for (Connection c : connections) {
            if (!c.update( sprite, connectionsGC, backgroundColor ))
                toDelete.add( c );
        }
        // connections.removeAll( toDelete );
    }

}
