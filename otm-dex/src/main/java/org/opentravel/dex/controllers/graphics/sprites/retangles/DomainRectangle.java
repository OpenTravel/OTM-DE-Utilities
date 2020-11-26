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
import org.opentravel.dex.controllers.graphics.sprites.SpriteManager;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

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
@Deprecated
public class DomainRectangle extends Rectangle {
    private static Log log = LogFactory.getLog( ColumnRectangle.class );

    public static final int DOMAIN_HEIGHT_MIN = 50;
    private static final double DOMAIN_WIDTH_MIN = 50;

    // private int index = 0;
    private List<DexSprite> activeSprites = new ArrayList<>();
    // private Pane spritePane;
    private ColumnRectangle column = null;

    public DomainRectangle(ColumnRectangle column) {
        super( 0, 0, DOMAIN_WIDTH_MIN, DOMAIN_HEIGHT_MIN );
        // this.spritePane = spritePane;
        this.column = column;

        this.x = column.getX();
        this.y = column.getY();
        log.debug( this );
    }

    public DomainRectangle(SpriteManager manager) {
        super( 0, 0, DOMAIN_WIDTH_MIN, DOMAIN_HEIGHT_MIN );
        // this.spritePane = spritePane;
        // this.column = column;
        //
        // this.x = column.getX();
        // this.y = column.getY();
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
            // add to list
            activeSprites.add( sprite );
        }
    }

    /**
     * Remove all sprites and their canvases. Remove all connections.
     */
    public void clear() {
        activeSprites.clear();
    }

    @Override
    public Rectangle draw(GraphicsContext gc) {
        Paint savedPaint = gc.getFill();
        gc.setFill( javafx.scene.paint.Color.BLACK );
        super.draw( gc );
        gc.setFill( savedPaint );
        return this;
    }

    public List<DexSprite> getSprites() {
        return activeSprites;
    }

    public void remove(DexSprite sprite) {
        // log.debug( "Removing sprite: " + sprite + " " + activeSprites.contains( sprite ) );
        activeSprites.remove( sprite );
    }

    public void resize(double width) {
        this.width = width;
    }

    @Override
    public Rectangle set(double x, double y) {
        super.set( x, y );
        activeSprites.forEach( s -> {
            s.set( x, s.getY() );
            s.refresh();
        } );
        // log.debug( "Set column " + this );
        return this;
    }

    @Override
    public String toString() {
        return "Domain:  x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }

}
