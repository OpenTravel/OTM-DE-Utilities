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

import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
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
public class BusinessObjectSprite extends MemberSprite<OtmLibraryMember> implements DexSprite<OtmLibraryMember> {
    // private static Log log = LogFactory.getLog( BusinessObjectSprite.class );

    public BusinessObjectSprite(OtmBusinessObject member, SpriteManager manager, GraphicsContext paramsGC) {
        super( member, manager, paramsGC );
    }

    @Override
    public OtmBusinessObject getMember() {
        return (OtmBusinessObject) member;
    }

    @Override
    public Rectangle drawContents(GraphicsContext gc, Font font, final double x, final double y) {
        double width = getBoundaries().getWidth();
        Rectangle mRect = new Rectangle( 0, 0, 0, 0 );

        // TODO - aliases

        // Show facets
        if (!isCollapsed())
            mRect = drawFacets( getMember(), gc, font, x, y, width );

        // log.debug( "Drew contents into " + mRect );
        return mRect;
    }

    public Rectangle drawFacets(OtmLibraryMember member, GraphicsContext gc, Font font, final double x, final double y,
        double width) {

        boolean compute = gc == null;
        FacetRectangle rect = null;
        double dxID = GraphicsUtils.PROPERTY_OFFSET;
        double dxSummary = dxID + GraphicsUtils.FACET_OFFSET;
        double dxDetail = dxSummary + GraphicsUtils.FACET_OFFSET;

        double fy = y + GraphicsUtils.FACET_MARGIN;
        // double fWidth = width - GraphicsUtils.PROPERTY_OFFSET;
        // width - GraphicsUtils.PROPERTY_OFFSET - GraphicsUtils.PROPERTY_MARGIN - GraphicsUtils.FACET_MARGIN;

        if (!isCollapsed()) {
            // for (OtmObject child : member.getChildren())
            // if (child instanceof OtmFacet && !(child instanceof OtmContributedFacet)
            // && !((OtmFacet<?>) child).getChildren().isEmpty()) {

            rect = new FacetRectangle( getMember().getIdFacet(), this, font, width - dxID );
            rect.set( x + dxID, fy );
            rect.draw( gc, true );
            fy += rect.getHeight() + GraphicsUtils.FACET_MARGIN;
            width = compute && rect.getWidth() > width ? rect.getWidth() + dxID : width;

            rect = new FacetRectangle( getMember().getSummary(), this, font, width - dxSummary );
            rect.set( x + dxSummary, fy );
            rect.draw( gc, true );
            fy += rect.getHeight() + GraphicsUtils.FACET_MARGIN;
            width = compute && rect.getWidth() > width ? rect.getWidth() + dxSummary : width;

            if (!getMember().getDetail().getChildren().isEmpty()) {
                rect = new FacetRectangle( getMember().getDetail(), this, font, width - dxDetail );
                rect.set( x + dxDetail, fy );
                rect.draw( gc, true );
                fy += rect.getHeight() + GraphicsUtils.FACET_MARGIN;
                width = compute && rect.getWidth() > width ? rect.getWidth() + dxDetail : width;
            }
            // double dx = rect.getX() - x + GraphicsUtils.PROPERTY_OFFSET;
            // if (gc == null && rect.getWidth() + dx > width)
            // width = rect.getWidth() + dx;
            // if (gc == null && rect.getWidth() > width)
            // width = rect.getWidth();
            // fx += 2;
            // fWidth -= 2;
            // }
        }
        // TODO - contributed and contextual facets
        Rectangle fRect = new Rectangle( x, y, width + GraphicsUtils.FACET_MARGIN, fy - y );
        // fRect.draw( gc, false );
        return fRect;
    }

}
