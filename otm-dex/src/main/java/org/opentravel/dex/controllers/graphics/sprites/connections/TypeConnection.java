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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.graphics.sprites.DexSprite;
import org.opentravel.dex.controllers.graphics.sprites.GraphicsUtils;
import org.opentravel.dex.controllers.graphics.sprites.retangles.PropertyRectangle;
import org.opentravel.model.otmProperties.OtmProperty;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public class TypeConnection extends Connection {
    private static Log log = LogFactory.getLog( TypeConnection.class );

    private OtmProperty property;

    public TypeConnection(PropertyRectangle propertyRect, DexSprite<?> userSprite, DexSprite<?> providerSprite) {
        if (userSprite == null || providerSprite == null || propertyRect == null)
            throw new IllegalArgumentException( "Missing parameter on connection constructor." );
        if (providerSprite.getBoundaries() == null)
            throw new IllegalArgumentException( "Missing provider boundaries on connection constructor." );
        if (propertyRect.getProperty() == null)
            throw new IllegalArgumentException( "Missing property rectangle otm-property on connection constructor." );
        if (propertyRect.getConnectionPoint() == null)
            throw new IllegalArgumentException(
                "Missing property rectangle connection point on connection constructor." );

        // Connect to connector arrow point
        getFromXY( propertyRect );
        property = propertyRect.getProperty();

        // Connect to provider's left, center
        tx = providerSprite.getBoundaries().getX();
        ty = providerSprite.getBoundaries().getY() + providerSprite.getBoundaries().getHeight() / 2;

        this.from = userSprite;
        this.to = providerSprite;
    }

    private void getFromXY(PropertyRectangle propertyRect) {
        // log.debug( "Start getFromXY: " + fx + " " + fy );
        if (propertyRect != null) {
            if (propertyRect.getConnectionPoint() != null) {
                // use property rectangle connection point
                fx = propertyRect.getConnectionPoint().getX();
                fy = propertyRect.getConnectionPoint().getY();
            } else {
                // Connect to the rectangle's right side
                fx = propertyRect.getMaxX();
                fy = propertyRect.getMaxY() - GraphicsUtils.CONNECTOR_SIZE / 2;
            }
            // log.debug( " End getFromXY: " + fx + " " + fy );
        }
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

            // Move the point
            if (from == sprite) {
                if (from.isCollapsed()) {
                    fx = from.getBoundaries().getMaxX();
                    fy = from.getBoundaries().getY() + sprite.getBoundaries().getHeight() / 2;
                } else {
                    getFromXY( from.find( property ) );
                }
            } else if (to == sprite) {
                tx = sprite.getBoundaries().getX();
                ty = sprite.getBoundaries().getY() + sprite.getBoundaries().getHeight() / 2;
            }

            // Draw new line from f to t
            draw( gc );
        }
        return false;
    }
}
