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

package org.opentravel.model.otmContainers;

import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmModelManager;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;

/**
 * OTM Object for libraries.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmBuiltInLibrary extends OtmLibrary {
    // private static Log log = LogFactory.getLog( OtmBuiltInLibrary.class );


    public OtmBuiltInLibrary(BuiltInLibrary lib, OtmModelManager mgr) {
        super( mgr );
        tlLib = lib;
    }

    @Override
    public AbstractLibrary getTL() {
        return tlLib;
    }

    @Override
    public String getName() {
        return getTL() != null ? getTL().getName() : "";
    }

    @Override
    public String getPrefix() {
        return getTL().getPrefix();
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.LIBRARY;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    /**
     * @return actual status of TL Libraries otherwise DRAFT
     */
    @Override
    public TLLibraryStatus getStatus() {
        return TLLibraryStatus.FINAL;
    }

    // @Override
    // public String getStateName() {
    // return "Built in library.";
    // }

    @Override
    public String getNameWithBasenamespace() {
        return getBaseNamespace() + "/" + getName();
    }

    @Override
    public String getLockedBy() {
        return "";
    }

    @Override
    public String getBaseNamespace() {
        return projectItems.isEmpty() ? "" : projectItems.get( 0 ).getBaseNamespace();
    }

    @Override
    public boolean isLatestVersion() {
        return mgr.isLatest( this );
    }

    // @Override
    // public void validate() {
    // findings = TLModelCompileValidator.validateModelElement(getTL(), true);
    // }

    /**
     * @return
     */
    @Override
    public String getVersion() {
        return getTL().getVersion();
    }

}
