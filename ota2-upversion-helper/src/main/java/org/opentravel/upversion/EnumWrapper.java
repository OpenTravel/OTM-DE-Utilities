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

package org.opentravel.upversion;

/**
 * Wrapper for enumeration values that can provide user-displayable labels
 * for the UI.
 */
public class EnumWrapper<T extends Enum<T>> {
	
	private T enumValue;
	
	/**
	 * Constructor that supplies the enum value to be displayed in a visual control.
	 * 
	 * @param enumValue  the enumeration value to be wrapped
	 */
	public EnumWrapper(T enumValue) {
		this.enumValue = enumValue;
	}
	
	/**
	 * Returns the underlying enumeration value.
	 * 
	 * @return T
	 */
	public T getValue() {
		return enumValue;
	}
	
	/**
	 * Returns a user-displayable label for the string.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return (enumValue == null) ? "" : MessageBuilder.formatMessage( enumValue.toString() );
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (enumValue == null) ? 0 : enumValue.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object other) {
		return (other instanceof EnumWrapper) ?
				(this.enumValue == ((EnumWrapper<T>) other).enumValue ) : false;
	}
	
}
