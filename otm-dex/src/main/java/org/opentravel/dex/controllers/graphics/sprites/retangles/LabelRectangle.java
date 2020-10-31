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
import org.opentravel.dex.controllers.graphics.sprites.GraphicsUtils;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Margins;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 * Rectangle containing labels with image regions.
 * 
 * @author dmh
 *
 */
public class LabelRectangle extends Rectangle {
    private static Log log = LogFactory.getLog( LabelRectangle.class );

    // un-comment if going to use the rectangle for mouse click handling
    // /**
    // * Render methods that create rectangles may set the event to run if the implement this interface.
    // * <p>
    // * Example: r.setOnMouseClicked( e -> manager.remove( this ) );
    // */
    // public abstract interface RectangleEventHandler {
    // public void onRectangleClick(MouseEvent e);
    // }

    protected DexSprite parent;
    protected Font font;
    protected SettingsManager settings;

    protected String label = "";
    private Image image = null;

    private boolean bold = false;
    private boolean imageLast = false;
    private double margin = LABEL_MARGIN;

    private static final double LABEL_MARGIN = 4;
    private static final double MINIMUM_WIDTH = 10;


    public LabelRectangle(DexSprite parent, String label, Image image, boolean bold, boolean italic,
        boolean imageLast) {
        super( 0, 0, MINIMUM_WIDTH, 0 );
        this.parent = parent;
        this.label = label;
        this.image = image;
        this.imageLast = imageLast;
        this.bold = bold;
        this.font = parent.getFont();
        if (italic)
            this.font = parent.getItalicFont();

        settings = parent.getSettingsManager();
        if (settings != null) {
            margin = settings.getMargin( Margins.LABEL );
        }

        // Compute the size
        draw( null, font );
    }

    /**
     * Draw the rectangle.
     * 
     * @param gc
     */
    @Override
    public LabelRectangle draw(GraphicsContext gc) {
        if (gc != null)
            draw( gc, font );
        return this;
    }

    public LabelRectangle draw(GraphicsContext gc, double x, double y) {
        set( x, y );
        return draw( gc );
    }


    // label, icon, editable
    // TypeProvider, providerLabel, providerIcon
    protected Rectangle draw(GraphicsContext gc, Font font) {
        // boolean compute = gc == null;

        // Compute size, start with start and end margins
        width = 2 * margin;
        height = 2 * margin;
        double imageWidth = 0;
        double imageHeight = 0;
        Paint saveFill = null;
        Font saveFont = null;

        // Add size of image
        if (image != null) {
            width += image.getWidth() + margin;
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();
        }
        // Add size of text area
        Point2D textSize = GraphicsUtils.drawString( label, null, font, 0, 0 );
        height += textSize.getY() > imageHeight ? textSize.getY() : imageHeight;
        width += textSize.getX() + margin;

        if (gc != null) {
            saveFill = gc.getFill();
            saveFont = gc.getFont();
            gc.setFill( Color.BLACK );
            gc.setFont( font );
            if (imageLast) {
                renderText( gc, margin, textSize.getY() );
                if (image != null)
                    gc.drawImage( image, x + textSize.getX() + 2 + margin, y + 2 * margin );
            } else {
                if (image != null)
                    gc.drawImage( image, x + margin, y + margin );
                renderText( gc, 2 * margin + imageWidth, textSize.getY() );
            }
            gc.setFill( saveFill );
            gc.setFont( saveFont );
        }

        // // Register mouse listener with parent
        // if (gc != null && parent != null ) {
        // // this.setOnMouseClicked( e -> parent.connect( ((OtmTypeUser) property) ) );
        // }

        // // super.draw( gc, false ); // debug
        // // Log.debug("Drew "+this);
        return this;
    }

    private void renderText(GraphicsContext gc, double offsetX, double offsetY) {
        if (bold)
            gc.strokeText( label, x + offsetX, y + offsetY );
        else
            gc.fillText( label, x + offsetX, y + offsetY );
    }

    @Override
    public String toString() {
        return "Label: " + label + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
