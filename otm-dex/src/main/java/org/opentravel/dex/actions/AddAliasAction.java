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
     * Enabled for any editable OTM object whose TL is an TLAliasOwner and not a contextual facet.
     * 
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        if (subject == null || !(subject.getTL() instanceof TLAliasOwner))
            return false;
        if (subject instanceof OtmContextualFacet)
            return false; // would throw an unsupported operation exception
        return subject instanceof OtmLibraryMember && subject.isEditable();
    }

    public static boolean isEnabled(OtmObject subject, OtmObject value) {
        return isEnabled( subject );
    }

    private OtmAlias newAlias = null;

    public AddAliasAction() {
        // Constructor for reflection
    }

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
