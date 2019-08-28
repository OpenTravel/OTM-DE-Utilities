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

import org.opentravel.dex.events.DexChangeEvent;
import org.opentravel.dex.events.DexResourceChildModifiedEvent;
import org.opentravel.dex.events.DexResourceModifiedEvent;

/**
 * Listing of all actions that can change model and their associated event object class (if any).
 * 
 * @author dmh
 *
 */
public enum DexActions {

    NAMECHANGE(null),
    DESCRIPTIONCHANGE(null),
    TYPECHANGE(null),
    //
    BASEPATHCHANGE(DexResourceModifiedEvent.class),
    SETABSTRACT(DexResourceModifiedEvent.class),
    SETFIRSTCLASS(DexResourceModifiedEvent.class),
    SETRESOURCEEXTENSION(DexResourceModifiedEvent.class),
    //
    SETIDGROUP(DexResourceChildModifiedEvent.class),
    SETCOMMONACTION(DexResourceChildModifiedEvent.class),
    SETPARENTPARAMETERGROUP(DexResourceChildModifiedEvent.class),
    SETPARENTPATHTEMPLATE(DexResourceChildModifiedEvent.class),
    SETPARENTREFPARENT(DexResourceChildModifiedEvent.class),
    SETAFREFERENCETYPE(DexResourceChildModifiedEvent.class),
    SETAFREFERENCEFACET(DexResourceChildModifiedEvent.class),
    SETREQUESTPAYLOAD(DexResourceChildModifiedEvent.class),
    SETREQUESTPARAMETERGROUP(DexResourceChildModifiedEvent.class),
    SETREQUESTMETHOD(DexResourceChildModifiedEvent.class),
    SETREQUESTPATH(DexResourceChildModifiedEvent.class),
    SETRESPONSEPAYLOAD(DexResourceChildModifiedEvent.class),
    SETPARAMETERLOCATION(DexResourceChildModifiedEvent.class),
    SETPARAMETERGROUPFACET(DexResourceChildModifiedEvent.class);

    private final Class<? extends DexChangeEvent> handlerClass;

    public Class<? extends DexChangeEvent> handlerClass() {
        return handlerClass;
    }

    private DexActions(Class<? extends DexChangeEvent> handlerClass) {
        this.handlerClass = handlerClass;
    }

    /**
     * Get the event handler associated with the action.
     * 
     * @return handler or null
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ExceptionInInitializerError
     */
    public static DexChangeEvent getHandler(DexActions action)
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException {
        DexChangeEvent handler = null;
        if (action != null && action.handlerClass != null)
            handler = action.handlerClass.newInstance();
        return handler;
    }

}


