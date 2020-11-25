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

package org.opentravel.dex.controllers.graphics.sprites.retangles;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.graphics.sprites.DexSprite;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

/**
 * Graphics utility for containing regions (x, y, width, height). A rectangle does <b>not</b> have a canvas.
 * <p>
 * Sub-types have contents that can be drawn into the rectangle. These rectangles will compute their size when
 * constructed and when drawn with a null GraphicsContext (GC). A rectangle may be mouse click-able if the parent sprite
 * is passed when constructing the rectangle.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class Rectangle {
    private static Log log = LogFactory.getLog( Rectangle.class );

    /**
     * Render methods that create rectangles may set the event to run if they implement this interface.
     * <p>
     * Example to run remove on click: r.setOnMouseClicked( e -> manager.remove( this ) );
     * <p>
     * Mouse clicks are captured by sprite manager. It uses {@link DexSprite#findAndRunRectangle()} to find the
     * rectangle at the event's X and Y then runs {@link Rectangle#onMouseClicked(MouseEvent)}. onMouseClicked() will
     * run with the event the method that was the argument to {@link #setOnMouseClicked(RectangleEventHandler)} .
     */
    public abstract interface RectangleEventHandler {
        public void onRectangleClick(MouseEvent e); // the functional interface implemented by lambda expression
    }

    protected double x;
    protected double y;
    protected double width;
    protected double height;
    protected RectangleEventHandler eventHandler = null;
    protected Point2D connectionPoint = null;

    public Rectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
    }

    public void addWidth(double added) {
        width += added;
    }

    /**
     * @param x2
     * @param y2
     * @return
     */
    public boolean contains(double x, double y) {
        return contains( new Point2D( x, y ) );
    }

    public boolean contains(Point2D point) {
        if (point.getX() < x)
            return false;
        if (point.getX() > getMaxX())
            return false;
        if (point.getY() < y)
            return false;
        if (point.getY() > getMaxY())
            return false;
        return true;
    }

    /**
     * Draw this rectangle. If not overridden, draw outline with filled set to false.
     * <p>
     * Should be overridden by sub-types.
     * 
     * @param gc
     * @return this rectangle
     */
    public Rectangle draw(GraphicsContext gc) {
        drawOutline( gc, false );
        return this;
    }

    // /**
    // * Draw around the rectangle.
    // * <p>
    // * Should be overridden by sub-types.
    // *
    // * @param gc
    // * @param filled
    // */
    // @Deprecated
    // public Rectangle draw(GraphicsContext gc, boolean filled) {
    // if (gc != null) {
    // if (filled)
    // gc.fillRect( x, y, width, height );
    // else
    // gc.strokeRect( x, y, width, height );
    // }
    // return this;
    // }

    /**
     * Draw around the rectangle.
     * 
     * @param gc
     * @param filled
     */
    public final Rectangle drawOutline(GraphicsContext gc, boolean filled) {
        if (gc != null) {
            if (filled)
                gc.fillRect( x, y, width, height );
            else
                gc.strokeRect( x, y, width, height );
        }
        return this;
    }

    /**
     * Set x,y then Draw with filled set to false.
     * 
     * @param gc
     * @return
     */
    public Rectangle draw(GraphicsContext gc, double x, double y) {
        set( x, y );
        return draw( gc );
    }

    public Point2D getConnectionPoint() {
        return connectionPoint;
    }

    public double getHeight() {
        return height;
    }

    public double getMaxX() {
        return x + width;
    }

    public double getMaxY() {
        return y + height;
    }


    public double getWidth() {
        return width;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    /**
     * @return <b>NEW</b> connection point with delta added
     */
    public Point2D moveConnectionPoint(double deltaX, double deltaY) {
        // log.debug( "Connection point move: " + connectionPoint );
        if (connectionPoint != null)
            connectionPoint = connectionPoint.add( deltaX, deltaY );
        // log.debug( "New Connection point : " + connectionPoint );
        return connectionPoint;
    }

    /**
     * Run the method that was the argument to {@link #setOnMouseClicked(RectangleEventHandler)} with the event.
     * <p>
     * If a rectangle does not respond to click, check to see if it has an event handler. If it does, make sure
     * DexSpriteBase can find the rectangle.
     * 
     * @param e
     */
    public final void onMouseClicked(MouseEvent e) {
        if (e != null && eventHandler != null)
            eventHandler.onRectangleClick( e );
        else
            log.warn( "Missing event handler for mouse click." );
        // log.debug( "Mouse clicked. " + e.toString() );
    }

    public Rectangle set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Rectangle set(double x, double y, double width) {
        set( x, y );
        this.width = width;
        return this;
    }

    public final void setIfHigher(double height) {
        if (height > this.height)
            this.height = height;
    }

    public final void setIfLarger(Rectangle rectangle) {
        setIfWider( rectangle.getWidth() );
        setIfHigher( rectangle.getHeight() );
    }

    public final void setIfWider(double width) {
        if (width > this.width)
            this.width = width;
    }

    /**
     * Save in this rectangle a event handler to call on mouse click.
     * 
     * @param a
     */
    public final void setOnMouseClicked(RectangleEventHandler a) {
        eventHandler = a;
    }

    public String toString() {
        return "x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }

}
