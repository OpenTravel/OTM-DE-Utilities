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
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmComplexObjects;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.model.otmProperties.OtmProperty;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;

/**
 * Graphics utility for containing property regions.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class PropertyRectangle extends Rectangle {
    private static Log log = LogFactory.getLog( PropertyRectangle.class );

    protected static final double PROPERTY_MARGIN = 2;
    protected static final double PROPERTY_OFFSET = 10; // left margin
    private static final double PROPERTY_TYPE_MARGIN = 16; // distance between property name and type
    protected static final double CONNECTOR_SIZE = 16;

    /**
     * Render methods that create rectangles may set the event to run if the implement this interface.
     * <p>
     * Example: r.setOnMouseClicked( e -> manager.remove( this ) );
     */
    public abstract interface RectangleEventHandler {
        public void onRectangleClick(MouseEvent e);
    }

    // private static final Paint FACET_COLOR = Color.ANTIQUEWHITE;

    private OtmProperty property;
    private DexSprite<OtmLibraryMember> parent;
    private Font font;
    private Point2D connectionPoint;

    @Deprecated
    public PropertyRectangle(double x, double y, double width, double height) {
        super( x, y, width, height );
    }

    public PropertyRectangle(OtmProperty property, DexSprite<OtmLibraryMember> parentSprite, Font font, double width) {
        super( 0, 0, GraphicsUtils.MINIMUM_WIDTH, 0 );
        this.width = width;
        this.property = property;
        this.parent = parentSprite;
        this.font = font;
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
        OtmTypeProvider typeProvider = null;
        if (property instanceof OtmTypeUser)
            typeProvider = ((OtmTypeUser) property).getAssignedType();

        double connectorSize = GraphicsUtils.drawConnector( null, 0, 0 ).getX();
        double rightMargin = connectorSize + PROPERTY_MARGIN;
        double actualWidth = rightMargin + PROPERTY_MARGIN; // actual width as computed

        //
        // Property Name and icon
        Rectangle lRect = GraphicsUtils.drawLabel( property.getName(), property.getIcon(), gc, font, x, y );
        actualWidth += lRect.getWidth(); // Actual width

        // Type provider if any
        if (typeProvider != null) {
            Rectangle tRect =
                GraphicsUtils.drawLabel( typeProvider.getNameWithPrefix(), typeProvider.getIcon(), null, font, x, y );
            actualWidth += tRect.getWidth() + PROPERTY_TYPE_MARGIN;
            width = compute && actualWidth > width ? actualWidth : width;

            double tx = x + width - tRect.getWidth() - rightMargin - PROPERTY_MARGIN;
            tRect = GraphicsUtils.drawLabel( typeProvider.getNameWithPrefix(), typeProvider.getIcon(), true, gc, font,
                tx, y );
            // tRect.draw( gc, false );
        }

        // Compute property Height and Width
        height = lRect.getHeight() + 2 * PROPERTY_MARGIN;

        // Underline
        double lineY = y + lRect.getHeight() - PROPERTY_MARGIN;
        if (gc != null)
            gc.strokeLine( x, lineY, x + width - rightMargin, lineY );

        // Connector symbol
        if (typeProvider instanceof OtmComplexObjects) {
            connectionPoint = new Point2D( x + width - PROPERTY_MARGIN, lineY );
            GraphicsUtils.drawConnector( gc, x + width - 2 * connectorSize + 2 * PROPERTY_MARGIN,
                lineY - connectorSize / 2 );
        }
        // super.draw( gc, false ); // debug
        return this;
    }

    private static String getLabel(OtmProperty property) {
        String cardinality = "";
        if (property instanceof OtmElement)
            cardinality = Integer.toString( ((OtmElement<?>) property).getRepeatCount() );
        else if (property instanceof OtmTypeUser && property.isManditory())
            cardinality = "1";

        String label = property.getName() + "  " + cardinality;
        return label;
    }

    // public static Rectangle drawProperty(OtmProperty p, GraphicsContext gc, Font font, final double x, final double
    // y,
    // double width) {
    //
    // // String label = getLabel( p );
    // Rectangle lRect = drawLabel( p.getName(), p.getIcon(), null, font, x + PROPERTY_OFFSET, y );
    // double height = lRect.getHeight() + 3 * LABEL_MARGIN;
    // double propertyWidth = lRect.getWidth() + CONNECTOR_SIZE + PROPERTY_MARGIN;
    // Rectangle typeExtent = null;
    // // lRect.draw( gc, false );
    //
    // OtmTypeProvider tp = null;
    // if (p instanceof OtmTypeUser)
    // tp = ((OtmTypeUser) p).getAssignedType();
    // if (tp != null) {
    // typeExtent = drawLabel( tp.getNameWithPrefix(), tp.getIcon(), null, font, x, y );
    // propertyWidth += typeExtent.getWidth() + PROPERTY_TYPE_MARGIN;
    // // new Rectangle( x + width - typeExtent.getWidth() - CONNECTOR_SIZE - PROPERTY_MARGIN, y,
    // // typeExtent.getWidth(), height ).draw( gc, false );
    // }
    //
    // // Override width if computing size not drawing
    // if (gc == null && propertyWidth > width)
    // width = propertyWidth;
    //
    // // IF we are just computing size, return the sized rectangle.
    // //
    // Rectangle propertyRect = new Rectangle( x, y, width, height );
    // if (gc == null)
    // return propertyRect;
    //
    // // Draw property name and its icon.
    // //
    // double labelX = x + PROPERTY_OFFSET;
    // lRect = drawLabel( p.getName(), p.getIcon(), gc, font, labelX, y );
    // double lineY = y + lRect.getHeight() + 2 * PROPERTY_MARGIN;
    // gc.strokeLine( labelX, lineY, x + lRect.getWidth(), lineY );
    //
    // // Show type and Link if it has one
    // if (p instanceof OtmTypeUser && tp != p.getOwningMember()) {
    //
    // // Draw line under property name extending to end of the type name
    // gc.strokeLine( labelX, lineY, x + width - CONNECTOR_SIZE, lineY );
    //
    // // Location for the property type to start, ends at connector
    // if (tp != null) {
    // // Draw the name of the type
    // typeExtent = drawLabel( tp.getNameWithPrefix(), tp.getIcon(), false, null, font, labelX, y );
    // double typeX = x + width - typeExtent.getWidth() - CONNECTOR_SIZE - PROPERTY_MARGIN;
    // drawLabel( tp.getNameWithPrefix(), tp.getIcon(), true, gc, font, typeX, y );
    //
    // if (tp instanceof OtmComplexObjects) {
    // GraphicsUtils.drawConnector( gc, x + width - CONNECTOR_SIZE - PROPERTY_MARGIN,
    // lineY - CONNECTOR_SIZE / 2 );
    // }
    // }
    // }
    // return propertyRect;
    // }


    @Override
    public String toString() {
        return "Property: " + property + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
