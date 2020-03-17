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
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.model.TLReferenceType;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;

/**
 * Edit Field manager for Action Facets.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmActionFacetFieldManager {
    private static Log log = LogFactory.getLog( OtmActionFacetFieldManager.class );

    private static final String REFERENCE_FACET_LABEL = "Reference Facet";
    private static final String REFERENCE_FACET_TOOLTIP =
        "Specifies the name of the business object facet to be referenced in the message.  If the Reference Type value is None this value will be ignored. ";

    private static final String REPEAT_COUNT_LABEL = "Repeat Count";
    private static final String REPEAT_COUNT_TOOLTIP =
        "Specifies the maximum number of times that the business object reference should repeat in the message. Best practices state that this string value should contain a positive number that is greater than or equal to 1. ";

    private static final String REFERENCE_TYPE_LABEL = "Reference Type";
    private static final String REFERENCE_TYPE_TOOLTIP = "Reference type";

    private static final String BASE_PAYLOAD_LABEL = "Base Payload";
    private static final String BASE_PAYLOAD_TOOLTIP =
        " Optional reference to a core or choice object that indicates the basic structure of the message payload. If the 'referenceType' value is NONE, this will indicate the entirity of the message structure.  For reference type values other than NONE, the message structure will include all elements of the base payload, plus reference(s) to the owning resource's business ";

    private OtmActionFacet af;

    public OtmActionFacetFieldManager(OtmActionFacet af) {
        this.af = af;
    }

    private Node getBasePayloadNode() {
        String name = OtmResource.NONE;
        if (af.getBasePayload() != null)
            name = af.getBasePayload().getNameWithPrefix();
        Button button = new Button( name );
        button.setDisable( !af.isEditable() );
        button.setOnAction( a -> af.getActionManager().run( DexActions.TYPECHANGE, af ) );
        return button;
    }

    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        fields.add( new DexEditField( 0, 0, BASE_PAYLOAD_LABEL, BASE_PAYLOAD_TOOLTIP, getBasePayloadNode() ) );
        fields.add( new DexEditField( 0, 2, null, "Remove base payload.", getRemoveBasePayloadNode() ) );
        fields.add( new DexEditField( 1, 0, REFERENCE_TYPE_LABEL, REFERENCE_TYPE_TOOLTIP, getReferenceTypeNode() ) );
        fields.add( new DexEditField( 2, 0, REFERENCE_FACET_LABEL, REFERENCE_FACET_TOOLTIP, getReferenceFacetNode() ) );
        fields.add( new DexEditField( 3, 0, REPEAT_COUNT_LABEL, REPEAT_COUNT_TOOLTIP, getRepeatCountNode() ) );
        return fields;
    }

    /**
     * List of facet names on subject and entry for the substitution group
     * 
     * @return list of facets on the subject business object
     */
    private ObservableList<String> getReferenceFacetCandidates() {
        ObservableList<String> candidates = FXCollections.observableArrayList();
        if (af.getOwningMember() != null)
            af.getOwningMember().getSubjectFacets().forEach( f -> candidates.add( f.getName() ) );
        candidates.add( OtmResource.SUBGROUP );
        return candidates;
    }

    private Node getReferenceFacetNode() {
        StringProperty selection = null;
        if (!af.getReferenceFacetName().isEmpty())
            selection = af.getActionManager().add( DexActions.SETAFREFERENCEFACET, af.getReferenceFacetName(), af );
        else
            selection = af.getActionManager().add( DexActions.SETAFREFERENCEFACET, OtmResource.SUBGROUP, af );
        return DexEditField.makeComboBox( getReferenceFacetCandidates(), selection );
    }

    /**
     * @return all TL Modeled strings or for abstract resources just the first string (none)
     */
    protected ObservableList<String> getReferenceTypeCandidates() {
        ObservableList<String> candidates = FXCollections.observableArrayList();
        if (af.getOwningMember().isAbstract())
            candidates.add( TLReferenceType.values()[0].toString() );
        else
            for (TLReferenceType value : TLReferenceType.values())
                candidates.add( value.toString() );
        return candidates;
    }

    private Node getReferenceTypeNode() {
        StringProperty selection = null;
        if (af.getReferenceType() != null)
            selection = af.getActionManager().add( DexActions.SETAFREFERENCETYPE, af.getReferenceTypeString(), af );
        else
            selection =
                af.getActionManager().add( DexActions.SETAFREFERENCETYPE, TLReferenceType.values()[0].toString(), af );
        return DexEditField.makeComboBox( getReferenceTypeCandidates(), selection );
    }

    private Node getRemoveBasePayloadNode() {
        Button button = new Button( "-Remove-" );
        button.setDisable( !af.isEditable() || af.getBasePayload() == null );
        button.setOnAction( a -> af.getActionManager().run( DexActions.REMOVEAFBASEPAYLOAD, af ) );
        return button;
    }


    private Node getRepeatCountNode() {
        Spinner<Integer> spinner = new Spinner<>( 0, 10000, af.getRepeatCount() ); // min, max, init
        spinner.setDisable( !af.isEditable() );
        spinner.setEditable( af.isEditable() );
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
            if (spinner.getValue() != af.getRepeatCount()) {
                af.getActionManager().run( DexActions.SETAFREFERENCEFACETCOUNT, af, spinner.getValue() );
            }
        }
    }

}
