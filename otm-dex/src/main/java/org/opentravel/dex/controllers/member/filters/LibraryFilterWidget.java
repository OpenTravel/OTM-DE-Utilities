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

package org.opentravel.dex.controllers.member.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.member.MemberFilterController;
import org.opentravel.dex.events.DexEvent;
import org.opentravel.dex.events.DexLibrarySelectionEvent;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import java.util.TreeMap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tooltip;

/**
 * Widget for library selection filter based on member name.
 * 
 * @author dmh
 *
 */
public class LibraryFilterWidget extends FilterWidget {
    private static Log log = LogFactory.getLog( LibraryFilterWidget.class );

    private static final String ALLLIBS = "All Libraries";
    private static final String SELECTEDLIB = "Selected Library";
    private static final String TOOLTIP = "Filter to only show members from libraries that start with selected name.";

    private ChoiceBox<String> librarySelector;
    private OtmModelManager modelMgr;
    private TreeMap<String,OtmLibrary> libraryMap = new TreeMap<>();
    private String libraryFilter = null;
    private OtmLibrary eventLibrary = null;

    public LibraryFilterWidget(MemberFilterController parent, ChoiceBox<String> librarySelector) {
        super( parent );
        if (!(librarySelector instanceof ChoiceBox))
            throw new IllegalArgumentException( "Library filter widget must have access to choice box." );

        this.modelMgr = parent.getModelManager();
        this.librarySelector = librarySelector;
        if (modelMgr == null)
            throw new IllegalArgumentException( "Library filter widget must have access to model manager." );

        updateMap();

        librarySelector.getSelectionModel().select( 0 );
        librarySelector.setOnAction( e -> setLibraryFilter() );
        librarySelector.setTooltip( new Tooltip( TOOLTIP ) );
        // log.debug("Configured library selection combo control.");
    }

    /**
     * Simply add the library to the map. Map entries are name string and library OTM facade.
     * 
     * @param lib
     * @return library or null
     */
    private OtmLibrary add(OtmLibrary lib) {
        if (lib == null || lib.getName() == null || lib.getName().isEmpty())
            return null;
        // Tree map assures no duplicates
        libraryMap.put( lib.getName(), lib );
        return lib;
    }

    /**
     * Clear the selector's backing list and selection.
     */
    @Override
    public void clear() {
        libraryMap.clear();
        libraryFilter = null;
    }

    /**
     * Get the library or null. ALLLIBS selection will also return null.
     * 
     * @return
     */
    private OtmLibrary getSelectedLibrary() {
        String key = librarySelector.getSelectionModel().getSelectedItem();
        return key != null ? libraryMap.get( key ) : null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * True if ALL LIBS is selected or member's library starts with the selected value
     */
    @Override
    public boolean isSelected(OtmLibraryMember member) {
        if (libraryFilter == null)
            return true; // Not activated

        if (member == null || member.getLibrary() == null || member.getLibrary().getName() == null)
            return true;

        // Specific library mode
        if (eventLibrary != null)
            return member.getLibrary() == eventLibrary;

        // Matching library name mode
        // Filter is active, return true if it matches
        return member.getLibrary().getName().startsWith( libraryFilter );
    }


    @Override
    public void selectionHandler(DexEvent event) {
        if (event instanceof DexLibrarySelectionEvent)
            selectionHandler( (DexLibrarySelectionEvent) event );
    }

    /**
     * Respond to a library selection event from the application by setting the filter.
     * 
     * @param event
     */
    public void selectionHandler(DexLibrarySelectionEvent event) {
        if (event != null && event.getLibrary() != null) {
            librarySelector.getSelectionModel().select( SELECTEDLIB );
            if (!event.getLibrary().getName().equals( libraryFilter )) {
                libraryFilter = event.getLibrary().getName();
                librarySelector.getSelectionModel().select( SELECTEDLIB );
                // librarySelector.getSelectionModel().select( event.getLibrary().getName() );
            }

            // Enable specific library mode
            // setLibraryFilter() is called when selector is set
            eventLibrary = event.getLibrary();
            updateMap();
            parentController.fireFilterChangeEvent();
        }
    }

    /**
     * Update selection's backing list. Leave selected library selected.
     */
    @Override
    public void refresh() {
        OtmLibrary lib = getSelectedLibrary();
        updateMap();
        setLibraryFilter( lib );
        // log.debug( "Refreshed." );
    }


    // Future - use fxControls or other package to get a multiple check box or even check tree to select versions.
    //
    /**
     * Run when a GUI control changes the selected library.
     */
    private void setLibraryFilter() {
        String selection = ALLLIBS;
        if (librarySelector.getSelectionModel().getSelectedItem() != null) {
            selection = librarySelector.getSelectionModel().getSelectedItem();
            if (selection.equals( ALLLIBS ))
                setLibraryFilter( null );
            else
                setLibraryFilter( libraryMap.get( selection ) );
        }
        librarySelector.setValue( selection );
        // log.debug( "Set filter to " + libraryFilter );
    }


    /**
     * If the passed library is not already selected, set the library selector combo to passed library and fire event.
     * 
     * @param lib
     */
    private void setLibraryFilter(OtmLibrary lib) {
        eventLibrary = null; // Go back to namespace mode
        if (lib == null) {
            librarySelector.getSelectionModel().select( 0 );
            libraryFilter = null;
            // parentController.fireFilterChangeEvent();
        } else if (!lib.getName().equals( libraryFilter )) {
            librarySelector.getSelectionModel().select( lib.getName() );
            libraryFilter = lib.getName();
            // parentController.fireFilterChangeEvent();
        }
        updateMap();
        parentController.fireFilterChangeEvent();
        // log.debug( "Set Library filter to: " + libraryFilter );
    }

    /**
     * Clear the map the reload from model manager
     */
    private void updateMap() {
        libraryMap.clear();
        for (OtmLibrary lib : modelMgr.getLibraries())
            add( lib );
        ObservableList<String> libList = FXCollections.observableArrayList( libraryMap.keySet() );
        libList.add( 0, ALLLIBS );
        if (eventLibrary != null)
            libList.add( 1, SELECTEDLIB );
        else if (libList.get( 1 ).equals( SELECTEDLIB ))
            libList.remove( 1 );

        librarySelector.setItems( libList );

        if (eventLibrary != null) {
            OtmLibrary savedLibrary = eventLibrary;
            librarySelector.getSelectionModel().select( 1 ); // clears event library
            eventLibrary = savedLibrary;
            libraryFilter = savedLibrary.getName();
        }
        // log.debug( "Updated library selection map. It has " + libraryMap.size() + " entries." );
    }

}
