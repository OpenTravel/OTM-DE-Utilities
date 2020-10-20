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

package org.opentravel.dex.controllers.graphics.sprites.connections;

import org.opentravel.dex.controllers.graphics.sprites.DexSprite;
import org.opentravel.dex.controllers.graphics.sprites.ResourceSprite;
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public class ResourceTypeConnection extends Connection {

    public ResourceTypeConnection(ResourceSprite resourceSprite, DexSprite<?> subjectSprite) {
        if (resourceSprite == null || subjectSprite == null || resourceSprite.getBoundaries() == null
            || subjectSprite.getBoundaries() == null)
            throw new IllegalArgumentException( "Missing boundaries in connection constructor." );

        // use property rectangle connection point
        Point2D cp = resourceSprite.getSubjectCP();
        fx = cp.getX();
        fy = cp.getY();

        // setFxy( resourceSprite.getBoundaries() );
        setTxy( subjectSprite.getBoundaries() );

        this.from = resourceSprite;
        this.to = subjectSprite;
    }

    // private void setFxy(Rectangle btb) {
    // // Connect to the base type's bottom center
    // fx = btb.getX() + btb.getWidth() / 2;
    // fy = btb.getMaxY();
    // }

    private void setTxy(Rectangle stb) {
        // Connect to subType's top center
        tx = stb.getX() + stb.getWidth() / 2;
        ty = stb.getY();
    }

    /**
     * Update connections involving the sprite.
     * 
     * @param sprite
     * @param gc
     * @param backgroundColor
     * @return
     */
    public boolean update(DexSprite<?> sprite, GraphicsContext gc, Paint backgroundColor) {
        if (contains( sprite )) {

            // Erase old line, saving gc settings
            erase( gc, backgroundColor );

            // Move the point then draw
            if (from == sprite) {
                Point2D cp = ((ResourceSprite) from).getSubjectCP();
                fx = cp.getX();
                fy = cp.getY();
            } else if (to == sprite) {
                setTxy( to.getBoundaries() );
            }

            draw( gc );
        }
        return false;
    }
}
