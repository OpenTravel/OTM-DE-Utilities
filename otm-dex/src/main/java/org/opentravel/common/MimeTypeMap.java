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

package org.opentravel.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.resource.OtmActionRequest;
import org.opentravel.model.resource.OtmActionResponse;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLMimeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Manage a map between mime types and their values to and from lists.
 * 
 * @author dmh
 *
 */
public class MimeTypeMap {
    private static Logger log = LogManager.getLogger( MimeTypeMap.class );

    private SortedMap<String,Boolean> values = new TreeMap<>();

    public MimeTypeMap(OtmObject object, String defaultValues) {

        List<TLMimeType> types = null;
        if (object instanceof OtmActionRequest)
            types = ((OtmActionRequest) object).getTL().getMimeTypes();
        else if (object instanceof OtmActionResponse)
            types = ((OtmActionResponse) object).getTL().getMimeTypes();
        else
            // Default to the types available for requests
            types = new TLActionRequest().getMimeTypes();

        if (types != null)
            for (TLMimeType t : TLMimeType.values())
                values.put( t.toString(), types.contains( t ) );

        if (object instanceof OtmResource && defaultValues != null)
            // Use default from resource and user settings
            fromString( defaultValues );
    }

    public MimeTypeMap(OtmObject object) {
        this( object, null );
    }


    public SortedMap<String,Boolean> get() {
        return values;
    }

    public void set(SortedMap<String,Boolean> values) {
        this.values = values;
    }

    public List<TLMimeType> getTLList() {
        List<TLMimeType> types = new ArrayList<>();
        values.forEach( (key, value) -> {
            if (value && !key.isEmpty())
                types.add( TLMimeType.valueOf( key ) );
        } );
        return types;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        for (Entry<String,Boolean> e : values.entrySet())
            if (e.getValue())
                string.append( e.getKey() + ";" );
        return string.toString();
    }

    public void fromString(String string) {
        String[] tokens = string.split( ";" );
        for (String token : tokens) {
            values.put( token, true );
        }
    }

    protected void print() {
        for (Entry<String,Boolean> e : values.entrySet())
            log.debug( e.getKey() + " = " + e.getValue() );
    }
}
