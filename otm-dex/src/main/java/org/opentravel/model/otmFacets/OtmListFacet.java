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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.model.otmLibraryMembers.OtmComplexObjects;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * @author Dave Hollander
 * 
 */
public class OtmListFacet extends OtmAbstractFacet<TLListFacet> {
    private static Log log = LogFactory.getLog( OtmListFacet.class );

    /**
     */
    public OtmListFacet(TLListFacet tl, OtmComplexObjects<?> parent) {
        super( tl, parent );
    }

    /**
     * @see org.opentravel.model.OtmPropertyOwner#add(org.opentravel.schemacompiler.model.TLModelElement)
     */
    @Override
    public OtmProperty<?> add(TLModelElement newTL) {
        // NO-OP
        return null;
    }

    /**
     * @see org.opentravel.model.OtmChildrenOwner#modelChildren()
     */
    @Override
    public void modelChildren() {
        // NO-OP
    }

    /**
     * @see org.opentravel.model.OtmChildrenOwner#modelInheritedChildren()
     */
    @Override
    public void modelInheritedChildren() {
        // NO-OP
    }

}
