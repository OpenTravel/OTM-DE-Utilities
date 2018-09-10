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

/**
 * Interface for components that require notification when an entity facet
 * selection has been modified.
 */
public interface FacetSelectionListener {
	
	/**
	 * Called when the selected facet of the modified selection has been changed.
	 * 
	 * @param modifiedSelection  the facet selection that was modified
	 */
	public void facetSelectionChanged(EntityFacetSelection modifiedSelection);
	
}
