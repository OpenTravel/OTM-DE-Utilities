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

import org.opentravel.application.common.AbstractUserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Persists settings for the <code>Diff-Utility</code> application between sessions.
 */
public class UserSettings extends AbstractUserSettings {

    private static final String USER_SETTINGS_FILE = "/.ota2/.uh-settings.properties";

    private static final Logger log = LoggerFactory.getLogger( UserSettings.class );

    private static File settingsFile = new File( System.getProperty( "user.home" ), USER_SETTINGS_FILE );

    private File projectFolder;
    private File outputFolder;

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
                log.error( "Error loading settings from prior session (using defaults).", e );
                settings = getDefaultSettings();
            }


        }
        return settings;
    }

    /**
     * @see org.opentravel.application.common.AbstractUserSettings#load(java.util.Properties)
     */
    @Override
    protected void load(Properties settingsProps) {
        String currentFolder = System.getProperty( "user.dir" );
        String pFolder = settingsProps.getProperty( "projectFolder", currentFolder );
        String oFolder = settingsProps.getProperty( "outputFolder", currentFolder );
    
        setProjectFolder( new File( pFolder ) );
        setOutputFolder( new File( oFolder ) );
        super.load( settingsProps );
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
            log.error( "Error saving user settings", e );
        }
    }

    /**
     * @see org.opentravel.application.common.AbstractUserSettings#save(java.util.Properties)
     */
    @Override
    protected void save(Properties settingsProps) {
        String currentFolder = System.getProperty( "user.dir" );
        String pFolder = (projectFolder == null) ? currentFolder : projectFolder.getAbsolutePath();
        String oFolder = (outputFolder == null) ? currentFolder : outputFolder.getAbsolutePath();
    
        settingsProps.put( "projectFolder", pFolder );
        settingsProps.put( "outputFolder", oFolder );
        super.save( settingsProps );
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
        settings.setProjectFolder( new File( userHomeDirectory ) );
        settings.setOutputFolder( new File( userHomeDirectory ) );
        return settings;
    }

    /**
     * @see org.opentravel.application.common.AbstractUserSettings#getDefaultWindowSize()
     */
    @Override
    public Dimension getDefaultWindowSize() {
        return new Dimension( 625, 400 );
    }

    /**
     * Returns the location of the project folder.
     *
     * @return File
     */
    public File getProjectFolder() {
        return projectFolder;
    }

    /**
     * Assigns the location of the project folder.
     *
     * @param projectFolder the folder location to assign
     */
    public void setProjectFolder(File projectFolder) {
        this.projectFolder = projectFolder;
    }

    /**
     * Returns the location of the output folder.
     *
     * @return File
     */
    public File getOutputFolder() {
        return outputFolder;
    }

    /**
     * Assigns the location of the output folder.
     *
     * @param outputFolder the folder location to assign
     */
    public void setOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

}
