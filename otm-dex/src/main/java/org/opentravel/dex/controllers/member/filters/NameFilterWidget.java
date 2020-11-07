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

import javafx.scene.control.TextField;

/**
 * Widget for library selection.
 * 
 * @author dmh
 *
 */
public class NameFilterWidget implements DexFilterWidget<OtmLibraryMember> {
    // private static Log log = LogFactory.getLog( LibraryFilterWidget.class );

    private MemberFilterController parentController;
    private TextField nameSelector;

    public NameFilterWidget(MemberFilterController parent, TextField nameSelector) {
        if (!(nameSelector instanceof TextField))
            throw new IllegalArgumentException( "Filter widget must have access to text field." );
        if (parent == null)
            throw new IllegalArgumentException( "Filter widget must have access filter controller." );

        this.parentController = parent;
        this.nameSelector = nameSelector;

        nameSelector.textProperty().addListener( (v, o, n) -> applyTextFilter() );
        // log.debug("Configured library selection combo control.");
    }

    /**
     * Filter on any case of the text in the memberNameFilter
     */
    private void applyTextFilter() {
        parentController.fireFilterChangeEvent();
    }

    /**
     * Clear the selector's backing list and selection.
     */
    public void clear() {
        // textFilterValue = null;
        nameSelector.setText( "" );
    }

    /**
     * {@inheritDoc}
     * <p>
     * True if name.toLowerCase() contains selector text
     */
    public boolean isSelected(OtmLibraryMember member) {
        if (nameSelector == null || nameSelector.getText().isEmpty())
            return true; // Not activated

        if (member == null || member.getName() == null)
            return true;

        // Filter is active, return true if it matches
        return member.getName().toLowerCase().contains( nameSelector.getText() );
    }

    @Override
    public void selectionHandler(DexEvent event) {
        // No-op
    }

    /**
     * Update selection's backing list. Leave selected library selected.
     */
    public void refresh() {
        // No-Op
    }
}
