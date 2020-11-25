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
import org.opentravel.dex.controllers.graphics.sprites.DomainSprite;
import org.opentravel.dex.controllers.graphics.sprites.GraphicsUtils;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Margins;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Offsets;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;

/**
 * Click-able graphics rectangle for single line properties with connectors.
 * <p>
 * <ol>
 * <li>Sprite manager of the parent will receive mouse click.
 * <li>Sprite manager will attempt to find the sprite that contains the X,Y of the click.
 * <li>If found, the selected sprite's {@link DexSprite#findAndRunRectangle(javafx.scene.input.MouseEvent)} will be run.
 * <li>DexSpriteBase will attempt to find the rectangle that contains the X,Y
 * <li>The selected rectangle's {@link Rectangle#onMouseClicked(javafx.scene.input.MouseEvent)} is invoked.
 * <li>The rectangle must have an event handler {@link Rectangle#setOnMouseClicked(RectangleEventHandler)}
 * </ol>
 * 
 * @author dmh
 * @param <O>
 *
 */
// TODO - merge this with FacetRectangle
// TODO - merge - use instead of hand-jam code in propertyRec
public abstract class ClickableRectangle extends Rectangle {
    private static Log log = LogFactory.getLog( ClickableRectangle.class );

    protected DexSprite sprite;
    protected String label;
    protected Image icon;

    protected SettingsManager settings;
    protected double margin;
    protected double offset;
    // protected boolean collapsed = false;

    double connectorSize = SettingsManager.CONNECTOR_SIZE;

    Paint connectorColor = GraphicsUtils.CONNECTOR_COLOR; // TODO

    /**
     * 
     * @param obj - must have object
     * @param parent - parent sprite for connections
     * @param width - overrides actual width if gc != null when drawn
     */
    public ClickableRectangle(DexSprite parent, String label, Image icon, double width) {
        super( 0, 0, width, 0 );
        this.sprite = parent;
        this.label = label;
        this.icon = icon;
        this.width = width;

        if (parent == null)
            throw new IllegalArgumentException( "Must have parent sprite." );

        this.settings = parent.getSettingsManager();
        if (settings == null)
            throw new IllegalArgumentException( "Must have settings" );

        margin = settings.getMargin( Margins.PROPERTY );
        offset = settings.getOffset( Offsets.PROPERTY );
        connectorSize = GraphicsUtils.drawConnector( null, null, connectorSize, 0, 0 ).getX();
    }

    /**
     * Draw a connector symbol at the end of the rectangle.
     * 
     * @param gc
     * @param rec
     * @param width
     * @param connectorColor
     * @return
     */
    protected Point2D drawConnector(GraphicsContext gc, Rectangle rec, double width, Paint connectorColor) {
        double fx = rec.getX() + width - 2 * connectorSize;
        double fy = rec.getMaxY() - margin - connectorSize;
        connectionPoint = GraphicsUtils.drawConnector( gc, connectorColor, connectorSize, fx, fy );
        return connectionPoint;
    }

    /**
     * Draw Underline
     * 
     * @param width if 0, use width of rectangle
     */
    protected void drawUnderline(GraphicsContext gc, Rectangle rec, double width, double margin) {
        double y = rec.getMaxY() - margin;
        double x = rec.getX() + margin / 2;
        double connectorWidth = 16;
        double maxX = x + width - margin - 2 * connectorWidth;
        if (width <= 0)
            maxX = rec.getMaxX();
        if (gc != null)
            gc.strokeLine( x, y, maxX, y );
    }

    //
    /**
     * Draw a vertical line just inside the passed rectangle used for the property.
     * 
     * @param gc
     * @param rec
     */
    protected void drawVerticalLine(GraphicsContext gc, Rectangle rec, double margin) {
        double fx = rec.getX() + margin / 2;
        double y = rec.getY() - 1;
        double maxY = rec.getMaxY() - 1;
        if (gc != null) {
            gc.strokeLine( fx, y, fx, maxY );
        }
    }

    /**
     * Draw the label and icon. If there is a sprite, add mouse click handler event. If gc == null, adjust the width.
     * 
     * @param gc
     * @param x
     * @param y
     * @param target
     * @return
     */
    protected LabelRectangle drawLabel(GraphicsContext gc, double x, double y, String target) {
        this.x = x;
        this.y = y;

        LabelRectangle lr = null;
        lr = new LabelRectangle( sprite, label, icon, false, false, false );
        lr.draw( gc, x, y );

        height = lr.getHeight() + margin;
        if (gc == null)
            width = lr.getWidth() > width ? lr.getWidth() : width;

        if (sprite != null && target != null) {
            this.setOnMouseClicked( e -> ((DomainSprite) sprite).connect( this, target ) );
            sprite.add( this );
        }
        return lr;
    }

    @Override
    public String toString() {
        return "Clickable: " + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
