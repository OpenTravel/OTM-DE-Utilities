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
import org.opentravel.dex.controllers.graphics.sprites.retangles.ColumnRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.LabelRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.PropertyRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle.RectangleEventHandler;
import org.opentravel.model.otmProperties.OtmProperty;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 * Graphics Display Object (Sprite) base class.
 * 
 * @author dmh
 *
 */
public abstract class DexSpriteBase implements DexSprite, RectangleEventHandler {
    private static Log log = LogFactory.getLog( DexSpriteBase.class );

    protected static final double MIN_HEIGHT = 50;
    protected static final double MIN_WIDTH = 50;

    protected double x;
    protected double y;

    protected ColumnRectangle column;
    protected Canvas canvas;
    protected GraphicsContext gc = null;

    protected Rectangle boundaries = null;
    protected boolean collapsed = false;

    protected List<Rectangle> rectangles = new ArrayList<>();

    protected SpriteManager manager;
    protected SettingsManager settingsManager;

    /**
     * Initialize sprite. Create canvas and GC parameters. Compute initial size. Create tool tip. Sub-types will
     * initialize settings using the manager's setting manager.
     * 
     * @param member
     * @param settingsManager, must <b>not</b> be null.
     */
    public DexSpriteBase(SpriteManager manager) {
        this.manager = manager;
        this.settingsManager = manager.getSettingsManager();

        // Create canvas with configured GC
        canvas = new Canvas( MIN_WIDTH, MIN_HEIGHT );
        gc = canvas.getGraphicsContext2D();
        settingsManager.setGCParams( gc );
    }

    @Override
    public void add(Rectangle rectangle) {
        if (!rectangles.contains( rectangle ))
            rectangles.add( rectangle );
    }

    @Override
    public void clear() {
        gc.clearRect( 0, 0, canvas.getWidth(), canvas.getHeight() );
        rectangles.clear();
        boundaries = null;
        // do NOT remove from column...let caller do that
    }

    public void clip(Canvas canvas, Rectangle boundaries) {
        // Clip the canvas to just have the sprite
        double clipX = boundaries.getX() - 4;
        double clipY = boundaries.getY() - 4;
        double clipW = boundaries.getWidth() + 8 + settingsManager.getMargin( Margins.FACET );
        double clipH = boundaries.getHeight() + 8 + settingsManager.getMargin( Margins.FACET );
        canvas.setClip( new javafx.scene.shape.Rectangle( clipX, clipY, clipW, clipH ) );
    }

    @Override
    public void collapseOrExpand() {
        clear();
        collapsed = !collapsed;
        render();
        manager.updateConnections( this );
    }

    @Override
    public boolean contains(Point2D point) {
        return boundaries.contains( point );
    }

    /**
     * Draw the bounding box, title, icon, prefix if any. Does <b>not</b> draw the contents.
     * <p>
     * Sets boundaries to fit the title line.
     * 
     * @param gc if null compute size, otherwise draw
     * @param color of the bounding box
     * @param name
     * @param icon
     * @param prefix
     * @param editable if true the text is in bold
     * @return y value to start drawing content.
     */
    public double drawSprite(GraphicsContext gc, Paint color, String name, Image icon, String prefix,
        boolean editable) {
        if (boundaries == null)
            boundaries = new Rectangle( x, y, MIN_WIDTH, MIN_HEIGHT );

        // Draw background box
        if (gc != null) {
            Paint p = gc.getFill();
            gc.setFill( color );
            Rectangle bRect = new Rectangle( boundaries.getX(), boundaries.getY(),
                boundaries.getWidth() + settingsManager.getMargin( Margins.FACET ),
                boundaries.getHeight() + settingsManager.getMargin( Margins.FACET ) );
            bRect.draw( gc, false ); // Outline
            bRect.draw( gc, true ); // Fill
            gc.setFill( p );
            // Clip the canvas so tool tips (and mouse clicks) can go the right sprite
            clip( canvas, boundaries );
        }

        // Draw the name of the object
        Rectangle lr = new LabelRectangle( this, name, icon, editable, false, false ).draw( gc, x, y );

        // Add the controls
        double cWidth = drawControls( boundaries, gc ) + settingsManager.getMargin( Margins.MEMBER );

        // prefix
        LabelRectangle pRect = null;
        if (prefix != null) {
            double px = boundaries.getMaxX() - cWidth;
            pRect = new LabelRectangle( this, prefix, null, editable, false, false );
            px -= pRect.getWidth() + settingsManager.getMargin( Margins.LABEL );
            pRect.draw( gc, px, y );
        }

        // Adjust width
        if (gc == null) {
            boundaries.setIfLarger( lr );
            boundaries.addWidth( cWidth );
            if (pRect != null)
                boundaries.addWidth( pRect.getWidth() );
        }

        return y + lr.getHeight();
    }

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
     * Utility to draw or compute size of a rectangle.
     * 
     * @param rect
     * @param gc
     * @param width
     * @param x
     * @param dx
     * @param y
     * @return actual width if gc = null, width argument otherwise
     */
    public double draw(Rectangle rect, GraphicsContext gc, double width, double x, double dx, double y) {
        rect.set( x + dx, y );
        rect.draw( gc, true );
        if (gc == null && rect.getWidth() > width)
            width = rect.getWidth() + dx;
        return width;
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

    public void findAndRunRectangle(MouseEvent e) {
        Rectangle selected = find( e.getX(), e.getY() );
        if (selected != null)
            selected.onMouseClicked( e );
    }

    @Override
    public PropertyRectangle get(OtmProperty property) {
        for (Rectangle r : rectangles)
            if (r instanceof PropertyRectangle && ((PropertyRectangle) r).getProperty() == property)
                return ((PropertyRectangle) r);
        return null;
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


    @Override
    public void refresh() {
        clear();
        render();
    }

    @Override
    public Canvas render(ColumnRectangle column, boolean collapsed) {
        if (column == null)
            throw new IllegalArgumentException( "Required column missing to render into." );

        // What to do if the sprite is in a different column?
        if (this.column == column)
            log.debug( "TODO handle removing from column." );
        setCollapsed( collapsed );
        Point2D p = column.getNextInColumn();
        column.add( this );
        this.column = column;
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

    @Override
    public void setCollapsed(boolean collapsed) {
        if (this.collapsed != collapsed) {
            collapseOrExpand();
        }
    }

}
