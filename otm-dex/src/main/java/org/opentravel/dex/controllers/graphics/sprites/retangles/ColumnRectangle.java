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

import org.opentravel.dex.controllers.graphics.sprites.DexSprite;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;

/**
 * Graphics utility for containing regions (x, y, width, height). A rectangle does <b>not</b> have a canvas.
 * <p>
 * Sub-types have contents that can be drawn into the rectangle. These rectangles will compute their size when
 * constructed and when drawn with a null GraphicsContext (GC). A rectangle may be mouse click-able if the parent sprite
 * is passed when constructing the rectangle.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class ColumnRectangle extends Rectangle {
    // private static Log log = LogFactory.getLog( Rectangle.class );

    public static final int COLUMN_HEIGHT_MIN = 1000;
    public static final int COLUMN_MARGIN_X = 50;
    public static final int COLUMN_MARGIN_Y = 10;
    private static final double COLUMN_WIDTH = 100;

    int index = 0;
    private List<DexSprite<?>> activeSprites = new ArrayList<>();
    Pane spritePane;
    private ColumnRectangle previousColumn = null;
    private ColumnRectangle nextColumn = null;

    public ColumnRectangle(Pane spritePane, ColumnRectangle previous) {
        super( 0, 0, COLUMN_WIDTH, COLUMN_HEIGHT_MIN );
        this.spritePane = spritePane;
        if (previous == null)
            index = 1;
        else
            index = previous.getIndex() + 1;

        this.x = (index - 1) * width + COLUMN_MARGIN_X;
        this.y = COLUMN_MARGIN_Y;

        previousColumn = previous;
    }

    // public Point2D getNextInColumn(double x, double y) {
    // Point2D bottom = new Point2D( x, y );
    // DexSprite<?> next = null;
    // do {
    // next = findSprite( bottom );
    // if (next != null)
    // bottom = new Point2D( x, next.getBoundaries().getMaxY() + 5 );
    // else {
    // bottom = new Point2D( x, bottom.getY() + MINIMUM_SLOT_HEIGHT );// minimum slot size;
    // next = findSprite( bottom );
    // }
    // } while (next != null && bottom.getY() < spritePane.getHeight());
    // return bottom;
    // }

    /**
     * Place the sprite in the next available slot in this column.
     * 
     * @param sprite
     * @return the sprite (added or found already column)
     */
    public DexSprite<OtmLibraryMember> add(DexSprite<OtmLibraryMember> sprite) {
        if (sprite != null && !activeSprites.contains( sprite )) {
            // If the sprite is wider than column, resize column
            if (sprite.getBoundaries().getWidth() + COLUMN_MARGIN_X > width)
                resize( sprite.getBoundaries().getWidth() + COLUMN_MARGIN_X );

            // Add the sprite to list and pane
            spritePane.getChildren().add( sprite.render( getNextInColumn() ) );
            activeSprites.add( sprite );
            sprite.set( this );

            sprite.refresh();
        }
        return sprite;
    }

    /**
     * Remove all sprites and their canvases. Remove all connections.
     */
    public void clear() {
        for (DexSprite<?> sprite : activeSprites) {
            sprite.clear();
            spritePane.getChildren().remove( sprite.getCanvas() );
        }
        activeSprites.clear();
        resize( COLUMN_WIDTH + COLUMN_MARGIN_X );
        y = COLUMN_MARGIN_Y;
    }

    public DexSprite<OtmLibraryMember> find(OtmLibraryMember member) {
        DexSprite<OtmLibraryMember> selectedSprite = null;
        for (DexSprite<?> sprite : activeSprites)
            if (sprite.getMember() == member) {
                return (DexSprite<OtmLibraryMember>) sprite;
            }
        return selectedSprite;
    }

    public DexSprite<?> findSprite(Point2D point) {
        DexSprite<?> selectedSprite = null;
        for (DexSprite<?> sprite : activeSprites)
            if (sprite.contains( point )) {
                return (sprite);
            }
        return selectedSprite;
    }

    public int getIndex() {
        return index;
    }

    public ColumnRectangle getNext() {
        return nextColumn != null ? nextColumn : this;
    }

    // public Point2D getNextInColumn(DexSprite<?> sprite) {
    // double dx = 2 * COLUMN_MARGIN_X; // get past the margin
    // double dy = 2 * COLUMN_MARGIN_Y;
    // Point2D bottom = new Point2D( dx, dy );
    // if (sprite != null) {
    // DexSprite<?> next = sprite;
    // do {
    // bottom = new Point2D( x + dx, next.getBoundaries().getMaxY() + dy );
    // next = findSprite( bottom );
    // } while (next != null && bottom.getY() < spritePane.getHeight());
    // }
    // return bottom;
    // }

    public Point2D getNextInColumn() {
        double cx = x + 20; // get past the margin
        double dy = COLUMN_MARGIN_Y;
        DexSprite<?> next = null;
        Point2D bottom = new Point2D( cx, 2 * COLUMN_MARGIN_Y );
        do {
            next = findSprite( bottom );
            if (next != null)
                bottom = new Point2D( cx, next.getBoundaries().getMaxY() + dy );
        } while (next != null && bottom.getY() < height);

        return new Point2D( x, bottom.getY() );
    }


    // public Point2D getNextInColumn(OtmLibraryMember member) {
    // return getNextInColumn( find( member ) );
    // }

    public ColumnRectangle getPrev() {
        return previousColumn != null ? previousColumn : this;
    }

    public List<DexSprite<?>> getSprites() {
        return activeSprites;
    }

    public void resize(double width) {
        this.width = width;
        if (nextColumn != null)
            nextColumn.set( getMaxX(), COLUMN_MARGIN_Y );
    }

    @Override
    public Rectangle set(double x, double y) {
        super.set( x, y );
        activeSprites.forEach( s -> {
            s.set( x, s.getBoundaries().getY() );
            s.refresh();
        } );
        if (nextColumn != null)
            nextColumn.set( getMaxX(), COLUMN_MARGIN_Y );
        return this;
    }

    public void setNext(ColumnRectangle next) {
        nextColumn = next;
    }

    public String toString() {
        return "index = " + getIndex() + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }

}
