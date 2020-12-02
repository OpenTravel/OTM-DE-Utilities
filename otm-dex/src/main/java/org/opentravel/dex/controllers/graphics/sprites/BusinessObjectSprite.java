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
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.model.otmFacets.OtmQueryFacet;
import org.opentravel.model.otmFacets.OtmUpdateFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;

/**
 * Graphics Display Object (Sprite) for containing OTM business object.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class BusinessObjectSprite extends MemberSprite<OtmLibraryMember> implements DexSprite {
    // private static Log log = LogFactory.getLog( BusinessObjectSprite.class );

    double dxID;
    double dxSummary;
    double dxDetail;
    double dxCustom;
    double dxQuery;
    double dxUpdate;
    double margin;

    public BusinessObjectSprite(OtmBusinessObject member, SpriteManager manager) {
        super( member, manager );

        dxID = settingsManager.getOffset( Offsets.ID );
        dxSummary = settingsManager.getOffset( Offsets.SUMMARY );
        dxDetail = settingsManager.getOffset( Offsets.DETAIL );
        dxCustom = settingsManager.getOffset( Offsets.CUSTOM );
        dxQuery = settingsManager.getOffset( Offsets.QUERY );
        dxUpdate = settingsManager.getOffset( Offsets.UPDATE );
        margin = settingsManager.getMargin( Margins.FACET );
    }

    @Override
    public OtmBusinessObject getMember() {
        return (OtmBusinessObject) member;
    }

    @Override
    public Rectangle drawContents(GraphicsContext gc, final double x, final double y) {
        double width = getBoundaries().getWidth();
        Rectangle mRect = new Rectangle( 0, 0, 0, 0 );

        // TODO - aliases

        // Show facets
        if (!isCollapsed())
            mRect = drawFacets( getMember(), gc, x, y, width );

        // log.debug( "Drew contents of " + getMember() + " contents into " + getBoundaries() );
        return mRect;
    }


    public Rectangle drawFacets(OtmLibraryMember member, GraphicsContext gc, final double x, final double y,
        double width) {

        // boolean compute = gc == null;
        FacetRectangle rect = null;

        double fy = y + margin;
        // double fw = getBoundaries().getWidth() - margin;
        // double offset = 0;

        // Mouse clicks use rectangle regions to determine source. If these large rectangles are added to list, they
        // will receive the mouse clicks. Consider "active rectangle" interface as solution.
        if (!isCollapsed()) {
            rect = new FacetRectangle( getMember().getIdFacet(), this, width - dxID );
            width = draw( rect, gc, width, x, dxID, y );
            fy += rect.getHeight() + margin;

            rect = new FacetRectangle( getMember().getSummary(), this, width - dxSummary );
            width = draw( rect, gc, width, x, dxSummary, fy );
            fy += rect.getHeight() + margin;

            if (!getMember().getDetail().getChildren().isEmpty()) {
                rect = new FacetRectangle( getMember().getDetail(), this, width - dxDetail );
                width = draw( rect, gc, width, x, dxDetail, fy );
                fy += rect.getHeight() + margin;
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
                rect = new FacetRectangle( f, this, width - dxCustom );
                width = draw( rect, gc, width, x, dxCustom, fy );
                fy += rect.getHeight() + margin;
            }
            for (OtmQueryFacet f : queries) {
                rect = new FacetRectangle( f, this, width - dxQuery );
                width = draw( rect, gc, width, x, dxQuery, fy );
                fy += rect.getHeight() + margin;
            }
            for (OtmUpdateFacet f : updates) {
                rect = new FacetRectangle( f, this, width - dxUpdate );
                width = draw( rect, gc, width, x, dxUpdate, fy );
                fy += rect.getHeight() + margin;
            }
        }

        return new Rectangle( x, y, width + margin, fy - y );
    }
}
