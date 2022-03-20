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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * OTM Object for managed, minor version libraries.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmMinorLibrary extends OtmManagedLibrary {
    private static Logger log = LogManager.getLogger( OtmMinorLibrary.class );

    /**
     * Should only be called by Factory.
     * 
     * @see OtmLibraryFactory#newLibrary(AbstractLibrary, OtmModelManager)
     * 
     * @param tl
     * @param mgr
     */
    protected OtmMinorLibrary(TLLibrary tl, OtmModelManager mgr) {
        super( tl, mgr );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DexActionManager getActionManager() {
        return getModelManager().getMinorActionManager( isEditable() );
    }

    /**
     * {@inheritDoc}
     * <p>
     * Members of minor libraries return Read-only, Minor or Full action manager.
     * <ul>
     * <li>Full if the member is new to the chain and editable library
     * <li>else Minor if the member is the latest in the version chain.
     * <li>Read-only otherwise.
     * </ul>
     */
    @Override
    public DexActionManager getActionManager(OtmLibraryMember member) {
        if (isNewToChain( member ))
            return getModelManager().getActionManager( isEditable() );
        return getModelManager().getMinorActionManager( getVersionChain().isLatestVersion( member ) );
        //
        //// if (isEditable())
        //// return getModelManager().getMinorActionManager( getVersionChain().isNewToChain( member ) );
        //// return getModelManager().getActionManager( false );

        // // if (isMajorVersion() && isEditable())
        // // return getModelManager().getActionManager( true );
        // if (isChainEditable()) {
        // if (isEditable() && getVersionChain().isNewToChain( member ))
        // return getModelManager().getActionManager( true );
        // return getModelManager().getMinorActionManager( getVersionChain().isLatestVersion( member ) );
        // }
        // return getModelManager().getActionManager( false );
    }

    // @Override
    // public OtmVersionChainVersioned getVersionChain() {
    // return (OtmVersionChainVersioned) getModelManager().getVersionChain( this );
    // }
}
