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

package org.opentravel.model.otmFacets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;

import java.util.Collection;
import java.util.Collections;

import javafx.beans.property.StringProperty;

/**
 * @author Dave Hollander
 * 
 */
public class OtmVWAValueFacet extends OtmAbstractDisplayFacet {
    private static Log log = LogFactory.getLog( OtmVWAValueFacet.class );

    public OtmVWAValueFacet(OtmPropertyOwner parent) {
        super( parent );
    }

    @Override
    public String getName() {
        return "Value";
    }

    public StringProperty exampleProperty() {
        return ((OtmValueWithAttributes) getOwningMember()).exampleProperty();
    }

    public String getValueType() {
        String type = "";
        if (getParent() instanceof OtmValueWithAttributes
            && ((OtmValueWithAttributes) getParent()).getAssignedType() != null)
            type = ((OtmValueWithAttributes) getParent()).getAssignedType().getName();
        return type;
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        return Collections.emptyList();
    }

}
