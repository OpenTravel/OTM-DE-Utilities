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
import org.opentravel.model.resource.OtmAction;
import org.opentravel.model.resource.OtmActionResponse;
import org.opentravel.schemacompiler.model.TLActionResponse;

/**
 * Actions that add a resource child to a resource.
 * <p>
 * 
 * @author dmh
 *
 */
public class AddResourceResponseAction extends DexRunAction {
    // private static Log log = LogFactory.getLog( AddResourceChildAction.class );

    private OtmActionResponse newChild = null;

    public static boolean isEnabled(OtmObject otm) {
        return otm instanceof OtmAction && otm.isEditable();
    }

    public static boolean isEnabled(OtmObject subject, OtmObject value) {
        return isEnabled( subject );
    }

    public AddResourceResponseAction() {
        super();
    }

    /**
     * @see org.opentravel.dex.actions.DexRunAction#doIt(java.lang.Object)
     */
    @Override
    public Object doIt(Object data) {
        if (data == null)
            data = new TLActionResponse();
        if (data instanceof TLActionResponse)
            return doIt( getSubject(), (TLActionResponse) data );
        return null;
    }

    public OtmResourceChild doIt(OtmAction action, TLActionResponse tlChild) {
        if (action != null && tlChild != null) {
            newChild = action.add( tlChild );
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
        OtmResourceChild result = null;
        // Remove OTM Object from resource (-sub-type detail can be here)
        getSubject().remove( newChild );
        return result;
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
        if (subject instanceof OtmAction) {
            otm = subject;
            return true;
        }
        return false;
    }

    @Override
    public OtmAction getSubject() {
        return otm instanceof OtmAction ? (OtmAction) otm : null;
    }

    @Override
    public String toString() {
        return "Added " + newChild.getClass().getSimpleName() + " to " + getSubject();
    }
}
