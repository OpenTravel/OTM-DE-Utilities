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

package org.opentravel.dex.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.dex.controllers.member.MemberFilterController;

import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * OTM DEX event for signaling when a filter controller setting has changed.
 * 
 * @author dmh
 *
 */
public class DexFilterChangeEvent extends DexEvent {
    private static Logger log = LogManager.getLogger( DexFilterChangeEvent.class );
    private static final long serialVersionUID = 20190409L;

    public static final EventType<DexFilterChangeEvent> FILTER_CHANGED = new EventType<>( DEX_ALL, "FILTER_CHANGED" );

    private MemberFilterController filter = null;

    public MemberFilterController getFilter() {
        return filter;
    }

    /**
     * Filter change event with no subject.
     */
    public DexFilterChangeEvent() {
        super( FILTER_CHANGED );
    }

    /**
     * Filter change event with access to the filter controller.
     * 
     * @param source
     * @param target
     */
    public DexFilterChangeEvent(Object source, EventTarget target) {
        super( source, target, FILTER_CHANGED );
        if (source instanceof MemberFilterController)
            filter = (MemberFilterController) source;
    }

}
