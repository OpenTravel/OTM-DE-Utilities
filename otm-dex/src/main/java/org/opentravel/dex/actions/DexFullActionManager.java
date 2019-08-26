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

package org.opentravel.dex.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.resource.OtmAction;
import org.opentravel.model.resource.OtmActionFacet;
import org.opentravel.model.resource.OtmActionRequest;
import org.opentravel.model.resource.OtmActionResponse;
import org.opentravel.model.resource.OtmParameter;
import org.opentravel.model.resource.OtmParameterGroup;
import org.opentravel.model.resource.OtmParentRef;

import javafx.beans.value.ObservableValue;

/**
 * Default action manager used by OTM elements to determine what actions are available and adding listeners to
 * observable values (properties) to execute the action.
 * <p>
 * To create a StringProperty with associated un-doable action use
 * {@link #add(DexActionManager.DexActions, CurrentValue, OtmObject)} when creating an observable property.
 * <p>
 * Extends {@link DexActionManagerBase} which controls and manages actions; maintains queue of actions and notifies user
 * of performed action status.
 * 
 * @author dmh
 *
 */
public class DexFullActionManager extends DexActionManagerBase {
    private static Log log = LogFactory.getLog( DexFullActionManager.class );

    public DexFullActionManager(DexMainController mainController) {
        super( mainController );
    }

    @Override
    public boolean addAction(DexActions action, ObservableValue<? extends Boolean> op, OtmObject subject) {
        switch (action) {
            case SETABSTRACT:
                if (subject instanceof OtmResource)
                    op.addListener( (ObservableValue<? extends Boolean> o, Boolean x,
                        Boolean y) -> doBoolean( new SetAbstractAction( (OtmResource) subject ), o ) );
                break;
            case SETCOMMONACTION:
                if (subject instanceof OtmAction)
                    op.addListener( (ObservableValue<? extends Boolean> o, Boolean x,
                        Boolean y) -> doBoolean( new SetCommonAction( (OtmAction) subject ), o ) );
                break;
            case SETFIRSTCLASS:
                if (subject instanceof OtmResource)
                    op.addListener( (ObservableValue<? extends Boolean> o, Boolean x,
                        Boolean y) -> doBoolean( new SetFirstClassAction( (OtmResource) subject ), o ) );
                break;
            case SETIDGROUP:
                if (subject instanceof OtmParameterGroup)
                    op.addListener( (ObservableValue<? extends Boolean> o, Boolean x,
                        Boolean y) -> doBoolean( new SetIdGroupAction( (OtmParameterGroup) subject ), o ) );
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean addAction(DexActions action, ObservableValue<? extends String> op, OtmModelElement<?> subject) {
        // Make sure the action can register itself and access main controller
        if (subject.getActionManager() == null)
            throw new IllegalStateException( "Subject of an action must provide access to action manger." );

        switch (action) {
            case NAMECHANGE:
                op.addListener( (ObservableValue<? extends String> o, String old,
                    String newVal) -> doString( new NameChangeAction( subject ), o, old, newVal ) );
                break;
            case DESCRIPTIONCHANGE:
                op.addListener( (ObservableValue<? extends String> o, String old,
                    String newVal) -> doString( new DescriptionChangeAction( subject ), o, old, newVal ) );
                break;
            case BASEPATHCHANGE:
                if (subject instanceof OtmResource)
                    op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
                        new BasePathChangeAction( (OtmResource) subject ), o, old, newVal ) );
                break;
            case SETPARENTPARAMETERGROUP:
                if (subject instanceof OtmParentRef)
                    op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
                        new SetParentParameterGroupAction( (OtmParentRef) subject ), o, old, newVal ) );
                break;
            case SETPARENTPATHTEMPLATE:
                if (subject instanceof OtmParentRef)
                    op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
                        new SetParentPathTemplateAction( (OtmParentRef) subject ), o, old, newVal ) );
                break;
            case SETPARENTREFPARENT:
                if (subject instanceof OtmParentRef)
                    op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
                        new SetParentRefParentAction( (OtmParentRef) subject ), o, old, newVal ) );
                break;
            case SETRESOURCEEXTENSION:
                if (subject instanceof OtmResource)
                    op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
                        new SetResourceExtensionAction( (OtmResource) subject ), o, old, newVal ) );
                break;

            case SETAFREFERENCEFACET:
                if (subject instanceof OtmActionFacet)
                    op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
                        new SetAFReferenceFacetAction( (OtmActionFacet) subject ), o, old, newVal ) );
                break;
            case SETAFREFERENCETYPE:
                if (subject instanceof OtmActionFacet)
                    op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
                        new SetAFReferenceTypeAction( (OtmActionFacet) subject ), o, old, newVal ) );
                break;

            case SETPARAMETERLOCATION:
                if (subject instanceof OtmParameter)
                    op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
                        new SetParameterLocationAction( (OtmParameter) subject ), o, old, newVal ) );
                break;
            case SETREQUESTMETHOD:
                if (subject instanceof OtmActionRequest)
                    op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
                        new SetRequestMethodAction( (OtmActionRequest) subject ), o, old, newVal ) );
                break;
            case SETREQUESTPARAMETERGROUP:
                if (subject instanceof OtmActionRequest)
                    op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
                        new SetRequestParameterGroupAction( (OtmActionRequest) subject ), o, old, newVal ) );
                break;
            case SETREQUESTPAYLOAD:
                if (subject instanceof OtmActionRequest)
                    op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
                        new SetRequestPayloadAction( (OtmActionRequest) subject ), o, old, newVal ) );
                break;
            case SETREQUESTPATH:
                if (subject instanceof OtmActionRequest)
                    op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
                        new SetRequestPathAction( (OtmActionRequest) subject ), o, old, newVal ) );;
                break;
            case SETRESPONSEPAYLOAD:
                if (subject instanceof OtmActionResponse)
                    op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
                        new SetResponsePayloadAction( (OtmActionResponse) subject ), o, old, newVal ) );;
                break;
            case SETPARAMETERGROUPFACET:
                if (subject instanceof OtmParameterGroup)
                    op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
                        new SetParameterGroupFacetAction( (OtmParameterGroup) subject ), o, old, newVal ) );;
                break;

            // Boolean actions
            case SETABSTRACT:
            case SETCOMMONACTION:
            case SETIDGROUP:
            case SETFIRSTCLASS:
                return false;
            default:
                return false;
        }
        return true;
    }
    // TODO
    // The ObservableValue stores a strong reference to the listener which will prevent the listener from being garbage
    // collected and may result in a memory leak. It is recommended to either unregister a listener by calling
    // removeListener after use or to use an instance of WeakChangeListener avoid this situation.

    /**
     * Actions available for OTM Properties wrapped by PropertiesDAO.
     * 
     * @param action
     * @param subject
     */
    // FIXME - have properties DAO use add then remove run and action factory
    @Override
    public void run(DexActions actionType, OtmObject subject, Object data) {
        actionFactory( actionType, subject ).doIt( data );
    }

    @Override
    public boolean isEnabled(DexActions action, OtmObject subject) {
        switch (action) {
            case BASEPATHCHANGE:
                return BasePathChangeAction.isEnabled( subject );
            case TYPECHANGE:
                return AssignedTypeChangeAction.isEnabled( subject );
            case DESCRIPTIONCHANGE:
                return DescriptionChangeAction.isEnabled( subject );

            case SETABSTRACT:
                return SetAbstractAction.isEnabled( subject );
            case SETCOMMONACTION:
                return SetCommonAction.isEnabled( subject );
            case SETFIRSTCLASS:
                return SetFirstClassAction.isEnabled( subject );
            case SETIDGROUP:
                return SetIdGroupAction.isEnabled( subject );
            case SETPARENTPARAMETERGROUP:
                return SetParentParameterGroupAction.isEnabled( subject );
            case SETPARENTPATHTEMPLATE:
                return SetParentPathTemplateAction.isEnabled( subject );
            case SETPARENTREFPARENT:
                return SetParentRefParentAction.isEnabled( subject );
            case SETRESOURCEEXTENSION:
                return SetResourceExtensionAction.isEnabled( subject );
            case SETAFREFERENCEFACET:
                return SetAFReferenceFacetAction.isEnabled( subject );
            case SETAFREFERENCETYPE:
                return SetAFReferenceTypeAction.isEnabled( subject );
            case SETPARAMETERLOCATION:
                return SetParameterLocationAction.isEnabled( subject );
            case SETREQUESTMETHOD:
                return SetRequestMethodAction.isEnabled( subject );
            case SETREQUESTPARAMETERGROUP:
                return SetRequestParameterGroupAction.isEnabled( subject );
            case SETREQUESTPAYLOAD:
                return SetRequestPayloadAction.isEnabled( subject );
            case SETREQUESTPATH:
                return SetRequestPathAction.isEnabled( subject );
            case SETRESPONSEPAYLOAD:
                return SetResponsePayloadAction.isEnabled( subject );
            case SETPARAMETERGROUPFACET:
                return SetParameterGroupFacetAction.isEnabled( subject );
            default:
                return false;
        }
    }
}
