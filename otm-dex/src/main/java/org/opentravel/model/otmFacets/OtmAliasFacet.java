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
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.TLAlias;

/**
 * Abstract OTM Node for aliases on facets.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmAliasFacet extends OtmModelElement<TLAlias> implements OtmTypeProvider {
    private static Log log = LogFactory.getLog( OtmAliasFacet.class );

    private OtmAlias parent = null;

    /**
     * @param tlBusinessObject
     */
    public OtmAliasFacet(TLAlias tl, OtmAlias parent) {
        super( tl, parent.getActionManager() );
        // log.debug("Created facet alias on " + parent);
        this.parent = parent;

        if (this.parent == null)
            throw new IllegalStateException( "Created alias without parent." );
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.ALIAS;
    }

    @Override
    public OtmLibraryMember getOwningMember() {
        return parent.getOwningMember();
    }

    @Override
    public boolean isNameControlled() {
        return true;
    }

}
