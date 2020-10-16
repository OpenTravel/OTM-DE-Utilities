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
import org.opentravel.dex.controllers.DexIncludedController;

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
    private static Log log = LogFactory.getLog( SettingsManager.class );

    public static final Font DEFAULT_FONT = new Font( "Monospaced", 15 );
    public static final Font DEFAULT_FONT_ITALIC = Font.font( "Monospaced", FontWeight.NORMAL, FontPosture.ITALIC, 15 );
    private static final String DEFAULT_FONT_NAME = "Monospaced";
    private int fontSize = 14;
    private Font currentFont;
    private Font currentItalicFont;

    private static final Paint DEFAULT_STROKE = Color.BLACK;

    static final Paint DEFAULT_FILL = Color.gray( 0.8 );

    private static final double COLUMN_START = 10;
    private static final double COLUMN_WIDTH = 150;
    private static final int FACET_OFFSET = 5;
    private static final double MINIMUM_SLOT_HEIGHT = 20;
    private Paint backgroundColor = Color.gray( 0.95 );

    private Pane spritePane;
    private DexIncludedController<?> controller;

    private GraphicsContext currentGC;

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
    }

    public Font getDefaultFont() {
        return DEFAULT_FONT;
    }

    public Font getCurrentFont() {
        return currentFont;
    }

    public Font getItalicFont() {
        return currentItalicFont;
    }


    public GraphicsContext getGc() {
        return currentGC;
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
     * Get the current font.
     * 
     * @return
     */
    public Font getFont() {
        return currentFont;
    }

    public void update(Color color) {
        currentGC.setFill( color );
    }

    /**
     * 
     * @param fontSize
     * @return true if the font has changed
     */
    public boolean updateFontSize(int fontSize) {
        if (fontSize != this.fontSize) {
            currentFont = new Font( DEFAULT_FONT_NAME, fontSize );
            currentItalicFont = Font.font( DEFAULT_FONT_NAME, FontWeight.NORMAL, FontPosture.ITALIC, fontSize );
            currentGC.setFont( currentFont );
            return true;
        }
        return false;
    }
}
