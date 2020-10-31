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

import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Margins;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Offsets;
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObjects;

import javafx.scene.canvas.GraphicsContext;

/**
 * Graphics Display Object (Sprite) for containing OTM Emumeration object.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class SimpleSprite extends MemberSprite<OtmSimpleObjects<?>> implements DexSprite {

    double dx;
    double margin;

    public SimpleSprite(OtmSimpleObjects<?> member, SpriteManager manager) {
        super( member, manager );
        dx = settingsManager.getOffset( Offsets.ID );
        margin = settingsManager.getMargin( Margins.FACET );
    }

    @Override
    public Rectangle drawContents(GraphicsContext gc, final double x, final double y) {
        // boolean compute = gc == null;
        Rectangle rect = null;

        double width = getBoundaries().getWidth();
        double fy = y + margin;

        // Show base type
        // if (getMember() instanceof OtmEnumerationOpen) {
        // rect = GraphicsUtils.drawLabel( "Other", null, false, gc, font, x + dx, fy );
        // fy += rect.getHeight() + margin;
        // }
        //
        return new Rectangle( x, y, width + margin, fy - y );
    }

}
