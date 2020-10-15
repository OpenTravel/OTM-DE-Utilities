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

import org.opentravel.dex.controllers.graphics.GraphicsCanvasController;
import org.opentravel.dex.controllers.graphics.sprites.DexSprite;
import org.opentravel.dex.controllers.graphics.sprites.GraphicsUtils;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmComplexObjects;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObjects;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.model.otmProperties.OtmProperty;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 * Graphics utility for containing property regions.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class PropertyRectangle extends Rectangle {
    // private static Log log = LogFactory.getLog( PropertyRectangle.class );

    public static final double PROPERTY_MARGIN = 2;
    public static final double PROPERTY_OFFSET = 10; // left margin

    private static final double PROPERTY_TYPE_MARGIN = 8; // distance between property name and type
    protected static final double CONNECTOR_SIZE = 16;

    /**
     * Render methods that create rectangles may set the event to run if the implement this interface.
     * <p>
     * Example: r.setOnMouseClicked( e -> manager.remove( this ) );
     */
    public abstract interface RectangleEventHandler {
        public void onRectangleClick(MouseEvent e);
    }

    private DexSprite<OtmLibraryMember> parent;
    private Font font;
    private Point2D connectionPoint;

    private OtmProperty property;

    public OtmProperty getProperty() {
        return property;
    }

    private String label;
    private Image icon;

    private OtmTypeProvider typeProvider = null;
    private String providerLabel;
    private Image providerIcon;
    private Paint providerColor;



    public PropertyRectangle(OtmProperty property, DexSprite<OtmLibraryMember> parentSprite, Font font, double width) {
        super( 0, 0, GraphicsUtils.MINIMUM_WIDTH, 0 );

        this.parent = parentSprite;
        this.font = font;
        this.width = width;

        // Get property information
        this.property = property;
        label = property.getName();
        icon = property.getIcon();

        // Get type information
        if (property instanceof OtmTypeUser) {
            typeProvider = ((OtmTypeUser) property).getAssignedType();
            if (typeProvider == property.getOwningMember())
                typeProvider = null;
            if (typeProvider != null) {
                providerLabel = typeProvider.getNameWithPrefix();
                if (typeProvider instanceof OtmComplexObjects)
                    providerLabel = getCardinality( property ) + typeProvider.getPrefix();
                providerIcon = typeProvider.getIcon();
                providerColor = property.isAssignedTypeInNamespace() ? null : GraphicsUtils.CONNECTOR_COLOR;
            }
        }

        // Compute the size
        draw( null, font );
    }

    public PropertyRectangle(OtmCore core, DexSprite<OtmLibraryMember> parentSprite, double width) {
        super( 0, 0, GraphicsUtils.MINIMUM_WIDTH, 0 );

        this.parent = parentSprite;
        if (parent != null)
            this.font = parent.getFont();
        else
            this.font = GraphicsCanvasController.DEFAULT_FONT;
        this.width = width;

        // Get property information
        label = "Simple";
        icon = core.getIcon();
        icon = null;

        // Get type information
        typeProvider = core.getAssignedType();
        if (typeProvider != null) {
            providerLabel = typeProvider.getNameWithPrefix();
            providerIcon = typeProvider.getIcon();
            providerColor = null;
        }

        // Compute the size
        draw( null, font );
    }

    public PropertyRectangle(OtmValueWithAttributes vwa, DexSprite<OtmLibraryMember> parentSprite, double width) {
        super( 0, 0, GraphicsUtils.MINIMUM_WIDTH, 0 );

        this.parent = parentSprite;
        if (parent != null)
            this.font = parent.getFont();
        else
            this.font = GraphicsCanvasController.DEFAULT_FONT;
        this.width = width;

        // Get property information
        label = "Value";
        icon = null;

        // Get type information
        typeProvider = vwa.getAssignedType();
        if (typeProvider != null) {
            providerLabel = typeProvider.getNameWithPrefix();
            providerIcon = typeProvider.getIcon();
            providerColor = null;
        }

        // Compute the size
        draw( null, font );
    }

    /**
     * Draw the facet.
     * 
     * @param gc
     * @param filled
     */
    @Override
    public Rectangle draw(GraphicsContext gc, boolean filled) {
        if (gc != null) {
            draw( gc, font );
        }
        return this;
    }

    public Point2D getConnectionPoint() {
        return connectionPoint;
    }

    private Rectangle draw(GraphicsContext gc, Font font) {
        boolean compute = gc == null;

        double connectorSize = GraphicsUtils.drawConnector( null, null, 0, 0 ).getX();
        double rightMargin = connectorSize + PROPERTY_MARGIN;
        double actualWidth = rightMargin + PROPERTY_MARGIN; // actual width as computed

        //
        // Draw Property Name and icon
        Rectangle lRect = GraphicsUtils.drawLabel( label, icon, gc, font, x, y );
        actualWidth += lRect.getWidth(); // Actual width

        // Draw Type provider if any
        if (typeProvider != null) {
            Rectangle tRect;
            tRect = GraphicsUtils.drawLabel( providerLabel, providerIcon, null, font, x, y );
            actualWidth += tRect.getWidth() + PROPERTY_TYPE_MARGIN;
            double tx = x + width - tRect.getWidth() - rightMargin - PROPERTY_MARGIN;
            GraphicsUtils.drawLabel( providerLabel, providerIcon, true, gc, font, tx, y );
        }

        // Compute property Height
        width = compute && actualWidth > width ? actualWidth : width;
        height = lRect.getHeight() + 2 * PROPERTY_MARGIN;

        // Draw Underline
        double lineY = y + lRect.getHeight() - PROPERTY_MARGIN;
        if (gc != null)
            gc.strokeLine( x, lineY, x + width - rightMargin, lineY );

        // Draw Connector symbol and register listener
        if (typeProvider != null && !(typeProvider.getOwningMember() instanceof OtmSimpleObjects)) {
            connectionPoint = new Point2D( x + width - PROPERTY_MARGIN, lineY );
            GraphicsUtils.drawConnector( gc, providerColor, x + width - 2 * connectorSize + 2 * PROPERTY_MARGIN,
                lineY - connectorSize / 2 );

            // Register mouse listener with parent
            if (!compute && property != null && parent != null) {
                this.setOnMouseClicked( e -> parent.connect( ((OtmTypeUser) property), parent, e.getX(), e.getY() ) );
                parent.add( this );
            }

        }
        // super.draw( gc, false ); // debug
        return this;
    }

    private static String getCardinality(OtmProperty property) {
        String cardinality = "";
        if (property instanceof OtmElement) {
            if (property.isManditory())
                cardinality = "[1";
            else
                cardinality = "[0";
            if (((OtmElement<?>) property).getRepeatCount() > 0)
                cardinality += Integer.toString( ((OtmElement<?>) property).getRepeatCount() );

            cardinality += "] ";
        } else if (property instanceof OtmTypeUser && property.isManditory())
            cardinality = "[1] ";
        return cardinality;

    }

    @Override
    public String toString() {
        return "Property: " + label + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
