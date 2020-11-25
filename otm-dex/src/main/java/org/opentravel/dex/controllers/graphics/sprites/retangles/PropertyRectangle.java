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

import org.opentravel.dex.controllers.graphics.sprites.GraphicsUtils;
import org.opentravel.dex.controllers.graphics.sprites.MemberSprite;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Margins;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Offsets;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObjects;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.model.otmProperties.OtmIdReferenceElement;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.resource.OtmActionRequest;
import org.opentravel.model.resource.OtmActionResponse;

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
    // private static Log log = LogFactory.getLog( PropertyRectangle.class );

    /**
     * Render methods that create rectangles may set the event to run if the implement this interface.
     * <p>
     * Example: r.setOnMouseClicked( e -> manager.remove( this ) );
     */
    public abstract interface RectangleEventHandler {
        public void onRectangleClick(MouseEvent e);
    }

    protected MemberSprite<?> parent;
    protected Font font;
    protected SettingsManager settings;

    private OtmProperty property;
    protected OtmTypeProvider typeProvider = null;

    protected String label = "";
    private Image icon = null;
    private boolean editable = false;
    private boolean inherited = false;

    protected String providerLabel = "";
    private Image providerIcon = null;
    protected Paint providerColor = null;

    double margin = 2;
    double offset = 10; // left margin
    double typeMargin = 8; // distance between property name and type
    double connectorSize = SettingsManager.CONNECTOR_SIZE;


    public PropertyRectangle(MemberSprite<?> parent, double width, String label, Image icon, boolean editable,
        boolean inherited) {
        super( 0, 0, GraphicsUtils.MINIMUM_WIDTH, 0 );
        this.parent = parent;
        this.label = label;
        this.icon = icon;
        this.editable = editable;
        this.inherited = inherited;
        this.width = width;
        this.font = parent.getFont();
        if (inherited) {
            this.font = parent.getItalicFont();
            this.label += " (i)";
        }

        settings = parent.getSettingsManager();
        if (settings != null) {
            margin = settings.getMargin( Margins.PROPERTY );
            offset = settings.getOffset( Offsets.PROPERTY );
            typeMargin = settings.getMargin( Margins.PROPERTY_TYPE );
            connectorSize = settings.getConnectorSize();
        }
    }

    public PropertyRectangle(OtmProperty property, MemberSprite<?> parentSprite, double width) {
        this( parentSprite, width, property.getName(), property.getIcon(), property.isEditable(),
            property.isInherited() );

        // Get property information
        this.property = property;
        if (property instanceof OtmTypeUser) {
            OtmTypeProvider provider = ((OtmTypeUser) property).getAssignedType();
            if (provider == property.getOwningMember() || provider == property.getOwningMember().getBaseType())
                this.typeProvider = null;
            else
                setProvider( provider );
            this.providerColor = property.isAssignedTypeInNamespace() ? null : GraphicsUtils.CONNECTOR_COLOR;
        }

        // Compute the size
        draw( null );
    }

    public PropertyRectangle(OtmActionRequest rq, MemberSprite<?> parentSprite, double width) {
        this( parentSprite, width, rq.getName(), rq.getIcon(), rq.isEditable(), rq.isInherited() );

        if (rq.getMethod() != null)
            this.label = rq.getMethod().toString();
        if (rq.getPayloadActionFacet() != null)
            this.label += "  " + rq.getPayloadActionFacetName();

        // Compute the size
        draw( null );
    }

    public PropertyRectangle(OtmActionResponse rs, MemberSprite<?> parentSprite, double width) {
        this( parentSprite, width, rs.getName(), rs.getIcon(), rs.isEditable(), rs.isInherited() );

        if (rs.getPayloadActionFacet() != null)
            this.label = rs.getPayloadActionFacetName();

        // Compute the size
        draw( null );
    }


    protected void setProvider(OtmTypeProvider provider) {
        if (provider != null) {
            this.typeProvider = provider;
            this.providerLabel = provider.getNameWithPrefix();
            if (showConnector( provider ))
                this.providerLabel = getCardinality( property ) + typeProvider.getPrefix();
            this.providerIcon = typeProvider.getIcon();

            // TODO - get color from settingsManager
            this.providerColor = null;
        }
    }

    // /**
    // * Draw the rectangle.
    // *
    // * @param gc
    // * @param filled
    // */
    // @Deprecated
    // @Override
    // public Rectangle draw(GraphicsContext gc, boolean filled) {
    // if (gc != null)
    // draw( gc );
    // return this;
    // }

    /**
     * @return the property field
     */
    public OtmProperty getProperty() {
        return property;
    }

    public OtmTypeProvider getProvider() {
        return typeProvider;
    }


    // // label, icon, editable
    // // TypeProvider, providerLabel, providerIcon
    // protected Rectangle draw(GraphicsContext gc, Font font) {
    // return draw( gc );
    // }

    @Override
    public Rectangle draw(GraphicsContext gc) {
        boolean compute = gc == null;

        connectorSize = GraphicsUtils.drawConnector( null, null, connectorSize, 0, 0 ).getX();
        double rightMargin = connectorSize + margin;
        double actualWidth = rightMargin + margin; // actual width as computed
        //
        // Draw Property Name and icon
        LabelRectangle lRect = new LabelRectangle( parent, label, icon, editable, inherited, false ).draw( gc, x, y );
        actualWidth += lRect.getWidth(); // Actual width
        // lRect.draw( gc, false );

        // Draw Type provider if any
        LabelRectangle tRect;
        if (typeProvider != null) {
            tRect = new LabelRectangle( parent, providerLabel, providerIcon, false, inherited, true );
            actualWidth += tRect.getWidth() + typeMargin;
            double tx = x + width - tRect.getWidth() - rightMargin - 3 * margin;
            tRect.draw( gc, tx, y );
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
        if (showConnector( typeProvider )) {
            double cx = x + width - connectorSize - 6 * margin;
            connectionPoint =
                GraphicsUtils.drawConnector( gc, providerColor, connectorSize, cx, lineY - connectorSize );
            // log.debug( " Set connection point to: " + connectionPoint );

            // Register mouse listener with parent
            // parent.connect won't run until clicked, parent needs rectangle to dispatch click.
            if (!compute && parent != null) {
                if (property != null)
                    this.setOnMouseClicked( e -> parent.connect( this ) );
                else
                    this.setOnMouseClicked( e -> parent.connect( (OtmLibraryMember) typeProvider ) );
                parent.add( this );
                // log.debug( "Added mouse listener to " + property );
            }
        }
        // super.draw( gc, false ); // debug
        // Log.debug("Drew "+this);
        return this;

    }

    private static boolean showConnector(OtmTypeProvider provider) {
        return (provider != null && !(provider.getOwningMember() instanceof OtmSimpleObjects));
    }

    private static String getCardinality(OtmProperty property) {
        String cardinality = "";
        if (property instanceof OtmElement && !(property instanceof OtmIdReferenceElement)) {
            if (property.isManditory())
                cardinality = "[1";
            else
                cardinality = "[0";
            if (((OtmElement<?>) property).getRepeatCount() > 0)
                cardinality += ".." + Integer.toString( ((OtmElement<?>) property).getRepeatCount() );

            cardinality += "] ";
        } else if (property instanceof OtmTypeUser)
            if (property.isManditory())
                cardinality = "[1] ";
            else
                cardinality = "[0] ";
        return cardinality;

    }

    @Override
    public String toString() {
        return "Property: " + label + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
