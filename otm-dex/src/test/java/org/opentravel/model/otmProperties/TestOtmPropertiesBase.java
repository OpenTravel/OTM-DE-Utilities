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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmLibraryMembers.OtmChoiceObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.TestChoice;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base test class for all otm properties.
 * <p>
 */
public class TestOtmPropertiesBase<L extends OtmPropertyBase<?>> {
    private static Log log = LogFactory.getLog( TestOtmPropertiesBase.class );

    protected static OtmModelManager staticModelManager = null;
    protected static OtmResource testResource;
    protected static OtmResourceChild subject;
    protected static OtmLibraryMember baseObject;

    // NOTE - does not run in abstract classes - must copy into test sub-types
    // @BeforeClass
    // public static void beforeClass() {
    // staticModelManager = new OtmModelManager( null, null );
    // baseObject = TestBusiness.buildOtm( staticModelManager );
    // testResource = TestResource.buildOtm( staticModelManager );
    // log.debug( "Before class resource base." );
    // }

    @Test
    public void testBuildOneOfEach() throws ExceptionInInitializerError, InstantiationException, IllegalAccessException,
        NoSuchMethodException, InvocationTargetException {
        // Given - a model manager with full range of actions
        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null );
        OtmChoiceObject choice = TestChoice.buildOtm( staticModelManager );
        OtmFacet<?> facet = choice.getShared();

        buildOneOfEach( facet );
        facet.getChildren().forEach( c -> test( (OtmPropertyBase) c ) );
    }

    @Test
    public void testBuilder() {
        // Given - a model manager with full range of actions
        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null );
        OtmChoiceObject choice = TestChoice.buildOtm( staticModelManager );
        OtmFacet<?> facet = choice.getShared();

        for (OtmPropertyType type : OtmPropertyType.values()) {
            OtmProperty p = OtmPropertyType.build( type, facet );
            assertTrue( p.getClass() == type.propertyClass() );
            assertTrue( OtmPropertyType.getType( p.getClass() ) == type );
            assertTrue( OtmPropertyType.getType( p ) == type );
            test( p );
            assertTrue( facet.getChildren().contains( p ) );
        }
    }

    @Test
    public void testDelete() {
        // Given - a model manager with full range of actions
        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null );
        OtmChoiceObject choice = TestChoice.buildOtm( staticModelManager );
        OtmFacet<?> facet = choice.getShared();
        for (OtmPropertyType type : OtmPropertyType.values())
            OtmPropertyType.build( type, facet );
        assertTrue( "Given - attributes from build.", facet.getChildren().size() > 0 );

        // // Test deleting all property types
        List<OtmObject> kids = new ArrayList<>( facet.getChildren() );
        kids.forEach( k -> facet.delete( (OtmProperty) k ) );
    }

    // Property constructors rely on being able to add to TL even if it was already there
    @Test
    public void testMultipleTLAdds() {
        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null );
        OtmChoiceObject choice = TestChoice.buildOtm( staticModelManager );
        OtmFacet<?> facet = choice.getShared();
        TLFacet tlFacet = facet.getTL();
        int attrCount = tlFacet.getAttributes().size();
        assertTrue( "Given - attributes from build.", attrCount > 0 );

        TLAttribute tlAttr = new TLAttribute();
        assertFalse( "Given", tlFacet.getAttributes().contains( tlAttr ) );

        // When added
        tlFacet.addAttribute( tlAttr );
        assertTrue( "Then", tlFacet.getAttributes().contains( tlAttr ) );
        assertTrue( "Then", ++attrCount == tlFacet.getAttributes().size() );

        // When added again
        tlFacet.addAttribute( tlAttr );
        tlFacet.addAttribute( tlAttr );
        tlFacet.addAttribute( tlAttr );
        tlFacet.addAttribute( tlAttr );
        assertTrue( "Then", tlFacet.getAttributes().contains( tlAttr ) );
        assertTrue( "Then - count must not change.", attrCount == tlFacet.getAttributes().size() );
        assertTrue( "Then - facet must be owner.", tlAttr.getOwner() == tlFacet );
        assertTrue( "Then", tlFacet.getAttributes().contains( tlAttr ) );
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
    public static void test(OtmProperty otm) {
        assertNotNull( otm );
        if (otm.getName() == null)
            log.debug( "Null name" );
        assertTrue( "Must have parent", otm.getParent() != null );
        assertTrue( "Must have owning member", otm.getOwningMember() != null );
        assertTrue( "Must have identity listner.", OtmModelElement.get( otm.getTL() ) == otm );
        log.debug( "Property " + otm.getName() + " OK." );
    }

    /**
     * *****************************************************************
     */

    /**
     * @deprecated - use OtmPropertyType.buildTL() Add one of each type of property to the passed property owner
     */
    @Deprecated
    public static void buildOneOfEach(OtmPropertyOwner owner) {
        TLModelElement tl = null;
        OtmProperty property = null;

        tl = new TLProperty();
        property = owner.add( tl );
        property.setName( "Element1" );

        tl = new TLProperty();
        ((TLProperty) tl).setReference( true );
        property = owner.add( tl );
        property.setName( "ElementRef2" );

        tl = new TLAttribute();
        property = owner.add( tl );
        property.setName( "Attribute3" );

        tl = new TLAttribute();
        property = owner.add( tl );
        // TODO - set type to XmlId
        property.setName( "Id4" );

        tl = new TLAttribute();
        ((TLAttribute) tl).setReference( true );
        property = owner.add( tl );
        property.setName( "AttributeRef5" );

        tl = new TLIndicator();
        ((TLIndicator) tl).setPublishAsElement( false );
        property = owner.add( tl );
        property.setName( "Indicator6" );

        tl = new TLIndicator();
        ((TLIndicator) tl).setPublishAsElement( true );
        property = owner.add( tl );
        property.setName( "IndicatorElement7" );
    }
}
