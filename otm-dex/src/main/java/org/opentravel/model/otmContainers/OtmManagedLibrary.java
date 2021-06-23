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
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;

/**
 * OTM Library facade for versioned, repository managed TL libraries.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmManagedLibrary extends OtmLibrary {
    private static Log log = LogFactory.getLog( OtmManagedLibrary.class );

    /**
     * Should only be called by Factory.
     * <p>
     * See {@link OtmLibraryFactory#newLibrary(AbstractLibrary, OtmModelManager)}
     * 
     * @param tl
     * @param mgr
     */
    protected OtmManagedLibrary(TLLibrary tl, OtmModelManager mgr) {
        super( tl, mgr );
    }

    /**
     * Managed library can be locked if:
     * <ul>
     * <li>{@linkplain #getStatus()} is DRAFT library, and
     * <li>{@linkplain #getState()} is MANAGED_UNLOCKED, and
     * <li>Any project this library is in has permission to WRITE {@linkplain OtmProject#getPermission()}
     * </ul>
     * 
     * @return true if draft library is unlocked and user has permission to write in repository.
     */
    @Override
    public boolean canBeLocked() {
        if (getStatus() == TLLibraryStatus.DRAFT && getState() == RepositoryItemState.MANAGED_UNLOCKED) {
            // See if any of this library's projects has WRITE permission
            for (OtmProject p : getProjects())
                if (p.getPermission().equals( RepositoryPermission.WRITE ))
                    return true;
        }
        return false;
    }

    /**
     * Managed library can be unlocked if {@linkplain #getState()} is MANAGED_LOCKED or MANAGED_WIP
     * <p>
     * Note: does not check to see if this is the user that locked it.
     * 
     * @return true if is locked
     */
    @Override
    public boolean canBeUnlocked() {
        return getState() == RepositoryItemState.MANAGED_LOCKED || getState() == RepositoryItemState.MANAGED_WIP;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getBaseNS() {
        return getTL().getBaseNamespace();
    }

    /**
     * @return the user or empty string if not locked
     * @see org.opentravel.model.otmContainers.OtmLibrary#getLockedBy()
     */
    @Override
    public String getLockedBy() {
        for (ProjectItem pi : projectItems)
            if (pi.getLockedByUser() != null)
                return pi.getLockedByUser();
        return "";
    }

    /**
     * Managed libraries are always TLLibraries.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public TLLibrary getTL() {
        return (TLLibrary) tlLib;
    }

    /**
     * Examine all project items and return the state that grants the user the most rights.
     * <p>
     * {@inheritDoc}
     * 
     * @return MANAGED_UNLOCKED, MANAGED_UNLOCKED, or MANAGED_WIP
     */
    @Override
    public RepositoryItemState getState() {
        RepositoryItemState state = RepositoryItemState.MANAGED_UNLOCKED; // the weakest state
        if (projectItems == null || projectItems.isEmpty()) {
            log.warn( "Managed library " + this + " is missing project items." );
            return state;
        }

        for (ProjectItem pi : projectItems) {
            // log.debug("state = " + pi.getState());
            switch (pi.getState()) {
                case MANAGED_WIP:
                    return pi.getState(); // This gives user most rights, if found, return it.
                case MANAGED_LOCKED:
                    state = pi.getState();
                    break;
                case MANAGED_UNLOCKED:
                    break;
                default:
                    // No-Op
                    // error: case BUILT_IN:
                    // error: case UNMANAGED:
            }

        }

        return state;
    }

    /**
     * @return actual status of TL Library
     */
    public TLLibraryStatus getStatus() {
        return getTL().getStatus();
    }

    @Override
    public OtmVersionChainVersioned getVersionChain() {
        return (OtmVersionChainVersioned) getModelManager().getVersionChain( this );
    }

    /**
     * A managed library is editable if:
     * <ul>
     * <li>{@linkplain #getStatus()} returns DRAFT and
     * <li>{@linkplain #getState()} returns either MANAGED_WIP or MANAGED_LOCKED.
     * </ul>
     * {@inheritDoc}
     */
    @Override
    public boolean isEditable() {
        if (getStatus() != TLLibraryStatus.DRAFT)
            return false;
        return getState() == RepositoryItemState.MANAGED_WIP || getState() == RepositoryItemState.MANAGED_LOCKED;
    }

    /**
     * Ask the model manager if this is the latest version of the library
     * {@link OtmModelManager#isLatest(OtmManagedLibrary)}
     * 
     * @return
     */
    @Override
    public boolean isLatestVersion() {
        return mgr.isLatest( this );
    }

    /**
     * Facade for {@link OtmVersionChain#isNewToChain(OtmLibraryMember)}
     * 
     * @param member
     * @return
     */
    public boolean isNewToChain(OtmLibraryMember member) {
        return getVersionChain().isNewToChain( member );
    }

}
