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
import org.opentravel.model.resource.OtmParameter;
import org.opentravel.model.resource.OtmParameterGroup;
import org.opentravel.schemacompiler.model.TLParameter;

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

    public static boolean isEnabled(OtmObject subject, OtmObject value) {
        return isEnabled( subject );
    }


    public AddResourceParameterAction() {
        super();
    }

    /**
     * @see org.opentravel.dex.actions.DexRunAction#doIt(java.lang.Object)
     */
    @Override
    public Object doIt(Object data) {
        if (data == null)
            data = new TLParameter();
        if (data instanceof TLParameter)
            return doIt( getSubject(), (TLParameter) data );
        return null;
    }

    public OtmResourceChild doIt(OtmParameterGroup group, TLParameter tlChild) {
        if (group != null && tlChild != null) {
            newChild = group.add( tlChild );
            newChild.setExpanded( true );
            group.setExpanded( true );

            // if (OtmModelElement.get( newChild.getTL() ) != newChild)
            // throw new IllegalStateException( "Incorrect identity listener." );
            // if (!group.getChildren().contains( newChild ))
            // throw new IllegalStateException( "Parent does not own new child." );

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
        OtmResourceChild result = null;
        getSubject().delete( (OtmParameter) newChild );
        return result;
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
