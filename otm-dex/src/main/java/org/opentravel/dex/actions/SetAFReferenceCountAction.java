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
import org.opentravel.model.resource.OtmActionFacet;

public class SetAFReferenceCountAction extends DexRunAction {
    // private static Log log = LogFactory.getLog( SetAbstractAction.class );
    private int oldValue;

    /**
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        if (subject instanceof OtmActionFacet)
            return subject.isEditable();
        return false;
    }

    protected SetAFReferenceCountAction() {}

    protected Integer get() {
        return getSubject().getRepeatCount();
    }

    @Override
    public OtmActionFacet getSubject() {
        return (OtmActionFacet) otm;
    }

    protected void set(int value) {
        getSubject().setRepeatCount( value );
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (!(subject instanceof OtmActionFacet))
            return false;
        otm = subject;
        return true;
    }

    @Override
    public String toString() {
        return "Reference facet repeat count set to " + get();
    }

    /**
     * @see org.opentravel.dex.actions.DexAction#undoIt()
     */
    @Override
    public Object undoIt() {
        set( oldValue );
        oldValue = 0;
        return 0;
    }

    /**
     * @see org.opentravel.dex.actions.DexRunAction#doIt(java.lang.Object)
     */
    @Override
    public Object doIt(Object data) {
        oldValue = getSubject().getRepeatCount();
        if (data instanceof Integer)
            set( (int) data );
        return get();
    }

}
