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

package org.opentravel.objecteditor;

import org.opentravel.application.common.AbstractUserSettings;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmContainers.OtmProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Persists settings for the <code>DE-x Object Editor</code> application between sessions.
 * <p>
 * To add a setting:
 * <ol>
 * <li>Define field
 * <li>Add getter and setter
 * <li>Add {@link #load(Properties)}
 * <li>Add {@link #save(Properties)}
 * <li>Add {@link #getDefaultSettings()}
 */
public class UserSettings extends AbstractUserSettings {

    private static final int EXAMPLE_MAX_REPEAT = 3;
    private static final int EXAMPLE_MAX_DEPTH = 3;


    public String getServiceEndpointUrl() {
        return serviceEndpointUrl;
    }

    public void setServiceEndpointUrl(String serviceEndpointUrl) {
        this.serviceEndpointUrl = serviceEndpointUrl;
    }

    public String getResourceBaseUrl() {
        return resourceBaseUrl;
    }

    public void setResourceBaseUrl(String resourceBaseUrl) {
        this.resourceBaseUrl = resourceBaseUrl;
    }

    public boolean isSuppressOtmExtensions() {
        return suppressOtmExtensions;
    }

    public void setSuppressOtmExtensions(boolean suppressOtmExtensions) {
        this.suppressOtmExtensions = suppressOtmExtensions;
    }

    public boolean isSuppressOptionalFields() {
        return suppressOptionalFields;
    }

    public void setSuppressOptionalFields(boolean suppressOptionalFields) {
        this.suppressOptionalFields = suppressOptionalFields;
    }

    public boolean isCompileSchemas() {
        return compileSchemas;
    }

    public void setCompileSchemas(boolean compileSchemas) {
        this.compileSchemas = compileSchemas;
    }

    public boolean isCompileJsonSchemas() {
        return compileJsonSchemas;
    }

    public void setCompileJsonSchemas(boolean compileJson) {
        this.compileJsonSchemas = compileJson;
    }

    public boolean isCompileServices() {
        return compileServices;
    }

    public void setCompileServices(boolean compileServices) {
        this.compileServices = compileServices;
    }

    public boolean isCompileSwagger() {
        return compileSwagger;
    }

    public void setCompileSwagger(boolean compileSwagger) {
        this.compileSwagger = compileSwagger;
    }

    public boolean isCompileHtml() {
        return compileHtml;
    }

    public void setCompileHtml(boolean compileHtml) {
        this.compileHtml = compileHtml;
    }

    public boolean isGenerateExamples() {
        return generateExamples;
    }

    public void setGenerateExamples(boolean generateExamples) {
        this.generateExamples = generateExamples;
    }

    public boolean isGenerateMaxDetailsForExamples() {
        return generateMaxDetailsForExamples;
    }

    public void setGenerateMaxDetailsForExamples(boolean generateMaxDetailsForExamples) {
        this.generateMaxDetailsForExamples = generateMaxDetailsForExamples;
    }

    public String getExampleContext() {
        return exampleContext;
    }


    public String getDisplaySize() {
        return displaySize;
    }

    public void setDisplaySize(String size) {
        displaySize = size;
    }

    /**
     * Default request payload
     */
    public OtmObject getDefaultRequestPayload(OtmModelManager mgr) {
        return mgr.getMember( defaultRequestPayload );
    }

    public void setDefaultRequestPayload(String nameWithPrefix) {
        this.defaultRequestPayload = nameWithPrefix;
    }

    public void setDefaultRequestPayload(OtmObject payload) {
        if (payload != null)
            setDefaultRequestPayload( payload.getNameWithPrefix() );
    }

    /**
     * Default resource mime types
     */
    public String getDefaultMimeTypes() {
        return defaultMimeTypes;
    }

    public void setDefaultMimeTypes(String values) {
        defaultMimeTypes = values;
    }

    /**
     * Default response payload
     */
    public OtmObject getDefaultResponsePayload(OtmModelManager mgr) {
        return mgr.getMember( defaultResponsePayload );
    }

    public void setDefaultResponsePayload(OtmObject payload) {
        setDefaultResponsePayload( payload.getNameWithPrefix() );
    }

    public void setDefaultResponsePayload(String nameWithPrefix) {
        this.defaultResponsePayload = nameWithPrefix;
    }

    public void setExampleContext(String exampleContext) {
        this.exampleContext = exampleContext;
    }

    public int getExampleMaxRepeat() {
        return exampleMaxRepeat;
    }

    public void setExampleMaxRepeat(int exampleMaxRepeat) {
        this.exampleMaxRepeat = exampleMaxRepeat;
    }

    public void setExampleMaxRepeat(String exampleMaxRepeatString) {
        try {
            if (exampleMaxRepeatString != null && !exampleMaxRepeatString.isEmpty())
                this.exampleMaxRepeat = Integer.parseInt( exampleMaxRepeatString );
        } catch (NumberFormatException e) {
            exampleMaxRepeat = EXAMPLE_MAX_REPEAT;
        }
    }

    public int getExampleMaxDepth() {
        return exampleMaxDepth;
    }

    public void setExampleMaxDepth(int exampleMaxDepth) {
        this.exampleMaxDepth = exampleMaxDepth;
    }

    public void setExampleMaxDepth(String exampleMaxDepthString) {
        try {
            if (exampleMaxDepthString != null && !exampleMaxDepthString.isEmpty())
                this.exampleMaxDepth = Integer.parseInt( exampleMaxDepthString );
        } catch (NumberFormatException e) {
            exampleMaxDepth = EXAMPLE_MAX_DEPTH;
        }
    }

    private static final String USER_SETTINGS_FILE = "/.ota2/.dex-settings.properties";
    private static final String PROJECT_DIRECTORY_LABEL = "lastProjectFolder";
    private static final String HIDE_PROJECT_OPEN_DIALOG = "hideOpenProjectDialog";
    private static final String PROJECT_ARRAY_LABEL = "recentlyUsedProject";
    private static final int MAX_PROJECTS = 9;

    private static File settingsFile = new File( System.getProperty( "user.home" ), USER_SETTINGS_FILE );
    private static final Logger log = LoggerFactory.getLogger( UserSettings.class );

    private int repeatCount;
    // private File lastModelFile;
    private File lastProjectFile;
    private boolean hideProjectOpenDialog;
    // Compiler Options
    private boolean compileSchemas = true;
    private boolean compileJsonSchemas = true;
    private boolean compileServices = true;
    private boolean compileSwagger = true;
    private boolean compileHtml = true;
    // private URL serviceLibraryUrl;
    private String serviceEndpointUrl;
    private String resourceBaseUrl;
    private boolean suppressOtmExtensions = false;
    private boolean generateExamples = true;
    private boolean generateMaxDetailsForExamples = true;

    private String defaultMimeTypes;
    private String defaultRequestPayload;
    private String defaultResponsePayload;

    private String exampleContext;
    private int exampleMaxRepeat;
    private int exampleMaxDepth;
    private boolean suppressOptionalFields = false;
    private String lastRepositoryId = "";
    private String displaySize = "Normal";

    private List<String> recentProjects = new ArrayList<>();


    public String getLastRepositoryId() {
        return lastRepositoryId;
    }

    public void setLastRepositoryId(String lastRepositoryId) {
        if (lastRepositoryId == null)
            lastRepositoryId = "";
        this.lastRepositoryId = lastRepositoryId;
    }

    /**
     * Returns the user settings from the prior session. If no prior settings exist, default settings are returned.
     * 
     * @return UserSettings
     */
    public static UserSettings load() {
        UserSettings settings;

        if (!settingsFile.exists()) {
            settings = getDefaultSettings();
        } else {
            try (InputStream is = new FileInputStream( settingsFile )) {
                Properties usProps = new Properties();

                usProps.load( is );
                settings = new UserSettings();
                settings.load( usProps );

            } catch (Exception e) {
                log.warn( "Error loading settings from prior session (using defaults).", e );
                settings = getDefaultSettings();
            }

        }
        return settings;
    }

    /**
     * @see org.opentravel.application.common.AbstractUserSettings#save()
     */
    @Override
    public void save() {
        if (!settingsFile.getParentFile().exists()) {
            settingsFile.getParentFile().mkdirs();
        }
        try (OutputStream out = new FileOutputStream( settingsFile )) {
            Properties usProps = new Properties();

            save( usProps );
            usProps.store( out, null );

        } catch (IOException e) {
            log.error( "Error saving user settings.", e );
        }
    }

    /**
     * Returns the default user settings.
     * 
     * @return UserSettings
     */
    public static UserSettings getDefaultSettings() {
        String userHomeDirectory = System.getProperty( "user.home" );
        UserSettings settings = new UserSettings();

        settings.setWindowPosition( settings.getDefaultWindowPosition() );
        settings.setWindowSize( settings.getDefaultWindowSize() );
        // settings.setRepeatCount( 2 );
        // settings.setLastModelFile( new File( userHomeDirectory + "/dummy-file.otm" ) );
        settings.setLastProjectFolder( new File( userHomeDirectory ) );
        // Nothing to do for recentProjects - it is an empty array
        settings.setRecentProject( null );

        settings.setHideOpenProjectDialog( false );
        settings.setLastRepositoryId( "" );
        settings.defaultMimeTypes = "APPLICATION_JSON;APPLICATION_XML";
        settings.defaultRequestPayload = "";
        settings.defaultResponsePayload = "";

        // Compiler Options
        settings.setCompileSchemas( true );
        settings.setCompileJsonSchemas( true );
        settings.setCompileServices( true );
        settings.setCompileSwagger( true );
        settings.setCompileHtml( true );
        settings.setResourceBaseUrl( "http://example.com/resource" );
        settings.setServiceEndpointUrl( "http://example.com/resource" );
        settings.setSuppressOtmExtensions( false );
        settings.setGenerateExamples( true );
        settings.setGenerateMaxDetailsForExamples( true );
        settings.setExampleContext( "example.com" );
        settings.setExampleMaxRepeat( EXAMPLE_MAX_REPEAT );
        settings.setExampleMaxDepth( EXAMPLE_MAX_DEPTH );
        settings.setSuppressOptionalFields( false );

        settings.setDisplaySize( "Normal" );
        return settings;
    }

    /**
     * @see org.opentravel.application.common.AbstractUserSettings#load(java.util.Properties)
     */
    @Override
    protected void load(Properties settingsProps) {
        // String modelFile = settingsProps.getProperty( "lastModelFile" );
        String projectFolder = settingsProps.getProperty( PROJECT_DIRECTORY_LABEL );
        setLastProjectFolder( (projectFolder == null) ? null : new File( projectFolder ) );
        loadRecentlyUsedProjects( settingsProps );

        String hideOpenProjectDialog = settingsProps.getProperty( HIDE_PROJECT_OPEN_DIALOG );
        setHideOpenProjectDialog( Boolean.valueOf( hideOpenProjectDialog ) );
        setLastRepositoryId( settingsProps.getProperty( "lastRepositoryId" ) );
        // int rptCount = Integer.parseInt( settingsProps.getProperty( "repeatCount" ) );
        // setRepeatCount( rptCount );

        // Resource defaults
        setDefaultMimeTypes( settingsProps.getProperty( "defaultMimeTypes" ) );
        setDefaultRequestPayload( settingsProps.getProperty( "defaultRequestPayload" ) );
        setDefaultResponsePayload( settingsProps.getProperty( "defaultResponsePayload" ) );

        // Compiler Options
        setCompileSchemas( Boolean.valueOf( settingsProps.getProperty( "compileSchemas" ) ) );
        setCompileJsonSchemas( Boolean.valueOf( settingsProps.getProperty( "compileJsonSchemas" ) ) );
        setCompileServices( Boolean.valueOf( settingsProps.getProperty( "compileServices" ) ) );
        setCompileSwagger( Boolean.valueOf( settingsProps.getProperty( "compileSwagger" ) ) );
        setCompileHtml( Boolean.valueOf( settingsProps.getProperty( "compileHtml" ) ) );
        setSuppressOtmExtensions( Boolean.valueOf( settingsProps.getProperty( "suppressOtmExtensions" ) ) );
        setGenerateExamples( Boolean.valueOf( settingsProps.getProperty( "generateExamples" ) ) );
        setSuppressOptionalFields( Boolean.valueOf( settingsProps.getProperty( "suppressOptionalFields" ) ) );
        setResourceBaseUrl( settingsProps.getProperty( "resourceBaseUrl" ) );
        setServiceEndpointUrl( settingsProps.getProperty( "serviceEndpointUrl" ) );
        setExampleContext( settingsProps.getProperty( "exampleContext" ) );
        setExampleMaxRepeat( settingsProps.getProperty( "exampleMaxRepeat" ) );
        setExampleMaxDepth( settingsProps.getProperty( "exampleMaxDepth" ) );

        setDisplaySize( settingsProps.getProperty( "displaySize" ) );

        // setLastModelFile( (modelFile == null) ? null : new File( modelFile ) );
        super.load( settingsProps );
    }

    /**
     * Load strings from settings into recentProjects array
     * 
     * @param settingsProps
     */
    private void loadRecentlyUsedProjects(Properties settingsProps) {
        String project = "";
        for (int i = 0; i < MAX_PROJECTS; i++) {
            project = settingsProps.getProperty( PROJECT_ARRAY_LABEL + i );
            if (project != null)
                recentProjects.add( project );
        }
    }

    /**
     * Save strings in recentProjects array into settings
     * 
     * @param settingsProps
     */
    private void saveRecentlyUsedProjects(Properties settingsProps) {
        String project = "";
        if (recentProjects.isEmpty())
            return;
        for (int i = 0; i < recentProjects.size(); i++) {
            project = recentProjects.get( i );
            if (!project.isEmpty())
                settingsProps.put( PROJECT_ARRAY_LABEL + i, project );
        }
    }

    // TODO - how to assure least recently used order?
    // List maintains insertion order
    /**
     * Set the project's absolute path into the top of the recentProjects array.
     * 
     * @param project
     */
    public void setRecentProject(OtmProject project) {
        // Get a string for the project
        if (project != null && project.getTL() != null && project.getTL().getProjectFile() != null) {
            String pf = project.getTL().getProjectFile().getAbsolutePath();
            if (recentProjects.contains( pf ))
                recentProjects.remove( pf ); // It will get added at front
            while (recentProjects.size() >= MAX_PROJECTS)
                recentProjects.remove( MAX_PROJECTS - 1 ); // remove from end
            if (recentProjects.size() < MAX_PROJECTS)
                recentProjects.add( 0, pf ); // set to front
        }
    }

    /**
     * Get Files from the saved absolute paths
     * 
     * @return
     */
    public List<File> getRecentProjects() {
        List<File> files = new ArrayList<>();
        for (String pf : recentProjects)
            files.add( new File( pf ) );
        return files;
    }

    /**
     * @see org.opentravel.application.common.AbstractUserSettings#save(java.util.Properties)
     */
    @Override
    protected void save(Properties settingsProps) {
        UserSettings defaultValues = getDefaultSettings();
        // String modelFile = (this.lastModelFile == null) ?
        // defaultValues.getLastModelFile().getAbsolutePath() : this.lastModelFile.getAbsolutePath();
        String projectFolder = (this.lastProjectFile == null) ? defaultValues.getLastProjectFolder().getAbsolutePath()
            : this.lastProjectFile.getAbsolutePath();
        putString( settingsProps, "lastRepositoryId", lastRepositoryId );

        saveRecentlyUsedProjects( settingsProps );

        // settingsProps.put( "repeatCount", repeatCount + "" );
        // settingsProps.put( "lastModelFile", modelFile );
        putString( settingsProps, PROJECT_DIRECTORY_LABEL, projectFolder );
        settingsProps.put( HIDE_PROJECT_OPEN_DIALOG, Boolean.toString( hideProjectOpenDialog ) );

        // Resource Defaults
        putString( settingsProps, "defaultMimeTypes", defaultMimeTypes );
        putString( settingsProps, "defaultResponsePayload", defaultResponsePayload );
        putString( settingsProps, "defaultRequestPayload", defaultRequestPayload );

        // Compiler Options
        settingsProps.put( "compileSchemas", Boolean.toString( compileSchemas ) );
        settingsProps.put( "compileJsonSchemas", Boolean.toString( compileJsonSchemas ) );
        settingsProps.put( "compileServices", Boolean.toString( compileServices ) );
        settingsProps.put( "compileSwagger", Boolean.toString( compileSwagger ) );
        settingsProps.put( "compileHtml", Boolean.toString( compileHtml ) );
        putString( settingsProps, "resourceBaseUrl", resourceBaseUrl );
        putString( settingsProps, "serviceEndpointUrl", serviceEndpointUrl );
        putString( settingsProps, "suppressOtmExtensions", Boolean.toString( suppressOtmExtensions ) );
        settingsProps.put( "generateExamples", Boolean.toString( generateExamples ) );
        settingsProps.put( "generateMaxDetailsForExamples", Boolean.toString( generateMaxDetailsForExamples ) );
        putString( settingsProps, "exampleContext", exampleContext );
        settingsProps.put( "exampleMaxRepeat", exampleMaxRepeat + "" );
        settingsProps.put( "exampleMaxDepth", exampleMaxDepth + "" );
        settingsProps.put( "suppressOptionalFields", Boolean.toString( suppressOptionalFields ) );

        putString( settingsProps, "displaySize", displaySize );

        super.save( settingsProps );
    }

    private void putString(Properties settingsProps, String key, String value) {
        if (key == null)
            return;
        if (value == null)
            value = "";
        settingsProps.put( key, value );
    }

    /**
     * Returns the value of the repeat-count spinner.
     *
     * @return int
     */
    public int getRepeatCount() {
        return repeatCount;
    }

    /**
     * Assigns the value of the repeat-count spinner.
     *
     * @param repeatCount the repeat count value to assign
     */
    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }
    //
    // /**
    // * Returns the file location of the last OTM model that was opened.
    // *
    // * @return File
    // */
    // public File getLastModelFile() {
    // return lastModelFile;
    // }
    //
    // /**
    // * Assigns the file location of the last OTM model that was opened.
    // *
    // * @param lastModelFile
    // * the file location to assign
    // */
    // public void setLastModelFile(File lastModelFile) {
    // this.lastModelFile = lastModelFile;
    // }

    /**
     * Returns the folder location where the last EXAMPLE file was saved.
     *
     * @return File
     */
    public File getLastProjectFolder() {
        return lastProjectFile;
    }

    public boolean getHideOpenProjectDialog() {
        return hideProjectOpenDialog;
    }

    /**
     * Assigns the folder location where the last EXAMPLE file was saved.
     *
     * @param lastExampleFolder the folder location to assign
     */
    public void setLastProjectFolder(File lastProjectFolder) {
        this.lastProjectFile = lastProjectFolder;
    }

    public void setHideOpenProjectDialog(boolean value) {
        this.hideProjectOpenDialog = value;
    }

}
