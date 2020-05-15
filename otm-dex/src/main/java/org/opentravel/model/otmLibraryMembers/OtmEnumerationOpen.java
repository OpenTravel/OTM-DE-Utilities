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

package org.opentravel.model.otmLibraryMembers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmFacets.OtmEnumerationOtherFacet;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;

import java.util.ArrayList;
import java.util.Collection;

/**
 * OTM Object Node for Simple objects.
 * 
 * @author Dave Hollander
 * @param <T>
 * 
 */
public class OtmEnumerationOpen extends OtmEnumeration<TLOpenEnumeration> {
    private static Log log = LogFactory.getLog( OtmEnumerationOpen.class );

    public OtmEnumerationOpen(String name, OtmModelManager mgr) {
        super( new TLOpenEnumeration(), mgr );
        setName( name );
    }

    public OtmEnumerationOpen(TLOpenEnumeration tlo, OtmModelManager mgr) {
        super( tlo, mgr );
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        Collection<OtmObject> hierarchy = new ArrayList<>();
        hierarchy.add( new OtmEnumerationOtherFacet( (OtmPropertyOwner) this ) );
        hierarchy.addAll( super.getChildrenHierarchy() );
        return hierarchy;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.ENUMERATION_OPEN;
    }

}
