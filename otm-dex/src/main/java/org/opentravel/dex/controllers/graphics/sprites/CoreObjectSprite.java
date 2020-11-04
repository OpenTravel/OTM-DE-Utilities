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
import org.opentravel.dex.controllers.graphics.sprites.retangles.CorePropertyRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.FacetRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;
import org.opentravel.model.otmLibraryMembers.OtmCore;

import javafx.scene.canvas.GraphicsContext;

/**
 * Graphics Display Object (Sprite) for containing OTM choice object.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class CoreObjectSprite extends MemberSprite<OtmCore> implements DexSprite {
    // private static Log log = LogFactory.getLog( BusinessObjectSprite.class );

    double dxID;
    double dxSummary;
    double dxDetail;
    double margin;


    public CoreObjectSprite(OtmCore member, SpriteManager manager) {
        super( member, manager );

        dxID = settingsManager.getOffset( Offsets.ID );
        dxSummary = settingsManager.getOffset( Offsets.SUMMARY );
        dxDetail = settingsManager.getOffset( Offsets.DETAIL );
        margin = settingsManager.getMargin( Margins.FACET );
    }


    @Override
    public Rectangle drawContents(GraphicsContext gc, final double x, final double y) {
        Rectangle rect = null;
        double width = 0;
        double fy = y;

        // Show facets
        if (!isCollapsed()) {
            width = getBoundaries().getWidth();

            // Show simple type
            rect = new CorePropertyRectangle( getMember(), this, width );
            width = draw( rect, gc, width, x, dxSummary, y );
            fy += rect.getHeight() + margin;

            // Draw facets
            rect = new FacetRectangle( getMember().getSummary(), this, width - dxSummary );
            width = draw( rect, gc, width, x, dxSummary, fy );
            fy += rect.getHeight() + margin;

            if (!getMember().getDetail().getChildren().isEmpty()) {
                rect = new FacetRectangle( getMember().getDetail(), this, width - dxDetail );
                width = draw( rect, gc, width, x, dxDetail, fy );
                fy += rect.getHeight() + margin;
            }
        }
        // Return an enclosing rectangle
        return new Rectangle( x, y, width + margin, fy - y );
    }

}
