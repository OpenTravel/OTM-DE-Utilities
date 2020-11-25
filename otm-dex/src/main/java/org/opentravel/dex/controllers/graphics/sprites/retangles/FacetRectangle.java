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
import org.opentravel.dex.controllers.graphics.sprites.GraphicsUtils;
import org.opentravel.dex.controllers.graphics.sprites.GraphicsUtils.DrawType;
import org.opentravel.dex.controllers.graphics.sprites.MemberSprite;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Margins;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Offsets;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmEnumeration;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.resource.OtmAction;
import org.opentravel.model.resource.OtmActionRequest;
import org.opentravel.model.resource.OtmActionResponse;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;

/**
 * Graphics utility for containing facet regions.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class FacetRectangle extends Rectangle {
    private static Log log = LogFactory.getLog( FacetRectangle.class );

    private OtmFacet<?> facet = null;
    private OtmObject otmObject = null;

    private MemberSprite<?> parent;
    private SettingsManager settings;

    private String label;
    private Image icon;

    private List<OtmObject> children;
    boolean editable = false;

    double propertyMargin;
    double propertyOffset;
    private boolean collapsed = false;

    /**
     * 
     * @param obj - must have object
     * @param parent - must have parent with member
     * @param width - overrides actual width if gc != null when drawn
     * @param label - object label to draw can be null
     * @param icon - object icon to draw, can be null
     */
    public FacetRectangle(OtmObject obj, MemberSprite<?> parent, double width, String label, Image icon) {
        super( 0, 0, width, 0 );
        this.parent = parent;
        this.icon = icon;
        this.label = label;
        this.otmObject = obj;

        if (obj == null)
            throw new IllegalArgumentException( "Must have object." );
        if (parent == null)
            throw new IllegalArgumentException( "Must have parent sprite." );
        if (!(parent.getMember() instanceof OtmLibraryMember))
            throw new IllegalArgumentException( "Parent must have member." );

        this.settings = parent.getSettingsManager();
        if (settings == null)
            throw new IllegalArgumentException( "Must have settings" );

        this.editable = obj.isEditable();
        if (obj instanceof OtmChildrenOwner)
            this.children = getChildren( (OtmChildrenOwner) obj );

        propertyMargin = settings.getMargin( Margins.PROPERTY );
        propertyOffset = settings.getOffset( Offsets.PROPERTY );

        log.debug( "Created facet rectangle: " + label );
    }

    public FacetRectangle(OtmFacet<?> facet, MemberSprite<?> parentSprite, double width) {
        this( facet, parentSprite, width, facet.getName(), facet.getIcon() );
        this.facet = facet;
        this.collapsed = facet.isCollapsed();
        draw( null ); // Compute the size
    }

    public FacetRectangle(OtmContextualFacet member, MemberSprite<?> parentSprite, double width) {
        this( member.getWhereContributed(), parentSprite, width, member.getName(), member.getIcon() );
        this.facet = member.getWhereContributed();
        draw( null ); // Compute the size
    }

    public FacetRectangle(OtmEnumeration<?> member, MemberSprite<OtmEnumeration<?>> parentSprite, double width) {
        this( member, parentSprite, width, null, null );
        draw( null ); // Compute the size
    }

    public FacetRectangle(OtmValueWithAttributes member, MemberSprite<OtmValueWithAttributes> parentSprite,
        double width) {
        this( member, parentSprite, width, null, null );
        draw( null ); // Compute the size
    }

    public FacetRectangle(OtmAction action, MemberSprite<OtmResource> parentSprite, double width) {
        this( action, parentSprite, width, action.getName(), action.getIcon() );
        this.children = action.getChildren();
        draw( null ); // Compute the size
    }

    private List<OtmObject> getChildren(OtmChildrenOwner owner) {
        List<OtmObject> kids = new ArrayList<>();
        kids.addAll( owner.getInheritedChildren() );
        kids.addAll( owner.getChildren() );
        return kids;
    }

    protected void drawBackground(GraphicsContext gc) {
        if (gc != null) {
            Paint savedColor = gc.getFill();
            // gc.setFill( javafx.scene.paint.Color.WHITE );
            gc.setFill( settings.getColor( this ) );

            // super.drawOutline( gc, false ); // draw outline
            super.drawOutline( gc, true ); // Draw fill

            gc.setFill( savedColor );
            log.debug( "Drew background " + this );
        }
    }

    private double drawControl(GraphicsContext gc) {
        Rectangle r = null;
        Image cIcon = null;

        if (!collapsed)
            cIcon = ImageManager.getImage( ImageManager.Icons.COLLAPSE );
        else
            cIcon = ImageManager.getImage( ImageManager.Icons.EXPAND );

        r = GraphicsUtils.drawImage( cIcon, DrawType.OUTLINE, gc, x, y + propertyMargin );
        if (gc != null && facet != null) {
            parent.add( r );
            r.setOnMouseClicked( e -> parent.collapseOrExpand( facet ) );
        }
        return r != null ? r.getWidth() : 0;
    }

    private Rectangle drawTitleLine(GraphicsContext gc) {
        double titleW = 0;
        double titleH = 0;
        LabelRectangle lRect = null;

        // Control icon
        if (otmObject instanceof OtmFacet)
            titleW += drawControl( gc );

        // Label
        if (label != null) {
            lRect = new LabelRectangle( parent, label, icon, editable, false, false );
            lRect.draw( gc, x + titleW, y );
            titleW += lRect.getWidth();
            titleH = lRect.getHeight();
        }

        // prefix
        if (otmObject instanceof OtmContributedFacet) {
            LabelRectangle pRect =
                new LabelRectangle( parent, otmObject.getPrefix(), null, otmObject.isEditable(), false, false );
            double prefixX = x + width - pRect.getWidth() - settings.getMargin( Margins.LABEL );
            if (gc == null)
                prefixX = x + titleW;
            pRect.draw( gc, prefixX, y );
            titleW += pRect.getWidth();
        }
        return new Rectangle( x, y, titleW, titleH );
    }

    @Override
    public Rectangle draw(GraphicsContext gc) {
        boolean compute = gc == null;
        // Update the collapsed state saved in the facet facade.
        collapsed = otmObject instanceof OtmFacet && ((OtmFacet<?>) otmObject).isCollapsed();

        drawBackground( gc );

        height = 0; // Recompute height
        // Title line - control, name, prefix
        Rectangle r = drawTitleLine( gc );
        if (gc == null && r.getWidth() > width)
            width = r.getWidth();
        height += r.getHeight();

        // Properties
        PropertyRectangle pRect = null;
        double py = y + height;
        double px = x + propertyOffset;
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
                    pRect.set( px, py ).draw( gc );
                    height += pRect.getHeight();
                    py += pRect.getHeight();
                    if (gc == null)
                        width = pRect.getWidth() > width ? pRect.getWidth() : width;
                }
                log.debug( "Drew/sized - compute = " + compute + " " + pRect );
            }
            // // Draw vertical line
            // if (pRect != null && !compute) {
            // px = px + propertyMargin - 1;
            // double ly = y + height - 2 * propertyMargin - 1;
            // gc.strokeLine( px, y + pRect.getHeight(), px, ly );
            // }
        }
        log.debug( "Drew/sized - compute = " + compute + " " + this );
        // super.draw( gc, false ); // debug
        return this;
    }

    @Override
    public String toString() {
        return "Facet: " + facet + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
