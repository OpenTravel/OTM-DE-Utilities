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

package org.opentravel.model.otmFacets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.schemacompiler.model.TLAbstractFacet;

/**
 * Abstract OTM facade for abstract TL Facets.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmAbstractFacet<T extends TLAbstractFacet> extends OtmModelElement<TLAbstractFacet>
    implements OtmTypeProvider {
    private static Log log = LogFactory.getLog( OtmAbstractFacet.class );


    // Collapsed is only for graphical views. Tree and table views should use Expanded.
    private boolean collapsed;

    public OtmAbstractFacet(T tl) {
        super( tl );
        setExpanded( true ); // Start out expanded
        setCollapsed( false ); // Start out expanded
    }

    public abstract DexActionManager getActionManger();

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.FACET;
    }

    @Override
    public String getName() {
        return tlObject.getLocalName();
    }

    @Override
    public String getNamespace() {
        return tlObject.getNamespace();
    }

    public abstract OtmObject getParent();

    public String getRole() {
        return tlObject.getFacetType().getIdentityName();
    }

    /**
     * Collapsed is only for graphical views. Tree and table views should use Expanded.
     */
    public boolean isCollapsed() {
        return collapsed;
    }

    @Override
    public boolean isNameControlled() {
        return true;
    }

    @Override
    public boolean isRenameable() {
        return false;
    }

    /**
     * Collapsed is only for graphical views. Tree and table views should use Expanded.
     * 
     * @param flag
     */
    public void setCollapsed(boolean flag) {
        collapsed = flag;
    }
}
