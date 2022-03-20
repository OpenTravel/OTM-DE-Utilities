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
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;

/**
 * Action that deletes an alias.
 * <p>
 * 
 * @author dmh
 *
 */
public class DeleteAliasAction extends DexRunAction {
    private static Logger log = LogManager.getLogger( DeleteAliasAction.class );

    /**
     * Can the passed alias be deleted?
     * 
     * @param the alias to be deleted
     * @return must be alias and editable to be true
     */
    public static boolean isEnabled(OtmObject otm) {
        if (otm instanceof OtmAlias) {
            if (otm.getOwningMember() == null)
                return false;
            if (((OtmAlias) otm).getOwningMember() == null)
                return false;
        } else
            return false;
        return otm.isEditable();
    }

    public static boolean isEnabled(OtmObject subject, OtmObject value) {
        return isEnabled( subject );
    }

    private OtmAlias deletedAlias = null;

    public DeleteAliasAction() {
        // Reflection constructor
    }

    public Object doIt() {
        Object parent = null;
        if (isEnabled( otm )) {
            deletedAlias = getSubject();
            parent = getSubject().getOwningMember();
            getSubject().getOwningMember().delete( getSubject() );
        }
        return parent;
    }

    @Override
    public Object doIt(Object data) {
        return doIt();
    }

    @Override
    public Object get() {
        return deletedAlias;
    }

    @Override
    public OtmResourceChild undoIt() {
        if (deletedAlias != null && deletedAlias.getOwningMember() != null) {
            ((TLAliasOwner) deletedAlias.getOwningMember().getTL()).addAlias( deletedAlias.getTL() );
            deletedAlias.getOwningMember().add( deletedAlias );
            deletedAlias = null;
        }
        return null;
    }

    // /**
    // * @see org.opentravel.dex.actions.DexAction#getVetoFindings()
    // */
    // @Override
    // public ValidationFindings getVetoFindings() {
    // return null;
    // }

    // /**
    // * @see org.opentravel.dex.actions.DexAction#isValid()
    // */
    // @Override
    // public boolean isValid() {
    // return true;
    // }

    /**
     * @see org.opentravel.dex.actions.DexAction#setSubject(org.opentravel.model.OtmObject)
     */
    @Override
    public boolean setSubject(OtmObject subject) {
        if (subject instanceof OtmAlias) {
            otm = subject;
            return true;
        }
        return false;
    }

    @Override
    public OtmAlias getSubject() {
        return otm instanceof OtmAlias ? (OtmAlias) otm : null;
    }

    @Override
    public String toString() {
        return "Deleted alias " + getSubject();
    }
}
