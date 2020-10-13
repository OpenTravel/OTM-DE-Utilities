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

import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

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
     * @param user
     * @param from
     * @param x
     * @param y
     */
    void connect(OtmTypeUser user, DexSprite<?> from, double x, double y);

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

    public void findAndRunRectangle(MouseEvent e);

    /**
     * @return
     */
    public Rectangle getBoundaries();

    /**
     * @return the font from the active graphics context
     */
    Font getFont();

    public OtmLibraryMember getMember();

    /**
     * Clear then draw the sprite.
     */
    public void refresh();

    /**
     * Render the sprite.
     */
    public Canvas render();

    /**
     * Simply set the top, left coordinates.
     */
    public void set(double x, double y);

    public void set(Font font);

    public void setBackgroundColor(Color color);

    /**
     * @param collapsed
     */
    void setCollapsed(boolean collapsed);
}
