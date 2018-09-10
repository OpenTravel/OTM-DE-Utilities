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

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * Static utility methods for the Example Helper application.
 */
public class HelperUtils {
	
	/**
	 * Returns a display name label for the given OTM entity.
	 * 
	 * @param entity  the entity for which to return a display name
	 * @param showPrefix  flag indicating whether the owning library's prefix should be included in the label
	 * @return String
	 */
	public static String getDisplayName(NamedEntity entity, boolean showPrefix) {
		TLLibrary library = (TLLibrary) entity.getOwningLibrary();
		QName elementName = XsdCodegenUtils.getGlobalElementName(entity);
		String localName = (elementName != null) ? elementName.getLocalPart() : entity.getLocalName();
		StringBuilder displayName = new StringBuilder();
		
		if (showPrefix && (library.getPrefix() != null)) {
			displayName.append( library.getPrefix() ).append( ":" );
		}
		displayName.append( localName );
		
		return displayName.toString();
	}
	
}
