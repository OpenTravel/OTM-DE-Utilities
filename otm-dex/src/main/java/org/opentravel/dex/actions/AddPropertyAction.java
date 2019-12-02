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
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * This action creates a new property in the subject property owner.
 */
public class AddPropertyAction extends DexRunAction {
    private static Log log = LogFactory.getLog( AddPropertyAction.class );

    /**
     * Any OTM object that uses the intended model manager.
     * 
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        if (subject.getLibrary() == null)
            return false;
        // boolean isEditable = subject.getLibrary().isChainEditable();
        return subject instanceof OtmPropertyOwner && subject.getLibrary().isChainEditable();
    }

    private OtmProperty newProperty = null;
    // private OtmLibraryMember newMinorLibraryMember = null;
    private OtmPropertyOwner newPropertyOwner = null;

    public AddPropertyAction() {
        // Constructor for reflection
    }

    /**
     * {@inheritDoc} Create a new property of passed type to the subject property owner.
     * 
     * @see org.opentravel.dex.actions.DexRunAction#doIt(java.lang.Object)
     */
    @Override
    public Object doIt(Object data) {
        if (otm != null && otm.getModelManager() != null && data instanceof OtmPropertyType) {

            OtmLibrary subjectLibrary = getSubject().getLibrary();
            if (subjectLibrary == null)
                return null;

            // Create a minor version if the subject is in an older library in editable chain
            //
            if (!subjectLibrary.isEditable() && subjectLibrary.isChainEditable()) {
                // Get the latest library in the chain that is editable
                newPropertyOwner = subjectLibrary.getVersionChain().getNewMinorPropertyOwner( getSubject() );
                if (newPropertyOwner == null)
                    return null;
            }

            // Build and hold onto for undo
            OtmPropertyOwner pOwner = newPropertyOwner == null ? getSubject() : newPropertyOwner;
            newProperty = OtmPropertyType.build( (OtmPropertyType) data, pOwner );

            isValid();
        }
        // log.debug( "Added new property " + get() );
        return get();
    }

    /**
     * Return the new member or null if none created.
     * 
     * @see org.opentravel.dex.actions.DexRunAction#get()
     */
    @Override
    public OtmProperty get() {
        return newProperty;
    }

    @Override
    public OtmPropertyOwner getSubject() {
        return (OtmPropertyOwner) otm;
    }

    @Override
    public ValidationFindings getVetoFindings() {
        return null;
    }

    @Override
    public boolean isValid() {
        // Validate the parent - adding a property could change validation status
        if (newProperty != null && newProperty.getOwningMember() != null)
            newProperty.getOwningMember().isValid( true );
        return newProperty != null ? newProperty.isValid() : false;
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (subject instanceof OtmPropertyOwner)
            otm = subject;
        return otm != null;
    }

    @Override
    public String toString() {
        String name = "";
        if (newProperty != null)
            if (newProperty.getName() != null)
                name = newProperty.getName();
            else
                name = OtmPropertyType.getType( newProperty ).label();
        return "Created new property: " + name;
    }

    @Override
    public OtmProperty undoIt() {
        if (newProperty != null) {
            newProperty.getParent().delete( newProperty );
            newProperty = null;
        }
        if (newPropertyOwner != null) {
            // TODO - what if they have added more stuff to this member?
            // if (member.hasNonInheritedDescendants())
            // Just remove the new property.

            OtmLibraryMember member = newPropertyOwner.getOwningMember();
            if (member != null && member.getLibrary() != null)
                // undo when minor version created
                member.getLibrary().delete( newPropertyOwner.getOwningMember() );
            newPropertyOwner = null;
            newProperty = null;
        }
        // log.debug( "Undo new property." );
        return newProperty;
    }
}
