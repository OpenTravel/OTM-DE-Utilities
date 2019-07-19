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
import org.opentravel.dex.actions.DexActionManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmOperation;
import org.opentravel.schemacompiler.model.TLFacet;

import java.util.Collection;
import java.util.List;

/**
 * Abstract OTM Object for Operation Facets.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmOperationFacet extends OtmAbstractFacet<TLFacet> {
    private static Log log = LogFactory.getLog( OtmOperationFacet.class );

    // This is the only facet whose parent is not an library member
    private OtmOperation parent;

    /**
     */
    public OtmOperationFacet(TLFacet tl, OtmOperation parent) {
        super( tl );
        this.parent = parent;
    }

    @Override
    public List<OtmObject> getChildren() {
        if (children != null && children.isEmpty())
            modelChildren();
        return children;
    }

    public DexActionManager getActionManger() {
        return parent.getActionManager();
    }

    public OtmOperation getParent() {
        return parent;
    }

    @Override
    public OtmLibraryMember getOwningMember() {
        return getParent().getOwningMember();
    }

    @Override
    public boolean isExpanded() {
        return false;
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        return getChildren();
    }

}
