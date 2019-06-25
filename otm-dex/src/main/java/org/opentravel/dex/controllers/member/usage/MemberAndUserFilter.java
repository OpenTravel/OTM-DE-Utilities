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

package org.opentravel.dex.controllers.member.usage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.DexFilter;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmFacets.OtmContributedFacet;

/**
 * Filter that rejects empty children owners, non-children owners that are not type users, and aliases.
 * 
 * @author dmh
 *
 */
public class MemberAndUserFilter implements DexFilter<OtmObject> {
    private static Log log = LogFactory.getLog( MemberAndUserFilter.class );

    @Override
    public boolean isSelected(OtmObject obj) {
        // log.debug("Is " + obj + " selected?");
        if (obj instanceof OtmContributedFacet && ((OtmContributedFacet) obj).getContributor() != null)
            obj = ((OtmContributedFacet) obj).getContributor();

        if (obj instanceof OtmChildrenOwner) {
            if (((OtmChildrenOwner) obj).getChildren().isEmpty())
                return false;
        } else {
            if (!(obj instanceof OtmTypeUser))
                return false;
        }
        if (obj instanceof OtmAlias)
            return false;
        return true;
    }
}
