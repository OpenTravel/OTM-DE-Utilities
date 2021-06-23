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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryItemState;

/**
 * OTM Facade for TLLibraries that are not in repository--they are un-managed, local libraries.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmLocalLibrary extends OtmLibrary {
    private static Log log = LogFactory.getLog( OtmLocalLibrary.class );

    /**
     * Should only be called by Factory.
     * 
     * @see OtmLibraryFactory#newLibrary(AbstractLibrary, OtmModelManager)
     * 
     * @param tl
     * @param mgr
     */
    protected OtmLocalLibrary(TLLibrary tl, OtmModelManager mgr) {
        super( tl, mgr );
    }

    /**
     * {@inheritDoc}
     * <p>
     * Local libraries always return full action manager.
     * 
     */
    @Override
    public DexActionManager getActionManager() {
        return getModelManager().getActionManager( isEditable() );
    }

    /**
     * {@inheritDoc}
     * <p>
     * Members of local libraries always return full action manager.
     * 
     */
    @Override
    public DexActionManager getActionManager(OtmLibraryMember member) {
        return getModelManager().getActionManager( isEditable() );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBaseNS() {
        return getTL().getBaseNamespace();
    }

    /**
     * Local libraries are always UNMANAGED.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public RepositoryItemState getState() {
        return RepositoryItemState.UNMANAGED;
    }

    /**
     * Local libraries always have DRAFT status.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public TLLibraryStatus getStatus() {
        return TLLibraryStatus.DRAFT;
    }

    /**
     * Local libraries are always TLLibraries.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public TLLibrary getTL() {
        return (TLLibrary) tlLib;
    }

    /**
     * {@inheritDoc}
     * <p>
     * A local library is always editable.
     */
    @Override
    public boolean isEditable() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * A local library is always the latest version.
     */
    @Override
    public boolean isLatestVersion() {
        return true;
    }

    /**
     * @deprecated - use instanceof OtmLocalLibrary
     * @return true if the state equals RepositoryItemStage.UNMANAGED
     */
    @Override
    public boolean isUnmanaged() {
        return true;
        // return getState() == RepositoryItemState.UNMANAGED;
    }
}
