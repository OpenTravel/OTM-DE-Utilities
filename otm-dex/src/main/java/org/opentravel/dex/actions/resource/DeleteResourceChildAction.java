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

package org.opentravel.dex.actions.resource;

import org.opentravel.dex.actions.DexRunAction;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.resource.OtmAction;
import org.opentravel.model.resource.OtmActionResponse;
import org.opentravel.model.resource.OtmParameter;
import org.opentravel.model.resource.OtmParameterGroup;

/**
 * Actions that delete a resource child from a resource.
 * <p>
 * 
 * @author dmh
 *
 */
public class DeleteResourceChildAction extends DexRunAction {
    // private static Logger log = LogManager.getLogger( AddResourceChildAction.class );

    public static boolean isEnabled(OtmObject otm) {
        if (otm instanceof OtmResourceChild) {
            if (otm.getOwningMember() == null)
                return false;
            if (((OtmResourceChild) otm).getParent() == null)
                return false;
        } else
            return false;
        return otm.isEditable();
    }

    public static boolean isEnabled(OtmObject subject, OtmObject value) {
        return isEnabled( subject );
    }

    public DeleteResourceChildAction() {
        super();
    }

    public Object doIt() {
        OtmObject parent = null;
        if (otm instanceof OtmResourceChild) {
            // Remove from TL and Otm parents
            parent = ((OtmResourceChild) otm).getParent();
            if (parent instanceof OtmResource)
                ((OtmResource) parent).delete( otm );
            else if (parent instanceof OtmParameterGroup && otm instanceof OtmParameter)
                ((OtmParameterGroup) parent).delete( otm );
            else if (parent instanceof OtmAction && otm instanceof OtmActionResponse)
                ((OtmAction) parent).delete( otm );
        }
        return parent;
    }

    @Override
    public Object doIt(Object data) {
        return doIt();
    }

    @Override
    public Object get() {
        return null;
    }

    @Override
    public OtmResourceChild undoIt() {
        if (otm instanceof OtmResourceChild) {
            OtmObject parent = ((OtmResourceChild) otm).getParent();
            if (parent instanceof OtmResource)
                ((OtmResource) parent).add( otm.getTL() );
            else if (parent instanceof OtmParameterGroup && otm instanceof OtmParameter)
                ((OtmParameterGroup) parent).add( (OtmParameter) otm );
            else if (parent instanceof OtmAction && otm instanceof OtmActionResponse)
                ((OtmAction) parent).add( (OtmActionResponse) otm );
            return (OtmResourceChild) otm;
        }
        return null;
    }

    /**
     * @see org.opentravel.dex.actions.DexAction#isValid()
     */
    @Override
    public boolean isValid() {
        return true;
    }

    /**
     * @see org.opentravel.dex.actions.DexAction#setSubject(org.opentravel.model.OtmObject)
     */
    @Override
    public boolean setSubject(OtmObject subject) {
        if (subject instanceof OtmResourceChild) {
            otm = subject;
            return true;
        }
        return false;
    }

    @Override
    public OtmResourceChild getSubject() {
        return otm instanceof OtmResourceChild ? (OtmResourceChild) otm : null;
    }

    @Override
    public String toString() {
        return "Deleted " + getSubject();
    }
}
