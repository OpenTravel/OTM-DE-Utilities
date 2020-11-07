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
 * Widget for filter buttons. This one is always active.
 * 
 * @author dmh
 *
 */
public class ButtonToggleFilterWidget extends ButtonFilterWidget {
    // private static Log log = LogFactory.getLog( LibraryFilterWidget.class );

    // public interface Selector {
    // public boolean isSelected(OtmLibraryMember m);
    // }
    //
    // private MemberFilterController parentController;
    // private RadioButton button = null;
    // private Selector selector;

    public ButtonToggleFilterWidget(MemberFilterController parent, RadioButton button) {
        super( parent, button );
        // if (!(button instanceof RadioButton))
        // throw new IllegalArgumentException( " filter widget must have access to button." );
        // if (parent == null)
        // throw new IllegalArgumentException( "filter widget must have access filter controller." );
        //
        // this.button = button;
        // button.setOnAction( e -> set() );
        // this.parentController = parent;
    }

    // @Override
    // public void clear() {
    // button.setSelected( false );
    // }

    /**
     * {@inheritDoc}
     * <p>
     * True if member's library is in built in library.
     */
    @Override
    public boolean isSelected(OtmLibraryMember member) {
        if (member == null || member.getLibrary() == null)
            return true;
        if (!button.isSelected())
            return !selector.isSelected( member );
        return button.isSelected();
    }

    // /**
    // * No-Op
    // */
    // @Override
    // public void refresh() {
    // // No-Op
    // }
    //
    // /**
    // * No-Op
    // */
    // @Override
    // public void selectionHandler(DexEvent event) {
    // // No-Op
    // }
    //
    // public void set(boolean state) {
    // button.setSelected( state );
    // }
    //
    // private void set() {
    // parentController.fireFilterChangeEvent();
    // }
    //
    // public ButtonToggleFilterWidget setSelector(Selector s) {
    // this.selector = s;
    // return this;
    // }

}
