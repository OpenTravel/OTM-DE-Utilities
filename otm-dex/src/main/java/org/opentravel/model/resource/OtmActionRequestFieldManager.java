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
import org.opentravel.common.DexMimeTypeHandler;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.schemacompiler.model.TLHttpMethod;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;

/**
 * OTM Object for Resource objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmActionRequestFieldManager {
    // private static Log log = LogFactory.getLog( OtmActionRequestFieldManager.class );


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

    private static final String PATH_DEFAULT_LABEL = "Default";
    private static final String PATH_DEFAULT_TOOLTIP = "Reset to default path for action request.";

    private OtmActionRequest ar = null;

    /**
     * Create action request field manager from the passed otm action request.
     */
    public OtmActionRequestFieldManager(OtmActionRequest ar) {
        this.ar = ar;
    }

    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        fields.add( new DexEditField( 0, 0, PAYLOAD_LABEL, PAYLOAD_TOOLTIP, getPayloadNode() ) );
        fields.add( new DexEditField( 1, 0, PARAMETERS_LABEL, PARAMETERS_TOOLTIP, getParametersNode() ) );
        fields.add( new DexEditField( 2, 0, METHOD_LABEL, METHOD_TOOLTIP, getMethodNode() ) );
        fields.add( new DexEditField( 3, 0, MIME_LABEL, MIME_TOOLTIP, getMimeNode() ) );
        fields.add( new DexEditField( 4, 0, PATH_LABEL, PATH_TOOLTIP, getPathNode() ) );
        fields.add( new DexEditField( 4, 2, null, PATH_DEFAULT_TOOLTIP, getDefaultPathNode() ) );

        return fields;
    }

    private Node getDefaultPathNode() {
        Button button = new Button( PATH_DEFAULT_LABEL );
        button.setDisable( !ar.isEditable() );
        button.setOnAction( e -> ar.setPathTemplate( ar.getPathTemplateDefault(), false ) );
        // button.setOnAction( e -> ar.pathProperty.set( ar.getPathTemplateDefault() ) );
        return button;
    }

    protected ObservableList<String> getMethodCandidates() {
        ObservableList<String> candidates = FXCollections.observableArrayList();
        for (TLHttpMethod m : TLHttpMethod.values())
            candidates.add( m.toString() );
        return candidates;
    }

    private Node getMethodNode() {
        StringProperty selection = ar.getActionManager().add( DexActions.SETREQUESTMETHOD, ar.getMethodString(), ar );
        return DexEditField.makeComboBox( getMethodCandidates(), selection );
    }

    private Node getMimeNode() {
        return new DexMimeTypeHandler( ar ).makeMimeTypeBox();
    }

    public ObservableList<String> getParameterGroupCandidates() {
        ObservableList<String> groups = FXCollections.observableArrayList();
        groups.add( "NONE" );
        ar.getOwningMember().getInheritedParameterGroups().forEach( pg -> groups.add( pg.getName() ) );
        ar.getOwningMember().getParameterGroups().forEach( pg -> groups.add( pg.getName() ) );
        return groups;
    }

    private Node getParametersNode() {
        StringProperty selection =
            ar.getActionManager().add( DexActions.SETREQUESTPARAMETERGROUP, ar.getParamGroupName(), ar );
        return DexEditField.makeComboBox( getParameterGroupCandidates(), selection );
    }


    private Node getPathNode() {
        // log.debug( "Parent URL: " + getParent().getEndpointURL() );
        ar.pathProperty = ar.getActionManager().add( DexActions.SETREQUESTPATH, ar.getPathTemplate(), ar );
        return DexEditField.makeTextField( ar.pathProperty );
    }


    public ObservableList<String> getPayloadCandidates() {
        ObservableList<String> actionFacets = FXCollections.observableArrayList();
        actionFacets.add( OtmActionRequest.NO_PARAMETERS );
        if (ar.getMethod() == null || !ar.getMethod().equals( TLHttpMethod.GET )) {
            ar.getOwningMember().getInheritedActionFacets().forEach( af -> actionFacets.add( af.getName() ) );
            ar.getOwningMember().getActionFacets().forEach( af -> actionFacets.add( af.getName() ) );
        }
        return actionFacets;
    }

    private Node getPayloadNode() {
        StringProperty selection =
            ar.getActionManager().add( DexActions.SETREQUESTPAYLOAD, ar.getPayloadActionFacetName(), ar );
        return DexEditField.makeComboBox( getPayloadCandidates(), selection );
    }

}
