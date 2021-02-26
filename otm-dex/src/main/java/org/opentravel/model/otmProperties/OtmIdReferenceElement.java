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
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.TLProperty;

import javax.xml.namespace.QName;

/**
 * Abstract OTM Node for attribute properties.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmIdReferenceElement<TL extends TLProperty> extends OtmElement<TLProperty> implements OtmTypeUser {

    /**
     */
    protected OtmIdReferenceElement(TL tl, OtmPropertyOwner parent) {
        super( tl, parent );

        if (!(tl instanceof TLProperty))
            throw new IllegalArgumentException( "OtmElement constructor not passed a tl property." );
        if (!tl.isReference())
            throw new IllegalArgumentException( "OtmElementRef constructor a reference element." );
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.ELEMENTREF;
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
        if (getAssignedType() != null)
            getTL().setName( getTypeBasedName() );
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ignore the passed name and use the assigned type to get the name.
     * 
     */
    @Override
    public String fixName(String name) {
        return getTypeBasedName();
    }

    /**
     * Let the compiler return the corrected name.
     * 
     * @return
     */
    protected String getTypeBasedName() {
        QName qn = PropertyCodegenUtils.getDefaultSchemaElementName( getAssignedTLType(), true );
        return qn != null ? qn.getLocalPart() : "";
    }

    @Override
    public boolean isRenameable() {
        return false;
    }


    /**
     * {@inheritDoc}
     * <p>
     * Get the name from the compiler based on the assigned type. responsible for setting the name to *Ref
     * 
     * @param name <b>ignored</b> unless compiler does not return a name
     */
    @Override
    public String setName(String name) {
        getTL().setName( getTypeBasedName().isEmpty() ? name : getTypeBasedName() );
        isValid( true );
        return getName();
    }
}
