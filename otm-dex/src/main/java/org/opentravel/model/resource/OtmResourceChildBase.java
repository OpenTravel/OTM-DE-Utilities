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

package org.opentravel.model.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * OTM Object for Resource objects.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmResourceChildBase<C> extends OtmModelElement<TLModelElement> {
    private static Log log = LogFactory.getLog( OtmResourceChildBase.class );

    protected OtmResource parent = null;

    public OtmResourceChildBase(TLModelElement tlo, OtmResource parent) {
        super( tlo );
        this.parent = parent;
        if (parent != null)
            parent.add( this );
    }

    @Override
    public OtmResource getOwningMember() {
        return parent;
    }

    // public OtmResourceChildBase(TLResource tlo, OtmModelManager mgr) {
    // super( tlo, mgr );
    // }
    //
    // public OtmResourceChildBase(String name, OtmModelManager mgr) {
    // super( new TLResource(), mgr );
    // setName( name );
    // }
    //
    // @Override
    // public String setName(String name) {
    // getTL().setName( name );
    // isValid( true );
    // return getName();
    // }
    //
    // @Override
    // public TLResource getTL() {
    // return (TLResource) tlObject;
    // }
    //
    // @Override
    // public Icons getIconType() {
    // return ImageManager.Icons.RESOURCE;
    // }
    //
    // @Override
    // public Collection<OtmObject> getChildrenHierarchy() {
    // Collection<OtmObject> ch = new ArrayList<>();
    // // children.forEach(c -> {
    // // if (c instanceof OtmIdFacet)
    // // ch.add(c);
    // // if (c instanceof OtmAlias)
    // // ch.add(c);
    // // });
    // return ch;
    // }
    //
    //
    // @Override
    // public boolean isExpanded() {
    // return true;
    // }
    //
    // @Override
    // public boolean isNameControlled() {
    // return false;
    // }
    //
    // /**
    // * @see org.opentravel.model.otmLibraryMembers.OtmLibraryMemberBase#modelChildren()
    // */
    // @Override
    // public void modelChildren() {
    // getTL().getActionFacets();
    // getTL().getActions();
    // getTL().getParamGroups();
    // getTL().getParentRefs();
    //
    // super.modelChildren();
    // }
}
