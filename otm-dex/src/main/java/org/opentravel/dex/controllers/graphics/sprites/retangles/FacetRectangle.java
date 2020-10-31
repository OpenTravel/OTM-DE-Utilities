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
import org.opentravel.dex.controllers.graphics.sprites.DexSprite;
import org.opentravel.dex.controllers.graphics.sprites.GraphicsUtils;
import org.opentravel.dex.controllers.graphics.sprites.GraphicsUtils.DrawType;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Margins;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Offsets;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmEnumeration;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.resource.OtmAction;
import org.opentravel.model.resource.OtmActionRequest;
import org.opentravel.model.resource.OtmActionResponse;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
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

    // public static final double FACET_MARGIN = 5;
    // public static final double FACET_OFFSET = 8;

    // Left Margin offsets per facet type
    // public static final double ID_OFFSET = FACET_OFFSET;
    // public static final double SHARED_OFFSET = ID_OFFSET;
    // public static final double QUERY_OFFSET = ID_OFFSET;
    // public static final double UPDATE_OFFSET = ID_OFFSET;
    // public static final double SUMMARY_OFFSET = ID_OFFSET + FACET_OFFSET;
    // public static final double CHOICE_OFFSET = SUMMARY_OFFSET;
    // public static final double DETAIL_OFFSET = SUMMARY_OFFSET + FACET_OFFSET;
    // public static final double CUSTOM_OFFSET = DETAIL_OFFSET;

    private static final Paint FACET_COLOR = Color.ANTIQUEWHITE;


    // /**
    // * Render methods that create rectangles may set the event to run if the implement this interface.
    // * <p>
    // * Example: r.setOnMouseClicked( e -> manager.remove( this ) );
    // */
    // public abstract interface RectangleEventHandler {
    // public void onRectangleClick(MouseEvent e);
    // }
    //

    private OtmFacet<?> facet = null;
    private OtmObject otmObject = null;
    private DexSprite parent;
    private Font font;
    private String label;
    private Image icon;
    private List<OtmObject> children;
    boolean editable = false;
    private SettingsManager settings;

    double pMargin;
    double pOffset;
    private boolean collapsed = false;

    public FacetRectangle(OtmObject obj, DexSprite parent, double width, String label, Image icon) {
        super( 0, 0, width, 0 );
        this.parent = parent;
        this.font = parent.getFont();
        this.icon = icon;
        this.label = label;
        this.editable = obj.isEditable();
        if (obj instanceof OtmChildrenOwner)
            this.children = getChildren( (OtmChildrenOwner) obj );
        settings = parent.getSettingsManager();
        this.otmObject = obj;
        collapsed = !obj.isExpanded();

        if (settings == null)
            throw new IllegalArgumentException( "Must have settings" );

        pMargin = settings.getMargin( Margins.PROPERTY );
        pOffset = settings.getOffset( Offsets.PROPERTY );

        // log.debug( "Created facet rectangle: " + label );
    }

    public FacetRectangle(OtmFacet<?> facet, DexSprite parentSprite, double width) {
        this( facet, parentSprite, width, facet.getName(), facet.getIcon() );
        this.facet = facet;
        // Compute the size
        draw( null );
    }

    public FacetRectangle(OtmContextualFacet member, DexSprite parentSprite, double width) {
        this( member.getWhereContributed(), parentSprite, width, member.getName(), member.getIcon() );
        this.facet = member.getWhereContributed();

        // Compute the size
        draw( null );
    }

    public FacetRectangle(OtmEnumeration<?> member, DexSprite parentSprite, double width) {
        this( member, parentSprite, width, null, null );
        // this.otmObject = member;
        // Compute the size
        draw( null );
    }

    public FacetRectangle(OtmValueWithAttributes member, DexSprite parentSprite, double width) {
        this( member, parentSprite, width, null, null );
        // this.otmObject = member;
        // Compute the size
        draw( null );
    }

    public FacetRectangle(OtmAction action, DexSprite parentSprite, double width) {
        this( action, parentSprite, width, action.getName(), action.getIcon() );
        this.children = action.getChildren();
        draw( null );
    }

    private List<OtmObject> getChildren(OtmChildrenOwner owner) {
        List<OtmObject> kids = new ArrayList<>();
        kids.addAll( owner.getInheritedChildren() );
        kids.addAll( owner.getChildren() );
        return kids;
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

            draw( gc );

            gc.setFill( savedColor );
        }
        return this;
    }

    @Override
    public Rectangle draw(GraphicsContext gc) {
        boolean compute = gc == null;
        height = 0;
        double margin = settings.getMargin( Margins.MEMBER );
        Rectangle r = null;

        if (otmObject instanceof OtmFacet) {
            Image collapse = null;
            if (otmObject.isExpanded())
                collapse = ImageManager.getImage( ImageManager.Icons.COLLAPSE );
            else
                collapse = ImageManager.getImage( ImageManager.Icons.EXPAND );
            double cx = x;
            double cy = y + margin;
            r = GraphicsUtils.drawImage( collapse, DrawType.OUTLINE, gc, cx, cy );
            if (gc != null && facet != null) {
                parent.add( r );
                r.setOnMouseClicked( e -> parent.collapseOrExpand( facet ) );
            }
            // width += r.getWidth() + margin;
        }

        // Label
        double lx = r == null ? x : x + r.getWidth();
        if (label != null) {
            Rectangle lRect = new LabelRectangle( parent, label, icon, editable, false, false ).draw( gc, lx, y );
            height = lRect.getHeight();
            width = compute && lRect.getWidth() > width ? lRect.getWidth() : width;
            // lRect.draw( gc, false );
        }
        // prefix
        if (otmObject instanceof OtmContributedFacet) {
            LabelRectangle pRect =
                new LabelRectangle( parent, otmObject.getPrefix(), null, otmObject.isEditable(), false, false );
            pRect.draw( gc, x + width - pRect.getWidth(), y );
            // width += pRect.getWidth();
        }


        // Properties
        collapsed = otmObject instanceof OtmFacet ? !otmObject.isExpanded() : false;
        PropertyRectangle pRect = null;
        double py = y + height;
        double px = x + pOffset;
        if (!children.isEmpty() && !collapsed) {
            for (OtmObject c : children) {
                pRect = null;
                if (c instanceof OtmProperty)
                    pRect = new PropertyRectangle( (OtmProperty) c, parent, width );
                else if (c instanceof OtmActionRequest)
                    pRect = new PropertyRectangle( (OtmActionRequest) c, parent, width );
                else if (c instanceof OtmActionResponse)
                    pRect = new PropertyRectangle( (OtmActionResponse) c, parent, width );
                if (pRect != null) {
                    pRect.set( px, py ).draw( gc, true );
                    height += pRect.getHeight();
                    width = compute && pRect.getWidth() > width ? pRect.getWidth() : width;
                    py += pRect.getHeight();
                }
            }
            // Draw vertical line
            if (pRect != null && !compute) {
                px = px + pMargin - 1;
                double ly = y + height - 2 * pMargin - 1;
                gc.strokeLine( px, y + pRect.getHeight(), px, ly );
            }
        }
        // log.debug( "Drew/sized - compute = " + compute + " " + this );
        // super.draw( gc, false ); // debug
        return this;
    }

    @Override
    public String toString() {
        return "Facet: " + facet + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
