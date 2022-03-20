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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.AbstractDexTest;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.controllers.graphics.sprites.rectangles.ColumnRectangle;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmComplexObjects;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.schemacompiler.model.TLLibrary;

import javafx.geometry.Point2D;

/**
 * Verifies the functions of the <code>SelectProjectDialog</code>
 */
public class TestDexSpriteBase extends AbstractDexTest {
    private static Logger log = LogManager.getLogger( TestOtmModelManager.class );

    final String FXID_PROJECTLIST = "#projectList";
    final String FXID_LIBTREETABLE = "#librariesTreeTable";

    SpriteManager spriteMgr = null;

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestDexSpriteBase.class );
    }

    @Test
    public void testRender() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmModelManager mgr = lib.getModelManager();

        TestLibrary.addOneOfEach( lib );
        spriteMgr = TestSpriteManager.buildSpriteManager();

        double height = 0;
        int columnIndex = 2;
        ColumnRectangle column = spriteMgr.getColumn( columnIndex );
        double columnX = column.getX();
        assertTrue( "Given: ", columnX > 100 );

        for (OtmLibraryMember member : lib.getMembers()) {
            assertTrue( column.getNextInColumn().getX() == columnX );
            MemberSprite<?> sprite = spriteMgr.add( member, column );
            // Not all members have sprites
            if (sprite != null) {
                assertTrue( sprite.getX() == columnX );
            }
        }
    }

    /**
     * Test the impact of rendering into a column on the columns.
     * <p>
     * Test impact of collapse/expand on columns
     * <p>
     * Assure the width of the column is adjusted as well as the next column.
     */
    @Test
    public void testRenderColumnLayout() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmModelManager mgr = lib.getModelManager();

        TestLibrary.addOneOfEach( lib );
        spriteMgr = TestSpriteManager.buildSpriteManager();

        ColumnRectangle column1 = spriteMgr.getColumn( 1 );
        ColumnRectangle column2 = spriteMgr.getColumn( 2 );
        ColumnRectangle column3 = spriteMgr.getColumn( 3 );
        double column1X = column1.getX();
        double column1W = column1.getWidth();

        for (OtmLibraryMember member : lib.getMembers()) {
            assertTrue( column2.getNextInColumn().getX() == column2.getX() );
            MemberSprite<?> sprite = spriteMgr.add( member, column2, true );
            // Not all members have sprites
            if (sprite != null) {
                assertTrue( sprite.getX() == column2.getX() );
                assertTrue( column3.getX() > column2.getX() + sprite.getWidth() );
                assertTrue( column1.getX() == column1X );
                assertTrue( column1.getWidth() == column1W );
            }
        }
        spriteMgr.clear();

        for (OtmLibraryMember member : lib.getMembers()) {
            assertTrue( column2.getNextInColumn().getX() == column2.getX() );
            MemberSprite<?> sprite = spriteMgr.add( member, column2, false );
            // Not all members have sprites
            if (sprite != null) {
                assertTrue( sprite.getX() == column2.getX() );
                assertTrue( "Column 3 must be right of column 2.",
                    column3.getX() > column2.getX() + sprite.getWidth() );
                assertTrue( column1.getX() == column1X );
                assertTrue( column1.getWidth() == column1W );
            }
        }
    }

    @Test
    public void testSetCollapse_True() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmModelManager mgr = lib.getModelManager();

        spriteMgr = TestSpriteManager.buildSpriteManager();

        OtmCore otm1 = TestCore.buildOtm( lib );
        otm1.setName( "C1" + otm1.getName() );
        ColumnRectangle column = spriteMgr.getColumn( 2 );
        Point2D nextP = column.getNextInColumn();
        double spriteX = nextP.getX();
        double spriteY = nextP.getY();

        MemberSprite<?> sprite = spriteMgr.factory( otm1 );
        sprite.render( column, false );

        // Given
        assertTrue( sprite.getX() == spriteX );
        assertTrue( sprite.getY() == spriteY );

        // When
        sprite.setCollapsed( true );
        // Then
        assertTrue( sprite.getX() == spriteX );
        assertTrue( sprite.getY() == spriteY );
    }

    @Test
    public void testSetCollapse_Width() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmModelManager mgr = lib.getModelManager();

        spriteMgr = TestSpriteManager.buildSpriteManager();

        OtmCore otm1 = TestCore.buildOtm( lib );
        otm1.setName( "C1" + otm1.getName() );
        otm1.setExpanded( false );
        ColumnRectangle column = spriteMgr.getColumn( 2 );
        double spriteW = 0;
        double spriteH = 0;

        MemberSprite<?> sprite = spriteMgr.factory( otm1 );
        sprite.render( column, true );

        // Given - collapsed
        spriteW = sprite.getWidth();
        spriteH = sprite.getHeight();

        // When
        sprite.setCollapsed( false );
        // Then
        assertTrue( sprite.getWidth() > spriteW );
        assertTrue( sprite.getHeight() > spriteH );

        // When
        sprite.setCollapsed( true );
        // Then
        assertTrue( sprite.getWidth() == spriteW );
        assertTrue( sprite.getHeight() == spriteH );
    }

    @Test
    public void testSetCollapse_False() {
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmModelManager mgr = lib.getModelManager();

        spriteMgr = TestSpriteManager.buildSpriteManager();

        OtmCore otm1 = TestCore.buildOtm( lib );
        otm1.setName( "C1" + otm1.getName() );
        otm1.setExpanded( false );
        ColumnRectangle column = spriteMgr.getColumn( 2 );
        Point2D nextP = column.getNextInColumn();
        double spriteX = nextP.getX();
        double spriteY = nextP.getY();

        MemberSprite<?> sprite = spriteMgr.factory( otm1 );
        sprite.render( column, true );

        // Given
        assertTrue( sprite.isCollapsed() != otm1.isExpanded() );
        assertTrue( sprite.getX() == spriteX );
        assertTrue( sprite.getY() == spriteY );

        // When
        sprite.setCollapsed( false );
        // Then
        assertTrue( sprite.getX() == spriteX );
        assertTrue( sprite.getY() == spriteY );
    }

    @Test
    public void testSetCollapse() {
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null, null );
        OtmLibrary lib = mgr.addLibrary( new TLLibrary() );
        TestLibrary.addOneOfEach( lib );

        spriteMgr = TestSpriteManager.buildSpriteManager();

        double height = 0;
        int columnIndex = 0;
        ColumnRectangle column = spriteMgr.getColumn( 2 );
        double spriteX = column.getX();
        double spriteY = column.getY();

        for (OtmLibraryMember member : lib.getMembers()) {
            member.setExpanded( true );
            MemberSprite<?> sprite = spriteMgr.add( member, column );
            // Not all members have sprites
            if (sprite != null) {
                // Given
                assertTrue( sprite.getBoundaries() != null );
                assertTrue( sprite.getBoundaries().getWidth() > 50 );
                assertTrue( sprite.getColumn() != null );
                assertTrue( sprite.isCollapsed() != member.isExpanded() );

                spriteX = sprite.getX();
                spriteY = sprite.getY();
                height = sprite.getBoundaries().getHeight();
                columnIndex = sprite.getColumn().getIndex();

                // When
                sprite.setCollapsed( true );
                // Then
                if (member instanceof OtmComplexObjects)
                    assertTrue( sprite.getBoundaries().getHeight() < height );
                assertTrue( sprite.getColumn().getIndex() == columnIndex );
                assertTrue( sprite.isCollapsed() );
                assertTrue( sprite.getX() == spriteX );
                assertTrue( sprite.getY() == spriteY );

                // When
                sprite.setCollapsed( false );
                // Then
                assertTrue( sprite.getBoundaries().getHeight() == height );
                assertTrue( sprite.getColumn().getIndex() == columnIndex );
                assertTrue( !sprite.isCollapsed() );
                assertTrue( sprite.getX() == spriteX );
                assertTrue( sprite.getY() == spriteY );
            } else
                log.debug( "No sprite for " + member + " of class " + member.getClass().getSimpleName() );
        }
    }
}
