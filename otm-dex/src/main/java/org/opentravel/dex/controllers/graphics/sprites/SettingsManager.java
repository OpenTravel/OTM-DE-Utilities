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

import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.dex.controllers.graphics.sprites.rectangles.CollapsableRectangle;
import org.opentravel.dex.controllers.graphics.sprites.rectangles.FacetRectangle;
import org.opentravel.model.otmContainers.OtmLibrary;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * Manage all configuration settings for the graphics sprites.
 * 
 * @author dmh
 */
public class SettingsManager {
    // private static Log log = LogFactory.getLog( SettingsManager.class );

    public static final double FONT_BASE = 8;
    public static final double FONT_INCREMENT = 1;
    public static final Font DEFAULT_FONT = new Font( "Monospaced", 15 );
    public static final Font DEFAULT_FONT_ITALIC = Font.font( "Monospaced", FontWeight.NORMAL, FontPosture.ITALIC, 15 );
    private static final String DEFAULT_FONT_NAME = "Monospaced";

    public static final double CONNECTOR_SIZE = 18;
    public static final Paint CONNECTOR_COLOR = Color.gray( 0.3 );
    private static final Paint FACET_COLOR = Color.ANTIQUEWHITE;
    private Paint backgroundColor = Color.gray( 0.95 );

    private static final double COLUMN_START = 10;
    private static final double COLUMN_WIDTH = 150;
    // private static final int FACET_OFFSET = 5;
    private static final double MINIMUM_SLOT_HEIGHT = 20;

    private int currentSize = 3;
    private Font currentFont;
    private Font currentItalicFont;

    private static final Paint DEFAULT_STROKE = Color.BLACK;

    static final Paint DEFAULT_FILL = Color.gray( 0.8 );
    private Map<OtmLibrary,Paint> colorMap = new HashMap();

    private static final double FACET_OFFSET = 8;

    public enum Offsets {
        PROPERTY(10),
        FACET(FACET_OFFSET),
        ID(FACET_OFFSET),
        SHARED(FACET_OFFSET),
        QUERY(FACET_OFFSET),
        UPDATE(FACET_OFFSET),
        SUMMARY(FACET_OFFSET + FACET_OFFSET),
        CHOICE(FACET_OFFSET + FACET_OFFSET),
        DETAIL(FACET_OFFSET + FACET_OFFSET + FACET_OFFSET),
        CUSTOM(FACET_OFFSET + FACET_OFFSET + FACET_OFFSET);

        public final double offset;

        private Offsets(double offset) {
            this.offset = offset;
        }

        public double getOffset() {
            return this.offset;
        }
    }

    public double getOffset(Offsets offset) {
        return offset.getOffset() * scale;
    }

    private static final double FACET_MARGIN = 5;

    public enum Margins {
        CANVAS(10), FACET(FACET_MARGIN), LABEL(4), PROPERTY(1), PROPERTY_TYPE(8), TEXT(2), MEMBER(2);

        public final double margin;

        private Margins(double margin) {
            this.margin = margin;
        }

        public double getMargin() {
            return this.margin;
        }
    }

    public double getMargin(Margins margin) {
        return margin.getMargin() * scale;
    }



    private Pane spritePane;
    private DexIncludedController<?> controller;

    private GraphicsContext currentGC;
    private double scale;

    /**
     * Initialize the sprite. Create connections canvas and add to pane. Set mouse click handler.
     * 
     * @param spritePane
     * @param owner
     * @param gc
     */
    public SettingsManager(Pane spritePane, DexIncludedController<?> owner, GraphicsContext gc) {
        this.spritePane = spritePane;
        this.controller = owner;
        this.currentGC = gc;

        currentFont = DEFAULT_FONT;
        currentItalicFont = DEFAULT_FONT_ITALIC;
        scale = 1;
    }

    public Font getDefaultFont() {
        return DEFAULT_FONT;
    }

    public Paint getColor(OtmLibrary lib) {
        return colorMap.get( lib ) != null ? colorMap.get( lib ) : DEFAULT_FILL;
    }

    public Paint setColor(OtmLibrary lib, Paint color) {
        return colorMap.put( lib, color );
    }

    public Font getItalicFont() {
        return currentItalicFont;
    }


    public GraphicsContext getGc() {
        return currentGC;
    }

    public double getConnectorSize() {
        return CONNECTOR_SIZE * scale;
    }

    public Paint getDefaultFill() {
        return DEFAULT_FILL;
    }

    public Paint getDefaultStroke() {
        return DEFAULT_STROKE;
    }

    public Pane getSpritePane() {
        return spritePane;
    }

    /**
     * Set the passed GC to font, fill, stroke and linewidth of current gc
     * 
     * @param gc
     */
    public void setGCParams(GraphicsContext gc) {
        if (gc != null && gc != currentGC) {
            gc.setFont( currentGC.getFont() );
            gc.setFill( currentGC.getFill() );
            gc.setStroke( currentGC.getStroke() );
            gc.setLineWidth( currentGC.getLineWidth() );
        }
    }

    /**
     * Use defaults
     */
    public void setGCParamsDefaults(GraphicsContext gc) {
        if (gc != null) {
            gc.setFont( new Font( "Arial", 18 ) );
            gc.setFill( Color.gray( 0.85 ) );
            gc.setStroke( Color.DARKSLATEBLUE );
            gc.setLineWidth( 1 );
        }
    }

    /**
     * Get the current font.
     * 
     * @return
     */
    public Font getFont() {
        return currentFont;
    }

    public int getSize() {
        return currentSize;
    }

    public void update(Color color) {
        currentGC.setFill( color );
    }

    /**
     * 
     * @param size a value from 1 to 10
     * @return true if the size has changed
     */
    public boolean updateSize(int size) {
        if (size != this.currentSize) {
            // log.debug( "Setting size to " + size );
            currentFont = new Font( DEFAULT_FONT_NAME, FONT_BASE + size * FONT_INCREMENT );
            currentItalicFont = Font.font( DEFAULT_FONT_NAME, FontWeight.NORMAL, FontPosture.ITALIC,
                FONT_BASE + size * FONT_INCREMENT );
            currentGC.setFont( currentFont );
            //
            scale = .2 + size * 0.125;
            return true;
        }
        return false;
    }

    /**
     * @param a facet rectangle
     */
    public Paint getColor(FacetRectangle rect) {
        return (Color.ANTIQUEWHITE);
    }

    /**
     * @param a facet rectangle
     */
    public Paint getColor(CollapsableRectangle rect) {
        return (Color.ANTIQUEWHITE);
    }

    /**
     * @param member sprite
     */
    public Paint getColor(MemberSprite<?> sprite) {
        OtmLibrary lib = sprite.getMember().getLibrary();
        return lib != null ? getColor( lib ) : currentGC.getFill();
    }
}
