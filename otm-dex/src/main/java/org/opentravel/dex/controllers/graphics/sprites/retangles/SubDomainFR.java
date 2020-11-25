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
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.dex.controllers.graphics.sprites.DexSpriteBase;
import org.opentravel.dex.controllers.graphics.sprites.DomainSprite;

import javafx.scene.canvas.GraphicsContext;

/**
 * Graphics utility for containing virtual facet containing sub-domains in a domain.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class SubDomainFR extends CollapsableRectangle {
    private static Log log = LogFactory.getLog( SubDomainFR.class );

    /**
     * 
     * @param obj - must have object
     * @param parent - must have parent with member
     * @param width - overrides actual width if gc != null when drawn
     */
    public SubDomainFR(DomainSprite parent, double width, boolean collapsed) {
        super( parent, width, "Sub-Domains", ImageManager.getImage( Icons.DOMAIN ) );
        this.collapsed = collapsed;
    }

    public DomainSprite getSprite() {
        return (DomainSprite) parent;
    }

    @Override
    public Rectangle draw(GraphicsContext gc) {
        // draw label and control
        super.draw( gc );
        // double connectorSize = 16;

        // Properties
        if (!collapsed) {
            double fy = y + height;
            double fx = x + offset;
            LabelRectangle lr = null;
            for (String sd : ((DomainSprite) parent).getSubDomainNames()) {
                SubDomainCR subCR = new SubDomainCR( (DomainSprite) parent, sd, width );
                subCR.draw( gc, fx, fy );

                if (gc == null)
                    width = DexSpriteBase.computeWidth( width, lr, margin );
                fy += subCR.getHeight();
                height += subCR.getHeight();
            }
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
