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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmListFacet;
import org.opentravel.model.otmProperties.TestOtmPropertiesBase;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * test class the otm library member base class.
 */
public class TestLibraryMemberBase {

    private static Log log = LogFactory.getLog( TestLibraryMemberBase.class );

    protected static OtmModelManager staticModelManager = null;
    protected static OtmLibraryMember subject;
    protected static OtmLibraryMember baseObject;

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        log.debug( "Model manager created." );
    }


    @Test
    public void testAddAndRemove() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException,
        NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        OtmModelManager mgr = new OtmModelManager( null, null );
        OtmLibrary lib = mgr.add( new TLLibrary() );

        for (OtmLibraryMemberType type : OtmLibraryMemberType.values()) {
            OtmLibraryMember member = OtmLibraryMemberType.buildMember( type, "Test" + type.toString(), mgr );

            // When added
            lib.add( member );
            mgr.add( member );
            // Then - add works
            // assertTrue(lib.contains( member ));
            assertTrue( mgr.contains( member.getTlLM() ) );

            // When removed -- NO ACTION
            lib.remove( member ); // No-op
            mgr.remove( member ); // Removes from map not TL
            assertFalse( mgr.contains( member.getTlLM() ) );
            assertFalse( mgr.getMembers().contains( member ) );

            log.debug( "Added and removed: " + member );
        }
    }

    @Test
    public void testEnumFactory() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException,
        NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        for (OtmLibraryMemberType lmType : OtmLibraryMemberType.values()) {
            OtmLibraryMember member = OtmLibraryMemberType.buildMember( lmType, "Hi", staticModelManager );
            assertNotNull( member );
        }
    }

    @Test
    public void testGetLabel() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException,
        NoSuchMethodException, InvocationTargetException {
        for (OtmLibraryMemberType lmType : OtmLibraryMemberType.values()) {
            OtmLibraryMember member = OtmLibraryMemberType.buildMember( lmType, "Hi", staticModelManager );
            assertNotNull( member );

            String label = OtmLibraryMemberType.getLabel( member );
            assertTrue( !label.isEmpty() );
            log.debug( "Label for " + member.getClass().getSimpleName() + " is " + label );
        }
    }

    @Test
    public void getPropertiesWhereUsed() {
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null );
        OtmLibrary lib = TestLibrary.buildOtm( mgr );
        // buildOneOfEachWithProperties( mgr, lib );
        assertTrue( "Given", lib.isEditable() );

        // Create a core and assign to elements in a BO
        OtmCore core = TestCore.buildOtm( mgr, "TestCore" );
        OtmBusinessObject bo = TestBusiness.buildOtm( mgr, "TestBo" );
        lib.add( core );
        lib.add( bo );

        for (OtmTypeUser user : bo.getDescendantsTypeUsers())
            user.setAssignedType( core );
        assertTrue( "Given", core.getWhereUsed().contains( bo ) );

        Map<OtmTypeUser,OtmTypeProvider> properties = core.getPropertiesWhereUsed();
        assertTrue( "Must have type user properties.", !properties.isEmpty() );
    }

    @Test
    public void testOneOfEachWithProperties() {
        // Givens
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, null );
        OtmLibrary lib = TestLibrary.buildOtm( mgr );
        assertTrue( "Given", lib.isEditable() );

        buildOneOfEachWithProperties( mgr, lib );

        for (OtmLibraryMember lm : mgr.getMembers()) {
            if (lm instanceof OtmResource)
                continue;
            if (lm instanceof OtmServiceObject)
                continue;
            if (lm instanceof OtmValueWithAttributes)
                continue; // Should the facets be children?
            if (lm instanceof OtmContextualFacet)
                continue;
            if (lm instanceof OtmSimpleObjects)
                continue;

            assertTrue( "Must have children", !lm.getChildren().isEmpty() );
            for (OtmObject child : lm.getChildren()) {
                if (child instanceof OtmListFacet)
                    continue;
                if (child instanceof OtmPropertyOwner)
                    assertTrue( "Property owers must have children.",
                        !((OtmPropertyOwner) child).getChildren().isEmpty() );
            }
        }
    }

    /**
     * Create a new member of the type of subject, set as the base type, then test.
     * 
     * @param subject
     * @return the new member set as base type
     * @throws ExceptionInInitializerError
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public static OtmLibraryMember testBaseType(OtmLibraryMember subject) throws ExceptionInInitializerError,
        InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // Get another of the same type
        OtmLibraryMember base = OtmLibraryMemberType.buildMember( OtmLibraryMemberType.get( subject ),
            "Base" + subject.getName(), subject.getModelManager() );
        // When - extension set
        if (subject.getLibrary() != null)
            subject.getLibrary().add( base );
        OtmObject result = subject.setBaseType( base );
        // Then
        if (subject.getTL() instanceof TLExtensionOwner) {
            assertTrue( "Then", ((TLExtensionOwner) subject.getTL()).getExtension() != null );
            assertTrue( "Then",
                ((TLExtensionOwner) subject.getTL()).getExtension().getExtendsEntity() == base.getTL() );
            assertTrue( "Then - must have returned base type.", result == base );
            assertTrue( "Then - all extension owners must be extended", subject.getBaseType() == base );
            log.debug( "Created base type for " + subject.getClass().getSimpleName() );
        }
        return base;
    }


    // Confirm map
    public static void confirmMap(Map<OtmLibraryMember,OtmLibraryMember> baseObjects) {
        for (Entry<OtmLibraryMember,OtmLibraryMember> entry : baseObjects.entrySet()) {
            assertTrue( entry.getValue().getBaseType() == entry.getKey() );
            assertTrue( entry.getKey().getWhereUsed().contains( entry.getValue() ) );
        }
    }


    public static Map<OtmLibraryMember,OtmLibraryMember> buildBaseObjectsForAll(OtmModelManager mgr)
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException, NoSuchMethodException,
        InvocationTargetException {
        Map<OtmLibraryMember,OtmLibraryMember> baseObjects = new HashMap<>();
        Collection<OtmLibraryMember> members = new ArrayList<>( mgr.getMembers() );
        for (OtmLibraryMember member : members) {
            OtmLibraryMember base = testBaseType( member );
            if (member instanceof OtmSimpleObject)
                continue; // FIXME
            if (member instanceof OtmValueWithAttributes)
                continue; // FIXME
            // Not all members can be extended. Contextual facets use base type for where contribted.
            if (base != null && !(base instanceof OtmServiceObject) && !(base instanceof OtmContextualFacet)) {
                List<OtmLibraryMember> bwu = base.getWhereUsed();
                assertTrue( bwu.contains( member ) );
                baseObjects.put( base, member );
            }
            // else
            // log.debug( "Could not create base for " + member.getClass().getSimpleName() + " " + member );
        }
        confirmMap( baseObjects );
        return baseObjects;
    }


    /**
     * Create one of each library member and give each property owner one of each property.
     * 
     * @param mgr
     * @param lib
     */
    public static void buildOneOfEachWithProperties(OtmModelManager mgr, OtmLibrary lib) {
        // Givens
        assertTrue( "Given", lib.isEditable() );
        assertTrue( "Given", mgr != null );

        // Build one of each library member type
        TestLibrary.addOneOfEach( lib );
        // assertTrue( "Given", lib.isValid() );
        assertTrue( "Given", !mgr.getMembers().isEmpty() );

        // Given all property owners have children
        for (OtmLibraryMember lm : mgr.getMembers()) {
            if (lm instanceof OtmPropertyOwner)
                TestOtmPropertiesBase.buildOneOfEach2( (OtmPropertyOwner) lm );

            for (OtmObject child : lm.getChildren())
                if (child instanceof OtmPropertyOwner)
                    TestOtmPropertiesBase.buildOneOfEach2( (OtmPropertyOwner) child );
        }
        // assertTrue( "Given", lib.isValid() );
    }


    /**
     * TODO
     * 
     * @param member
     */
    public static void check(OtmLibraryMember member) {}

    /**
     * Check to assure all properties are owned by this member.
     * 
     * @param member
     */
    public static void checkOwnership(OtmLibraryMember member) {
        for (OtmObject d : member.getDescendants()) {
            assertTrue( d.getOwningMember() == member );
            assertTrue( d.getLibrary() == member.getLibrary() );
        }
    }

}
