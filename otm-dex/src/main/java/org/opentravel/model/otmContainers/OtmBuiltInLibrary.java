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

import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryItemState;

/**
 * OTM Facade for built-in libraries.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmBuiltInLibrary extends OtmLibrary {
    // private static Log log = LogFactory.getLog( OtmBuiltInLibrary.class );

    protected OtmBuiltInLibrary(BuiltInLibrary lib, OtmModelManager mgr) {
        super( lib, mgr );
    }

    @Override
    public AbstractLibrary getTL() {
        return tlLib;
    }

    /**
     * Built-in libraries return read-only action manger.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public DexActionManager getActionManager(OtmLibraryMember member) {
        return mgr.getActionManager( false );
    }

    /**
     * Built-in libraries return read-only action manger.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public DexActionManager getActionManager() {
        return mgr.getActionManager( false );
    }

    // @Override
    // public String getName() {
    // return getTL() != null ? getTL().getName() : "";
    // }
    //
    // @Override
    // public String getPrefix() {
    // return getTL().getPrefix();
    // }
    //
    // @Override
    // public Icons getIconType() {
    // return ImageManager.Icons.LIBRARY;
    // }

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
    // public String getNameWithBasenamespace() {
    // return getBaseNS() + "/" + getName();
    // }

    // @Override
    // public String getLockedBy() {
    // return "";
    // }

    /**
     * Built-in libraries are always unmanaged.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public RepositoryItemState getState() {
        return RepositoryItemState.UNMANAGED;
    }

    /**
     * Built-in libraries just return their namespace.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public String getBaseNS() {
        return getTL().getNamespace();
        // return projectItems.isEmpty() ? "" : projectItems.get( 0 ).getBaseNamespace();
    }

    @Override
    public boolean isLatestVersion() {
        return true;
        // return mgr.isLatest( this );
    }

    // /**
    // * @return
    // */
    // @Override
    // public String getVersion() {
    // return getTL().getVersion();
    // }

}
