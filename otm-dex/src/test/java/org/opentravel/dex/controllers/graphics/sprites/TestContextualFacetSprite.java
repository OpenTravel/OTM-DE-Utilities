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

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.dex.controllers.graphics.sprites.rectangles.Rectangle;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCustomFacet;
import org.opentravel.model.otmProperties.OtmAttribute;
import org.opentravel.model.otmProperties.TestOtmPropertiesBase;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import javafx.scene.canvas.GraphicsContext;

/**
 * Verifies the functions of the <code>SelectProjectDialog</code>
 */
public class TestContextualFacetSprite extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestOtmModelManager.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 5000; // How long to sleep so we can see what is happening. Can be 0.

    final String FXID_PROJECTLIST = "#projectList";
    final String FXID_LIBTREETABLE = "#librariesTreeTable";

    SpriteManager spriteMgr = null;
    GraphicsContext gc = null;
    ContextualFacetSprite csC = null;
    ContextualFacetSprite csE = null;


    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestContextualFacetSprite.class );
        repoManager = repositoryManager.get();
    }

    @Test
    public void testConstructor() {
        // DexFullActionManager fullMgr = new DexFullActionManager( null );
        // OtmModelManager mgr = new OtmModelManager( fullMgr, null, null );
        OtmModelManager mgr = TestOtmModelManager.build();
        OtmLibrary lib = TestLibrary.buildOtm( mgr );
        // OtmLibrary lib = mgr.add( new TLLibrary() );
        spriteMgr = TestSpriteManager.buildSpriteManager();
        gc = spriteMgr.getSettingsManager().getGc();

        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "Bo1" );
        OtmContextualFacet cfC = TestCustomFacet.buildOtm( mgr, bo );
        OtmContextualFacet cfE = TestCustomFacet.buildOtm( mgr, bo );
        OtmAttribute<?> attr = TestOtmPropertiesBase.buildAttribute( cfE );
        assertTrue( attr != null );
        attr.setName( "thisisareallyreallyreallylongnametoassureexpandedwidthiswiderthancollapsed" );

        cfC.setExpanded( false );
        cfE.setExpanded( true );

        // 157 x 28
        csC = new ContextualFacetSprite( cfC, spriteMgr );
        assertTrue( "Must have had size set.", csC.getBoundaries() != null );
        assertTrue( "Must have width.", csC.getWidth() > 0 );
        assertTrue( "Must have height.", csC.getHeight() > 0 );

        // 567 x 116
        csE = new ContextualFacetSprite( cfE, spriteMgr );
        assertTrue( "Must have had size set.", csE.getBoundaries() != null );
        assertTrue( "Must have width.", csE.getWidth() > csC.getWidth() );
        assertTrue( "Must have height.", csE.getHeight() > csC.getHeight() );
    }

    @Test
    public void testDraw() {
        // Set up the two sprites
        testConstructor();

        // Draw Contents
        Rectangle cRect = csC.drawContents( null, 0, 0 ); // 157 x 0
        Rectangle eRect = csE.drawContents( null, 0, 0 ); // 567 x 63

        // Draw Member
        Rectangle cmRect = csC.drawMember( null ); // 162 x 28
        Rectangle emRect = csE.drawMember( null ); // 572 x 121

        // Then - the contents must be smaller
        assertTrue( cRect.getWidth() < cmRect.getWidth() );
        assertTrue( eRect.getWidth() < emRect.getWidth() );
        assertTrue( cRect.getHeight() < cmRect.getHeight() );
        assertTrue( eRect.getHeight() < emRect.getHeight() );
    }

    @Test
    public void testDrawWithGC() {
        // Set up the two sprites
        testConstructor();
        // Draw Contents
        Rectangle cRect = csC.drawContents( gc, 0, 0 ); // 157 x 0
        Rectangle eRect = csE.drawContents( gc, 0, 0 ); // 567 x 63

        // Draw Member
        Rectangle cmRect = csC.drawMember( gc ); // 162 x 28
        Rectangle emRect = csE.drawMember( gc ); // 572 x 121

        // Then - the contents must be smaller
        assertTrue( cRect.getWidth() < cmRect.getWidth() );
        assertTrue( eRect.getWidth() < emRect.getWidth() );
        assertTrue( cRect.getHeight() < cmRect.getHeight() );
        assertTrue( eRect.getHeight() < emRect.getHeight() );

        // Make the sprites wider than they need.
        csC.getBoundaries().set( 10, 10, 600 );

        // Draw contents
        cRect = csC.drawContents( gc, 0, 0 ); // 0 x 0
        eRect = csE.drawContents( gc, 0, 0 ); // 582 x 63

        // Draw Member
        cmRect = csC.drawMember( gc ); // 157 x 28
        emRect = csE.drawMember( gc ); // 577 x 121

        // Must conform to width less some margins set when gc was null
        assertTrue( eRect.getWidth() >= 500 );
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getApplicationClass()
     */
    @Override
    protected Class<? extends AbstractOTMApplication> getApplicationClass() {
        return ObjectEditorApp.class;
    }

    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getBackgroundTaskNodeQuery()
     */
    @Override
    protected String getBackgroundTaskNodeQuery() {
        return "#libraryText";
    }
}
