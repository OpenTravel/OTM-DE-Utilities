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

package org.opentravel.upversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.testutil.AbstractRepositoryTest;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;
import java.util.Arrays;

/**
 * Verifies the functions of the <code>UpversionOrchestrator</code> class.
 */
public class TestUpversionOrchestrator extends AbstractRepositoryTest {

    protected static File repositoryConfig =
        new File( System.getProperty( "user.dir" ) + "/src/test/resources/ota2-repository-config.xml" );
    protected static RepositoryManager repoManager;

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestUpversionOrchestrator.class );
        startTestServer( "versions-repository", 9492, repositoryConfig, true, false, TestUpversionOrchestrator.class );
        repoManager = repositoryManager.get();
    }

    @AfterClass
    public static void tearDownTests() throws Exception {
        shutdownTestServer();
    }

    @Test
    public void testUpversionOrchestration() throws Exception {
        RepositoryItem libAItem = repoManager.getRepositoryItem(
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/orchestrator-test-a", "LibA_1_0_0.otm", "1.0.0" );
        RepositoryItem libBItem = repoManager.getRepositoryItem(
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/orchestrator-test-b", "LibB_1_0_0.otm", "1.0.0" );
        UpversionOrchestrator o = new UpversionOrchestrator();
        File outputFolder = new File( wipFolder.get(), "/upversion-output" );

        o.setRepositoryManager( repoManager );
        o.setOldVersions( Arrays.asList( libAItem, libBItem ) );
        o.setOutputFolder( outputFolder );
        o.setProjectFilename( "upversion-test.otp" );
        o.setProjectName( "upversion-test" );
        o.setProjectId( "upversion-test" );
        o.createNewVersions();

        // Perform some assertions to make sure the references in the new model version were adjusted properly
        File projectFile = new File( outputFolder, "/upversion-test.otp" );
        ProjectManager projectManager = new ProjectManager( new TLModel(), false, repoManager );
        ValidationFindings findings = new ValidationFindings();
        TLModel model = projectManager.getModel();

        projectManager.loadProject( projectFile, findings );
        Assert.assertFalse( findings.hasFinding( FindingType.ERROR ) );

        TLLibrary libAv2 = (TLLibrary) model
            .getLibrary( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/orchestrator-test-a/v02", "LibA" );
        TLLibrary libBv2 = (TLLibrary) model
            .getLibrary( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/orchestrator-test-b/v02", "LibB" );
        TLCoreObject internalRefs = libAv2.getCoreObjectType( "InternalRefs" );
        TLCoreObject externalRefs = libBv2.getCoreObjectType( "ExternalRefs" );

        assertNotNull( internalRefs );
        assertNotNull( externalRefs );
        validateReferences( internalRefs );
        validateReferences( externalRefs );
    }

    private void validateReferences(TLCoreObject coreRefs) throws Exception {
        TLProperty coreRef = coreRefs.getSummaryFacet().getElement( "TargetCore" );
        TLProperty choiceRef = coreRefs.getSummaryFacet().getElement( "TargetChoice" );
        TLProperty boRef = coreRefs.getSummaryFacet().getElement( "TargetBO" );
        String v2Str = "2.0.0";

        assertNotNull( coreRef );
        assertNotNull( choiceRef );
        assertNotNull( boRef );

        assertTrue( coreRef.getType() instanceof TLCoreObject );
        assertTrue( choiceRef.getType() instanceof TLChoiceObject );
        assertTrue( boRef.getType() instanceof TLBusinessObject );

        assertEquals( v2Str, coreRef.getType().getOwningLibrary().getVersion() );
        assertEquals( v2Str, choiceRef.getType().getOwningLibrary().getVersion() );
        assertEquals( v2Str, boRef.getType().getOwningLibrary().getVersion() );
    }

}
