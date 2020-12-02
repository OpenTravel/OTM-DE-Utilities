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

package org.opentravel.dex.controllers.graphics.sprites.connections;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.controllers.graphics.sprites.BusinessObjectSprite;
import org.opentravel.dex.controllers.graphics.sprites.CoreObjectSprite;
import org.opentravel.dex.controllers.graphics.sprites.MemberSprite;
import org.opentravel.dex.controllers.graphics.sprites.SpriteManager;
import org.opentravel.dex.controllers.graphics.sprites.TestSpriteManager;
import org.opentravel.dex.controllers.graphics.sprites.rectangles.PropertyRectangle;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmFacets.OtmQueryFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmLibraryMembers.TestQueryFacet;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.model.otmProperties.TestElement;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * Verifies the functions of the <code>SelectProjectDialog</code>
 */
public class TestTypeConnection extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestOtmModelManager.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 5000; // How long to sleep so we can see what is happening. Can be 0.

    final String FXID_PROJECTLIST = "#projectList";
    final String FXID_LIBTREETABLE = "#librariesTreeTable";

    SpriteManager spriteMgr = null;
    GraphicsContext gc = null;

    BusinessObjectSprite userSprite = null;
    CoreObjectSprite providerSprite = null;
    OtmElement<?> propertyBO = null;
    OtmElement<?> propertyCF = null;

    TypeConnection boTC = null;
    TypeConnection cfTC = null;

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestTypeConnection.class );
        repoManager = repositoryManager.get();
    }

    @Test
    public void testConstructor() {
        setup();
    }

    @Test
    public void testMoveProvider() {
        setup();
    }

    @Test
    public void testMoveUser() {
        setup();

        moveFrom( boTC, userSprite, userSprite.get( propertyBO ) );
        moveFrom( cfTC, userSprite, userSprite.get( propertyCF ) );

        // Test with collapsed facets
    }

    private void moveFrom(TypeConnection connection, MemberSprite<?> sprite, PropertyRectangle prop) {
        // Givens
        assertTrue( connection.from == sprite );
        assertTrue( connection.fromProperty == prop.getProperty() );

        // Given - the connection point
        Point2D startPoint = prop.getConnectionPoint();
        double delta = 10;

        // When connection point is moved
        Point2D cp = prop.moveConnectionPoint( delta, delta );
        assertTrue( cp.getX() == startPoint.getX() + delta );
        assertTrue( cp.getY() == startPoint.getY() + delta );

        // When - the connection is updated
        connection.update( sprite, gc, gc.getFill() );

        // Then the connections's fx, fy are changed
        assertTrue( connection.fx == startPoint.getX() + delta );
        assertTrue( connection.fy == startPoint.getY() + delta );


        // TODO - move the whole sprite
        // sprite.clear();
        // sprite.set( x += delta, y += delta );
        // sprite.render(); // Will update connections
        // spriteMgr.updateConnections();
    }

    protected void setup() {
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null, null );
        OtmLibrary lib = mgr.add( new TLLibrary() );
        spriteMgr = TestSpriteManager.buildSpriteManager();
        gc = spriteMgr.getSettingsManager().getGc();

        // Givens
        OtmCore core = TestCore.buildOtm( lib, "Core1" );
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "Bo1" );
        propertyBO = TestElement.buildOtm( bo.getSummary() );
        propertyBO.setAssignedType( core );
        OtmQueryFacet cf = TestQueryFacet.buildOtm( mgr, bo );
        propertyCF = TestElement.buildOtm( cf );
        propertyCF.setAssignedType( core );

        userSprite = new BusinessObjectSprite( bo, spriteMgr );
        providerSprite = new CoreObjectSprite( core, spriteMgr );
        userSprite.render( spriteMgr.getColumn( 1 ), false ); // needed to create property rectangles
        providerSprite.render( spriteMgr.getColumn( 2 ), true );

        PropertyRectangle cfPropertyRect = userSprite.get( propertyCF );
        PropertyRectangle boPropertyRect = userSprite.get( propertyBO );
        assertTrue( boPropertyRect != null );
        assertTrue( cfPropertyRect != null );

        boTC = new TypeConnection( boPropertyRect, userSprite, providerSprite );
        cfTC = new TypeConnection( cfPropertyRect, userSprite, providerSprite );

        assertTrue( boTC.fx == boPropertyRect.getConnectionPoint().getX() );
        assertTrue( cfTC.fx == cfPropertyRect.getConnectionPoint().getX() );
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
