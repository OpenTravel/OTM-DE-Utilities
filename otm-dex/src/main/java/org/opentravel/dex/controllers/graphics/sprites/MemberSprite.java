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
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Margins;
import org.opentravel.dex.controllers.graphics.sprites.connections.SuperTypeConnection;
import org.opentravel.dex.controllers.graphics.sprites.connections.TypeConnection;
import org.opentravel.dex.controllers.graphics.sprites.retangles.BaseTypeRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.FacetRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.LabelRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.PropertyRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle.RectangleEventHandler;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;

/**
 * Graphics Display Object (Sprite) for containing OTM library members.
 * 
 * @author dmh
 * @param <O>
 *
 */
public abstract class MemberSprite<M extends OtmLibraryMember> extends DexSpriteBase
    implements DexSprite, RectangleEventHandler {
    private static Log log = LogFactory.getLog( MemberSprite.class );

    private static final double MIN_HEIGHT = 50;
    private static final double MIN_WIDTH = 50;

    protected M member;

    /**
     * Initialize member sprite. Create canvas and GC parameters. Compute initial size. Create tool tip. Sub-types will
     * initialize settings using the manager's setting manager.
     * 
     * @param member
     * @param settingsManager, must <b>not</b> be null.
     */
    public MemberSprite(M member, SpriteManager manager) {
        super( manager );
        if (member == null)
            throw new IllegalArgumentException( "Missing member to create sprite." );

        this.member = member;
        this.collapsed = !member.isExpanded();

        // Compute size
        draw( null, 0, 0 );

        // Correct tool tip display relies on the canvas being clipped to this sprite's active boundaries
        String desc = member.getDescription();
        String users = !member.getWhereUsed().isEmpty() ? member.getWhereUsed().size() + " Users" : "";
        Tooltip t = new Tooltip();
        Tooltip.install( canvas, t );
        t.setText( member.getPrefix() + " : " + member.getNamespace() + "\n" + desc + "\n" + users );
    }


    /**
     * Get or Create provider sprite if none exists. Draw connection to this sprite.
     * 
     * @param user
     * @return the provider sprite
     */
    public MemberSprite<?> addConnection(OtmTypeUser user) {
        // log.debug( "Adding connection to " + user );
        if (getColumn() == null || user == null || user.getAssignedType() == null || !(user instanceof OtmProperty))
            return null;

        OtmLibraryMember provider = user.getAssignedType().getOwningMember();
        if (provider == null || provider == user.getOwningMember())
            return null;

        MemberSprite<?> toSprite = manager.get( provider );
        if (!(toSprite instanceof MemberSprite)) {
            // Create new collapsed sprite and connect it
            toSprite = manager.add( provider, getColumn().getNext(), true );
            connect( (OtmProperty) user, this, toSprite );
        }
        return toSprite;
    }

    // @Override
    // public void collapseOrExpand() {
    // super.collapseOrExpand();
    // getMember().setExpanded( !isCollapsed() );
    // }

    /**
     * Toggle collapsed state.
     */
    public void collapseOrExpand(OtmFacet<?> f) {
        log.debug( "Collapse or expand." );
        if (f != null) {
            clear();
            f.setCollapsed( !f.isCollapsed() );
            render();
            manager.updateConnections( this );
            log.debug( "Collapse or expand: " + f + " to " + f.isExpanded() );
        }
    }


    /**
     * Utility to compute the wider width. Returns
     * 
     * @param width - current effective with
     * @param rect - rectangle with width that may be larger that current effective width
     * @param offsetX - added to rectangle's width
     * @return the computed effective width
     */
    public static double computeWidth(double width, Rectangle rect, double offsetX) {
        if (rect != null)
            width = rect.getWidth() + offsetX > width ? rect.getWidth() + offsetX : width;
        return width;
    }

    @Override
    public DexSprite connect() {
        if (!(getMember().getBaseType() instanceof OtmLibraryMember))
            return null;

        DexSprite baseSprite = manager.get( (OtmLibraryMember) member.getBaseType() );
        if (baseSprite == null) {
            baseSprite = manager.add( (OtmLibraryMember) member.getBaseType(), getColumn() );
        } else
            baseSprite.setCollapsed( !baseSprite.isCollapsed() );
        if (baseSprite != null) {
            manager.addAndDraw( new SuperTypeConnection( baseSprite, this ) );
            baseSprite.getCanvas().toFront();
            baseSprite.refresh();
        }
        return baseSprite;
    }

    /**
     * Find the property's rectangle and make type connection
     * 
     * @param property
     * @param from
     * @param to
     * @return
     */
    private TypeConnection connect(OtmProperty property, DexSprite from, DexSprite to) {
        // Duplicate type connections?
        TypeConnection c = null;
        PropertyRectangle fRect;
        fRect = from.get( property );
        if (fRect != null) {
            c = new TypeConnection( fRect, from, to );
            manager.addAndDraw( c );
        }
        return c;
    }

    // @Override
    public MemberSprite<?> connect(PropertyRectangle pRect) {
        // log.debug( "Connecting property " + pRect );
        if (pRect == null || pRect.getProperty() == null || pRect.getProvider() == null)
            return null;
        if (getColumn() == null)
            return null;

        OtmProperty user = pRect.getProperty();
        OtmLibraryMember provider = pRect.getProvider().getOwningMember();
        if (provider == null || provider == user.getOwningMember())
            return null;

        MemberSprite<?> toSprite = manager.get( provider );
        if (toSprite == null) {
            // Place the new sprite and connect it
            toSprite = manager.add( provider, getColumn().getNext(), collapsed );
            if (toSprite != null)
                manager.addAndDraw( new TypeConnection( pRect, this, toSprite ) );
        } else {
            toSprite.setCollapsed( !toSprite.isCollapsed() );
        }
        if (toSprite != null) {
            toSprite.getCanvas().toFront();
            toSprite.refresh();
        }
        return toSprite;
    }

    @Override
    public Rectangle draw(GraphicsContext gc, double x, double y) {
        set( x, y );
        return drawMember( gc );
    }

    /**
     * Draw the facets and properties for this member. Start at the passed x, y.
     * <p>
     * This must be implemented by sub-types to draw their object type specific contents.
     * <p>
     * If the object is collapsed, most will return a rectangle(0, 0, 0, 0)
     * <p>
     * Bottom margin is added appropriate to the type of property last drawn.
     * <p>
     * Width includes margin
     * 
     * @param gc if null, compute size. If not-null, draw within boundaries.
     * @param x
     * @param y
     * @return rectangle around all contents
     */
    public abstract Rectangle drawContents(GraphicsContext gc, final double x, final double y);


    /**
     * Draw background, label and controls.
     * 
     * @param gc
     * @return
     */
    protected Rectangle drawMember(GraphicsContext gc) {
        if (member == null)
            return new Rectangle( 0, 0, 0, 0 );

        // Rectangles are disposable.
        rectangles.clear();

        if (boundaries == null)
            boundaries = new Rectangle( x, y, MIN_WIDTH, MIN_HEIGHT );

        // height of member sprite computed on the fly
        double fy = y;

        // Draw background box
        if (gc != null) {
            Paint p = gc.getFill();
            gc.setFill( settingsManager.getColor( this ) );
            Rectangle bRect = new Rectangle( boundaries.getX(), boundaries.getY(),
                boundaries.getWidth() + settingsManager.getMargin( Margins.FACET ),
                boundaries.getHeight() + settingsManager.getMargin( Margins.FACET ) );
            bRect.draw( gc, false ); // Outline
            bRect.draw( gc, true ); // Fill
            gc.setFill( p );
        }
        double px = boundaries.getX();

        // TODO - DRAW property for users if any
        // int users = member.getWhereUsed().size();
        // if (gc != null && !collapsed && users > 0) {
        // // create rectangle to contain the connector
        // // add to rectangles
        // // create mouse handler (thows event?)
        // GraphicsUtils.drawConnector( gc, gc.getFill(), settingsManager.getConnectorSize(), px, y );
        // // LabelRectangle usersR =
        // // new LabelRectangle( this, users + " Users", null, member.isEditable(), false, false );
        // // usersR.draw( gc, boundaries.getX(), y + height );
        // // height += usersR.getHeight();
        // px += settingsManager.getConnectorSize();
        // }

        // Draw the name of the object
        Rectangle mRect =
            new LabelRectangle( this, member.getName(), member.getIcon(), member.isEditable(), false, false ).draw( gc,
                px, fy );
        double width = mRect.getWidth();
        fy += mRect.getHeight();
        // double height = mRect.getHeight();

        // Add the controls
        double cWidth = drawControls( boundaries, gc ) + settingsManager.getMargin( Margins.MEMBER );
        width += cWidth;

        // prefix
        px = boundaries.getMaxX() - cWidth;
        LabelRectangle pRect = new LabelRectangle( this, member.getPrefix(), null, member.isEditable(), false, false );
        px -= pRect.getWidth() + settingsManager.getMargin( Margins.LABEL );
        pRect.draw( gc, px, y );
        width += pRect.getWidth();

        // Draw property for base type if any
        if (!collapsed && member.getBaseType() != null) {
            mRect = new BaseTypeRectangle( this, member, width );
            mRect.set( boundaries.getMaxX() - mRect.getWidth(), fy ).draw( gc, true );
            width = computeWidth( width, mRect, 0 );
            fy += mRect.getHeight();
            // height += mRect.getHeight();
        }

        // Show content (facets, properties, etc)
        mRect = drawContents( gc, boundaries.getX(), fy );
        if (mRect != null) {
            width = computeWidth( width, mRect, settingsManager.getMargin( Margins.FACET ) );
            fy += mRect.getHeight();
        }

        // Handler for canvas layer
        if (manager != null) {
            canvas.setOnMouseDragged( manager::drag );
            canvas.setOnDragDetected( manager::dragStart );
            canvas.setOnMouseReleased( manager::dragEnd );
            // Clicks go to the top most node...so let the pane catch them
            // canvas.setOnMouseClicked( this::mouseClick );
        }
        boundaries = new Rectangle( x, y, width, fy - y );
        // boundaries.draw( gc, false );

        // Clip the canvas to just have the sprite
        double clipX = boundaries.getX() - 4;
        double clipY = boundaries.getY() - 4;
        double clipW = boundaries.getWidth() + 8 + settingsManager.getMargin( Margins.FACET );
        double clipH = boundaries.getHeight() + 8 + settingsManager.getMargin( Margins.FACET );
        canvas.setClip( new javafx.scene.shape.Rectangle( clipX, clipY, clipW, clipH ) );

        if (gc == null)
            log.debug( "ComputeMember: " + this );
        else
            log.debug( "DrawMember: " + this );

        return boundaries;
    }

    @Override
    public PropertyRectangle get(OtmProperty property) {
        for (Rectangle r : rectangles)
            if (r instanceof PropertyRectangle && ((PropertyRectangle) r).getProperty() == property)
                return ((PropertyRectangle) r);
        return null;
    }

    public List<FacetRectangle> getFacetRectangles() {
        List<FacetRectangle> list = new ArrayList<>();
        rectangles.forEach( r -> {
            if (r instanceof FacetRectangle)
                list.add( (FacetRectangle) r );
        } );
        return list;
    }

    public List<PropertyRectangle> getPropertyRectangles() {
        List<PropertyRectangle> list = new ArrayList<>();
        rectangles.forEach( r -> {
            if (r instanceof PropertyRectangle)
                list.add( (PropertyRectangle) r );
        } );
        return list;
    }

    public M getMember() {
        return member;
    }



    @Override
    public void onRectangleClick(MouseEvent e) {
        // log.debug( "Rectangle click at: " + e.getX() + " " + e.getY() );
    }

    @Override
    public Canvas render() {
        log.debug( "Rendering at " + x + " " + y + " sprite for: " + member );
        if (member == null || manager == null)
            return null;

        if (boundaries == null)
            draw( null, x, y );

        // Size Canvas
        Rectangle canvasR = new Rectangle( x, y, boundaries.getWidth() + settingsManager.getMargin( Margins.CANVAS ),
            boundaries.getHeight() + settingsManager.getMargin( Margins.CANVAS ) );
        canvas.setHeight( y + canvasR.getHeight() );
        canvas.setWidth( x + canvasR.getWidth() );
        // log.debug( "Sized canvas: " + canvasR );

        drawMember( gc );
        manager.updateConnections( this );
        log.debug( "Rendered " + member + " at " + getBoundaries() );
        return canvas;
    }


    public String toString() {
        return "Sprite for " + getMember() + " at " + getBoundaries();
    }
}
