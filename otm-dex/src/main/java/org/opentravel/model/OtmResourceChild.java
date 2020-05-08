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

package org.opentravel.model;

import org.opentravel.common.DexEditField;
import org.opentravel.model.otmLibraryMembers.OtmResource;

import java.util.List;

/**
 * All children of resources (Actions, ActionFacets...) must implement this interface.
 * 
 * @author dmh
 *
 */
public interface OtmResourceChild extends OtmObject {

    /**
     * Get the Owning Resource library member.
     * 
     * @see org.opentravel.model.OtmObject#getOwningMember()
     */
    @Override
    public OtmResource getOwningMember();

    /**
     * @return list of edit fields containing FX Nodes ready for posting
     */
    public abstract List<DexEditField> getFields();

    /**
     * 
     * @return resource or resourceChild parent
     */
    public OtmObject getParent();

}
