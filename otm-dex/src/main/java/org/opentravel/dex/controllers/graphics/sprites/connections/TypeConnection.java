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
import org.opentravel.dex.controllers.graphics.sprites.GraphicsUtils;
import org.opentravel.dex.controllers.graphics.sprites.retangles.PropertyRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public class TypeConnection extends Connection {
    private Rectangle fromRect;
    private double offsetY; // offset from sprite Y
    private double offsetX; // offset from sprite X

    public TypeConnection(Rectangle propertyRect, DexSprite<?> userSprite, DexSprite<?> providerSprite) {
        if (userSprite == null || providerSprite == null || userSprite.getBoundaries() == null
            || providerSprite.getBoundaries() == null)
            throw new IllegalArgumentException( "Missing rectangle on connection constructor." );

        // Connect to the rectangle's right side
        fx = propertyRect.getMaxX() - PropertyRectangle.PROPERTY_MARGIN;
        offsetX = fx - userSprite.getBoundaries().getX();
        // Connect to connector arrow point
        fy = propertyRect.getMaxY() - GraphicsUtils.CONNECTOR_SIZE / 2;
        offsetY = fy - userSprite.getBoundaries().getY();
        // Connect to provider's left, center
        tx = providerSprite.getBoundaries().getX();
        ty = providerSprite.getBoundaries().getY() + providerSprite.getBoundaries().getHeight() / 2;

        this.from = userSprite;
        this.to = providerSprite;
        this.fromRect = propertyRect;
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
                if (fromRect != null) {
                    fx = from.getBoundaries().getX() + offsetX;
                    fy = from.getBoundaries().getY() + offsetY;
                } else {
                    fx = from.getBoundaries().getX() + offsetX;
                    fy = from.getBoundaries().getY() + sprite.getBoundaries().getHeight() / 2;
                }
            } else if (to == sprite) {
                tx = sprite.getBoundaries().getX();
                ty = sprite.getBoundaries().getY() + sprite.getBoundaries().getHeight() / 2;
            }

            draw( gc );
        }
        return false;
    }
}
