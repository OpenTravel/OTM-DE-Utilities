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
import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.graphics.sprites.GraphicsUtils.DrawType;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Margins;
import org.opentravel.dex.controllers.graphics.sprites.connections.SuperTypeConnection;
import org.opentravel.dex.controllers.graphics.sprites.connections.TypeConnection;
import org.opentravel.dex.controllers.graphics.sprites.retangles.BaseTypeRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.ColumnRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.FacetRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.LabelRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.PropertyRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle.RectangleEventHandler;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Graphics Display Object (Sprite) for containing OTM library members.
 * 
 * @author dmh
 * @param <O>
 *
 */
public abstract class MemberSprite<M extends OtmLibraryMember> implements DexSprite, RectangleEventHandler {
    protected static Log log = LogFactory.getLog( MemberSprite.class );

    private static final double MIN_HEIGHT = 50;
    private static final double MIN_WIDTH = 50;

    private double x;
    private double y;
    protected M member;
    protected ColumnRectangle column;
    private Canvas canvas;
    protected GraphicsContext gc = null;

    private Rectangle boundaries = null;
    private boolean collapsed = false;

    private List<Rectangle> rectangles = new ArrayList<>();

    protected SpriteManager manager;
    protected SettingsManager settingsManager;

    /**
     * Initialize member sprite. Create canvas and GC parameters. Compute initial size. Create tool tip. Sub-types will
     * initialize settings using the manager's setting manager.
     * 
     * @param member
     * @param settingsManager, must <b>not</b> be null.
     */
    public MemberSprite(M member, SpriteManager manager) {
        this.member = member;
        this.manager = manager;
        this.settingsManager = manager.getSettingsManager();

        // Create canvas with configured GC
        canvas = new Canvas( MIN_WIDTH, MIN_HEIGHT );
        gc = canvas.getGraphicsContext2D();
        settingsManager.setGCParams( gc );

        // Compute size
        draw( null, 0, 0 );

        // Correct tool tip display relies on the canvas being clipped to this sprite's active boundaries
        String desc = member.getDescription();
        Tooltip t = new Tooltip();
        Tooltip.install( canvas, t );
        t.setText( member.getPrefix() + " : " + member.getNamespace() + "\n" + desc );
    }

    @Override
    public void add(Rectangle rectangle) {
        if (!rectangles.contains( rectangle ))
            rectangles.add( rectangle );
    }

    /**
     * Get or Create provider sprite if none exists. Draw connection to this sprite.
     * 
     * @param user
     * @return the provider sprite
     */
    public MemberSprite<OtmLibraryMember> addConnection(OtmTypeUser user) {
        // log.debug( "Adding connection to " + user );
        if (getColumn() == null || user == null || user.getAssignedType() == null || !(user instanceof OtmProperty))
            return null;

        OtmLibraryMember provider = user.getAssignedType().getOwningMember();
        if (provider == null || provider == user.getOwningMember())
            return null;

        MemberSprite<OtmLibraryMember> toSprite = manager.get( provider );
        if (!(toSprite instanceof MemberSprite)) {
            // Create new collapsed sprite and connect it
            toSprite = manager.add( provider, getColumn().getNext(), true );
            connect( (OtmProperty) user, this, toSprite );
        }
        return toSprite;
    }

    @Override
    public void clear() {
        gc.clearRect( 0, 0, canvas.getWidth(), canvas.getHeight() );
        rectangles.clear();
        boundaries = null;
        // do NOT remove from column...let caller do that
    }

    /**
     * Toggle collapsed state.
     */
    public void collapseOrExpand() {
        clear();
        collapsed = !collapsed;
        render();
        manager.updateConnections( this );
    }

    /**
     * Toggle collapsed state.
     */
    @Override
    public void collapseOrExpand(OtmFacet<?> f) {
        log.debug( "Collapse or expand." );
        if (f != null) {
            clear();
            f.setExpanded( !f.isExpanded() );
            // collapsed = !collapsed;
            render();
            manager.updateConnections( this );
            log.debug( "Collapse or expand: " + f + " to " + f.isExpanded() );
        }
    }


    /**
     * Utility to compute the wider width. Returns
     * 
     * @param width - current effective with
     * @param rect - rectangle with width that may be larger that current effective width
     * @param offsetX - added to rectangle's width
     * @return the computed effective width
     */
    public static double computeWidth(double width, Rectangle rect, double offsetX) {
        if (rect != null)
            width = rect.getWidth() + offsetX > width ? rect.getWidth() + offsetX : width;
        return width;
    }

    @Override
    public DexSprite connect() {
        if (!(getMember().getBaseType() instanceof OtmLibraryMember))
            return null;

        DexSprite baseSprite = manager.get( (OtmLibraryMember) member.getBaseType() );
        if (baseSprite == null) {
            baseSprite = manager.add( (OtmLibraryMember) member.getBaseType(), getColumn() );
        } else
            baseSprite.setCollapsed( !baseSprite.isCollapsed() );
        if (baseSprite != null) {
            manager.addAndDraw( new SuperTypeConnection( baseSprite, this ) );
            baseSprite.getCanvas().toFront();
            baseSprite.refresh();
        }
        return baseSprite;
    }

    /**
     * Find the property's rectangle and make type connection
     * 
     * @param property
     * @param from
     * @param to
     * @return
     */
    private TypeConnection connect(OtmProperty property, DexSprite from, DexSprite to) {
        TypeConnection c = null;
        PropertyRectangle fRect;
        fRect = from.get( property );
        if (fRect != null) {
            c = new TypeConnection( fRect, from, to );
            manager.addAndDraw( c );
        }
        return c;
    }

    @Override
    public MemberSprite<OtmLibraryMember> connect(PropertyRectangle pRect) {
        log.debug( "Connecting property " + pRect );
        if (pRect == null || pRect.getProperty() == null || pRect.getProvider() == null)
            return null;
        if (getColumn() == null)
            return null;

        OtmProperty user = pRect.getProperty();
        OtmLibraryMember provider = pRect.getProvider().getOwningMember();
        if (provider == null || provider == user.getOwningMember())
            return null;

        MemberSprite<OtmLibraryMember> toSprite = manager.get( provider );
        if (toSprite == null) {
            // Place the new sprite and connect it
            toSprite = manager.add( provider, getColumn().getNext(), collapsed );
            if (toSprite != null)
                manager.addAndDraw( new TypeConnection( pRect, this, toSprite ) );
        } else {
            toSprite.setCollapsed( !toSprite.isCollapsed() );
        }
        if (toSprite != null) {
            toSprite.getCanvas().toFront();
            toSprite.refresh();
        }
        return toSprite;
    }

    @Override
    public boolean contains(Point2D point) {
        return boundaries.contains( point );
    }

    @Override
    public Rectangle draw(GraphicsContext gc, double x, double y) {
        set( x, y );
        return drawMember( gc );
    }

    /**
     * Draw the facets and properties for this member. Start at the passed x, y.
     * <p>
     * This must be implemented by sub-types to draw their object type specific contents.
     * 
     * @param gc if null, compute size. If not-null, draw within boundaries.
     * @param x
     * @param y
     * @return rectangle around all contents
     */
    public abstract Rectangle drawContents(GraphicsContext gc, final double x, final double y);

    /**
     * Draw close and collapse active rectangles.
     * 
     * @param boundaries
     * @param cgc
     * @return
     */
    public double drawControls(Rectangle boundaries, GraphicsContext cgc) {
        Image close = ImageManager.getImage( ImageManager.Icons.CLOSE );
        Image collapse = ImageManager.getImage( ImageManager.Icons.COLLAPSE );

        // Start at right edge and work backwards
        double margin = settingsManager.getMargin( Margins.MEMBER );
        double cy = boundaries.getY() + margin;

        double cx = boundaries.getMaxX() - margin - close.getWidth();
        Rectangle r = GraphicsUtils.drawImage( close, DrawType.OUTLINE, cgc, cx, cy );
        rectangles.add( r );
        r.setOnMouseClicked( e -> manager.remove( this ) );
        double width = r.getWidth() + margin;

        cx = r.getX() - collapse.getWidth();
        r = GraphicsUtils.drawImage( collapse, DrawType.OUTLINE, cgc, cx, cy );
        rectangles.add( r );
        r.setOnMouseClicked( e -> collapseOrExpand() );
        width += r.getWidth() + margin;

        return width;
    }

    /**
     * Draw background, label and controls.
     * 
     * @param gc
     * @return
     */
    private Rectangle drawMember(GraphicsContext gc) {
        if (member == null)
            return new Rectangle( 0, 0, 0, 0 );
        if (boundaries == null)
            // Use minimum boundaries
            boundaries = new Rectangle( x, y, MIN_WIDTH, MIN_HEIGHT );

        // Draw background box
        if (gc != null) {
            Rectangle bRect = new Rectangle( boundaries.getX(), boundaries.getY(),
                boundaries.getWidth() + settingsManager.getMargin( Margins.FACET ),
                boundaries.getHeight() + settingsManager.getMargin( Margins.FACET ) );
            bRect.draw( gc, false ); // Outline
            bRect.draw( gc, true ); // Fill
        }

        // TODO - DRAW property for users if any
        // int users = member.getWhereUsed().size();
        double px = boundaries.getX();
        // if (gc != null && !collapsed && users > 0) {
        // // TODO
        // // create rectangle to contain the connector
        // // add to rectangles
        // // create mouse handler (thows event?)
        // GraphicsUtils.drawConnector( gc, gc.getFill(), settingsManager.getConnectorSize(), px, y );
        // // LabelRectangle usersR =
        // // new LabelRectangle( this, users + " Users", null, member.isEditable(), false, false );
        // // usersR.draw( gc, boundaries.getX(), y + height );
        // // height += usersR.getHeight();
        // px += settingsManager.getConnectorSize();
        // }

        // Draw the name of the object
        Rectangle mRect =
            new LabelRectangle( this, member.getName(), member.getIcon(), member.isEditable(), false, false ).draw( gc,
                px, y );
        // GraphicsUtils.drawLabel( member.getName(), member.getIcon(), member.isEditable(), false, gc, font, x, y );
        double width = mRect.getWidth();
        double height = mRect.getHeight();

        // Add the controls
        double cWidth = drawControls( boundaries, gc ) + settingsManager.getMargin( Margins.MEMBER );
        width += cWidth;

        // prefix
        px = boundaries.getMaxX() - cWidth;
        LabelRectangle pRect = new LabelRectangle( this, member.getPrefix(), null, member.isEditable(), false, false );
        px -= pRect.getWidth() + settingsManager.getMargin( Margins.LABEL );
        pRect.draw( gc, px, y );
        width += pRect.getWidth();

        // Draw property for base type if any
        if (!collapsed && member.getBaseType() != null) {
            mRect = new BaseTypeRectangle( this, member, width );
            mRect.set( boundaries.getMaxX() - mRect.getWidth(), y + height ).draw( gc, true );
            width = computeWidth( width, mRect, 0 );
            height += mRect.getHeight();
        }

        // Show content (facets, properties, etc)
        mRect = drawContents( gc, boundaries.getX(), y + height );
        if (mRect != null) {
            width = computeWidth( width, mRect, settingsManager.getMargin( Margins.FACET ) );
            height += mRect.getHeight();
        }

        // Handler for canvas layer
        if (manager != null) {
            canvas.setOnMouseDragged( manager::drag );
            canvas.setOnDragDetected( manager::dragStart );
            canvas.setOnMouseReleased( manager::dragEnd );
            // Clicks go to the top most node...so let the pane catch them
            // canvas.setOnMouseClicked( this::mouseClick );
        }
        // log.debug( "Refreshed " + member );
        boundaries = new Rectangle( x, y, width, height );
        // boundaries.draw( gc, false );

        // Clip the canvas to just have the sprite
        double clipX = boundaries.getX() - 4;
        double clipY = boundaries.getY() - 4;
        double clipW = boundaries.getWidth() + 8 + settingsManager.getMargin( Margins.FACET );
        double clipH = boundaries.getHeight() + 8 + settingsManager.getMargin( Margins.FACET );
        canvas.setClip( new javafx.scene.shape.Rectangle( clipX, clipY, clipW, clipH ) );

        return boundaries;
    }

    @Override
    public Rectangle find(double x, double y) {
        Rectangle selected = null;
        for (Rectangle r : rectangles)
            if (r.contains( x, y )) {
                selected = r;
                break;
            }
        return selected;
    }

    @Override
    public PropertyRectangle get(OtmProperty property) {
        for (Rectangle r : rectangles)
            if (r instanceof PropertyRectangle && ((PropertyRectangle) r).getProperty() == property)
                return ((PropertyRectangle) r);
        return null;
    }

    public void findAndRunRectangle(MouseEvent e) {
        Rectangle selected = find( e.getX(), e.getY() );
        if (selected != null)
            selected.onMouseClicked( e );
    }

    @Override
    public Rectangle getBoundaries() {
        return boundaries;
    }

    @Override
    public Canvas getCanvas() {
        return canvas;
    }

    @Override
    public ColumnRectangle getColumn() {
        return column;
    }

    public List<FacetRectangle> getFacetRectangles() {
        List<FacetRectangle> list = new ArrayList<>();
        rectangles.forEach( r -> {
            if (r instanceof FacetRectangle)
                list.add( (FacetRectangle) r );
        } );
        return list;
    }

    @Override
    public Font getFont() {
        if (gc.getFont() == null)
            return settingsManager.getFont();
        return gc.getFont();
    }

    @Override
    public double getHeight() {
        return boundaries != null ? boundaries.getHeight() : 0;
    }

    @Override
    public Font getItalicFont() {
        return settingsManager.getItalicFont();
    }

    public M getMember() {
        return member;
    }

    @Override
    public List<Rectangle> getRectangles() {
        return rectangles;
    }

    @Override
    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    @Override
    public double getWidth() {
        return boundaries != null ? boundaries.getWidth() : 0;
    }

    @Override
    public double getX() {
        return boundaries != null ? boundaries.getX() : 0;
    }

    @Override
    public double getY() {
        return boundaries != null ? boundaries.getY() : 0;
    }

    @Override
    public boolean isCollapsed() {
        return collapsed;
    }


    // public void todo(GraphicsContext gc) {
    // MouseEvent event = null;
    // ArrayList<Circle> listOfCircles = new ArrayList<>();
    // for (Circle circle : listOfCircles) {
    // Point2D point2D = new Point2D( event.getX(), event.getY() );
    // if (circle.contains( point2D )) {
    // // log.debug( "circle clicked" );
    // }
    // }
    // }

    @Override
    public void onRectangleClick(MouseEvent e) {
        // log.debug( "Rectangle click at: " + e.getX() + " " + e.getY() );
    }

    @Override
    public void refresh() {
        clear();
        render();
    }

    @Override
    public Canvas render() {
        // log.debug( "Rendering at " + x + " " + y + " sprite for: " + member );
        if (member == null || manager == null)
            return null;

        if (boundaries == null)
            draw( null, x, y );

        // Size Canvas
        Rectangle canvasR = new Rectangle( x, y, boundaries.getWidth() + settingsManager.getMargin( Margins.CANVAS ),
            boundaries.getHeight() + settingsManager.getMargin( Margins.CANVAS ) );
        canvas.setHeight( y + canvasR.getHeight() );
        canvas.setWidth( x + canvasR.getWidth() );
        // log.debug( "Sized canvas: " + canvasR );

        drawMember( gc );
        manager.updateConnections( this );
        // log.debug( "Rendered " + member + " at " + getBoundaries() );
        return canvas;
    }

    @Override
    public Canvas render(ColumnRectangle column) {
        this.column = column;
        Point2D p = column.getNextInColumn();
        this.x = p.getX();
        this.y = p.getY();
        boundaries = null;
        return render();
    }

    @Override
    public void set(double x, double y) {
        // Not all rectangles are saved at sprite level
        // rectangles.forEach( r -> r.moveConnectionPoint( this.x - x, this.y - y ) );
        this.x = x;
        this.y = y;
    }

    @Override
    public void set(Font font) {
        gc.setFont( font );
    }

    @Override
    public void setBackgroundColor(Color color) {
        gc.setFill( color );
    }


    /**
     * If width or height are 0 then compute new values
     * 
     * @param width if 0, compute
     * @param height if 0, compute
     */
    private void setBoundaries(double width, double height) {
        // Use minimum boundaries
        boundaries = new Rectangle( x, y, MIN_WIDTH, MIN_HEIGHT );
        // Get size of sprite using minimum boundaries
        Rectangle ms = drawMember( null );
        // Set the true boundaries
        boundaries = new Rectangle( x, y, width == 0 ? ms.getWidth() : width, height == 0 ? ms.getHeight() : height );
        // boundaries.draw( gc, false );
    }

    @Override
    public void setCollapsed(boolean collapsed) {
        // log.debug( "Collapsed = " + collapsed + " " + this );
        this.collapsed = collapsed;
        if (!collapsed)
            getCanvas().toFront();

        // resize this sprite
        setBoundaries( 0, 0 );
        // log.debug( " became = " + this );
        manager.updateConnections( this );
    }

    public String toString() {
        return "Sprite for " + getMember() + " at " + getBoundaries();
    }
}
