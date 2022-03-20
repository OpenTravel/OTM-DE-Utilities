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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmLocalLibrary;
import org.opentravel.model.otmContainers.OtmManagedLibrary;
import org.opentravel.model.otmContainers.OtmVersionChain;
import org.opentravel.model.otmContainers.OtmVersionChainEmpty;
import org.opentravel.model.otmContainers.OtmVersionChainVersioned;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Manage access to version chains.
 * <p>
 * Version chains relate major and minor libraries with the same base namespace.
 * <p>
 * junit: {@link TestOtmModelChainsManager}
 * 
 * @author dmh
 *
 */
public class OtmModelChainsManager {
    private static Logger log = LogManager.getLogger( OtmModelChainsManager.class );

    // Map of name+base namespace and their version chain
    // Map assures NO Duplicate Keys, allows null keys and values
    protected Map<String,OtmVersionChain> chainMap = null;


    /**
     */
    public OtmModelChainsManager() {
        chainMap = new TreeMap<>( Collections.reverseOrder() ); // Newest first, sorted map
    }

    /**
     * Add the library to new or existing version chain. Called when adding a library to the model manager maps.
     * 
     * @param mLib
     * @return
     */
    public OtmVersionChain add(OtmLibrary lib) {
        String chainName = lib.getChainName();
        OtmVersionChain otmVC = get( chainName );

        if (otmVC != null) {
            otmVC.add( lib ); // Add to existing chain
        } else {
            if (lib instanceof OtmManagedLibrary)
                otmVC = new OtmVersionChainVersioned( (OtmManagedLibrary) lib );
            else if (lib instanceof OtmLocalLibrary)
                otmVC = new OtmVersionChainEmpty( (OtmLocalLibrary) lib );
            else
                otmVC = null;
            chainMap.put( chainName, otmVC );
        }
        return get( lib );
    }

    protected void clear() {
        chainMap.clear();
    }

    /**
     * Get the version chain associated with the library. It must contain the library.
     */
    public OtmVersionChain get(OtmLibrary lib) {
        OtmVersionChain chain = get( getChainName( lib ) );
        if (chain != null && get( getChainName( lib ) ).contains( lib ))
            return chain;
        return null;
    }

    /**
     * Facade for getting chain name from library. {@linkplain OtmLibrary#getChainName()}
     * 
     * @param lib
     * @return
     */
    public String getChainName(OtmLibrary lib) {
        return lib.getChainName();
    }

    /**
     * Get the version chain associated with the chainName.
     */
    public OtmVersionChain get(String chainName) {
        return chainMap.get( chainName );
    }

    /**
     * Get the list of libraries from the chain.
     * 
     * @param chainName
     * @return unmodifiable, sorted list
     */
    public List<OtmLibrary> getChainLibraries(String chainName) {
        log.debug( "Getting chain libraries for: " + chainName );
        OtmVersionChain chain = chainMap.get( chainName );
        return chain != null ? chain.getLibraries() : Collections.emptyList();
    }

    /**
     * Remove chainName:chain entry if no other library has the chainName.
     * 
     * @param lib
     */
    public void remove(OtmLibrary lib) {
        String chainName = getChainName( lib );
        OtmVersionChain chain = get( lib );
        if (chain != null) {
            chain.remove( lib );
            if (chain.isEmpty())
                chainMap.remove( chainName );
        }
        // log.debug( "Removed " + chainName + " from chainMap." );
    }

    /**
     * Get the name of all chains. This will include empty chains containing local libraries.
     * 
     * @return unmodifiable set of map keys
     */
    public Set<String> getChainNames() {
        return Collections.unmodifiableSet( chainMap.keySet() );
    }

    /**
     * @return unmodifiable collection of OtmVersionChains
     */
    public Collection<OtmVersionChain> getChains() {
        return Collections.unmodifiableCollection( chainMap.values() );
    }

}

