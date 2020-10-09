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
import org.opentravel.dex.events.DexEvent;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmChoiceObject;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

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
    private static final double COLUMN_START = 10;
    private static final double COLUMN_WIDTH = 150;

    private static final int FACET_OFFSET = 5;
    private static final double MINIMUM_SLOT_HEIGHT = 20;

    private static Log log = LogFactory.getLog( SpriteManager.class );

    private Pane spritePane;
    List<DexSprite<?>> activeSprites = new ArrayList<>();
    DexIncludedController<?> parentController = null;

    private DexSprite<?> draggedSprite = null;

    // private Canvas backgroundCanvas;
    private GraphicsContext defaultGC;
    private GraphicsContext connectionsGC;
    private List<Connection> connections;
    private Canvas connectionsCanvas;

    private Paint backgroundColor = Color.gray( 0.95 );

    /**
     * Initialize the sprite. Create connections canvas and add to pane. Set mouse click handler.
     * 
     * @param spritePane
     * @param owner
     * @param gc
     */
    public SpriteManager(Pane spritePane, DexIncludedController<?> owner, GraphicsContext gc) {
        this.spritePane = spritePane;
        parentController = owner;
        defaultGC = gc;
        //
        connectionsCanvas = new Canvas( spritePane.getWidth(), spritePane.getHeight() );
        spritePane.getChildren().add( connectionsCanvas );
        connectionsGC = connectionsCanvas.getGraphicsContext2D();
        connectionsGC.setFill( backgroundColor );
        connectionsGC.fillRect( 0, 0, connectionsCanvas.getWidth(), connectionsCanvas.getHeight() );
        connections = new ArrayList<>();

        //
        spritePane.setOnMouseClicked( this::mouseClick );
    }

    /**
     * Add the sprite to the list and render into pane
     * 
     * @param sprite
     */
    public void add(DexSprite<?> sprite) {
        activeSprites.add( sprite );
        spritePane.getChildren().add( sprite.render() );
    }

    /**
     * Uses Sprite factory to adds sprite and related sprites to list and FX pane's children. Manager determines the
     * location for the sprite.
     * 
     * @param member
     */
    public DexSprite<?> add(OtmLibraryMember member) {
        Point2D p = getNextInColumn( 2 * COLUMN_START, 2 * FACET_OFFSET );

        // Do base
        DexSprite<?> baseSprite = null;
        if (member.getBaseType() != null) {
            baseSprite = add( (OtmLibraryMember) member.getBaseType(), p.getX(), p.getY() );
            if (baseSprite != null) {
                log.debug( "Added at " + p.getX() + " " + p.getY() + " base sprite: " + member.getBaseType() );
                p = getNextInColumn( baseSprite.getBoundaries().getX(),
                    baseSprite.getBoundaries().getMaxY() + FACET_OFFSET );
            }
        }

        DexSprite<?> memberSprite = add( member, p.getX(), p.getY() );

        if (baseSprite != null)
            addAndDraw( new SuperTypeConnection( baseSprite, memberSprite ) );

        return memberSprite;
    }

    /**
     * Sprite factory. Adds sprite to list and FX pane's children. Does NOT do related sprite for base type.
     * 
     * @param member
     */
    public DexSprite<?> add(OtmLibraryMember member, double x, double y) {

        DexSprite<?> newSprite = null;
        if (!contains( member )) {
            // sprite factory
            if (member instanceof OtmBusinessObject)
                newSprite = new BusinessObjectSprite( (OtmBusinessObject) member, this, defaultGC );
            else if (member instanceof OtmChoiceObject)
                newSprite = new ChoiceObjectSprite( (OtmChoiceObject) member, this, defaultGC );
            else if (member instanceof OtmContextualFacet)
                newSprite = new ContextualFacetSprite( (OtmContextualFacet) member, this, defaultGC );

            if (newSprite != null) {
                newSprite.set( x, y );
                add( newSprite );
            }

            // Add related contextual facets if any
            DexSprite<?> relationS = newSprite;
            for (OtmContributedFacet cf : member.getChildrenContributedFacets()) {
                if (cf != null && cf.getContributor() != null) {
                    // Place new sprite in next column
                    Point2D p = getNextInColumn( relationS );
                    relationS = add( cf.getContributor(), p.getX(), p.getY() + FACET_OFFSET );
                    // Do connection
                    if (relationS != null)
                        addAndDraw( new ContributedConnection( relationS, newSprite ) );
                }
            }
        }
        return newSprite;
    }

    public void addAndDraw(Connection c) {
        connections.add( c );
        c.draw( connectionsGC );
    }

    public void clear() {
        for (DexSprite<?> sprite : activeSprites)
            sprite.clear();
        activeSprites.clear();

        eraseConnections();
        connections.clear();
    }

    public boolean contains(OtmLibraryMember member) {
        for (DexSprite<?> s : activeSprites)
            if (s.getMember() == member)
                return true;
        return false;
    }

    /**
     * clear the dragged sprite then post it at the new location
     */
    public void drag(MouseEvent e) {
        if (draggedSprite != null) {
            log.debug( "Found Selected Sprite: " + draggedSprite.getMember() );
            draggedSprite.clear();
            draggedSprite.set( e.getX(), e.getY() );
            draggedSprite.render();
            updateConnections( draggedSprite );
        }
        log.debug( "Dragging sprite." );
    }


    public void dragEnd(MouseEvent e) {
        if (draggedSprite != null)
            updateConnections();
        draggedSprite = null;
        log.debug( "Drag end." );
    }

    public void dragStart(MouseEvent e) {
        log.debug( "Drag start. x = " + e.getX() + " y = " + e.getY() );
        draggedSprite = findSprite( new Point2D( e.getX(), e.getY() ) );
    }

    public void eraseConnections() {
        // connectionsGC.clearRect( 0, 0, connectionsCanvas.getWidth(), connectionsCanvas.getHeight() );
        connectionsGC.fillRect( 0, 0, connectionsCanvas.getWidth(), connectionsCanvas.getHeight() );
    }

    public DexSprite<?> findSprite(OtmLibraryMember member) {
        DexSprite<?> selectedSprite = null;
        for (DexSprite<?> sprite : activeSprites)
            if (sprite.getMember() == member) {
                selectedSprite = sprite;
                log.debug( "findSprite found: " + selectedSprite.getMember() );
                break;
            }
        return selectedSprite;
    }

    public DexSprite<?> findSprite(Point2D point) {
        DexSprite<?> selectedSprite = null;
        for (DexSprite<?> sprite : activeSprites)
            if (sprite.contains( point )) {
                selectedSprite = sprite;
                log.debug( "findSprite found: " + selectedSprite.getMember() );
                break;
            }
        return selectedSprite;
    }

    public GraphicsContext getConnectionsGC() {
        return connectionsGC;
    }

    // public Point2D getPrevInColumn(DexSprite<?> sprite) {
    // Point2D top = new Point2D( 0, 0 );
    // if (sprite != null) {
    // DexSprite<?> next = sprite;
    // do {
    // top = new Point2D( next.getBoundaries().getX(), next.getBoundaries().getY() - 5 );
    // next = findSprite( top );
    // } while (next != null && top.getY() > 0);
    // }
    // return new Point2D( top.getX(), top.getY() < 0 ? 0 : top.getY() );
    // }

    public Point2D getNextInColumn(DexSprite<?> sprite) {
        Point2D bottom = new Point2D( 0, 0 );
        if (sprite != null) {
            DexSprite<?> next = sprite;
            do {
                bottom = new Point2D( next.getBoundaries().getX(), next.getBoundaries().getMaxY() + 5 );
                next = findSprite( bottom );
            } while (next != null && bottom.getY() < spritePane.getHeight());
        }
        return bottom;
    }

    public Point2D getNextInColumn(double x, double y) {
        Point2D bottom = new Point2D( x, y );
        DexSprite<?> next = null;
        do {
            next = findSprite( bottom );
            if (next != null)
                bottom = new Point2D( x, next.getBoundaries().getMaxY() + 5 );
            else {
                bottom = new Point2D( x, bottom.getY() + MINIMUM_SLOT_HEIGHT );// minimum slot size;
                next = findSprite( bottom );
            }
        } while (next != null && bottom.getY() < spritePane.getHeight());
        return bottom;
    }

    public Point2D getNextRightColumn(DexSprite<?> sprite) {
        double columnX = sprite.getBoundaries().getMaxX() + COLUMN_WIDTH;
        Point2D bottom = new Point2D( columnX, sprite.getBoundaries().getY() - 20 );
        DexSprite<?> next = null;
        do {
            next = findSprite( bottom );
            if (next != null)
                bottom = new Point2D( columnX, next.getBoundaries().getMaxY() + 5 );
        } while (next != null && bottom.getY() < spritePane.getHeight());

        return bottom;
    }

    private void mouseClick(MouseEvent e) {
        log.debug( "Mouse click on at: " + e.getX() + " " + e.getY() );
        // The whole canvas is active, check boundaries
        DexSprite<?> selected = null;
        for (DexSprite<?> sprite : activeSprites)
            if (sprite.contains( new Point2D( e.getX(), e.getY() ) )) {
                selected = sprite;
                break;
            }
        if (selected != null) {
            if (e.getButton() == MouseButton.SECONDARY)
                log.debug( "TODO - secondary button click." );
            else if (e.getClickCount() >= 2) {
                log.debug( "Throw event: " + selected.getMember() );
                publishEvent( new DexMemberSelectionEvent( selected.getMember() ) );
            } else
                selected.findAndRunRectangle( e );
        }
    }

    /**
     * Put all sprites onto the parent node
     */
    public void post() {
        log.debug( "Posting all sprites." );
        for (DexSprite<?> sprite : activeSprites)
            if (sprite != null) {
                sprite.render();
                spritePane.getChildren().add( sprite.render() );
            }
    }

    protected void publishEvent(DexEvent event) {
        if (parentController != null)
            parentController.publishEvent( event );
    }

    public void refresh() {
        for (DexSprite<?> sprite : activeSprites) {
            sprite.clear();
            sprite.render();
        }
    }

    public void remove(DexSprite<?> sprite) {
        if (sprite != null) {
            removeConnection( sprite );
            sprite.clear();
            activeSprites.remove( sprite );
        }
    }

    public void remove(OtmLibraryMember member) {
        DexSprite<?> sprite = findSprite( member );
        remove( sprite );
    }

    public void removeConnection(DexSprite<?> sprite) {
        List<Connection> list = new ArrayList<>( connections );
        for (Connection c : list)
            if (c.contains( sprite ))
                connections.remove( c );
        updateConnections();
    }

    public void update(Color color) {
        defaultGC.setFill( color );
        for (DexSprite<?> sprite : activeSprites)
            sprite.setBackgroundColor( color );
        refresh();
    }

    public void updateConnections() {
        eraseConnections();
        for (Connection c : connections) {
            c.draw( connectionsGC );
        }
    }

    public void updateConnections(DexSprite<?> sprite) {
        for (Connection c : connections) {
            c.update( sprite, connectionsGC, backgroundColor );
        }
    }

}
