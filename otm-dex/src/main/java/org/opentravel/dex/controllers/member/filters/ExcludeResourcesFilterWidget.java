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
import org.opentravel.model.otmLibraryMembers.OtmResource;

/**
 * Exclude resources filter widget.
 * 
 * @author dmh
 *
 */
public class ExcludeResourcesFilterWidget extends FilterWidget {
    // private static Logger log = LogManager.getLogger( LibraryFilterWidget.class );

    /**
     * Exclude resources filter widget.
     */
    public ExcludeResourcesFilterWidget(MemberFilterController parent) {
        super( parent );
    }

    @Override
    public void clear() {
        // No-op
    }

    @Override
    public boolean isSelected(OtmLibraryMember member) {
        return !(member instanceof OtmResource);
    }

    // public void set(OtmTypeProvider provider) {
    // parentController.fireFilterChangeEvent();
    // }
}
