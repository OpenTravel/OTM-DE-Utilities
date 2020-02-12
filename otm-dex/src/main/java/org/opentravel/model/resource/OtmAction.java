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
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLHttpMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

/**
 * OTM Object for Resource Action objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmAction extends OtmResourceChildBase<TLAction> implements OtmResourceChild, OtmChildrenOwner {
    private static Log log = LogFactory.getLog( OtmAction.class );

    private static final String TOOLTIP = "Specify an action request and possible responses.";

    private static final String COMMON_LABEL = "Common";

    private static final String COMMON_TOOLTIP =
        "Indicates that the action is a common or shared action, meaning that all other actions defined for the resource will inherit the characteristics (typically responses) defined for it.";

    public enum BuildTemplate {
        GET, POST
    }

    private DexParentRefsEndpointMap endpoints = null;

    public OtmAction(String name, OtmResource parent) {
        super( new TLAction(), parent );
        if (parent != null)
            parent.getTL().addAction( getTL() );
        setName( name );
    }

    public OtmAction(TLAction tla, OtmResource parent) {
        super( tla, parent );
        // Do it now so the methods that use the TLAction directly will have listeners.
        if (parent != null && tla.getOwner() == null)
            parent.getTL().addAction( getTL() );

        modelChildren();
        modelInheritedChildren();
    }

    @Override
    public OtmObject add(OtmObject child) {
        if (child instanceof OtmActionRequest || child instanceof OtmActionResponse)
            if (!children.contains( child ))
                children.add( child );
        return child;
    }

    /**
     * Add the passed action to the TL resource if not already owned, child list and set action's parent.
     * 
     * @param tlGroup
     * @return
     */
    public OtmActionResponse add(TLActionResponse tlActionResponse) {
        OtmActionResponse response = null;
        if (tlActionResponse != null && !getTL().getResponses().contains( tlActionResponse )) {
            getTL().addResponse( tlActionResponse );
            response = new OtmActionResponse( tlActionResponse, this );
            log.debug( "Added response to " + this );
            getOwningMember().refresh( true );
        }
        return response;
    }

    public void build(BuildTemplate template) {
        String baseName = "";
        if (getOwningMember() != null) {
            OtmActionRequest rq = new OtmActionRequest( new TLActionRequest(), this );
            OtmActionResponse rs = new OtmActionResponse( new TLActionResponse(), this );
            List<Integer> codes = new ArrayList<>();
            codes.add( 200 );

            if (getOwningMember().getSubject() != null)
                baseName = getOwningMember().getSubject().getName();

            switch (template) {
                case GET:
                    setName( "Get" );
                    rq.setMethod( TLHttpMethod.GET );
                    rq.setParamGroup( getOwningMember().getIdGroup() );
                    rq.setPathTemplate( rq.getPathTemplateDefault(), false );
                    rs.setRestStatusCodes( codes );
                    break;
                case POST:
                    setName( baseName + "Post" );
                    break;
            }
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

    private Node getCommonNode() {
        BooleanProperty commonProperty = getActionManager().add( DexActions.SETCOMMONACTION, isCommon(), this );
        return DexEditField.makeCheckBox( commonProperty, COMMON_LABEL );
    }

    @Override
    public Collection<OtmPropertyOwner> getDescendantsPropertyOwners() {
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
     * Get the URL for this action without contributions from parent resources. If it is not a first class resource,
     * return static strings.
     * 
     * @return
     */
    public String getEndpointURL() {
        StringBuilder path = new StringBuilder();
        if (getOwningMember().isFirstClass()) {
            path.append( DexParentRefsEndpointMap.getResourceBaseURL( this.getOwningMember() ) );
            path.append( DexParentRefsEndpointMap.getActionContribution( this ) );
        } else {
            if (getOwningMember().getParentRefs().isEmpty())
                path.append( DexParentRefsEndpointMap.NO_PATH_NOTFIRSTCLASS_AND_NOPARENTREFS );
            else
                path.append( DexParentRefsEndpointMap.NO_PATH );
        }
        log.debug( "Resource Base Url = " + DexParentRefsEndpointMap.getResourceBaseURL( this.getOwningMember() ) );
        log.debug( "Action contribution = " + DexParentRefsEndpointMap.getActionContribution( this ) );
        // log.debug( "Endpoint URL on " + this + " is " + path.toString() );
        return path.toString();
    }

    /**
     * Get the URL for this action with contributions from ancestor resources.
     * 
     * @return
     */
    public String getEndpointURL(OtmParentRef parentRef) {
        StringBuilder path = new StringBuilder();
        path.append( DexParentRefsEndpointMap.getResourceBaseURL( this.getOwningMember() ) );
        path.append( getOwningMember().getParentRefEndpointsMap().get( parentRef ) );
        path.append( DexParentRefsEndpointMap.getActionContribution( this ) );
        return path.toString();
    }

    @Override
    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        fields.add( new DexEditField( 0, 0, null, COMMON_TOOLTIP, getCommonNode() ) );
        return fields;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE_ACTION;
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        modelInheritedChildren();
        return inheritedChildren != null ? inheritedChildren : Collections.emptyList();
    }

    /**
     * @see org.opentravel.model.OtmModelElement#getName()
     */
    @Override
    public String getName() {
        return getTL().getActionId();
    }

    /**
     * @see org.opentravel.model.OtmModelElement#setName(java.lang.String)
     */
    @Override
    public String setName(String name) {
        getTL().setActionId( name );
        if (getRequest() != null)
            getRequest().nameProperty();
        return getName();
    }

    public OtmActionRequest getRequest() {
        return (OtmActionRequest) OtmModelElement.get( getTL().getRequest() );
    }

    public List<OtmActionResponse> getResponses() {
        List<OtmActionResponse> list = new ArrayList<>();
        getTL().getResponses().forEach( r -> list.add( (OtmActionResponse) OtmModelElement.get( r ) ) );
        return list;
    }

    /**
     * 
     * @return this action's responses and any inherited responses
     */
    public List<OtmActionResponse> getAllResponses() {
        List<OtmActionResponse> list = new ArrayList<>();
        ResourceCodegenUtils.getInheritedResponses( getTL() )
            .forEach( r -> list.add( (OtmActionResponse) OtmModelElement.get( r ) ) );
        // getTL().getResponses().forEach( r -> list.add( (OtmActionResponse) OtmModelElement.get( r ) ) );
        return list;
    }

    @Override
    public TLAction getTL() {
        return (TLAction) tlObject;
    }

    public Tooltip getTooltip() {
        return new Tooltip( TOOLTIP );
    }

    public boolean isCommon() {
        return getTL().isCommonAction();
    }

    // /**
    // * @see org.opentravel.model.OtmChildrenOwner#isExpanded()
    // */
    // @Override
    // public boolean isExpanded() {
    // return false;
    // }

    /**
     * @see org.opentravel.model.OtmChildrenOwner#modelChildren()
     */
    @Override
    public void modelChildren() {
        if (getTL().getRequest() != null)
            new OtmActionRequest( getTL().getRequest(), this );
        if (getTL().getResponses() != null)
            getTL().getResponses().forEach( r -> new OtmActionResponse( r, this ) );
    }

    /**
     * @see org.opentravel.model.OtmChildrenOwner#modelInheritedChildren()
     */
    @Override
    public void modelInheritedChildren() {
        if (inheritedChildren == null)
            inheritedChildren = new ArrayList<>();
        else
            inheritedChildren.clear();

        for (TLActionResponse tlAR : ResourceCodegenUtils.getInheritedResponses( getTL() )) {
            if (!getTL().getResponses().contains( tlAR )) {
                if (OtmModelElement.get( tlAR ) != null)
                    // inheritedChildren.add( new OtmActionResponse( tlAR, this ) );
                    // else
                    inheritedChildren.add( OtmModelElement.get( tlAR ) );
            }
        }
    }

    /**
     * @param value
     */
    public void setCommon(boolean value) {
        getTL().setCommonAction( value );
    }

    /**
     * Remove the response from the TL and Otm parents.
     * 
     * @param response
     */
    @Override
    public void delete(OtmObject response) {
        if (response.getTL() instanceof TLActionResponse)
            getTL().removeResponse( (TLActionResponse) response.getTL() );
        remove( response );
    }

    @Override
    public void remove(OtmObject response) {
        children.remove( response );
    }
}
