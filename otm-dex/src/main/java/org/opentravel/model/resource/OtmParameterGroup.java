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
import org.opentravel.dex.actions.DexActionManager.DexActions;
import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmFacets.OtmQueryFacet;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.model.TLParamGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.BooleanProperty;
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
public class OtmParameterGroup extends OtmResourceChildBase<TLParamGroup>
    implements OtmResourceChild, OtmChildrenOwner {
    private static Log log = LogFactory.getLog( OtmParameterGroup.class );

    public OtmParameterGroup(TLParamGroup tla, OtmResource parent) {
        super( tla, parent );
        // Model the children to set their identity listeners
        modelChildren();
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE_PARAMETERGROUP;
    }

    @Override
    public TLParamGroup getTL() {
        return (TLParamGroup) tlObject;
    }

    public List<OtmParameter> getParameters() {
        List<OtmParameter> list = new ArrayList<>();
        getTL().getParameters().forEach( t -> {
            if (OtmModelElement.get( t ) instanceof OtmParameter)
                list.add( (OtmParameter) OtmModelElement.get( t ) );
        } );
        return list;
    }

    /**
     * Not a named entity, must provide a name
     */
    @Override
    public String getName() {
        return getTL().getName();
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        return getChildren();
    }

    @Override
    public OtmObject add(OtmObject child) {
        if (child instanceof OtmParameter && !children.contains( child ))
            children.add( child );
        return null;
    }

    @Override
    public List<OtmObject> getChildren() {
        if (children != null && children.isEmpty())
            modelChildren();
        return children;
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmTypeProvider> getChildrenTypeProviders() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmChildrenOwner> getDescendantsChildrenOwners() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmTypeProvider> getDescendantsTypeProviders() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmTypeUser> getDescendantsTypeUsers() {
        return Collections.emptyList();
    }

    /**
     * @see org.opentravel.model.OtmChildrenOwner#modelChildren()
     */
    @Override
    public void modelChildren() {
        getTL().getParameters().forEach( p -> new OtmParameter( p, this ) );
    }

    /**
     * @see org.opentravel.model.OtmChildrenOwner#modelInheritedChildren()
     */
    @Override
    public void modelInheritedChildren() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.opentravel.model.OtmChildrenOwner#isExpanded()
     */
    @Override
    public boolean isExpanded() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isIdGroup() {
        return getTL().isIdGroup();
    }

    public Node getIdGroupNode(DexIncludedController<?> ec) {
        BooleanProperty idGroupProperty = getActionManager().add( DexActions.SETIDGROUP, isIdGroup(), this );
        return DexEditField.makeCheckBox( idGroupProperty, IDGROUP_LABEL, ec, this );
    }

    /**
     * Get facet names from subject business object. Omit query facets if this is an ID Parameter Group.
     * 
     * @return a string array of subject facet names.
     */
    protected ObservableList<String> getSubjectFacetCandidates() {
        ObservableList<String> facets = FXCollections.observableArrayList();
        if (getOwningMember() != null) {
            getOwningMember().getSubjectFacets().forEach( f -> {
                if (f instanceof OtmFacet)
                    facets.add( f.getName() );
                else if (f instanceof OtmQueryFacet) {
                    if (!isIdGroup())
                        facets.add( f.getName() );
                } else if (f instanceof OtmContextualFacet)
                    facets.add( f.getName() );
            } );
        }
        return facets;
    }

    public Node getReferenceFacetNode(DexIncludedController<?> ec) {
        StringProperty selection =
            getActionManager().add( DexActions.SETPARAMETERGROUPFACET, getReferenceFacetName(), this );
        return DexEditField.makeComboBox( getSubjectFacetCandidates(), selection, ec, this );

        // ComboBox<String> box = new ComboBox<>( getSubjectFacetCandidates() );
        // box.setEditable( getOwningMember().isEditable() );
        // box.getSelectionModel().select( getTL().getFacetRefName() );
        // return box;
    }

    public String getReferenceFacetName() {
        return getTL().getFacetRefName();
    }

    public void setReferenceFacetString(String value) {
        log.error( "FIXME - Set reference facet to " + value );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DexEditField> getFields(DexIncludedController<?> ec) {
        List<DexEditField> fields = new ArrayList<>();
        fields.add(
            new DexEditField( 0, 0, REFERENCE_FACET_LABEL, REFERENCE_FACET_TOOLTIP, getReferenceFacetNode( ec ) ) );
        fields.add( new DexEditField( 1, 0, null, IDGROUP_TOOLTIP, getIdGroupNode( ec ) ) );
        return fields;
    }

    public Tooltip getTooltip() {
        return new Tooltip( TOOLTIP );
    }

    private static final String TOOLTIP =
        "Provides a collection of field references to the resource's business object that will be used as parameters on a REST request.";
    private static final String IDGROUP_LABEL = "ID Group";
    private static final String IDGROUP_TOOLTIP =
        "Indicates whether this group is intended to be used as an identity group for the owning resource.";
    private static final String REFERENCE_FACET_LABEL = "Facet Name";
    private static final String REFERENCE_FACET_TOOLTIP =
        "Name of the business object facet from which all parameters in this group will be referenced. Possible parameters will include any indicators and simple type attributes and elements that are not contained within repeating elements of the given facet or its children.";

    public void setIdGroup(boolean value) {
        getTL().setIdGroup( value );
        log.debug( "Set id group to " + isIdGroup() );
    }

    /**
     * @param otmResource
     */
    public void setParent(OtmResource otmResource) {
        owner = otmResource;
        parent = null;
    }
}
