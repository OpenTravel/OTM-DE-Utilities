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
import org.opentravel.schemacompiler.model.TLActionResponse;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 * OTM Object for Resource objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmActionResponse extends OtmResourceChildBase<TLActionResponse> implements OtmResourceChild {
    private static Log log = LogFactory.getLog( OtmActionResponse.class );

    public OtmActionResponse(TLActionResponse tla, OtmAction parent) {
        super( tla, parent );
    }

    public boolean isInherited() {
        return getParent().getResponses().contains( this );
    }

    @Override
    public OtmAction getParent() {
        return (OtmAction) parent;
    }
    // public OtmActionResponse(String name, OtmAction parent) {
    // super( new TLActionResponse(), parent );
    // setName( name );
    // }

    /**
     * @see org.opentravel.model.OtmModelElement#getName()
     */
    @Override
    public String getName() {
        return getTL().getStatusCodes().toString() + "  " + getTL().getPayloadTypeName();
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE_RESPONSE;
    }

    @Override
    public TLActionResponse getTL() {
        return (TLActionResponse) tlObject;
    }

    /**
     * Get the payload name from the action facet returned from {@link #getPayloadActionFacet()}
     * 
     * @return the Otm object used as the request payload or null
     */
    public OtmObject getPayload() {
        return getPayloadActionFacet() != null ? getPayloadActionFacet().getResponsePayload( this ) : null;
    }

    /**
     * @return the name of the payload which in responses is the name of the action facet
     */
    public String getPayloadName() {
        String name = "";
        if (getPayloadActionFacet() != null && getPayloadActionFacet().getResponsePayload( this ) != null)
            name = getPayloadActionFacet().getResponsePayload( this ).getName();
        return name;
    }

    /**
     * @return actionFacet from the tlObject
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

    private Node getPayloadNode() {
        ObservableList<String> actionFacets = FXCollections.observableArrayList();
        getOwningMember().getActionFacets().forEach( af -> actionFacets.add( af.getName() ) );
        ComboBox<String> box = DexEditField.makeComboBox( actionFacets, getPayloadActionFacetName(), this );
        return box;
    }

    private Node getMimeNode() {
        SortedMap<String,Boolean> values = new TreeMap<>();
        for (TLMimeType t : TLMimeType.values())
            values.put( t.toString(), getTL().getMimeTypes().contains( t ) );
        HBox hbox = DexEditField.makeCheckBoxRow( values, this );
        return hbox;
    }

    private Node getStatus1Node() {
        SortedMap<String,Boolean> values = new TreeMap<>();
        int cnt = 0;
        for (RestStatusCodes t : RestStatusCodes.values()) {
            if (cnt++ < 4)
                values.put( t.toString(), getTL().getStatusCodes().contains( t.value() ) );
        }
        HBox hbox = DexEditField.makeCheckBoxRow( values, this );
        return hbox;
    }

    private Node getStatus2Node() {
        SortedMap<String,Boolean> values = new TreeMap<>();
        int cnt = 0;
        for (RestStatusCodes t : RestStatusCodes.values())
            if (cnt++ >= 4)
                values.put( t.toString(), getTL().getStatusCodes().contains( t.value() ) );
        HBox hbox = DexEditField.makeCheckBoxRow( values, this );
        return hbox;
    }

    @Override
    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        fields.add( new DexEditField( 0, 0, PAYLOAD_LABEL, PAYLOAD_TOOLTIP, getPayloadNode() ) );
        fields.add( new DexEditField( 1, 0, MIME_TYPE_LABEL, MIME_TYPE_TOOLTIP, getMimeNode() ) );
        fields.add( new DexEditField( 2, 0, STATUS_CODES_LABEL, STATUS_CODES_TOOLTIP, getStatus1Node() ) );
        fields.add( new DexEditField( 3, 0, STATUS_CODES_LABEL, STATUS_CODES_TOOLTIP, getStatus2Node() ) );
        return fields;
    }

    public Tooltip getTooltip() {
        return new Tooltip( TOOLTIP );
    }

    private static final String TOOLTIP = "Specifies the characteristics and payload for a REST Action response.";
    private static final String PAYLOAD_LABEL = "Payload Type";
    private static final String PAYLOAD_TOOLTIP =
        "Name of the action facet or core object that specifies the payload (if any) for the response.";
    private static final String MIME_TYPE_LABEL = "MIME Types";
    private static final String MIME_TYPE_TOOLTIP = "List of supported MIME types.";
    private static final String STATUS_CODES_LABEL = " HTTP Status Codes";
    private static final String STATUS_CODES_TOOLTIP = "Specifies the acceptable HTTP response codes.";

    /**
     * @return
     */
    public StringProperty statusCodeProperty() {
        StringBuilder codes = new StringBuilder();
        getTL().getStatusCodes().forEach( code -> {
            if (!codes.toString().isEmpty())
                codes.append( ", " );
            codes.append( RestStatusCodes.getLabel( code ) );
            codes.append( " [" + code + "] " );
        } );
        return new ReadOnlyStringWrapper( codes.toString() );
        // return new ReadOnlyStringWrapper( getTL().getStatusCodes().toString() );
    }

    /**
     * Get the example pay load from the resources URL helper in a string property
     * 
     * @return
     */
    public StringProperty examplePayloadProperty() {
        // if (getOwningMember() != null)
        // return new ReadOnlyStringWrapper( getOwningMember().getPayloadExample( this ) );
        return new ReadOnlyStringWrapper( getPayloadName() );
    }

    /**
     * @param action facet to set as payload type
     */
    public void setPayloadActionFacet(OtmActionFacet actionFacet) {
        if (actionFacet != null)
            getTL().setPayloadType( actionFacet.getTL() );
        else
            getTL().setPayloadType( null );
    }


}
