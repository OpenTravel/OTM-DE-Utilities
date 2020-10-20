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
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.VWAPropertyRectangle;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;

/**
 * Graphics Display Object (Sprite) for containing OTM VWA object.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class VWASprite extends MemberSprite<OtmValueWithAttributes> implements DexSprite<OtmLibraryMember> {
    // gets log from base class
    // private static Log log = LogFactory.getLog( BusinessObjectSprite.class );

    double dxSummary;
    double margin;

    public VWASprite(OtmValueWithAttributes member, SpriteManager manager, SettingsManager settingsManager) {
        super( member, manager, settingsManager );

        dxSummary = settingsManager.getOffset( Offsets.SUMMARY );
        margin = settingsManager.getMargin( Margins.FACET );
    }

    @Override
    public Rectangle drawContents(GraphicsContext gc, Font font, final double x, final double y) {
        boolean compute = gc == null;
        Rectangle rect = null;

        double width = getBoundaries().getWidth();
        double fy = y + margin;

        // Show value type
        rect = new VWAPropertyRectangle( getMember(), this, width );
        rect.set( x + dxSummary, fy ).draw( gc, true );
        fy += rect.getHeight() + margin;
        width = compute && rect.getWidth() > width ? rect.getWidth() + dxSummary : width;

        // Show facets
        if (!isCollapsed()) {
            rect = new FacetRectangle( getMember(), this, width - dxSummary );
            rect.set( x + dxSummary, fy ).draw( gc, true );
            fy += rect.getHeight() + margin;
            width = compute && rect.getWidth() > width ? rect.getWidth() + dxSummary : width;

        }
        // Return the enclosing rectangle
        // Rectangle sRect = new Rectangle( x, y, width + margin, fy - y );
        // log.debug( "Drew choice contents into " + sRect );
        // fRect.draw( gc, false );
        // return sRect;
        return new Rectangle( x, y, width + margin, fy - y );
    }

}
