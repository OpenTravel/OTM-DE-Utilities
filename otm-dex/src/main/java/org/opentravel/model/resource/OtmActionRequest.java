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
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLMimeType;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 * OTM Object for Resource objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmActionRequest extends OtmResourceChildBase<TLActionRequest> implements OtmResourceChild {
    private static Log log = LogFactory.getLog( OtmActionRequest.class );

    private static final String TOOLTIP = "Specifies the characteristics and payload for a REST Action request.";

    private static final String MIME_LABEL = "Mime Types";

    private static final String MIME_TOOLTIP =
        "Specifies the message MIME type for a REST request. Only XML and JSON types are supported by the OTM model.";

    private static final String PAYLOAD_LABEL = "Payload Type";

    private static final String PAYLOAD_TOOLTIP =
        "Name of the action facet that specifies the payload (if any) for the request.";

    private static final String PATH_LABEL = "Path Template";

    private static final String PATH_TOOLTIP =
        "Specifies the path for this action relative to the base path of the owning resource.  The path template must contain a reference to all path parameters specified in the request's parameter group.";

    private static final String METHOD_LABEL = "HTTP Method";

    private static final String METHOD_TOOLTIP = "Specify the HTTP method for a REST action request.";

    private static final String PARAMETERS_LABEL = "Parameters";

    private static final String PARAMETERS_TOOLTIP =
        "Name of the parameter group that provides the URL and header parameters (if any) for the request.";

    public static MenuButton makeMenuButton(List<String> values, OtmObject object) {
        MenuButton mb = new MenuButton();
        values.forEach( v -> mb.getItems().add( new CheckMenuItem( v ) ) );
        return mb;
    }

    public OtmActionRequest(TLActionRequest tla, OtmAction parent) {
        super( tla, parent );
    }

    /**
     * Get the example pay load from the resources URL helper in a string property
     * 
     * @return
     */
    public StringProperty examplePayloadProperty() {
        if (getOwningMember() != null)
            return new ReadOnlyStringWrapper( getOwningMember().getPayloadExample( this ) );
        return new ReadOnlyStringWrapper( "" );
    }

    @Override
    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        fields.add( new DexEditField( 0, 0, PAYLOAD_LABEL, PAYLOAD_TOOLTIP, getPayloadNode() ) );
        fields.add( new DexEditField( 1, 0, PARAMETERS_LABEL, PARAMETERS_TOOLTIP, getParametersNode() ) );
        fields.add( new DexEditField( 2, 0, METHOD_LABEL, METHOD_TOOLTIP, getMethodNode() ) );
        fields.add( new DexEditField( 3, 0, MIME_LABEL, MIME_TOOLTIP, getMimeNode() ) );
        fields.add( new DexEditField( 4, 0, PATH_LABEL, PATH_TOOLTIP, getPathNode() ) );

        return fields;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE_REQUEST;
    }

    public TLHttpMethod getMethod() {
        return getTL().getHttpMethod();
    }

    public String getMethodString() {
        return getTL().getHttpMethod().toString();
    }

    public ObservableList<String> getMethodCandidates() {
        ObservableList<String> candidates = FXCollections.observableArrayList();
        for (TLHttpMethod m : TLHttpMethod.values())
            candidates.add( m.toString() );
        return candidates;
    }

    private Node getMethodNode() {
        StringProperty selection = getActionManager().add( DexActions.SETREQUESTMETHOD, getMethodString(), this );
        return DexEditField.makeComboBox( getMethodCandidates(), selection );
    }

    private Node getMimeNode() {
        SortedMap<String,Boolean> values = new TreeMap<>();
        for (TLMimeType t : TLMimeType.values())
            values.put( t.toString(), getTL().getMimeTypes().contains( t ) );
        HBox hbox = DexEditField.makeCheckBoxRow( values, this );
        return hbox;
    }

    //
    /**
     * @see org.opentravel.model.OtmModelElement#getName()
     */
    @Override
    public String getName() {
        // Get the name of the parent then add method
        return getOwner() != null ? getOwner().getName() : "";
    }

    public OtmAction getOwner() {
        return OtmModelElement.get( getTL().getOwner() ) instanceof OtmAction
            ? ((OtmAction) OtmModelElement.get( getTL().getOwner() )) : null;
    }

    public ObservableList<String> getParameterGroupCandidates() {
        ObservableList<String> groups = FXCollections.observableArrayList();
        getOwningMember().getParameterGroups().forEach( pg -> groups.add( pg.getName() ) );
        return groups;
    }

    private Node getParametersNode() {
        StringProperty selection =
            getActionManager().add( DexActions.SETREQUESTPARAMETERGROUP, getParamGroupName(), this );
        return DexEditField.makeComboBox( getParameterGroupCandidates(), selection );
    }

    public OtmParameterGroup getParamGroup() {
        if (OtmModelElement.get( getTL().getParamGroup() ) instanceof OtmParameterGroup)
            return (OtmParameterGroup) OtmModelElement.get( getTL().getParamGroup() );
        return null;
    }

    public String getParamGroupName() {
        return getParamGroup() != null ? getParamGroup().getName() : "";

    }

    @Override
    public OtmAction getParent() {
        return (OtmAction) parent;
    }

    private Node getPathNode() {
        StringProperty selection = getActionManager().add( DexActions.SETREQUESTPATH, getPathTemplate(), this );
        return DexEditField.makeTextField( selection );
    }

    public String getPathTemplate() {
        return getTL().getPathTemplate();
    }

    /**
     * Get the payload name from the action facet returned from {@link #getPayloadActionFacet()}
     * 
     * @return the Otm object used as the request payload or null
     */
    public OtmObject getPayload() {
        return getPayloadActionFacet() != null ? getPayloadActionFacet().getRequestPayload() : null;
    }

    /**
     * @return the actionFacet from tlObject or null
     */
    public OtmActionFacet getPayloadActionFacet() {
        if (OtmModelElement.get( getTL().getPayloadType() ) instanceof OtmActionFacet)
            return (OtmActionFacet) OtmModelElement.get( getTL().getPayloadType() );
        return null;
    }

    /**
     * Try to return the name of the payload action facet. If null, try the TL's payload type name.
     * 
     * @return
     */
    public String getPayloadActionFacetName() {
        if (getPayloadActionFacet() != null)
            return getPayloadActionFacet().getName();
        return getTL().getPayloadTypeName() != null ? getTL().getPayloadTypeName() : "";
    }

    public ObservableList<String> getPayloadCandidates() {
        ObservableList<String> actionFacets = FXCollections.observableArrayList();
        getOwningMember().getActionFacets().forEach( af -> actionFacets.add( af.getName() ) );
        return actionFacets;
    }

    /**
     * @return the name of the payload from the action facet
     */
    public String getPayloadName() {
        String name = "";
        if (getPayloadActionFacet() != null && getPayloadActionFacet().getRequestPayload() != null)
            name = getPayloadActionFacet().getRequestPayload().getName();
        return name;
    }

    private Node getPayloadNode() {
        StringProperty selection =
            getActionManager().add( DexActions.SETREQUESTPAYLOAD, getPayloadActionFacetName(), this );
        return DexEditField.makeComboBox( getPayloadCandidates(), selection );
    }

    @Override
    public TLActionRequest getTL() {
        return (TLActionRequest) tlObject;
    }

    public Tooltip getTooltip() {
        return new Tooltip( TOOLTIP );
    }

    /**
     * 
     */
    public StringProperty methodProperty() {
        return new ReadOnlyStringWrapper( getTL().getHttpMethod().toString() );
    }

    public TLHttpMethod setMethodString(String value) {
        TLHttpMethod method = null;
        if (value != null)
            method = TLHttpMethod.valueOf( value );
        return setMethod( method );
    }

    public TLHttpMethod setMethod(TLHttpMethod method) {
        getTL().setHttpMethod( method );
        if (method != null)
            log.debug( "Set method to " + getMethod() );
        // else
        // log.debug( "Set method to null" );
        return getMethod();
    }

    /**
     * @param group can be null
     */
    public OtmParameterGroup setParamGroup(OtmParameterGroup group) {
        if (group != null)
            getTL().setParamGroup( group.getTL() );
        else
            getTL().setParamGroup( null );
        log.debug( "Set parameter group to " + group );
        return group;
    }

    /**
     * Set the parameter group from the owner's parameter group with the passed {@link OtmParameterGroup#getName()}
     * value.
     * 
     * @param value
     * @return
     */
    public OtmParameterGroup setParamGroupString(String value) {
        OtmParameterGroup pg = null;
        for (OtmParameterGroup c : getOwningMember().getParameterGroups())
            if (c.getName().equals( value ))
                pg = c;

        return setParamGroup( pg );
    }

    /**
     * Set the base path for this action request to the passed path. If addParameters is true and the parameter group is
     * an ID group, add the parameters in brackets.
     * 
     * @param basePath
     * @param addParameters
     * @return path as set or null
     */
    public String setPathTemplate(String basePath, boolean addParameters) {
        if (basePath == null)
            getTL().setPathTemplate( null );
        else {
            StringBuilder path = new StringBuilder( basePath );
            if (addParameters && getParamGroup() != null && getParamGroup().isIdGroup()) {
                if (!getParamGroup().getParameters().isEmpty())
                    path.append( "/" );
                // Add these parameters
                for (OtmParameter p : getParamGroup().getParameters()) {
                    path.append( "{" );
                    path.append( p.getName() );
                    path.append( "}" );
                }
            }
            getTL().setPathTemplate( path.toString() );
        }
        return getTL().getPathTemplate();
    }

    /**
     * Set the payload from owner's action facet with the passed {@link OtmActionFacet#getName()} value.
     * 
     * @param value
     * @return
     */
    public OtmActionFacet setPayloadActionFacetString(String value) {
        OtmActionFacet af = null;
        for (OtmActionFacet c : getOwningMember().getActionFacets())
            if (c.getName().equals( value ))
                af = c;
        // if (af == null)
        // log.debug( "No action facet found for value " + value );
        return setPayloadType( af );
    }

    /**
     * @param actionFacet
     */
    public OtmActionFacet setPayloadType(OtmActionFacet actionFacet) {
        if (actionFacet == null) {
            getTL().setPayloadType( null );
            getTL().setPayloadTypeName( null );
            getTL().setMimeTypes( null ); // validation error if set and no payload
        } else {
            getTL().setPayloadType( actionFacet.getTL() );
        }
        log.debug( "Set payload action facet to " + getPayloadActionFacet() );
        return getPayloadActionFacet();
    }

}
