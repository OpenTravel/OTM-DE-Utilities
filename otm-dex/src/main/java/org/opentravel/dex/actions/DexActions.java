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

import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.events.DexChangeEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.DexResourceChangeEvent;
import org.opentravel.dex.events.DexResourceChildModifiedEvent;
import org.opentravel.dex.events.DexResourceModifiedEvent;
import org.opentravel.dex.events.OtmObjectChangeEvent;
import org.opentravel.dex.events.OtmObjectModifiedEvent;
import org.opentravel.model.OtmObject;

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
    TYPECHANGE(AssignedTypeChangeAction.class, OtmObjectChangeEvent.class),
    BASETYPECHANGE(BaseTypeChangeAction.class, OtmObjectChangeEvent.class),
    // Library members
    NEWLIBRARYMEMBER(NewLibraryMemberAction.class, DexModelChangeEvent.class),
    SETMEMBERLIBRARY(SetLibraryAction.class, DexModelChangeEvent.class),
    DELETELIBRARYMEMBER(DeleteLibraryMemberAction.class, DexModelChangeEvent.class),
    // Properties
    ADDPROPERTY(AddPropertyAction.class, OtmObjectChangeEvent.class),
    DELETEPROPERTY(DeletePropertyAction.class, OtmObjectChangeEvent.class),
    SETREPEATCOUNT(SetRepeatCountAction.class, OtmObjectModifiedEvent.class),
    PROPERTYROLECHANGE(PropertyRoleChangeAction.class, OtmObjectModifiedEvent.class),
    //
    ADDRESOURCECHILD(AddResourceChildAction.class, DexResourceChangeEvent.class),
    ADDRESOURCEPARAMETER(AddResourceParameterAction.class, DexResourceChangeEvent.class),
    ADDRESOURCERESPONSE(AddResourceResponseAction.class, DexResourceChangeEvent.class),
    DELETERESOURCECHILD(DeleteResourceChildAction.class, DexResourceChangeEvent.class),
    //
    ASSIGNSUBJECT(AssignResourceSubjectAction.class, DexResourceModifiedEvent.class),
    BASEPATHCHANGE(BasePathChangeAction.class, DexResourceModifiedEvent.class),
    SETABSTRACT(SetAbstractAction.class, DexResourceModifiedEvent.class),
    SETFIRSTCLASS(SetFirstClassAction.class, DexResourceModifiedEvent.class),
    SETRESOURCEEXTENSION(SetResourceExtensionAction.class, DexResourceModifiedEvent.class),
    //
    REMOVEAFBASEPAYLOAD(RemoveActionFacetBasePayloadAction.class, DexResourceChildModifiedEvent.class),
    SETAFREFERENCETYPE(SetAFReferenceTypeAction.class, DexResourceChildModifiedEvent.class),
    SETAFREFERENCEFACET(SetAFReferenceFacetAction.class, DexResourceChildModifiedEvent.class),
    SETCOMMONACTION(SetCommonAction.class, DexResourceChildModifiedEvent.class),
    SETIDGROUP(SetIdGroupAction.class, DexResourceChildModifiedEvent.class),
    SETPARAMETERLOCATION(SetParameterLocationAction.class, DexResourceChildModifiedEvent.class),
    SETPARAMETERGROUPFACET(SetParameterGroupFacetAction.class, DexResourceChildModifiedEvent.class),
    SETPARENTPARAMETERGROUP(SetParentParameterGroupAction.class, DexResourceChildModifiedEvent.class),
    SETPARENTPATHTEMPLATE(SetParentPathTemplateAction.class, DexResourceChildModifiedEvent.class),
    SETPARENTREFPARENT(SetParentRefParentAction.class, DexResourceChildModifiedEvent.class),
    SETMIMETYPES(SetMimeTypesAction.class, DexResourceChildModifiedEvent.class),
    SETREQUESTPAYLOAD(SetRequestPayloadAction.class, DexResourceChildModifiedEvent.class),
    SETREQUESTPARAMETERGROUP(SetRequestParameterGroupAction.class, DexResourceChildModifiedEvent.class),
    SETREQUESTMETHOD(SetRequestMethodAction.class, DexResourceChildModifiedEvent.class),
    SETREQUESTPATH(SetRequestPathAction.class, DexResourceChildModifiedEvent.class),
    SETRESPONSEPAYLOAD(SetResponsePayloadAction.class, DexResourceChildModifiedEvent.class),
    SETRESTSTATUSCODES(SetRestStatusCodesAction.class, DexResourceChildModifiedEvent.class);

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
        if (actionManager.isEnabled( action, subject )) {
            handler = action.actionClass.newInstance();
            // do not return the handler if the subject can't be set
            if (!handler.setSubject( subject ))
                handler = null;
        }
        if (handler != null)
            handler.setType( action );
        return handler;
    }
    // Reflection Development notes - Constructors take many sub-types of OtmObject which are not returned.
    // Constructor<? extends DexAction<?>> constructor;
    // constructor = action.actionClass.getDeclaredConstructor( OtmObject.class );
    // if (constructor != null)
    // handler = constructor.newInstance( subject );

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


