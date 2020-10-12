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
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.model.otmFacets.OtmQueryFacet;
import org.opentravel.model.otmFacets.OtmUpdateFacet;
import org.opentravel.model.otmLibraryMembers.OtmChoiceObject;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;

/**
 * Graphics Display Object (Sprite) for containing OTM business object.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class ContextualFacetSprite extends MemberSprite<OtmLibraryMember> implements DexSprite<OtmLibraryMember> {
    // private static Log log = LogFactory.getLog( BusinessObjectSprite.class );

    public ContextualFacetSprite(OtmContextualFacet member, SpriteManager manager, GraphicsContext paramsGC) {
        super( member, manager, paramsGC );
    }

    @Override
    public Rectangle drawContents(GraphicsContext gc, Font font, final double x, final double y) {
        boolean compute = gc == null;

        double ox = 0;
        if (member instanceof OtmCustomFacet)
            ox = FacetRectangle.CUSTOM_OFFSET;
        else if (member instanceof OtmQueryFacet)
            ox = FacetRectangle.QUERY_OFFSET;
        else if (member instanceof OtmChoiceObject)
            ox = FacetRectangle.CHOICE_OFFSET;
        else if (member instanceof OtmUpdateFacet)
            ox = FacetRectangle.UPDATE_OFFSET;

        double fy = y;
        double width = getBoundaries().getWidth() - ox - FacetRectangle.FACET_MARGIN;
        double fx = x + ox;

        Rectangle rect = new Rectangle( 0, 0, 0, 0 );

        if (!getMember().getChildren().isEmpty()) {
            rect = new FacetRectangle( (OtmContextualFacet) member, this, width );
            rect.set( fx, fy );
            rect.draw( gc, true );
            width = compute && rect.getWidth() > width ? rect.getWidth() + fx : width;
        }

        // Rectangle fRect = new Rectangle( rect.getX(), rect.getY(), width, rect.getHeight() );
        // log.debug( "Drew CF contents into " + fRect );
        // return fRect;
        return new Rectangle( rect.getX(), rect.getY(), width, rect.getHeight() );
    }

}
