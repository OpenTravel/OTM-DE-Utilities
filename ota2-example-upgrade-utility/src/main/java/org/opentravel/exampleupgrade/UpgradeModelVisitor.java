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

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;

/**
 * Visitor that extends the standard model visitor with an addition method that is notified when the traversal of a
 * nested element has been completed.
 */
public abstract class UpgradeModelVisitor extends ModelElementVisitorAdapter {

    /**
     * Called when the visitation to the given element has ended.
     * 
     * @param element the element being visited
     * @return boolean
     */
    public boolean visitElementEnd(TLProperty element) {
        return true;
    }

    /**
     * Called when the navigator is starting to process a group of <code>TLExtensionPointFacet</code>s.
     * 
     * @param extensionPointType the facet type of the extension point group
     * @return boolean
     */
    public abstract boolean visitExtensionPointGroupStart(TLFacetType extensionPointType);

    /**
     * Called when the navigator has completed processing of a group of <code>TLExtensionPointFacet</code>s.
     * 
     * @param extensionPointType the facet type of the extension point group
     */
    public abstract void visitExtensionPointGroupEnd(TLFacetType extensionPointType);

    /**
     * Called when the visitation to the given extension point facet has ended.
     * 
     * @param extensionPointFacet the extension point facet being visited
     * @return boolean
     */
    public boolean visitExtensionPointEnd(TLExtensionPointFacet extensionPointFacet) {
        return true;
    }

    /**
     * Returns the element type that was resolved during visitation of the last element. This may be different than the
     * OTM elements assigned type if the type was the root of a substitution group.
     * 
     * @return TLPropertyType
     */
    public abstract NamedEntity getResolvedElementType();

    /**
     * Returns true if the most recently navigated element can be repeated in order to accept more content from the
     * original document.
     * 
     * @param otmElement the OTM element to check for repeat
     * @param resolvedElementType the resolved type of the OTM element
     * @return boolean
     */
    public abstract boolean canRepeat(TLProperty otmElement, NamedEntity resolvedElementType);

    /**
     * Returns true if the visitor has enabled auto-generation mode.
     * 
     * @return boolean
     */
    public abstract boolean isAutoGenerationEnabled();

}
