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

package org.opentravel.dex.tasks.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.tasks.DexTaskBase;
import org.opentravel.dex.tasks.DexTaskSingleton;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.objecteditor.UserCompilerSettings;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.task.CompileAllCompilerTask;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;

/**
 * A JavaFX task for compiling a project
 * 
 * @author dmh
 *
 */
public class CompileProjectTask extends DexTaskBase<OtmProject> implements DexTaskSingleton {
    private static Log log = LogFactory.getLog( CompileProjectTask.class );
    private static final String COMPILER_SUFFIX = "CompilerOutput";

    private OtmProject selectedProject = null;
    private File targetFile = null;
    private UserSettings userSettings = null;
    private ValidationFindings findings = null;

    /**
     * Create compile project task.
     * 
     * @param taskData - a project to compile
     * @param handler - results handler
     * @param status - a status controller that can post message and progress indicator
     */
    public CompileProjectTask(OtmProject taskData, TaskResultHandlerI handler, DexStatusController status) {
        super( taskData, handler, status );

        selectedProject = taskData;

        // Replace start message from super-type.
        msgBuilder = new StringBuilder( "Compiling: " );
        msgBuilder.append( taskData.getName() );
        updateMessage( msgBuilder.toString() );
    }

    public CompileProjectTask(OtmProject taskData, TaskResultHandlerI handler, DexStatusController status,
        String folderName, UserSettings userSettings) {
        this( taskData, handler, status );

        targetFile = new File( folderName );
        createCompileDirectory( targetFile );
        this.userSettings = userSettings;
    }

    @Override
    public void doIT() throws Exception {
        if (targetFile != null && userSettings != null) {
            findings = compile( targetFile, selectedProject, userSettings );
        }
    }

    public ValidationFindings getFindings() {
        return findings;
    }

    public static String getCompileDirectoryPath(OtmProject project) {
        String directoryName = "";
        if (project != null) {
            directoryName = project.getTL().getProjectFile().getAbsolutePath();
            // strip .otp
            directoryName = directoryName.substring( 0, directoryName.length() - 4 );
            directoryName += "_" + COMPILER_SUFFIX;
        }
        return directoryName;
    }

    public static File getCompileDirectory(OtmProject project) {
        File targetFolder = null;
        targetFolder = new File( getCompileDirectoryPath( project ) );
        return targetFolder;
    }

    public static File createCompileDirectory(File targetFolder) {
        if (!targetFolder.exists()) {
            if (!targetFolder.mkdirs()) {
                log.warn( "Could not make directory: " + targetFolder );
                // return "Error. Could not make directory " + targetFolder.getPath() + " for the compiled output.";
            }
        }
        return targetFolder;
    }

    /**
     * {@link CompileAllCompilerTask#applyTaskOptions }
     * 
     * @param targetFolder
     * @param userSettings
     * @return
     * @throws Exception
     */
    public static ValidationFindings compile(File targetFolder, OtmProject project, UserSettings userSettings)
        throws Exception {
        ValidationFindings lastCompileFindings = new ValidationFindings();
        CompileAllCompilerTask codegenTask = new CompileAllCompilerTask();
        setOptions( codegenTask, userSettings );
        codegenTask.setOutputFolder( targetFolder.getAbsolutePath() );
        try {
            lastCompileFindings = codegenTask.compileOutput( project.getTL() );
        } catch (final SchemaCompilerException e) {
            // log.debug( "Error: Could not compile - " + e.getMessage() );
            // log.debug( "Error: Could not compile localized - " + e.getLocalizedMessage() );
            // log.debug( "Error: Could not compile cause - " + e.getCause() );
            throw (new Exception( "Could not compile - " + e.getMessage() ));
        } catch (final Exception e) {
            // log.debug( "Error: Could not compile , unknown error occurred - " + e.getMessage() );
            throw (new Exception( "Could not compile - " + e.getMessage() ));
        }
        // for (String s : lastCompileFindings.getAllValidationMessages( FindingMessageFormat.DEFAULT ))
        // log.debug( s );
        // log.debug( "Compiled " + project.getName() + " into " + targetFolder.getPath() );
        return lastCompileFindings;
    }

    public static void setOptions(CompileAllCompilerTask options, UserSettings userSettings) {
        if (options != null && userSettings != null) {
            UserCompilerSettings compilerSettings = userSettings.getCompilerSettings();
            options.setCompileSchemas( compilerSettings.isCompileSchemas() );
            options.setCompileServices( compilerSettings.isCompileServices() );
            options.setCompileJsonSchemas( compilerSettings.isCompileJsonSchemas() );
            options.setCompileSwagger( compilerSettings.isCompileSwagger() );
            options.setCompileHtml( compilerSettings.isCompileHtml() );
            options.setServiceEndpointUrl( compilerSettings.getServiceEndpointUrl() );
            options.setResourceBaseUrl( compilerSettings.getResourceBaseUrl() );
            options.setSuppressOtmExtensions( compilerSettings.isSuppressOtmExtensions() );
            options.setGenerateExamples( compilerSettings.isGenerateExamples() );
            options.setGenerateMaxDetailsForExamples( compilerSettings.isGenerateMaxDetailsForExamples() );
            options.setExampleMaxRepeat( compilerSettings.getExampleMaxRepeat() );
            options.setExampleMaxDepth( compilerSettings.getExampleMaxDepth() );
            options.setSuppressOptionalFields( compilerSettings.isSuppressOptionalFields() );
        }
    }
}
