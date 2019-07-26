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

package org.opentravel.model.resource;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestResource;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLResource;

import java.util.List;

/**
 * Test class for Action Facet resource descendants.
 * <p>
 */
public class TestAction extends TestOtmResourceBase<OtmAction> {
    private static Log log = LogFactory.getLog( TestAction.class );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null );
        baseObject = TestBusiness.buildOtm( staticModelManager );
        testResource = TestResource.buildOtm( staticModelManager );

        subject = buildOtm( testResource );
        log.debug( "Before class ran." );
    }

    @Test
    public void testChildren() {
        OtmAction a = buildOtm( testResource );
        List<OtmObject> kids = a.getChildren();
        assertTrue( a.getChildren().size() >= 2 );

        // Then - make sure there are request and responses in the children
        assertTrue( a.getChildren().contains( a.getRequest() ) );
        for (OtmActionResponse r : a.getResponses())
            assertTrue( a.getChildren().contains( r ) );
    }

    /**
     * Build an action with one request and response.
     * 
     * @param resource
     * @return
     */
    public static OtmAction buildOtm(OtmResource resource) {
        OtmAction action = new OtmAction( buildTL( resource.getTL() ), resource );

        assertTrue( action.getRequest() != null );
        assertTrue( action.getTL().getRequest() != null );
        assertTrue( resource.getChildren().contains( action ) );
        assertTrue( resource.getTL().getActions().contains( action.getTL() ) );

        return action;
    }

    public static TLAction buildTL(TLResource tlResource) {
        TLAction tla = new TLAction();
        tla.setActionId( "Create" );
        tla.addResponse( new TLActionResponse() );
        tla.setRequest( new TLActionRequest() );
        tla.getRequest().setHttpMethod( TLHttpMethod.POST );
        tlResource.addAction( tla );
        return tla;
    }
}
