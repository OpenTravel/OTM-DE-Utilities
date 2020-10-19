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

    double dxID;
    double dxSummary;
    double dxDetail;
    double margin;


    public CoreObjectSprite(OtmCore member, SpriteManager manager, SettingsManager settingsManager) {
        super( member, manager, settingsManager );

        dxID = settingsManager.getOffset( Offsets.ID );
        dxSummary = settingsManager.getOffset( Offsets.SUMMARY );
        dxDetail = settingsManager.getOffset( Offsets.DETAIL );
        margin = settingsManager.getMargin( Margins.FACET );
    }


    @Override
    public Rectangle drawContents(GraphicsContext gc, Font font, final double x, final double y) {
        boolean compute = gc == null;
        Rectangle rect = null;

        double width = getBoundaries().getWidth();
        double fy = y + margin;

        // Show simple type
        rect = new PropertyRectangle( getMember(), this, width );
        rect.set( x + dxSummary, y );
        rect.draw( gc, true );
        fy += rect.getHeight();
        width = compute && rect.getWidth() > width ? rect.getWidth() + dxSummary : width;

        // Show facets
        if (!isCollapsed()) {

            rect = new FacetRectangle( getMember().getSummary(), this, width - dxSummary );
            rect.set( x + dxSummary, fy );
            rect.draw( gc, true );
            fy += rect.getHeight() + margin;
            width = compute && rect.getWidth() > width ? rect.getWidth() + dxSummary : width;

            if (!getMember().getDetail().getChildren().isEmpty()) {
                rect = new FacetRectangle( getMember().getDetail(), this, width - dxDetail );
                rect.set( x + dxDetail, fy );
                rect.draw( gc, true );
                fy += rect.getHeight() + margin;
                width = compute && rect.getWidth() > width ? rect.getWidth() + dxDetail : width;
            }
        }
        // Return the enclosing rectangle
        // Rectangle sRect = new Rectangle( x, y, width + margin, fy - y );
        // log.debug( "Drew choice contents into " + sRect );
        // fRect.draw( gc, false );
        // return sRect;
        return new Rectangle( x, y, width + margin, fy - y );
    }

}
