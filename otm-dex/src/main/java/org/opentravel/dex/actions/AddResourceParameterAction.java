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
import org.opentravel.model.resource.OtmParameter;
import org.opentravel.model.resource.OtmParameterGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Actions that add a resource child to a resource.
 * <p>
 * 
 * @author dmh
 *
 */
public class AddResourceParameterAction extends DexRunAction {
    // private static Log log = LogFactory.getLog( AddResourceChildAction.class );

    private OtmResourceChild newChild = null;

    public static boolean isEnabled(OtmObject otm) {
        return otm instanceof OtmParameterGroup && otm.isEditable();
    }

    public AddResourceParameterAction() {
        super();
    }

    public Object doIt() {
        return doIt( new TLParameter() );
    }

    /**
     * @see org.opentravel.dex.actions.DexRunAction#doIt(java.lang.Object)
     */
    @Override
    public Object doIt(Object data) {
        if (data instanceof TLParameter)
            return doIt( getSubject(), (TLParameter) data );
        return null;
    }

    public OtmResourceChild doIt(OtmParameterGroup group, TLParameter tlChild) {
        if (group != null && tlChild != null) {
            newChild = group.add( tlChild );

            // Record action to allow undo. Will validate results and warn user.
            group.getActionManager().push( this );
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
        getSubject().remove( (OtmParameter) newChild );
        return result;
    }

    @Override
    public ValidationFindings getVetoFindings() {
        return null;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (subject instanceof OtmParameterGroup) {
            otm = subject;
            return true;
        }
        return false;
    }

    @Override
    public OtmParameterGroup getSubject() {
        return otm instanceof OtmParameterGroup ? (OtmParameterGroup) otm : null;
    }

    @Override
    public String toString() {
        return "Added " + newChild.getClass().getSimpleName() + " to " + getSubject();
    }
}
