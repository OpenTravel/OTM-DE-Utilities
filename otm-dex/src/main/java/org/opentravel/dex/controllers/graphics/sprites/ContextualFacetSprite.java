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
import org.opentravel.dex.controllers.graphics.sprites.rectangles.FacetRectangle;
import org.opentravel.dex.controllers.graphics.sprites.rectangles.Rectangle;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;

import javafx.scene.canvas.GraphicsContext;

/**
 * Graphics Display Object (Sprite) for containing OTM contextual facets (choice, custom, query, update).
 * 
 * @author dmh
 * @param <O>
 *
 */
public class ContextualFacetSprite extends MemberSprite<OtmContextualFacet> implements DexSprite {
    // private static Logger log = LogManager.getLogger( BusinessObjectSprite.class );

    double margin;
    double dx = 0;

    public ContextualFacetSprite(OtmContextualFacet member, SpriteManager manager) {
        super( member, manager );

        margin = settingsManager.getMargin( Margins.FACET );
        dx = margin;
    }

    @Override
    public Rectangle drawContents(GraphicsContext gc, final double x, final double y) {
        double fy = y;
        double width = 0;
        Rectangle rect = null;

        if (!isCollapsed() && !getMember().getChildren().isEmpty() && member.getWhereContributed() != null) {
            width = getBoundaries().getWidth();
            rect = new FacetRectangle( (OtmContextualFacet) member, this, width );
            width = draw( rect, gc, width, x, dx, fy );
            fy += rect.getHeight() + margin;
        }

        return new Rectangle( x, y, width + margin, fy - y );
    }

}
