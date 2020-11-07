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

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public abstract class Connection {
    protected double fx;
    protected double fy;
    protected double tx;
    protected double ty;

    protected DexSprite from;
    protected DexSprite to;

    public boolean contains(DexSprite sprite) {
        return from == sprite || to == sprite;
    }

    /**
     * Update connections involving the sprite.
     * 
     * @param sprite
     * @param gc
     * @param backgroundColor
     * @return
     */
    public abstract boolean update(DexSprite sprite, GraphicsContext gc, Paint backgroundColor);

    public void draw(GraphicsContext gc) {
        gc.strokeLine( fx, fy, tx, ty );
    }

    public void erase(GraphicsContext gc, Paint backgroundColor) {
        // Erase old line, saving gc settings
        Paint save = gc.getStroke();
        double saveW = gc.getLineWidth();
        gc.setStroke( backgroundColor );
        gc.setLineWidth( 3 );

        draw( gc );

        gc.setStroke( save );
        gc.setLineWidth( saveW );
    }

    @Override
    public String toString() {
        return "Connection: " + fx + "-" + fy + " to " + tx + "-" + ty;
    }
}
