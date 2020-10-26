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
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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
    private static Log log = LogFactory.getLog( GraphicsUtils.class );

    private static final double LABEL_MARGIN = 4;

    public static final Paint CONNECTOR_COLOR = Color.gray( 0.3 );

    public static final double MINIMUM_WIDTH = 50;

    public enum DrawType {
        NONE, OUTLINE, FILL
    }

    private GraphicsUtils() {
        // NO-OP static methods
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
        if (gc != null)
            gc.drawImage( image, x, y );
        Rectangle r = new Rectangle( x, y, image.getWidth(), image.getHeight() );
        if (gc != null)
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
    public static Point2D drawConnector(GraphicsContext gc, Paint color, final double size, final double x,
        final double y) {
        if (gc != null) {
            Paint savedColor = gc.getFill();
            if (color != null) {
                gc.setFill( color );
                gc.fillOval( x, y, size, size );
                gc.setFill( savedColor );
                // log.debug( "Drew Connector with filled oval." );
            }
            gc.strokeOval( x, y, size, size );
            Image link = ImageManager.getImage( ImageManager.Icons.CONN_L );
            if (size + 3 < link.getWidth())
                link = ImageManager.getImage( ImageManager.Icons.CONN_M );
            if (size + 3 < link.getWidth())
                link = ImageManager.getImage( ImageManager.Icons.CONN_SM );

            double imageX = x + size - link.getWidth();
            double imageY = y + size / 2 - link.getHeight() / 2;
            gc.drawImage( link, imageX, imageY );

            return new Point2D( x + size, y + size / 2 );
        }
        return new Point2D( x + size, y + size / 2 );

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
    public static Rectangle drawLabel(String label, Image image, Font font, final double x, final double y) {
        return drawLabel( label, image, true, false, null, font, x, y );
    }

    // public static Rectangle drawLabel(String label, Image image, boolean imageAfterText, GraphicsContext gc, Font
    // font,
    // final double x, final double y) {
    // return drawLabel( label, image, true, imageAfterText, gc, font, x, y );
    // }

    public static Rectangle drawLabel(String label, Image image, boolean bold, boolean imageAfterText,
        GraphicsContext gc, Font font, final double x, final double y) {

        // Compute size, start with start and end margins
        double width = 2 * LABEL_MARGIN;
        double height = 2 * LABEL_MARGIN;
        double imageWidth = 0;
        double imageHeight = 0;
        Paint saveFill = null;
        Font saveFont = null;

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
            saveFill = gc.getFill();
            saveFont = gc.getFont();
            gc.setFill( Color.BLACK );
            gc.setFont( font );
            if (imageAfterText) {
                if (bold)
                    gc.strokeText( label, x + LABEL_MARGIN, y + textSize.getY() );
                else
                    gc.fillText( label, x + LABEL_MARGIN, y + textSize.getY() );
                if (image != null)
                    gc.drawImage( image, x + textSize.getX() + 2 + LABEL_MARGIN, y + 2 * LABEL_MARGIN );
            } else {
                if (image != null)
                    gc.drawImage( image, x + LABEL_MARGIN, y + LABEL_MARGIN );
                if (bold)
                    gc.strokeText( label, x + 2 * LABEL_MARGIN + imageWidth, y + textSize.getY() );
                else
                    gc.fillText( label, x + 2 * LABEL_MARGIN + imageWidth, y + textSize.getY() );
            }
            gc.setFill( saveFill );
            gc.setFont( saveFont );
        }
        Rectangle lRect = new Rectangle( x, y, width, height );
        // lRect.draw( gc, false );
        return lRect;
    }
}
