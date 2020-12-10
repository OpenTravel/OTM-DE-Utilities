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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.actions.DexRunAction;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * Actions that add a resource child to a resource.
 * <p>
 * 
 * @author dmh
 *
 */
public class AddResourceChildAction extends DexRunAction {
    private static Log log = LogFactory.getLog( AddResourceChildAction.class );

    private OtmResourceChild newChild = null;
    private OtmResource newResource = null;

    public static boolean isEnabled(OtmObject otm) {
        // Made to match AddPropertyAction
        if (otm.getLibrary() == null)
            return false;
        return otm instanceof OtmResource && otm.getLibrary().isChainEditable();
    }

    public static boolean isEnabled(OtmObject subject, OtmObject value) {
        return isEnabled( subject );
    }

    public AddResourceChildAction() {
        super();
    }

    /**
     * @see org.opentravel.dex.actions.DexRunAction#doIt(java.lang.Object)
     */
    @Override
    public Object doIt(Object data) {
        if (data instanceof TLModelElement)
            return doIt( getSubject(), (TLModelElement) data );
        return null;
    }

    public OtmResourceChild doIt(OtmResource resource, TLModelElement tlChild) {
        if (resource != null && tlChild != null) {

            // Create a minor version if the subject is in an older library in editable chain
            OtmLibrary subjectLibrary = getSubject().getLibrary();
            if (subjectLibrary == null)
                return null;
            if (!subjectLibrary.isEditable() && subjectLibrary.isChainEditable()) {
                // Get the latest library in the chain that is editable
                OtmLibraryMember newOTM = subjectLibrary.getVersionChain().getNewMinorLibraryMember( getSubject() );
                if (!(newOTM instanceof OtmResource))
                    return null;
                newResource = (OtmResource) newOTM;
                resource = newResource;
            }

            newChild = resource.add( tlChild );
            resource.setExpanded( true );
            newChild.setExpanded( true );
        }
        return newChild;
    }

    /**
     * Simply get the object's field value
     */
    public Object get() {
        return null;
    }

    @Override
    public OtmResourceChild undoIt() {
        getSubject().delete( newChild );
        return null;
    }

    // /**
    // * @see org.opentravel.dex.actions.DexAction#getVetoFindings()
    // */
    // @Override
    // public ValidationFindings getVetoFindings() {
    // return null;
    // }

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
        if (subject instanceof OtmResource) {
            otm = subject;
            return true;
        }
        return false;
    }

    // FIXME
    // Throw different event DexResourceChildCreated
    // Give that event the newChild not otm as subject
    //
    @Override
    public OtmResource getSubject() {
        return otm instanceof OtmResource ? (OtmResource) otm : null;
    }

    @Override
    public String toString() {
        String result = "";
        if (newChild != null)
            result = "Added " + newChild.getClass().getSimpleName() + " to " + getSubject();
        return result;
    }
}
