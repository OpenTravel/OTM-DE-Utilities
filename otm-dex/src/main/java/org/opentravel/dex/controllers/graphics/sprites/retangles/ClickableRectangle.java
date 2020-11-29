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
import org.opentravel.dex.controllers.graphics.sprites.MemberSprite;
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
    private boolean bold = false;
    private boolean italic = false;
    // protected Font font;

    private double initialWidth;
    protected double labelWidth;

    // TODO - use settings manager
    double typeMargin = 8; // distance between property name and type

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
    public ClickableRectangle(DexSprite parent, String label, Image icon, double width, boolean bold, boolean italic) {
        super( 0, 0, width, 0 );
        this.sprite = parent;
        this.label = label;
        this.icon = icon;
        this.width = width;
        this.initialWidth = width;
        this.bold = bold;
        this.italic = italic;

        if (parent == null)
            throw new IllegalArgumentException( "Must have parent sprite." );

        this.settings = parent.getSettingsManager();
        if (settings == null)
            throw new IllegalArgumentException( "Must have settings" );

        // TODO - is font ever used?
        // this.font = parent.getFont();
        // if (italic) {
        // this.font = parent.getItalicFont();
        // this.label += " (i)";
        // }
        margin = settings.getMargin( Margins.PROPERTY );
        offset = settings.getOffset( Offsets.PROPERTY );
        connectorSize = GraphicsUtils.drawConnector( null, null, connectorSize, 0, 0 ).getX();
    }

    public ClickableRectangle(DexSprite parent, String label, Image icon, double width) {
        this( parent, label, icon, width, false, false );
    }

    /**
     * Draw a connector symbol at the end of the rectangle.
     * <p>
     * width from this rectangle, not the passed one.
     * 
     * @param gc
     * @param rec
     * 
     * @param connectorColor
     * @return
     */
    protected Point2D drawConnector(GraphicsContext gc, Rectangle rec, Paint connectorColor) {
        double fx = rec.getX() + width - margin - connectorSize - connectorSize / 2;
        double fy = rec.getMaxY() - margin - connectorSize;
        connectionPoint = GraphicsUtils.drawConnector( gc, connectorColor, connectorSize, fx, fy );
        return connectionPoint;
    }

    protected double drawConnectorLabel(GraphicsContext gc, Rectangle rec, String label, Image icon,
        boolean inherited) {
        double rightOffset = 3 * margin + connectorSize;
        LabelRectangle tRect = new LabelRectangle( sprite, label, icon, false, inherited, true );
        tRect.draw( null ); // Get size
        tRect.drawOutline( gc, false );
        double fx = rec.getX() + width - rightOffset;
        double tx = fx - tRect.getWidth();
        tRect.draw( gc, tx, y );

        double addedWidth = tRect.getWidth() + typeMargin;
        if (gc == null) {
            double actualWidth = labelWidth + tRect.getWidth() + typeMargin + rightOffset;
            this.width = width > actualWidth ? width : actualWidth;
        }
        return addedWidth;
    }

    /**
     * Draw Underline
     * 
     * @param width if 0, use width of rectangle
     */
    protected void drawUnderline(GraphicsContext gc, Rectangle rec, double width, double margin) {
        double y = rec.getMaxY() - margin;
        double x = rec.getX() + margin / 2;
        double maxX = x + width - connectorSize - 2 * margin;
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
     * {@link #drawLabel(GraphicsContext, double, double)} then add mouse click event handler for target.
     * 
     * @param target property rectangle
     * @return
     */
    protected LabelRectangle drawLabel(GraphicsContext gc, double x, double y, PropertyRectangle target) {
        LabelRectangle lr = this.drawLabel( gc, x, y );
        if (gc != null && sprite instanceof MemberSprite) {
            this.setOnMouseClicked( e -> ((MemberSprite<?>) sprite).connect( target ) );
            sprite.add( this );
        }
        return lr;
    }

    /**
     * {@link #drawLabel(GraphicsContext, double, double)} then add mouse click event handler for target.
     * 
     * @param target domain name string (base namespace)
     * @return
     */
    protected LabelRectangle drawLabel(GraphicsContext gc, double x, double y, String target) {
        LabelRectangle lr = this.drawLabel( gc, x, y );
        if (gc != null && sprite instanceof DomainSprite) {
            this.setOnMouseClicked( e -> ((DomainSprite) sprite).connect( this, target ) );
            sprite.add( this );
        }
        return lr;
    }

    /**
     * {@link #drawLabel(GraphicsContext, double, double)} then add mouse click event handler for target.
     * 
     * @param target domain name string (base namespace)
     * @return
     */
    protected LabelRectangle drawLabel(GraphicsContext gc, double x, double y, DomainSprite target) {
        LabelRectangle lr = this.drawLabel( gc, x, y );
        if (gc != null && sprite instanceof DomainSprite) {
            if (this instanceof DomainProvidersCR)
                this.setOnMouseClicked( e -> ((DomainSprite) sprite).connect( (DomainProvidersCR) this, target ) );
            else if (this instanceof DomainUsersCR)
                this.setOnMouseClicked( e -> ((DomainSprite) sprite).connect( (DomainUsersCR) this, target ) );
            sprite.add( this );
        }
        return lr;
    }

    /**
     * Draw the label and icon. If gc == null, reset then adjust the width.
     * 
     * @param gc
     * @param x
     * @param y
     * @return
     */
    protected LabelRectangle drawLabel(GraphicsContext gc, double x, double y) {
        this.x = x;
        this.y = y;
        if (gc == null)
            width = initialWidth; // Allow running with null GC multiple times have the same result.

        LabelRectangle lr = null;
        lr = new LabelRectangle( sprite, label, icon, bold, italic, false );
        lr.draw( gc, x, y );

        height = lr.getHeight() + margin;
        labelWidth = lr.getWidth();
        if (gc == null)
            width = lr.getWidth() > width ? lr.getWidth() : width;

        return lr;
    }

    @Override
    public String toString() {
        return "Clickable: " + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
