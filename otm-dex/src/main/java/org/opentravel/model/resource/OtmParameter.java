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

import org.opentravel.common.DexEditField;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLMemberFieldOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamLocation;
import org.opentravel.schemacompiler.model.TLParameter;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

/**
 * OTM Object for Resource Action objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmParameter extends OtmResourceChildBase<TLParameter> implements OtmResourceChild {
    // private static Log log = LogFactory.getLog( OtmParameter.class );
    private static TLParamLocation defaultLocation = TLParamLocation.PATH;

    private static final String TOOLTIP =
        "Provides a reference to a property, attribute or element that should be used as a parameter in a REST request message.Tip: If you want to return a 404 (not found) error when the parameter value does not correspond to an existing resource then use a PATH parameter.";
    private static final String FIELD_LABEL = "Field";
    private static final String FIELD_TOOLTIP = "Name of the field to be used as a REST request parameter. ";
    private static final String LOCATION_LABEL = "Location";
    private static final String LOCATION_TOOLTIP = "Specifies the location of the parameter in the REST request. ";

    public OtmParameter(TLParameter tla, OtmParameterGroup parent) {
        super( tla, parent );

        if (parent != null && tla.getOwner() == null)
            parent.getTL().addParameter( tla );

        if (tla.getLocation() == null)
            tla.setLocation( defaultLocation );
    }

    public OtmObject getFieldRef() {
        return OtmModelElement.get( (TLModelElement) getTL().getFieldRef() );
    }

    /**
     * @return null or name from the otm wrapper of the TL field ref
     */
    public String getFieldRefName() {
        return getFieldRef() != null ? getFieldRef().getName() : "";
    }

    /**
     * 
     * @return the type assigned to the field reference or null
     */
    public OtmTypeProvider getFieldAssignedType() {
        return getFieldRef() instanceof OtmTypeUser ? ((OtmTypeUser) getFieldRef()).getAssignedType() : null;
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
        return getTL().getFieldRef() != null ? getTL().getFieldRef().getName() : getTL().getFieldRefName();
    }

    @Override
    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        fields.add( new DexEditField( 1, 0, FIELD_LABEL, FIELD_TOOLTIP, getFieldsNode() ) );
        fields.add( new DexEditField( 0, 0, LOCATION_LABEL, LOCATION_TOOLTIP, getLocationsNode() ) );
        return fields;
    }

    /**
     * Get an FX node used to select from Reference Facet field candidates
     * 
     * @return
     */
    public Node getFieldsNode() {
        StringProperty selection = getActionManager().add( DexActions.SETPARAMETERFIELD, getFieldRefName(), this );
        return DexEditField.makeComboBox( getFieldCandidates(), selection );
    }

    /**
     * Get facet names from subject business object. Omit query facets if this is an ID Parameter Group.
     * 
     * @return a string array of subject facet names.
     */
    protected ObservableList<String> getFieldCandidates() {
        ObservableList<String> fields = FXCollections.observableArrayList();
        getFieldRefCandidates().forEach( c -> fields.add( c.getName() ) );
        return fields;
    }

    /**
     * Use the compiler to get a list of field reference candidates.
     * <p>
     * Note: children of types assigned to complex fields will be included.
     * 
     * @return new list of eligible properties
     */
    protected List<OtmProperty> getFieldRefCandidates() {
        List<OtmProperty> fields = new ArrayList<>();
        TLFacet facetRef = null;
        if (getTL() != null && getTL().getOwner() != null)
            facetRef = getTL().getOwner().getFacetRef();
        if (facetRef != null) {
            OtmObject field;
            for (TLMemberField<TLMemberFieldOwner> tlField : ResourceCodegenUtils
                .getEligibleParameterFields( (TLFacet) facetRef )) {
                field = OtmModelElement.get( (TLModelElement) tlField );
                if (field instanceof OtmProperty)
                    fields.add( (OtmProperty) field );
            }
        }
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

    @Override
    public OtmParameterGroup getParent() {
        return (OtmParameterGroup) parent;
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
        }
        return contribution.toString();
    }


    private ObservableList<String> getLocationCandidates() {
        ObservableList<String> locations = FXCollections.observableArrayList();
        for (TLParamLocation l : TLParamLocation.values())
            locations.add( l.toString() );
        return locations;
    }

    private Node getLocationsNode() {
        String location = "";
        if (getLocation() != null)
            location = getLocation().toString();
        StringProperty selection = getActionManager().add( DexActions.SETPARAMETERLOCATION, location, this );
        return DexEditField.makeComboBox( getLocationCandidates(), selection );
    }

    public void setFieldString(String fieldName) {
        if (fieldName == null || fieldName.isEmpty())
            getTL().setFieldRef( null );
        else
            for (OtmProperty c : getFieldRefCandidates())
                if (c.getName().equals( fieldName ))
                    setFieldRef( c );
        nameProperty = null;
    }

    public void setFieldRef(OtmProperty field) {
        if (field != null && field.getTL() instanceof TLMemberField)
            getTL().setFieldRef( (TLMemberField<?>) field.getTL() );
        nameProperty = null;
    }


    public TLParamLocation setLocation(TLParamLocation location) {
        getTL().setLocation( location );
        // log.debug( "Set loction to " + location );
        return getLocation();
    }

    public TLParamLocation setLocationString(String value) {
        TLParamLocation location = null;
        for (TLParamLocation c : TLParamLocation.values())
            if (c.toString().equals( value ))
                location = c;
        return setLocation( location );
    }

    public Tooltip getTooltip() {
        return new Tooltip( TOOLTIP );
    }

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

    public StringProperty nameProperty() {
        if (nameProperty == null)
            nameProperty = new ReadOnlyStringWrapper( getName() );
        return nameProperty;
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
