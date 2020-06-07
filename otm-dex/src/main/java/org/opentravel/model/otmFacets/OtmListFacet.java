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
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyBase;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModelElement;

import java.util.ArrayList;

/**
 * @author Dave Hollander
 * 
 */
public class OtmListFacet<T extends TLListFacet> extends OtmAbstractFacet<TLListFacet> {
    private static Log log = LogFactory.getLog( OtmListFacet.class );

    private OtmLibraryMember parent;

    public OtmListFacet(T tl, OtmLibraryMember parent) {
        super( tl );
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     * <p>
     * False
     */
    @Override
    public boolean canAdd(OtmProperty property) {
        return false;
    }

    public DexActionManager getActionManger() {
        return parent.getActionManager();
    }

    public OtmLibraryMember getParent() {
        return parent;
    }

    @Override
    public OtmLibraryMember getOwningMember() {
        return getParent();
    }

    /**
     * @see org.opentravel.model.OtmPropertyOwner#add(org.opentravel.schemacompiler.model.TLModelElement)
     */
    @Override
    public OtmPropertyBase<?> add(TLModelElement newTL) {
        // NO-OP
        return null;
    }

    @Override
    public boolean isNameControlled() {
        return true;
    }

    /**
     * @see org.opentravel.model.OtmChildrenOwner#modelChildren()
     */
    @Override
    public void modelChildren() {
        // Only model once
        if (children == null)
            children = new ArrayList<>();
        else
            children.clear();
    }

    @Override
    public void modelInheritedChildren() {
        // Only model once
        if (inheritedChildren == null)
            inheritedChildren = new ArrayList<>();
        else
            inheritedChildren.clear();
    }

}
