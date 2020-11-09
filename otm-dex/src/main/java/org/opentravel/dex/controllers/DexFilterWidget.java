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

package org.opentravel.dex.controllers;

import org.opentravel.dex.events.DexEvent;

/**
 * Interface for all Otm-DE FX view filter widgets.
 * 
 * @author dmh
 *
 */
public interface DexFilterWidget<T> {

    public void clear();

    /**
     * True if:
     * <ul>
     * <li>not active
     * <li>enabled and data meets selection criteria
     * <li>data fails pre-tests such as null properties.
     * </ul>
     * 
     * @param data to test
     * @return true if the object passes the filter (should be displayed)
     */
    public boolean isSelected(T data);

    public void refresh();

    /**
     * Handler for external events.
     * 
     * @param event
     */
    public void selectionHandler(DexEvent event);
}
