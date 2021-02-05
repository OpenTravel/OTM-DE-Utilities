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
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.dex.actions.BaseTypeChangeAction;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmFacets.OtmChoiceFacet;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.model.otmFacets.OtmQueryFacet;
import org.opentravel.model.otmProperties.OtmAttribute;
import org.opentravel.model.otmProperties.TestOtmPropertiesBase;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Verifies the functions of the <code>OtmContextualFacet</code> class.
 */
public class TestContextualFacet extends TestOtmLibraryMemberBase<OtmContextualFacet> {
    private static Log log = LogFactory.getLog( TestContextualFacet.class );
    private static final String CF_NAME = "TestCF";

    protected static OtmLibraryMember member = null;
    protected static OtmContextualFacet cf = null;
    protected static OtmContributedFacet contrib = null;
    // protected static OtmModelManager staticModelManager = new OtmModelManager( null, null );

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null );
        // Needed for library member tests
        subject = TestChoiceFacet.buildOtm( staticModelManager );
        baseObject = TestChoiceFacet.buildOtm( staticModelManager );
    }

    /** *************** Contextual facet overrides ********************/
    @Override
    public OtmContextualFacet extendObject(OtmContextualFacet base, OtmContextualFacet extension) {
        // This sets injection site, not base type.
        OtmObject result = extension.setBaseType( base );

        assertTrue( result == base );
        assertTrue( extension.getBaseType() != null );
        assertTrue( extension.getBaseType() == base );
        // Not true for injection.
        // assertTrue( base.getWhereUsed().contains( extension ) );

        return extension;
    }

    @Override
    public void testInheritance(OtmContextualFacet otm) {
        // Nothing to test - properties are not inherited from base object
    }

    /** *************** Static Contextual Facet Tests ********************/

    public static void testContributedFacet(OtmContributedFacet contrib, OtmContextualFacet cf, OtmLibraryMember lm) {
        log.debug( "Testing contributed facet: " + cf );
        assertTrue( contrib != null );
        assertTrue( cf != null );
        assertTrue( lm != null );
        if (lm.getLibrary() != null)
            assertTrue( cf.getLibrary() != null );

        // Library Member Tests
        assertTrue( "Library member has contributor child.", lm.getChildren().contains( contrib ) );
        assertTrue( "Library Member must contain contributed facet.",
            lm.getChildrenContributedFacets().contains( contrib ) );

        // Contributed Facet Tests
        assertTrue( contrib.getParent() == lm );
        assertTrue( contrib.getActionManager() != null );
        assertTrue( contrib.getModelManager() != null );
        assertTrue( "Contributor must be owned by Library member.", contrib.getOwningMember() == lm );
        assertTrue( "Contextual facet must have contributed object.", cf.getContributedObject() == lm );
        assertTrue( "Contributor linked to contextual facet.", contrib.getContributor() == cf );
        assertTrue( "Contextual facet knows where it is contributed.", cf.getWhereContributed() == contrib );
        assertTrue( "Model manager must have facet as member.", lm.getModelManager().getMembers().contains( cf ) );

        // TL Facet Tests
        assertTrue( "Both facets have same TL facet", cf.getTL() == contrib.getTL() );
        assertTrue( "TL is a TLContextual facet", cf.getTL() instanceof TLContextualFacet );
        if (cf instanceof OtmCustomFacet && lm instanceof OtmBusinessObject)
            assertTrue( "Member's TL must have contributor's TL. ",
                ((TLBusinessObject) lm.getTL()).getCustomFacets().contains( contrib.getTL() ) );
        if (cf instanceof OtmQueryFacet && lm instanceof OtmBusinessObject)
            assertTrue( "Member's TL must have contributor's TL. ",
                ((TLBusinessObject) lm.getTL()).getQueryFacets().contains( contrib.getTL() ) );
        if (cf instanceof OtmChoiceFacet && lm instanceof OtmChoiceObject)
            assertTrue( "Member's TL must have contributor's TL. ",
                ((TLChoiceObject) lm.getTL()).getChoiceFacets().contains( contrib.getTL() ) );
        // TODO - test if LM is contextual facet

        // Verify the contributed owner is the same as the TL contextual facet's owner
        if (cf.getTL().getOwningEntity() != null && cf.getWhereContributed() != null)
            assertTrue( lm == OtmModelElement.get( (TLModelElement) cf.getTL().getOwningEntity() ) );
    }

    public void testAdd() {
        int childCount = cf.getChildren().size();
        // When - children added using factory
        TestOtmPropertiesBase.buildOneOfEach2( contrib );
        // Then - both contextual and contrib must report having children
        assertTrue( cf.getChildren().size() > childCount );
        assertFalse( contrib.getChildren().isEmpty() );
        assertFalse( cf.getChildren().isEmpty() );
        testContributedFacet( contrib, cf, member );

        // When - adding a TL directly
        childCount = cf.getChildren().size();
        TLAttribute tla = new TLAttribute();
        tla.setOwner( cf.getTL() );
        tla.setName( "attrX" );
        cf.add( tla );
        // Then
        assertTrue( tla.getListeners().size() == 1 );
        assertTrue( cf.getChildren().size() > childCount );
        OtmObject p = OtmModelElement.get( tla );
        assertTrue( p instanceof OtmAttribute );
        assertTrue( cf.getChildren().contains( p ) );
        assertTrue( contrib.getChildren().contains( p ) );
        testContributedFacet( contrib, cf, member );
    }

    public void testCFInheritance(OtmLibraryMember extension) {
        // Given - a member to use as base type with CF and contrib
        assertTrue( member != null );
        assertTrue( extension != null );
        // assertTrue( extension.getLibrary() != null );
        assertTrue( member.getClass() == extension.getClass() );
        assertTrue( member.getTL() instanceof TLExtensionOwner );

        // When - extension extends base
        extension.setBaseType( member );

        // Then - Member TL extension is null; Extension's TL has extension.
        assertTrue( "Given", extension.getBaseType() == member );
        assertTrue( "Given", ((TLExtensionOwner) extension.getTL()).getExtension() != null );
        assertTrue( "Given",
            ((TLExtensionOwner) extension.getTL()).getExtension().getExtendsEntity() == member.getTL() );

        if (member instanceof OtmChoiceObject) {
            // Then - facet codegen utils reports out inherited children as used in to modelInheritedChildren()
            List<TLContextualFacet> tlKids = ((OtmChoiceObject) member).getTL().getChoiceFacets();
            assertTrue( "Given: there must be TL choice facets.", !tlKids.isEmpty() );
            TLFacetOwner extendedOwner = (TLFacetOwner) extension.getTL();
            List<TLContextualFacet> ghosts = FacetCodegenUtils.findGhostFacets( extendedOwner, TLFacetType.CHOICE );
            assertTrue( "CodegenUtils must find ghost facets.", !ghosts.isEmpty() );
        }

        // Then - non-contextual facets will not be inherited.
        Collection<OtmContributedFacet> cfKids = member.getChildrenContributedFacets();
        List<OtmObject> iKids = extension.getInheritedChildren();
        assertTrue( cfKids.size() == iKids.size() );
    }

    public void testNestedContributedFacets(OtmContextualFacet nestedCF) {
        // Given - a cf with children contributed to a member
        // Given - a second CF with children

        // When - injection point set
        OtmObject result = nestedCF.setBaseType( cf );
        // Then
        assertTrue( "Non-null result.", result == cf );
        assertTrue( "TL Owning entity is set.", nestedCF.getTL().getOwningEntity() == cf.getTL() );
        // Then - cf has nested as child
        List<OtmObject> kids = cf.getChildren();
        assertTrue( "Nested CF is a child of CF.", kids.contains( nestedCF.getWhereContributed() ) );
        assertTrue( "Nested CF is contributed to CF.", nestedCF.getContributedObject() == cf );
        testContributedFacet( nestedCF.getWhereContributed(), nestedCF, cf );
    }
    // Test via OtmFacetFactory

    public void testDeletingChildren() {
        // When - children added using factory
        int childCount = cf.getChildren().size();
        ArrayList<OtmObject> children = new ArrayList<>( cf.getChildren() );
        List<TLModelElement> tlKids = getTLChildren();
        assertTrue( tlKids.size() == children.size() );
        TestOtmPropertiesBase.buildOneOfEach2( contrib );
        assertFalse( cf.getChildren().isEmpty() );

        children = new ArrayList<>( cf.getChildren() );
        tlKids = getTLChildren();
        assertTrue( tlKids.size() == children.size() );

        children.forEach( c -> cf.delete( c ) );
        // Then
        assertTrue( cf.getChildren().isEmpty() );
        assertTrue( cf.getTL().getIndicators().isEmpty() );
        assertTrue( cf.getTL().getAttributes().isEmpty() );
        assertTrue( cf.getTL().getElements().isEmpty() );
    }

    public List<TLModelElement> getTLChildren() {
        ArrayList<TLModelElement> tlKids = new ArrayList<>();
        tlKids.addAll( cf.getTL().getAttributes() );
        tlKids.addAll( cf.getTL().getIndicators() );
        tlKids.addAll( cf.getTL().getElements() );
        return tlKids;
    }

    public void testDeleteFromMember() {
        // Given
        testContributedFacet( contrib, cf, member );
        assertTrue( member.getChildren().contains( contrib ) );

        // When deleted
        member.delete( cf );
        assertFalse( member.getChildren().contains( contrib ) );
    }

    // /**
    // */
    //
    // @Test
    // public void testOpenedContextualFacets() {
    // OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
    // // Given a project that uses local library files
    // TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
    // for (OtmLibrary lib : mgr.getLibraries())
    // log.debug( "Library " + lib + " opened." );
    // assertTrue( "Must have project items.", !mgr.getProjectManager().getAllProjectItems().isEmpty() );
    //
    // for (OtmLibraryMember m : mgr.getMembers()) {
    // if (m instanceof OtmContextualFacet) {
    // OtmContributedFacet contrib = ((OtmContextualFacet) m).getWhereContributed();
    // OtmLibraryMember base = ((OtmContextualFacet) m).getContributedObject();
    // if (contrib != null && base != null)
    // TestContextualFacet.testContributedFacet( contrib, (OtmContextualFacet) m, base );
    // else {
    // String oeName = ((TLContextualFacet) m.getTL()).getOwningEntityName();
    // log.debug( "Bad contextual facet: " + m + " Entity name = " + oeName );
    // for (OtmLibraryMember candidate : mgr.getMembers()) {
    // if (candidate.getNameWithPrefix().equals( oeName ))
    // log.debug( "Name Match Found " );
    // }
    // }
    // }
    // }
    // }


    @Test
    public void testInModelManager() {
        log.debug( "Testing contextual facets in model manager." );

        // Given - a Choice object and contextual facet
        OtmChoiceObject co1 = TestChoice.buildOtm( TestLibrary.buildOtm(), "TestCH" );
        assertTrue( "Model manager must contain choice object.", co1.getModelManager().getMembers().contains( co1 ) );

        // Given - a contextual facet contributed to the choice object
        OtmContextualFacet cf = TestChoiceFacet.buildOtm( co1.getModelManager() );
        assertTrue( "Manager must not contain contextual facet.", !staticModelManager.getMembers().contains( cf ) );

        // When - contextual facet added to model manager
        staticModelManager.add( cf );
        assertTrue( "Manager must contain contextual facet.", staticModelManager.getMembers().contains( cf ) );

        // When - contributed to the choice object
        OtmContributedFacet contrib = co1.add( cf );
        assertTrue( "Choice object must contain facet.", co1.getChildren().contains( contrib ) );
        assertTrue( "Manager must not contain contributed.", !staticModelManager.getMembers().contains( contrib ) );

        // Given - a contextual facet NOT contributed
        OtmContextualFacet cf2 = TestChoiceFacet.buildOtm( staticModelManager );
        staticModelManager.add( cf2 );
        assertTrue( "Manager must contain contextual facet.", staticModelManager.getMembers().contains( cf2 ) );
    }

    @Test
    public void testChangingWhereContributed() {
        log.debug( "Testing changing base type (where contributed) of a contextual facet." );

        // Given - a Choice object and contextual facet
        OtmLibrary ln = TestLibrary.buildOtm( staticModelManager );
        OtmChoiceObject co1 = TestChoice.buildOtm( ln, "Ch1" );
        OtmChoiceObject co2 = TestChoice.buildOtm( ln, "Ch2" );

        // Given - a contextual facet contributed to the choice object
        OtmContextualFacet cf = TestChoiceFacet.buildOtm( staticModelManager );
        ln.add( cf );
        assertTrue( "Given - is editable", cf.isEditable() );
        OtmContributedFacet contrib = co1.add( cf );
        assertTrue( "Given - where contributed is set.", cf.getWhereContributed() == contrib );
        assertTrue( "Given - contributor is set.", contrib.getContributor() == cf );
        testContributedFacet( contrib, cf, co1 );

        // Then
        assertTrue( co1.getChildren().contains( contrib ) );
        assertTrue( !co2.getChildren().contains( contrib ) );

        // When - moved to co2
        cf.setBaseType( co2 );
        // Then
        assertTrue( co2.getChildren().contains( contrib ) );
        assertTrue( !co1.getChildren().contains( contrib ) );
        testContributedFacet( contrib, cf, co2 );

        // When - moved to co3 using action
        OtmChoiceObject co3 = TestChoice.buildOtm( ln, "Ch3" );
        BaseTypeChangeAction action = new BaseTypeChangeAction();
        action.setSubject( cf );
        action.doIt( co3 );

        // Then
        assertTrue( !co1.getChildren().contains( contrib ) );
        assertTrue( !co2.getChildren().contains( contrib ) );
        assertTrue( co3.getChildren().contains( contrib ) );
        testContributedFacet( contrib, cf, co3 );
    }

    @Test
    public void testCF_Names() {
        log.debug( "Testing names when changing base type (where contributed) of a contextual facet." );

        // Given - a Choice object and contextual facet
        OtmLibrary ln = TestLibrary.buildOtm( staticModelManager );
        OtmChoiceObject co1 = TestChoice.buildOtm( ln, "Ch1" );
        OtmChoiceObject co2 = TestChoice.buildOtm( ln, "Ch2" );

        // Given - a contextual facet contributed to the choice object
        OtmContextualFacet cf = TestChoiceFacet.buildOtm( staticModelManager );
        String baseCfName = TestChoiceFacet.CF_NAME;
        ln.add( cf );
        assertTrue( "Given - is editable", cf.isEditable() );
        // Given - a contributed facet
        OtmContributedFacet contrib = co1.add( cf );
        testContributedFacet( contrib, cf, co1 );

        String co1Name = co1.getName();
        String cfName = cf.getName();
        String contribName = contrib.getName();

        // Then
        assertTrue( cfName.startsWith( co1Name ) );
        assertTrue( contribName.startsWith( co1Name ) );
        assertTrue( cfName.endsWith( baseCfName ) );
        assertTrue( contribName.endsWith( baseCfName ) );

        // When - moved to co2
        cf.setBaseType( co2 );
        String co2Name = co2.getName();
        cfName = cf.getName();
        contribName = contrib.getName();

        // Then
        assertTrue( cfName.startsWith( co2Name ) );
        assertTrue( contribName.startsWith( co2Name ) );
        assertTrue( cfName.endsWith( baseCfName ) );
        assertTrue( contribName.endsWith( baseCfName ) );
        assertTrue( "Must not have orginial name component.", !cfName.contains( co1Name ) );
        assertTrue( "Must not have orginial name component.", !contribName.contains( co1Name ) );

        // When - given a new name
        final String NEWNAME = "SomethingNew";
        cf.setName( NEWNAME );

        cfName = cf.getName();
        contribName = contrib.getName();
        // Then
        assertTrue( cfName.startsWith( co2Name ) );
        assertTrue( contribName.startsWith( co2Name ) );
        assertTrue( cfName.endsWith( NEWNAME ) );
        assertTrue( contribName.endsWith( NEWNAME ) );
        assertTrue( "Must not have orginial name component.", !cfName.contains( baseCfName ) );
        assertTrue( "Must not have orginial name component.", !contribName.contains( baseCfName ) );

        // Then - nameProperty is upto date
        cfName = cf.nameProperty().get();
        contribName = contrib.nameProperty().get();
        // Then
        assertTrue( cfName.startsWith( co2Name ) );
        assertTrue( contribName.startsWith( co2Name ) );
        assertTrue( cfName.endsWith( NEWNAME ) );
        assertTrue( contribName.endsWith( NEWNAME ) );
        assertTrue( "Must not have orginial name component.", !cfName.contains( baseCfName ) );
        assertTrue( "Must not have orginial name component.", !contribName.contains( baseCfName ) );
    }

    /**
     * Test inheritance of contextual facets injected into contextual facets on both base object and extensions of that
     * base object.
     */
    @Test
    public void testInheritedNestedCFs() {
        // Given two libraries
        OtmLibrary baseLib = TestLibrary.buildOtm();
        OtmModelManager mgr = baseLib.getModelManager();
        OtmLibrary exLib = TestLibrary.buildOtm( mgr, baseLib.getBaseNamespace() + "/ex", "ex", "ExtensionLib" );

        // Given two Business Objects
        OtmBusinessObject baseBO = TestBusiness.buildOtm( baseLib, "BaseBO" );
        OtmBusinessObject exBO = TestBusiness.buildOtm( exLib, "ExBO" );
        exBO.setBaseType( baseBO );

        // Given two contextual facets, one injecting on the other on the base BO
        OtmCustomFacet baseCF = TestCustomFacet.buildOtm( baseBO, "BaseCF" );
        OtmCustomFacet exCF = TestCustomFacet.buildOtm( baseCF, "ExCF" );

        // Given Tests
        assertTrue( exBO.getBaseType() == baseBO );
        assertTrue( baseCF.getContributedObject() == baseBO );
        assertTrue( exCF.getContributedObject() == baseCF );

        // Issues
        // When saved and opened:
        // Member Tree: baseBO does not list exCF
        // Member Tree: baseCF does not list exCF
        // Member Tree: exBO does not list baseCF or exCF , baseCF is shown in properties tree
        // > OK - top level : currentModelMgr.getMembers(filter);
        // >> Recursion - childrenOwner.getChildrenTypeProviders()
        // >> Does NOT find exCF
        assertTrue( "Must have contributed facet child.", baseCF.getChildren().contains( exCF.getWhereContributed() ) );
        Collection<OtmTypeProvider> bcfKids = baseCF.getChildrenTypeProviders();
        assertTrue( "Must have contributed facet in type provider children list.",
            bcfKids.contains( exCF.getWhereContributed() ) );
        // assertTrue( "Missing injected facet as child.", bcfKids.contains( exCF ) );
        // Why - is isn't the children handler catching the contextual facet?

        //
        // Properties Tree: baseBO does not show exCF,
        // Properties Tree: baseCF does not show exCF,
        // Properties Tree: exBO does not show exCF, it does show baseCF inherited from base
        //
        // Graphics: TODO
        // When constructed in DEX, the properties and graphics displays do not show CFs
        // When saved and opened, display changes but not fully correct.
    }
}
