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

package org.opentravel.exampleupgrade;

/**
 * Specifies the strategy that should be used to populate the OTM Object choice
 * box on the visual display.
 */
public class SelectionStrategy {
	
	private static final SelectionStrategy defaultStrategy = new SelectionStrategy( Type.BASE_FAMILY, null );
	
	/**
	 * Specifies the basic type of the object selection strategy.
	 */
	public enum Type {
		
		/**
		 * Strategy that considers only those objects in the same base namespace
		 * and substitution group hierarchy.
		 */
		BASE_FAMILY,
		
		/**
		 * Strategy that considers only those objects in the same base namespace
		 * as the original XML EXAMPLE document.
		 */
		EXAMPLE_NAMESPACE,
		
		/**
		 * Strategy that considers only those objects in a user-specified base
		 * namespace.
		 */
		USER_NAMESPACE
		
	}
	
	private Type strategyType;
	private String userNamespace;
	
	/**
	 * Full constructor.
	 * 
	 * @param strategyType  the basic type of the object selection strategy
	 * @param userNamespace  the base namespace specified for the USER_NAMESPACE strategy
	 */
	public SelectionStrategy(Type strategyType, String userNamespace) {
		this.strategyType = strategyType;
		this.userNamespace = userNamespace;
	}
	
	/**
	 * Returns the default selection strategy.
	 * 
	 * @return SelectionStrategy
	 */
	public static SelectionStrategy getDefault() {
		return defaultStrategy;
	}

	/**
	 * Returns the basic type of the object selection strategy.
	 *
	 * @return Type
	 */
	public Type getStrategyType() {
		return strategyType;
	}

	/**
	 * Returns the base namespace specified for the USER_NAMESPACE strategy.
	 *
	 * @return String
	 */
	public String getUserNamespace() {
		return userNamespace;
	}
	
}
