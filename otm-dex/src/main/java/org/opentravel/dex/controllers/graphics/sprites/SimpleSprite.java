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
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;

/**
 * Graphics Display Object (Sprite) for containing OTM Emumeration object.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class SimpleSprite extends MemberSprite<OtmSimpleObjects<?>> implements DexSprite<OtmLibraryMember> {

    public SimpleSprite(OtmSimpleObjects<?> member, SpriteManager manager, GraphicsContext paramsGC) {
        super( member, manager, paramsGC );
    }

    @Override
    public Rectangle drawContents(GraphicsContext gc, Font font, final double x, final double y) {
        boolean compute = gc == null;
        Rectangle rect = null;

        double dx = FacetRectangle.ID_OFFSET;
        double width = getBoundaries().getWidth();
        double fy = y + FacetRectangle.FACET_MARGIN;

        // Show base type
        // // Show open's Other property
        // if (getMember() instanceof OtmEnumerationOpen) {
        // rect = GraphicsUtils.drawLabel( "Other", null, false, gc, font, x + dx, fy );
        // fy += rect.getHeight() + FacetRectangle.FACET_MARGIN;
        // }
        //
        // // Show values
        // if (!isCollapsed()) {
        // rect = new FacetRectangle( getMember(), this, width - dx );
        // rect.set( x + dx, fy ).draw( gc, true );
        // fy += rect.getHeight() + FacetRectangle.FACET_MARGIN;
        // width = computeWidth( compute, width, rect, dx );
        // }
        // // Return the enclosing rectangle
        // // Rectangle sRect = new Rectangle( x, y, width + FacetRectangle.FACET_MARGIN, fy - y );
        // // log.debug( "Drew choice contents into " + sRect );
        // // fRect.draw( gc, false );
        // // return sRect;
        return new Rectangle( x, y, width + FacetRectangle.FACET_MARGIN, fy - y );
    }

}
