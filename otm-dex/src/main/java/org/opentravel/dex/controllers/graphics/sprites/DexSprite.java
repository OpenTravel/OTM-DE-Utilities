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

import org.opentravel.dex.controllers.graphics.sprites.retangles.ColumnRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.PropertyRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;

import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Interface for all Otm-DE FX graphics sprites.
 * <p>
 * No business logic (actions, validation, filters, etc.) should be in the Sprite.
 * 
 * @author dmh
 *
 */
public interface DexSprite<O extends OtmObject> {

    /**
     * @param rectangle
     */
    void add(Rectangle rectangle);

    /**
     * Clear the drawing canvas
     */
    public void clear();

    /**
     * Connect this member to its base type. Create sprite for base type if not already managed. If there was a sprite,
     * toggle its collapsed state.
     * 
     * @return
     */
    DexSprite<?> connect();

    /**
     * If needed, add sprite for the member that provides the type then add connection to new sprite. Otherwise, toggle
     * the collapsed state.
     * 
     * @param pSprite
     * @return
     */
    MemberSprite<OtmLibraryMember> connect(PropertyRectangle pSprite);

    /**
     * Is this point within the bounding box for the sprite?
     * 
     * @param point
     * @return
     */
    public boolean contains(Point2D point);

    /**
     * @param x
     * @param y
     * @return found rectangle or null
     */
    public Rectangle find(double x, double y);

    /**
     * Find the rectangle belonging to the property.
     * 
     * @return found rectangle or null
     */
    public PropertyRectangle find(OtmProperty property);

    public void findAndRunRectangle(MouseEvent e);

    /**
     * @return
     */
    public Rectangle getBoundaries();

    /**
     * @return the drawing canvas
     */
    public Canvas getCanvas();

    /**
     * @return the column this sprite is in.
     */
    public ColumnRectangle getColumn();

    /**
     * @return the font from settings
     */
    public Font getFont();

    public Font getItalicFont();

    public OtmLibraryMember getMember();

    /**
     * @return
     */
    public List<Rectangle> getRectangles();

    /**
     * @return
     */
    SettingsManager getSettingsManager();

    /**
     * @return collapsed field
     */
    boolean isCollapsed();

    /**
     * Clear then draw the sprite.
     */
    public void refresh();

    /**
     * Render the sprite.
     */
    public Canvas render();

    /**
     * @param point
     * @return
     */
    Canvas render(Point2D point);

    /**
     * Simply set the column field.
     * 
     * @param column
     */
    void set(ColumnRectangle column);

    /**
     * Simply set the top, left coordinates and update the rectangle's connection points.
     */
    public void set(double x, double y);

    public void set(Font font);

    /**
     * @param p
     */
    void set(Point2D p);

    public void setBackgroundColor(Color color);

    /**
     * Set the collapsed flag and resize.
     */
    void setCollapsed(boolean collapsed);


}
