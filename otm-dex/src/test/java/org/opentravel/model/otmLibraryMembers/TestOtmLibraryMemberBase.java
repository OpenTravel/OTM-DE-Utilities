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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmFacets.OtmAbstractFacet;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmRoleEnumeration;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base test class for all otm objects. Implementations must provide their own static, type specific,
 * buildOtm() and buildTL() methods. See {@link TestLibraryMemberBase} for static utilities and actual tests on the base
 * class
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
 * 
 * @see TestLibraryMemberBase - static utilities and actual tests on the base classs
 */
public abstract class TestOtmLibraryMemberBase<L extends OtmLibraryMember> {

    static Log log = LogFactory.getLog( TestOtmLibraryMemberBase.class );

    protected static OtmModelManager staticModelManager = null;
    protected static OtmLibrary staticLib = null;
    protected static OtmLibraryMember subject;
    protected static OtmLibraryMember baseObject;

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        log.debug( "Model manager created." );
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
     * <li>It is managed in its model manager
     * </ol>
     * 
     * @param otm
     */
    public void testConstructors(L otm) {
        assertNotNull( otm );
        assertTrue( "Must be its own owner", otm.getOwningMember() == otm );
        assertTrue( "Must have identity listner.", OtmModelElement.get( otm.getTL() ) == otm );
        if (otm.getModelManager() != null)
            assertTrue( "Must be managed in model manager.", otm.getModelManager().getMembers().contains( otm ) );
        log.debug( "Constuctor OK." );
    }


    @Test
    public void testBaseType() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException,
        NoSuchMethodException, InvocationTargetException {
        TestLibraryMemberBase.testBaseType( subject );
    }

    @Test
    public void testChildrenOwner() {
        if (subject instanceof OtmChildrenOwner)
            testChildrenOwner( (OtmChildrenOwner) subject );
    }

    /**
     * Test children, childrenHierarcy, and descendants access. Requires the object to have children.
     * 
     * @param otm
     */
    public void testChildrenOwner(OtmChildrenOwner otm) {
        OtmChildrenOwner co = (OtmChildrenOwner) otm;
        List<OtmObject> kids = co.getChildren();
        assertTrue( !kids.isEmpty() );

        assertTrue( !co.getChildrenHierarchy().isEmpty() );
        assertNotNull( co.getChildrenTypeProviders() );
        assertNotNull( co.getDescendantsTypeUsers() );
        assertNotNull( co.getDescendantsChildrenOwners() );
        assertNotNull( co.getDescendantsTypeUsers() );
        log.debug( "Children owner methods OK." );
    }

    @Test
    public void testCopy() {
        testCopy( subject );
    }

    public void testCopy(OtmLibraryMember member) {
        OtmLibraryMember copy = member.copy();
        TestLibraryMemberBase.check( copy );

        if (copy != null) {
            assertTrue( copy.getClass() == member.getClass() );
            List<OtmObject> memberDesc = member.getDescendants();
            List<OtmObject> copyDesc = copy.getDescendants();

            assertTrue( copy.getDescendants().size() == member.getDescendants().size() );

            if (copy instanceof OtmChildrenOwner)
                testChildrenOwner( copy );
        }
    }

    @Test
    public void testRefresh() {
        if (subject instanceof OtmLibraryMember)
            ((OtmLibraryMember) subject).refresh();
    }

    public void testRefresh(OtmLibraryMember member) {
        member.refresh();
        log.debug( "Testing refresh of " + subject );
    }

    @Test
    public void testTypeUser() {
        if (subject instanceof OtmTypeUser)
            testTypeUser( (OtmTypeUser) subject );
    }

    public void testTypeUser(OtmTypeUser tu) {

        assertTrue( tu.getAssignedType() != null );
        assertTrue( tu.getAssignedTLType() != null );
        assertTrue( tu.getAssignedTLType() == tu.getAssignedType().getTL() );

        // FIXME - this will fail because it needs to know the library
        // assertTrue( !core.assignedTypeProperty().get().isEmpty() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testWhereUsed() {
        if (subject != null)
            testWhereUsed( (L) subject );
    }

    public void testWhereUsed(L otm) {
        assertNotNull( otm.getUsedTypes() );
        assertNotNull( otm.getWhereUsed() );
    }

    @Test
    public void testLibrary() {
        if (subject != null)
            testLibrary( (L) subject );
    }

    public void testLibrary(L member) {
        OtmLibrary lib = staticModelManager.add( new TLLibrary() );
        assertTrue( "Must register library.", staticModelManager.get( lib ) == lib.getTL() );
        OtmLibraryMember result = lib.add( member );

        // Then the underlying TL model changed.
        if (result != null) {
            assertTrue( lib.getTL().getNamedMembers().contains( member.getTL() ) );
            assertTrue( member.getLibrary() == lib );
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInheritance() {
        // Make sure the subject does not have children that could conflict
        List<OtmObject> kids = new ArrayList<>( subject.getChildren() );
        kids.forEach( c -> subject.delete( c ) );

        if (subject != null && baseObject != null) {
            extendObject( (L) baseObject, (L) subject );
            testInheritance( (L) subject );
        }
    }

    @SuppressWarnings("unchecked")
    public void testInheritance(L otm) {
        L base = (L) otm.getBaseType();
        if (base == null)
            return;

        if (otm instanceof OtmChildrenOwner) {
            List<OtmObject> otmInherited = otm.getInheritedChildren();
            List<OtmObject> baseKids = ((OtmChildrenOwner) base).getChildren();
            // Remove the kids that can't be inherited
            // Note: this list was based on test results and not analysis after fixing enumeration inheritance
            List<OtmObject> candidates = new ArrayList<OtmObject>( baseKids );
            for (OtmObject k : candidates) {
                if (k instanceof OtmContributedFacet)
                    continue;
                if (k instanceof OtmAbstractFacet)
                    baseKids.remove( k );
                if (k instanceof OtmRoleEnumeration)
                    baseKids.remove( k );
            }

            assertTrue( "Must have interited child for every base child.", baseKids.size() == otmInherited.size() );
            for (OtmObject i : otmInherited) {
                assertTrue( "Inherited child must report it is inherited.", i.isInherited() );
            }
        }
    }

    /**
     * Extend the base object with the extension object. Return the extension object if successful. Example:
     * extendObject(animals, cat);
     * <p>
     * Note, extension only changes the underlying TL objects.
     * 
     * @param base
     * @param extension object to become a sub-type of base
     * @return extension if successful or null
     */
    public L extendObject(L base, L extension) {
        // Equivalent code
        // if (base.getTL() instanceof NamedEntity && extension.getTL() instanceof TLExtensionOwner) {
        // TLExtension tlex = new TLExtension();
        // tlex.setExtendsEntity( (NamedEntity) base.getTL() );
        // ((TLExtensionOwner) extension.getTL()).setExtension( tlex );
        // }
        OtmObject result = extension.setBaseType( base );
        assertTrue( "Util: extension must return base type.", result == base );

        assertTrue( extension.getBaseType() != null );
        assertTrue( extension.getBaseType() == base );
        assertTrue( base.getWhereUsed().contains( extension ) );
        return extension;
        // return null;
    }

    // public static O buildOtm(OtmModelManager mgr) {}
    // public static TLModelElement buildTL() {}
}
