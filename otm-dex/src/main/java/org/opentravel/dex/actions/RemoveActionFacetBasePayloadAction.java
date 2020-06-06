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
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.resource.OtmActionFacet;

/**
 * This action remove base payload from action facet.
 */
public class RemoveActionFacetBasePayloadAction extends DexRunAction {
    private static Log log = LogFactory.getLog( RemoveActionFacetBasePayloadAction.class );

    /**
     * An action facet with base payload set.
     * 
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        return subject instanceof OtmActionFacet && ((OtmActionFacet) subject).getBasePayload() != null;
    }

    public static boolean isEnabled(OtmObject subject, OtmObject value) {
        return isEnabled( subject );
    }

    private OtmLibraryMember oldPayload = null;

    public RemoveActionFacetBasePayloadAction() {
        // Constructor for reflection
    }

    /**
     * {@inheritDoc}
     */
    public OtmLibraryMember doIt() {
        if (ignore)
            return null;
        log.debug( "Ready to remove base payload" );
        if (isEnabled( otm )) {
            oldPayload = ((OtmActionFacet) otm).getBasePayload();
            ((OtmActionFacet) otm).setBasePayload( null );
        }
        return get();
    }

    /**
     * {@inheritDoc} The new library action adds library members to the model manager.
     * 
     * @return
     */
    @Override
    public Object doIt(Object data) {
        return doIt();
    }

    /**
     * Return the new member or null if none created.
     * 
     * @see org.opentravel.dex.actions.DexRunAction#get()
     */
    @Override
    public OtmLibraryMember get() {
        return oldPayload;
    }

    // @Override
    // public ValidationFindings getVetoFindings() {
    // return null;
    // }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        otm = subject;
        return otm != null;
    }

    @Override
    public String toString() {
        return "Removed base payload " + oldPayload + " from " + otm;
    }

    @Override
    public OtmLibraryMember undoIt() {
        ((OtmActionFacet) otm).setBasePayload( oldPayload );
        return get();
    }
}
