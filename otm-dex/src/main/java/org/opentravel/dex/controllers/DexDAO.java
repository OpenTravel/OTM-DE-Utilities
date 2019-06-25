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

import org.opentravel.common.ImageManager;

import javafx.scene.image.ImageView;

/**
 * Interface for all Otm-DE FX view Data Access Objects. DAOs are simple POJOs that expose data using JavaFX properties.
 * Listeners are registered for editable properties.
 * <p>
 * No business logic (actions, validation, filters, etc.) should be in the DAO.
 * 
 * @author dmh
 *
 */
public interface DexDAO<T> {

    /**
     * 
     * @param imageMgr manages access to image views for icons
     * @return JavaFX imageView for the icon representing the data item.
     */
    @SuppressWarnings("restriction")
    public ImageView getIcon(ImageManager imageMgr);

    /**
     * @return the data item
     */
    public T getValue();

    /**
     * @return a string representing this data item
     */
    @Override
    public String toString();
}
