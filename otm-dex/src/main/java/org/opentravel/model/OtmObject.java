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

package org.opentravel.model;

import org.opentravel.common.ImageManager;
import org.opentravel.dex.actions.DexActionManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Interface for all OTM Facade objects which wrap all OTM libraries, objects, facets and properties.
 * 
 * @author Dave Hollander
 * 
 */
public interface OtmObject {

    /**
     * @return fx property for description
     */
    public StringProperty descriptionProperty();

    public DexActionManager getActionManager();

    public OtmModelManager getModelManager();

    public String getDeprecation();

    public String getDescription();

    public String getExample();

    public ValidationFindings getFindings();

    public Image getIcon();

    public ImageManager.Icons getIconType();

    /**
     * @return this member's, or owning object's library from the TLLibrary or null
     */
    public OtmLibrary getLibrary();

    /**
     * @return the named entity's local name
     * 
     **/
    public String getName();

    public String getNamespace();

    /**
     * @return
     */
    public String getNameWithPrefix();

    public String getObjectTypeName();

    public OtmLibraryMember getOwningMember();

    /**
     * 
     */
    public String getPrefix();

    public String getRole();

    public TLModelElement getTL();

    public Tooltip getTooltip();

    public String getValidationFindingsAsString();

    /**
     * @deprecated - use the action manager or owning library
     * @return
     */
    @Deprecated
    public boolean isEditable();

    /**
     * Used by any view to remember if the object has been expanded.
     * 
     * @param expanded will be set to flag.
     */
    public void setExpanded(boolean flag);

    /**
     * Used by any view to remember if the object has been expanded.
     * 
     * @return
     */
    public boolean isExpanded();

    /**
     * Is the property inherited? Properties use the property specific TL Owner and compare with parent's TL object.
     * Contributed facets are inherited when their contributor does not point back to the contributed facet.
     * 
     * @return true if inherited as indicated by different owners
     */
    public boolean isInherited();

    /**
     * Are there any warnings or errors in the findings?
     * 
     * @return
     */
    public boolean isValid();

    /**
     * Are there any warnings or errors in the findings?
     * 
     * @param force regenerating findings by validating with the compiler
     * @return
     */
    public boolean isValid(boolean refresh);

    /**
     * FX observable property for this name. The nameProperty will be updated by the {@link OtmModelElementListener}
     * when the value changes.
     * 
     * @return
     */
    public StringProperty nameProperty();

    public void setDescription(String description);

    /**
     * Set the name if possible.
     * 
     * @param name
     * @return the actual name after assignment attempted
     */
    public String setName(String name);

    @Override
    public String toString();

    public ImageView validationImage();

    public ObjectProperty<ImageView> validationImageProperty();

    public StringProperty validationProperty();
}
