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
package org.opentravel.application.common;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Properties;

/**
 * Base class for user settings files that maintains common properties for window
 * location and dimensions.
 */
public abstract class AbstractUserSettings {
	
	private Point windowPosition;
	private Dimension windowSize;
	
	/**
	 * Loads the common user settings from the given properties.
	 * 
	 * @param settingsProps  the properties that contain the common settings
	 */
	protected void load(Properties settingsProps) {
		Point defaultPosition = getDefaultWindowPosition();
		Dimension defaultSize = getDefaultWindowSize();
		int windowPositionX = Integer.parseInt( settingsProps.getProperty( "windowPositionX", defaultPosition.x + "" ) );
		int windowPositionY = Integer.parseInt( settingsProps.getProperty( "windowPositionY", defaultPosition.y + "" ) );
		int windowWidth = Integer.parseInt( settingsProps.getProperty( "windowWidth", defaultSize.width + "" ) );
		int windowHeight = Integer.parseInt( settingsProps.getProperty( "windowHeight", defaultSize.height + "" ) );
		
		setWindowPosition( new Point( windowPositionX, windowPositionY ) );
		setWindowSize( new Dimension( windowWidth, windowHeight ) );
	}
	
	/**
	 * Saves the common user settings from the given properties.
	 * 
	 * @param settingsProps  the properties to which the common settings will be saved
	 */
	protected void save(Properties settingsProps) {
		Point windowPosition = (this.windowPosition == null) ? getDefaultWindowPosition() : this.windowPosition;
		Dimension windowSize = (this.windowSize == null) ? getDefaultWindowSize() : this.windowSize;
		
		settingsProps.put( "windowPositionX", windowPosition.x + "" );
		settingsProps.put( "windowPositionY", windowPosition.y + "" );
		settingsProps.put( "windowWidth", windowSize.width + "" );
		settingsProps.put( "windowHeight", windowSize.height + "" );
	}
	
	/**
	 * Saves the settings in the user's home directory.
	 */
	public abstract void save();
	
	/**
	 * Returns the location of the application window.
	 *
	 * @return Point
	 */
	public Point getWindowPosition() {
		return windowPosition;
	}

	/**
	 * Assigns the location of the application window.
	 *
	 * @param windowPosition  the window position to assign
	 */
	public void setWindowPosition(Point windowPosition) {
		this.windowPosition = windowPosition;
	}
	
	/**
	 * Returns the default location of the application window position.
	 * 
	 * @return Point
	 */
	protected Point getDefaultWindowPosition() {
		return new Point( 0, 0 );
	}
	
	/**
	 * Returns the size of the application window.
	 *
	 * @return Dimension
	 */
	public Dimension getWindowSize() {
		return windowSize;
	}

	/**
	 * Assigns the size of the application window.
	 *
	 * @param windowSize  the window size to assign
	 */
	public void setWindowSize(Dimension windowSize) {
		this.windowSize = windowSize;
	}

	/**
	 * Returns the default size of the application window.
	 *
	 * @return Dimension
	 */
	public Dimension getDefaultWindowSize() {
		return new Dimension( 800, 600 );
	}

}
