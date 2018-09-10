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

/**
 * Encapsulates a single property of a <code>TreeNode</code>.
 */
public class NodeProperty {
	
	private String name;
	private String value;
	
	/**
	 * Constructor that specifies the property name and value.
	 * 
	 * @param propertyNameKey  the resource bundle key for the property name
	 * @param valueProvider  provides the value for the node property
	 */
	public NodeProperty(String propertyNameKey, ValueProvider valueProvider) {
		this.name = MessageBuilder.formatMessage( "propertyName." + propertyNameKey );
		
		try {
			this.value = valueProvider.getValue();
			
		} catch (Throwable t) {
			this.value = "";
		}
	}
	
	/**
	 * Returns the property name.
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the value of property.
	 *
	 * @return String
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Interface used to provide a value for the node property.
	 */
	public static interface ValueProvider {
		
		public String getValue();
		
	}
	
}
