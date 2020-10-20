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
import org.opentravel.dex.controllers.graphics.sprites.DexSprite;
import org.opentravel.dex.controllers.graphics.sprites.GraphicsUtils;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Margins;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Offsets;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmComplexObjects;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObjects;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.model.otmProperties.OtmIdReferenceElement;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.resource.OtmActionRequest;
import org.opentravel.model.resource.OtmActionResponse;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 * Graphics utility for containing property regions.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class PropertyRectangle extends Rectangle {
    private static Log log = LogFactory.getLog( PropertyRectangle.class );

    /**
     * Render methods that create rectangles may set the event to run if the implement this interface.
     * <p>
     * Example: r.setOnMouseClicked( e -> manager.remove( this ) );
     */
    public abstract interface RectangleEventHandler {
        public void onRectangleClick(MouseEvent e);
    }

    protected DexSprite<OtmLibraryMember> parent;
    protected Font font;
    private Point2D connectionPoint = null;

    // Objects this can draw
    private OtmProperty property;

    protected String label = "";
    private Image icon = null;
    private boolean editable = false;

    protected OtmTypeProvider typeProvider = null;
    protected String providerLabel = "";
    private Image providerIcon = null;
    protected Paint providerColor = null;

    double margin = 2;
    double offset = 10; // left margin
    double typeMargin = 8; // distance between property name and type


    public PropertyRectangle(DexSprite<OtmLibraryMember> parent, double width, String label, Image icon,
        boolean editable) {
        super( 0, 0, GraphicsUtils.MINIMUM_WIDTH, 0 );
        this.parent = parent;
        this.label = label;
        this.icon = icon;
        this.editable = editable;
        this.width = width;
        this.font = parent.getFont();

        if (parent.getSettingsManager() != null) {
            margin = parent.getSettingsManager().getMargin( Margins.PROPERTY );
            offset = parent.getSettingsManager().getOffset( Offsets.PROPERTY );
            typeMargin = parent.getSettingsManager().getMargin( Margins.PROPERTY_TYPE );
        }
    }

    public PropertyRectangle(OtmProperty property, DexSprite<OtmLibraryMember> parentSprite, double width) {
        this( parentSprite, width, property.getName(), property.getIcon(), property.isEditable() );

        // Get property information
        this.property = property;
        if (property.isInherited()) {
            this.font = parent.getItalicFont();
            this.label += " (i)";
        }
        if (property instanceof OtmTypeUser) {
            OtmTypeProvider provider = ((OtmTypeUser) property).getAssignedType();
            if (provider == property.getOwningMember() || provider == property.getOwningMember().getBaseType())
                this.typeProvider = null;
            else
                setProvider( provider );
            this.providerColor = property.isAssignedTypeInNamespace() ? null : GraphicsUtils.CONNECTOR_COLOR;
        }

        // Compute the size
        draw( null, font );

        // // Register mouse listener with parent
        // if (parent != null && property instanceof OtmTypeUser) {
        // this.setOnMouseClicked( e -> parent.connect( ((OtmTypeUser) property) ) );
        // parent.add( this );
        // }
    }

    public PropertyRectangle(OtmActionRequest rq, DexSprite<OtmLibraryMember> parentSprite, double width) {
        this( parentSprite, width, rq.getName(), rq.getIcon(), rq.isEditable() );

        if (rq.getMethod() != null)
            this.label = rq.getMethod().toString();
        if (rq.getPayloadActionFacet() != null)
            this.label += "  " + rq.getPayloadActionFacetName();

        // Compute the size
        draw( null, font );
    }

    public PropertyRectangle(OtmActionResponse rs, DexSprite<OtmLibraryMember> parentSprite, double width) {
        this( parentSprite, width, rs.getName(), rs.getIcon(), rs.isEditable() );

        if (rs.getPayloadActionFacet() != null)
            this.label = rs.getPayloadActionFacetName();

        // Compute the size
        draw( null, font );
    }

    public PropertyRectangle(OtmCore core, DexSprite<OtmLibraryMember> parentSprite, double width) {
        this( parentSprite, width, "Simple", null, core.isEditable() );

        // Get type information
        setProvider( core.getAssignedType() );

        // Compute the size
        draw( null, font );
    }

    public PropertyRectangle(OtmValueWithAttributes vwa, DexSprite<OtmLibraryMember> parentSprite, double width) {
        this( parentSprite, width, "Value", null, vwa.isEditable() );

        // Get type information
        setProvider( vwa.getAssignedType() );

        // Compute the size
        draw( null, font );
    }


    protected void setProvider(OtmTypeProvider provider) {
        if (provider != null) {
            this.typeProvider = provider;
            this.providerLabel = provider.getNameWithPrefix();
            if (provider instanceof OtmComplexObjects)
                this.providerLabel = getCardinality( property ) + typeProvider.getPrefix();
            this.providerIcon = typeProvider.getIcon();
            this.providerColor = null;
        }
    }

    /**
     * Draw the facet.
     * 
     * @param gc
     * @param filled
     */
    @Override
    public Rectangle draw(GraphicsContext gc, boolean filled) {
        if (gc != null)
            draw( gc, font );
        return this;
    }

    public Point2D getConnectionPoint() {
        return connectionPoint;
    }

    public OtmProperty getProperty() {
        return property;
    }


    // label, icon, editable
    // TypeProvider, providerLabel, providerIcon
    protected Rectangle draw(GraphicsContext gc, Font font) {
        boolean compute = gc == null;

        double connectorSize = GraphicsUtils.drawConnector( null, null, 0, 0 ).getX();
        double rightMargin = connectorSize + margin;
        double actualWidth = rightMargin + margin; // actual width as computed
        //
        // Draw Property Name and icon
        Rectangle lRect = GraphicsUtils.drawLabel( label, icon, editable, false, gc, font, x, y );
        actualWidth += lRect.getWidth(); // Actual width
        // lRect.draw( gc, false );

        // Draw Type provider if any
        Rectangle tRect;
        if (typeProvider != null) {
            tRect = GraphicsUtils.drawLabel( providerLabel, providerIcon, null, font, x, y );
            actualWidth += tRect.getWidth() + typeMargin;
            double tx = x + width - tRect.getWidth() - rightMargin - margin - margin;
            GraphicsUtils.drawLabel( providerLabel, providerIcon, false, true, gc, font, tx, y );
            // tRect.draw( gc, false );
        }

        // Compute property height and width
        width = compute && actualWidth > width ? actualWidth : width;
        height = lRect.getHeight() + 2 * margin;

        // Draw Underline
        double lineY = y + lRect.getHeight() - margin;
        double lineX = x + width - rightMargin;
        if (property != null && !(property instanceof OtmTypeUser))
            lineX = x + lRect.getWidth();
        if (gc != null)
            gc.strokeLine( x, lineY, lineX, lineY );

        // Draw Connector symbol and register listener
        if (typeProvider != null && !(typeProvider.getOwningMember() instanceof OtmSimpleObjects)) {
            connectionPoint = new Point2D( x + width - margin, lineY );
            GraphicsUtils.drawConnector( gc, providerColor, x + width - 2 * connectorSize + 2 * margin,
                lineY - connectorSize / 2 );

            // Register mouse listener with parent
            if (!compute && parent != null) {
                if (property != null) {
                    this.setOnMouseClicked( e -> parent.connect( ((OtmTypeUser) property) ) );
                    parent.add( this );
                }
                // else if (baseType != null) {
                // this.setOnMouseClicked( e -> parent.connect() );
                // parent.add( this );
                // } else if (resource != null && parent instanceof ResourceSprite) {
                // this.setOnMouseClicked( e -> ((ResourceSprite) parent).connect() );
                // parent.add( this );
                // }
            }
        }
        // super.draw( gc, false ); // debug
        // Log.debug("Drew "+this);
        return this;

    }

    private static String getCardinality(OtmProperty property) {
        String cardinality = "";
        if (property instanceof OtmElement && !(property instanceof OtmIdReferenceElement)) {
            if (property.isManditory())
                cardinality = "[1";
            else
                cardinality = "[0";
            if (((OtmElement<?>) property).getRepeatCount() > 0)
                cardinality += Integer.toString( ((OtmElement<?>) property).getRepeatCount() );

            cardinality += "] ";
        } else if (property instanceof OtmTypeUser && property.isManditory())
            cardinality = "[1] ";
        return cardinality;

    }

    @Override
    public String toString() {
        return "Property: " + label + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
