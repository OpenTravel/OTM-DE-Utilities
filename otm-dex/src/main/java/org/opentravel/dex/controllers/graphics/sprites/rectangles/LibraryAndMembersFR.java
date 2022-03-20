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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.dex.controllers.graphics.sprites.ProvidersSprite;
import org.opentravel.model.otmContainers.OtmDomain;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import java.util.List;
import java.util.Map.Entry;

import javafx.scene.canvas.GraphicsContext;

/**
 * Virtual facet containing a library that provide types to a domain.
 * <p>
 * Parent is the domain provider Facet Rectangle.
 * <p>
 * 
 * @author dmh
 * @param <O>
 *
 */
public class LibraryAndMembersFR extends CollapsableRectangle {
    private static Logger log = LogManager.getLogger( LibraryAndMembersFR.class );

    private OtmLibrary providerLib = null;
    // private Map<OtmLibrary,List<OtmLibraryMember>> map;
    private OtmDomain usersDomain = null;
    private DomainProviderFR parentFR = null;
    private List<OtmLibraryMember> providers;

    /**
     * 
     * @param parent - domain that uses these type providers
     * @param width - overrides actual width if gc != null when drawn
     */
    public LibraryAndMembersFR(DomainProviderFR parentFR, Entry<OtmLibrary,List<OtmLibraryMember>> entry, double width,
        boolean collapsed) {
        super( parentFR.getSprite(), width, "Provider Library", ImageManager.getImage( Icons.LIBRARY ) );
        this.collapsed = collapsed;
        this.parentFR = parentFR;

        this.providerLib = entry.getKey();
        this.providers = entry.getValue();
        if (this.providerLib == null)
            throw new IllegalArgumentException( "Must have provider library." );
        this.label = providerLib.getName();

        this.usersDomain = parentFR.getUsersDomain();
        if (usersDomain == null)
            throw new IllegalArgumentException( "Must have users domain." );

    }

    public ProvidersSprite getSprite() {
        return getParent().getSprite();
    }

    public DomainProviderFR getParent() {
        return parentFR;
    }

    @Override
    public Rectangle draw(GraphicsContext gc) {
        // draw label and control
        super.draw( gc );
        // double connectorSize = 16;

        // Properties are the type provider members
        if (!collapsed) {
            double fy = y + height;
            double fx = x + offset;

            LabelRectangle lr = null;
            for (OtmLibraryMember provider : providers) {
                log.debug( "Create facet for " + provider );
                // todo - create clickable for providers
                lr = new LabelRectangle( getSprite(), provider.getName(), provider.getIcon(), provider.isEditable(),
                    provider.isInherited(), false );
                lr.draw( gc, fx, fy );
                //
                drawVerticalLine( gc, lr, fx );
                //
                fy += lr.getHeight();
                height += lr.getHeight();
            }

            // for (Entry<OtmLibrary,List<OtmLibraryMember>> entry : map.entrySet()) {
            // log.debug( "Create facet for " + entry.getKey().getName() );
            // }
            // for (String sd : ((DomainSprite) parent).getSubDomainNames()) {
            // SubDomainCR subCR = new SubDomainCR( (DomainSprite) parent, sd, width );
            // subCR.draw( gc, fx, fy );
            //
            // if (gc == null)
            // width = DexSpriteBase.computeWidth( width, lr, margin );
            // fy += subCR.getHeight();
            // height += subCR.getHeight();
            // }
        }
        height = height + 2 * margin;

        // log.debug( "Drew/sized - compute = " + compute + " " + this );
        // drawOutline( gc, false ); // debug
        return this;

    }

    @Override
    public String toString() {
        return "Sub-Domain Facet: " + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
