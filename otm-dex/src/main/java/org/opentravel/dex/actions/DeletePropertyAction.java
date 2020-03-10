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
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmProperties.OtmProperty;

/**
 * Actions that delete a property.
 * <p>
 * 
 * @author dmh
 *
 */
public class DeletePropertyAction extends DexRunAction {
    private static Log log = LogFactory.getLog( AddResourceChildAction.class );

    public static boolean isEnabled(OtmObject otm) {
        if (otm instanceof OtmProperty) {
            if (otm.getOwningMember() == null)
                return false;
            if (((OtmProperty) otm).getParent() == null)
                return false;
        } else
            return false;
        return otm.isEditable();
    }

    private OtmProperty deletedProperty = null;

    public DeletePropertyAction() {
        // Reflection constructor
    }

    public Object doIt() {
        Object parent = null;
        if (isEnabled( otm )) {
            deletedProperty = getSubject();
            parent = getSubject().getParent();
            getSubject().getParent().delete( getSubject() );
        }
        return parent;
    }

    @Override
    public Object doIt(Object data) {
        return doIt();
    }

    @Override
    protected Object get() {
        return deletedProperty;
    }

    @Override
    public OtmResourceChild undoIt() {
        if (deletedProperty != null) {
            log.debug( "TEST - undo delete" );
            deletedProperty.getParent().add( deletedProperty.getTL() );
            deletedProperty.getParent().refresh();
            deletedProperty = null;
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
        if (subject instanceof OtmProperty) {
            otm = subject;
            return true;
        }
        return false;
    }

    @Override
    public OtmProperty getSubject() {
        return otm instanceof OtmProperty ? (OtmProperty) otm : null;
    }

    @Override
    public String toString() {
        return "Deleted " + getSubject();
    }
}
