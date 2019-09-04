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

package org.opentravel.model.otmLibraryMembers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.DexEditField;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.resource.DexParentRefsEndpointMap;
import org.opentravel.model.resource.OtmAction;
import org.opentravel.model.resource.OtmActionFacet;
import org.opentravel.model.resource.OtmActionRequest;
import org.opentravel.model.resource.OtmActionResponse;
import org.opentravel.model.resource.OtmParameterGroup;
import org.opentravel.model.resource.OtmParentRef;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

/**
 * OTM Object for Resource objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmResource extends OtmLibraryMemberBase<TLResource> implements OtmTypeUser {
    private static Log log = LogFactory.getLog( OtmResource.class );

    private DexParentRefsEndpointMap parentRefsEndpointMap;
    private ResourceCodegenUtils codegenUtils;
    public static final String NONE = "None";
    public static final String SUBGROUP = "Substitution Group";

    public OtmResource(TLResource tlo, OtmModelManager mgr) {
        super( tlo, mgr );
        modelChildren();
        // Do not build on construction - all parents may not exist
        // parentRefsEndpointMap = new DexParentRefsEndpointMap( this );
    }

    public DexParentRefsEndpointMap getParentRefEndpointsMap() {
        if (parentRefsEndpointMap == null)
            parentRefsEndpointMap = new DexParentRefsEndpointMap( this );
        return parentRefsEndpointMap;
    }

    public String getPayloadExample(OtmActionRequest request) {
        return DexParentRefsEndpointMap.getPayloadExample( request );
    }

    public String getPayloadExample(OtmActionResponse response) {
        return DexParentRefsEndpointMap.getPayloadExample( response );
    }

    /**
     * Get the parent refs and all of the ancestors.
     * 
     * @param firstClassOnly
     * @return new list of parent refs that may be empty
     */
    public List<OtmParentRef> getAllParentRefs(boolean firstClassOnly) {
        List<OtmParentRef> refs = new ArrayList<>();
        getParentRefs().forEach( pr -> {
            if (!firstClassOnly || pr.isParentFirstClass())
                refs.add( pr );
            if (pr.getParentResource() != null)
                refs.addAll( pr.getParentResource().getAllParentRefs( firstClassOnly ) );
        } );
        return refs;
    }

    /**
     * Get a list of all sub-resources of this resource.
     * <p>
     * Compute intensive: examines list from model manager and filter out non-sub resources.
     * 
     * @return a list of all resources that have this resource in its paths or empty list
     */
    public List<OtmResource> getAllSubResources() {
        List<OtmResource> candidates = new ArrayList<>();
        List<OtmResource> subResources = new ArrayList<>();
        // Only resources with parentRefs could be sub-resources
        for (OtmResource r : getModelManager().getResources( false ))
            if (!r.getParentRefs().isEmpty())
                candidates.add( r );
        // Get first generation and recurse
        for (OtmResource c : candidates)
            for (OtmParentRef pr : c.getParentRefs())
                if (pr.getParentResource() == this) {
                    subResources.add( c );
                    subResources.addAll( c.getAllSubResources() ); // recurse
                }
        return subResources;
    }

    public OtmResource(String name, OtmModelManager mgr) {
        super( new TLResource(), mgr );
        setName( name );
    }

    /**
     * Set the base path on this resource. If override is true, do all the action resources also.
     * 
     * @param basePath
     * @param override
     */
    public void setBasePath(String basePath, boolean override) {
        getTL().setBasePath( basePath );
        if (override)
            getActionRequests().forEach( ar -> ar.setPathTemplate( basePath, override ) );
        refresh( true );
    }

    public String getBasePath() {
        return getTL().getBasePath();
    }

    public StringProperty basePathProperty() {
        return getActionManager() != null ? getActionManager().add( DexActions.BASEPATHCHANGE, getBasePath(), this )
            : new ReadOnlyStringWrapper( getBasePath() );

        // Question - what to use as action enum? Field or base path?
        // StringProperty bpProperty = new ReadOnlyStringWrapper( getBasePath() );
        // if (isEditable() && getActionManager() != null) {
        // bpProperty = new SimpleStringProperty( getBasePath() );
        // getActionManager().addAction( DexActions.BASEPATHCHANGE, bpProperty, this );
        // }
        // return bpProperty;
    }

    @Override
    public String setName(String name) {
        getTL().setName( name );
        isValid( true );
        return getName();
    }

    @Override
    public TLResource getTL() {
        return (TLResource) tlObject;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE;
    }

    public Tooltip getTooltip() {
        return new Tooltip( TOOLTIP );
    }

    @Override
    public OtmResource getBaseType() {
        return (OtmResource) super.getBaseType();
    }

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

    @Override
    public OtmLibraryMember getOwningMember() {
        return this;
    }

    @Override
    public boolean isExpanded() {
        return true;
    }

    @Override
    public boolean isNameControlled() {
        return false;
    }

    @Override
    public OtmResourceChild add(OtmObject child) {
        if (child instanceof OtmResourceChild) {
            // Make sure it has not already been added
            if (children == null)
                children = new ArrayList<>();
            else if (contains( children, child ))
                return null;

            if (inheritedChildren == null)
                inheritedChildren = new ArrayList<>();
            else if (contains( inheritedChildren, child ))
                return null;

            if (!child.isInherited())
                children.add( child );
            else
                inheritedChildren.add( child );
            return (OtmResourceChild) child;
        }
        return null;
    }


    /**
     * @see org.opentravel.model.otmLibraryMembers.OtmLibraryMemberBase#modelChildren()
     */
    @Override
    public void modelChildren() {
        getTL().getActionFacets().forEach( a -> new OtmActionFacet( a, this ) );
        getTL().getActions().forEach( a -> new OtmAction( a, this ) );
        getTL().getParamGroups().forEach( a -> new OtmParameterGroup( a, this ) );
        getTL().getParentRefs().forEach( a -> new OtmParentRef( a, this ) );

        // log.debug( "Modeled " + children.size() + " resource children for " + getName() );
    }

    /** ************************************** */
    /**
     * @see org.opentravel.model.OtmTypeUser#assignedTypeProperty()
     */
    @Override
    public StringProperty assignedTypeProperty() {
        String typeName = "";
        if (getAssignedType() != null)
            typeName = getAssignedType().getName();
        return new ReadOnlyStringWrapper( typeName );
    }

    /**
     * Returns the business object reference
     * 
     * @see org.opentravel.model.OtmTypeUser#getAssignedTLType()
     */
    @Override
    public NamedEntity getAssignedTLType() {
        return getTL().getBusinessObjectRef();
    }

    /**
     * @see org.opentravel.model.OtmTypeUser#getAssignedType()
     */
    @Override
    public OtmBusinessObject getAssignedType() {
        return (OtmBusinessObject) OtmModelElement.get( (TLModelElement) getAssignedTLType() );
    }

    public OtmBusinessObject getBusinessObject() {
        return getAssignedType();
    }

    public String getBusinessObjectName() {
        return getAssignedType() != null ? getAssignedType().getName() : "";
    }

    /**
     * {@inheritDoc} Returns the TL business object reference name not getName() of the assigned type
     * 
     * @see org.opentravel.model.OtmTypeUser#getTlAssignedTypeName()
     */
    @Override
    public String getTlAssignedTypeName() {
        return getTL().getBusinessObjectRefName() != null ? getTL().getBusinessObjectRefName() : "";
    }

    /**
     * @see org.opentravel.model.OtmTypeUser#setAssignedTLType(org.opentravel.schemacompiler.model.NamedEntity)
     */
    @Override
    public NamedEntity setAssignedTLType(NamedEntity type) {
        if (type instanceof TLBusinessObject) {
            getTL().setBusinessObjectRef( (TLBusinessObject) type );
            return type;
        }
        return null;
    }

    /**
     * {@inheritDoc} For resources, <b>must</b> be a business object.
     * 
     * @see org.opentravel.model.OtmTypeUser#setAssignedType(org.opentravel.model.OtmTypeProvider)
     */
    @Override
    public OtmTypeProvider setAssignedType(OtmTypeProvider type) {
        OtmLibraryMember oldUser = getAssignedType() == null ? null : getAssignedType().getOwningMember();
        if (setAssignedTLType( (NamedEntity) type.getTL() ) != null)
            // add to type's typeUsers
            type.getOwningMember().addWhereUsed( oldUser, getOwningMember() );

        return getAssignedType();
    }

    /**
     * Get facets from subject business object. Replace contributed facets with their contributor.
     * 
     * @return a non-null list of subject facets and contextual facets.
     */
    public List<OtmObject> getSubjectFacets() {
        List<OtmObject> facets = null;
        if (getSubject() != null) {
            facets = new ArrayList<>();
            for (OtmObject object : getSubject().getChildren()) {
                if (object instanceof OtmFacet) {
                    if (object instanceof OtmContributedFacet)
                        object = ((OtmContributedFacet) object).getContributor();
                    if (object != null)
                        facets.add( object );
                }
            }
        }
        return facets != null ? facets : Collections.emptyList();
    }

    /**
     * Get the name of the business object. First trys the business object and if that is missing, trys the business
     * object reference name field.
     * 
     * @return name of the business object assigned or empty string
     */
    public String getSubjectName() {
        if (getAssignedType() == null)
            return getTlAssignedTypeName();
        return getAssignedType().getName();
    }

    /**
     * Convenience method for {@link #getAssignedType()}
     * 
     * @return business object assigned
     */
    public OtmBusinessObject getSubject() {
        return getAssignedType();
    }

    /**
     * Convenience method for {@link #setAssignedType(OtmTypeProvider)}
     * 
     * @param subject
     */
    public OtmBusinessObject setSubject(OtmBusinessObject subject) {
        OtmBusinessObject result = (OtmBusinessObject) setAssignedType( subject );
        refresh( true );
        return result;
    }

    /**
     * {@link TLResource#setExtension()}
     * 
     * @param extensionName
     */
    public OtmResource setExtendedResource(OtmResource superType) {
        if (superType == null)
            getTL().setExtension( null );
        else {
            TLExtension extension = getTL().getExtension();
            if (extension == null) {
                extension = new TLExtension();
                getTL().setExtension( extension );
            }
            extension.setExtendsEntity( superType.getTL() );
        }
        log.debug( "Set extension to " + getExtendedResource() );
        return getExtendedResource();
    }

    public OtmResource setExtendedResourceString(String extensionName) {
        OtmResource superType = null;
        // get the candidate list and match one
        for (OtmResource c : getModelManager().getResources( true ))
            if (c.getNameWithPrefix().equals( extensionName ))
                superType = c;

        return setExtendedResource( superType );
    }

    /**
     * @see org.opentravel.model.OtmTypeUser#setTLTypeName(java.lang.String)
     */
    @Override
    public void setTLTypeName(String name) {
        // no-op
    }

    /**
     * Get all the actions in the resource
     * 
     * @return new list of OtmAction
     */
    public List<OtmActionRequest> getActionRequests() {
        List<OtmActionRequest> requests = new ArrayList<>();
        // List<TLAction> tlas = getTL().getActions();
        for (TLAction ta : getTL().getActions()) {
            if (OtmModelElement.get( ta ) instanceof OtmAction
                && ((OtmAction) OtmModelElement.get( ta )).getRequest() != null)
                requests.add( ((OtmAction) OtmModelElement.get( ta )).getRequest() );
        }
        return requests;
    }

    public OtmResource getExtendedResource() {
        OtmResource extended = null;
        if (getTL().getExtension() != null) {
            OtmObject r = OtmModelElement.get( (TLModelElement) getTL().getExtension().getExtendsEntity() );
            if (r instanceof OtmResource)
                extended = (OtmResource) r;
        }
        return extended;
    }

    /**
     * Get the name with prefix of the extended resource or else "None"
     * 
     * @return
     */
    public String getExtendedResourceName() {
        return getExtendedResource() == null ? NONE : getExtendedResource().getNameWithPrefix();
    }

    /**
     * 
     * If this is a minor version, returns ONLY the name of the extension object
     * 
     * @return an array of other resource {@link #getNameWithPrefix()} including NONE
     */
    public ObservableList<String> getExtensionCandidates() {
        ObservableList<String> candidates = FXCollections.observableArrayList();
        // TODO - handle minor version case
        candidates.add( NONE );
        getModelManager().getResources( true ).forEach( r -> candidates.add( r.getNameWithPrefix() ) );
        candidates.remove( getNameWithPrefix() );
        return candidates;
    }

    private Node getExtensionNode() {
        StringProperty selection = null;
        if (getExtendedResource() != null)
            selection = getActionManager().add( DexActions.SETRESOURCEEXTENSION,
                getExtendedResource().getNameWithPrefix(), this );
        else
            selection = getActionManager().add( DexActions.SETRESOURCEEXTENSION, NONE, this );
        return DexEditField.makeComboBox( getExtensionCandidates(), selection );
    }

    private Node getSubectNode() {
        Button button = new Button( getSubjectName() );
        button.setDisable( !isEditable() );
        button.setOnAction( a -> assignSubject() );
        return button;
    }

    private void assignSubject() {
        log.debug( "Button selected" );
        getActionManager().run( DexActions.ASSIGNSUBJECT, this );
    }

    private Node getBasePathNode() {
        TextField field = DexEditField.makeTextField( basePathProperty() );
        // TextField field = DexEditField.makeTextField( getBasePath(), this );
        if (basePathProperty().isEmpty().get())
            field.setPromptText( basePath_PROMPT );
        // field.setOnAction( a -> setBasePath( "NEW PATH", true ) );
        return field;
    }



    public boolean isAbstract() {
        return getTL().isAbstract();
    }

    private Node getIsAbstractNode() {
        BooleanProperty abstractProperty = getActionManager().add( DexActions.SETABSTRACT, isAbstract(), this );
        return DexEditField.makeCheckBox( abstractProperty, abstract_LABEL );
    }

    public boolean isFirstClass() {
        return getTL().isFirstClass();
    }

    private Node getIsFirstClassNode() {
        // SimpleBooleanProperty firstClassProperty = new SimpleBooleanProperty( isFirstClass() );
        BooleanProperty firstClassProperty = getActionManager().add( DexActions.SETFIRSTCLASS, isFirstClass(), this );
        // getActionManager().addAction( DexActions.BASEPATHCHANGE, firstClassProperty, this );
        CheckBox box = DexEditField.makeCheckBox( firstClassProperty, firstClass_LABEL );
        // CheckBox box = DexEditField.makeCheckBox( isFirstClass(), firstClass_LABEL, this );
        // box.setOnAction( a -> log.debug( "First class check box selected." ) );
        return box;
    }

    @Override
    public boolean isEditable() {
        return getLibrary() != null && getLibrary().isEditable();
        // return getName().startsWith( "S" ); // testing only
    }

    // FIXME
    // Add namespace to fields.
    /**
     * Get the fields for this object.
     * 
     * @param ec controller that can fire events when resource changes
     * @return
     */
    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        fields.add( new DexEditField( 0, 0, extension_LABEL, extension_TOOLTIP, getExtensionNode() ) );
        fields.add( new DexEditField( 1, 0, businessObject_LABEL, businessObject_TOOLTIP, getSubectNode() ) );
        fields.add( new DexEditField( 2, 0, basePath_LABEL, basePath_TOOLTIP, getBasePathNode() ) );
        fields.add( new DexEditField( 3, 0, null, abstract_TOOLTIP, getIsAbstractNode() ) );
        fields.add( new DexEditField( 3, 1, null, firstClass_TOOLTIP, getIsFirstClassNode() ) );
        return fields;

    }

    // public List<DexEditField> getFields() {
    // List<DexEditField> fields = new ArrayList<>();
    // fields.add( new DexEditField( 0, 0, extension_LABEL, extension_TOOLTIP, getExtensionNode() ) );
    // fields.add( new DexEditField( 1, 0, businessObject_LABEL, businessObject_TOOLTIP, getSubectNode() ) );
    // fields.add( new DexEditField( 2, 0, basePath_LABEL, basePath_TOOLTIP, getBasePathNode() ) );
    // fields.add( new DexEditField( 3, 0, null, abstract_TOOLTIP, getIsAbstractNode( null ) ) );
    // fields.add( new DexEditField( 3, 1, null, firstClass_TOOLTIP, getIsFirstClassNode( null ) ) );
    //
    // // fields.add( new DexEditField( 1, 0, name_LABEL, name_TOOLTIP, new Button() ) );
    // // fields.add( new DexEditField( 1, 0, parentRef_LABEL, parentRef_TOOLTIP, new Button() ) );
    // // fields.add( new DexEditField( 1, 0, parent_LABEL, parent_TOOLTIP, new Button() ) );
    // return fields;
    // }

    private static final String TOOLTIP =
        "Encapsulates all aspects of a RESTful resource used to expose and manage a particular business object.";

    // private static final String name_LABEL = "Resource Name";
    // private static final String name_TOOLTIP =
    // "The name of the resource. This name is used to uniquely identify the resource within the OTM model, but will not
    // conflict with any naming conventions used in generated XSD documents.";

    private static final String businessObject_LABEL = "Business Object";
    private static final String businessObject_TOOLTIP =
        "The name of the business object with which this resource is associated. ";

    private static final String abstract_LABEL = "Abstract";
    private static final String abstract_TOOLTIP = "Indicates whether this is an abstract resource.";

    private static final String firstClass_LABEL = "First Class";
    private static final String firstClass_TOOLTIP =
        "Indicates whether this is a first-class resource. If checked the generated SWAGGER will have paths with and without the parent resource. First class resources may exist independently of a parent resource.";

    private static final String basePath_LABEL = "Base Path";
    private static final String basePath_TOOLTIP =
        "Specifies the base path for this resource. Changing will cause changes to action request path templates. ";
    private static final String basePath_PROMPT = "Enter / to use the business object name as the collection name.";

    // private static final String parentRef_LABEL = "Parent";
    // private static final String parentRef_TOOLTIP = " The list of parent references for the resource. ";

    private static final String extension_LABEL = "Extends";
    private static final String extension_TOOLTIP =
        "Reference to the resource from which the child resource will inherit.";

    private static final String parent_LABEL = "Parent";
    private static final String parent_TOOLTIP = "Reference to the parent resource for this sub-resource.";

    private static final String baseResponseWizard_LABEL = "Base Response Wizard";
    private static final String baseResponseWizard_TOOLTIP =
        "Set base response on all Action Facets used for responses.";

    /**
     * @return non-null list of parent refs
     */
    public List<OtmParentRef> getParentRefs() {
        List<OtmParentRef> parents = new ArrayList<>();
        getTL().getParentRefs().forEach( pr -> {
            if (OtmModelElement.get( pr ) instanceof OtmParentRef)
                parents.add( (OtmParentRef) OtmModelElement.get( pr ) );
        } );
        return parents;
    }

    public OtmParentRef addParentRef(OtmResource parent) {
        // Do we need to test if already done?
        // Create the TL parent ref and set to
        TLResourceParentRef tlParentRef = new TLResourceParentRef();
        // Set initial path template
        tlParentRef.setPathTemplate( parent.getBasePath() );

        tlParentRef.setParentResource( parent.getTL() );
        OtmParentRef parentRef = new OtmParentRef( tlParentRef, this );

        // parentRef.setOwner( parent.getTL() );
        getTL().addParentRef( parentRef.getTL() );
        add( parentRef );
        refresh( true );
        return parentRef;
    }

    /**
     * Something changed in this resource so refresh it. To refresh all who use this resource as parent
     * {@link #refresh(boolean)}
     */
    public void refresh() {
        refresh( false );
    }

    /**
     * Something changed in this resource so refresh it.
     * 
     * @param deep if true, refresh all sub resources too. Sub-resources have this resource in their paths
     */
    public void refresh(boolean deep) {
        log.debug( "Deep refesh of " + this );
        parentRefsEndpointMap = null; // build on next access

        if (deep)
            getAllSubResources().forEach( sr -> sr.refresh( true ) );
    }

    public void addParameterGroup(OtmParameterGroup group) {
        if (group != null)
            getTL().addParamGroup( group.getTL() );
        add( group ); // Add to children list
        group.setParent( this );
    }

    public List<OtmParameterGroup> getParameterGroups() {
        List<OtmParameterGroup> groups = new ArrayList<>();
        getTL().getParamGroups().forEach( pg -> {
            if (OtmModelElement.get( pg ) instanceof OtmParameterGroup)
                groups.add( (OtmParameterGroup) OtmModelElement.get( pg ) );
        } );
        return groups;
    }

    public List<OtmActionFacet> getActionFacets() {
        List<OtmActionFacet> actionFacets = new ArrayList<>();
        getTL().getActionFacets().forEach( af -> {
            if (OtmModelElement.get( af ) instanceof OtmActionFacet)
                actionFacets.add( (OtmActionFacet) OtmModelElement.get( af ) );
        } );
        return actionFacets;
    }

    /**
     * @return
     */
    public List<OtmAction> getActions() {
        List<OtmAction> actions = new ArrayList<>();
        getTL().getActions().forEach( ta -> {
            if (OtmModelElement.get( ta ) instanceof OtmAction)
                actions.add( (OtmAction) OtmModelElement.get( ta ) );
        } );
        return actions;
    }

    /**
     * @param b
     */
    public void setFirstClass(boolean b) {
        getTL().setFirstClass( b );
        refresh( true );
        log.debug( "First class set to " + isFirstClass() );
    }

    /**
     * @param b
     */
    public void setAbstract(boolean b) {
        getTL().setAbstract( b );
        refresh( true );
    }
}
