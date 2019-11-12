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

import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;

import java.util.Collection;
import java.util.Collections;

/**
 * Used to post namespaces as facet in trees and tables
 * 
 * @author Dave Hollander
 * 
 */
public class OtmNamespaceFacet extends OtmAbstractDisplayFacet {
    // private static Log log = LogFactory.getLog( OtmEmptyTableFacet.class );

    private OtmModelManager modelManager;
    private OtmObject object;

    public OtmNamespaceFacet(OtmObject object) {
        super( null );
        this.modelManager = object.getModelManager();
        this.object = object;
    }

    @Override
    public OtmModelManager getModelManager() {
        return modelManager;
    }

    @Override
    public String getName() {
        return object.getNamespace();
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        return Collections.emptyList();
    }

}
