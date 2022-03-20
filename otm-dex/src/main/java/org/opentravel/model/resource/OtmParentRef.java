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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.common.DexEditField;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 * OTM Object for Resource Action objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmParentRef extends OtmResourceChildBase<TLResourceParentRef> implements OtmResourceChild {
    private static Logger log = LogManager.getLogger( OtmParentRef.class );

    private static final String TOOLTIP = "Specifies a parent reference for a REST resource.";

    private static final String PARAM_GROUP_LABEL = "Parameter Group";

    private static final String PARAM_GROUP_TOOLTIP =
        "The name of the parameter group on the the parent resource with which this parent reference is associated.  The referenced group must be an ID parameter group.";

    private static final String PATH_LABEL = "Path Template";

    private static final String PATH_TOOLTIP = "Specifies the path template for the parent resource. "
        + "This path will be pre-pended to all action path templates when the child resource "
        + "is treated as a sub-resource (non-first class).";

    private static final String PARENT_LABEL = "Parent";

    private static final String PARENT_TOOLTIP = "Specifies a parent reference for a REST resource.";
    private static final String PATH_DEFAULT_LABEL = "Default";
    private static final String PATH_DEFAULT_TOOLTIP = "Reset to default path for action request.";

    private StringProperty pathProperty;


    /**
     * Create OTM facade for the TL object. The TL object is not modified.
     * 
     * @param tla
     * @param owner
     */
    public OtmParentRef(TLResourceParentRef tla, OtmResource owner) {
        super( tla, owner );
    }

    @Override
    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        fields.add( new DexEditField( 0, 0, PARENT_LABEL, PARENT_TOOLTIP, getParentNode() ) );
        fields.add( new DexEditField( 1, 0, PARAM_GROUP_LABEL, PARAM_GROUP_TOOLTIP, getParameterGroupNode() ) );
        fields.add( new DexEditField( 2, 0, PATH_LABEL, PATH_TOOLTIP, getPathNode() ) );
        fields.add( new DexEditField( 2, 2, null, PATH_DEFAULT_TOOLTIP, getDefaultPathNode() ) );
        return fields;
    }

    // TODO
    // DONE - Add default path button like on request
    // FIX - Add default path method

    private Node getDefaultPathNode() {
        Button button = new Button( PATH_DEFAULT_LABEL );
        button.setDisable( !isEditable() );
        button.setOnAction( e -> pathProperty.set( getPathTemplateDefault() ) );
        return button;
    }

    /**
     * Get the default path template. This is a slash followed by any path parameters if in ID group
     * 
     * @return
     */
    public String getPathTemplateDefault() {
        String d = DexParentRefsEndpointMap.PATH_SEPERATOR
            + DexParentRefsEndpointMap.makePlural( getParentResource().getSubject().getName() )
            + DexParentRefsEndpointMap.getContribution( this.getParameterGroup() );
        return d.isEmpty() ? "/" : d;
    }


    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE_PARENTREF;
    }

    @Override
    public String getName() {
        return getTL().getParentResourceName() + "Ref";
    }

    /**
     * @return the parameter group used by this parent reference {@link TLResourceParentRef#getParentParamGroup()}.
     */
    public OtmParameterGroup getParameterGroup() {
        return (OtmParameterGroup) OtmModelElement.get( getTL().getParentParamGroup() );
    }

    public ObservableList<String> getParameterGroupCandidates() {
        ObservableList<String> candidates = FXCollections.observableArrayList();
        if (getParentResource() != null) {
            getParentResource().getParameterGroups( true ).forEach( pg -> candidates.add( pg.getName() ) );
            // getParentResource().getInheritedParameterGroups().forEach( ipg -> candidates.add( ipg.getName() ) );
            // candidates.sort( null );
        }
        return candidates;
    }

    public String getParameterGroupName() {
        return getTL().getParentParamGroupName();
    }

    private Node getParameterGroupNode() {
        StringProperty selection =
            getActionManager().add( DexActions.SETPARENTPARAMETERGROUP, getParameterGroupName(), this );
        return DexEditField.makeComboBox( getParameterGroupCandidates(), selection );
    }

    /**
     * Get all resources from the owner's model manager except the owner.
     * 
     * @return new observable list containing {@link OtmResource#getNameWithPrefix()}.
     */
    public ObservableList<String> getParentCandidates() {
        ObservableList<String> candidates = FXCollections.observableArrayList();
        getOwningMember().getModelManager().getResources( true )
            .forEach( r -> candidates.add( r.getNameWithPrefix() ) );
        candidates.remove( getOwningMember().getNameWithPrefix() );
        return candidates;
    }

    private Node getParentNode() {
        StringProperty selection = null;
        if (getParentResource() != null)
            selection =
                getActionManager().add( DexActions.SETPARENTREFPARENT, getParentResource().getNameWithPrefix(), this );
        else
            selection = getActionManager().add( DexActions.SETPARENTREFPARENT, "", this );
        return DexEditField.makeComboBox( getParentCandidates(), selection );
    }

    public OtmResource getParentResource() {
        return (OtmResource) OtmModelElement.get( getTL().getParentResource() );
    }

    /**
     * 
     * @return name of parent resource or empty string
     */
    public String getParentResourceName() {
        return getParentResource() != null ? getParentResource().getName() : "";
    }

    private Node getPathNode() {
        pathProperty = getActionManager().add( DexActions.SETPARENTPATHTEMPLATE, getPathTemplate(), this );
        return DexEditField.makeTextField( pathProperty );
    }

    /**
     * @return the template from the tl object
     */
    public String getPathTemplate() {
        return getTL().getPathTemplate();
    }

    @Override
    public TLResourceParentRef getTL() {
        return (TLResourceParentRef) tlObject;
    }

    public Tooltip getTooltip() {
        return new Tooltip( TOOLTIP );
    }

    /**
     * @return true if the parent resource exists and is first class
     */
    public boolean isParentFirstClass() {
        return getParentResource() != null && getParentResource().isFirstClass();
    }

    public OtmParameterGroup setParameterGroup(OtmParameterGroup parentParameterGroup) {
        if (parentParameterGroup != null)
            getTL().setParentParamGroup( parentParameterGroup.getTL() );
        else
            getTL().setParentParamGroup( null );

        // TODO -
        // Update the path template to match the selected PG
        // FIXME String basePath = DexParentRefsEndpointMap.getCollectionContribution( this );
        setPathTemplate( getPathTemplate(), true );

        log.error( "Set parameter group name to " + getParameterGroup() );
        return (getParameterGroup());
    }

    /**
     * Set the parameter group from the parent's parameter groups that {@link OtmParameterGroup#getName()} match the
     * name
     * 
     * @param name
     * @return the actual group set or null
     */
    public OtmParameterGroup setParameterGroupString(String name) {
        OtmParameterGroup pg = null;
        if (getParentResource() != null) {
            List<OtmParameterGroup> groups = getParentResource().getParameterGroups( true );
            // groups.addAll( getParentResource().getInheritedParameterGroups() );
            for (OtmParameterGroup c : groups)
                if (c.getName().equals( name ))
                    pg = c;
        }
        return setParameterGroup( pg );
    }

    /**
     * Set the parent resource to a resource from the model manager whose {@link OtmResource#getNameWithPrefix()}
     * matches value.
     * 
     * @param value
     * @return actual resource set or null
     */
    public OtmResource setParentResourceString(String value) {
        OtmResource r = null;
        for (OtmResource c : getOwningMember().getModelManager().getResources( false ))
            if (c.getNameWithPrefix().equals( value ))
                r = c;
        // else if (c.getNamespace().equals( getOwningMember().getNamespace() ) && c.getName().equals( value ))
        // r = c; // Could be without prefix

        return setParentResource( r );
    }

    public OtmResource setParentResource(OtmResource resource) {
        if (resource != null) {
            getTL().setParentResource( resource.getTL() );
            // if (getName().isEmpty())
            // setName( getParentResourceName() + "Ref" );
        } else
            getTL().setParentResource( null );
        // log.debug( "Set parent resource to " + getParentResource() );
        // TODO - update parameter group list
        return getParentResource();
    }

    /**
     * @param path is the string to set or null
     * @return actual set string or null
     */
    public String setPathTemplate(String path) {
        getTL().setPathTemplate( path );
        getOwningMember().refresh(); // update endpoint map
        return getTL().getPathTemplate();
    }

    public String setPathTemplate(String path, boolean addParameters) {
        // FIXME
        return setPathTemplate( path );
    }


}
