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
import org.opentravel.dex.controllers.graphics.sprites.MemberSprite;
import org.opentravel.dex.controllers.graphics.sprites.SpriteManager;
import org.opentravel.dex.controllers.graphics.sprites.TestSpriteManager;
import org.opentravel.dex.controllers.graphics.sprites.rectangles.ColumnRectangle;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import javafx.geometry.Point2D;

/**
 * Verifies the functions of the <code>SelectProjectDialog</code>
 */
public class TestColumnRectangle extends AbstractDexTest {
    private static Logger log = LogManager.getLogger( TestOtmModelManager.class );

    final String FXID_PROJECTLIST = "#projectList";
    final String FXID_LIBTREETABLE = "#librariesTreeTable";

    SpriteManager spriteMgr = null;

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestColumnRectangle.class );
    }

    @Test
    public void testGetNextInColumn() {
        // DexFullActionManager fullMgr = new DexFullActionManager( null );
        // OtmModelManager mgr = new OtmModelManager( fullMgr, null, null );
        // OtmLibrary lib = mgr.addOLD( new TLLibrary() );
        OtmLibrary lib = TestLibrary.buildOtm();

        TestLibrary.addOneOfEach( lib );
        spriteMgr = TestSpriteManager.buildSpriteManager();

        // double height = 0;
        int columnIndex = 2;
        ColumnRectangle column = spriteMgr.getColumn( columnIndex );
        double columnX = column.getX();
        assertTrue( "Given: ", columnX > 100 );
        double columnY = column.getY();

        for (OtmLibraryMember member : lib.getMembers()) {
            // Given
            Point2D nextP = column.getNextInColumn();
            assertTrue( nextP.getX() == columnX );
            assertTrue( nextP.getY() >= columnY );

            // When
            MemberSprite<?> sprite = spriteMgr.add( member, column, false );
            if (sprite != null) {
                // Then
                assertTrue( sprite.getX() == nextP.getX() );
                assertTrue( sprite.getY() == nextP.getY() );

                //
                columnY += sprite.getHeight();
            }
        }

        // MemberSprite<?> sprite = spriteMgr.add( member, column, false );
        // // Not all members have sprites
        // if (sprite != null) {
        // assertTrue( sprite.getX() >= columnX );
        // }
    }
}
