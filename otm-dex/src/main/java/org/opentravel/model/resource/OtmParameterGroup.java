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
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmFacets.OtmQueryFacet;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParamLocation;
import org.opentravel.schemacompiler.model.TLParameter;

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

    private static final String TOOLTIP =
        "Provides a collection of field references to the resource's business object that will be used as parameters on a REST request.";

    private static final String IDGROUP_LABEL = "ID Group";

    private static final String IDGROUP_TOOLTIP =
        "Indicates whether this group is intended to be used as an identity group for the owning resource.";

    private static final String REFERENCE_FACET_LABEL = "Facet Name";

    private static final String REFERENCE_FACET_TOOLTIP =
        "Name of the business object facet from which all parameters in this group will be referenced. Possible parameters will include any indicators and simple type attributes and elements that are not contained within repeating elements of the given facet or its children.";

    /**
     * Create an Otm Parameter Group, add it to the parent and model its children. Do not change TL object.
     * 
     * @param tla
     * @param parent
     */
    public OtmParameterGroup(TLParamGroup tla, OtmResource parent) {
        super( tla, parent );
        // Model the children to set their identity listeners
        if (parent != null && tla.getOwner() == null)
            parent.getTL().addParamGroup( getTL() );
        modelChildren();
    }

    @Override
    public OtmObject add(OtmObject child) {
        if (child instanceof OtmParameter && !children.contains( child ))
            children.add( child );
        return child;
    }

    /**
     * Add the passed action to the TL resource if not already owned, child list and set action's parent.
     * 
     * @param tlGroup
     * @return
     */
    public OtmParameter add(TLParameter tlParameter) {
        OtmParameter parameter = null;
        if (tlParameter != null && !getTL().getParameters().contains( tlParameter )) {
            getTL().addParameter( tlParameter );
            OtmObject oldParam = OtmModelElement.get( tlParameter );
            // Reuse existing OTM facade if it exists
            if (oldParam instanceof OtmParameter)
                parameter = (OtmParameter) oldParam;
            else
                parameter = new OtmParameter( tlParameter, this );
            // log.debug( "Added parameter to " + this );
            getOwningMember().refresh( true );
        }
        return parameter;
    }

    public void build() {
        if (getOwningMember().getSubject() != null) {
            setReferenceFacet( getOwningMember().getSubject().getIdFacet() );
            setIdGroup( true );
            setName( "Identifier" );
            // ? How to add a parameter ?
            OtmParameter param = new OtmParameter( new TLParameter(), this );
            param.setLocation( TLParamLocation.PATH );
        }
    }

    @Override
    public List<OtmObject> getChildren() {
        if (children != null && children.isEmpty())
            modelChildren();
        return children;
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        return getChildren();
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
    public Collection<OtmPropertyOwner> getDescendantsPropertyOwners() {
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
     * {@inheritDoc}
     */
    @Override
    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        fields.add( new DexEditField( 0, 0, REFERENCE_FACET_LABEL, REFERENCE_FACET_TOOLTIP, getReferenceFacetNode() ) );
        fields.add( new DexEditField( 1, 0, null, IDGROUP_TOOLTIP, getIdGroupNode() ) );
        return fields;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE_PARAMETERGROUP;
    }

    public Node getIdGroupNode() {
        BooleanProperty idGroupProperty = getActionManager().add( DexActions.SETIDGROUP, isIdGroup(), this );
        return DexEditField.makeCheckBox( idGroupProperty, IDGROUP_LABEL );
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        return Collections.emptyList();
    }

    /**
     * Not a named entity, must provide a name
     */
    @Override
    public String getName() {
        return getTL().getName();
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
     * 
     * @return {@link TLParamGroup#getFacetRefName()}
     */
    public String getReferenceFacetName() {
        return getTL().getFacetRefName();
    }

    /**
     * Note: returns OtmObject because not all TLFacets are OtmFacets
     * 
     * @return
     */
    public OtmObject getReferenceFacet() {
        return OtmModelElement.get( getTL().getFacetRef() );
    }

    public Node getReferenceFacetNode() {
        StringProperty selection =
            getActionManager().add( DexActions.SETPARAMETERGROUPFACET, getReferenceFacetName(), this );
        return DexEditField.makeComboBox( getReferenceFacetCandidates(), selection );
    }

    /**
     * Get facet names from subject business object. Omit query facets if this is an ID Parameter Group.
     * 
     * @return a string array of subject facet names.
     */
    protected ObservableList<String> getReferenceFacetCandidates() {
        ObservableList<String> facets = FXCollections.observableArrayList();
        getFacetCandidates().forEach( c -> facets.add( c.getName() ) );
        return facets;
    }

    protected List<OtmObject> getFacetCandidates() {
        List<OtmObject> facets = new ArrayList<>();
        if (getOwningMember() != null) {
            getOwningMember().getSubjectFacets().forEach( f -> {
                if (f != getReferenceFacet()) {
                    if (f instanceof OtmFacet)
                        facets.add( f );
                    else if (f instanceof OtmQueryFacet) {
                        if (!isIdGroup())
                            facets.add( f );
                    } else if (f instanceof OtmContextualFacet)
                        facets.add( f );
                }
            } );
        }
        return facets;
    }

    @Override
    public TLParamGroup getTL() {
        return (TLParamGroup) tlObject;
    }

    public Tooltip getTooltip() {
        return new Tooltip( TOOLTIP );
    }

    /**
     * Is this an ID group. Only Id groups can contribute path parameters to the path template.
     * 
     * @return
     */
    public boolean isIdGroup() {
        return getTL().isIdGroup();
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
        // TODO - model inherited children ??
    }

    public void setIdGroup(boolean value) {
        getTL().setIdGroup( value );
        // log.debug( "Set id group to " + isIdGroup() );
    }

    public String setName(String value) {
        getTL().setName( value );
        // log.debug( "Set name to " + getTL().getName() );
        return getTL().getName();
    }

    /**
     * @param otmResource
     */
    public void setParent(OtmResource otmResource) {
        owner = otmResource;
        parent = null;
    }

    public OtmObject setReferenceFacet(OtmObject facet) {
        if (facet != null && facet.getTL() instanceof TLFacet) {
            getTL().setFacetRef( (TLFacet) facet.getTL() );
            if (nameProperty != null)
                nameProperty.setValue( getName() );
        } else
            getTL().setFacetRef( null );

        log.debug( "Set reference facet to " + getReferenceFacet() );
        return getReferenceFacet();
    }

    public OtmObject setReferenceFacetString(String value) {
        OtmObject f = null;
        for (OtmObject c : getFacetCandidates()) {
            if (c.getName().equals( value ) || c.getNameWithPrefix().equals( value ))
                f = c;
        }
        if (f == null)
            log.debug( "Did not find a facet matching " + value );
        return setReferenceFacet( f );
    }

    @Override
    public void delete(OtmObject param) {
        if (param.getTL() instanceof TLParameter)
            getTL().removeParameter( (TLParameter) param.getTL() );
        remove( param );
    }

    @Override
    public void remove(OtmObject param) {
        children.remove( param );
    }

    /**
     * @return list of path parameters or empty list
     */
    public List<OtmParameter> getPathParameters() {
        List<OtmParameter> params = new ArrayList<>();
        for (OtmParameter p : getParameters())
            if (p.isPathParam())
                params.add( p );
        return params;
    }
}
