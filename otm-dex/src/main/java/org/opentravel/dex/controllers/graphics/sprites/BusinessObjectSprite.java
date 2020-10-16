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
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.model.otmFacets.OtmQueryFacet;
import org.opentravel.model.otmFacets.OtmUpdateFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import java.util.ArrayList;
import java.util.List;

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

    public BusinessObjectSprite(OtmBusinessObject member, SpriteManager manager, SettingsManager settingsManager) {
        super( member, manager, settingsManager );
    }

    @Override
    public OtmBusinessObject getMember() {
        return (OtmBusinessObject) member;
    }

    @Override
    public Rectangle drawContents(final double x, final double y) {
        return drawContents( settingsManager.getGc(), settingsManager.getFont(), x, y );
    }


    @Override
    public Rectangle drawContents(GraphicsContext gc, Font font, final double x, final double y) {
        double width = getBoundaries().getWidth();
        Rectangle mRect = new Rectangle( 0, 0, 0, 0 );

        // TODO - aliases

        // Show facets
        if (!isCollapsed())
            mRect = drawFacets( getMember(), gc, font, x, y, width );

        log.debug( "Drew " + getMember() + " contents into " + getBoundaries() );
        return mRect;
    }

    public Rectangle drawFacets(OtmLibraryMember member, GraphicsContext gc, Font font, final double x, final double y,
        double width) {

        boolean compute = gc == null;
        FacetRectangle rect = null;
        double dxID = FacetRectangle.ID_OFFSET;
        double dxSummary = FacetRectangle.SUMMARY_OFFSET;
        double dxDetail = FacetRectangle.DETAIL_OFFSET;

        double fy = y + FacetRectangle.FACET_MARGIN;

        if (!isCollapsed()) {

            rect = new FacetRectangle( getMember().getIdFacet(), this, width - dxID );
            rect.set( x + dxID, fy ).draw( gc, true );
            fy += rect.getHeight() + FacetRectangle.FACET_MARGIN;
            width = computeWidth( compute, width, rect, dxID );

            rect = new FacetRectangle( getMember().getSummary(), this, width - dxSummary );
            rect.set( x + dxSummary, fy ).draw( gc, true );
            fy += rect.getHeight() + FacetRectangle.FACET_MARGIN;
            width = computeWidth( compute, width, rect, dxSummary );

            if (!getMember().getDetail().getChildren().isEmpty()) {
                rect = new FacetRectangle( getMember().getDetail(), this, width - dxDetail );
                rect.set( x + dxDetail, fy ).draw( gc, true );
                fy += rect.getHeight() + FacetRectangle.FACET_MARGIN;
                width = computeWidth( compute, width, rect, dxDetail );
            }
        }
        List<OtmCustomFacet> customs = new ArrayList<>();
        List<OtmQueryFacet> queries = new ArrayList<>();
        List<OtmUpdateFacet> updates = new ArrayList<>();
        for (OtmContributedFacet cf : member.getChildrenContributedFacets())
            if (cf.getContributor() instanceof OtmCustomFacet)
                customs.add( (OtmCustomFacet) cf.getContributor() );
            else if (cf.getContributor() instanceof OtmQueryFacet)
                queries.add( (OtmQueryFacet) cf.getContributor() );
            else if (cf.getContributor() instanceof OtmUpdateFacet)
                updates.add( (OtmUpdateFacet) cf.getContributor() );

        for (OtmCustomFacet f : customs) {
            rect = new FacetRectangle( f, this, width - FacetRectangle.CUSTOM_OFFSET );
            rect.set( x + FacetRectangle.CUSTOM_OFFSET, fy ).draw( gc, true );
            fy += rect.getHeight() + FacetRectangle.FACET_MARGIN;
            width = computeWidth( compute, width, rect, dxSummary );
        }
        for (OtmQueryFacet f : queries) {
            rect = new FacetRectangle( f, this, width - FacetRectangle.QUERY_OFFSET );
            rect.set( x + FacetRectangle.QUERY_OFFSET, fy ).draw( gc, true );
            fy += rect.getHeight() + FacetRectangle.FACET_MARGIN;
            width = computeWidth( compute, width, rect, dxID );
        }
        for (OtmUpdateFacet f : updates) {
            rect = new FacetRectangle( f, this, width - FacetRectangle.UPDATE_OFFSET );
            rect.set( x + FacetRectangle.UPDATE_OFFSET, fy ).draw( gc, true );
            fy += rect.getHeight() + FacetRectangle.FACET_MARGIN;
            width = computeWidth( compute, width, rect, dxID );
        }

        // Return the enclosing rectangle
        // Rectangle fRect = new Rectangle( x, y, width + FacetRectangle.FACET_MARGIN, fy - y );
        // fRect.draw( gc, false );
        // return fRect;
        return new Rectangle( x, y, width + FacetRectangle.FACET_MARGIN, fy - y );
    }
}
