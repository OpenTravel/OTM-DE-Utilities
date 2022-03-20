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

package org.opentravel.model.otmContainers;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.opentravel.AbstractDexTest;
import org.opentravel.common.ValidationUtils;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberType;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestChoice;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmLibraryMembers.TestEnumerationClosed;
import org.opentravel.model.otmLibraryMembers.TestEnumerationOpen;
import org.opentravel.model.otmLibraryMembers.TestValueWithAttributes;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.ValidationFinding;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Static utilities for building and testing libraries.
 * 
 */
// FIXME - do not pass in prefix, or check and warn if namespace is already known but different prefix.
@Ignore
public class TestLibraryUtils extends AbstractDexTest {
    private static Logger log = LogManager.getLogger( TestLibraryUtils.class );

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestLibrary.class );
    }

    public void testLibraryUtils() {
        OtmLocalLibrary lib = buildOtm();
        OtmLocalLibrary lib2 = buildOtm( getModelManager() );
        OtmLocalLibrary lib3 = buildOtm( getModelManager(), "NS", "PRE", "Name" );

        TLLibrary tlLib1 = buildTL();
        TLLibrary tlLib2 = buildTL( "NS", "PRE", "Name" );

        addOneOfEach( lib );
        addOneOfEachValid( lib2 );

        check( lib );
        check( lib2 );
        check( lib3 );
        checkContentsAreEditable( lib3 );
    }

    /** *************************** Static Builders *************************** **/

    /**
     * Create new ModelManager and Full Action Manager. Then {@link #buildOtm(OtmModelManager)}
     * 
     * @param mgr
     * @return
     */
    public static OtmLocalLibrary buildOtm() {
        return buildOtm( new OtmModelManager( new DexFullActionManager( null ), null, null ) );
    }

    /**
     * Run {@link #buildOtm(OtmModelManager, String, String, String)} named "LibraryName" in example.com namespace with
     * prefix "pre".
     * 
     * @param mgr
     * @return
     */
    public static OtmLocalLibrary buildOtm(OtmModelManager mgr) {
        return buildOtm( mgr, "http://example.com/ns/v0", "pre", "LibraryName" );
    }

    /**
     * Build a tl and otm library. Add to manager. Check library.
     * 
     * @param mgr
     * @param namespace
     * @param prefix
     * @param name
     * @return
     */
    public static OtmLocalLibrary buildOtm(OtmModelManager mgr, String namespace, String prefix, String name) {
        TLLibrary tlLib = buildTL();
        tlLib.setOwningModel( mgr.getTlModel() );
        // If the model has a library with the Ns:name, change the name.
        int i = 1;
        while (mgr.getTlModel().getLibrary( namespace, name ) != null)
            name = name + i++;
        tlLib.setName( name );
        tlLib.setPrefix( prefix );
        tlLib.setNamespace( namespace );
        tlLib.setLibraryUrl( URLUtils.toURL( "file://exampleLib.otm/" + name ) );

        // OtmLibrary lib = mgr.add( tlLib );
        OtmLibrary lib = mgr.addLibrary( tlLib );

        // Post-checks
        assertTrue( "Builder", lib instanceof OtmLocalLibrary );
        assertTrue( "Builder", lib.isEditable() );
        assertTrue( "Builder - model manager must be able to find the library.", mgr.get( lib.getTL() ) == lib );
        assertTrue( "Builder", lib.getStatus() == TLLibraryStatus.DRAFT );
        assertTrue( "Builder: library must be owned by model.", tlLib.getOwningModel() != null );
        assertTrue( "Builder",
            lib.getState() == RepositoryItemState.MANAGED_WIP || lib.getState() == RepositoryItemState.UNMANAGED );
        assertTrue( "Builder: Must have URL.", tlLib.getLibraryUrl() != null );
        assertTrue( "Builder: Must have URL.", !tlLib.getLibraryUrl().toString().isEmpty() );

        if (lib.getBaseNS().equals( namespace ))
            log.warn( "Library namespace is not a valid OTA2 versioned namespace: " + namespace );
        checkLibrary( lib );

        return (OtmLocalLibrary) lib;
    }

    public static TLLibrary buildTL(String namespace, String prefix, String name) {
        TLLibrary tlLib = new TLLibrary();
        tlLib.setName( name );
        tlLib.setPrefix( prefix );
        tlLib.setNamespace( namespace );
        tlLib.setLibraryUrl( URLUtils.toURL( "File://example.otm" ) );
        return tlLib;
    }

    /**
     * Simply create a new TLLibrary.
     * 
     * @return
     */
    public static TLLibrary buildTL() {
        return new TLLibrary();
    }

    /**
     * Check model manager, prefix and namespace, tlLibrary.
     * 
     * @param library
     */
    public static void checkLibrary(OtmLibrary library) {
        assertTrue( "Check library: ", library != null );
        assertTrue( "Check library: ", library.getTL() instanceof TLLibrary );

        assertTrue( "Check library: ", library.getModelManager() != null );
        assertTrue( "Check library: ", library.getModelManager().getTlModel() != null );
        //
        assertTrue( "Check library: Must have name.", library.getName() != null );
        assertTrue( "Check library: ", !library.getName().isEmpty() );
        assertTrue( "Check library: Must have prefix.", library.getPrefix() != null );
        assertTrue( "Check library: ", !library.getPrefix().isEmpty() );
        assertTrue( "Check library: ", !library.getBaseNS().isEmpty() );
        //
        assertTrue( "Check library: ", library.contains( library.getTL() ) );
        assertTrue( "Check library: Must have URL.", library.getTL().getLibraryUrl() != null );
    }

    /**
     * Assure library contents are editable.
     */
    public static void checkContentsAreEditable(OtmLibrary lib) {
        for (OtmLibraryMember lm : lib.getMembers()) {
            assertTrue( lm.isEditable() );
            for (OtmObject d : lm.getDescendants())
                assertTrue( d.isEditable() );
        }
    }

    private static final String AOEV = "AddOneOfEachValid_";

    /**
     * Build and add one of each of the 6 primary object. No service or resource built.
     * 
     * @param lib
     */
    public static void addOneOfEachValid(OtmLibrary lib) {
        assertTrue( "Add Util pre-condition: must be valid.", lib.isValid() );
        TestBusiness.buildOtm( lib, AOEV + "bo" );
        TestCore.buildOtm( lib, AOEV + "core" );
        TestChoice.buildOtm( lib, AOEV + "choice" );
        TestValueWithAttributes.buildOtm( lib, AOEV + "vwa" );
        TestEnumerationClosed.buildOtm( lib, AOEV + "ec" );
        TestEnumerationOpen.buildOtm( lib, AOEV + "eo" );
        if (!lib.isValid()) {
            for (OtmLibraryMember m : lib.getMembers())
                if (!m.isValid()) {
                    List<ValidationFinding> findings = lib.getFindings().getAllFindingsAsList();
                    String fString = ValidationUtils.getMessagesAsString( lib.getFindings() );
                }
            log.debug( ValidationUtils.getMessagesAsString( lib.getFindings() ) );
        }
        assertTrue( "Add Util post-condition: must be valid.", lib.isValid() );
    }

    /**
     * Build one of each library member type.
     * <p>
     * <b>Note: </b> the library will not be valid.
     * 
     * @see OtmLibraryMemberType for enumeration of member types
     * @param lib
     */
    public static int addOneOfEach(OtmLibrary lib) {
        int i = 1;
        assertTrue( "Must be an editable library.", lib.isEditable() );

        for (OtmLibraryMemberType value : OtmLibraryMemberType.values()) {
            try {
                OtmLibraryMember member =
                    OtmLibraryMemberType.buildMember( value, "TestObj" + i++, lib.getModelManager() );
                OtmLibraryMember result = lib.add( member );
                // Checks
                if (result != null) {
                    assertTrue( member.isEditable() );
                    assertTrue( member.getTlLM().getOwningLibrary() == lib.getTL() );
                    // if (member instanceof OtmContextualFacet)
                    // log.debug( "Here" );
                } else {
                    i--;
                    log.debug( "Could not add an " + value );
                }
            } catch (ExceptionInInitializerError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // Attach the contextual facets
        for (OtmLibraryMember m : lib.getMembers()) {
        }
        log.debug( "Added " + i + " to " + lib );
        return i;
    }


}
