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
import org.opentravel.model.otmProperties.OtmElement;

/**
 * Set the repeat count on a property.
 */
public class SetRepeatCountAction extends DexRunAction {
    // private static Log log = LogFactory.getLog( SetRepeatCountAction.class );


    /**
     * @param subject
     * @return true if editable property
     */
    public static boolean isEnabled(OtmObject subject) {
        return subject instanceof OtmElement && subject.isEditable();
    }

    private int oldCount = 0;

    public SetRepeatCountAction() {
        // Constructor for reflection
    }


    /**
     * {@inheritDoc} Set the library in the library member.
     * 
     * @return
     */
    @Override
    public Object doIt(Object data) {
        if (otm instanceof OtmElement && data instanceof Integer)
            return doIt( ((int) data) );
        return null;
    }

    /**
     * Add the member to the model and clear its no-library action
     * 
     * @param library
     * @return
     */
    public Integer doIt(int value) {
        oldCount = getSubject().getRepeatCount();
        getSubject().setRepeatCount( value );
        return get();
    }

    /**
     * Return the new member or null if none created.
     * 
     * @see org.opentravel.dex.actions.DexRunAction#get()
     */
    @Override
    public Integer get() {
        return getSubject().getRepeatCount();
    }

    // @Override
    // public ValidationFindings getVetoFindings() {
    // return null;
    // }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (isEnabled( subject )) {
            otm = subject;
            return true;
        }
        return false;
    }

    @Override
    public OtmElement getSubject() {
        return (OtmElement) otm;
    }

    @Override
    public String toString() {
        return "Set repeat count to: " + get();
    }

    @Override
    public Integer undoIt() {
        getSubject().setRepeatCount( oldCount );
        return get();
    }
}
