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
import org.opentravel.dex.controllers.graphics.sprites.rectangles.PropertyRectangle;
import org.opentravel.dex.controllers.graphics.sprites.rectangles.Rectangle;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmQueryFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmLibraryMembers.TestQueryFacet;
import org.opentravel.model.otmProperties.OtmAttribute;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.model.otmProperties.TestElement;
import org.opentravel.model.otmProperties.TestOtmPropertiesBase;
import org.opentravel.schemacompiler.model.TLProperty;

import java.util.List;

/**
 * Verifies the functions of the <code>SelectProjectDialog</code>
 */
public class TestMemberSprite extends AbstractDexTest {
    private static Logger log = LogManager.getLogger( TestOtmModelManager.class );

    final String FXID_PROJECTLIST = "#projectList";
    final String FXID_LIBTREETABLE = "#librariesTreeTable";

    SpriteManager spriteMgr = null;

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestMemberSprite.class );
    }

    @Test
    public void testConstructor() {
        OtmLibrary lib = TestLibrary.buildOtm();

        TestLibrary.addOneOfEach( lib );
        spriteMgr = TestSpriteManager.buildSpriteManager();

        for (OtmLibraryMember member : lib.getMembers()) {
            MemberSprite<?> sprite = spriteMgr.factory( member );
            // Not all members have sprites
            if (sprite != null) {
                assertTrue( sprite.getBoundaries() != null );
                assertTrue( sprite.getBoundaries().getWidth() > 50 );
            } else
                log.debug( "No sprite for " + member + " of class " + member.getClass().getSimpleName() );
        }
    }

    @Test
    public void testGetProperty() {
        // Set up
        OtmLibrary lib = TestLibrary.buildOtm();
        spriteMgr = TestSpriteManager.buildSpriteManager();

        // Givens
        OtmCore core = TestCore.buildOtm( lib, "Core1" );
        OtmBusinessObject bo = TestBusiness.buildOtm( lib, "Bo1" );
        OtmElement<?> propertyBO = TestElement.buildOtm( bo.getSummary() );
        propertyBO.setAssignedType( core );
        OtmQueryFacet cf = TestQueryFacet.buildOtm( bo, "QF1" );
        OtmElement<?> propertyCF = TestElement.buildOtm( cf );
        propertyCF.setAssignedType( core );

        BusinessObjectSprite boSprite = new BusinessObjectSprite( bo, spriteMgr );
        CoreObjectSprite coreSprite = new CoreObjectSprite( core, spriteMgr );
        assertTrue( boSprite.getPropertyRectangles().size() == 0 );

        boSprite.render( spriteMgr.getColumn( 1 ), false ); // needed to create property rectangles
        coreSprite.render( spriteMgr.getColumn( 2 ), true );

        // Assure that the correct property is retrieved when core is assigned to two or more
        List<PropertyRectangle> pRectList = boSprite.getPropertyRectangles();
        assertTrue( boSprite.getPropertyRectangles().size() >= 2 );

        for (PropertyRectangle pr : pRectList) {
            PropertyRectangle result = boSprite.get( pr.getProperty() );
            assertTrue( result == pr );
        }
    }

    @Test
    public void testDraw() {
        OtmLibrary lib = TestLibrary.buildOtm();
        spriteMgr = TestSpriteManager.buildSpriteManager();

        OtmCore otm1 = TestCore.buildOtm( lib );
        otm1.setName( "C1" + otm1.getName() );
        otm1.setExpanded( false );
        OtmAttribute<?> attr = TestOtmPropertiesBase.buildAttribute( otm1.getSummary() );
        attr.setName( "ThisIsAVERYlongnametomakesureitsetswidthofattribute" );
        OtmElement<TLProperty> ele = TestOtmPropertiesBase.buildElement( otm1.getSummary() );
        ele.setName( "ThisIsAVERYlongnametomakesureitsetswidth" );

        MemberSprite<OtmCore> csCollapsed = new CoreObjectSprite( otm1, spriteMgr );
        csCollapsed.draw( null, 10, 10 ); // 168 x 28
        // Draw simple type and facets if not collapsed
        Rectangle cr = csCollapsed.drawContents( null, 10, 10 );
        assertTrue( cr.getWidth() <= csCollapsed.getWidth() );

        otm1.setExpanded( true );
        MemberSprite<OtmCore> csExpanded = new CoreObjectSprite( otm1, spriteMgr );
        csExpanded.draw( null, 10, 10 ); // 433 x 216
        assertTrue( csExpanded.getWidth() > csCollapsed.getWidth() );
        assertTrue( csExpanded.getHeight() > csCollapsed.getHeight() );
    }
}
