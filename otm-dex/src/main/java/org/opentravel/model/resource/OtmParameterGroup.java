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
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.model.TLParamGroup;

/**
 * OTM Object for Resource Action objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmParameterGroup extends OtmResourceChildBase<TLParamGroup> implements OtmResourceChild {
    private static Log log = LogFactory.getLog( OtmParameterGroup.class );

    public OtmParameterGroup(TLParamGroup tla, OtmResource parent) {
        super( tla, parent );
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE_PARAMETERGROUP;
    }

    @Override
    public TLParamGroup getTL() {
        return (TLParamGroup) tlObject;
    }

    /**
     * Not a named entity, must provide a name
     */
    @Override
    public String getName() {
        return getTL().getName();
    }

    //
    // public OtmActionFacet(String name, OtmModelManager mgr) {
    // super( new TLResource(), mgr );
    // setName( name );
    // }

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
}
