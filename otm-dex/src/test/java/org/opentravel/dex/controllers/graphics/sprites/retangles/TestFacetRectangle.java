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

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.AbstractDexTest;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.controllers.graphics.sprites.ContextualFacetSprite;
import org.opentravel.dex.controllers.graphics.sprites.ResourceSprite;
import org.opentravel.dex.controllers.graphics.sprites.SpriteManager;
import org.opentravel.dex.controllers.graphics.sprites.TestSpriteManager;
import org.opentravel.dex.controllers.graphics.sprites.rectangles.FacetRectangle;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCustomFacet;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.model.resource.OtmAction;
import org.opentravel.schemacompiler.model.TLLibrary;

import javafx.scene.canvas.GraphicsContext;

/**
 * Verifies the functions of the <code>SelectProjectDialog</code>
 */
public class TestFacetRectangle extends AbstractDexTest {
    private static Logger log = LogManager.getLogger( TestOtmModelManager.class );

    final String FXID_PROJECTLIST = "#projectList";
    final String FXID_LIBTREETABLE = "#librariesTreeTable";

    SpriteManager spriteMgr = null;

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestFacetRectangle.class );
    }

    @Test
    public void testFacet() {
        OtmLibrary lib = TestLibrary.buildOtm();

        // TestLibrary.addOneOfEach( lib );
        spriteMgr = TestSpriteManager.buildSpriteManager();


    }

    @Test
    public void testAction() {
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null, null );
        OtmLibrary lib = mgr.addLibrary( new TLLibrary() );
        // TestLibrary.addOneOfEach( lib );
        spriteMgr = TestSpriteManager.buildSpriteManager();
        GraphicsContext gc = spriteMgr.getSettingsManager().getGc();

        OtmResource resource = TestResource.buildFullOtm( "http://test.com/p", "TestBO", lib, mgr );
        ResourceSprite resourceS = new ResourceSprite( resource, spriteMgr );

        for (OtmAction action : resource.getActions()) {
            FacetRectangle fr = new FacetRectangle( action, resourceS, 0 );
            fr.draw( gc );
        }

    }

    @Test
    public void testValueWithAttributes() {
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null, null );
        OtmLibrary lib = mgr.addLibrary( new TLLibrary() );
        // TestLibrary.addOneOfEach( lib );
        spriteMgr = TestSpriteManager.buildSpriteManager();


    }

    @Test
    public void testContextualFacet() {
        OtmLibrary lib = TestLibrary.buildOtm();
        spriteMgr = TestSpriteManager.buildSpriteManager();

        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "Bo1" );
        OtmContextualFacet cf = TestCustomFacet.buildOtm( bo, "CF1" );

        ContextualFacetSprite cfSprite = new ContextualFacetSprite( cf, spriteMgr );
        cfSprite.render( spriteMgr.getColumn( 1 ), false ); // 186 x 151
        FacetRectangle fc = new FacetRectangle( cf, cfSprite, 0 ); // 135 x 88

        assertTrue( cfSprite.getWidth() > 0 );
        assertTrue( fc.getWidth() < cfSprite.getWidth() );
        assertTrue( fc.getMaxX() < cfSprite.getX() + cfSprite.getWidth() );
    }

}
