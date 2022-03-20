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
import org.opentravel.dex.controllers.graphics.sprites.rectangles.FacetRectangle;
import org.opentravel.dex.controllers.graphics.sprites.rectangles.Rectangle;
import org.opentravel.dex.controllers.graphics.sprites.rectangles.VWAPropertyRectangle;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;

import javafx.scene.canvas.GraphicsContext;

/**
 * Graphics Display Object (Sprite) for containing OTM VWA object.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class VWASprite extends MemberSprite<OtmValueWithAttributes> implements DexSprite {
    // gets log from base class
    // private static Logger log = LogManager.getLogger( BusinessObjectSprite.class );

    double dxSummary;
    double margin;

    public VWASprite(OtmValueWithAttributes member, SpriteManager manager) {
        super( member, manager );

        dxSummary = settingsManager.getOffset( Offsets.SUMMARY );
        margin = settingsManager.getMargin( Margins.FACET );
    }

    @Override
    public Rectangle drawContents(GraphicsContext gc, final double x, final double y) {
        Rectangle rect = null;

        double width = 0;
        double fy = y;

        if (!isCollapsed()) {
            width = getBoundaries().getWidth();

            // Show value type
            rect = new VWAPropertyRectangle( getMember(), this, width );
            width = draw( rect, gc, width, x, dxSummary, y );
            fy += rect.getHeight() + margin;

            // Show facets
            rect = new FacetRectangle( getMember(), this, width - dxSummary );
            width = draw( rect, gc, width, x, dxSummary, fy );
            fy += rect.getHeight() + margin;
        }

        // Return the enclosing rectangle
        return new Rectangle( x, y, width + margin, fy - y );
    }

}
