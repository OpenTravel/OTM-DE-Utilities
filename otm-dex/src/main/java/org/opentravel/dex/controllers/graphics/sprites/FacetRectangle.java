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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 * Graphics utility for containing facet regions.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class FacetRectangle extends Rectangle {
    private static Log log = LogFactory.getLog( FacetRectangle.class );

    protected static final double FACET_MARGIN = 5;
    protected static final double FACET_OFFSET = 8;

    /**
     * Render methods that create rectangles may set the event to run if the implement this interface.
     * <p>
     * Example: r.setOnMouseClicked( e -> manager.remove( this ) );
     */
    public abstract interface RectangleEventHandler {
        public void onRectangleClick(MouseEvent e);
    }

    private static final Paint FACET_COLOR = Color.ANTIQUEWHITE;

    private OtmFacet<?> facet;
    private DexSprite<OtmLibraryMember> parent;
    private Font font;

    @Deprecated
    public FacetRectangle(double x, double y, double width, double height) {
        super( x, y, width, height );
    }

    public FacetRectangle(OtmFacet<?> facet, DexSprite<OtmLibraryMember> parentSprite, Font font, double width) {
        super( 0, 0, width, 0 );
        this.facet = facet;
        this.parent = parentSprite;
        this.font = font;
        // Compute the size
        draw( null, font );
    }

    /**
     * Draw the facet.
     * 
     * @param gc
     * @param filled
     */
    @Override
    public Rectangle draw(GraphicsContext gc, boolean filled) {
        if (gc != null) {
            Paint savedColor = gc.getFill();
            gc.setFill( FACET_COLOR );

            super.draw( gc, false ); // draw outline
            if (filled)
                super.draw( gc, true ); // Draw fill

            draw( gc, font );

            gc.setFill( savedColor );
        }
        return this;
    }


    private Rectangle draw(GraphicsContext gc, Font font) {
        boolean compute = gc == null;

        // Label
        Rectangle lRect = GraphicsUtils.drawLabel( facet.getName(), facet.getIcon(), gc, font, x, y );
        // lRect.draw( gc, false );

        height = lRect.getHeight();
        width = compute && lRect.getWidth() > width ? lRect.getWidth() : width;

        // Properties
        PropertyRectangle pRect = null;
        double py = y + lRect.getHeight();
        double px = x + PropertyRectangle.PROPERTY_OFFSET;
        if (!facet.getChildren().isEmpty()) {
            for (OtmObject c : facet.getChildren()) {
                if (c instanceof OtmProperty) {
                    pRect = new PropertyRectangle( (OtmProperty) c, parent, font, width );
                    pRect.set( px, py );
                    pRect.draw( gc, true );

                    height += pRect.getHeight();
                    width = compute && pRect.getWidth() > width ? pRect.getWidth() : width;

                    py += pRect.getHeight();

                    if (!compute) {
                        if (c instanceof OtmTypeUser)
                            pRect.setOnMouseClicked(
                                e -> parent.connect( ((OtmTypeUser) c), parent, e.getX(), e.getY() ) );
                        parent.add( pRect );
                    }
                }
            }
            // Draw vertical line
            if (pRect != null && !compute) {
                px = px + PropertyRectangle.PROPERTY_MARGIN - 1;
                double ly = y + height - 2 * PropertyRectangle.PROPERTY_MARGIN - 1;
                gc.strokeLine( px, y + lRect.getHeight(), px, ly );
            }
        }
        log.debug( "Drew/sized " + this );
        // super.draw( gc, false ); // debug
        return this;
    }

    @Override
    public String toString() {
        return "Facet: " + facet + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
