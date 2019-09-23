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

package org.opentravel.model.otmProperties;

import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.schemacompiler.model.TLAttribute;

/**
 * Abstract OTM Node for attribute properties.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmIdAttribute<TL extends TLAttribute> extends OtmAttribute<TLAttribute> implements OtmTypeUser {

    /**
     */
    protected OtmIdAttribute(TL tl, OtmPropertyOwner parent) {
        super( tl, parent );
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.IDATTR;
    }

    // @Override
    // public OtmPropertyType getPropertyType() {
    // return OtmPropertyType.ID;
    // }


    // FIXME
    // /**
    // * {@inheritDoc}
    // * <p>
    // * Get the name from the compiler based on the assigned type. responsible for setting the name to *Ref
    // *
    // * @param name <b>ignored</b> unless compiler does not return a name
    // */
    // @Override
    // public String setName(String name) {
    // return getName();
    // }
}
