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

package org.opentravel.dex.controllers.graphics.sprites.rectangles;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.graphics.sprites.ProvidersSprite;
import org.opentravel.model.otmContainers.OtmDomain;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.scene.canvas.GraphicsContext;

/**
 * Virtual facet containing libraries that provide types to a domain.
 * <p>
 * Parent is the provider sprite associated with the user domain. It knows what domain the users are in.
 * <p>
 * Provider domain has the library list of type providers used by the user domain
 * 
 * @author dmh
 * @param <O>
 *
 */
public class DomainProviderFR extends CollapsableRectangle {
    private static Log log = LogFactory.getLog( DomainProviderFR.class );

    private OtmDomain providerDomain = null;
    private Map<OtmLibrary,List<OtmLibraryMember>> map;
    private OtmDomain userDomain = null;

    /**
     * 
     * @param parent - domain that uses these type providers
     * @param width - overrides actual width if gc != null when drawn
     */
    public DomainProviderFR(ProvidersSprite parent, OtmDomain providerDomain, double width, boolean collapsed) {
        super( parent, width, "Provider Domain", parent.getIcon() );
        this.collapsed = collapsed;

        this.providerDomain = providerDomain;
        if (this.providerDomain == null)
            throw new IllegalArgumentException( "Must have provider domain." );
        this.label = providerDomain.getName();

        this.userDomain = parent.getDomain().getOtmDomain();
        if (userDomain == null)
            throw new IllegalArgumentException( "Must have user domain." );

        map = userDomain.getProvidersMap();
    }

    public ProvidersSprite getSprite() {
        return (ProvidersSprite) parent;
    }

    public OtmDomain getUsersDomain() {
        return getSprite().getOtmDomain();
    }

    @Override
    public Rectangle draw(GraphicsContext gc) {
        // draw label and control
        super.draw( gc );
        // double connectorSize = 16;
        double fWidth = parent.getBoundaries().getWidth();
        double fy = y + height;
        double fx = x + offset;

        // Properties are the library facets
        if (!collapsed) {
            LibraryAndMembersFR lp = null;
            for (Entry<OtmLibrary,List<OtmLibraryMember>> entry : map.entrySet()) {
                log.debug( "Create facet for " + entry.getKey().getName() );
                lp = new LibraryAndMembersFR( this, entry, width, false );
                lp.draw( gc, fx, fy );
                //
                drawVerticalLine( gc, lp, fx );
                //
                if (gc == null)
                    fWidth = fWidth > width ? fWidth : width;
                fy += lp.getHeight();
                height += lp.getHeight();
            }
        }
        height = height + 2 * margin;
        if (gc == null)
            this.width = fWidth > width ? fWidth : width;

        // log.debug( "Drew/sized - compute = " + compute + " " + this );
        // drawOutline( gc, false ); // debug
        return this;

    }

    @Override
    public String toString() {
        return "Sub-Domain Facet: " + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
