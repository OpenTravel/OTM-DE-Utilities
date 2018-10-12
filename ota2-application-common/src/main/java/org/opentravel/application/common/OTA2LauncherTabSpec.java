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

/**
 * Specifies the name and position of a tab in the OTA2 launcher application.
 */
public class OTA2LauncherTabSpec implements Comparable<OTA2LauncherTabSpec> {
	
	public static final OTA2LauncherTabSpec RELEASED_TAB     = new OTA2LauncherTabSpec( "Released Apps", 10 );
	public static final OTA2LauncherTabSpec EXPERIMENTAL_TAB = new OTA2LauncherTabSpec( "Experimental Apps", 20 );
	
	private String name;
	private int priority;
	
	/**
	 * Full constructor.
	 * 
	 * @param name  the name of the launcher tab
	 * @param priority  the priority that indicates the order of tabs in the launcher
	 */
	public OTA2LauncherTabSpec(String name, int priority) {
		this.name = name;
		this.priority = priority;
	}

	/**
	 * Returns the name of the launcher tab.
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the priority that indicates the order of tabs in the launcher.
	 *
	 * @return int
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return priority;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof OTA2LauncherTabSpec)
				&& (this.compareTo( (OTA2LauncherTabSpec) obj ) == 0);
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(OTA2LauncherTabSpec other) {
		int result;
		
		if (other == null) {
			result = 1;
		} else if (this.priority == other.priority) {
			if (other.name == null) {
				result = 1;
			} else if (this.name == null) {
				result = -1;
			} else {
				result = this.name.compareTo( other.name );
			}
		} else {
			result = (this.priority < other.priority) ? -1 : 1;
		}
		return result;
	}
	
}
