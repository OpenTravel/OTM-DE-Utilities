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
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Dave Hollander
 * 
 */
public class OtmCoreValueFacet extends OtmAbstractDisplayFacet {
    private static Logger log = LogManager.getLogger( OtmCoreValueFacet.class );

    private OtmCore owner;

    public OtmCoreValueFacet(OtmCore owner) {
        super( owner.getSummary() ); // Give the super-type a property owner parent
        this.owner = owner;
    }

    @Override
    public String getName() {
        return "Simple";
    }

    public String getValueType() {
        String type = "";
        if (owner.getAssignedType() != null)
            type = owner.getAssignedType().getName();
        return type;
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        return Collections.emptyList();
    }

    /**
     * @see org.opentravel.model.OtmObject#isDeprecated()
     */
    @Override
    public boolean isDeprecated() {
        return false;
    }
}
