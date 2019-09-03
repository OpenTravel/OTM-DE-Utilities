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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    // @Override
    // public boolean addAction(DexActions action, ObservableValue<? extends Boolean> op, OtmObject subject) {
    // switch (action) {
    // case SETABSTRACT:
    // if (subject instanceof OtmResource)
    // op.addListener( (ObservableValue<? extends Boolean> o, Boolean x,
    // Boolean y) -> doBoolean( new SetAbstractAction( (OtmResource) subject ), o ) );
    // break;
    // case SETCOMMONACTION:
    // if (subject instanceof OtmAction)
    // op.addListener( (ObservableValue<? extends Boolean> o, Boolean x,
    // Boolean y) -> doBoolean( new SetCommonAction( (OtmAction) subject ), o ) );
    // break;
    // case SETFIRSTCLASS:
    // if (subject instanceof OtmResource)
    // op.addListener( (ObservableValue<? extends Boolean> o, Boolean x,
    // Boolean y) -> doBoolean( new SetFirstClassAction( (OtmResource) subject ), o ) );
    // break;
    // case SETIDGROUP:
    // if (subject instanceof OtmParameterGroup)
    // op.addListener( (ObservableValue<? extends Boolean> o, Boolean x,
    // Boolean y) -> doBoolean( new SetIdGroupAction( (OtmParameterGroup) subject ), o ) );
    // break;
    // default:
    // return false;
    // }
    // return true;
    // }

    @Deprecated
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
            // case BASEPATHCHANGE:
            // // setListener( op, action, subject );
            //
            // if (subject instanceof OtmResource)
            // op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
            // new BasePathChangeAction( (OtmResource) subject ), o, old, newVal ) );
            // break;
            // case SETPARENTPARAMETERGROUP:
            // if (subject instanceof OtmParentRef)
            // op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
            // new SetParentParameterGroupAction( (OtmParentRef) subject ), o, old, newVal ) );
            // break;
            // case SETPARENTPATHTEMPLATE:
            // if (subject instanceof OtmParentRef)
            // op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
            // new SetParentPathTemplateAction( (OtmParentRef) subject ), o, old, newVal ) );
            // break;
            // case SETPARENTREFPARENT:
            // if (subject instanceof OtmParentRef)
            // op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
            // new SetParentRefParentAction( (OtmParentRef) subject ), o, old, newVal ) );
            // break;
            // case SETRESOURCEEXTENSION:
            // if (subject instanceof OtmResource)
            // op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
            // new SetResourceExtensionAction( (OtmResource) subject ), o, old, newVal ) );
            // break;
            //
            // case SETAFREFERENCEFACET:
            // if (subject instanceof OtmActionFacet)
            // op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
            // new SetAFReferenceFacetAction( (OtmActionFacet) subject ), o, old, newVal ) );
            // break;
            // case SETAFREFERENCETYPE:
            // if (subject instanceof OtmActionFacet)
            // op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
            // new SetAFReferenceTypeAction( (OtmActionFacet) subject ), o, old, newVal ) );
            // break;
            //
            // case SETPARAMETERLOCATION:
            // if (subject instanceof OtmParameter)
            // op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
            // new SetParameterLocationAction( (OtmParameter) subject ), o, old, newVal ) );
            // break;
            // case SETREQUESTMETHOD:
            // if (subject instanceof OtmActionRequest)
            // op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
            // new SetRequestMethodAction( (OtmActionRequest) subject ), o, old, newVal ) );
            // break;
            // case SETREQUESTPARAMETERGROUP:
            // if (subject instanceof OtmActionRequest)
            // op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
            // new SetRequestParameterGroupAction( (OtmActionRequest) subject ), o, old, newVal ) );
            // break;
            // case SETREQUESTPAYLOAD:
            // if (subject instanceof OtmActionRequest)
            // op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
            // new SetRequestPayloadAction( (OtmActionRequest) subject ), o, old, newVal ) );
            // break;
            // case SETREQUESTPATH:
            // if (subject instanceof OtmActionRequest)
            // op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
            // new SetRequestPathAction( (OtmActionRequest) subject ), o, old, newVal ) );;
            // break;
            // case SETRESPONSEPAYLOAD:
            // if (subject instanceof OtmActionResponse)
            // op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
            // new SetResponsePayloadAction( (OtmActionResponse) subject ), o, old, newVal ) );;
            // break;
            // case SETPARAMETERGROUPFACET:
            // if (subject instanceof OtmParameterGroup)
            // op.addListener( (ObservableValue<? extends String> o, String old, String newVal) -> doString(
            // new SetParameterGroupFacetAction( (OtmParameterGroup) subject ), o, old, newVal ) );
            // // setListener( op, action, subject );
            // break;
            //
            // // Boolean actions
            // case SETABSTRACT:
            // case SETCOMMONACTION:
            // case SETIDGROUP:
            // case SETFIRSTCLASS:
            // return false;
            default:
                return false;
        }
        return true;
    }


    // TODO - move the creation of the handler to the "doString" method
    // /**
    // * @deprecated - moved to action manager base
    // * @param op
    // * @param action
    // * @param subject
    // * @return
    // */
    // private DexAction<?> setListener(ObservableValue<? extends String> op, DexActions action, OtmObject subject) {
    // try {
    // DexAction<?> actionHandler = DexActions.getAction( action, subject );
    // if (actionHandler instanceof DexStringAction) {
    // op.addListener( (ObservableValue<? extends String> o, String oldValue,
    // String newValue) -> doString( (DexStringAction) actionHandler, o, oldValue, newValue ) );
    // return actionHandler;
    // }
    // } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException e) {
    // log.warn( "Failed to set listener on " + action + " because: " + e.getLocalizedMessage() );
    // }
    // // Do i want to use reflection to set subject?
    // return null;
    // }

    // /**
    // * Actions available for OTM Properties wrapped by PropertiesDAO.
    // *
    // * @param action
    // * @param subject
    // */
    // // FIXME - row factory and memberDetailsController use add then remove run and action factory
    // // Object data is always set to null
    // @Override
    // public void run(DexActions actionType, OtmObject subject, Object data) {
    // run( actionType, subject );
    // // ((AssignedTypeChangeAction) actionFactory( actionType, subject )).doIt();
    // }

    // @Override
    // public void run(DexActions actionType, OtmObject subject) {
    // // DexAction<?> action = null;
    // switch (actionType) {
    // // case NAMECHANGE:
    // // action = new NameChangeAction( subject );
    // // break;
    // // case DESCRIPTIONCHANGE:
    // // action = new DescriptionChangeAction( subject );
    // // break;
    // case TYPECHANGE:
    // if (subject instanceof OtmTypeUser)
    // new AssignedTypeChangeAction( (OtmTypeUser) subject ).doIt();
    // break;
    // default:
    // log.debug( "Unknown action: " + actionType.toString() );
    // }
    //
    // // ((AssignedTypeChangeAction) actionFactory( actionType, subject )).doIt();
    // }

    /**
     * Use reflection on the action to get the action handler's isEnabled method and return its result.
     * <p>
     * Note: this could be static but do NOT move to DexActions because there are multiple action managers.
     */
    @Override
    public boolean isEnabled(DexActions action, OtmObject subject) {
        boolean result = false;
        if (subject.getOwningMember().isEditable())
            try {
                Method m = action.actionClass().getMethod( "isEnabled", OtmObject.class );
                result = (boolean) m.invoke( null, subject );
                log.debug( "Method " + action.toString() + " isEnabled invoke result: " + result );
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
                log.error( "Could not invoke action.isEnabled( ):" + e.getMessage() );
            }
        return result;

        // switch (action) {
        // case BASEPATHCHANGE:
        // return BasePathChangeAction.isEnabled( subject );
        // case TYPECHANGE:
        // return AssignedTypeChangeAction.isEnabled( subject );
        // case DESCRIPTIONCHANGE:
        // return DescriptionChangeAction.isEnabled( subject );
        //
        // case SETABSTRACT:
        // return SetAbstractAction.isEnabled( subject );
        // case SETCOMMONACTION:
        // return SetCommonAction.isEnabled( subject );
        // case SETFIRSTCLASS:
        // return SetFirstClassAction.isEnabled( subject );
        // case SETIDGROUP:
        // return SetIdGroupAction.isEnabled( subject );
        // case SETPARENTPARAMETERGROUP:
        // return SetParentParameterGroupAction.isEnabled( subject );
        // case SETPARENTPATHTEMPLATE:
        // return SetParentPathTemplateAction.isEnabled( subject );
        // case SETPARENTREFPARENT:
        // return SetParentRefParentAction.isEnabled( subject );
        // case SETRESOURCEEXTENSION:
        // return SetResourceExtensionAction.isEnabled( subject );
        // case SETAFREFERENCEFACET:
        // return SetAFReferenceFacetAction.isEnabled( subject );
        // case SETAFREFERENCETYPE:
        // return SetAFReferenceTypeAction.isEnabled( subject );
        // case SETPARAMETERLOCATION:
        // return SetParameterLocationAction.isEnabled( subject );
        // case SETREQUESTMETHOD:
        // return SetRequestMethodAction.isEnabled( subject );
        // case SETREQUESTPARAMETERGROUP:
        // return SetRequestParameterGroupAction.isEnabled( subject );
        // case SETREQUESTPAYLOAD:
        // return SetRequestPayloadAction.isEnabled( subject );
        // case SETREQUESTPATH:
        // return SetRequestPathAction.isEnabled( subject );
        // case SETRESPONSEPAYLOAD:
        // return SetResponsePayloadAction.isEnabled( subject );
        // case SETPARAMETERGROUPFACET:
        // return SetParameterGroupFacetAction.isEnabled( subject );
        // default:
        // return false;
        // }
    }

}
