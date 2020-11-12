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
import org.opentravel.common.MimeTypeMap;
import org.opentravel.dex.actions.DexRunAction;
import org.opentravel.model.OtmObject;
import org.opentravel.model.resource.OtmActionRequest;
import org.opentravel.model.resource.OtmActionResponse;
import org.opentravel.schemacompiler.model.TLMimeType;

import java.util.Collections;
import java.util.List;

public class SetMimeTypesAction extends DexRunAction {
    private static Log log = LogFactory.getLog( SetMimeTypesAction.class );

    public static boolean isEnabled(OtmObject subject) {
        return (subject.isEditable() && subject instanceof OtmActionRequest || subject instanceof OtmActionResponse);
    }

    public static boolean isEnabled(OtmObject subject, OtmObject value) {
        return isEnabled( subject );
    }

    private OtmObject object = null;
    private MimeTypeMap oldMap = null;


    public SetMimeTypesAction() {
        // Constructor for reflection
    }

    /**
     * This action will get the data from the user via modal dialog
     */
    @Override
    public Object doIt(Object data) {
        if (data instanceof MimeTypeMap)
            return doIt( (MimeTypeMap) data );
        return null;
    }

    public Object doIt(MimeTypeMap data) {
        if (isEnabled( object )) {
            oldMap = new MimeTypeMap( object );
            set( data.getTLList() );
            return get();
        }
        return null;
    }

    @Override
    public List<TLMimeType> get() {
        if (object instanceof OtmActionRequest)
            return ((OtmActionRequest) object).getMimeTypes();
        if (object instanceof OtmActionResponse)
            return ((OtmActionResponse) object).getMimeTypes();
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

    public boolean set(List<TLMimeType> list) {
        if (object instanceof OtmActionRequest) {
            ((OtmActionRequest) object).setMimeTypes( list );
            return true;
        }
        if (object instanceof OtmActionResponse) {
            ((OtmActionResponse) object).setMimeTypes( list );
            return true;
        }
        return false;
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (subject instanceof OtmActionRequest || subject instanceof OtmActionResponse) {
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
        return "Set Mime Types to: " + get();
    }

    @Override
    public List<TLMimeType> undoIt() {
        log.debug( "Undo-ing mime type change." );
        if (oldMap != null) {
            if (object instanceof OtmActionRequest)
                ((OtmActionRequest) object).setMimeTypes( oldMap.getTLList() );
            if (object instanceof OtmActionResponse)
                ((OtmActionResponse) object).setMimeTypes( oldMap.getTLList() );
        }
        return get();
    }
}
