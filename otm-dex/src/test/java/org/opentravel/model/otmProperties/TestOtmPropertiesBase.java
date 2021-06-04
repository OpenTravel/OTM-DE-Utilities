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
import org.opentravel.dex.actions.string.PropertyRoleChangeAction;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmChoiceObject;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmXsdSimple;
import org.opentravel.model.otmLibraryMembers.TestChoice;
import org.opentravel.model.otmLibraryMembers.TestXsdSimple;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;

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
    protected static OtmBusinessObject baseObject;

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
        // staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null, null );

        OtmLibrary lib = TestLibrary.buildOtm();
        OtmChoiceObject choice = TestChoice.buildOtm( lib, "TestChoice" );
        OtmFacet<?> facet = choice.getShared();

        buildOneOfEach2( facet );
        facet.getChildren().forEach( c -> test( (OtmPropertyBase) c ) );
    }

    @Test
    public void testBuilder() {
        // Given - a model manager with full range of actions
        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null, null );
        OtmChoiceObject choice = TestChoice.buildOtm( staticModelManager );
        OtmFacet<?> facet = choice.getShared();

        for (OtmPropertyType type : OtmPropertyType.values()) {
            OtmProperty p = OtmPropertyType.build( type, facet );
            // Not all properties can be added to shared facet (e.g. enumValue)
            if (p != null) {
                assertTrue( p.getClass() == type.propertyClass() );
                assertTrue( OtmPropertyType.getType( p.getClass() ) == type );
                assertTrue( OtmPropertyType.getType( p ) == type );
                assertTrue( p.getPropertyType() == type );
                test( p );
                assertTrue( facet.getChildren().contains( p ) );
            }
        }
    }

    @Test
    public void testDelete() {
        // Given - a model manager with full range of actions
        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null, null );
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
        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null, null );
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

    @Test
    public void testTypeChange() {
        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null, null );
        OtmChoiceObject choice = TestChoice.buildOtm( staticModelManager );
        // Givens: a facet with one of each property type
        OtmFacet<?> facet = choice.getShared();
        List<OtmObject> kids = new ArrayList<>( facet.getChildren() );
        kids.forEach( k -> facet.delete( (OtmProperty) k ) );
        buildOneOfEach2( facet );
        int kidCount = facet.getChildren().size();

        PropertyRoleChangeAction action = null;

        for (OtmPropertyType type : OtmPropertyType.values()) {
            kids = new ArrayList<>( facet.getChildren() );
            for (OtmObject k : kids) {
                if (k instanceof OtmProperty) {
                    OtmPropertyType oldType = ((OtmProperty) k).getPropertyType();
                    action = new PropertyRoleChangeAction();
                    action.setSubject( k );
                    // Change it
                    OtmProperty newProperty = action.change( type.label() );
                    // Then - if not the same type, there should be a new property
                    if (newProperty != null) {
                        assertTrue( newProperty.getPropertyType() == type );
                        assertTrue( "Must have correct parent.", newProperty.getParent() == facet );
                        // Then - new property replaced old property
                        assertTrue( facet.getChildren().size() == kidCount );
                        assertFalse( facet.contains( k ) );
                        assertTrue( facet.contains( newProperty ) );
                        // Then - TL facet must contain the new TL property
                        if (newProperty instanceof OtmElement)
                            assertTrue( ((OtmElement<?>) newProperty).getTL().getOwner() == facet.getTL() );
                        if (newProperty instanceof OtmAttribute)
                            assertTrue( ((OtmAttribute<?>) newProperty).getTL().getOwner() == facet.getTL() );
                        if (newProperty instanceof OtmIndicator)
                            assertTrue( ((OtmIndicator<?>) newProperty).getTL().getOwner() == facet.getTL() );
                        assertFalse( newProperty.isInherited() );

                        // Change it back
                        action.undo();
                        assertTrue( facet.getChildren().size() == kidCount );
                        assertTrue( ((OtmProperty) k).getPropertyType() == oldType );
                        assertFalse( facet.contains( newProperty ) );
                        assertTrue( facet.contains( k ) );
                        if (k instanceof OtmElement)
                            assertTrue( ((OtmElement) k).getTL().getOwner() == facet.getTL() );
                        if (k instanceof OtmAttribute)
                            assertTrue( ((OtmAttribute) k).getTL().getOwner() == facet.getTL() );
                        if (k instanceof OtmIndicator)
                            assertTrue( ((OtmIndicator) k).getTL().getOwner() == facet.getTL() );
                        assertFalse( k.isInherited() );
                    }
                    // log.debug( "Changed " + oldType + " and undid change." );
                }
            }
        }
    }


    @Test
    public void testCloneTypeAssignments() {
        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null, null );
        OtmChoiceObject choice = TestChoice.buildOtm( staticModelManager );
        // Given an empty facet
        OtmFacet<?> facet = choice.getShared();
        List<OtmObject> kids = new ArrayList<>( facet.getChildren() );
        int i = 1;
        for (OtmObject k : kids) {
            assertTrue( "Given", k instanceof OtmProperty );
            assertTrue( facet.getChildren().contains( k ) );
            k.setName( "p" + i++ );
            // facet.delete( (OtmProperty) k );
        }
        kids.forEach( k -> facet.delete( (OtmProperty) k ) );

        // Given - a simple type to assign
        OtmXsdSimple simple = TestXsdSimple.buildOtm( staticModelManager );

        // Given - two type user properties
        OtmTypeUser p1 = (OtmTypeUser) OtmPropertyType.build( OtmPropertyType.ELEMENT, facet );
        p1.setAssignedType( simple );
        OtmTypeUser p2 = (OtmTypeUser) OtmPropertyType.build( OtmPropertyType.ATTRIBUTE, facet );

        // When - cloned
        ((OtmProperty) p2).clone( (OtmProperty) p1 );

        assertTrue( "Then - must have assigned type.", p2.getAssignedType() == simple );
        assertTrue( "Then - must have assigned type.", p2.getAssignedTLType() == simple.getTL() );
    }

    /**
     * *****************************************************************
     */

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
        // log.debug( "Property " + otm.getName() + " OK." );
    }

    /** *********************** builders ********************/
    /**
     * Build a new element in the passed owner
     * 
     * @param owner must be facade for TLPropertyOwner
     * @return element or null
     */
    public static OtmElement<TLProperty> buildElement(OtmPropertyOwner owner) {
        OtmElement<TLProperty> element = null;
        if (owner.getTL() instanceof TLPropertyOwner) {
            TLProperty tlp = new TLProperty();
            ((TLPropertyOwner) owner.getTL()).addElement( tlp );
            element = new OtmElement<>( tlp, owner );
        }
        return element;
    }

    /**
     * Build a new attribute in the passed owner
     * 
     * @param owner must be facade for TLAttributeOwner
     * @return element or null
     */
    public static OtmAttribute<TLAttribute> buildAttribute(OtmPropertyOwner owner) {
        OtmAttribute<TLAttribute> attr = null;
        if (owner.getTL() instanceof TLAttributeOwner) {
            TLAttribute tlp = new TLAttribute();
            ((TLAttributeOwner) owner.getTL()).addAttribute( tlp );
            attr = new OtmAttribute<>( tlp, owner );
        }
        return attr;
    }

    /**
     * Build a new attribute in the passed owner and assign type without checks.
     */
    public static OtmAttribute<TLAttribute> buildAttribute(OtmPropertyOwner owner, OtmTypeProvider provider) {
        OtmAttribute<TLAttribute> attr = buildAttribute( owner );
        attr.setAssignedType( provider );
        return attr;
    }

    /**
     * Use the enumeration's builder {@link OtmPropertyType#build(OtmPropertyType, OtmPropertyOwner)} to add one of each
     * compatible property type to the owner. Uses {@link OtmPropertyFactory}.
     * 
     * @param owner
     */
    public static void buildOneOfEach2(OtmPropertyOwner owner) {
        for (OtmPropertyType type : OtmPropertyType.values()) {
            OtmProperty p = OtmPropertyType.build( type, owner );
            if (p != null) {
                p.setName( owner.getName() + type.toString() );
                // log.debug( "Created property " + p + " of type " + type.toString() );
            }
        }
    }

    /**
     * Explicitly build a new TL then add to the owner.
     * 
     * @deprecated - use buildOneOfEach2 which uses the property type enumeration and factory
     */
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
