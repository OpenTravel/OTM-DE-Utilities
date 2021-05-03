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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.model.otmLibraryMembers.TestResource;

/**
 * Base test class for all otm resource descendants.
 * <p>
 * Implementations must provide their own static, type specific, buildOtm() and buildTL() methods.
 * <p>
 * If <i>subject</i> is set in the before class, the following will be automatically run:
 * <ul>
 * <li>testConstructors
 * <li>testChildrenOwner
 * <li>testTypeUser
 * <li>testWhereUsed
 * <li>testInheritance - if baseObject is set
 * </ul>
 * Sub-types that do not conform to the tests, should override the test method.
 * <p>
 * Sub-types should test their own facets.
 */
public abstract class TestOtmResourceBase<L extends OtmResourceChildBase> {

    private static Log log = LogFactory.getLog( TestOtmResourceBase.class );

    protected static OtmModelManager staticModelManager = null;
    protected static OtmResource testResource;
    protected static OtmResourceChild subject;
    protected static OtmLibraryMember baseObject;

    // NOTE - does not run in abstract classes - must copy into test sub-types
    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        baseObject = TestBusiness.buildOtm( staticModelManager );
        testResource = TestResource.buildOtm( staticModelManager );
        log.debug( "Before class resource base." );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConstructors() {
        if (subject != null)
            testConstructors( (L) subject );
    }

    /**
     * Assure:
     * <ol>
     * <li>Must be its own owner
     * <li>Must have identity listener
     * </ol>
     * 
     * @param otm
     */
    public void testConstructors(L otm) {
        assertNotNull( otm );
        assertTrue( "Must have owner", otm.getOwningMember() != null );
        assertTrue( "Must have identity listner.", OtmModelElement.get( otm.getTL() ) == otm );
        log.debug( "Constuctor OK." );
    }
}
