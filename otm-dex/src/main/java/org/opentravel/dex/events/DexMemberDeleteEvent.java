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

import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import javafx.event.EventType;

/**
 * OTM DEX event for signaling when a model managed has a significant change to the model. This event signals that the
 * old model is invalid and users should reload from the manager.
 * 
 * @author dmh
 *
 */
public class DexMemberDeleteEvent extends DexChangeEvent {
    private static final long serialVersionUID = 20191119L;

    public static final EventType<DexMemberDeleteEvent> MEMBER_DELETED = new EventType<>( DEX_ALL, "MEMBER_DELETED" );
    private final transient OtmLibraryMember alternateMember;


    public OtmLibraryMember getDeletedMember() {
        return otmObject instanceof OtmLibraryMember ? (OtmLibraryMember) otmObject : null;
    }

    public OtmLibraryMember getAlternateMember() {
        return alternateMember;
    }

    /**
     * Member deleted event with no subject. Needed for DexActions to create event reflectively.
     */
    public DexMemberDeleteEvent() {
        super( MEMBER_DELETED );
        this.otmObject = null;
        this.alternateMember = null;
    }

    /**
     * Event to signal that a member has been deleted from the model.
     * 
     * @param deletedMember is the member that was deleted
     * @param alternateMember if not null, suggestion as to what member to present instead of deleted member
     */
    public DexMemberDeleteEvent(OtmLibraryMember deletedMember, OtmLibraryMember alternateMember) {
        super( MEMBER_DELETED );
        this.otmObject = deletedMember;
        this.alternateMember = alternateMember;
    }

}
