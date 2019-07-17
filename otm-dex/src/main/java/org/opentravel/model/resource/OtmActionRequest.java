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
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;

/**
 * OTM Object for Resource objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmActionRequest extends OtmResourceChildBase<TLActionRequest> implements OtmResourceChild {
    private static Log log = LogFactory.getLog( OtmActionRequest.class );

    public OtmActionRequest(TLActionRequest tla, OtmAction parent) {
        super( tla, parent );

        tla.getHttpMethod();
        TLActionFacet tlf = tla.getPayloadType();
        tla.getParamGroupName();
    }

    //
    // public OtmActionFacet(String name, OtmResource parent) {
    // super( new TLActionRequest(), parent );
    // setName( name );
    // }

    /**
     * @see org.opentravel.model.OtmModelElement#getName()
     */
    @Override
    public String getName() {
        if (getTL().getPayloadType() != null)
            return getTL().getHttpMethod().toString() + " <" + getTL().getPayloadType().getName() + ">";
        if (getTL().getParamGroup() != null)
            return getTL().getHttpMethod().toString() + " ?" + getTL().getParamGroup().getName();
        else
            return getTL().getHttpMethod().toString();
    }

    @Override
    public TLActionRequest getTL() {
        return (TLActionRequest) tlObject;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE_REQUEST;
    }


    // @Override
    // public String setName(String name) {
    // getTL().setName( name );
    // isValid( true );
    // return getName();
    // }
    //
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
