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
import org.opentravel.model.resource.OtmActionResponse;
import org.opentravel.model.resource.RestStatusCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Manage a map between status codes and their values to and from lists.
 * 
 * @author dmh
 *
 */
public class RestStatusCodesMap {
    private static Logger log = LogManager.getLogger( RestStatusCodesMap.class );

    private SortedMap<RestStatusCodes,Boolean> values = new TreeMap<>();

    /**
     * Create a map of all integer status codes with boolean on/off state
     * 
     * @param object
     */
    public RestStatusCodesMap(OtmObject object) {
        List<Integer> codes = ((OtmActionResponse) object).getTL().getStatusCodes();
        if (object instanceof OtmActionResponse) {
            for (RestStatusCodes t : RestStatusCodes.values()) {
                values.put( t, codes.contains( t.value() ) );
            }
        }
    }

    public SortedMap<RestStatusCodes,Boolean> get() {
        return values;
    }

    /**
     * Assign the value to the status code associated with the label.
     * 
     * @param codeLabel
     * @param value
     */
    public RestStatusCodes set(String codeLabel, boolean value) {
        RestStatusCodes code = RestStatusCodes.valueOf( codeLabel );
        values.put( code, value );
        return code;
    }

    public void set(SortedMap<RestStatusCodes,Boolean> values) {
        this.values = values;
    }

    public List<Integer> getTLList() {
        List<Integer> codes = new ArrayList<>();
        values.forEach( (key, value) -> {
            if (value)
                codes.add( key.value() );
        } );
        return codes;
    }

    protected void print() {
        for (Entry<RestStatusCodes,Boolean> e : values.entrySet())
            log.debug( RestStatusCodes.getLabel( e.getKey().value() ) + " = " + e.getValue() );
    }
}
