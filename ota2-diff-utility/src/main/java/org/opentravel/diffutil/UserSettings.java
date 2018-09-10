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
package org.opentravel.diffutil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.opentravel.application.common.AbstractUserSettings;
import org.opentravel.schemacompiler.diff.ModelCompareOptions;

/**
 * Persists settings for the <code>Diff-Utility</code> application between sessions.
 */
public class UserSettings extends AbstractUserSettings {
	
	private static final String USER_SETTINGS_FILE = "/.ota2/.du-settings.properties";
	
	private static File settingsFile = new File( System.getProperty( "user.home" ), USER_SETTINGS_FILE );
	
	private File oldProjectFolder;
	private File newProjectFolder;
	private File oldLibraryFolder;
	private File newLibraryFolder;
	private File reportFolder;
	private ModelCompareOptions compareOptions = new ModelCompareOptions();
	
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
		settings.setOldProjectFolder( new File( userHomeDirectory ) );
		settings.setNewProjectFolder( new File( userHomeDirectory ) );
		settings.setOldLibraryFolder( new File( userHomeDirectory ) );
		settings.setNewLibraryFolder( new File( userHomeDirectory ) );
		settings.compareOptions = new ModelCompareOptions();
		return settings;
	}

	/**
	 * @see org.opentravel.application.common.AbstractUserSettings#load(java.util.Properties)
	 */
	@Override
	protected void load(Properties settingsProps) {
		String currentFolder = System.getProperty( "user.dir" );
		String opFolder = settingsProps.getProperty( "oldProjectFolder", currentFolder );
		String npFolder = settingsProps.getProperty( "newProjectFolder", currentFolder );
		String olFolder = settingsProps.getProperty( "oldLibraryFolder", currentFolder );
		String nlFolder = settingsProps.getProperty( "newLibraryFolder", currentFolder );
		String reportFolder = settingsProps.getProperty( "reportFolder", currentFolder );
		
		setOldProjectFolder( new File( opFolder ) );
		setNewProjectFolder( new File( npFolder ) );
		setOldLibraryFolder( new File( olFolder ) );
		setNewLibraryFolder( new File( nlFolder ) );
		setReportFolder( new File( reportFolder ) );
		compareOptions.loadOptions( settingsProps );
		super.load( settingsProps );
	}

	/**
	 * @see org.opentravel.application.common.AbstractUserSettings#save(java.util.Properties)
	 */
	@Override
	protected void save(Properties settingsProps) {
		String currentFolder = System.getProperty( "user.dir" );
		String opFolder = (oldProjectFolder == null) ? currentFolder : oldProjectFolder.getAbsolutePath();
		String npFolder = (newProjectFolder == null) ? currentFolder : newProjectFolder.getAbsolutePath();
		String olFolder = (oldLibraryFolder == null) ? currentFolder : oldLibraryFolder.getAbsolutePath();
		String nlFolder = (newLibraryFolder == null) ? currentFolder : newLibraryFolder.getAbsolutePath();
		String rptFolder = (reportFolder == null) ? currentFolder : reportFolder.getAbsolutePath();
		
		settingsProps.put( "oldProjectFolder", opFolder );
		settingsProps.put( "newProjectFolder", npFolder );
		settingsProps.put( "oldLibraryFolder", olFolder );
		settingsProps.put( "newLibraryFolder", nlFolder );
		settingsProps.put( "reportFolder", rptFolder );
		compareOptions.saveOptions( settingsProps );
		super.save( settingsProps );
	}

	/**
	 * Returns the location of the old project folder.
	 *
	 * @return File
	 */
	public File getOldProjectFolder() {
		return oldProjectFolder;
	}

	/**
	 * Assigns the location of the old project folder.
	 *
	 * @param oldProjectFolder  the folder location to assign
	 */
	public void setOldProjectFolder(File oldProjectFolder) {
		this.oldProjectFolder = oldProjectFolder;
	}

	/**
	 * Returns the location of the new project folder.
	 *
	 * @return File
	 */
	public File getNewProjectFolder() {
		return newProjectFolder;
	}

	/**
	 * Assigns the location of the new project folder.
	 *
	 * @param newProjectFolder  the folder location to assign
	 */
	public void setNewProjectFolder(File newProjectFolder) {
		this.newProjectFolder = newProjectFolder;
	}

	/**
	 * Returns the location of the old library folder.
	 *
	 * @return File
	 */
	public File getOldLibraryFolder() {
		return oldLibraryFolder;
	}

	/**
	 * Assigns the location of the old library folder.
	 *
	 * @param oldLibraryFolder  the folder location to assign
	 */
	public void setOldLibraryFolder(File oldLibraryFolder) {
		this.oldLibraryFolder = oldLibraryFolder;
	}

	/**
	 * Returns the location of the new library folder.
	 *
	 * @return File
	 */
	public File getNewLibraryFolder() {
		return newLibraryFolder;
	}

	/**
	 * Assigns the location of the new library folder.
	 *
	 * @param newLibraryFolder  the folder location to assign
	 */
	public void setNewLibraryFolder(File newLibraryFolder) {
		this.newLibraryFolder = newLibraryFolder;
	}

	/**
	 * Returns the location of the report folder.
	 *
	 * @return File
	 */
	public File getReportFolder() {
		return reportFolder;
	}

	/**
	 * Assigns the location of the report folder.
	 *
	 * @param reportFolder  the folder location to assign
	 */
	public void setReportFolder(File reportFolder) {
		this.reportFolder = reportFolder;
	}

	/**
	 * Returns the options that should be applied when comparing two OTM models,
	 * libraries, or entities.
	 * 
	 * @return ModelCompareOptions
	 */
	public ModelCompareOptions getCompareOptions() {
		return compareOptions;
	}
	
}
