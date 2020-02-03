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

import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResourceParentRef;

/**
 * Actions that add a resource child to a resource.
 * <p>
 * 
 * @author dmh
 *
 */
public class AddResourceChildAction extends DexRunAction {
    // private static Log log = LogFactory.getLog( AddResourceChildAction.class );

    private OtmResourceChild newChild = null;

    public static boolean isEnabled(OtmObject otm) {
        return otm instanceof OtmResource && otm.isEditable();
    }

    public AddResourceChildAction() {
        super();
    }

    // public Object doIt() {
    // return doIt(null);
    // }

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
            // newChild = resource.add( tlChild );
            if (tlChild instanceof TLResourceParentRef)
                newChild = resource.add( (TLResourceParentRef) tlChild, null );
            else if (tlChild instanceof TLParamGroup)
                newChild = resource.add( (TLParamGroup) tlChild );
            else if (tlChild instanceof TLAction)
                newChild = resource.add( (TLAction) tlChild );
            else if (tlChild instanceof TLActionFacet)
                newChild = resource.add( (TLActionFacet) tlChild );
            resource.setExpanded( true );
            newChild.setExpanded( true );
        }
        return newChild;
    }

    /**
     * Simply get the object's field value
     */
    protected Object get() {
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
