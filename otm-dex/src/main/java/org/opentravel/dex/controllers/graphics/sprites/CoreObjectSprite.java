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

import org.opentravel.dex.controllers.graphics.sprites.retangles.FacetRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.PropertyRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;

/**
 * Graphics Display Object (Sprite) for containing OTM choice object.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class CoreObjectSprite extends MemberSprite<OtmCore> implements DexSprite<OtmLibraryMember> {
    // private static Log log = LogFactory.getLog( BusinessObjectSprite.class );

    public CoreObjectSprite(OtmCore member, SpriteManager manager, SettingsManager settingsManager) {
        super( member, manager, settingsManager );
    }

    @Override
    public Rectangle drawContents(final double x, final double y) {
        return drawContents( settingsManager.getGc(), settingsManager.getFont(), x, y );
    }


    @Override
    public Rectangle drawContents(GraphicsContext gc, Font font, final double x, final double y) {
        boolean compute = gc == null;
        Rectangle rect = null;

        double dxSummary = FacetRectangle.SUMMARY_OFFSET;
        double dxDetail = FacetRectangle.DETAIL_OFFSET;
        double width = getBoundaries().getWidth();
        double fy = y + FacetRectangle.FACET_MARGIN;

        // Show simple type
        rect = new PropertyRectangle( getMember(), this, width );
        rect.set( x + dxSummary, fy );
        rect.draw( gc, true );
        fy += rect.getHeight() + FacetRectangle.FACET_MARGIN;
        width = compute && rect.getWidth() > width ? rect.getWidth() + dxSummary : width;

        // Show facets
        if (!isCollapsed()) {

            rect = new FacetRectangle( getMember().getSummary(), this, width - dxSummary );
            rect.set( x + dxSummary, fy );
            rect.draw( gc, true );
            fy += rect.getHeight() + FacetRectangle.FACET_MARGIN;
            width = compute && rect.getWidth() > width ? rect.getWidth() + dxSummary : width;

            if (!getMember().getDetail().getChildren().isEmpty()) {
                rect = new FacetRectangle( getMember().getDetail(), this, width - dxDetail );
                rect.set( x + dxDetail, fy );
                rect.draw( gc, true );
                fy += rect.getHeight() + FacetRectangle.FACET_MARGIN;
                width = compute && rect.getWidth() > width ? rect.getWidth() + dxDetail : width;
            }
        }
        // Return the enclosing rectangle
        // Rectangle sRect = new Rectangle( x, y, width + FacetRectangle.FACET_MARGIN, fy - y );
        // log.debug( "Drew choice contents into " + sRect );
        // fRect.draw( gc, false );
        // return sRect;
        return new Rectangle( x, y, width + FacetRectangle.FACET_MARGIN, fy - y );
    }

}
