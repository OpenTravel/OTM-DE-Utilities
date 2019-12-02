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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.member.MemberAndProvidersDAO;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;

import javafx.event.EventType;
import javafx.scene.control.TreeItem;

/**
 * OTM DEX event for signaling when a library member has been selected.
 * 
 * @author dmh
 *
 */
public class DexMemberSelectionEvent extends DexNavigationEvent {
    private static Log log = LogFactory.getLog( DexMemberSelectionEvent.class );
    private static final long serialVersionUID = 20190409L;

    public static final EventType<DexMemberSelectionEvent> MEMBER_SELECTED =
        new EventType<>( DEX_ALL, "MEMBER_SELECTED" );
    public static final EventType<DexMemberSelectionEvent> RESOURCE_SELECTED =
        new EventType<>( DEX_ALL, "RESOURCE_SELECTED" );
    // Member selected because it or some descendant is assigned as type provider
    public static final EventType<DexMemberSelectionEvent> TYPE_PROVIDER_SELECTED =
        new EventType<>( DEX_ALL, "TYPE_PROVIDER_SELECTED" );
    // Member selected because it or some descendant is type user
    public static final EventType<DexMemberSelectionEvent> TYPE_USER_SELECTED =
        new EventType<>( DEX_ALL, "TYPE_USER_SELECTED" );


    /**
     * Filter change event with no subject.
     */
    public DexMemberSelectionEvent() {
        super( MEMBER_SELECTED );
        member = null;
    }

    /**
     * A library member selection event.
     * 
     * @param source is the controller that created the event
     * @param target the tree item that was selected
     */
    public DexMemberSelectionEvent(Object source, TreeItem<MemberAndProvidersDAO> target) {
        super( source, target, MEMBER_SELECTED );
        // If there is data, extract it from target
        OtmObject m = null;
        if (target != null && target.getValue() != null && target.getValue().getValue() != null)
            m = target.getValue().getValue();
        if (m instanceof OtmContributedFacet)
            m = ((OtmContributedFacet) m).getContributor();
        if (m != null && !(m instanceof OtmLibraryMember))
            m = m.getOwningMember();
        member = (OtmLibraryMember) m;
    }

    /**
     * @param otm
     */
    public DexMemberSelectionEvent(OtmLibraryMember otm) {
        super( MEMBER_SELECTED );
        if (otm instanceof OtmContributedFacet)
            otm = ((OtmContributedFacet) otm).getContributor();
        member = otm;
    }

    /**
     * @param otm
     */
    public DexMemberSelectionEvent(OtmLibraryMember otm, EventType<DexMemberSelectionEvent> type) {
        super( type );
        if (otm instanceof OtmContributedFacet)
            otm = ((OtmContributedFacet) otm).getContributor();
        member = otm;
    }

    public DexMemberSelectionEvent(OtmResource otm) {
        super( RESOURCE_SELECTED );
        member = otm;
    }

}
