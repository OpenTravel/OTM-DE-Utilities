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
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLReferenceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;

/**
 * OTM Object for Resource objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmActionFacet extends OtmResourceChildBase<TLActionFacet> implements OtmResourceChild, OtmTypeUser {
    private static Log log = LogFactory.getLog( OtmActionFacet.class );

    private static final String TOOLTIP =
        "            Action facets describe the message payload for RESTful action requests and responses.  In addition to their own payload, they provide basic information about how the resource's business object should be referenced in the message.";

    private static final String REFERENCE_FACET_LABEL = "Reference Facet";

    private static final String REFERENCE_FACET_TOOLTIP =
        "Specifies the name of the business object facet to be referenced in the message.  If the Reference Type value is None this value will be ignored. ";

    private static final String REPEAT_COUNT_LABEL = "Repeat Count";

    private static final String REPEAT_COUNT_TOOLTIP =
        "Specifies the maximum number of times that the business object reference should repeat in the message. Best practices state that this string value should contain a positive number that is greater than or equal to 1. ";

    private static final String REFERENCE_TYPE_LABEL = "Reference Type";


    private static final String REFERENCE_TYPE_TOOLTIP = "Reference type";

    // public static final String SUBGRP = "Substitution Group";

    private static final String BASE_PAYLOAD_LABEL = "Base Payload";

    private static final String BASE_PAYLOAD_TOOLTIP =
        " Optional reference to a core or choice object that indicates the basic structure of the message payload. If the 'referenceType' value is NONE, this will indicate the entirity of the message structure.  For reference type values other than NONE, the message structure will include all elements of the base payload, plus reference(s) to the owning resource's business ";

    public OtmActionFacet(String name, OtmResource parent) {
        super( new TLActionFacet(), parent );
        setName( name );
    }


    /**
     * Add this action facet to the OtmResource. Do not change the TL object.
     * 
     * @param tla
     * @param parent
     */
    public OtmActionFacet(TLActionFacet tla, OtmResource parent) {
        super( tla, parent );

        // tla.getReferenceFacetName(); -
        // tla.getBasePayloadName();
        // tla.getReferenceRepeat();
        // tla.getReferenceType();
    }

    /**
     * 
     * @return base payload if any or else the reference facet
     */
    public OtmObject getWrappedObject() {
        return getBasePayload() != null ? getBasePayload() : getReferenceFacet();
    }

    /**
     * Could be Choice or Core
     * 
     * @return core, choice or null
     */
    public OtmLibraryMember getBasePayload() {
        OtmObject obj = OtmModelElement.get( (TLModelElement) getTL().getBasePayload() );
        return obj instanceof OtmLibraryMember ? (OtmLibraryMember) obj : null;
    }

    private Node getRemoveBasePayloadNode() {
        Button button = new Button( "-Remove-" );
        button.setDisable( !isEditable() || getBasePayload() == null );
        button.setOnAction( a -> getActionManager().run( DexActions.REMOVEAFBASEPAYLOAD, this ) );
        return button;
    }

    private Node getBasePayloadNode() {
        String name = OtmResource.NONE;
        if (getBasePayload() != null)
            name = getBasePayload().getNameWithPrefix();
        Button button = new Button( name );
        button.setDisable( !isEditable() );
        button.setOnAction( a -> getActionManager().run( DexActions.TYPECHANGE, this ) );
        // button.setOnAction( a -> log.debug( "Base Payload Button selected" ) );
        return button;
    }

    // @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        Collection<OtmObject> ch = new ArrayList<>();
        ch.add( OtmModelElement.get( (TLModelElement) getTL().getBasePayload() ) );
        return ch;
    }

    @Override
    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        fields.add( new DexEditField( 0, 0, BASE_PAYLOAD_LABEL, BASE_PAYLOAD_TOOLTIP, getBasePayloadNode() ) );
        fields.add( new DexEditField( 0, 2, null, "Remove base payload.", getRemoveBasePayloadNode() ) );
        fields.add( new DexEditField( 1, 0, REFERENCE_TYPE_LABEL, REFERENCE_TYPE_TOOLTIP, getReferenceTypeNode() ) );
        fields.add( new DexEditField( 2, 0, REFERENCE_FACET_LABEL, REFERENCE_FACET_TOOLTIP, getReferenceFacetNode() ) );
        fields.add( new DexEditField( 3, 0, REPEAT_COUNT_LABEL, REPEAT_COUNT_TOOLTIP, getRepeatCountNode() ) );
        return fields;
    }
    // FIXME - repeat count needs to support typing in values

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.FACET;
    }

    /**
     * Get the facet from the reference facet name
     * 
     * @return OtmFacet or null
     */
    public OtmFacet<? extends TLFacet> getReferenceFacet() {
        TLBusinessObject referencedBO = null;
        TLFacet tlFacet = null;
        if (getOwningMember() != null && getOwningMember().getSubject() != null)
            referencedBO = getOwningMember().getSubject().getTL();
        if (referencedBO != null)
            tlFacet = ResourceCodegenUtils.getReferencedFacet( referencedBO, getReferenceFacetName() );
        OtmObject obj = OtmModelElement.get( tlFacet );
        if (obj instanceof OtmContextualFacet)
            obj = ((OtmContextualFacet) obj).getWhereContributed();

        return obj instanceof OtmFacet ? (OtmFacet<?>) obj : null;
    }

    public int getRepeatCount() {
        return getTL().getReferenceRepeat();
    }

    public void setRepeatCount(int value) {
        getTL().setReferenceRepeat( value );
    }

    /**
     * List of facet names on subject and entry for the substitution group
     * 
     * @return list of facets on the subject business object
     */
    private ObservableList<String> getReferenceFacetCandidates() {
        ObservableList<String> candidates = FXCollections.observableArrayList();
        if (getOwningMember() != null)
            getOwningMember().getSubjectFacets().forEach( f -> candidates.add( f.getName() ) );
        candidates.add( OtmResource.SUBGROUP );
        return candidates;
    }


    public String getReferenceFacetName() {
        return getTL().getReferenceFacetName() != null ? getTL().getReferenceFacetName() : "";
    }

    private Node getReferenceFacetNode() {
        StringProperty selection = null;
        if (!getReferenceFacetName().isEmpty())
            selection = getActionManager().add( DexActions.SETAFREFERENCEFACET, getReferenceFacetName(), this );
        else
            selection = getActionManager().add( DexActions.SETAFREFERENCEFACET, OtmResource.SUBGROUP, this );
        return DexEditField.makeComboBox( getReferenceFacetCandidates(), selection );

        // String selection = OtmResource.SUBGROUP;
        // if (!getReferenceFacetName().isEmpty())
        // selection = getReferenceFacetName();
        // ComboBox<String> box = DexEditField.makeComboBox( getReferenceFacetCandidates(), selection, this );
        // box.setOnAction( a -> log.debug( "Reference Facet Selected" ) );
        // return box;
    }

    public TLReferenceType getReferenceType() {
        return getTL().getReferenceType() != null ? getTL().getReferenceType() : TLReferenceType.NONE;
    }

    /**
     * @return all TL Modeled strings or for abstract resources just the first string (none)
     */
    public ObservableList<String> getReferenceTypeCandidates() {
        ObservableList<String> candidates = FXCollections.observableArrayList();
        if (getOwningMember().isAbstract())
            candidates.add( TLReferenceType.values()[0].toString() );
        else
            for (TLReferenceType value : TLReferenceType.values())
                candidates.add( value.toString() );
        return candidates;
    }

    private Node getReferenceTypeNode() {
        StringProperty selection = null;
        if (getReferenceType() != null)
            selection = getActionManager().add( DexActions.SETAFREFERENCETYPE, getReferenceTypeString(), this );
        else
            selection =
                getActionManager().add( DexActions.SETAFREFERENCETYPE, TLReferenceType.values()[0].toString(), this );
        return DexEditField.makeComboBox( getReferenceTypeCandidates(), selection );

        // String selection = TLReferenceType.values()[0].toString();
        // if (getReferenceType() != null)
        // selection = getReferenceType().toString();
        // ComboBox<String> box = DexEditField.makeComboBox( getReferenceTypeCandidates(), selection, this );
        // box.setOnAction( a -> log.debug( "Type Selected" ) );
        // return box;
    }

    public String getReferenceTypeString() {
        return getReferenceType() != null ? getReferenceType().toString() : "";
    }

    private Node getRepeatCountNode() {
        Spinner<Integer> spinner = new Spinner<>( 0, 10000, getRepeatCount() ); // min, max, init
        spinner.setDisable( !isEditable() );
        spinner.setEditable( isEditable() );
        spinner.getEditor().setOnAction( a -> spinnerListener( spinner ) );
        spinner.focusedProperty().addListener( (o, old, newV) -> spinnerListener( spinner ) );
        return spinner;
    }

    /**
     * Handle focus change and typing <Enter>. If value changed, run action.
     * 
     * @param spinner
     */
    private void spinnerListener(Spinner<Integer> spinner) {
        if (spinner != null) {
            // If they type, the editor will access the value
            int value = Integer.parseInt( spinner.getEditor().getText() );
            if (spinner.getValue() != value)
                spinner.getValueFactory().setValue( value );

            // If the value changed, run the action
            if (spinner.getValue() != getRepeatCount()) {
                getActionManager().run( DexActions.SETAFREFERENCEFACETCOUNT, this, spinner.getValue() );
            }
        }
    }

    /**
     * 
     * @return
     */
    public OtmObject getRequestPayload() {
        // For a request, the "payloadType" indicates which action facet.
        // This action facet defines the root element.
        OtmObject rqPayload = null;
        String rfName = getReferenceFacetName();
        OtmBusinessObject subject = getOwningMember().getSubject();
        OtmLibraryMember base = getBasePayload(); // creates wrapper object

        // Early Exit Conditions
        // Don't care about reference type - that is only for responses
        // if (getReferenceType() == TLReferenceType.NONE)
        // return null; // User set to have No request payload
        if (subject == null)
            return rqPayload; // Error, resource is not complete
        if (rfName.equals( OtmResource.SUBGROUP ) || rfName.isEmpty())
            return subject; // The whole object is the substitution group

        rqPayload = getReferenceFacet();
        return rqPayload;
    }

    /**
     * Apply the rules to determine what type of object will be in the response
     * 
     * @return
     */
    public OtmObject getResponsePayload(OtmActionResponse response) {
        TLActionResponse source = response.getTL();
        NamedEntity plt = ResourceCodegenUtils.getPayloadType( source.getPayloadType() );
        return plt != null ? OtmModelElement.get( (TLModelElement) plt ) : this;
    }

    @Override
    public TLActionFacet getTL() {
        return (TLActionFacet) tlObject;
    }

    @Override
    public String getName() {
        return getTL().getName(); // Override the localName used by supertype
    }

    public Tooltip getTooltip() {
        return new Tooltip( TOOLTIP );
    }

    public void setBasePayload(OtmObject basePayload) {
        // Must be core or choice object
        if (basePayload != null && basePayload.getTL() instanceof NamedEntity)
            getTL().setBasePayload( (NamedEntity) basePayload.getTL() );
        else
            getTL().setBasePayload( null );
    }

    @Override
    public String setName(String name) {
        getTL().setName( name );
        // isValid( true );
        return getName();
    }

    public String setReferenceFacetName(String name) {
        getTL().setReferenceFacetName( name );
        // log.debug( "Set reference facet to " + name );
        return getReferenceFacetName();
    }

    /**
     * Set the tlReferenceFacetName to the identity name of the facet. If set to null, then the object is used instead
     * of facet (substitution group)
     * 
     * @param facet
     */
    public void setReferenceFacet(OtmFacet<?> facet) {
        String name = "";
        if (facet == null)
            name = null;
        // name = OtmResource.SUBGROUP;
        else
            name = ResourceCodegenUtils.getActionFacetReferenceName( facet.getTL() );
        getTL().setReferenceFacetName( name );
        // log.debug( "Setting reference facet name to: " + name );
    }

    public TLReferenceType setReferenceType(TLReferenceType type) {
        if (type == null)
            type = TLReferenceType.NONE;
        getTL().setReferenceType( type );
        // log.debug( "Set reference type to " + getReferenceType() );
        return getReferenceType();
    }

    /**
     * @param value
     */
    public TLReferenceType setReferenceTypeString(String value) {
        TLReferenceType type = null;
        for (TLReferenceType t : TLReferenceType.values())
            if (t.toString().equals( value ))
                type = t;
        return setReferenceType( type );
    }



    /**
     * Not-implemented
     * 
     * @see org.opentravel.model.OtmTypeUser#assignedTypeProperty()
     */
    @Override
    public StringProperty assignedTypeProperty() {
        return null;
    }


    /**
     * Not-implemented
     * 
     * @see org.opentravel.model.OtmTypeUser#getAssignedTLType()
     */
    @Override
    public NamedEntity getAssignedTLType() {
        return null;
    }


    /**
     * @see org.opentravel.model.OtmTypeUser#getAssignedType()
     */
    @Override
    public OtmTypeProvider getAssignedType() {
        return (OtmTypeProvider) getBasePayload();
    }


    /**
     * Not-implemented
     * 
     * @see org.opentravel.model.OtmTypeUser#getTlAssignedTypeName()
     */
    @Override
    public String getTlAssignedTypeName() {
        return null;
    }


    /**
     * Not-implemented
     * 
     * @see org.opentravel.model.OtmTypeUser#setAssignedTLType(org.opentravel.schemacompiler.model.NamedEntity)
     */
    @Override
    public NamedEntity setAssignedTLType(NamedEntity type) {
        return null;
    }


    /**
     * @see org.opentravel.model.OtmTypeUser#setAssignedType(org.opentravel.model.OtmTypeProvider)
     */
    @Override
    public OtmTypeProvider setAssignedType(OtmTypeProvider type) {
        setBasePayload( type );
        return getAssignedType();
    }


    /**
     * Not-implemented
     * 
     * @see org.opentravel.model.OtmTypeUser#setTLTypeName(java.lang.String)
     */
    @Override
    public void setTLTypeName(String name) {}

}
