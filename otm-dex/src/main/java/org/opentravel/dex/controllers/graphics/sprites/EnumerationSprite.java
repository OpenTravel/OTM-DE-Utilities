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
import org.opentravel.dex.controllers.graphics.sprites.retangles.FacetRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.LabelRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;
import org.opentravel.model.otmLibraryMembers.OtmEnumeration;
import org.opentravel.model.otmLibraryMembers.OtmEnumerationOpen;

import javafx.scene.canvas.GraphicsContext;

/**
 * Graphics Display Object (Sprite) for containing OTM Emumeration object.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class EnumerationSprite extends MemberSprite<OtmEnumeration<?>> implements DexSprite {

    private double margin;
    private double dx;


    public EnumerationSprite(OtmEnumeration<?> member, SpriteManager manager) {
        super( member, manager );

        margin = settingsManager.getMargin( Margins.FACET );
        dx = settingsManager.getOffset( Offsets.ID );
    }


    @Override
    public Rectangle drawContents(GraphicsContext gc, final double x, final double y) {
        Rectangle rect = null;
        double width = 0;
        double fy = y;

        if (!isCollapsed()) {
            width = getBoundaries().getWidth();

            // Show base type

            // Show open's Other property
            if (getMember() instanceof OtmEnumerationOpen) {
                rect = new LabelRectangle( this, "Other", null, false, false, false ).draw( gc, x + dx, y );
                fy += rect.getHeight();
            }

            // Show values
            rect = new FacetRectangle( getMember(), this, width - dx );
            width = draw( rect, gc, width, x, dx, fy );
            fy += rect.getHeight() + margin;
        }

        // Return the enclosing rectangle
        return new Rectangle( x, y, width + margin, fy - y );
    }

}
