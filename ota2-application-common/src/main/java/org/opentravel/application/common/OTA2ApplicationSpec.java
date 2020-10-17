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

import javafx.scene.image.Image;

/**
 * Provides launcher configuration settings for an OTA2 application.
 */
public class OTA2ApplicationSpec implements Comparable<OTA2ApplicationSpec> {

    private String name;
    private String description;
    private int priority;
    private OTA2LauncherTabSpec launcherTab;
    private Image launchIcon;
    private String applicationClassname;
    private String libraryFolderPath;

    /**
     * Constructor that accepts the Java class that will be used to launch the application.
     * 
     * @param name the name of the application
     * @param description a brief description of the application
     * @param priority the priority that indicates the order of applications in the launcher tab
     * @param launcherTab the spec for the tab where the application's launch button should be displayed
     * @param launchIcon the icon to display on the launch button for the application
     * @param applicationClass the application class to be called when launching the application
     */
    public OTA2ApplicationSpec(String name, String description, int priority, OTA2LauncherTabSpec launcherTab,
        Image launchIcon, Class<? extends AbstractOTMApplication> applicationClass) {
        this( name, description, priority, launcherTab, launchIcon, applicationClass.getName(), null );
    }

    /**
     * Constructor that accepts the Java class that will be used to launch the application.
     * 
     * @param name the name of the application
     * @param description a brief description of the application
     * @param priority the priority that indicates the order of applications in the launcher tab
     * @param launcherTab the spec for the tab where the application's launch button should be displayed
     * @param launchIcon the icon to display on the launch button for the application
     * @param applicationClassname the fully-qualified name of the application class to be called when launching the
     *        application
     * @param libraryFolderPath the path of the library folder to use when launching the application
     */
    public OTA2ApplicationSpec(String name, String description, int priority, OTA2LauncherTabSpec launcherTab,
        Image launchIcon, String applicationClassname, String libraryFolderPath) {
        this.name = name;
        this.description = description;
        this.launcherTab = launcherTab;
        this.launchIcon = launchIcon;
        this.applicationClassname = applicationClassname;
        this.applicationClassname = applicationClassname;
    }

    /**
     * Returns the name of the application.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a brief description of the application.
     *
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the spec for the tab where the application's launch button should be displayed.
     *
     * @return OTA2LauncherTabSpec
     */
    public OTA2LauncherTabSpec getLauncherTab() {
        return launcherTab;
    }

    /**
     * Returns the icon to display on the launch button for the application.
     *
     * @return Image
     */
    public Image getLaunchIcon() {
        return launchIcon;
    }

    /**
     * Returns the application class to be called when launching the application.
     *
     * @return String
     */
    public String getApplicationClassname() {
        return applicationClassname;
    }

    /**
     * Returns the path of the library folder to use when launching the application.
     * 
     * @return String
     */
    public String getLibraryFolderPath() {
        return libraryFolderPath;
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
        return (obj instanceof OTA2ApplicationSpec) && (this.compareTo( (OTA2ApplicationSpec) obj ) == 0);
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(OTA2ApplicationSpec other) {
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
