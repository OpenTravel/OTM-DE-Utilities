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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.common.DexEditField;
import org.opentravel.common.DexMimeTypeHandler;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.actions.resource.AssignResourceSubjectAction;
import org.opentravel.dex.controllers.popup.DexPopupControllerBase.Results;
import org.opentravel.dex.controllers.popup.TypeSelectionContoller;
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
import org.opentravel.objecteditor.UserCompilerSettings;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 * OTM Object for Resource objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmResource extends OtmLibraryMemberBase<TLResource> implements OtmTypeUser {
    private static Logger log = LogManager.getLogger( OtmResource.class );

    public static final String NONE = "None";
    public static final String SUBGROUP = "Substitution Group";
    private static final String TOOLTIP =
        "Encapsulates all aspects of a RESTful resource used to expose and manage a particular business object.";
    private static final String businessObject_LABEL = "Business Object";
    private static final String businessObject_TOOLTIP =
        "Name and major version of the business object this resource exposes.  ";

    private static final String abstract_LABEL = "Abstract";
    private static final String abstract_TOOLTIP = "Indicates whether this is an abstract resource.";

    private static final String firstClass_LABEL = "First Class";
    private static final String firstClass_TOOLTIP =
        "Indicates whether this is a first-class resource. If checked the generated SWAGGER will have paths with and without the parent resource. First class resources may exist independently of a parent resource.";

    private static final String basePath_LABEL = "Base Path";
    private static final String BASEPATH_TOOLTIP =
        "Specifies the base path for this resource. Will be used as part of basePath in OpenAPI file. ";
    private static final String basePath_PROMPT = "Enter / to use the business object name as the collection name.";

    private static final String extension_LABEL = "Extends";
    private static final String extension_TOOLTIP =
        "Add parentRef to make this a sub-resource. Extension sets the resource from which this resource will inherit action facets and responses.";

    private static final String parent_LABEL = "Parent";
    private static final String parent_TOOLTIP = "Reference to the parent resource for this sub-resource.";

    private static final String baseResponseWizard_LABEL = "Base Response Wizard";
    private static final String baseResponseWizard_TOOLTIP =
        "Set base response on all Action Facets used for responses.";

    private static final String DEFAULT_MIME_LABEL = "Default Mime Types";
    private static final String DEFAULT_MIME_TOOLTIP =
        "Specifies the message MIME type for new actions. Can be changed on each request and response.";

    private static final String DEFAULT_BASE_PAYLOAD_LABEL = "Default Base Payload";
    private static final String DEFAULT_BASE_PAYLOAD_TOOLTIP =
        " Default reference to a core or choice object that indicates the basic structure of the message payload.";

    private DexParentRefsEndpointMap parentRefsEndpointMap;

    // Session storage of mime type defaults.
    // TODO - save these in user settings
    private DexMimeTypeHandler mimeHandler = null;
    private OtmTypeProvider defaultRequestPayload = null;
    private OtmTypeProvider defaultResponsePayload = null;

    public OtmTypeProvider getDefaultRequestPayload() {
        if (defaultRequestPayload == null && getModelManager().getUserSettings() != null) {
            UserCompilerSettings compilerSettings = getModelManager().getUserSettings().getCompilerSettings();
            OtmObject obj = compilerSettings.getDefaultRequestPayload( getModelManager() );
            if (obj instanceof OtmTypeProvider)
                defaultRequestPayload = (OtmTypeProvider) obj;
        }
        return defaultRequestPayload;
    }

    public void setDefaultRequestPayload(OtmTypeProvider payload) {
        defaultRequestPayload = payload;
        if (payload != null && getModelManager().getUserSettings() != null) {
            UserCompilerSettings compilerSettings = getModelManager().getUserSettings().getCompilerSettings();
            compilerSettings.setDefaultRequestPayload( payload );
            getModelManager().getUserSettings().save();
        }
    }

    public OtmTypeProvider getDefaultResponsePayload() {
        if (defaultResponsePayload == null && getModelManager().getUserSettings() != null) {
            UserCompilerSettings compilerSettings = getModelManager().getUserSettings().getCompilerSettings();
            OtmObject obj = compilerSettings.getDefaultResponsePayload( getModelManager() );
            if (obj instanceof OtmTypeProvider)
                defaultResponsePayload = (OtmTypeProvider) obj;
        }
        return defaultResponsePayload;
    }

    public void setDefaultResponsePayload(OtmTypeProvider payload) {
        defaultResponsePayload = payload;
        if (getModelManager().getUserSettings() != null) {
            UserCompilerSettings compilerSettings = getModelManager().getUserSettings().getCompilerSettings();
            compilerSettings.setDefaultResponsePayload( payload );
            getModelManager().getUserSettings().save();
        }
    }

    public DexMimeTypeHandler getMimeHandler() {
        return mimeHandler;
    }

    public OtmResource(String name, OtmModelManager mgr) {
        super( new TLResource(), mgr );
        setName( name );
    }

    public OtmResource(TLResource tlo, OtmModelManager mgr) {
        super( tlo, mgr );
        modelChildren();
        // Factory will add object to mgr

        // Do not build on construction - all parents may not exist yet
        // parentRefsEndpointMap = new DexParentRefsEndpointMap( this );
    }

    /**
     * {@inheritDoc}
     * <p>
     * NOTE: Caller must make sure tlResource has this child - that is a type specific operation
     */
    @Override
    public OtmResourceChild add(OtmObject child) {
        if (child instanceof OtmResourceChild) {
            // Make sure it has not already been added
            if (children == null)
                children = new ArrayList<>();
            else if (contains( children, child ))
                return null;
            // Caller must make sure tlResource has this child - that is a type specific operation
            if (inheritedChildren == null)
                inheritedChildren = new ArrayList<>();
            else if (contains( inheritedChildren, child ))
                return null;

            if (!child.isInherited()) {
                children.add( child );
            } else
                inheritedChildren.add( child );
            return (OtmResourceChild) child;
        }
        return null;
    }


    /**
     * Add the passed action to the TL resource if not already owned, child list and set action's parent. Adds a request
     * if the tlAction does not have one.
     * 
     * @param tlGroup
     * @return
     */
    private OtmAction add(TLAction tlAction) {
        OtmAction action = (OtmAction) OtmModelElement.get( tlAction );;
        if (tlAction != null && !getTL().getActions().contains( tlAction )) {
            getTL().addAction( tlAction );
            if (tlAction.getRequest() == null)
                tlAction.setRequest( new TLActionRequest() );
            if (action == null)
                action = new OtmAction( tlAction, this );
            else
                add( action );
            // log.debug( "Added action to " + this );
            refresh( true );
        }
        return action;
    }

    /**
     * Add the passed action facet to the TL resource if not already owned, child list and set facet's parent.
     * 
     * @param tlGroup
     * @return
     */
    private OtmActionFacet add(TLActionFacet tlAction) {
        OtmActionFacet action = (OtmActionFacet) OtmModelElement.get( tlAction );
        if (tlAction != null && !getTL().getActionFacets().contains( tlAction )) {
            getTL().addActionFacet( tlAction );
            if (action == null)
                action = new OtmActionFacet( tlAction, this );
            else
                add( action );
            // log.debug( "Added action facet to " + this );
            refresh( true );
        }
        return action;
    }

    public OtmResourceChild add(TLModelElement tlChild) {
        OtmResourceChild newChild = null;
        if (tlChild instanceof TLAction)
            newChild = add( (TLAction) tlChild );
        else if (tlChild instanceof TLActionFacet)
            newChild = add( (TLActionFacet) tlChild );
        else if (tlChild instanceof TLParamGroup)
            newChild = add( (TLParamGroup) tlChild );
        else if (tlChild instanceof TLResourceParentRef)
            newChild = add( (TLResourceParentRef) tlChild, null );
        else
            log.warn( "Tried to add unsupported toChild " + tlChild.getClass().getSimpleName() + " to " + this );
        return newChild;
    }

    /**
     * Add the passed group to the TL resource if not already owned, child list and set group's parent.
     * 
     * @param tlGroup
     * @return
     */
    private OtmParameterGroup add(TLParamGroup tlGroup) {
        OtmParameterGroup group = (OtmParameterGroup) OtmModelElement.get( tlGroup );;
        if (tlGroup != null && !getTL().getParamGroups().contains( tlGroup )) {
            getTL().addParamGroup( tlGroup );
            if (group == null)
                group = new OtmParameterGroup( tlGroup, this );
            else
                add( group );
            // log.debug( "Added parameter group to " + this );
            refresh( true );
        }
        return group;
    }

    /**
     * Create a parentRef and set its path template.
     * 
     * @param tlParentRef - existing tl object. if null, a new TLResorceParentRef will be created.
     * @param parent
     * @return
     */
    public OtmParentRef add(TLResourceParentRef tlParentRef, OtmResource parent) {
        // Do we need to test if already done?
        OtmParentRef parentRef = null;

        if (tlParentRef == null)
            tlParentRef = new TLResourceParentRef();

        // ? Should this test first to make sure not duplicate ?
        OtmObject x = OtmModelElement.get( tlParentRef );
        if (!(x instanceof OtmParentRef))
            parentRef = new OtmParentRef( tlParentRef, this );
        else
            parentRef = (OtmParentRef) x;

        if (!getTL().getParentRefs().contains( tlParentRef ))
            getTL().addParentRef( tlParentRef );

        if (parent != null) {
            // Set parent and initial path template
            tlParentRef.setPathTemplate( parent.getBasePath() );
            tlParentRef.setParentResource( parent.getTL() );
        }
        // log.debug( "Added parent reference to " + this );
        refresh( true );
        return parentRef;
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

    private void assignSubject() {
        // log.debug( "Button selected" );
        getActionManager().run( DexActions.ASSIGNSUBJECT, this );
    }

    public StringProperty basePathProperty() {
        return getActionManager() != null ? getActionManager().add( DexActions.BASEPATHCHANGE, getBasePath(), this )
            : new ReadOnlyStringWrapper( getBasePath() );
    }

    /**
     * {@inheritDoc} Add a new parameter group, action facet, and action.
     */
    @Override
    public void build() {
        setFirstClass( true );

        if (getBasePath() == null || getBasePath().isEmpty())
            setBasePath( DexParentRefsEndpointMap.PATH_SEPERATOR );

        OtmParameterGroup pg = new OtmParameterGroup( new TLParamGroup(), this );
        pg.build();

        OtmActionFacet af = null;
        for (org.opentravel.model.resource.OtmActionFacet.BuildTemplate template : OtmActionFacet.BuildTemplate
            .values()) {
            af = new OtmActionFacet( new TLActionFacet(), this );
            af.build( template );
        }

        OtmAction action = null;
        for (OtmAction.BuildTemplate template : OtmAction.BuildTemplate.values()) {
            action = new OtmAction( new TLAction(), this );
            action.build( template );
        }

        // TODO - get defaults from settings
    }


    /**
     * Get the list of action facets from the TL object and return their OtmActionFacet facades.
     * 
     * @return
     */
    public List<OtmActionFacet> getActionFacets() {
        List<OtmActionFacet> actionFacets = new ArrayList<>();
        getTL().getActionFacets().forEach( af -> {
            if (OtmModelElement.get( af ) instanceof OtmActionFacet)
                actionFacets.add( (OtmActionFacet) OtmModelElement.get( af ) );
        } );
        actionFacets.sort( null );
        return actionFacets;
    }

    /**
     * Find either local or inherited action facet.
     * 
     * @param name
     * @return found action facet or null
     */
    public OtmActionFacet getActionFacet(String name) {
        if (name != null && !name.equals( OtmActionRequest.NO_PAYLOAD )) {
            // If there is a local AF with the name, return it.
            for (OtmActionFacet c : getActionFacets())
                if (c.getName().equals( name ))
                    return c;
            // If there is an inherited AF with the name, return it.
            for (OtmActionFacet c : getInheritedActionFacets())
                if (c.getName().equals( name ))
                    return c;
        }
        return null;
    }

    /**
     * TESTING use only. Get all the actions requests in the resource
     * 
     * @return new list of OtmActionRequest
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

    /**
     * @return
     */
    public List<OtmAction> getActions() {
        List<OtmAction> actions = new ArrayList<>();
        getTL().getActions().forEach( ta -> {
            if (OtmModelElement.get( ta ) instanceof OtmAction)
                actions.add( (OtmAction) OtmModelElement.get( ta ) );
        } );
        actions.sort( null );
        return actions;
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

    /**
     * @return the contents of the TL objects base path
     */
    public String getBasePath() {
        return getTL().getBasePath();
    }

    private Node getBasePathNode() {
        TextField field = DexEditField.makeTextField( basePathProperty() );
        // TextField field = DexEditField.makeTextField( getBasePath(), this );
        if (basePathProperty().isEmpty().get())
            field.setPromptText( basePath_PROMPT );
        // field.setOnAction( a -> setBasePath( "NEW PATH", true ) );
        return field;
    }

    @Override
    public OtmResource getBaseType() {
        return (OtmResource) super.getBaseType();
    }

    public OtmBusinessObject getBusinessObject() {
        return getAssignedType();
    }

    public String getBusinessObjectName() {
        return getAssignedType() != null ? getAssignedType().getName() : "";
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
        fields.add( new DexEditField( 2, 0, basePath_LABEL, BASEPATH_TOOLTIP, getBasePathNode() ) );
        fields.add( new DexEditField( 3, 0, null, abstract_TOOLTIP, getIsAbstractNode() ) );
        fields.add( new DexEditField( 3, 1, null, firstClass_TOOLTIP, getIsFirstClassNode() ) );
        //
        fields.add( new DexEditField( 4, 0, null, "", new Separator() ) );
        fields.add( new DexEditField( 4, 1, null, "", new Separator() ) );
        fields.add( new DexEditField( 5, 0, DEFAULT_MIME_LABEL, DEFAULT_MIME_TOOLTIP, getMimeNode() ) );
        fields.add( new DexEditField( 6, 0, DEFAULT_BASE_PAYLOAD_LABEL, DEFAULT_BASE_PAYLOAD_TOOLTIP,
            makeDefaultPayloadBox() ) );

        return fields;

    }

    public HBox makeDefaultPayloadBox() {
        HBox hb = new HBox();
        hb.setSpacing( 10 );
        Label labelRQ = new Label( "Request" );
        Button buttonRQ = new Button( NONE );
        if (getDefaultRequestPayload() != null)
            buttonRQ.setText( getDefaultRequestPayload().getNameWithPrefix() );
        buttonRQ.setDisable( !isEditable() );
        hb.getChildren().add( labelRQ );
        hb.getChildren().add( buttonRQ );

        Label labelRS = new Label( "Response" );
        Button buttonRS = new Button( NONE );
        if (getDefaultResponsePayload() != null)
            buttonRS.setText( getDefaultResponsePayload().getNameWithPrefix() );
        buttonRS.setDisable( !isEditable() );
        hb.getChildren().add( labelRS );
        hb.getChildren().add( buttonRS );

        Label labelClear = new Label( "Clear" );
        Button buttonClear = new Button( "Clear" );
        buttonClear.setDisable( !isEditable() );
        hb.getChildren().add( labelClear );
        hb.getChildren().add( buttonClear );

        buttonRQ.setOnAction( e -> setDefaultRequestPayload( getUserSelectedDefaultPayload( e ) ) );
        buttonRS.setOnAction( e -> setDefaultResponsePayload( getUserSelectedDefaultPayload( e ) ) );
        buttonClear.setOnAction( e -> {
            defaultRequestPayload = null;
            defaultResponsePayload = null;
            buttonRQ.setText( NONE );
            buttonRS.setText( NONE );
        } );
        return hb;
    }

    private OtmTypeProvider getUserSelectedDefaultPayload(ActionEvent e) {
        // Setup controller to get the users selection
        TypeSelectionContoller controller = TypeSelectionContoller.init();
        controller.setManager( mgr );
        controller.getMemberFilterController().setTypeFilterValue( OtmLibraryMemberType.CORE );

        controller.showAndWait( "MSG" );
        OtmTypeProvider selected = controller.getSelectedProvider();
        if (selected != null && controller.getResult() == Results.OK) {
            if (e != null && e.getSource() instanceof Button)
                ((Button) e.getSource()).setText( selected.getNameWithPrefix() );
            return selected;
        } else {
            return null;
        }
    }

    private Node getMimeNode() {
        if (mimeHandler == null)
            mimeHandler = new DexMimeTypeHandler( this, getModelManager().getUserSettings() );
        return mimeHandler.getHBox();
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE;
    }



    private Node getIsAbstractNode() {
        BooleanProperty abstractProperty = getActionManager().add( DexActions.SETABSTRACT, isAbstract(), this );
        return DexEditField.makeCheckBox( abstractProperty, abstract_LABEL );
    }

    private Node getIsFirstClassNode() {
        BooleanProperty firstClassProperty = getActionManager().add( DexActions.SETFIRSTCLASS, isFirstClass(), this );
        return DexEditField.makeCheckBox( firstClassProperty, firstClass_LABEL );
    }

    @Override
    public OtmLibraryMember getOwningMember() {
        return this;
    }

    /**
     * @return new list of the parameter group facades for the ParamGroups on the tlResource
     */
    public List<OtmParameterGroup> getParameterGroups() {
        List<OtmParameterGroup> groups = new ArrayList<>();
        getTL().getParamGroups().forEach( pg -> {
            if (OtmModelElement.get( pg ) instanceof OtmParameterGroup)
                groups.add( (OtmParameterGroup) OtmModelElement.get( pg ) );
        } );
        groups.sort( null );
        return groups;
    }

    /**
     * @return new list of the natural and inherited parameter groups facades for the ParamGroups on the tlResource
     */
    public List<OtmParameterGroup> getParameterGroups(boolean includeInherited) {
        List<OtmParameterGroup> groups = getParameterGroups();
        if (includeInherited)
            groups.addAll( getInheritedParameterGroups() );
        groups.sort( null );
        return groups;
    }

    /**
     * Get the named parameter group from either the locally owned or inherited groups.
     * 
     * @param name
     * @return found parameter group or null
     */
    public OtmParameterGroup getParameterGroup(String name) {
        if (name != null && !name.equals( OtmActionRequest.NO_PARAMETERS )) {
            for (OtmParameterGroup c : getParameterGroups())
                if (c.getName().equals( name ))
                    return c;
            for (OtmParameterGroup c : getInheritedParameterGroups())
                if (c.getName().equals( name ))
                    return c;
        }
        return null;
    }

    public DexParentRefsEndpointMap getParentRefEndpointsMap() {
        if (parentRefsEndpointMap == null)
            parentRefsEndpointMap = new DexParentRefsEndpointMap( this );
        return parentRefsEndpointMap;
    }

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

    /**
     * @return non-null list of inherited parent refs
     */
    public List<OtmParentRef> getInheritedParentRefs() {
        List<OtmParentRef> parents = new ArrayList<>();
        getInheritedChildren().forEach( ic -> {
            if (ic instanceof OtmParentRef)
                parents.add( (OtmParentRef) ic );
        } );
        return parents;
    }


    /**
     * @return non-null list of inherited parameter groups
     */
    public List<OtmParameterGroup> getInheritedParameterGroups() {
        List<OtmParameterGroup> parents = new ArrayList<>();
        getInheritedChildren().forEach( ic -> {
            if (ic instanceof OtmParameterGroup)
                parents.add( (OtmParameterGroup) ic );
        } );
        return parents;
    }

    /**
     * @return non-null list of inherited actions
     */
    public List<OtmAction> getInheritedActions() {
        List<OtmAction> parents = new ArrayList<>();
        getInheritedChildren().forEach( ic -> {
            if (ic instanceof OtmAction)
                parents.add( (OtmAction) ic );
        } );
        return parents;
    }

    /**
     * @return non-null list of inherited action facets
     */
    public List<OtmActionFacet> getInheritedActionFacets() {
        List<OtmActionFacet> parents = new ArrayList<>();
        getInheritedChildren().forEach( ic -> {
            if (ic instanceof OtmActionFacet)
                parents.add( (OtmActionFacet) ic );
        } );
        return parents;
    }

    public String getPayloadExample(OtmActionRequest request) {
        // log.debug( DexParentRefsEndpointMap.getPayloadExample( request ) );
        return DexParentRefsEndpointMap.getPayloadExample( request );
    }

    public String getPayloadExample(OtmActionResponse response) {
        return DexParentRefsEndpointMap.getPayloadExample( response );
    }

    private Node getSubectNode() {
        Button button = new Button( getSubjectName() );
        button.setDisable( !AssignResourceSubjectAction.isEnabled( this ) );
        button.setOnAction( a -> assignSubject() );
        return button;
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
     * Get facets from subject business object. Replace contributed facets with their contributor.
     * 
     * @return a non-null list of subject facets and contextual facets.
     */
    public List<OtmObject> getSubjectFacets() {
        List<OtmObject> facets = null;
        Collection<OtmObject> candidates = getSubject().getChildren();
        candidates.addAll( getSubject().getInheritedChildren() );
        if (getSubject() != null) {
            facets = new ArrayList<>();
            for (OtmObject object : candidates) {
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
     * Get the name of the business object. First tries the business object and if that is missing, tries the business
     * object reference name field.
     * <p>
     * Note: SWAGGER will use the latest minor version of a subject so just show the major version number.
     * 
     * @return name of the business object assigned or empty string
     */
    public String getSubjectName() {
        OtmBusinessObject subject = getAssignedType();
        String sName = "";
        // Try the base type if any.
        if (subject == null && getBaseType() != null) {
            subject = getBaseType().getAssignedType();
            // Workaround for defect reported 5/3/2021
            if (subject != null)
                setAssignedType( subject );
        }
        // If still not found, use the type name from the TL object.
        if (subject == null)
            sName = getTlAssignedTypeName();
        else {
            // Add the version indicator
            int major = -1;
            if (subject.getLibrary() != null)
                // try {
                major = subject.getLibrary().getMajorVersion();
            sName = subject.getName() + " /v" + major;
            // } catch (VersionSchemeException e) {
            // }
        }
        return sName;

        // if (getAssignedType() == null)
        // return getTlAssignedTypeName();
        // int major = -1;
        // if (getAssignedType().getLibrary() != null)
        // try {
        // major = getAssignedType().getLibrary().getMajorVersion();
        // } catch (VersionSchemeException e) {
        // }
        // return major > -1 ? getAssignedType().getName() + " /v" + major : getAssignedType().getName();
        // // return getAssignedType().getNameWithPrefix();
    }

    @Override
    public TLResource getTL() {
        return (TLResource) tlObject;
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

    public Tooltip getTooltip() {
        return new Tooltip( TOOLTIP );
    }

    public boolean isAbstract() {
        return getTL().isAbstract();
    }

    @Override
    public boolean isEditable() {
        return getLibrary() != null && getLibrary().isEditable();
        // return getName().startsWith( "S" ); // testing only
    }

    public boolean isFirstClass() {
        return getTL().isFirstClass();
    }

    @Override
    public boolean isNameControlled() {
        return false;
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

    @Override
    public void modelInheritedChildren() {
        // log.debug( "Model inherited resource children for " + getName() );
        if (inheritedChildren == null)
            inheritedChildren = new ArrayList<>();
        else
            inheritedChildren.clear(); // force re-compute

        OtmResource base = getBaseType();
        if (base == null)
            return;

        // Get all the inherited TL model elements
        // Create a new ResourceChild for each inherited element
        // DE gathers list from the TLExtension on the resource. It does not use codegen utils.
        //
        // // List can include itself
        // List<TLResource> foo = ResourceCodegenUtils.getInheritanceHierarchy( getTL() );
        // log.debug( "Found " + foo.size() + " inheritance sources." );

        // ResourceCodegenUtils.getInheritedActions( getTL() ).forEach( a -> new OtmAction( a, this ) );
        // ResourceCodegenUtils.getInheritedActionFacets( getTL() ).forEach( a -> new OtmActionFacet( a, this ) );
        // ResourceCodegenUtils.getInheritedParamGroups( getTL() ).forEach( a -> new OtmParameterGroup( a, this ) );
        // ResourceCodegenUtils.getInheritedParentRefs( getTL() ).forEach( a -> new OtmParentRef( a, this ) );
        // ResourceCodegenUtils.getReferencedFacet( businessObject, referenceFacetName )

        for (TLAction tlA : ResourceCodegenUtils.getInheritedActions( getTL() ))
            if (!getTL().getActions().contains( tlA ) && OtmModelElement.get( tlA ) != null)
                makeInherited( new OtmAction( tlA, this ), base );

        for (TLActionFacet tlAf : ResourceCodegenUtils.getInheritedActionFacets( getTL() ))
            if (!getTL().getActionFacets().contains( tlAf ) && OtmModelElement.get( tlAf ) != null)
                makeInherited( new OtmActionFacet( tlAf, this ), base );

        for (TLParamGroup tlPG : ResourceCodegenUtils.getInheritedParamGroups( getTL() ))
            if (!getTL().getParamGroups().contains( tlPG ) && OtmModelElement.get( tlPG ) != null)
                makeInherited( new OtmParameterGroup( tlPG, this ), base );

        for (TLResourceParentRef tlPR : ResourceCodegenUtils.getInheritedParentRefs( getTL() ))
            if (!getTL().getParentRefs().contains( tlPR ) && OtmModelElement.get( tlPR ) != null)
                makeInherited( new OtmParentRef( tlPR, this ), base );
    }

    private void makeInherited(OtmResourceChild iKid, OtmResource base) {
        inheritedChildren.add( iKid );
        children.remove( iKid );
        iKid.setInheritedFrom( base );
        // log.debug( "Made " + iKid + " inherited." );
    }

    @Override
    public StringProperty nameProperty() {
        if (getLibrary() != null && getLibrary().getVersionChain() != null
            && getLibrary().getVersionChain().isNewToChain( this ))

            // Override default behavior of letting the latest version of a member be renamed
            // if (getLibrary() != null && (getLibrary().isMajorVersion() || getLibrary().isUnmanaged()))
            return super.nameProperty();
        if (nameProperty == null)
            nameProperty = new ReadOnlyStringWrapper();
        nameProperty.set( getName() );
        return nameProperty;
    }

    /**
     * Something changed in this resource so refresh it. Updates the endpoint map. To refresh all who use this resource
     * as parent {@link #refresh(boolean)}
     */
    @Override
    public void refresh() {
        super.refresh();
        refresh( false );
    }

    /**
     * Something changed in this resource so refresh it.
     * 
     * @param deep if true, refresh all sub resources too. Sub-resources have this resource in their paths
     */
    public void refresh(boolean deep) {
        // log.debug( "Deep refesh of " + this );
        // clear the map. Leave this object in place
        if (parentRefsEndpointMap != null)
            parentRefsEndpointMap.refresh();
        basePathProperty().setValue( getBasePath() );

        if (deep)
            getAllSubResources().forEach( sr -> sr.refresh( true ) );
    }

    /**
     * Remove the child from the Otm and TL objects
     * 
     * @param child
     */
    @Override
    public void delete(OtmObject child) {
        if (child instanceof OtmResourceChild) {
            TLModelElement tlChild = child.getTL();
            // Remove from TL Resource
            if (tlChild instanceof TLResourceParentRef)
                getTL().removeParentRef( (TLResourceParentRef) tlChild );
            else if (tlChild instanceof TLParamGroup)
                getTL().removeParamGroup( (TLParamGroup) tlChild );
            else if (tlChild instanceof TLAction)
                getTL().removeAction( (TLAction) tlChild );
            else if (tlChild instanceof TLActionFacet)
                getTL().removeActionFacet( (TLActionFacet) tlChild );
            else
                log.debug( "Can't remove tl " + child + " from " + this );
            children.remove( child );
        }
    }

    @Override
    public void remove(OtmObject child) {
        if (contains( children, child ))
            children.remove( child );
    }

    /**
     * @param b
     */
    public void setAbstract(boolean b) {
        getTL().setAbstract( b );
        refresh( true );
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
     * {@inheritDoc} For resources, <b>must</b> be a business object. Also sets base path if not already set.
     * 
     * @see org.opentravel.model.OtmTypeUser#setAssignedType(org.opentravel.model.OtmTypeProvider)
     */
    // TODO - what is impact on parameter group? Action request parameters and paths
    @Override
    public OtmTypeProvider setAssignedType(OtmTypeProvider type) {
        OtmLibraryMember oldUser = getAssignedType() == null ? null : getAssignedType().getOwningMember();
        if (type == null) {
            setAssignedTLType( null );
            if (oldUser != null)
                oldUser.changeWhereUsed( this, null );
        } else {
            if (setAssignedTLType( (NamedEntity) type.getTL() ) != null) {
                // add to type's typeUsers
                type.getOwningMember().changeWhereUsed( oldUser, getOwningMember() );
                // Set the base path with the subject as the collection if base path is not already set.
                if (getBasePath() == null || getBasePath().isEmpty())
                    // setBasePath( DexParentRefsEndpointMap.PATH_SEPERATOR
                    // + DexParentRefsEndpointMap.makePlural( type.getName() ) );
                    setBasePath( DexParentRefsEndpointMap.PATH_SEPERATOR );
            }
        }
        // Check impact of change and Make changes undo-able

        return getAssignedType();
    }

    public List<OtmResourceChild> getInvalidChildren() {
        List<OtmResourceChild> errors = new ArrayList<>();
        for (OtmObject c : getChildren()) {
            if (!c.isValid( true ) && c instanceof OtmResourceChild)
                errors.add( (OtmResourceChild) c );
        }
        return errors;
    }

    /**
     * Set the base path on this resource then refresh the object.
     * 
     * @param basePath
     */
    public void setBasePath(String basePath) {
        getTL().setBasePath( basePath );
        refresh( true );
    }

    // /**
    // * Set the base path on this resource. If override is true, do all the action requests also.
    // *
    // * @param basePath
    // * @param override
    // */
    // @Deprecated
    // public void setBasePath(String basePath, boolean override) {
    // getTL().setBasePath( basePath );
    // if (override)
    // getActionRequests().forEach( ar -> ar.setPathTemplate( basePath, override ) );
    // refresh( true );
    // }

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
        // With a change to the class, remodel inheritance
        getActions().forEach( a -> a.modelInheritedChildren() );

        // log.debug( "Set extension to " + getExtendedResource() );
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
     * @param b
     */
    public void setFirstClass(boolean b) {
        getTL().setFirstClass( b );
        refresh( true );
        // log.debug( "First class set to " + isFirstClass() );
    }

    @Override
    public String setName(String name) {
        getTL().setName( name );
        isValid( true );
        return getName();
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
     * @see org.opentravel.model.OtmTypeUser#setTLTypeName(java.lang.String)
     */
    @Override
    public void setTLTypeName(String name) {
        // no-op
    }

    /**
     * @return id parameter group or null
     */
    public OtmParameterGroup getIdGroup() {
        for (OtmParameterGroup pg : getParameterGroups())
            if (pg.isIdGroup())
                return pg;
        return null;
    }
}
