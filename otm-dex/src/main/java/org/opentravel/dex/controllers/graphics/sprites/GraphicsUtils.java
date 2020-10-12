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
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmComplexObjects;
import org.opentravel.model.otmProperties.OtmProperty;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Graphics Display Object (Sprite) for containing OTM library members.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class GraphicsUtils {
    // protected static final double FACET_MARGIN = 5;
    // protected static final double FACET_OFFSET = 8;

    protected static final double MEMBER_MARGIN = 2;
    protected static final double CANVAS_MARGIN = 10;
    protected static final double LABEL_MARGIN = 4;
    private static final double LABEL_OFFSET = 8; // distance from line for text

    protected static final double CONNECTOR_SIZE = 16;
    protected static final double PROPERTY_MARGIN = 2;
    protected static final double PROPERTY_OFFSET = 10; // left margin
    private static final double PROPERTY_TYPE_MARGIN = 8; // distance between property name and type

    public static final double MINIMUM_WIDTH = 50;

    private static Log log = LogFactory.getLog( GraphicsUtils.class );

    public enum DrawType {
        NONE, OUTLINE, FILL
    }

    private GraphicsUtils() {
        // NO-OP static methods
    }



    public Rectangle drawProperty(OtmProperty p, GraphicsContext gc, Font font, final double x, final double y,
        double width) {

        // String label = getLabel( p );
        Rectangle lRect = drawLabel( p.getName(), p.getIcon(), null, font, x + PROPERTY_OFFSET, y );
        double height = lRect.getHeight() + 3 * LABEL_MARGIN;
        double propertyWidth = lRect.getWidth() + CONNECTOR_SIZE + PROPERTY_MARGIN;
        Rectangle typeExtent = null;
        // lRect.draw( gc, false );

        OtmTypeProvider tp = null;
        if (p instanceof OtmTypeUser)
            tp = ((OtmTypeUser) p).getAssignedType();
        if (tp != null) {
            typeExtent = drawLabel( tp.getNameWithPrefix(), tp.getIcon(), null, font, x, y );
            propertyWidth += typeExtent.getWidth() + PROPERTY_TYPE_MARGIN;
            // new Rectangle( x + width - typeExtent.getWidth() - CONNECTOR_SIZE - PROPERTY_MARGIN, y,
            // typeExtent.getWidth(), height ).draw( gc, false );
        }

        // Override width if computing size not drawing
        if (gc == null && propertyWidth > width)
            width = propertyWidth;

        // IF we are just computing size, return the sized rectangle.
        //
        Rectangle propertyRect = new Rectangle( x, y, width, height );
        if (gc == null)
            return propertyRect;

        // Draw property name and its icon.
        //
        double labelX = x + PROPERTY_OFFSET;
        lRect = drawLabel( p.getName(), p.getIcon(), gc, font, labelX, y );
        double lineY = y + lRect.getHeight() + 2 * PROPERTY_MARGIN;
        gc.strokeLine( labelX, lineY, x + lRect.getWidth(), lineY );

        // Show type and Link if it has one
        if (p instanceof OtmTypeUser && tp != p.getOwningMember()) {

            // Draw line under property name extending to end of the type name
            gc.strokeLine( labelX, lineY, x + width - CONNECTOR_SIZE, lineY );

            // Location for the property type to start, ends at connector
            if (tp != null) {
                // Draw the name of the type
                typeExtent = drawLabel( tp.getNameWithPrefix(), tp.getIcon(), false, null, font, labelX, y );
                double typeX = x + width - typeExtent.getWidth() - CONNECTOR_SIZE - PROPERTY_MARGIN;
                drawLabel( tp.getNameWithPrefix(), tp.getIcon(), true, gc, font, typeX, y );

                if (tp instanceof OtmComplexObjects) {
                    GraphicsUtils.drawConnector( gc, x + width - CONNECTOR_SIZE - PROPERTY_MARGIN,
                        lineY - CONNECTOR_SIZE / 2 );
                }
            }
        }
        return propertyRect;
    }

    /**
     * 
     * @param image
     * @param fill
     * @param gc
     * @param x
     * @param y
     * @return
     */
    public static Rectangle drawImage(Image image, DrawType fill, GraphicsContext gc, final double x, final double y) {
        gc.drawImage( image, x, y );
        Rectangle r = new Rectangle( x, y, image.getWidth(), image.getHeight() );
        if (fill == DrawType.OUTLINE)
            r.draw( gc, false );
        else if (fill == DrawType.FILL)
            r.draw( gc, true );
        return r;
    }


    /**
     * Get the size of a string rendered in the font. Return a point at the maxX and maxY value.
     * <p>
     * Assumes start point of 0,0. No offsets, margins or other adjustments.
     * 
     * @return
     */
    public static Point2D drawString(String string, GraphicsContext gc, Font font, final double x, final double y) {
        Text text = new Text( string );
        text.setFont( font );
        double tx = text.getBoundsInLocal().getWidth();
        double ty = text.getBoundsInLocal().getHeight();
        tx = Math.round( tx );
        ty = Math.round( ty );
        if (gc != null)
            gc.strokeText( string, x, y );
        // log.debug( "Text Size: " + tx + " " + ty + " " + string );
        return new Point2D( tx, ty );
    }

    /**
     * Draw a circle and triangle connector symbol
     */
    public static Point2D drawConnector(GraphicsContext gc, final double x, final double y) {
        if (gc != null) {
            gc.strokeOval( x, y, CONNECTOR_SIZE, CONNECTOR_SIZE );
            Image link = ImageManager.getImage( ImageManager.Icons.NAV_GO );
            gc.drawImage( link, x, y );
            return new Point2D( x + CONNECTOR_SIZE, y + CONNECTOR_SIZE );
        }
        return new Point2D( CONNECTOR_SIZE, CONNECTOR_SIZE );
    }

    /**
     * Draw the label with optional image on the graphics context at x, y.
     * 
     * @param label
     * @param image - can be null
     * @param gc - if null, just compute size
     * @param x
     * @param y
     * @return bounding rectangle
     */
    public static Rectangle drawLabel(String label, Image image, GraphicsContext gc, Font font, final double x,
        final double y) {
        return drawLabel( label, image, false, gc, font, x, y );
    }

    public static Rectangle drawLabel(String label, Image image, boolean imageAfterText, GraphicsContext gc, Font font,
        final double x, final double y) {

        // Compute size, start with start and end margins
        double width = 2 * LABEL_MARGIN;
        double height = 2 * LABEL_MARGIN;
        double imageWidth = 0;
        double imageHeight = 0;

        // Add size of image
        if (image != null) {
            width += image.getWidth() + LABEL_MARGIN;
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();
        }
        // Add size of text area
        Point2D textSize = drawString( label, null, font, 0, 0 );
        height += textSize.getY() > imageHeight ? textSize.getY() : imageHeight;
        width += textSize.getX();

        if (gc != null) {
            if (imageAfterText) {
                gc.strokeText( label, x + LABEL_MARGIN, y + textSize.getY() );
                if (image != null)
                    gc.drawImage( image, x + textSize.getX() + LABEL_MARGIN, y + 2 * LABEL_MARGIN );
            } else {
                if (image != null)
                    gc.drawImage( image, x + LABEL_MARGIN, y + LABEL_MARGIN );
                gc.strokeText( label, x + 2 * LABEL_MARGIN + imageWidth, y + textSize.getY() );
            }
        }
        Rectangle lRect = new Rectangle( x, y, width, height );
        // lRect.draw( gc, false );
        return lRect;
    }

    // public static String getLabel(OtmProperty property) {
    // String cardinality = "";
    // if (property instanceof OtmElement)
    // cardinality = Integer.toString( ((OtmElement<?>) property).getRepeatCount() );
    // else if (property instanceof OtmTypeUser && property.isManditory())
    // cardinality = "1";
    //
    // String label = property.getName() + " " + cardinality;
    // return label;
    // }



}
