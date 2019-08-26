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
import org.opentravel.dex.actions.DexActionManager;
import org.opentravel.dex.actions.DexActionManager.DexActions;
import org.opentravel.dex.actions.DexFullActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.resource.OtmAction;
import org.opentravel.model.resource.OtmActionRequest;
import org.opentravel.model.resource.OtmParameterGroup;
import org.opentravel.model.resource.OtmParentRef;
import org.opentravel.model.resource.TestAction;
import org.opentravel.model.resource.TestActionFacet;
import org.opentravel.model.resource.TestParamGroup;
import org.opentravel.model.resource.TestParentRef;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.repository.RepositoryItemState;

import java.util.List;

/**
 * Verifies the functions of the <code>OtmResource</code> class.
 */
public class TestResource extends TestOtmLibraryMemberBase<OtmResource> {
    private static final String R_NAME = "Testbo";

    private static final String INITIALBASEPATH = "/";
    private static final String BASEPATH1 = "/MyRecordCollection";
    private static final String BASEPATH2 = "/Sams/Records";

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

    @Test
    public void testBasePathAction() {
        // Givens
        DexActionManager fullActionMgr = new DexFullActionManager( null );
        OtmModelManager modelMgr = new OtmModelManager( fullActionMgr, null );

        TLLibrary tlLib = new TLLibrary();
        OtmLibrary lib = modelMgr.add( tlLib );
        // OtmLibrary lib = new OtmLibrary( tlLib, modelMgr );
        log.debug( "Status = " + lib.getStatus() + "  State = " + lib.getStateName() );
        // Need to add to an editable library
        assertTrue( lib.getStatus() == TLLibraryStatus.DRAFT && lib.getState() == RepositoryItemState.MANAGED_WIP
            || lib.getState() == RepositoryItemState.UNMANAGED );

        OtmResource r = buildOtm( modelMgr );
        lib.add( r );
        assertTrue( r.getLibrary() == lib );
        // Given - resource is editable
        assertTrue( r.getActionManager() != null );
        assertTrue( r.isEditable() );
        assertTrue( r.getActionManager().isEnabled( DexActions.BASEPATHCHANGE, r ) );

        // Given - an initial base path
        r.setBasePath( INITIALBASEPATH, true );
        // Given - no actions in the queue
        assertTrue( fullActionMgr.getQueueSize() == 0 );

        // When
        r.basePathProperty().set( BASEPATH1 );
        log.debug( fullActionMgr.getQueueSize() );
        assertTrue( r.getBasePath().equals( BASEPATH1 ) );

        r.basePathProperty().set( BASEPATH2 );
        assertTrue( r.getBasePath().equals( BASEPATH2 ) );

        // Then - queue must have both actions
        assertTrue( "Two actions must be in queue.", fullActionMgr.getQueueSize() == 2 );

        // When - action is undone
        fullActionMgr.undo();
        assertTrue( r.getBasePath().equals( BASEPATH1 ) );
        fullActionMgr.undo();
        assertTrue( r.getBasePath().equals( INITIALBASEPATH ) );
        assertTrue( fullActionMgr.getQueueSize() == 0 );

        // Side-effect? changes to action requests?
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

    // Test one parent
    @Test
    public void testParentRefs() {
        // Given - two resources
        OtmResource resource = buildOtm( staticModelManager );
        OtmResource parent = buildOtm( staticModelManager );
        assertTrue( staticModelManager.getResources( false ).contains( resource ) );
        assertTrue( staticModelManager.getResources( false ).contains( parent ) );

        // When - parent ref is added
        OtmParentRef parentRef = resource.addParentRef( parent );
        assertTrue( parent.getAllSubResources().contains( resource ) );

        // Then - the parent ref check is OK
        TestParentRef.check( parentRef, resource, parent );
    }

    // Test parent and grandparent
    @Test
    public void testParentAndGrandparentRefs() {
        // Given - 4 resources
        OtmResource resource = buildOtm( staticModelManager );
        OtmResource parent1 = buildOtm( staticModelManager );
        OtmResource parent2 = buildOtm( staticModelManager );
        OtmResource parent3 = buildOtm( staticModelManager );

        // When - parent ref is added
        OtmParentRef parentRef3 = parent2.addParentRef( parent3 );
        OtmParentRef parentRef2 = parent1.addParentRef( parent2 );
        OtmParentRef parentRef1 = resource.addParentRef( parent1 );

        assertTrue( resource.getParentRefs().size() == 1 );
        assertTrue( parent1.getParentRefs().size() == 1 );
        assertTrue( parent2.getParentRefs().size() == 1 );

        // Then - sub-resources can be found
        assertTrue( parent1.getAllSubResources().contains( resource ) );
        assertTrue( parent2.getAllSubResources().contains( resource ) );

        // Then - the parent ref check is OK
        TestParentRef.check( parentRef1, resource, parent1 );
        TestParentRef.check( parentRef2, parent1, parent2 );
        TestParentRef.check( parentRef3, parent2, parent3 );
    }

    // Test multiple parents
    @Test
    public void testParentsRefs() {
        // Given - 4 resources
        OtmResource resource = buildOtm( staticModelManager );
        OtmResource parent1 = buildOtm( staticModelManager );
        OtmResource parent2 = buildOtm( staticModelManager );
        OtmResource parent3 = buildOtm( staticModelManager );

        // When - parent ref is added
        OtmParentRef parentRef1 = resource.addParentRef( parent1 );
        OtmParentRef parentRef2 = resource.addParentRef( parent2 );
        OtmParentRef parentRef3 = resource.addParentRef( parent3 );

        // Then - 3 parent refs
        assertTrue( resource.getParentRefs().size() == 3 );
        // Then - the parent ref check is OK
        TestParentRef.check( parentRef1, resource, parent1 );
        TestParentRef.check( parentRef2, resource, parent2 );
        TestParentRef.check( parentRef3, resource, parent3 );
    }

    @Test
    public void testActionRequests() {
        OtmResource testResource = buildOtm( staticModelManager );

        // Given - how many requests have been created
        List<OtmActionRequest> requests = testResource.getActionRequests();
        int initialRQsize = requests.size();

        // When - two actions are added
        OtmAction action1 = TestAction.buildOtm( testResource );
        OtmAction action2 = TestAction.buildOtm( testResource );

        requests = testResource.getActionRequests();
        // Then there must be 2 more requests
        assertTrue( requests.size() == initialRQsize + 2 );
    }

    // @Test
    // public void testSWAGGERTransformer() {
    // OtmResource r = buildFullOtm( "/thisPath", "Subject", staticModelManager );
    // OtmResource p = buildParentResource( r, "Parent", staticModelManager );
    // TLResource source = r.getTL();
    //
    // for (QualifiedAction qAction : ResourceCodegenUtils.getQualifiedActions( source )) {
    // if (qAction.getAction().isCommonAction()) {
    // continue;
    // }
    // OtmAction action = (OtmAction) OtmModelElement.get( qAction.getAction() );
    // log.debug( r.getName() + " " + qAction.getActionRequest().getHttpMethod() + " action: " + action.getName()
    // + " on path " + qAction.getPathTemplate() );
    // TLActionRequest actionRequest = qAction.getActionRequest();
    // String pathTemplate = qAction.getPathTemplate();
    // TLHttpMethod httpMethod = actionRequest.getHttpMethod();
    // }
    //
    // }

    /** ****************************************************** **/

    public static OtmResource buildOtm(OtmModelManager mgr) {
        OtmResource resource = new OtmResource( buildTL(), mgr );
        mgr.add( resource );
        assertNotNull( resource );
        assertTrue( resource.getChildren().size() == 3 );
        return resource;
    }

    public static final String BASEPATH = "/SomeBasePath";

    public static TLResource buildTL() {
        TLResource tlr = new TLResource();
        tlr.setName( R_NAME );
        tlr.setBasePath( BASEPATH );

        TLActionFacet tlaf = new TLActionFacet();
        tlr.addActionFacet( tlaf );
        tlaf.setName( R_NAME + "af1" );

        TLAction tla = new TLAction();
        tla.setActionId( R_NAME + "a1" );
        tlr.addAction( tla );
        // All actions must have a request
        TLActionRequest tlar = new TLActionRequest();
        tlar.setHttpMethod( TLHttpMethod.GET );
        tla.setRequest( tlar );
        tlar.setPathTemplate( BASEPATH );

        TLParamGroup tlpg = new TLParamGroup();
        tlpg.setName( R_NAME + "pg1" );
        tlr.addParamGroup( tlpg );

        // No parent Ref
        return tlr;
    }

    /**
     * Create 1st class resource with ID parameter group and make it a parent to the passed resource. Set ID Group in
     * the parent resource reference.
     * 
     * @param r
     * @param name
     * @param mgr
     * @return
     */
    public static OtmResource buildParentResource(OtmResource r, String name, OtmModelManager mgr) {
        // Given a subject for the resource
        OtmBusinessObject parentBO = TestBusiness.buildOtm( mgr, name );
        // String parentNameString = name + "BO";
        // parentBO.setName( parentNameString );

        // Create the parent resource with path
        String parentPathString = "/" + name + "Path";
        OtmResource parentR = TestResource.buildOtm( mgr );
        parentR.setName( name );
        parentR.setAssignedType( parentBO );
        parentR.setBasePath( parentPathString, true );
        parentR.getTL().setFirstClass( true );
        OtmParameterGroup idGroup = TestParamGroup.buildIdGroup( parentR );

        OtmParentRef parentRef = r.addParentRef( parentR );
        parentRef.getTL().setParentParamGroup( idGroup.getTL() );
        parentRef.getTL().setPathTemplate( null ); // do NOT use the override

        assertTrue( parentRef.getParentResource() == parentR );
        assertTrue( parentRef.getParameterGroup() == idGroup );
        assertTrue( idGroup.getOwningMember() == parentR );

        return parentR;
    }

    /**
     * Build a fully structured resource with:
     * <ul>
     * <li>subject
     * <li>base path
     * <li>First class set to true
     * <li>actions with payload
     * <li>an action with request that has parameter group added to resource
     * <li>an action facet named "af1"
     * </ul>
     * 
     * @param pathString
     * @return
     */
    public static OtmResource buildFullOtm(String pathString, String subjectName, OtmModelManager mgr) {
        OtmResource resource = TestResource.buildOtm( mgr );
        resource.setBasePath( pathString, true );
        resource.getTL().setFirstClass( true );

        OtmBusinessObject testBO = TestBusiness.buildOtm( mgr );
        testBO.setName( subjectName );
        resource.setAssignedType( testBO );

        TestAction.buildFullOtm( resource );
        TestActionFacet.buildOtm( resource );

        return resource;
    }
}
