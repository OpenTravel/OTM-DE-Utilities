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

/**
 * OTM Facade for TLLibraries managed in a repository and have a minor version number of 0 -- libraries that are
 * managed, major version libraries.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmMajorLibrary extends OtmManagedLibrary {
    private static Log log = LogFactory.getLog( OtmMajorLibrary.class );

    /**
     * Should only be called by Factory.
     * 
     * @see OtmLibraryFactory#newLibrary(AbstractLibrary, OtmModelManager)
     * 
     * @param tl
     * @param mgr
     */
    protected OtmMajorLibrary(TLLibrary tl, OtmModelManager mgr) {
        super( tl, mgr );
    }

    /**
     * Should only be called by Factory.
     * 
     * @see OtmLibraryFactory#newLibrary(OtmLocalLibrary)
     * 
     * @param unmanaged local library
     */
    public OtmMajorLibrary(OtmLocalLibrary unmanagedLib) {
        super( unmanagedLib.getTL(), unmanagedLib.getModelManager() );
        //
        // Now, get the data
        projectItems = unmanagedLib.getProjectItems();
        findings = null;
        providerMap = unmanagedLib.getProvidersMap();
        usersMap = unmanagedLib.getUsersMap();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DexActionManager getActionManager() {
        return getModelManager().getActionManager( isEditable() );
    }

    /**
     * {@inheritDoc}
     * <p>
     * A member of a major library returns:
     * <ul>
     * <li>If the major library is editable, full action manager.
     * <li>If the chain is editable AND member is latest in chain, minor action manager
     * <li>else read-only action manager
     * </ul>
     * <p>
     * Members in a major library are editable if the major library is editable. If a minor library has been created,
     * the major library will not be editable.
     */
    @Override
    public DexActionManager getActionManager(OtmLibraryMember member) {

        if (isEditable())
            return getModelManager().getActionManager( true );

        if (isChainEditable())
            return getModelManager().getMinorActionManager( getVersionChain().isLatestVersion( member ) );

        return getModelManager().getActionManager( false );

        // // Done 6/21/2021 - check logic, see junit: TestLibrary_Major
        // if (isChainEditable()) {
        // // if (isEditable() && getVersionChain().isNewToChain( member ))
        // // return getModelManager().getActionManager( true );
        // // return getModelManager().getMinorActionManager( getVersionChain().isLatestVersion( member ) );
        // }
        // // return getModelManager().getActionManager( false );
    }

    // @Override
    // public OtmVersionChainVersioned getVersionChain() {
    // return (OtmVersionChainVersioned) getModelManager().getVersionChain( this );
    // }
}
