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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;

/**
 * Factory that resolves which type of property (indicator, element, attribute) to create.
 * 
 * @author dmh
 *
 */
public class OtmPropertyFactory {
    private static Log log = LogFactory.getLog( OtmPropertyFactory.class );

    private OtmPropertyFactory() {
        // NO-OP - only static methods
    }

    public static OtmAttribute<TLAttribute> create(TLAttribute tlAttribute, OtmPropertyOwner parent) {
        OtmAttribute<TLAttribute> attribute;
        attribute = new OtmAttribute<>( tlAttribute, parent );
        return attribute;
    }

    public static OtmElement<TLProperty> create(TLProperty tlProperty, OtmPropertyOwner parent) {
        OtmElement<TLProperty> property;
        if (tlProperty.isReference())
            property = new OtmElementReference<>( tlProperty, parent );
        else
            property = new OtmElement<>( tlProperty, parent );
        return property;
    }

    public static OtmIndicator<TLIndicator> create(TLIndicator tlIndicator, OtmPropertyOwner parent) {
        OtmIndicator<TLIndicator> indicator;
        if (tlIndicator.isPublishAsElement())
            indicator = new OtmIndicatorElement<>( tlIndicator, parent );
        else
            indicator = new OtmIndicator<>( tlIndicator, parent );
        return indicator;
    }

    /**
     * @param tl
     * @param parent
     */
    public static OtmProperty<?> create(TLModelElement tl, OtmPropertyOwner parent) {
        OtmProperty<?> p = null;
        if (tl instanceof TLIndicator)
            p = OtmPropertyFactory.create( (TLIndicator) tl, parent );
        else if (tl instanceof TLProperty)
            p = OtmPropertyFactory.create( (TLProperty) tl, parent );
        else if (tl instanceof TLAttribute)
            p = OtmPropertyFactory.create( (TLAttribute) tl, parent );
        else
            log.debug( "unknown/not-implemented property type." );
        log.debug( "Created property " + p.getName() + " of " + p.getOwningMember().getName() + "  inherited? "
            + p.isInherited() );
        return p;
    }

    public static String getObjectName(OtmProperty property) {
        if (property instanceof OtmElement)
            return "Element";
        if (property instanceof OtmElementReference)
            return "Element Reference";
        if (property instanceof OtmAttribute)
            return "Attribute";
        if (property instanceof OtmIndicator)
            return "Indicator";

        return property.getClass().getSimpleName();
    }
}
