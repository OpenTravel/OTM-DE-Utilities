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
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;

/**
 * This action creates a new OtmAlias the subject library member.
 */
public class AddAliasAction extends DexRunAction {
    private static Log log = LogFactory.getLog( AddAliasAction.class );

    /**
     * Any OTM object that uses the intended model manager.
     * 
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        if (subject == null || !(subject.getTL() instanceof TLAliasOwner))
            return false;
        if (subject instanceof OtmContextualFacet)
            return false; // unsupported operation exception thrown
        return subject instanceof OtmLibraryMember && subject.isEditable();
    }

    private OtmAlias newAlias = null;

    public AddAliasAction() {
        // Constructor for reflection
    }

    // /**
    // * {@inheritDoc} Create a new property of passed type to the subject property owner.
    // *
    // * @see org.opentravel.dex.actions.DexRunAction#doIt(java.lang.Object)
    // */
    @Override
    public Object doIt(Object data) {
        String name = data instanceof String ? (String) data : "NewAliasName";
        try {
            newAlias = new OtmAlias( new TLAlias(), getSubject() );
        } catch (Exception e) {
            log.debug( "Error creating alias." );
            return null;
        }
        newAlias.setName( name );
        return newAlias;
    }

    public Object doIt() {
        return doIt( "NewAliasName" );
    }
    // if (otm != null && otm.getModelManager() != null && data instanceof OtmPropertyType) {
    //
    // OtmLibrary subjectLibrary = getSubject().getLibrary();
    // if (subjectLibrary == null)
    // return null;
    //
    // // Create a minor version if the subject is in an older library in editable chain
    // //
    // if (!subjectLibrary.isEditable() && subjectLibrary.isChainEditable()) {
    // // Get the latest library in the chain that is editable
    // newPropertyOwner = subjectLibrary.getVersionChain().getNewMinorPropertyOwner( getSubject() );
    // if (newPropertyOwner == null)
    // return null;
    // }
    //
    // // Build and hold onto for undo
    // OtmPropertyOwner pOwner = newPropertyOwner == null ? getSubject() : newPropertyOwner;
    // newProperty = OtmPropertyType.build( (OtmPropertyType) data, pOwner );
    //
    // isValid();
    // }
    // // log.debug( "Added new property " + get() );
    // return get();
    // }

    /**
     * Return the new member or null if none created.
     * 
     * @see org.opentravel.dex.actions.DexRunAction#get()
     */
    @Override
    public OtmAlias get() {
        return newAlias;
    }

    @Override
    public OtmLibraryMember getSubject() {
        return (OtmLibraryMember) otm;
    }

    // @Override
    // public ValidationFindings getVetoFindings() {
    // return null;
    // }

    // @Override
    // public boolean isValid() {
    // // Validate the parent - adding a property could change validation status
    // if (newProperty != null && newProperty.getOwningMember() != null)
    // newProperty.getOwningMember().isValid( true );
    // return newProperty != null ? newProperty.isValid() : false;
    // }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (isEnabled( subject ))
            otm = subject;
        return otm != null;
    }

    @Override
    public String toString() {
        String name = "";
        if (newAlias != null)
            name = newAlias.getName();
        return "Created new alias: " + name;
    }

    @Override
    public OtmAlias undoIt() {
        if (newAlias != null) {
            newAlias.getOwningMember().delete( newAlias );
            newAlias = null;
        }
        // log.debug( "Undo new alias." );
        return newAlias;
    }
}
