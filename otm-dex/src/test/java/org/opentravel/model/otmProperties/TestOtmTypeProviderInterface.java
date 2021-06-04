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

package org.opentravel.model.otmProperties;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.opentravel.dex.actions.AddAliasAction;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmFacets.OtmSharedFacet;
import org.opentravel.model.otmLibraryMembers.OtmExtensionPointFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmServiceObject;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestChoice;
import org.opentravel.model.otmLibraryMembers.TestCore;
import org.opentravel.model.otmLibraryMembers.TestLibraryMemberBase;
import org.opentravel.model.otmLibraryMembers.TestOtmSimple;
import org.opentravel.model.otmLibraryMembers.TestValueWithAttributes;
import org.opentravel.schemacompiler.model.TLAliasOwner;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for Property Type Elements
 * <p>
 */
public class TestOtmTypeProviderInterface {
    private static Log log = LogFactory.getLog( TestOtmTypeProviderInterface.class );

    // @BeforeClass
    // public static void beforeClass() {
    // log.debug( "Before class ran." );
    // }

    /**
     * Build, business, choice, VWA, core and simple objects. Add aliases to each one.
     * 
     * @param lib
     */
    public static void buildOneOfEachTypeProvider(OtmLibrary lib) {
        // roles
        // xsd

        // objects and facets
        TestBusiness.buildOtm( lib, "ProviderBO" );
        TestChoice.buildOtm( lib, "ProviderChoice" );
        TestValueWithAttributes.buildOtm( lib, "ProviderVWA" );
        TestCore.buildOtm( lib, "ProviderCore" );
        TestOtmSimple.buildOtm( lib, "ProviderSimple" );
        // Check
        for (OtmLibraryMember member : lib.getMembers()) {
            assertTrue( lib.getProviders().contains( member ) );
            for (OtmObject p : member.getChildren())
                if (p instanceof OtmTypeProvider)
                    assertTrue( lib.getProviders().contains( p ) );
        }

        // Add aliases
        AddAliasAction action = new AddAliasAction();
        OtmAlias newAlias = null;
        for (OtmLibraryMember member : lib.getMembers()) {
            if (member.getTL() instanceof TLAliasOwner) {
                action.setSubject( member );
                newAlias = (OtmAlias) action.doIt( member.getName() + "Alias" );
                // log.debug( "Added alias to " + member );

                // 1/12/2010 - Fails when all tests are run. Passes when run alone
                // assertTrue( "Must have new alias.", newAlias != null );

                // FIXME
                // assertTrue( lib.getProviders().contains( newAlias ) );
            }
        }

    }

    @Test
    public void test_assignedTypeProperty() {
        // TODO
    }


    public static void checkTypeAssignment(List<OtmTypeUser> users, OtmTypeProvider provider) {
        for (OtmTypeUser user : users)
            TestOtmTypeUserInterface.check( user, provider );
    }

    /**
     * Build one of each type provider in the passed library. Builds: Facets, Complex Objects, Role Enumeration, Simple
     * Objects, VWA, Resource
     * <p>
     * Exclude: service, resource, extensionPoint objects.
     * <p>
     * Preferred testing utility for building providers.
     * 
     * @param lib
     * @return list of providers including non-members
     */
    public static List<OtmTypeProvider> buildOneOfEach(OtmLibrary lib) {
        List<OtmTypeProvider> typeProviders = new ArrayList<>();

        TestLibraryMemberBase.buildOneOfEachWithProperties( lib );
        // TODO
        // Alias and AliasFacet
        // XSD element and simple

        for (OtmLibraryMember m : lib.getMembers()) {
            if (m instanceof OtmServiceObject)
                continue; // Do NOT include it
            if (m instanceof OtmResource)
                continue; // Do NOT include it
            if (m instanceof OtmExtensionPointFacet)
                continue; // Do NOT include it
            if (m instanceof OtmTypeProvider)
                typeProviders.add( (OtmTypeProvider) m );
            typeProviders.addAll( m.getDescendantsTypeProviders() );
        }
        // Remove shared facets. They are forced to be the owner when assigned
        List<OtmSharedFacet> shared = new ArrayList<>();
        typeProviders.forEach( p -> {
            if (p instanceof OtmSharedFacet)
                shared.add( (OtmSharedFacet) p );
        } );
        shared.forEach( s -> typeProviders.remove( s ) );

        assertTrue( "Builder: ", !typeProviders.isEmpty() );
        return typeProviders;
    }


}
