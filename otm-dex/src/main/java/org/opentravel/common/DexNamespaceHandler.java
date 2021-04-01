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

package org.opentravel.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;

import java.util.HashSet;
import java.util.Set;

/**
 * Create API for namespace related services.
 * <p>
 * The model manager is the backing store for all namespaces.
 * 
 * @author dmh
 *
 */
public class DexNamespaceHandler {
    private static Log log = LogFactory.getLog( DexNamespaceHandler.class );

    OtmModelManager modelMgr = null;

    public DexNamespaceHandler(OtmModelManager modelManager) {
        this.modelMgr = modelManager;

        if (modelMgr == null)
            throw new IllegalArgumentException( "Namespace handler must have model manager argument." );
    }

    /**
     * @return New HashSet of strings for both managed and unmanaged base namespaces from model manager.
     */
    public Set<String> getBaseNamespaces() {
        return modelMgr.getBaseNamespaces();
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

}
