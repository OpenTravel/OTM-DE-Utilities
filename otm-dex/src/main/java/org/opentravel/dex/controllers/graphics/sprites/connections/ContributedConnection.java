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
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public class ContributedConnection extends Connection {
    // private Rectangle fromRect;
    // private double offsetY; // offset from sprite Y
    // private double offsetX; // offset from sprite X

    public ContributedConnection(DexSprite contributorSprite, DexSprite objectSprite) {
        if (contributorSprite == null || objectSprite == null || contributorSprite.getBoundaries() == null
            || objectSprite.getBoundaries() == null)
            throw new IllegalArgumentException( "Missing boundaries in connection constructor." );

        setFxy( contributorSprite.getBoundaries() );
        setTxy( objectSprite.getBoundaries() );

        this.from = contributorSprite;
        this.to = objectSprite;
    }

    private void setFxy(Rectangle csb) {
        // Connect to the contribitor's top center
        fx = csb.getX() + csb.getWidth() / 2;
        fy = csb.getY();
    }

    private void setTxy(Rectangle osb) {
        // Connect to object bottom center
        tx = osb.getX() + osb.getWidth() / 2;
        ty = osb.getMaxY();
    }

    /**
     * Update connections involving the sprite.
     * 
     * @param sprite
     * @param gc
     * @param backgroundColor
     * @return
     */
    public boolean update(DexSprite sprite, GraphicsContext gc, Paint backgroundColor) {
        if (contains( sprite )) {

            // Erase old line, saving gc settings
            erase( gc, backgroundColor );

            // Move the point then draw
            if (from == sprite) {
                setFxy( from.getBoundaries() );
            } else if (to == sprite) {
                setTxy( to.getBoundaries() );
            }

            draw( gc );
            return true;
        }
        return false;
    }
}
