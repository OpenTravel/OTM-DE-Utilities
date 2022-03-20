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
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmContainers.OtmVersionChain;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

/**
 * In minor version chain (a.k.a. is later version) filter widget.
 * 
 * @author dmh
 *
 */
public class MinorVersionFilterWidget extends FilterWidget {
    // private static Logger log = LogManager.getLogger( LibraryFilterWidget.class );

    private OtmTypeProvider provider = null;
    private OtmVersionChain chain = null;

    /**
     * In minor version chain (a.k.a. is later version) filter widget.
     */
    public MinorVersionFilterWidget(MemberFilterController parent) {
        super( parent );
    }

    @Override
    public void clear() {
        provider = null;
    }

    @Override
    public boolean isSelected(OtmLibraryMember member) {
        if (provider == null || chain == null)
            return true;
        if (member == null)
            return true;
        return chain.isLaterVersion( provider, member );
    }

    public void set(OtmTypeProvider provider) {
        this.provider = provider;
        if (provider != null)
            this.chain = provider.getLibrary().getVersionChain();
        parentController.fireFilterChangeEvent();
    }
}
