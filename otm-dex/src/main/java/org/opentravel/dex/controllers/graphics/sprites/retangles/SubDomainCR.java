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
import org.opentravel.dex.controllers.graphics.sprites.DomainSprite;

import javafx.scene.canvas.GraphicsContext;

/**
 * Rectangle for a click-able, single sub-domain property.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class SubDomainCR extends ClickableRectangle {
    private static Log log = LogFactory.getLog( SubDomainCR.class );

    private String sd; // The property used in connect clicks

    /**
     * 
     * @param sprite - parent domain sprite
     * @param name - the base namespace
     * @param width
     */
    public SubDomainCR(DomainSprite sprite, String subDomain, double width) {
        super( sprite, DomainSprite.getDomainName( subDomain ), ImageManager.getImage( Icons.DOMAIN ), width );
        this.sd = subDomain;
    }

    @Override
    public Rectangle draw(GraphicsContext gc, double x, double y) {
        LabelRectangle lr = drawLabel( gc, x, y, sd );
        drawVerticalLine( gc, lr, margin );
        drawUnderline( gc, lr, width, margin );
        drawConnector( gc, lr, connectorColor );

        // log.debug( "Drew/sized " + this );
        // drawOutline( gc, false ); // debug
        return this;
    }

    @Override
    public String toString() {
        return "Sub-Domain: " + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
