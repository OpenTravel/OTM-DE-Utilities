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
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.resource.DexResourcePathHandler;
import org.opentravel.model.resource.OtmAction;
import org.opentravel.model.resource.OtmActionFacet;
import org.opentravel.model.resource.OtmActionRequest;
import org.opentravel.model.resource.OtmParameterGroup;
import org.opentravel.model.resource.OtmParentRef;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLResource;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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

    private DexResourcePathHandler pathHandler;

    public OtmResource(TLResource tlo, OtmModelManager mgr) {
        super( tlo, mgr );
        modelChildren();
        pathHandler = new DexResourcePathHandler( this );
    }

    public String getURL(OtmAction action) {
        return pathHandler.get( action );
    }

    public OtmResource(String name, OtmModelManager mgr) {
        super( new TLResource(), mgr );
        setName( name );
    }

    public void setBasePath(String basePath) {
        getTL().setBasePath( basePath );
    }

    public String getBasePath() {
        return getTL().getBasePath();
    }

    public StringProperty basePathProperty() {
        return new ReadOnlyStringWrapper( getBasePath() );
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
        // OtmObject subject = OtmModelElement.get( getTL().getBusinessObjectRef() );
        // return subject instanceof OtmBusinessObject ? (OtmBusinessObject) subject : null;
    }

    /**
     * Convenience method for {@link #setAssignedType(OtmTypeProvider)}
     * 
     * @param subject
     */
    public OtmBusinessObject setSubject(OtmBusinessObject subject) {
        return (OtmBusinessObject) setAssignedType( subject );
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

    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        fields.add( new DexEditField( 0, 0, extension_LABEL, extension_TOOLTIP, new ComboBox<String>() ) );
        fields.add( new DexEditField( 1, 0, businessObject_LABEL, businessObject_TOOLTIP,
            new Button( getBusinessObject().getName() ) ) );
        fields.add( new DexEditField( 2, 0, basePath_LABEL, basePath_TOOLTIP, new TextField() ) );
        fields.add( new DexEditField( 3, 0, null, abstract_TOOLTIP, new CheckBox( abstract_LABEL ) ) );
        fields.add( new DexEditField( 3, 1, null, firstClass_TOOLTIP, new CheckBox( firstClass_LABEL ) ) );

        // fields.add( new DexEditField( 1, 0, name_LABEL, name_TOOLTIP, new Button() ) );
        // fields.add( new DexEditField( 1, 0, parentRef_LABEL, parentRef_TOOLTIP, new Button() ) );
        // fields.add( new DexEditField( 1, 0, parent_LABEL, parent_TOOLTIP, new Button() ) );
        return fields;
    }

    private static final String TOOLTIP =
        "Encapsulates all aspects of a RESTful resource used to expose and manage a particular business object.";

    private static final String name_LABEL = "Resource Name";
    private static final String name_TOOLTIP =
        "The name of the resource.  This name is used to uniquely identify the resource within the OTM model, but will not conflict with any naming conventions used in generated XSD documents.";

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
        "Specifies the base path for this resource.  In usage, do not enter the name of the object because parameters are not allowed within a resource's base path. ";

    private static final String parentRef_LABEL = "Parent";
    private static final String parentRef_TOOLTIP = " The list of parent references for the resource. ";

    private static final String extension_LABEL = "Extends";
    private static final String extension_TOOLTIP =
        "Reference to the resource from which the child resource will inherit.";

    private static final String parent_LABEL = "Parent";
    private static final String parent_TOOLTIP = "Reference to the parent resource for this sub-resource.";

    private static final String baseResponseWizard_LABEL = "Base Response Wizard";
    private static final String baseResponseWizard_TOOLTIP =
        "Set base response on all Action Facets used for responses.";

    /**
     * @return
     */
    public Object getParentRef() {
        List<OtmParentRef> parents = new ArrayList<>();
        getTL().getParentRefs().forEach( pr -> {
            if (OtmModelElement.get( pr ) instanceof OtmParentRef)
                parents.add( (OtmParentRef) OtmModelElement.get( pr ) );
        } );
        return parents;
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
}
