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
import org.opentravel.dex.controllers.graphics.sprites.DomainSprite;
import org.opentravel.dex.controllers.graphics.sprites.MemberSprite;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;

/**
 * Rectangle for columns of sprites. When resized, columns will adjust their width then the "next" column.
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
    // private static Log log = LogFactory.getLog( ColumnRectangle.class );

    public static final int COLUMN_HEIGHT_MIN = 1000;
    public static final int COLUMN_MARGIN_X = 50;
    public static final int COLUMN_MARGIN_Y = 10;
    private static final double COLUMN_WIDTH = 100;

    private int index = 0;
    private List<DexSprite> activeSprites = new ArrayList<>();
    private Pane spritePane;
    private ColumnRectangle previousColumn = null;
    private ColumnRectangle nextColumn = null;
    private DomainSprite domainSprite;

    public ColumnRectangle(Pane spritePane, DomainSprite domain, ColumnRectangle previous) {
        super( 0, 0, COLUMN_WIDTH, COLUMN_HEIGHT_MIN );
        this.spritePane = spritePane;
        if (previous == null)
            index = 1;
        else
            index = previous.getIndex() + 1;
        previousColumn = previous;

        this.domainSprite = domain;

        this.x = (index - 1) * width + COLUMN_MARGIN_X;
        this.y = COLUMN_MARGIN_Y;
        log.debug( this );
    }

    /**
     * Place and render the sprite in the next available slot in this column.
     * 
     * @param sprite
     * @return the sprite (added or found already column)
     */
    public void add(DexSprite sprite) {
        // log.debug( "Adding " + sprite );
        if (sprite != null && !activeSprites.contains( sprite )) {
            // If the sprite is wider than column, resize column
            if (sprite.getWidth() + COLUMN_MARGIN_X > width)
                resize( sprite.getWidth() + COLUMN_MARGIN_X );

            // add to list
            activeSprites.add( sprite );

            // Show what domain it is in
            // domainSprite.add( sprite );
        }
    }

    /**
     * Remove all sprites and their canvases. Remove all connections.
     */
    public void clear() {
        for (DexSprite sprite : activeSprites) {
            sprite.clear();
            spritePane.getChildren().remove( sprite.getCanvas() );
        }
        activeSprites.clear();
        resize( COLUMN_WIDTH );
        y = COLUMN_MARGIN_Y;
    }

    public DexSprite findSprite(Point2D point) {
        DexSprite selectedSprite = null;
        for (DexSprite sprite : activeSprites)
            if (sprite.contains( point )) {
                return (sprite);
            }
        return selectedSprite;
    }

    @SuppressWarnings("unchecked")
    public MemberSprite<OtmLibraryMember> get(OtmLibraryMember member) {
        MemberSprite<OtmLibraryMember> selectedSprite = null;
        for (DexSprite sprite : activeSprites)
            if (sprite instanceof MemberSprite && ((MemberSprite<?>) sprite).getMember() == member) {
                return (MemberSprite<OtmLibraryMember>) sprite;
            }
        return selectedSprite;
    }

    public int getIndex() {
        return index;
    }

    public ColumnRectangle getNext() {
        return nextColumn != null ? nextColumn : this;
    }

    public Point2D getNextInColumn() {
        double cx = x + 20; // get past the margin
        double dy = COLUMN_MARGIN_Y;
        DexSprite next = null;
        Point2D bottom = new Point2D( cx, 2 * COLUMN_MARGIN_Y );
        do {
            next = findSprite( bottom );
            if (next != null)
                bottom = new Point2D( cx, next.getBoundaries().getMaxY() + dy );
        } while (next != null && bottom.getY() < height);

        return new Point2D( x, bottom.getY() );
    }


    public ColumnRectangle getPrev() {
        return previousColumn != null ? previousColumn : this;
    }

    public List<DexSprite> getSprites() {
        return activeSprites;
    }

    public void remove(DexSprite sprite) {
        // log.debug( "Removing sprite: " + sprite + " " + activeSprites.contains( sprite ) );
        activeSprites.remove( sprite );
        spritePane.getChildren().remove( sprite.getCanvas() );
    }

    public void resize(double width) {
        this.width = width;
        if (nextColumn != null)
            nextColumn.set( getMaxX(), COLUMN_MARGIN_Y );
        // log.debug( "Resized column " + this );
    }

    @Override
    public Rectangle set(double x, double y) {
        super.set( x, y );
        activeSprites.forEach( s -> {
            s.set( x, s.getY() );
            s.refresh();
        } );
        if (nextColumn != null)
            nextColumn.set( getMaxX(), COLUMN_MARGIN_Y );
        // log.debug( "Set column " + this );
        return this;
    }

    public void setNext(ColumnRectangle next) {
        nextColumn = next;
    }

    @Override
    public String toString() {
        return "Column: index = " + getIndex() + " x = " + x + " y = " + y + " width = " + width + " height = "
            + height;
    }

}
