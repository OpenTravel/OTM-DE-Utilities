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

package org.opentravel.dex.controllers.graphics.sprites.retangles;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.graphics.sprites.DexSpriteBase;
import org.opentravel.dex.controllers.graphics.sprites.DomainSprite;

import javafx.scene.canvas.GraphicsContext;

/**
 * Graphics utility for containing virtual facet containing libraries in a domain.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class LibraryFacetRectangle extends CollapsableRectangle {
    private static Log log = LogFactory.getLog( LibraryFacetRectangle.class );

    /**
     * 
     * @param obj - must have object
     * @param parent - must have parent with member
     * @param width - overrides actual width if gc != null when drawn
     */
    public LibraryFacetRectangle(DomainSprite parent, double width, boolean collapsed) {
        super( parent, width, "Libraries", null );
        this.collapsed = collapsed;
        log.debug( "New library facet rectangle." );
    }

    // OtmVersionChain chain = null;
    // boolean latest = libr.getLibrary().isLatestVersion();
    @Override
    public Rectangle draw(GraphicsContext gc) {
        // draw label and control
        super.draw( gc );

        // Properties = libraries
        if (!collapsed) {
            double fy = y + height;
            double fx = x + offset;
            for (LibraryRectangle libr : ((DomainSprite) parent).getLibraries()) {
                libr.draw( gc, fx, fy );
                width = DexSpriteBase.computeWidth( width, libr, margin );
                drawVerticalLine( gc, libr, margin );
                drawUnderline( gc, libr, 0, margin );
                fy += libr.getHeight();
                height += libr.getHeight();

                // if (gc != null)
                // log.debug( "Drew " + libr );
                // else
                // log.debug( "sized " + libr );
            }
        }
        height = height + 2 * margin;
        // if (gc != null)
        // log.debug( "Drew " + this );
        // else
        // log.debug( "sized " + this );
        // super.draw( gc, false ); // debug
        return this;
    }

    @Override
    public String toString() {
        return "Library Facet Rec: x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
