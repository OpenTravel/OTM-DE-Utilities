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

import org.opentravel.model.OtmResourceChild;

import javafx.event.EventType;

/**
 * OTM DEX event for signaling when a child of a resource has been selected.
 * 
 * @author dmh
 *
 */
public class DexResourceChildSelectionEvent extends DexEvent {
    // private static Logger log = LogManager.getLogger( DexResourceChildSelectionEvent.class );
    private static final long serialVersionUID = 20190409L;

    public static final EventType<DexResourceChildSelectionEvent> RESOURCE_CHILD_SELECTED =
        new EventType<>( DEX_ALL, "RESOURCE_CHILD_SELECTED" );

    private final OtmResourceChild resourceChild;

    public OtmResourceChild get() {
        return resourceChild;
    }

    /**
     * Filter change event with no subject.
     */
    public DexResourceChildSelectionEvent() {
        super( RESOURCE_CHILD_SELECTED );
        resourceChild = null;
    }

    // /**
    // * A library member selection event.
    // *
    // * @param source is the controller that created the event
    // * @param target the tree item that was selected
    // */
    // public DexResourceChildSelectionEvent(Object source, TreeItem<MemberAndProvidersDAO> target) {
    // super( source, target, RESOURCE_CHILD_SELECTED );
    // // If there is data, extract it from target
    // OtmObject m = null;
    // if (target != null && target.getValue() != null && target.getValue().getValue() != null)
    // m = target.getValue().getValue();
    // if (m instanceof OtmContributedFacet)
    // m = ((OtmContributedFacet) m).getContributor();
    // if (m != null && !(m instanceof OtmLibraryMember))
    // m = m.getOwningMember();
    // resourceChild = (OtmLibraryMember) m;
    // }

    /**
     * @param otm
     */
    public DexResourceChildSelectionEvent(OtmResourceChild otm) {
        super( RESOURCE_CHILD_SELECTED );
        resourceChild = otm;
    }

}
