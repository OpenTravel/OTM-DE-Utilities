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

import javafx.scene.control.RadioButton;

/**
 * Widget for filter buttons.
 * 
 * @author dmh
 *
 */
public class ButtonFilterWidget extends FilterWidget {
    // private static Logger log = LogManager.getLogger( LibraryFilterWidget.class );

    protected RadioButton button = null;

    public ButtonFilterWidget(MemberFilterController parent, RadioButton button) {
        super( parent );
        if (!(button instanceof RadioButton))
            throw new IllegalArgumentException( " filter widget must have access to button." );

        this.button = button;
        button.setOnAction( e -> set() );
    }

    @Override
    public void clear() {
        button.setSelected( false );
    }

    /**
     * {@inheritDoc}
     * <p>
     * True if member's library is in built in library.
     */
    @Override
    public boolean isSelected(OtmLibraryMember member) {
        if (!button.isSelected())
            return true;
        if (member == null || member.getLibrary() == null)
            return true;
        return selector.isSelected( member );
    }

    @Override
    public void set(boolean state) {
        button.setSelected( state );
    }

    private void set() {
        parentController.fireFilterChangeEvent();
    }
}
