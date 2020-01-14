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
import org.opentravel.common.RestStatusCodesMap;
import org.opentravel.model.OtmObject;
import org.opentravel.model.resource.OtmActionResponse;

import java.util.Collections;
import java.util.List;

public class SetRestStatusCodesAction extends DexRunAction {
    private static Log log = LogFactory.getLog( SetRestStatusCodesAction.class );

    public static boolean isEnabled(OtmObject subject) {
        return (subject.isEditable() && subject instanceof OtmActionResponse);
    }

    private OtmObject object = null;
    private RestStatusCodesMap oldMap = null;


    public SetRestStatusCodesAction() {
        // Constructor for reflection
    }

    /**
     * This action will get the data from the user via modal dialog
     */
    @Override
    public Object doIt(Object data) {
        if (data instanceof RestStatusCodesMap)
            return doIt( (RestStatusCodesMap) data );
        return null;
    }

    public Object doIt(RestStatusCodesMap data) {
        if (isEnabled( object )) {
            oldMap = new RestStatusCodesMap( object );
            set( data.getTLList() );
            return get();
        }
        return null;
    }

    @Override
    public List<Integer> get() {
        if (object instanceof OtmActionResponse)
            return ((OtmActionResponse) object).getRestStatusCodes();
        return Collections.emptyList();
    }

    // @Override
    // public ValidationFindings getVetoFindings() {
    // return null;
    // }

    @Override
    public boolean isValid() {
        return true;
    }

    public boolean set(List<Integer> list) {
        if (object instanceof OtmActionResponse) {
            ((OtmActionResponse) object).setRestStatusCodes( list );
            return true;
        }
        return false;
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (subject instanceof OtmActionResponse) {
            object = subject;
            return true;
        }
        return false;
    }

    @Override
    public OtmObject getSubject() {
        return object;
    }

    @Override
    public String toString() {
        return "Set Rest Status Codes to: " + get();
    }

    @Override
    public List<Integer> undoIt() {
        log.debug( "Undo-ing mime type change." );
        if (oldMap != null) {
            if (object instanceof OtmActionResponse)
                ((OtmActionResponse) object).setRestStatusCodes( oldMap.getTLList() );
        }
        return get();
    }

    // /**
    // * @see org.opentravel.dex.actions.DexRunAction#doIt()
    // */
    // @Override
    // public Object doIt() {
    // // No-op
    // return null;
    // }
}
