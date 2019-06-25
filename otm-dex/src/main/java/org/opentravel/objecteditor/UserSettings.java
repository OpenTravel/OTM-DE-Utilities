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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Persists settings for the <code>DE-x Object Editor</code> application between sessions.
 */
public class UserSettings extends AbstractUserSettings {

    private static final String USER_SETTINGS_FILE = "/.ota2/.dex-settings.properties";
    private static final String PROJECT_DIRECTORY_LABEL = "lastProjectFolder";

    private static File settingsFile = new File( System.getProperty( "user.home" ), USER_SETTINGS_FILE );
    private static final Logger log = LoggerFactory.getLogger( UserSettings.class );

    // private int repeatCount;
    // private File lastModelFile;
    private File lastProjectFile;

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
        return settings;
    }

    /**
     * @see org.opentravel.application.common.AbstractUserSettings#load(java.util.Properties)
     */
    @Override
    protected void load(Properties settingsProps) {
        // int rptCount = Integer.parseInt( settingsProps.getProperty( "repeatCount" ) );
        // String modelFile = settingsProps.getProperty( "lastModelFile" );
        String projectFolder = settingsProps.getProperty( PROJECT_DIRECTORY_LABEL );

        // setRepeatCount( rptCount );
        // setLastModelFile( (modelFile == null) ? null : new File( modelFile ) );
        setLastProjectFolder( (projectFolder == null) ? null : new File( projectFolder ) );
        super.load( settingsProps );
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

        // settingsProps.put( "repeatCount", repeatCount + "" );
        // settingsProps.put( "lastModelFile", modelFile );
        settingsProps.put( PROJECT_DIRECTORY_LABEL, projectFolder );
        super.save( settingsProps );
    }

    // /**
    // * Returns the value of the repeat-count spinner.
    // *
    // * @return int
    // */
    // public int getRepeatCount() {
    // return repeatCount;
    // }
    //
    // /**
    // * Assigns the value of the repeat-count spinner.
    // *
    // * @param repeatCount
    // * the repeat count value to assign
    // */
    // public void setRepeatCount(int repeatCount) {
    // this.repeatCount = repeatCount;
    // }
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

    /**
     * Assigns the folder location where the last EXAMPLE file was saved.
     *
     * @param lastExampleFolder the folder location to assign
     */
    public void setLastProjectFolder(File lastProjectFolder) {
        this.lastProjectFile = lastProjectFolder;
    }

}
