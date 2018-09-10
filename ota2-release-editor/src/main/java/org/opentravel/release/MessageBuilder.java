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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility methods for constructing and formatting localized messages.
 */
public class MessageBuilder {
	
	private static final ResourceBundle messageBundle = ResourceBundle.getBundle( "ota2-release-messages", Locale.getDefault() );
	
	/**
	 * Returns the text for the given error or warning message.
	 * 
	 * @param messageKey  the message key for which to return the human-readable text
	 * @param messageParams  substitution parameters for the message
	 * @return String
	 */
	public static String formatMessage(String messageKey, Object... messageParams) {
		String formattedMessage;
		try {
			String messageText = messageBundle.getString( messageKey );
			
			if (messageText != null) {
				formattedMessage = MessageFormat.format( messageText, messageParams );
				
			} else {
				formattedMessage = messageKey;
			}
			
		} catch (MissingResourceException e) {
			formattedMessage = messageKey;
		}
		return formattedMessage;
	}
	
}
