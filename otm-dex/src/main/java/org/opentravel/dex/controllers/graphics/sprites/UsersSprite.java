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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.dex.controllers.graphics.sprites.rectangles.LabelRectangle;
import org.opentravel.dex.controllers.graphics.sprites.rectangles.Rectangle;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmDomain;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;

/**
 * Graphics Display Object (Sprite) for users of a domain ( OTM base namespace ).
 * <p>
 * Providers are listed by domain, library and member.
 * 
 * <p>
 * Versions ??? ??How to flag when multiple versions of a library are being used?
 * 
 * @author dmh
 * @param <O>
 *
 */
public class UsersSprite extends DexSpriteBase {
    private static Logger log = LogManager.getLogger( UsersSprite.class );

    // private String baseNamespace = "";
    // private Map<OtmLibrary,LibraryRectangle> libMap = new HashMap<>();

    // private List<String> subDomains = null;
    private DomainSprite domainS = null;
    private OtmDomain otmDomain = null;
    private OtmModelManager modelManager = null;

    // private DomainUsersCR usersCR = null;
    // private DomainProvidersCR providersCR = null;
    // private List<String> providerDomains = null;
    //
    // private LibraryFacetRectangle libraryFacetRectangle;
    // private boolean librariesCollapsed = false;
    //
    // private SubDomainFR subDomainRectangle;
    // private boolean subDomainsCollapsed = false;



    /**
     * Initialize member sprite. Create canvas and GC parameters. Compute initial size. Create tool tip. Sub-types will
     * initialize settings using the manager's setting manager.
     * 
     * @param member
     * @param settingsManager, must <b>not</b> be null.
     */
    public UsersSprite(SpriteManager manager, DomainSprite domainSprite) {
        super( manager ); // Creates canvas
        this.domainS = domainSprite;
        if (domainS == null)
            throw new IllegalArgumentException( "Must have domain sprite." );

        this.otmDomain = domainS.getOtmDomain();
        if (otmDomain == null)
            throw new IllegalArgumentException( "Must have OTM domain." );

        // this.baseNamespace = domain.getBaseNamespace();
        // this.modelManager = manager.getModelManager();
        //
        // if (baseNamespace == null || baseNamespace.isEmpty())
        // throw new IllegalArgumentException( "Domain must have a base namespace." );
        // if (modelManager == null)
        // throw new IllegalArgumentException( "Domain must have access to model manager." );

        // buildLibraries();
        // buildSubDomains();

        // Correct tool tip display relies on the canvas being clipped to this sprite's active boundaries
        String desc = "Users of Domain " + otmDomain.getName();
        Tooltip t = new Tooltip();
        Tooltip.install( canvas, t );
        t.setText( desc );

        // Compute initial size
        draw( null, 0, 0 );
    }

    // private void buildLibraries() {
    // Collection<OtmLibrary> libs = otmDomain.getLibraries();
    // libs.forEach( lib -> libMap.put( lib, new LibraryRectangle( this, lib ) ) );
    // }

    // private void buildSubDomains() {
    // subDomains = modelManager.getSubDomains( baseNamespace );
    // }

    @Override
    public void clear() {
        super.clear();
        // libMap.clear();
        // // subDomains.clear();
        // subDomainRectangle = null;
        // libraryFacetRectangle = null;
        // buildLibraries();
        // // buildSubDomains();
    }

    // public DexSprite connect(ClickableRectangle clickableRectangle, String subDomain) {
    // log.debug( "Connect " + subDomain + " to " + clickableRectangle );
    // ProvidersSprite subDomainS = null;
    // if (subDomain != null && !subDomain.isEmpty()) {
    // subDomainS = manager.add( subDomain, getColumn() );
    // subDomainS.getCanvas().toFront();
    // // TODO - create connection?
    // }
    // return subDomainS;
    // }

    // public DexSprite connect(DomainUsersCR usersR, ProvidersSprite ds) {
    // log.debug( "Connect users for " + ds );
    // // DomainSprite subDomainS = null;
    // // if (subDomain != null && !subDomain.isEmpty()) {
    // // subDomainS = manager.add( subDomain, getColumn() );
    // // subDomainS.getCanvas().toFront();
    // // // TODO - create connection?
    // // }
    // return ds;
    // }
    //
    // public DexSprite connect(DomainProvidersCR providersR, ProvidersSprite ds) {
    // log.debug( "Connect providers for " + ds );
    // // DomainSprite subDomainS = null;
    // // if (subDomain != null && !subDomain.isEmpty()) {
    // // subDomainS = manager.add( subDomain, getColumn() );
    // // subDomainS.getCanvas().toFront();
    // // // TODO - create connection?
    // // }
    // return ds;
    // }

    // public void collapseOrExpand(CollapsableRectangle rec) {
    // clear();
    // if (rec instanceof LibraryFacetRectangle)
    // librariesCollapsed = rec.isCollapsed();
    // else if (rec instanceof SubDomainFR)
    // subDomainsCollapsed = rec.isCollapsed();
    // render();
    // log.debug( "Collapse or expand set to " + rec.isCollapsed() + " for " + rec );
    // }
    //
    @Override
    public Rectangle draw(GraphicsContext gc, double x, double y) {
        set( x, y );

        Paint color = null;
        if (gc != null)
            color = gc.getFill();

        double fy = y + drawSprite( gc, color, null, false );
        // double width = boundaries.getWidth();
        // double margin = settingsManager.getMargin( Margins.FACET );

        LabelRectangle lr = null;
        if (!collapsed) {
            // // The namespace
            // lr = new LabelRectangle( this, baseNamespace, ImageManager.getImage( Icons.URL ), false, false, false );
            // lr.draw( gc, x, fy );
            // fy += lr.getHeight();
            // width = computeWidth( width, lr, margin );
            // double fx = x + margin;
            // double offset = settingsManager.getOffset( Offsets.PROPERTY );
            //
            // // Up to parent
            // // TODO
            //
            // // Providers sprite
            // if (providersCR == null)
            // providersCR = new DomainProvidersCR( this, width );
            // providersCR.draw( gc, fx + offset, fy );
            // fy += providersCR.getHeight();
            //
            // // Users sprite
            // if (usersCR == null)
            // usersCR = new DomainUsersCR( this, width );
            // usersCR.draw( gc, fx + offset, fy );
            // fy += usersCR.getHeight();

            // fy += margin;
            //
            // if (subDomainRectangle == null)
            // subDomainRectangle = new SubDomainFR( this, width, subDomainsCollapsed );
            // subDomainRectangle.draw( gc, fx, fy );
            // fy += subDomainRectangle.getHeight() + margin;
            // width = computeWidth( width, subDomainRectangle, margin );
            //
            // // Directly owned libraries
            // if (libraryFacetRectangle == null)
            // libraryFacetRectangle = new LibraryFacetRectangle( this, width, librariesCollapsed );
            // libraryFacetRectangle.draw( gc, fx, fy );
            // fy += libraryFacetRectangle.getHeight();
            // width = computeWidth( width, libraryFacetRectangle, margin );
            //
            // if (gc == null) {
            // boundaries.setIfWider( width + margin );
            // boundaries.setIfHigher( fy - y );
            // }
        }
        // boundaries.draw( gc, false );
        return boundaries;

    }

    // /**
    // *
    // * @return live list of libraries in the map
    // */
    // public Collection<LibraryRectangle> getLibraries() {
    // return libMap.values();
    // }
    //
    // public Collection<String> getSubDomainNames() {
    // return otmDomain.getSubDomainNames();
    // }
    //
    @Override
    public String getName() {
        return "Users of " + otmDomain.getName();
    }
    //
    // public static String getDomainName(String baseNamespace) {
    // String name = "Domain";
    // if (baseNamespace != null) {
    // int lastSlash = baseNamespace.lastIndexOf( '/' );
    // if (lastSlash > 0)
    // name = baseNamespace.substring( lastSlash + 1 );
    // }
    // return name;
    // }

    // public String getDomain() {
    // return baseNamespace;
    // }

    @Override
    public Image getIcon() {
        return ImageManager.getImage( Icons.DOMAIN );
    }


    @Override
    public void onRectangleClick(MouseEvent e) {
        log.debug( "Rectangle click at: " + e.getX() + " " + e.getY() );
    }

    public DomainSprite getDomain() {
        return domainS;
    }

    //
    // public List<String> getProviderDomains() {
    // return otmDomain.getProviderDomains();
    // }
    //
    // public int getProviderCount() {
    // return otmDomain.getProviderDomains().size();
    // }
    //
    // public List<String> getUserDomains() {
    // return otmDomain.getUserDomains();
    // }
    //
    // public int getUserCount() {
    // return getUserDomains().size();
    // }

    @Override
    public String toString() {
        return "Sprite for users of " + otmDomain + " at " + getBoundaries();
    }
}
