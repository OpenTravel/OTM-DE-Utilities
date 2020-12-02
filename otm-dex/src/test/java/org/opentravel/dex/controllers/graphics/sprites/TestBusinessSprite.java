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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.controllers.graphics.sprites.rectangles.Rectangle;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmProperties.OtmAttribute;
import org.opentravel.model.otmProperties.TestOtmPropertiesBase;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import javafx.scene.canvas.GraphicsContext;

/**
 * Verifies the functions of the <code>SelectProjectDialog</code>
 */
public class TestBusinessSprite extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestOtmModelManager.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 5000; // How long to sleep so we can see what is happening. Can be 0.

    final String FXID_PROJECTLIST = "#projectList";
    final String FXID_LIBTREETABLE = "#librariesTreeTable";

    SpriteManager spriteMgr = null;
    GraphicsContext gc = null;
    BusinessObjectSprite boc = null;
    BusinessObjectSprite boe = null;


    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestBusinessSprite.class );
        repoManager = repositoryManager.get();
    }

    @Test
    public void testConstructor() {
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null, null );
        OtmLibrary lib = mgr.add( new TLLibrary() );
        spriteMgr = TestSpriteManager.buildSpriteManager();
        gc = spriteMgr.getSettingsManager().getGc();

        OtmBusinessObject bo1 = TestBusiness.buildOtm( lib, "Bo1" );
        OtmAttribute<?> attr1 = TestOtmPropertiesBase.buildAttribute( bo1.getIdFacet() );
        attr1.setName( "thisisareallyreallyreallylongnametoassureexpandedwidthiswiderthancollapsed" );
        bo1.setExpanded( false );
        assertFalse( bo1.isExpanded() );
        OtmBusinessObject bo2 = TestBusiness.buildOtm( lib, "Bo2" );
        OtmAttribute<?> attr2 = TestOtmPropertiesBase.buildAttribute( bo2.getIdFacet() );
        attr2.setName( "thisisareallyreallyreallylongnametoassureexpandedwidthiswiderthancollapsed" );
        bo2.setExpanded( true );

        // 107 x 28
        boc = new BusinessObjectSprite( bo1, spriteMgr );
        assertTrue( "Must have had size set.", boc.getBoundaries() != null );
        assertTrue( "Must have width.", boc.getWidth() > 0 );
        assertTrue( "Must have height.", boc.getHeight() > 0 );
        assertTrue( boc.isCollapsed() );

        // 567 x 322
        boe = new BusinessObjectSprite( bo2, spriteMgr );
        assertTrue( "Must have had size set.", boe.getBoundaries() != null );
        assertTrue( "Must have width.", boe.getWidth() > boc.getWidth() );
        assertTrue( "Must have height.", boe.getHeight() > boc.getHeight() );
        assertTrue( !boe.isCollapsed() );
    }

    @Test
    public void testDraw() {
        // Set up the two sprites
        testConstructor();

        // Draw Contents
        Rectangle cRect = boc.drawContents( null, 0, 0 ); // 0 x 0
        assertTrue( "Must be 0 when collapsed.", cRect.getWidth() == 0 );
        assertTrue( "Must be 0 when collapsed.", cRect.getHeight() == 0 );
        Rectangle eRect = boe.drawContents( null, 0, 0 ); // 572 x 314

        // Draw Member
        Rectangle cmRect = boc.drawMember( null ); // 107 x 28
        Rectangle emRect = boe.drawMember( null ); // 577 x 342

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
        Rectangle cRect = boc.drawContents( gc, 0, 0 ); // 157 x 0
        Rectangle eRect = boe.drawContents( gc, 0, 0 ); // 567 x 63

        // Draw Member
        Rectangle cmRect = boc.drawMember( gc ); // 162 x 28
        Rectangle emRect = boe.drawMember( gc ); // 572 x 121

        // Then - the contents must be smaller
        assertTrue( cRect.getWidth() < cmRect.getWidth() );
        assertTrue( eRect.getWidth() < emRect.getWidth() );
        assertTrue( cRect.getHeight() < cmRect.getHeight() );
        assertTrue( eRect.getHeight() < emRect.getHeight() );

        // Make the sprites wider than they need.
        boc.getBoundaries().set( 10, 10, 600 );
        boe.getBoundaries().set( 10, 10, 600 );
        cRect = boc.drawContents( gc, 0, 0 ); // 0 x 0
        eRect = boe.drawContents( gc, 0, 0 ); // 605 x 63

        // Draw Member
        cmRect = boc.drawMember( gc ); // 107 x 28
        emRect = boe.drawMember( gc ); // 610 x 121

        // Must conform to width set when gc was null
        // assertTrue( cRect.getWidth() >= 600 ); // collapsed is 0
        assertTrue( eRect.getWidth() >= 600 );
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
