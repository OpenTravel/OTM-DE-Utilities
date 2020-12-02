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

import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.dex.controllers.graphics.sprites.connections.Connection;
import org.opentravel.dex.controllers.graphics.sprites.rectangles.ColumnRectangle;
import org.opentravel.dex.events.DexEvent;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
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
    // private static Log log = LogFactory.getLog( SpriteManager.class );

    private SettingsManager settingsManager;
    private Pane spritePane;
    private DexIncludedController<?> parentController = null;
    private DexSprite draggedSprite = null;
    private GraphicsContext connectionsGC;
    private List<Connection> connections;
    private Canvas connectionsCanvas;
    private List<ColumnRectangle> columns;

    private Paint backgroundColor = Color.gray( 0.95 );

    // private Canvas domainCanvas;
    // private GraphicsContext domainGC;
    // private List<DomainSprite> domains;
    // private DomainRectangle domainR;

    // FIXME - retain sprites in columns
    private List<DomainSprite> domainSprites = new ArrayList<>();

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
        connectionsGC.setFill( Color.ALICEBLUE );
        connectionsGC.setFill( backgroundColor );
        connectionsGC.fillRect( 0, 0, connectionsCanvas.getWidth(), connectionsCanvas.getHeight() );
        connections = new ArrayList<>();
        //
        // createDomains();
        createColumns( 3 );

        //
        spritePane.setOnMouseClicked( this::mouseClick );
    }

    // FIXME
    public DomainSprite getDomainSprite(OtmLibrary library) {
        return !domainSprites.isEmpty() ? domainSprites.get( 0 ) : null;
    }

    public MemberSprite<?> add(OtmLibraryMember member, ColumnRectangle column) {
        return add( member, column, !member.isExpanded() );
    }

    /**
     * Try to find the sprite with the domain name / base namespace. If not found, create one. Render the sprite into
     * the passed column.
     * 
     * @param baseNamespace
     * @param column
     * @return
     */
    // FIXME - let column save domain sprite
    public DomainSprite add(String baseNamespace, ColumnRectangle column) {
        DomainSprite subDomainS = findSprite( baseNamespace );
        if (subDomainS == null)
            subDomainS = factory( baseNamespace );
        subDomainS.render( column, false );
        return subDomainS;
    }

    public DomainSprite add(OtmLibrary library, ColumnRectangle column) {
        DomainSprite ds = getDomainSprite( library );
        if (ds == null) {
            ds = factory( library );
        }
        // column.add( ds ); done in render
        ds.render( column, false );
        return ds;
    }

    /**
     * If the column does not contain the sprite, create it using {@link #factory(OtmLibraryMember)}.
     * 
     * @param member
     * @param column if null, use column 1
     * @param collapsed
     * @return
     */
    public MemberSprite<?> add(OtmLibraryMember member, ColumnRectangle column, boolean collapsed) {
        if (column == null)
            column = getColumn( 1 );

        MemberSprite<?> memberSprite = column.get( member );
        if (memberSprite == null)
            memberSprite = factory( member );

        if (memberSprite != null)
            memberSprite.render( column, collapsed );

        return memberSprite;
    }

    public ProvidersSprite addProvidersSprite(DomainSprite domainSprite, ColumnRectangle column) {
        if (column == null)
            column = getColumn( 1 );

        ProvidersSprite ps = column.getProvidersSprite( domainSprite );
        if (ps == null)
            ps = (ProvidersSprite) factory( domainSprite, false );

        if (ps != null)
            ps.render( column, false );

        return ps;
    }

    public UsersSprite addUsersSprite(DomainSprite domainSprite, ColumnRectangle column) {
        if (column == null)
            column = getColumn( 1 );

        UsersSprite us = column.getUsersSprite( domainSprite );
        if (us == null)
            us = (UsersSprite) factory( domainSprite, true );

        if (us != null)
            us.render( column, false );

        return us;
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
        domainSprites.clear(); // FIXME - these should be in columns
        eraseConnections();
        connections.clear();
        // should be done in columns, but just to be sure
        spritePane.getChildren().clear();
        spritePane.getChildren().add( connectionsCanvas );
    }


    private void createColumns(int count) {
        if (columns == null)
            columns = new ArrayList<>();
        ColumnRectangle column = new ColumnRectangle( spritePane, null, 0, 0 );
        // ColumnRectangle column = new ColumnRectangle( spritePane, null, domainR.getX(), domainR.getMaxY() );
        int i = 0;
        do {
            columns.add( column );
            ColumnRectangle nc = new ColumnRectangle( spritePane, column, 0, 0 );
            // ColumnRectangle nc = new ColumnRectangle( spritePane, column, domainR.getX(), domainR.getMaxY() );
            column.setNext( nc );
            column = nc;
        } while (i++ <= count);
    }

    // // Temporary
    // private void createDomains() {
    // createDomainCanvas();
    // domainR = new DomainRectangle( this );
    // domainR.draw( domainGC, true );
    // }

    // private void createDomainCanvas() {
    // domainCanvas = new Canvas( spritePane.getWidth(), spritePane.getHeight() );
    // spritePane.getChildren().add( domainCanvas );
    // double spw = spritePane.getWidth();
    // double sph = spritePane.getHeight();
    // domainCanvas.setHeight( 50 );
    // // domainCanvas.setWidth( 200 );
    // domainCanvas.widthProperty().bind( spritePane.widthProperty() );
    // // domainCanvas.heightProperty().bind( spritePane.heightProperty() );
    // domainGC = domainCanvas.getGraphicsContext2D();
    // domainGC.setFill( Color.SANDYBROWN );
    // domainGC.fillRect( 0, 0, domainCanvas.getWidth(), domainCanvas.getHeight() );
    // domains = new ArrayList<>();
    //
    // new Rectangle( 0, 0, 400, 50 ).draw( domainGC, true );
    // }

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
    protected MemberSprite<?> factory(OtmLibraryMember member) {
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

        if (newSprite != null)
            spritePane.getChildren().add( newSprite.getCanvas() );

        // log.debug( "factory created: " + newSprite );
        return newSprite;
    }

    protected DexSprite factory(DomainSprite domainSprite, boolean users) {
        DexSprite ps = null;
        if (users)
            ps = new UsersSprite( this, domainSprite );
        else
            ps = new ProvidersSprite( this, domainSprite );
        // retained in column - domainSprites.add( ps );
        spritePane.getChildren().add( ps.getCanvas() );
        return ps;
    }

    protected DomainSprite factory(OtmLibrary library) {
        DomainSprite ds = new DomainSprite( this, library.getBaseNamespace() );
        domainSprites.add( ds ); // FIXME
        spritePane.getChildren().add( ds.getCanvas() );
        return ds;
    }

    protected DomainSprite factory(String baseNamespace) {
        DomainSprite ds = new DomainSprite( this, baseNamespace );
        domainSprites.add( ds );// FIXME
        spritePane.getChildren().add( ds.getCanvas() );
        return ds;
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

    public DomainSprite findSprite(String subDomain) {
        DexSprite selectedSprite = null;
        for (DexSprite sprite : getAllSprites())
            if (sprite instanceof DomainSprite && ((DomainSprite) sprite).getDomain().equals( subDomain )) {
                return (DomainSprite) (sprite);
            }
        return (DomainSprite) selectedSprite;
    }

    @SuppressWarnings("unchecked")
    public MemberSprite<OtmLibraryMember> get(OtmLibraryMember member) {
        for (DexSprite s : getAllSprites())
            if (s instanceof MemberSprite && ((MemberSprite<?>) s).getMember() == member)
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

    public OtmModelManager getModelManager() {
        return parentController != null ? parentController.getModelManager() : null;
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
        if (selected instanceof MemberSprite) {
            if (e.getButton() != MouseButton.SECONDARY && e.getClickCount() >= 2) {
                // log.debug( "Throw event: " + selected.getMember() );
                publishEvent( new DexMemberSelectionEvent( ((MemberSprite<?>) selected).getMember() ) );
            } else
                selected.findAndRunRectangle( e );
        } else if (selected instanceof DomainSprite)
            selected.findAndRunRectangle( e );
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
