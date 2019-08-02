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
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

/**
 * OTM Object for Resource objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmActionRequest extends OtmResourceChildBase<TLActionRequest> implements OtmResourceChild {
    private static Log log = LogFactory.getLog( OtmActionRequest.class );

    public OtmActionRequest(TLActionRequest tla, OtmAction parent) {
        super( tla, parent );

        tla.getHttpMethod();
        TLActionFacet tlf = tla.getPayloadType();
        tla.getParamGroupName();

    }

    public String getPathTemplate() {
        return getTL().getPathTemplate();
    }

    public OtmParameterGroup getParamGroup() {
        if (OtmModelElement.get( getTL().getParamGroup() ) instanceof OtmParameterGroup)
            return (OtmParameterGroup) OtmModelElement.get( getTL().getParamGroup() );
        return null;
    }
    //
    // public OtmActionFacet(String name, OtmResource parent) {
    // super( new TLActionRequest(), parent );
    // setName( name );
    // }

    /**
     * @see org.opentravel.model.OtmModelElement#getName()
     */
    @Override
    public String getName() {
        // Get the name of the parent then add method
        return getOwner() != null ? getOwner().getName() : "";
        // if (getTL().getPayloadType() != null)
        // return getTL().getHttpMethod().toString() + " <" + getTL().getPayloadType().getName() + ">";
        // if (getTL().getParamGroup() != null)
        // return getTL().getHttpMethod().toString() + " ?" + getTL().getParamGroup().getName();
        // else
        // return getTL().getHttpMethod().toString();
    }

    public OtmAction getOwner() {
        return OtmModelElement.get( getTL().getOwner() ) instanceof OtmAction
            ? ((OtmAction) OtmModelElement.get( getTL().getOwner() )) : null;
    }

    @Override
    public OtmAction getParent() {
        return (OtmAction) parent;
    }

    @Override
    public TLActionRequest getTL() {
        return (TLActionRequest) tlObject;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE_REQUEST;
    }


    // @Override
    // public String setName(String name) {
    // getTL().setName( name );
    // isValid( true );
    // return getName();
    // }
    //
    //
    //
    // @Override
    // public Collection<OtmObject> getChildrenHierarchy() {
    // Collection<OtmObject> ch = new ArrayList<>();
    // // children.forEach(c -> {
    // // if (c instanceof OtmIdFacet)
    // // ch.add(c);
    // // if (c instanceof OtmAlias)
    // // ch.add(c);
    // // });
    // return ch;
    // }
    //
    @Override
    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        fields.add( new DexEditField( 0, 0, PAYLOAD_LABEL, PAYLOAD_TOOLTIP, new ComboBox<String>() ) );
        fields.add( new DexEditField( 0, 2, PARAMETERS_LABEL, PARAMETERS_TOOLTIP, new ComboBox<String>() ) );
        fields.add( new DexEditField( 1, 0, MIME_LABEL, MIME_TOOLTIP, new ChoiceBox<String>() ) );
        fields.add( new DexEditField( 1, 2, METHOD_LABEL, METHOD_TOOLTIP, new ChoiceBox<String>() ) );
        fields.add( new DexEditField( 2, 0, PATH_LABEL, PATH_TOOLTIP, new TextField() ) );

        return fields;
    }

    public Tooltip getTooltip() {
        return new Tooltip( TOOLTIP );
    }

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

    /**
     * 
     */
    public StringProperty methodProperty() {
        return new ReadOnlyStringWrapper( getTL().getHttpMethod().toString() );
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

    public StringProperty urlProperty() {
        if (getOwningMember() != null)
            return new ReadOnlyStringWrapper( getOwningMember().getURL( getOwner() ) );
        return new ReadOnlyStringWrapper( "http://www.example.com/Members/{memberID}" );
    }

    /**
     * @param group can be null
     */
    public void setParamGroup(OtmParameterGroup group) {
        getTL().setParamGroup( group.getTL() );
    }

    /**
     * @param actionFacet
     */
    public void setPayloadType(OtmActionFacet actionFacet) {
        if (actionFacet == null) {
            getTL().setPayloadType( null );
            getTL().setPayloadTypeName( null );
            getTL().setMimeTypes( null ); // validation error if set and no payload
        } else {
            getTL().setPayloadType( actionFacet.getTL() );
        }
    }

    /**
     * @param actionFacet
     */
    public OtmActionFacet getPayloadType() {
        if (OtmModelElement.get( getTL().getPayloadType() ) instanceof OtmActionFacet)
            return (OtmActionFacet) OtmModelElement.get( getTL().getPayloadType() );
        return null;
    }

    /**
     * 
     * @return the Otm object used as the payload or null
     */
    public OtmObject getPayload() {
        return getPayloadType() != null ? getPayloadType().getRequestPayload() : null;
    }

    /**
     * Try to return the name of the payload action facet. If null, try the TL's payload type name.
     * 
     * @return
     */
    public String getPayloadTypeName() {
        if (getPayloadType() != null)
            return getPayloadType().getName();
        return getTL().getPayloadTypeName() != null ? getTL().getPayloadTypeName() : "";
    }

    /**
     * Java model:
     * <P>
     * Action Request Info [] - each path parameter
     * 
     */
}
