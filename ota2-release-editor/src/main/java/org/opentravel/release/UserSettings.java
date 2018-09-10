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
package org.opentravel.release;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.opentravel.application.common.AbstractUserSettings;

/**
 * Persists settings for the OTM Release Editor application between sessions.
 */
public class UserSettings extends AbstractUserSettings {
	
	private static final String USER_SETTINGS_FILE = "/.ota2/.otr-settings.properties";
	
	private static File settingsFile = new File( System.getProperty( "user.home" ), USER_SETTINGS_FILE );
	
	private File releaseFolder;
	
	/**
	 * Returns the user settings from the prior session.  If no prior settings exist,
	 * default settings are returned.
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
				
			} catch(Throwable t) {
				t.printStackTrace( System.out );
				System.out.println("Error loading settings from prior session (using defaults).");
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
			
		} catch(IOException e) {
			System.out.println("Error saving user settings...");
			e.printStackTrace( System.out );
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
		settings.setReleaseFolder( new File( userHomeDirectory ) );
		return settings;
	}

	/**
	 * @see org.opentravel.application.common.AbstractUserSettings#load(java.util.Properties)
	 */
	@Override
	protected void load(Properties settingsProps) {
		String currentFolder = System.getProperty( "user.dir" );
		String releaseFolder = settingsProps.getProperty( "releaseFolder", currentFolder );
		
		setReleaseFolder( new File( releaseFolder ) );
		super.load( settingsProps );
	}

	/**
	 * @see org.opentravel.application.common.AbstractUserSettings#save(java.util.Properties)
	 */
	@Override
	protected void save(Properties settingsProps) {
		String currentFolder = System.getProperty( "user.dir" );
		String rFolder = (releaseFolder == null) ? currentFolder : releaseFolder.getAbsolutePath();
		
		settingsProps.put( "releaseFolder", rFolder );
		super.save( settingsProps );
	}

	/**
	 * Returns the location of the release folder.
	 *
	 * @return File
	 */
	public File getReleaseFolder() {
		return releaseFolder;
	}

	/**
	 * Assigns the location of the release folder.
	 *
	 * @param releaseFolder  the folder location to assign
	 */
	public void setReleaseFolder(File releaseFolder) {
		this.releaseFolder = releaseFolder;
	}

}
