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

package org.opentravel.launcher;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Utility methods for constructing and formatting localized messages.
 */
public class MessageBuilder {

    private static final ResourceBundle messageBundle =
        ResourceBundle.getBundle( "ota2-app-launcher", Locale.getDefault() );
    private static final String MESSAGE_DEFAULTS_FILE = "/message-defaults.properties";
    private static final Map<String,String> messageDefaults;

    /**
     * Private constructor to prevent instantiation.
     */
    private MessageBuilder() {}

    /**
     * Returns the text for the given error or warning message.
     * 
     * @param messageKey the message key for which to return the human-readable text
     * @param messageParams substitution parameters for the message
     * @return String
     */
    public static String formatMessage(String messageKey, Object... messageParams) {
        String formattedMessage;
        try {
            String messageText = messageBundle.getString( messageKey );

            if (messageText != null) {
                for (Entry<String,String> defaultValue : messageDefaults.entrySet()) {
                    messageText = messageText.replace( defaultValue.getKey(), defaultValue.getValue() );
                }
                formattedMessage = MessageFormat.format( messageText, messageParams );

            } else {
                formattedMessage = messageKey;
            }

        } catch (MissingResourceException e) {
            formattedMessage = messageKey;
        }
        return formattedMessage;
    }

    /**
     * Returns a user-displayable name for the given class.
     * 
     * @param clazz the class for which to return a display name
     * @return String
     */
    public static String getDisplayName(Class<?> clazz) {
        return formatMessage( clazz.getSimpleName() + ".displayName" );
    }

    /**
     * Returns a default value for the property with the specified name.
     * 
     * @param propertyName the name of the property for which to return a default value
     * @return String
     */
    public static String getDefaultValue(String propertyName) {
        return formatMessage( propertyName + ".default" );
    }

    /**
     * Initializes the map of default message substitution values. These values are only used in a development
     * environment when the property substitution has not yet occurred.
     */
    static {
        try (InputStream is = MessageBuilder.class.getResourceAsStream( MESSAGE_DEFAULTS_FILE )) {
            Map<String,String> msgDefaults = new HashMap<>();
            Properties defaultProps = new Properties();

            defaultProps.load( is );

            for (Entry<Object,Object> entry : defaultProps.entrySet()) {
                msgDefaults.put( "${" + entry.getKey() + "}", (String) entry.getValue() );
            }
            messageDefaults = Collections.unmodifiableMap( msgDefaults );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
