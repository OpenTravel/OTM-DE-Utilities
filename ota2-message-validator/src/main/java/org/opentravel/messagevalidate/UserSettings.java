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

package org.opentravel.messagevalidate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.application.common.AbstractUserSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Persists settings for the <code>ExampleHelper</code> application between sessions.
 */
public class UserSettings extends AbstractUserSettings {

    private static final String USER_SETTINGS_FILE = "/.ota2/.mv-settings.properties";

    private static File settingsFile = new File( System.getProperty( "user.home" ), USER_SETTINGS_FILE );

    private static final Logger log = LogManager.getLogger( UserSettings.class );

    private File projectFolder;
    private File messageFolder;

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
     * @see org.opentravel.application.common.AbstractUserSettings#load(java.util.Properties)
     */
    @Override
    protected void load(Properties settingsProps) {
        String currentFolder = System.getProperty( "user.dir" );
        String prjFolder = settingsProps.getProperty( "projectFolder", currentFolder );
        String msgFolder = settingsProps.getProperty( "messageFolder", currentFolder );

        setProjectFolder( new File( prjFolder ) );
        setMessageFolder( new File( msgFolder ) );
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
            log.error( "Error saving user settings.", e );
        }
    }

    /**
     * @see org.opentravel.application.common.AbstractUserSettings#save(java.util.Properties)
     */
    @Override
    protected void save(Properties settingsProps) {
        String currentFolder = System.getProperty( "user.dir" );
        String pFolder = (projectFolder == null) ? currentFolder : projectFolder.getAbsolutePath();
        String mFolder = (messageFolder == null) ? currentFolder : messageFolder.getAbsolutePath();

        settingsProps.put( "projectFolder", pFolder );
        settingsProps.put( "messageFolder", mFolder );
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
        settings.setMessageFolder( new File( userHomeDirectory ) );
        return settings;
    }

    /**
     * Returns the folder location of the OTM project file.
     *
     * @return File
     */
    public File getProjectFolder() {
        return projectFolder;
    }

    /**
     * Assigns the folder location of the OTM project file.
     *
     * @param projectFolder the folder location to assign
     */
    public void setProjectFolder(File projectFolder) {
        this.projectFolder = projectFolder;
    }

    /**
     * Returns the folder location of the XML or JSON message to be validated.
     *
     * @return File
     */
    public File getMessageFolder() {
        return messageFolder;
    }

    /**
     * Assigns the folder location of the XML or JSON message to be validated.
     *
     * @param messageFolder the folder location to assign
     */
    public void setMessageFolder(File messageFolder) {
        this.messageFolder = messageFolder;
    }

}
