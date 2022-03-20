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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * junit: {@link TestOtmModelNamespaceManager}
 * 
 * @author dmh
 *
 */
public class OtmModelNamespaceManager {
    private static Logger log = LogManager.getLogger( OtmModelNamespaceManager.class );

    // private Map<OtmLibrary,String> baseNSMap = new HashMap<>();
    private List<String> baseNSList = new ArrayList<>();
    OtmModelManager modelMgr = null;

    public static final String XSD_LIBRARY_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

    public static final String OTA_LIBRARY_NAMESPACE = "http://www.opentravel.org/OTM/Common/v0";

    public OtmModelNamespaceManager(OtmModelManager modelManager) {
        this.modelMgr = modelManager;

        if (modelMgr == null)
            throw new IllegalArgumentException( "Namespace handler must have model manager argument." );
    }

    /**
     * Add the base namespace for this library if not already added.
     * 
     * @param lib
     */
    protected void add(OtmLibrary lib) {
        if (!baseNSList.contains( lib.getBaseNS() )) {
            baseNSList.add( lib.getBaseNS() );
            // log.debug( "Added " + lib.getBaseNS() + " to list." );
        }
    }

    protected void clear() {
        baseNSList.clear();
    }

    /**
     * @return unmodifiable List of strings for both managed and unmanaged base namespaces from model manager.
     */
    public List<String> getBaseNamespaces() {
        return Collections.unmodifiableList( baseNSList );
    }

    /**
     * @return new set of all prefixes used by libraries in the model.
     */
    public Set<String> getPrefixes() {
        Set<String> prefixes = new HashSet<>();
        modelMgr.getLibraries().forEach( l -> prefixes.add( l.getPrefix() ) );
        return prefixes;
    }

    /**
     * If the namespace is in the model, return its prefix. Otherwise, return a unique prefix.
     * 
     * @param namespace
     * @return
     */
    public String getPrefix(String namespace) {
        String prefix = null;
        Set<String> prefixes = new HashSet<>();
        for (OtmLibrary l : modelMgr.getLibraries()) {
            if (l.getTL().getNamespace().equals( namespace ))
                prefix = l.getPrefix();
            prefixes.add( l.getPrefix() );
        }
        if (prefix == null) {
            // create a variant that is not used.
            prefix = "pf";
            int i = 0;
            do {
                i += 1;
            } while (prefixes.contains( prefix + i ));
            prefix = prefix + i;
        }

        return prefix;
    }

    /**
     * Assure namespace ends in version string (e.g. http://example.com/ns1/v1). Add the version part if missing.
     * 
     * @param ns
     * @return
     */
    public static String fixNamespaceVersion(String ns) {
        String suffix = ns;
        if (ns.lastIndexOf( '/' ) > 0)
            suffix = ns.substring( ns.lastIndexOf( '/' ) );
        if (!suffix.matches( "/v[0-9].*" ))
            ns += "/v1";

        // log.debug( "NS check: " + ns );
        return ns;
    }

    /**
     * Remove the base namespace IFF there are no other libraries with that baseNS
     * 
     * @param lib
     */
    public void remove(OtmLibrary lib) {
        String baseNS = lib.getBaseNS();
        for (OtmLibrary l : modelMgr.getUserLibraries())
            if (l != lib && l.getBaseNS().equals( baseNS ))
                return; // match found, no change made
        baseNSList.remove( baseNS );
    }

    /**
     * @param baseNS
     * @return new list
     */
    // TODO - should the version scheme be used to tring ns?
    // See the tests.
    // I hope not. This should be handled by tlLib.getBasenamespace()
    public List<OtmLibrary> getBaseNsLibraries(String baseNS) {
        List<OtmLibrary> list = new ArrayList<>();
        for (OtmLibrary lib : modelMgr.getLibraries())
            if (lib.getBaseNS().equals( baseNS ))
                list.add( lib );
        return list;
    }

}
