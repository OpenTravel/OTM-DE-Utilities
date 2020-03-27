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

package org.opentravel.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.objecteditor.UserSettings;

import javafx.stage.Stage;

/**
 * Handle requests for project related services.
 * 
 * @author dmh
 *
 */
public class DexStyleSheetHandler {
    private static Log log = LogFactory.getLog( DexStyleSheetHandler.class );

    /**
     * Known values for style sheets to apply across the application.
     */
    public enum Selector {
        SMALL("Small", "DavesViperSmall.css"),
        NORMAL("Normal", "DavesViper.css"),
        LARGE("Large", "DavesViperLarge.css");

        private String label;
        private String cssFileName;

        Selector(String label, String cssFileName) {
            this.label = label;
            this.cssFileName = cssFileName;
        }

        public static Selector getDefault() {
            return NORMAL;
        }

        public static Selector fromString(String label) {
            for (Selector s : values())
                if (s.label.equals( label ))
                    return s;
            return NORMAL; // Default
        }

        public static String[] labels() {
            String[] labels = new String[values().length];
            int i = 0;
            for (Selector v : values())
                labels[i++] = v.label;
            return labels;
        }
    }

    private Selector currentSelector = Selector.NORMAL;
    private UserSettings settings = null;

    public DexStyleSheetHandler(UserSettings settings) {
        this.currentSelector = Selector.fromString( settings.getDisplaySize() );
        this.settings = settings;
    }

    public String getLabel() {
        return currentSelector.label;
    }

    /**
     * Set the current style sheet, save in user settings and apply to the stage
     * 
     * @param stage can be null
     * @param selector
     */
    public void setStyleSheet(String selector, Stage stage) {
        if (selector != null)
            this.currentSelector = Selector.fromString( selector );
        else
            this.currentSelector = Selector.getDefault();

        if (settings != null) {
            settings.setDisplaySize( currentSelector.label );
            settings.save();
        }
        apply( stage );
    }

    /**
     * Apply the currently selected style sheet to this stage.
     * 
     * @param stage
     */
    public void apply(Stage stage) {
        if (stage != null) {
            stage.getScene().getStylesheets().clear();
            stage.getScene().getStylesheets().add( currentSelector.cssFileName );
        }
    }
}
