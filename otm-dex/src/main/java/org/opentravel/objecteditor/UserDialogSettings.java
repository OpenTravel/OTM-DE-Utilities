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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javafx.geometry.Dimension2D;

/**
 * Persists settings for the <code>Pop-up Dialog Windows</code> application between sessions.
 * <p>
 */
public class UserDialogSettings {
    private static final Logger log = LoggerFactory.getLogger( UserDialogSettings.class );

    private static final String DIMENSIONS_LABEL = "dimensions_";
    private static final String DEFAULT_DIMENSION = "default";
    private static final String DIMENSIONS_KEYS = "dimensions_keys";

    private static final double DEFAULT_WIDTH = 600;
    private static final double DEFAULT_HEIGHT = 400;

    /**
     * Returns the default user settings.
     */
    public static void getDefaultSettings(UserSettings settings) {
        settings.setDimensions( DEFAULT_DIMENSION, new Dimension2D( DEFAULT_WIDTH, DEFAULT_HEIGHT ) );
    }

    private Map<String,Dimension2D> dimensions = new HashMap<>();


    /**
     * Get the dimension with the label as key.
     * 
     * @param label
     * @return dimension or null
     */
    public Dimension2D get(String label) {
        return dimensions.get( label );
    }

    /**
     * Load the properties map from the settings
     */
    protected void load(Properties settingsProps) {

        // Get the key set listing each dialog window
        String[] keys = parse( settingsProps.getProperty( DIMENSIONS_KEYS ) );

        // Get the width and height for each window
        for (String key : keys) {
            double width = DEFAULT_WIDTH;
            double height = DEFAULT_HEIGHT;
            try {
                width = Double.parseDouble( settingsProps.getProperty( DIMENSIONS_LABEL + key + "_width" ) );
                height = Double.parseDouble( settingsProps.getProperty( DIMENSIONS_LABEL + key + "_height" ) );
            } catch (Exception e) {
                // NO-OP
            }
            dimensions.put( key, new Dimension2D( width, height ) );
        }
    }

    /**
     * Save the dimensions map into the settings
     */
    protected void save(Properties settingsProps) {
        // save the key set
        settingsProps.put( DIMENSIONS_KEYS, join() );

        // Save each of the dimensions
        for (Entry<String,Dimension2D> e : dimensions.entrySet()) {
            settingsProps.put( DIMENSIONS_LABEL + e.getKey() + "_height", e.getValue().getHeight() + "" );
            settingsProps.put( DIMENSIONS_LABEL + e.getKey() + "_width", e.getValue().getWidth() + "" );
        }
    }

    // Note: the : will be escaped in the file with a \
    private String join() {
        String keyString = "";
        for (String key : dimensions.keySet())
            if (keyString.isEmpty())
                keyString = key;
            else
                keyString += ":" + key;
        return keyString;
    }

    /**
     * Split the single key string into an array.
     * 
     * @param keys
     * @return
     */
    private String[] parse(String keys) {
        return keys.split( ":" );
    }

    /**
     * Put the dimension in the map using the label as the key.
     * 
     * @param label
     * @param dimension
     */
    public void put(String label, Dimension2D dimension) {
        dimensions.put( label, dimension );
    }

}
