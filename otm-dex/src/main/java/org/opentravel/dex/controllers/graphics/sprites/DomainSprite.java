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
import org.opentravel.dex.controllers.graphics.sprites.retangles.ColumnRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.LabelRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.LibraryRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import java.util.HashMap;
import java.util.Map;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

/**
 * Graphics Display Object (Sprite) for containing OTM namespaces (domains) and their libraries
 * 
 * @author dmh
 * @param <O>
 *
 */
public class DomainSprite extends DexSpriteBase {
    private static Log log = LogFactory.getLog( DomainSprite.class );

    private String baseNamespace = "";
    private Map<OtmLibrary,LibraryRectangle> libMap = new HashMap<>();

    /**
     * Initialize member sprite. Create canvas and GC parameters. Compute initial size. Create tool tip. Sub-types will
     * initialize settings using the manager's setting manager.
     * 
     * @param member
     * @param settingsManager, must <b>not</b> be null.
     */
    public DomainSprite(SpriteManager manager) {
        super( manager );

        // Correct tool tip display relies on the canvas being clipped to this sprite's active boundaries
        String desc = "Domain defined by base namespace and its libraries.";
        Tooltip t = new Tooltip();
        Tooltip.install( canvas, t );
        t.setText( desc );
    }


    public DomainSprite add(MemberSprite<?> sprite) {
        if (baseNamespace.isEmpty())
            baseNamespace = sprite.getMember().getLibrary().getBaseNamespace();

        if (LibraryRectangle.contains( baseNamespace, sprite )) {
            LibraryRectangle libR = new LibraryRectangle( sprite );
            baseNamespace = libR.getBaseNamespace();
            libMap.put( libR.getLibrary(), libR );
        }
        return this;
    }

    @Override
    public DexSprite connect() {
        return null;
    }

    @Override
    public DexSprite connect(OtmLibraryMember member) {
        return null;
    }



    @Override
    public Rectangle draw(GraphicsContext gc, double x, double y) {
        set( x, y );

        if (boundaries == null)
            boundaries = new Rectangle( x, y, MIN_WIDTH, MIN_HEIGHT );

        // Draw background box
        if (gc != null) {
            Rectangle bRect = new Rectangle( boundaries.getX(), boundaries.getY(),
                boundaries.getWidth() + settingsManager.getMargin( Margins.FACET ),
                boundaries.getHeight() + settingsManager.getMargin( Margins.FACET ) );
            bRect.draw( gc, false ); // Outline
            bRect.draw( gc, true ); // Fill
        }

        // Draw the name of the object
        LabelRectangle lr = new LabelRectangle( this, baseNamespace, null, false, false, false );
        lr.set( x, y ).draw( gc, true );
        if (gc == null)
            boundaries.setIfLarger( lr );

        // Add the controls
        double cWidth = drawControls( boundaries, gc ) + settingsManager.getMargin( Margins.MEMBER );
        if (gc == null)
            boundaries.addWidth( cWidth );

        return boundaries;
    }



    // public void todo(GraphicsContext gc) {
    // MouseEvent event = null;
    // ArrayList<Circle> listOfCircles = new ArrayList<>();
    // for (Circle circle : listOfCircles) {
    // Point2D point2D = new Point2D( event.getX(), event.getY() );
    // if (circle.contains( point2D )) {
    // // log.debug( "circle clicked" );
    // }
    // }
    // }

    @Override
    public void onRectangleClick(MouseEvent e) {
        log.debug( "Rectangle click at: " + e.getX() + " " + e.getY() );
    }

    @Override
    public Canvas render() {
        log.debug( "Rendering at " + x + " " + y + " sprite for: " + baseNamespace );
        // if (member == null || manager == null)
        // return null;
        //
        if (boundaries == null)
            draw( null, x, y );
        //
        // Size Canvas
        Rectangle canvasR = new Rectangle( x, y, boundaries.getWidth() + settingsManager.getMargin( Margins.CANVAS ),
            boundaries.getHeight() + settingsManager.getMargin( Margins.CANVAS ) );
        canvas.setHeight( y + canvasR.getHeight() );
        canvas.setWidth( x + canvasR.getWidth() );
        log.debug( "Sized canvas: " + canvasR );
        //
        draw( gc, x, y );
        // manager.updateConnections( this );
        // log.debug( "Rendered " + member + " at " + getBoundaries() );
        return canvas;
    }

    @Override
    public Canvas render(ColumnRectangle column, boolean collapsed) {
        this.column = column;
        Point2D p = column.getNextInColumn();
        this.x = p.getX();
        this.y = p.getY();
        boundaries = null;
        return render();
    }

    @Override
    public String toString() {
        return "Sprite for base namespace " + baseNamespace + " at " + getBoundaries();
    }
}
