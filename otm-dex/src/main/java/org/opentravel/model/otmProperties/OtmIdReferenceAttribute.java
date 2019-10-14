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
public class OtmIdReferenceAttribute<TL extends TLAttribute> extends OtmAttribute<TLAttribute> implements OtmTypeUser {

    /**
     */
    protected OtmIdReferenceAttribute(TL tl, OtmPropertyOwner parent) {
        super( tl, parent );
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.IDREFATTR;
    }

    /**
     * Name changes to references are not changed in the TL object when the name changes to the assigned type. Force
     * that change and set the name property to null.
     * 
     * @see org.opentravel.model.OtmModelElement#clearNameProperty()
     */
    @Override
    public void clearNameProperty() {
        nameProperty = null;
        // String name = XsdCodegenUtils.getGlobalTypeName( getAssignedTLType(), new TLProperty() );
        if (getAssignedType() != null)
            getTL().setName( getAssignedType().getName() + "Ref" );
    }
}
