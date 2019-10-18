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
import org.opentravel.schemacompiler.model.TLClosedEnumeration;

/**
 * OTM Object Node for Simple objects.
 * 
 * @author Dave Hollander
 * @param <T>
 * 
 */
public class OtmEnumerationClosed extends OtmEnumeration<TLClosedEnumeration> {
    private static Log log = LogFactory.getLog( OtmEnumerationClosed.class );

    public OtmEnumerationClosed(TLClosedEnumeration tlo, OtmModelManager mgr) {
        super( tlo, mgr );
    }

    public OtmEnumerationClosed(String name, OtmModelManager mgr) {
        super( new TLClosedEnumeration(), mgr );
        setName( name );
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.ENUMERATION_CLOSED;
    }

    // @Override
    // public OtmObject setBaseType(OtmObject baseObj) {
    // if (baseObj instanceof OtmEnumerationClosed) {
    // TLExtension tlExt = getTL().getExtension();
    // if (tlExt == null)
    // tlExt = new TLExtension();
    // tlExt.setExtendsEntity( ((OtmEnumerationClosed) baseObj).getTL() );
    // getTL().setExtension( tlExt );
    // }
    // return getBaseType();
    // }
}
