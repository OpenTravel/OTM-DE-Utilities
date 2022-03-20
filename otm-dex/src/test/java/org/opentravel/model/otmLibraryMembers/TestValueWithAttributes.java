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

package org.opentravel.model.otmLibraryMembers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.actions.BaseTypeChangeAction;
import org.opentravel.dex.actions.SetAssignedTypeAction;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmProperties.OtmAttribute;
import org.opentravel.model.otmProperties.TestOtmTypeProviderInterface;
import org.opentravel.model.otmProperties.TestOtmTypeUserInterface;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestValueWithAttributes extends TestOtmLibraryMemberBase<OtmValueWithAttributes> {
    // private static Logger log = LogManager.getLogger( TestValueWithAttributes.class );

    static String NAME = "TestVWA";

    @BeforeClass
    public static void beforeClass() {
        // staticModelManager = new OtmModelManager( null, null, null );
        staticLib = TestLibrary.buildOtm();
        staticModelManager = staticLib.getModelManager();
        subject = buildOtm( staticLib, "SubjectVWA" );
        baseObject = buildOtm( staticLib, "BaseVWA" );
        // baseObject.setName( "BaseVWA" );
    }

    @Before
    public void beforeMethods() {
        staticModelManager.clear();

        staticLib = TestLibrary.buildOtm( staticModelManager );
        subject = buildOtm( staticLib, "SubjectVWA" );
        baseObject = buildOtm( staticLib, "BaseVWA" );
    }


    /** *********************** VWA Tester ******************** **/
    public static void check(OtmValueWithAttributes vwa) {
        log.debug( "VWA Tester: testing " + vwa.getNameWithPrefix() );
        assertTrue( "VWA Tester: ", vwa != null );
        assertTrue( "VWA Tester: ", vwa.getLibrary() != null );
        assertTrue( "VWA Tester: ", vwa.getModelManager() != null );
        // assertTrue( "VWA Tester: ", vwa.getAssignedType() != null );
        // assertTrue( "VWA Tester: ", vwa.getBaseType() != null );
        // log.debug( "VWA Tester: assigned type = " + vwa.getAssignedType() );
        // log.debug( "VWA Tester: base type = " + vwa.getBaseType() );
        // log.debug( "VWA Tester: parent type = " + vwa.getTL().getParentType() );

        // TL Tests
        TLValueWithAttributes tlVwa = vwa.getTL();
        assertTrue( "VWA Tester: ", tlVwa instanceof TLValueWithAttributes );
        assertTrue( "VWA Tester: ", tlVwa.getOwningLibrary() instanceof AbstractLibrary );
        assertTrue( "VWA Tester: ", tlVwa.getOwningModel() != null );
        // assertTrue( "VWA Tester: ", tlVwa.getParentType() != null );
        assertTrue( "VWA Tester: ", tlVwa.getAttributes() != null );
        assertTrue( "VWA Tester: ", tlVwa.getIndicators() != null );
    }

    /** *************** VWA Builders ********************* **/

    private static OtmValueWithAttributes buildOtm(OtmModelManager mgr) {
        OtmTypeProvider simple = TestXsdSimple.buildOtm( mgr );
        OtmValueWithAttributes vwa = new OtmValueWithAttributes( buildTL( (TLAttributeType) simple.getTL() ), mgr );
        assertNotNull( vwa );
        mgr.add( vwa );

        OtmTypeProvider p = vwa.getAssignedType();
        return vwa;
    }


    /**
     * @param lib
     * @param name
     * @return
     */
    public static OtmValueWithAttributes buildOtm(OtmLibrary lib, String name) {
        assertTrue( lib.isEditable() );
        name = OtmLibraryMemberFactory.getUniqueName( lib, name );
        assertTrue( "Builder - must have unique name.", lib.getTL().getNamedMember( name ) == null );

        OtmValueWithAttributes vwa = buildOtm( lib.getModelManager() );
        vwa.setName( OtmLibraryMemberFactory.getUniqueName( lib, name ) );
        lib.add( vwa );

        assertTrue( "Builder - new TLVWA must be in TL library.", lib.getTL() == vwa.getTL().getOwningLibrary() );
        assertTrue( "Builder - new TLVWA must be in TL Model.",
            lib.getModelManager().getTlModel() == vwa.getTL().getOwningModel() );

        assertTrue( "Builder - new VWA must be managed in library.", lib.contains( vwa ) );
        assertTrue( "Builder - new VWA must be managed in model manager.",
            lib.getModelManager().getMembers().contains( vwa ) );

        return vwa;
    }


    /**
     * Create a TL Value With Attributes with 4 attribute children.
     * 
     * @param type if not null, set as parent type
     * @return
     */
    public static TLValueWithAttributes buildTL(TLAttributeType type) {
        TLValueWithAttributes tlvwa = new TLValueWithAttributes();
        tlvwa.setName( NAME );
        tlvwa.setParentType( type );

        // add attributes
        int i = 1;
        while (i < 5) {
            TLAttribute tla = new TLAttribute();
            tla.setName( NAME + i );
            tla.setType( type );
            tlvwa.addAttribute( tla );
            i++;
        }

        assertNotNull( tlvwa.getParentType() );
        assertTrue( tlvwa.getAttributes().size() == i - 1 );
        return tlvwa;
    }

    /** ***************** VWA extensions to standard tests ********************** **/

    @Override
    public void testChildrenOwner(OtmChildrenOwner otm) {
        super.testChildrenOwner( otm );

        OtmChildrenOwner co = (OtmChildrenOwner) otm;
        List<OtmObject> kids = co.getChildren();
        assertTrue( !kids.isEmpty() );

    }

    @Override
    public void testInheritance(OtmValueWithAttributes otm) {

        OtmValueWithAttributes base = (OtmValueWithAttributes) otm.getBaseType();
        List<OtmObject> baseKids = ((OtmChildrenOwner) base).getChildren();
        assertTrue( "Given: base VWA must have children.", baseKids.size() >= 4 );

        List<OtmObject> otmInherited = otm.getInheritedChildren();

        // If inherited children are missing, check how the inherited children are modeled.
        if (otmInherited.size() != baseKids.size()) {
            baseKids.forEach( k -> assertTrue( "Only attributes, no indicators.", k instanceof OtmAttribute ) );
            // TODO - make more robust
            // tlList.addAll( PropertyCodegenUtils.getInheritedIndicators( otm.getTL() ));
            // Check - Inherited children are found using property code gen utils.
            List<TLAttribute> tlList = PropertyCodegenUtils.getInheritedAttributes( otm.getTL() );
            assertTrue( "Then - inherited tl list must be same size as base list.", tlList.size() == baseKids.size() );
        }
        super.testInheritance( otm );
    }

    /** *************** VWA Specific Tests **************** **/

    /**
     * Something about VWA caused error when running in {@link TestOtmTypeUserInterface#test_setAssignedType()}
     * 
     * @throws InterruptedException
     */
    @Test
    public void test_CircularAssignments() {
        OtmLibrary lib = TestLibrary.buildOtm();

        OtmValueWithAttributes vwaBase = TestValueWithAttributes.buildOtm( lib, "TypeUserVWABase" );
        OtmValueWithAttributes vwa = TestValueWithAttributes.buildOtm( lib, "TypeUserVWA2" );
        List<OtmTypeUser> users = new ArrayList<>();
        users.add( vwa );

        List<OtmTypeProvider> providers = TestOtmTypeProviderInterface.buildOneOfEach( lib );
        List<OtmValueWithAttributes> vwaProviders = new ArrayList<>();
        providers.forEach( p -> {
            if (p instanceof OtmValueWithAttributes)
                vwaProviders.add( (OtmValueWithAttributes) p );
        } );

        // Note: one has base type (tlParent) and one does not.
        for (OtmTypeProvider p : vwaProviders) {
            log.debug( "Testing assigment of " + p.getNameWithPrefix() + " to " + vwa.getNameWithPrefix() );

            check( (OtmValueWithAttributes) p );
            check( vwa );

            vwa.setAssignedType( p );
            // goes into never-never land on getAssignedTLType() in setAssignedTLType()
        }
    }

    @Test
    public void testDescendentsTypeUsers() {
        OtmValueWithAttributes vwa = buildOtm( staticLib, "TestDescendentsVwa" );
        Collection<OtmTypeUser> d = vwa.getDescendantsTypeUsers();
        assertTrue( !d.isEmpty() );
    }

    @Test
    public void testBaseType() {
        OtmValueWithAttributes vwa = buildOtm( staticLib, "TestVwa" );
        OtmValueWithAttributes baseVwa = buildOtm( staticLib, "BaseVwa" );

        OtmValueWithAttributes base = vwa.getBaseType();
        vwa.setBaseType( baseVwa );
        base = vwa.getBaseType();
        assertTrue( base == baseVwa );
    }

    @Test
    public void TestVWAasBaseAndValueType() {
        // Determine and assure Value and Base type are correct.
        // OtmModelManager mgr = TestOtmModelManager.build();
        // OtmLibrary lib = TestLibrary.buildOtm( staticModelManager, "http://example.com", "ex", "lib1" );
        OtmLibrary lib = TestLibrary.buildOtm();
        OtmValueWithAttributes vwa1 = buildOtm( lib, "TestVWA1" );
        OtmValueWithAttributes vwa2 = buildOtm( lib, "TestVWA2" );
        OtmValueWithAttributes vwa3 = buildOtm( lib, "TestVWA3" );
        OtmValueWithAttributes vwa4 = buildOtm( lib, "TestVWA4" );

        assertTrue( "Given", vwa1.isEditable() );
        assertTrue( "Given", BaseTypeChangeAction.isEnabled( vwa1 ) == true );
        assertTrue( "Given", SetAssignedTypeAction.isEnabled( vwa1 ) == true );

        vwa1.setAssignedType( staticModelManager.getXsdMember( "decimal" ) );
        vwa2.setAssignedType( staticModelManager.getXsdMember( "date" ) );
        vwa3.setAssignedType( staticModelManager.getXsdMember( "float" ) );
        vwa4.setAssignedType( staticModelManager.getXsdMember( "time" ) );
        assertTrue( vwa1.getAssignedType() == staticModelManager.getXsdMember( "decimal" ) );
        assertTrue( vwa2.getAssignedType() == staticModelManager.getXsdMember( "date" ) );
        assertTrue( vwa3.getAssignedType() == staticModelManager.getXsdMember( "float" ) );
        assertTrue( vwa4.getAssignedType() == staticModelManager.getXsdMember( "time" ) );

        OtmTypeProvider base = vwa1.getBaseType();
        OtmTypeProvider value = vwa1.getAssignedType();
        assertTrue( "Given", base == null );
        // When
        vwa1.setBaseType( vwa2 );
        // Then
        base = vwa1.getBaseType();
        value = vwa1.getAssignedType();
        assertTrue( base == vwa2 );
        assertTrue( value == vwa2.getAssignedType() );

        base = vwa3.getBaseType();
        assertTrue( "Given", base == null );
        value = vwa3.getAssignedType();

        // When - assigned
        vwa3.setAssignedType( vwa4 );
        // Then
        base = vwa3.getBaseType();
        value = vwa3.getAssignedType();
        assertTrue( base == vwa4 ); // Not sure why
        assertTrue( value == vwa4.getAssignedType() );
    }



}
