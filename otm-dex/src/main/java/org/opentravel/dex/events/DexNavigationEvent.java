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

import org.opentravel.dex.controllers.member.MemberAndProvidersDAO;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import javafx.event.EventType;
import javafx.scene.control.TreeItem;

/**
 * DEX Navigation Event extends DexEvent and Abstract OTM event.
 * <p>
 * Navigation events occur when the user makes a selection that changes other controllers.
 * 
 * @author dmh
 *
 */
public abstract class DexNavigationEvent extends DexEvent {
    // private static Logger log = LogManager.getLogger( DexChangeEvent.class );
    private static final long serialVersionUID = 20190826L;

    protected OtmLibraryMember member = null;

    public OtmLibraryMember getMember() {
        return member;
    }

    /**
     * Filter change event with no subject.
     */
    public DexNavigationEvent() {
        super( DEX_ALL );
    }

    public DexNavigationEvent(Object source, TreeItem<MemberAndProvidersDAO> target,
        EventType<? extends DexEvent> eventType) {
        super( source, target, eventType );
    }

    /**
     * @param eventType
     */
    public DexNavigationEvent(EventType<? extends DexNavigationEvent> eventType) {
        super( eventType );
    }

    @Override
    public String toString() {
        return "Navigation Event: " + getMember();
    }
}
