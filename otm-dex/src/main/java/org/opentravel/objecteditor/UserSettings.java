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

import javafx.geometry.Dimension2D;

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
    private static final Logger log = LoggerFactory.getLogger( UserSettings.class );

    private static final String USER_SETTINGS_FILE = "/.ota2/.dex-settings.properties";
    private static final String PROJECT_DIRECTORY_LABEL = "lastProjectFolder";
    private static final String HIDE_PROJECT_OPEN_DIALOG = "hideOpenProjectDialog";
    private static final String PROJECT_ARRAY_LABEL = "recentlyUsedProject";
    private static final int MAX_PROJECTS = 9;

    private static final String GRAPHICS_DOMAINS = "graphicsDomains";
    private static final String GRAPHICS_SIZE = "graphicsSize";
    private static final String GRAPHICS_TRACKING = "graphicsTracking";


    private static File settingsFile = new File( System.getProperty( "user.home" ), USER_SETTINGS_FILE );


    // private File lastModelFile;
    private File lastProjectFile;
    private boolean hideProjectOpenDialog;
    private String lastRepositoryId = "";
    private String displaySize = "Normal";

    private List<String> recentProjects = new ArrayList<>();

    private UserDialogSettings dialogSettings = new UserDialogSettings();
    private UserCompilerSettings compilerSettings = new UserCompilerSettings();

    // Graphics settings
    private boolean graphicsDomains = false;
    private boolean graphicsTracking = true;
    private double graphicsSize = 5;

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
        settings.setLastProjectFolder( new File( userHomeDirectory ) );
        // Nothing to do for recentProjects - it is an empty array
        settings.setRecentProject( null );

        settings.setHideOpenProjectDialog( false );
        settings.setLastRepositoryId( "" );

        // Graphics Options
        settings.graphicsDomains = false;
        settings.graphicsTracking = true;
        settings.graphicsSize = 5;

        settings.setDisplaySize( "Normal" );

        UserCompilerSettings.getDefaultSettings( settings );
        UserDialogSettings.getDefaultSettings( settings );
        return settings;
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

    public String getDisplaySize() {
        return displaySize;
    }

    public UserCompilerSettings getCompilerSettings() {
        return compilerSettings;
    }

    public Dimension2D getDimensions(String label) {
        return dialogSettings.get( label );
    }

    public void setDimensions(String label, Dimension2D dimension) {
        dialogSettings.put( label, dimension );
    }

    public boolean getHideOpenProjectDialog() {
        return hideProjectOpenDialog;
    }

    /**
     * Returns the folder location where the last EXAMPLE file was saved.
     *
     * @return File
     */
    public File getLastProjectFolder() {
        return lastProjectFile;
    }

    public String getLastRepositoryId() {
        return lastRepositoryId;
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

    public boolean getGraphicsDomains() {
        return graphicsDomains;
    }

    public double getGraphicsSize() {
        return graphicsSize;
    }

    public boolean getGraphicsTracking() {
        return graphicsTracking;
    }

    /**
     * @see org.opentravel.application.common.AbstractUserSettings#load(java.util.Properties)
     */
    @Override
    protected void load(Properties settingsProps) {
        String projectFolder = settingsProps.getProperty( PROJECT_DIRECTORY_LABEL );
        setLastProjectFolder( (projectFolder == null) ? null : new File( projectFolder ) );
        loadRecentlyUsedProjects( settingsProps );

        String hideOpenProjectDialog = settingsProps.getProperty( HIDE_PROJECT_OPEN_DIALOG );
        setHideOpenProjectDialog( Boolean.valueOf( hideOpenProjectDialog ) );
        setLastRepositoryId( settingsProps.getProperty( "lastRepositoryId" ) );

        setDisplaySize( settingsProps.getProperty( "displaySize" ) );

        try {
            graphicsSize = Double.valueOf( settingsProps.getProperty( GRAPHICS_SIZE ) );
        } catch (Exception e) {
            graphicsSize = 5;
        }
        setGraphicsDomains( Boolean.valueOf( settingsProps.getProperty( GRAPHICS_DOMAINS ) ) );
        setGraphicsTracking( Boolean.valueOf( settingsProps.getProperty( GRAPHICS_TRACKING ) ) );

        compilerSettings.load( settingsProps );
        dialogSettings.load( settingsProps );

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

    private void putString(Properties settingsProps, String key, String value) {
        if (key == null)
            return;
        if (value == null)
            value = "";
        settingsProps.put( key, value );
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
     * @see org.opentravel.application.common.AbstractUserSettings#save(java.util.Properties)
     */
    @Override
    protected void save(Properties settingsProps) {
        UserSettings defaultValues = getDefaultSettings();
        String projectFolder = (this.lastProjectFile == null) ? defaultValues.getLastProjectFolder().getAbsolutePath()
            : this.lastProjectFile.getAbsolutePath();
        putString( settingsProps, "lastRepositoryId", lastRepositoryId );

        saveRecentlyUsedProjects( settingsProps );

        putString( settingsProps, PROJECT_DIRECTORY_LABEL, projectFolder );
        settingsProps.put( HIDE_PROJECT_OPEN_DIALOG, Boolean.toString( hideProjectOpenDialog ) );

        putString( settingsProps, "displaySize", displaySize );

        settingsProps.put( GRAPHICS_DOMAINS, Boolean.toString( graphicsDomains ) );
        settingsProps.put( GRAPHICS_SIZE, Double.toString( graphicsSize ) );
        settingsProps.put( GRAPHICS_TRACKING, Boolean.toString( graphicsTracking ) );

        compilerSettings.save( settingsProps );
        dialogSettings.save( settingsProps );

        super.save( settingsProps );
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

    public void setDisplaySize(String size) {
        displaySize = size;
    }

    public void setGraphicsDomains(boolean value) {
        this.graphicsDomains = value;
    }

    public void setGraphicsSize(double value) {
        this.graphicsSize = value;
    }

    public void setGraphicsTracking(boolean value) {
        this.graphicsTracking = value;
    }

    public void setHideOpenProjectDialog(boolean value) {
        this.hideProjectOpenDialog = value;
    }

    /**
     * Assigns the folder location where the last EXAMPLE file was saved.
     *
     * @param lastExampleFolder the folder location to assign
     */
    public void setLastProjectFolder(File lastProjectFolder) {
        this.lastProjectFile = lastProjectFolder;
    }

    public void setLastRepositoryId(String lastRepositoryId) {
        if (lastRepositoryId == null)
            lastRepositoryId = "";
        this.lastRepositoryId = lastRepositoryId;
    }

    /**
     * Set the project's absolute path into the top of the recentProjects array.
     * <p>
     * List maintains insertion order
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
}
