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

import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmProperties.OtmEnumerationImpliedValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
public class OtmEnumerationOtherFacet extends OtmAbstractDisplayFacet {
    // private static Log log = LogFactory.getLog( OtmEnumerationOtherFacet.class );

    private static final String OTHER = "Extension";
    private Collection<OtmObject> kids = new ArrayList<>();

    public OtmEnumerationOtherFacet(OtmPropertyOwner parent) {
        super( parent );
        new OtmEnumerationImpliedValue( this );
    }

    @Override
    public OtmObject add(OtmObject child) {
        kids.add( child );
        return child;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The open facet has no children. Always returns empty list. Use {@link #getChildrenHierarchy()} to access other
     * value literal.
     */
    @Override
    public List<OtmObject> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        return kids;
    }

    @Override
    public String getName() {
        return OTHER;
    }

    @Override
    public boolean isExpanded() {
        return false;
    }


}
