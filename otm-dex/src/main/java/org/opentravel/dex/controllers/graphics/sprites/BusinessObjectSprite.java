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


    // @Override
    // public Rectangle drawContents(GraphicsContext gc, Font font, final double x, final double y) {
    // return drawContents( gc, x, y );
    // }

    @Override
    public Rectangle drawContents(GraphicsContext gc, final double x, final double y) {
        double width = getBoundaries().getWidth();
        Rectangle mRect = new Rectangle( 0, 0, 0, 0 );

        // TODO - aliases

        // Show facets
        if (!isCollapsed())
            mRect = drawFacets( getMember(), gc, x, y, width );

        // log.debug( "Drew " + getMember() + " contents into " + getBoundaries() );
        return mRect;
    }


    public Rectangle drawFacets(OtmLibraryMember member, GraphicsContext gc, final double x, final double y,
        double width) {

        // boolean compute = gc == null;
        FacetRectangle rect = null;

        double fy = y + margin;
        double fw = getBoundaries().getWidth() - margin;
        double offset = 0;
        // Mouse clicks use rectangle regions to determine source. If these large rectangles are added to list, they
        // will receive the mouse clicks. Consider "active rectangle" interface as solution.
        if (!isCollapsed()) {
            // if (!getFacetRectangles().isEmpty()) {
            // for (FacetRectangle fr : getFacetRectangles()) {
            // offset = fr.getX() - getBoundaries().getX();
            // fr.set( fr.getX(), fr.getY(), fw - offset ).draw( gc, true );
            // fy += fr.getHeight() + margin;
            // }
            // } else {
            rect = new FacetRectangle( getMember().getIdFacet(), this, width - dxID );
            rect.set( x + dxID, fy ).draw( gc, true );
            fy += rect.getHeight() + margin;
            width = computeWidth( width, rect, dxID );
            // add( rect );

            rect = new FacetRectangle( getMember().getSummary(), this, width - dxSummary );
            rect.set( x + dxSummary, fy ).draw( gc, true );
            fy += rect.getHeight() + margin;
            width = computeWidth( width, rect, dxSummary );
            // add( rect );

            if (!getMember().getDetail().getChildren().isEmpty()) {
                rect = new FacetRectangle( getMember().getDetail(), this, width - dxDetail );
                rect.set( x + dxDetail, fy ).draw( gc, true );
                fy += rect.getHeight() + margin;
                width = computeWidth( width, rect, dxDetail );
                // add( rect );
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
                rect.set( x + dxCustom, fy ).draw( gc, true );
                fy += rect.getHeight() + margin;
                width = computeWidth( width, rect, dxSummary );
                // add( rect );
            }
            for (OtmQueryFacet f : queries) {
                rect = new FacetRectangle( f, this, width - dxQuery );
                rect.set( x + dxQuery, fy ).draw( gc, true );
                fy += rect.getHeight() + margin;
                width = computeWidth( width, rect, dxID );
                // add( rect );
            }
            for (OtmUpdateFacet f : updates) {
                rect = new FacetRectangle( f, this, width - dxUpdate );
                rect.set( x + dxUpdate, fy ).draw( gc, true );
                fy += rect.getHeight() + margin;
                width = computeWidth( width, rect, dxID );
                // add( rect );
            }
            // }
        }

        // Return the enclosing rectangle
        // Rectangle fRect = new Rectangle( x, y, width + margin, fy - y );
        // fRect.draw( gc, false );
        // return fRect;
        return new Rectangle( x, y, width + margin, fy - y );
    }
}
