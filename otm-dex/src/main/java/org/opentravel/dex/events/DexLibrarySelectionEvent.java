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
import org.opentravel.model.otmContainers.OtmLibrary;

import javafx.event.EventType;

/**
 * OTM DEX event for signaling when a library member has been selected.
 * 
 * @author dmh
 *
 */
public class DexLibrarySelectionEvent extends DexEvent {
    private static Log log = LogFactory.getLog( DexLibrarySelectionEvent.class );
    private static final long serialVersionUID = 20200409L;

    public static final EventType<DexLibrarySelectionEvent> LIBRARY_SELECTED =
        new EventType<>( DEX_ALL, "LIBRARY_SELECTED" );

    private final transient OtmLibrary library;

    public OtmLibrary getLibrary() {
        return library;
    }

    /**
     * Filter change event with no subject.
     */
    public DexLibrarySelectionEvent() {
        super( LIBRARY_SELECTED );
        library = null;
    }

    // /**
    // * A library member selection event.
    // *
    // * @param source is the controller that created the event
    // * @param target the tree item that was selected
    // */
    // public DexLibrarySelectionEvent(Object source, TreeItem<LibraryDAO> target) {
    // super( source, target, LIBRARY_SELECTED );
    // // log.debug("DexEvent source/target constructor ran.");
    //
    // // If there is data, extract it from target
    // if (target != null && target.getValue() != null && target.getValue().getValue() != null
    // && target.getValue().getValue() instanceof OtmLibrary)
    // library = target.getValue().getValue();
    // else
    // library = null;
    // }

    /**
     * @param otmLibrary
     */
    public DexLibrarySelectionEvent(OtmLibrary target) {
        super( LIBRARY_SELECTED );
        // log.debug("DexEvent target constructor ran.");
        library = target;
    }

}
