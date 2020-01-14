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
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import java.util.Map;
import java.util.Map.Entry;

/**
 * This action deletes a library member.
 */
public class DeleteLibraryMemberAction extends DexRunAction {
    private static Log log = LogFactory.getLog( DeleteLibraryMemberAction.class );


    /**
     * Any OTM object that uses the intended model manager.
     * 
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        return subject instanceof OtmLibraryMember && subject.isEditable();
    }

    private OtmLibraryMember deletedMember = null;
    // Contextual facets need the name of the contributed owner
    private String deletedMemberName = "";
    private OtmLibrary memberLibrary = null;
    private Map<OtmTypeUser,OtmTypeProvider> typeUsers = null;

    public DeleteLibraryMemberAction() {
        // Constructor for reflection
    }

    /**
     * {@inheritDoc} This action will delete the subject
     */
    public OtmLibraryMember doIt() {
        if (ignore)
            return null;

        if (isEnabled( otm ) && (otm.getLibrary() != null && otm instanceof OtmLibraryMember)) {
            // Hold onto member for undo
            deletedMember = (OtmLibraryMember) otm;
            deletedMemberName = otm.getName();
            memberLibrary = otm.getLibrary();
            typeUsers = ((OtmLibraryMember) otm).getPropertiesWhereUsed();

            // Delete from TL library and model manager
            memberLibrary.delete( (OtmLibraryMember) otm );
            log.debug( "Deleted library member: " + deletedMember );
        } else
            return null;

        return get(); // must return non-null to be put on queue
    }

    /**
     * {@inheritDoc} Just doIt().
     * 
     * @return
     */
    @Override
    public Object doIt(Object data) {
        return doIt();
    }

    /**
     * Return the deleted member or null.
     * 
     * @see org.opentravel.dex.actions.DexRunAction#get()
     */
    @Override
    public OtmLibraryMember get() {
        return deletedMember;
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
        return "Deleted library member: " + deletedMemberName;
    }

    @Override
    public OtmLibraryMember undoIt() {
        // TESTME - on undo, the types assigned to this member are no longer assigned
        memberLibrary.add( deletedMember );
        // Contextual facets are the only library members that also are children of other members via the
        // contributed facet.
        if (deletedMember instanceof OtmContextualFacet) {
            OtmContributedFacet contrib = ((OtmContextualFacet) deletedMember).getWhereContributed();
            if (contrib != null && contrib.getOwningMember() != null)
                contrib.getOwningMember().add( contrib );
        }

        // TESTME - assigns to LM even if a descendant was assigned
        for (Entry<OtmTypeUser,OtmTypeProvider> entry : typeUsers.entrySet())
            entry.getKey().setAssignedType( entry.getValue() );

        log.debug( "Undo delete of " + get() );
        return get();
    }
}
