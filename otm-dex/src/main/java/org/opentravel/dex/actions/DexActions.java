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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.actions.constraints.SetConstraintFractionDigitsAction;
import org.opentravel.dex.actions.constraints.SetConstraintMaxExclusiveAction;
import org.opentravel.dex.actions.constraints.SetConstraintMaxInclusiveAction;
import org.opentravel.dex.actions.constraints.SetConstraintMaxLengthAction;
import org.opentravel.dex.actions.constraints.SetConstraintMinExclusiveAction;
import org.opentravel.dex.actions.constraints.SetConstraintMinInclusiveAction;
import org.opentravel.dex.actions.constraints.SetConstraintMinLengthAction;
import org.opentravel.dex.actions.constraints.SetConstraintPatternAction;
import org.opentravel.dex.actions.constraints.SetConstraintTotalDigitsAction;
import org.opentravel.dex.actions.resource.AddResourceChildAction;
import org.opentravel.dex.actions.resource.AddResourceParameterAction;
import org.opentravel.dex.actions.resource.AddResourceResponseAction;
import org.opentravel.dex.actions.resource.AssignResourceSubjectAction;
import org.opentravel.dex.actions.resource.DeleteResourceChildAction;
import org.opentravel.dex.actions.resource.RemoveActionFacetBasePayloadAction;
import org.opentravel.dex.actions.resource.SetAFReferenceCountAction;
import org.opentravel.dex.actions.resource.SetAFReferenceFacetAction;
import org.opentravel.dex.actions.resource.SetAFReferenceTypeAction;
import org.opentravel.dex.actions.resource.SetAbstractAction;
import org.opentravel.dex.actions.resource.SetFirstClassAction;
import org.opentravel.dex.actions.resource.SetIdGroupAction;
import org.opentravel.dex.actions.resource.SetMimeTypesAction;
import org.opentravel.dex.actions.resource.SetParameterFieldAction;
import org.opentravel.dex.actions.resource.SetParameterGroupFacetAction;
import org.opentravel.dex.actions.resource.SetParameterLocationAction;
import org.opentravel.dex.actions.resource.SetParentParameterGroupAction;
import org.opentravel.dex.actions.resource.SetParentPathTemplateAction;
import org.opentravel.dex.actions.resource.SetParentRefParentAction;
import org.opentravel.dex.actions.resource.SetRepeatCountAction;
import org.opentravel.dex.actions.resource.SetRequestMethodAction;
import org.opentravel.dex.actions.resource.SetRequestParameterGroupAction;
import org.opentravel.dex.actions.resource.SetRequestPathAction;
import org.opentravel.dex.actions.resource.SetRequestPayloadAction;
import org.opentravel.dex.actions.resource.SetResourceExtensionAction;
import org.opentravel.dex.actions.resource.SetResponsePayloadAction;
import org.opentravel.dex.actions.resource.SetRestStatusCodesAction;
import org.opentravel.dex.actions.string.BasePathChangeAction;
import org.opentravel.dex.actions.string.DeprecationChangeAction;
import org.opentravel.dex.actions.string.DescriptionChangeAction;
import org.opentravel.dex.actions.string.ExampleChangeAction;
import org.opentravel.dex.actions.string.ManditoryChangeAction;
import org.opentravel.dex.actions.string.NameChangeAction;
import org.opentravel.dex.actions.string.PropertyRoleChangeAction;
import org.opentravel.dex.events.DexChangeEvent;
import org.opentravel.dex.events.DexMemberDeleteEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.DexResourceChangeEvent;
import org.opentravel.dex.events.DexResourceChildModifiedEvent;
import org.opentravel.dex.events.DexResourceModifiedEvent;
import org.opentravel.dex.events.OtmObjectChangeEvent;
import org.opentravel.dex.events.OtmObjectModifiedEvent;
import org.opentravel.model.OtmObject;

import java.lang.reflect.Constructor;

/**
 * Listing of all actions that can change model and their associated event object class (if any).
 * 
 * @author dmh
 *
 */
public enum DexActions {

    DESCRIPTIONCHANGE(DescriptionChangeAction.class, OtmObjectModifiedEvent.class),
    DEPRECATIONCHANGE(DeprecationChangeAction.class, OtmObjectModifiedEvent.class),
    EXAMPLECHANGE(ExampleChangeAction.class, OtmObjectModifiedEvent.class),
    MANDITORYCHANGE(ManditoryChangeAction.class, OtmObjectModifiedEvent.class),
    NAMECHANGE(NameChangeAction.class, OtmObjectModifiedEvent.class),
    TYPECHANGE(SetAssignedTypeAction.class, OtmObjectChangeEvent.class),
    BASETYPECHANGE(BaseTypeChangeAction.class, OtmObjectChangeEvent.class),
    VERSIONUPDATE(UpdateToLaterVersionAction.class, OtmObjectChangeEvent.class),
    // Library members
    ADDALIAS(AddAliasAction.class, OtmObjectChangeEvent.class),
    COPYLIBRARYMEMBER(CopyLibraryMemberAction.class, DexModelChangeEvent.class),
    DELETELIBRARYMEMBER(DeleteLibraryMemberAction.class, DexMemberDeleteEvent.class),
    NEWLIBRARYMEMBER(NewLibraryMemberAction.class, DexModelChangeEvent.class),
    SETLIBRARY(SetLibraryAction.class, DexModelChangeEvent.class),
    DELETEALIAS(DeleteAliasAction.class, OtmObjectChangeEvent.class),
    SETLIST(SetListAction.class, OtmObjectModifiedEvent.class),
    // Properties
    ADDPROPERTY(AddPropertyAction.class, OtmObjectChangeEvent.class),
    COPYPROPERTY(CopyPropertyAction.class, OtmObjectChangeEvent.class),
    DELETEPROPERTY(DeletePropertyAction.class, OtmObjectChangeEvent.class),
    SETREPEATCOUNT(SetRepeatCountAction.class, OtmObjectModifiedEvent.class),
    PROPERTYROLECHANGE(PropertyRoleChangeAction.class, OtmObjectModifiedEvent.class),
    MOVEELEMENT(MoveElementAction.class, OtmObjectChangeEvent.class),
    // Resource
    ADDRESOURCECHILD(AddResourceChildAction.class, DexResourceChangeEvent.class),
    ADDRESOURCEPARAMETER(AddResourceParameterAction.class, DexResourceChangeEvent.class),
    ADDRESOURCERESPONSE(AddResourceResponseAction.class, DexResourceChangeEvent.class),
    DELETERESOURCECHILD(DeleteResourceChildAction.class, DexResourceChangeEvent.class),
    // Assigning resource subject can cause a new resource to be created.
    ASSIGNSUBJECT(AssignResourceSubjectAction.class, DexModelChangeEvent.class),
    BASEPATHCHANGE(BasePathChangeAction.class, DexResourceModifiedEvent.class),
    REMOVEAFBASEPAYLOAD(RemoveActionFacetBasePayloadAction.class, DexResourceChildModifiedEvent.class),
    SETABSTRACT(SetAbstractAction.class, DexResourceModifiedEvent.class),
    SETFIRSTCLASS(SetFirstClassAction.class, DexResourceModifiedEvent.class),
    SETRESOURCEEXTENSION(SetResourceExtensionAction.class, DexResourceModifiedEvent.class),
    // Parameter Group
    SETIDGROUP(SetIdGroupAction.class, DexResourceChildModifiedEvent.class),
    SETPARAMETERGROUPFACET(SetParameterGroupFacetAction.class, DexResourceChildModifiedEvent.class),
    // Parameters
    SETPARAMETERLOCATION(SetParameterLocationAction.class, DexResourceChildModifiedEvent.class),
    SETPARAMETERFIELD(SetParameterFieldAction.class, DexResourceChildModifiedEvent.class),
    //
    SETAFREFERENCETYPE(SetAFReferenceTypeAction.class, DexResourceChildModifiedEvent.class),
    SETAFREFERENCEFACET(SetAFReferenceFacetAction.class, DexResourceChildModifiedEvent.class),
    SETCOMMONACTION(SetCommonAction.class, DexResourceChildModifiedEvent.class),
    //
    SETPARENTPARAMETERGROUP(SetParentParameterGroupAction.class, DexResourceChildModifiedEvent.class),
    SETPARENTPATHTEMPLATE(SetParentPathTemplateAction.class, DexResourceChildModifiedEvent.class),
    SETPARENTREFPARENT(SetParentRefParentAction.class, DexResourceChildModifiedEvent.class),
    //
    SETMIMETYPES(SetMimeTypesAction.class, DexResourceChildModifiedEvent.class),
    SETREQUESTPAYLOAD(SetRequestPayloadAction.class, DexResourceChildModifiedEvent.class),
    SETREQUESTPARAMETERGROUP(SetRequestParameterGroupAction.class, DexResourceChildModifiedEvent.class),
    SETREQUESTMETHOD(SetRequestMethodAction.class, DexResourceChildModifiedEvent.class),
    SETREQUESTPATH(SetRequestPathAction.class, DexResourceChildModifiedEvent.class),
    SETRESPONSEPAYLOAD(SetResponsePayloadAction.class, DexResourceChildModifiedEvent.class),
    SETRESTSTATUSCODES(SetRestStatusCodesAction.class, DexResourceChildModifiedEvent.class),
    SETAFREFERENCEFACETCOUNT(SetAFReferenceCountAction.class, DexResourceChildModifiedEvent.class),
    // Simple Type Constraints
    SETCONSTRAINT_PATTERN(SetConstraintPatternAction.class, OtmObjectModifiedEvent.class),
    SETCONSTRAINT_TOTALDIGITS(SetConstraintTotalDigitsAction.class, OtmObjectModifiedEvent.class),
    SETCONSTRAINT_FRACTIONDIGITS(SetConstraintFractionDigitsAction.class, OtmObjectModifiedEvent.class),
    SETCONSTRAINT_MINLENGTH(SetConstraintMinLengthAction.class, OtmObjectModifiedEvent.class),
    SETCONSTRAINT_MAXLENGTH(SetConstraintMaxLengthAction.class, OtmObjectModifiedEvent.class),
    SETCONSTRAINT_MININCLUSIVE(SetConstraintMinInclusiveAction.class, OtmObjectModifiedEvent.class),
    SETCONSTRAINT_MAXINCLUSIVE(SetConstraintMaxInclusiveAction.class, OtmObjectModifiedEvent.class),
    SETCONSTRAINT_MINEXCLUSIVE(SetConstraintMinExclusiveAction.class, OtmObjectModifiedEvent.class),
    SETCONSTRAINT_MAXEXCLUSIVE(SetConstraintMaxExclusiveAction.class, OtmObjectModifiedEvent.class);

    private final Class<? extends DexChangeEvent> eventClass;

    public Class<? extends DexChangeEvent> eventClass() {
        return eventClass;
    }

    private final Class<? extends DexAction<?>> actionClass;

    public Class<? extends DexAction<?>> actionClass() {
        return actionClass;
    }

    private DexActions(Class<? extends DexAction<?>> actionClass, Class<? extends DexChangeEvent> eventClass) {
        this.actionClass = actionClass;
        this.eventClass = eventClass;
    }

    /**
     * Get the action handler associated with the action. Will return null if subject can't be set, action is not
     * enabled or no action manager is assigned to the subject.
     * 
     * @param action
     * @param subject the otm object the action will act upon
     * @param action handler to use to see if action is enabled. If null, use subject's action handler.
     * @return handler or null
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ExceptionInInitializerError
     */
    public static DexAction<?> getAction(DexActions action, OtmObject subject, DexActionManager actionManager)
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        return getAction( action, subject, null, actionManager );
    }

    private static Logger log = LogManager.getLogger( DexActions.class );

    public static DexAction<?> getAction(DexActions action, OtmObject subject, OtmObject target,
        DexActionManager actionManager)
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {

        if (subject == null)
            throw new IllegalArgumentException( "Missing subject." );
        if (actionManager == null)
            actionManager = subject.getActionManager();
        if (actionManager == null)
            throw new IllegalArgumentException( "Missing action manager." );
        if (action == null)
            throw new IllegalArgumentException( "Missing action or action class." );

        if (action.actionClass == null)
            return null; // Enum does not define action

        // Create handler to return
        DexAction<?> handler = null;
        if (actionManager.isEnabled( action, subject, target )) {
            handler = action.actionClass.newInstance();
        } else {
            log.warn( "Tried to get an action that is not enabled. " + action );
        }
        // do not return the handler if the subject can't be set
        if (handler != null && !handler.setSubject( subject ))
            handler = null;
        if (handler != null)
            handler.setType( action );
        return handler;
    }

    /**
     * Just for debugging
     * <p>
     * Reflection Development notes - Constructors take many sub-types of OtmObject which are not returned.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public static Constructor<?> getConstructor(DexActions action) throws NoSuchMethodException, SecurityException {
        Constructor<? extends DexAction<?>> constructor;
        constructor = action.actionClass.getDeclaredConstructor();
        return constructor;
        // if (constructor != null)
        // handler = constructor.newInstance( subject );
    }


    /**
     * Get the event handler associated with the action.
     * 
     * @return handler or null
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ExceptionInInitializerError
     */
    public static DexChangeEvent getEvent(DexActions action)
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        DexChangeEvent handler = null;
        if (action != null && action.eventClass != null)
            handler = action.eventClass.newInstance();
        return handler;
    }

    /**
     * Get the event handler associated with the action.
     * 
     * @param subject to set into event handler
     * @return handler or null
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ExceptionInInitializerError
     */
    public static DexChangeEvent getEvent(DexActions action, OtmObject subject)
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        DexChangeEvent handler = null;
        if (action != null && action.eventClass != null)
            handler = action.eventClass.newInstance();
        if (handler != null)
            handler.set( subject );
        return handler;
    }
}


