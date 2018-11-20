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

import java.net.URI;

import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;

/**
 * Wrapper facade for the <code>RepositoryItem</code> interface that is extended
 * to support equality checking and comparisons for sorting.
 */
public class RepositoryItemWrapper implements RepositoryItem, Comparable<RepositoryItemWrapper> {
	
	private RepositoryItem item;
	
	/**
	 * Constructor that supplies the repository item to be wrapped.
	 * 
	 * @param item  the underlying repository item
	 */
	public RepositoryItemWrapper(RepositoryItem item) {
		this.item = item;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RepositoryItemWrapper other) {
		int result;
		
		if (other == null) {
			result = 1;
		} else {
			if ((result = strCompare( this.getBaseNamespace(), other.getBaseNamespace() )) == 0) {
				if ((result = strCompare( this.getLibraryName(), other.getLibraryName() )) == 0) {
					result = strCompare( this.getVersion(), other.getVersion() );
				}
			}
		}
		return result;
	}

	/**
	 * Returns the result of a comparison between the two strings.
	 * 
	 * @param str1  the first string to compare (may be null)
	 * @param str2  the second string to compare (may be null)
	 * @return int
	 */
	private int strCompare(String str1, String str2) {
		int result;
		
		if (str1 == null) {
			result = (str2 == null) ? 0 : -1;
			
		} else {
			result = (str2 == null) ? 1 : str1.compareTo( str2 );
		}
		return result;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (item == null) ? super.hashCode() : item.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof RepositoryItemWrapper) &&
				(compareTo( (RepositoryItemWrapper) obj ) == 0);
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryItem#getRepository()
	 */
	public Repository getRepository() {
		return item.getRepository();
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryItem#getNamespace()
	 */
	public String getNamespace() {
		return item.getNamespace();
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryItem#getBaseNamespace()
	 */
	public String getBaseNamespace() {
		return item.getBaseNamespace();
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryItem#getFilename()
	 */
	public String getFilename() {
		return item.getFilename();
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryItem#getLibraryName()
	 */
	public String getLibraryName() {
		return item.getLibraryName();
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryItem#getVersion()
	 */
	public String getVersion() {
		return item.getVersion();
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryItem#getVersionScheme()
	 */
	public String getVersionScheme() {
		return item.getVersionScheme();
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryItem#getStatus()
	 */
	public TLLibraryStatus getStatus() {
		return item.getStatus();
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryItem#getState()
	 */
	public RepositoryItemState getState() {
		return item.getState();
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryItem#getLockedByUser()
	 */
	public String getLockedByUser() {
		return item.getLockedByUser();
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryItem#toURI()
	 */
	public URI toURI() {
		return item.toURI();
	}

	/**
	 * @see org.opentravel.schemacompiler.repository.RepositoryItem#toURI(boolean)
	 */
	public URI toURI(boolean fullyQualified) {
		return item.toURI(fullyQualified);
	}
	
}
