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

package org.opentravel.model.otmFacets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.model.OtmPropertyOwner;

/**
 * @author Dave Hollander
 * 
 */
public class OtmVWAAttributeFacet extends OtmAbstractDisplayFacet {
    private static Logger log = LogManager.getLogger( OtmVWAAttributeFacet.class );

    public OtmVWAAttributeFacet(OtmPropertyOwner parent) {
        super( parent );
    }

    @Override
    public String getName() {
        return "Attributes";
    }
    // Get children and children hierarchy from base display facet
}
