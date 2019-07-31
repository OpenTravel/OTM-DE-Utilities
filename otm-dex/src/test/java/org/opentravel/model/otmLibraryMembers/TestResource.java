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
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.resource.OtmAction;
import org.opentravel.model.resource.OtmActionRequest;
import org.opentravel.model.resource.OtmParameterGroup;
import org.opentravel.model.resource.TestAction;
import org.opentravel.model.resource.TestActionFacet;
import org.opentravel.model.resource.TestParamGroup;
import org.opentravel.model.resource.TestParentRef;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;

import java.util.List;

/**
 * Verifies the functions of the <code>OtmResource</code> class.
 */
public class TestResource extends TestOtmLibraryMemberBase<OtmResource> {
    private static final String R_NAME = "Testbo";

    private static Log log = LogFactory.getLog( TestResource.class );
    private static OtmBusinessObject exposedObject;

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        subject = buildOtm( staticModelManager );
        baseObject = buildOtm( staticModelManager );
        baseObject.setName( "BaseR" );

        exposedObject = TestBusiness.buildOtm( staticModelManager );
    }

    @Test
    public void testBuildResource() {
        OtmResource r = buildOtm( staticModelManager );

        // Then
        assertNotNull( r );
        assertTrue( !r.getChildren().isEmpty() );
    }

    @Override
    @Test
    public void testTypeUser() {
        OtmResource r = buildOtm( staticModelManager );

        assertTrue( "No business object assigned yet.", r.getAssignedType() == null );

        // run base tests
        r.setAssignedType( (OtmTypeProvider) exposedObject );
        assertTrue( r.getAssignedType() == exposedObject );

        super.testTypeUser( r );
    }

    @Test
    public void testFacets() {
        OtmResource testResource = buildOtm( staticModelManager );
        int kidCount = testResource.getChildren().size();

        TestAction.buildOtm( testResource );
        TestActionFacet.buildOtm( testResource );
        TestParamGroup.buildOtm( testResource );
        TestParentRef.buildOtm( testResource );

        assertTrue( kidCount + 4 == testResource.getChildren().size() );
    }

    @Test
    public void testActions() {
        OtmResource r = buildOtm( staticModelManager );
        assertTrue( r.getActions().size() >= 1 );
    }

    @Test
    public void testParameterGroups() {
        // Given a new resource with one parameter group
        OtmResource r = buildOtm( staticModelManager );
        assertTrue( r.getParameterGroups().size() >= 1 );
        int groupCount = r.getParameterGroups().size();

        // Given - two parameter groups
        OtmParameterGroup group1 = new OtmParameterGroup( new TLParamGroup(), null );
        OtmParameterGroup group2 = new OtmParameterGroup( new TLParamGroup(), null );

        // When - one group is added
        r.addParameterGroup( group1 );
        assertTrue( r.getParameterGroups().size() == groupCount + 1 );
        assertTrue( r.getParameterGroups().contains( group1 ) );
        assertTrue( group1.getOwningMember() == r );
        groupCount++;

        // When - one group is added
        r.addParameterGroup( group2 );
        assertTrue( r.getParameterGroups().size() == groupCount + 1 );
        groupCount++;
    }

    @Test
    public void testSubject() {
        OtmResource resource = buildOtm( staticModelManager );
        OtmBusinessObject testBO = TestBusiness.buildOtm( staticModelManager );

        // Initially, no subject is set
        assertTrue( resource.getSubject() == null );
        assertTrue( resource.getSubjectName().isEmpty() );

        // When set
        resource.setAssignedType( testBO );
        assertTrue( resource.getAssignedType() == testBO );
        assertTrue( resource.getSubject() == testBO );
        assertFalse( resource.getSubjectName().isEmpty() );
    }

    @Test
    public void testActionRequests() {
        OtmResource testResource = buildOtm( staticModelManager );

        // Then - no requests have been created
        List<OtmActionRequest> requests = testResource.getActionRequests();
        assertTrue( requests.isEmpty() );

        // When two actions are added with two responses
        OtmAction action1 = TestAction.buildOtm( testResource );
        // assertTrue( action1.getRequest() != null );
        // assertTrue( action1.getTL().getRequest() != null );
        // assertTrue( testResource.getChildren().contains( action1 ) );
        // assertTrue( testResource.getTL().getActions().contains( action1.getTL() ) );

        OtmAction action2 = TestAction.buildOtm( testResource );
        requests = testResource.getActionRequests();
        assertTrue( requests.size() == 2 );
    }

    /** ****************************************************** **/

    public static OtmResource buildOtm(OtmModelManager mgr) {
        OtmResource resource = new OtmResource( buildTL(), mgr );
        assertNotNull( resource );
        assertTrue( resource.getChildren().size() == 3 );
        return resource;
    }

    public static TLResource buildTL() {
        TLResource tlr = new TLResource();
        tlr.setName( R_NAME );

        TLActionFacet tlaf = new TLActionFacet();
        tlr.addActionFacet( tlaf );
        tlaf.setName( R_NAME + "af1" );

        TLAction tla = new TLAction();
        tla.setActionId( R_NAME + "a1" );
        tlr.addAction( tla );

        TLParamGroup tlpg = new TLParamGroup();
        tlpg.setName( R_NAME + "pg1" );
        tlr.addParamGroup( tlpg );

        // No parent Ref
        return tlr;
    }
}
