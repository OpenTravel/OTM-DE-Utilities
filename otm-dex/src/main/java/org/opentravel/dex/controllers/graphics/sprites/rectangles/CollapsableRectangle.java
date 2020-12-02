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

package org.opentravel.dex.controllers.graphics.sprites.rectangles;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.graphics.sprites.DexSprite;
import org.opentravel.dex.controllers.graphics.sprites.DomainSprite;
import org.opentravel.dex.controllers.graphics.sprites.GraphicsUtils;
import org.opentravel.dex.controllers.graphics.sprites.GraphicsUtils.DrawType;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Margins;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Offsets;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;

/**
 * Collapsable graphics rectangle for containing virtual facets. Virtual facets are displayed as facets, but not modeled
 * as facets.
 * 
 * @author dmh
 * @param <O>
 *
 */
public abstract class CollapsableRectangle extends Rectangle {
    private static Log log = LogFactory.getLog( CollapsableRectangle.class );

    protected DexSprite parent;
    protected String label;
    protected Image icon;

    private SettingsManager settings;
    protected double margin;
    protected double offset;
    protected boolean collapsed = false;

    double connectorSize = SettingsManager.CONNECTOR_SIZE;

    /**
     * 
     * @param obj - must have object
     * @param parent - must have parent with member
     * @param width - overrides actual width if gc != null when drawn
     */
    public CollapsableRectangle(DexSprite parent, double width, String label, Image icon) {
        super( 0, 0, width, 0 );
        this.parent = parent;
        this.icon = icon;
        this.label = label;
        this.width = width;

        if (parent == null)
            throw new IllegalArgumentException( "Must have parent sprite." );

        this.collapsed = parent.isCollapsed();
        this.settings = parent.getSettingsManager();
        if (settings == null)
            throw new IllegalArgumentException( "Must have settings" );

        margin = settings.getMargin( Margins.FACET );
        offset = settings.getOffset( Offsets.FACET );
    }

    public void collapseOrExpand() {
        collapsed = !collapsed;
        if (parent instanceof DomainSprite)
            ((DomainSprite) parent).collapseOrExpand( this );
        // TODO - should the rectangle be a param on the base class?
    }

    /**
     * Draw the background and title line ({@link #drawTitleLine(GraphicsContext)}
     * <p>
     * Sub-types should call this before drawing facet members/properties.
     * 
     * @see org.opentravel.dex.controllers.graphics.sprites.rectangles.Rectangle#draw(javafx.scene.canvas.GraphicsContext)
     */
    @Override
    public Rectangle draw(GraphicsContext gc) {

        drawBackground( gc );

        height = 0;

        // Title line - control, name
        Rectangle r = drawTitleLine( gc );
        if (gc == null && r.getWidth() > width)
            width = r.getWidth();
        height += r.getHeight();

        // sub-types to provide Properties
        return this;
    }

    protected void drawBackground(GraphicsContext gc) {
        if (gc != null) {
            Paint savedColor = gc.getFill();
            // gc.setFill( javafx.scene.paint.Color.WHITE );
            gc.setFill( settings.getColor( this ) );

            // super.drawOutline( gc, false ); // draw outline
            super.drawOutline( gc, true ); // Draw fill

            gc.setFill( savedColor );
            // log.debug( "Drew background " + this );
        }
    }

    // TODO - merge - use instead of hand-jam code in propertyRec
    protected Point2D drawConnector(GraphicsContext gc, Rectangle rec, double width) {
        Paint connectorColor = GraphicsUtils.CONNECTOR_COLOR; // TODO
        double fx = rec.getX() + width - 2 * connectorSize;
        double fy = rec.getMaxY() - margin - connectorSize;
        connectionPoint = GraphicsUtils.drawConnector( gc, connectorColor, connectorSize, fx, fy );
        return connectionPoint;
    }

    // Merge - just make collapseOrExpand abstract
    private double drawControl(GraphicsContext gc) {
        Rectangle r = null;
        Image cIcon = null;

        if (!collapsed)
            cIcon = ImageManager.getImage( ImageManager.Icons.COLLAPSE );
        else
            cIcon = ImageManager.getImage( ImageManager.Icons.EXPAND );

        r = GraphicsUtils.drawImage( cIcon, DrawType.OUTLINE, gc, x, y + margin );
        if (gc != null && parent instanceof DomainSprite) {
            parent.add( r );
            r.setOnMouseClicked( e -> collapseOrExpand() );
        }
        return r != null ? r.getWidth() : 0;
    }

    // Merge - get prefix from globals then add from facetRectangle
    protected Rectangle drawTitleLine(GraphicsContext gc) {
        double titleW = 0;
        double titleH = 0;
        LabelRectangle lRect = null;

        // Control icon
        titleW += drawControl( gc );

        // Label
        if (label != null) {
            lRect = new LabelRectangle( parent, label, icon, false, false, false );
            lRect.draw( gc, x + titleW, y );
            titleW += lRect.getWidth();
            titleH = lRect.getHeight();
        }

        return new Rectangle( x, y, titleW, titleH );
    }

    /**
     * Draw Underline
     * 
     * @param width if 0, use width of rectangle
     */
    protected void drawUnderline(GraphicsContext gc, Rectangle rec, double width, double margin) {
        // rec.drawOutline( gc, false );
        double y = rec.getMaxY() - margin;
        double x = rec.getX() + margin / 2;
        double connectorWidth = 16;
        double maxX = x + width - margin - 2 * connectorWidth;
        if (width <= 0)
            maxX = rec.getMaxX();
        if (gc != null)
            gc.strokeLine( x, y, maxX, y );
    }

    // TODO - merge this with FacetRectangle
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

    public boolean isCollapsed() {
        return collapsed;
    }

    @Override
    public String toString() {
        return "Collapsable Facet: " + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
