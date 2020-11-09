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

/**
 * Generic filter widget.
 * 
 * @author dmh
 *
 */
public class FilterWidget implements DexFilterWidget<OtmLibraryMember> {
    // private static Log log = LogFactory.getLog( LibraryFilterWidget.class );

    public interface Selector {
        public boolean isSelected(OtmLibraryMember m);
    }

    protected MemberFilterController parentController;
    private static final boolean INITIALSTATE = false;
    boolean active = INITIALSTATE;
    protected Selector selector;

    public FilterWidget(MemberFilterController parent) {
        if (parent == null)
            throw new IllegalArgumentException( "filter widget must have access filter controller." );
        this.parentController = parent;
    }

    @Override
    public void clear() {
        active = INITIALSTATE;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSelected(OtmLibraryMember member) {
        if (!active)
            return true;
        if (member == null)
            return true;
        return selector.isSelected( member );
    }

    /**
     * No-Op
     */
    @Override
    public void refresh() {
        // No-Op
    }

    /**
     * No-Op
     */
    @Override
    public void selectionHandler(DexEvent event) {
        // No-Op
    }

    public void set(boolean state) {
        active = state;
        parentController.fireFilterChangeEvent();
    }

    public FilterWidget setSelector(Selector s) {
        this.selector = s;
        return this;
    }

}
