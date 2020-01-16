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

import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Dave Hollander
 * 
 */
public class OtmEmptyTableFacet extends OtmAbstractDisplayFacet {
    // private static Log log = LogFactory.getLog( OtmEmptyTableFacet.class );

    private OtmModelManager modelManager;
    private String label = "No Content in Table";

    public OtmEmptyTableFacet(OtmModelManager modelManager) {
        super( null );
        this.modelManager = modelManager;
    }

    /**
     * Create an empty, labeled facet
     * 
     * @param label
     * @param modelManager
     */
    public OtmEmptyTableFacet(String label, OtmModelManager modelManager) {
        super( null );
        this.modelManager = modelManager;
        this.label = label;
    }

    @Override
    public OtmModelManager getModelManager() {
        return modelManager;
    }

    @Override
    public String getName() {
        return label;
    }

    @Override
    public Icons getIconType() {
        // return ImageManager.Icons.NAMESPACEFACET;
        return null;
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        return Collections.emptyList();
    }

}
