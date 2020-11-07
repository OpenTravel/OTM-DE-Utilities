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

import org.opentravel.dex.controllers.DexFilterWidget;
import org.opentravel.dex.controllers.member.MemberFilterController;
import org.opentravel.dex.events.DexEvent;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;

/**
 * Widget for member object type filtering.
 * 
 * @author dmh
 *
 */
// Future - use fxControls or other package to get a multiple check box or even check tree to select versions.
public class ObjectTypeFilterWidget implements DexFilterWidget<OtmLibraryMember> {
    // private static Log log = LogFactory.getLog( LibraryFilterWidget.class );

    private static final String ALL = "All Objects";
    private MemberFilterController parentController;
    private ComboBox<String> typeSelector;
    private OtmLibraryMemberType objectType = null;

    public ObjectTypeFilterWidget(MemberFilterController parent, ComboBox<String> comboBox) {
        if (!(comboBox instanceof ComboBox))
            throw new IllegalArgumentException( "Filter widget must have access to combo box." );
        if (parent == null)
            throw new IllegalArgumentException( "Filter widget must have access filter controller." );

        this.parentController = parent;
        this.typeSelector = comboBox;

        update();

        typeSelector.getSelectionModel().select( 0 );
        typeSelector.setPromptText( "Object Type" );
        typeSelector.setOnAction( this::setFilter );

        // log.debug("Configured library selection combo control.");
    }

    private void setFilter(ActionEvent e) {
        if (typeSelector.getValue() != null) {
            String value = typeSelector.getValue();
            setTypeFilter( value );
        }
    }

    private void setTypeFilter(String value) {
        if (value == null || value.isEmpty() || value.equals( ALL ))
            set( null );
        else
            set( OtmLibraryMemberType.getClass( value ) );
    }

    /**
     * Set the type filter value. Expected to be the object type's class simple name.
     * <p>
     * (e.x. OtmLibraryMemberType.getClass( OtmLibraryMemberType.CHOICE ))
     * 
     * @param value member type to select, or null to disable filter
     */
    public void set(OtmLibraryMemberType value) {
        objectType = value;
        parentController.fireFilterChangeEvent();
        // log.debug("Set Type Filter: " + classNameFilter);
    }

    /**
     * Clear the selector's backing list and selection.
     */
    public void clear() {
        set( null );
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSelected(OtmLibraryMember member) {
        // Not active
        if (objectType == null)
            return true;
        if (member == null)
            return true;

        // Filter is active, return true if it matches
        return OtmLibraryMemberType.is( member, objectType );
        // return member.getLibrary().getName().startsWith( libraryFilter );
    }


    @Override
    public void selectionHandler(DexEvent event) {
        // No-Op
    }

    /**
     * Update selection's backing list. Leave selected library selected.
     */
    public void refresh() {
        String selection = typeSelector.getValue();
        update();
        setTypeFilter( selection );
        // log.debug( "Refreshed." );
    }

    /**
     * Clear the map the reload from model manager
     */
    private void update() {
        ObservableList<String> data = FXCollections.observableArrayList();
        data.add( ALL );
        for (OtmLibraryMemberType type : OtmLibraryMemberType.values()) {
            data.add( type.label() );
        }
        typeSelector.setItems( data );
    }

}
