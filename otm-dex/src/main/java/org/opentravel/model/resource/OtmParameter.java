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
import org.opentravel.common.DexEditField;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLMemberFieldOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamLocation;
import org.opentravel.schemacompiler.model.TLParameter;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;

/**
 * OTM Object for Resource Action objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmParameter extends OtmResourceChildBase<TLParameter> implements OtmResourceChild {
    private static Log log = LogFactory.getLog( OtmParameter.class );

    public OtmParameter(TLParameter tla, OtmParameterGroup parent) {
        super( tla, parent );

        // tla.getFieldRefName();
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE_PARAMETER;
    }

    @Override
    public TLParameter getTL() {
        return (TLParameter) tlObject;
    }

    @Override
    public String getName() {
        return getTL().getFieldRefName();
    }

    @Override
    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        // fields.add( new DexEditField( 0, 0, FIELD_LABEL, FIELD_TOOLTIP, new ComboBox<String>() ) );
        fields.add( new DexEditField( 0, 0, LOCATION_LABEL, LOCATION_TOOLTIP, new ComboBox<String>() ) );
        return fields;
    }

    public boolean isPathParam() {
        return getTL().getLocation() == TLParamLocation.PATH;
    }

    /**
     * 
     * @return parameter name surrounded by brackets if path parameter or else empty string
     */
    public String getPathContribution() {
        return isPathParam() ? "{" + getName() + "}" : "";
    }

    public boolean isQueryParam() {
        return getTL().getLocation() == TLParamLocation.QUERY;
    }

    public String getQueryContribution(String prefix) {
        StringBuilder contribution = new StringBuilder();
        if (isQueryParam()) {
            contribution.append( prefix + getName() );

            // Add example if possible
            // String ex = "";
            String value = "xyz";
            TLMemberField<TLMemberFieldOwner> src = getTL().getFieldRef();
            List<TLExample> examples = null;
            if (src instanceof TLExampleOwner)
                examples = ((TLExampleOwner) src).getExamples();
            TLExample example = null;
            if (examples != null && !examples.isEmpty())
                example = examples.get( 0 );
            if (example != null)
                value = example.getValue();
            contribution.append( "=" + value );

            // PropertyNode n = null;
            // TLMemberField<?> field = ((TLParameter) param.getTLModelObject()).getFieldRef();
            // if (field instanceof TLModelElement)
            // n = (PropertyNode) getNode( ((TLModelElement) field).getListeners() );

            // List<TLExample> examples = ((TLParameter) param.getTLModelObject()).getExamples();
            // if (examples != null && !examples.isEmpty())
            // ex = examples.get( 0 ).getValue();
            // else if (n != null)
            // ex = n.getExample( null ); // Try to get example from the actual field being referenced.
            // if (ex.isEmpty())
            // ex = "xxx";
            // contribution.append( "=" + value);
        }
        return contribution.toString();
    }


    public Tooltip getTooltip() {
        return new Tooltip( TOOLTIP );
    }

    private static final String TOOLTIP =
        "Provides a reference to a property, attribute or element that should be used as a parameter in a REST request message.Tip: If you want to return a 404 (not found) error when the parameter value does not correspond to an existing resource then use a PATH parameter.";
    // private static final String FIELD_LABEL = "Field";
    // private static final String FIELD_TOOLTIP = "Name of the field to be used as a REST request parameter. ";
    private static final String LOCATION_LABEL = "Location";
    private static final String LOCATION_TOOLTIP = "Specifies the location of the parameter in the REST request. ";

    public TLParamLocation getLocation() {
        return getTL().getLocation();
    }

    /**
     * @return
     */
    public StringProperty locationProperty() {
        String location = "";
        if (getLocation() != null)
            location = getLocation().toString();
        return new ReadOnlyStringWrapper( location );
    }

    /**
     * @return the object this parameter references
     */
    public StringProperty typeProperty() {
        OtmObject ref = OtmModelElement.get( (TLModelElement) getTL().getFieldRef() );
        String type = "";
        if (ref instanceof OtmTypeUser)
            type = ((OtmTypeUser) ref).getTlAssignedTypeName();
        return new ReadOnlyStringWrapper( type );
    }


}
