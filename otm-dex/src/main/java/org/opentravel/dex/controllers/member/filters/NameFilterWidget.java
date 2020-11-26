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

import org.opentravel.dex.controllers.member.MemberFilterController;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import javafx.scene.control.TextField;

/**
 * Widget for library selection.
 * 
 * @author dmh
 *
 */
public class NameFilterWidget extends FilterWidget {
    // private static Log log = LogFactory.getLog( LibraryFilterWidget.class );

    private TextField nameSelector;

    public NameFilterWidget(MemberFilterController parent, TextField nameSelector) {
        super( parent );
        if (!(nameSelector instanceof TextField))
            throw new IllegalArgumentException( "Filter widget must have access to text field." );

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
    @Override
    public void clear() {
        nameSelector.setText( "" );
    }

    @Override
    public void refresh() {
        nameSelector.setText( "" );
    }

    /**
     * {@inheritDoc}
     * <p>
     * True if name.toLowerCase() contains selector text
     */
    @Override
    public boolean isSelected(OtmLibraryMember member) {
        if (nameSelector == null || nameSelector.getText().isEmpty())
            return true; // Not activated

        if (member == null || member.getName() == null)
            return true;

        // Filter is active, return true if it matches
        return member.getName().toLowerCase().contains( nameSelector.getText() );
    }
}
