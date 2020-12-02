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
import org.opentravel.model.otmFacets.OtmChoiceFacet;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmLibraryMembers.OtmChoiceObject;

import javafx.scene.canvas.GraphicsContext;

/**
 * Graphics Display Object (Sprite) for containing OTM choice object.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class ChoiceObjectSprite extends MemberSprite<OtmChoiceObject> implements DexSprite {
    // private static Log log = LogFactory.getLog( BusinessObjectSprite.class );

    double dxID;
    double dxShared;
    double dxChoice;
    double margin;

    public ChoiceObjectSprite(OtmChoiceObject member, SpriteManager manager) {
        super( member, manager );

        dxID = settingsManager.getOffset( Offsets.ID );
        dxShared = settingsManager.getOffset( Offsets.SHARED );
        dxChoice = settingsManager.getOffset( Offsets.CHOICE );
        margin = settingsManager.getMargin( Margins.FACET );
    }


    @Override
    public Rectangle drawContents(GraphicsContext gc, final double x, final double y) {
        FacetRectangle rect = null;
        // double dxShared = FacetRectangle.SHARED_OFFSET;
        double fy = y;
        double width = 0;

        // Show facets
        if (!isCollapsed()) {
            width = getBoundaries().getWidth();

            rect = new FacetRectangle( getMember().getShared(), this, width - dxShared );
            fy += rect.getHeight() + margin;
            width = draw( rect, gc, width, x, dxShared, y );

            for (OtmContributedFacet f : member.getChildrenContributedFacets()) {
                if (f.getContributor() instanceof OtmChoiceFacet) {
                    rect = new FacetRectangle( f, this, width - dxChoice );
                    width = draw( rect, gc, width, x, dxChoice, fy );
                    fy += rect.getHeight() + margin;
                }
            }
        }

        return new Rectangle( x, y, width + margin, fy - y );
    }

}
