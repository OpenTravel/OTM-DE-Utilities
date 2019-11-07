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
import org.opentravel.model.OtmTypeProvider;
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
     * Create an Id attribute. If type is not set, set it to the ID type from parent's model manager.
     */
    public OtmIdAttribute(TL tl, OtmPropertyOwner parent) {
        super( tl, parent );
        if (tl.getType() == null && parent.getModelManager() != null && parent.getModelManager().getIdType() != null)
            tl.setType( parent.getModelManager().getIdType().getTL() );
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.IDATTR;
    }

    /**
     * Even though it is a type user, type can not be changed.
     * 
     * @see org.opentravel.model.otmProperties.OtmAttribute#setAssignedType(org.opentravel.model.OtmTypeProvider)
     */
    @Override
    public OtmTypeProvider setAssignedType(OtmTypeProvider type) {
        return null;
    }
}
