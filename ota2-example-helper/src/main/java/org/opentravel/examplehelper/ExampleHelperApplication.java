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

package org.opentravel.examplehelper;

import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.application.common.AbstractUserSettings;

/**
 * JavaFX application for the OTA2 Example Helpere Utility.
 */
public class ExampleHelperApplication extends AbstractOTMApplication {

    /**
     * Main method invoked from the command-line.
     * 
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        launch( args );
    }

    /**
     * @see org.opentravel.application.common.AbstractOTMApplication#getMainWindowFxmlLocation()
     */
    @Override
    protected String getMainWindowFxmlLocation() {
        return ExampleHelperController.FXML_FILE;
    }

    /**
     * @see org.opentravel.application.common.AbstractOTMApplication#getUserSettings()
     */
    @Override
    protected AbstractUserSettings getUserSettings() {
        return UserSettings.load();
    }

    /**
     * @see org.opentravel.application.common.AbstractOTMApplication#getMainWindowTitle()
     */
    @Override
    protected String getMainWindowTitle() {
        return "OTM-DE Example Helper";
    }

}
