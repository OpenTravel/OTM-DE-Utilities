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
import org.opentravel.common.DexRestStatusCodesHandler;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLMimeType;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

/**
 * OTM Object for Resource objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmActionResponse extends OtmResourceChildBase<TLActionResponse> implements OtmResourceChild {
    // private static Logger log = LogManager.getLogger( OtmActionResponse.class );

    private static final String TOOLTIP = "Specifies the characteristics and payload for a REST Action response.";
    private static final String PAYLOAD_LABEL = "Payload Type";
    private static final String PAYLOAD_TOOLTIP =
        "Name of the action facet or core object that specifies the payload (if any) for the response.";

    private static final String MIME_TYPE_LABEL = "MIME Types";
    private static final String MIME_TYPE_TOOLTIP = "List of supported MIME types.";

    private static final String STATUS_CODES_LABEL = " HTTP Status Codes";
    private static final String STATUS_CODES_TOOLTIP = "Specifies the acceptable HTTP response codes.";

    /**
     * Create action response from the passed tl response. Assure tl response has owner and add to parent.
     * 
     * @param tla
     * @param parent
     */
    public OtmActionResponse(TLActionResponse tla, OtmAction parent) {
        super( tla, parent );

        if (parent != null && tla.getOwner() == null) {
            parent.getTL().addResponse( tla );
            parent.add( this );
        }
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

    @Override
    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        fields.add( new DexEditField( 0, 0, PAYLOAD_LABEL, PAYLOAD_TOOLTIP, getPayloadNode() ) );
        fields.add( new DexEditField( 1, 0, MIME_TYPE_LABEL, MIME_TYPE_TOOLTIP, getMimeNode() ) );
        fields.add( new DexEditField( 2, 0, STATUS_CODES_LABEL, STATUS_CODES_TOOLTIP, getStatus1Node() ) );
        fields.add( new DexEditField( 3, 0, STATUS_CODES_LABEL, STATUS_CODES_TOOLTIP, getStatus2Node() ) );
        return fields;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE_RESPONSE;
    }

    private Node getMimeNode() {
        return new DexMimeTypeHandler( this ).makeMimeTypeBox();
    }

    public List<TLMimeType> getMimeTypes() {
        return getTL().getMimeTypes();
    }

    public List<Integer> getRestStatusCodes() {
        return getTL().getStatusCodes();
    }

    /**
     * Name is the combination of status code and payload type name
     * 
     * @see org.opentravel.model.OtmModelElement#getName()
     */
    @Override
    public String getName() {
        return getTL().getStatusCodes().toString() + "  " + getTL().getPayloadTypeName();
    }

    @Override
    public StringProperty nameProperty() {
        if (nameProperty == null)
            nameProperty = new ReadOnlyStringWrapper();
        nameProperty.set( getName() );
        return nameProperty;
    }

    @Override
    public OtmAction getParent() {
        return (OtmAction) parent;
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

    public ObservableList<String> getPayloadCandidates() {
        ObservableList<String> actionFacets = FXCollections.observableArrayList();
        actionFacets.add( "NONE" );
        getOwningMember().getInheritedActionFacets().forEach( af -> actionFacets.add( af.getName() ) );
        getOwningMember().getActionFacets().forEach( af -> actionFacets.add( af.getName() ) );
        return actionFacets;
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

    private Node getPayloadNode() {
        StringProperty selection =
            getActionManager().add( DexActions.SETRESPONSEPAYLOAD, getPayloadActionFacetName(), this );
        return DexEditField.makeComboBox( getPayloadCandidates(), selection );
    }

    private Node getStatus1Node() {
        return new DexRestStatusCodesHandler( this ).makeMimeTypeBox1();
    }

    private Node getStatus2Node() {
        return new DexRestStatusCodesHandler( this ).makeMimeTypeBox2();
    }

    @Override
    public TLActionResponse getTL() {
        return (TLActionResponse) tlObject;
    }

    public Tooltip getTooltip() {
        return new Tooltip( TOOLTIP );
    }

    // If the response is inherited, its parent will be the base type.
    public boolean isInherited() {
        return !getParent().getResponses().contains( this );
    }

    public void setMimeTypes(List<TLMimeType> list) {
        getTL().setMimeTypes( list );
    }

    public void setRestStatusCodes(List<Integer> codes) {
        getTL().setStatusCodes( codes );
    }

    /**
     * @param action facet to set as payload type
     */
    public OtmActionFacet setPayloadActionFacet(OtmActionFacet actionFacet) {
        if (actionFacet != null) {
            getTL().setPayloadType( actionFacet.getTL() );
            // Set to default mime types if the payload is set and mime types is not set
            if ((getTL().getMimeTypes() == null || getTL().getMimeTypes().isEmpty())
                && getOwningMember().getMimeHandler() != null) {
                getTL().setMimeTypes( getOwningMember().getMimeHandler().getTLValues() );
            }
        } else {
            getTL().setPayloadType( null );
            getTL().setMimeTypes( null ); // clear mime types
        }
        // log.debug( "Set action facet to " + getPayloadActionFacet() );
        return getPayloadActionFacet();
    }

    /**
     * Set the payload action facet from owner's {@link OtmActionFacet#getName()} that matches value.
     * <p>
     * Unknown names, included "NONE" will result in setting to null.
     * 
     * @param value
     * @return
     */
    public OtmActionFacet setPayloadActionFacetString(String value) {
        return setPayloadActionFacet( getOwningMember().getActionFacet( value ) );
    }

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
    }

}
