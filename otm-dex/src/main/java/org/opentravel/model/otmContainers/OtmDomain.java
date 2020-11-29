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
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmModelManager;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;

/**
 * OTM Object Node for business objects. Project does NOT extend model element
 * 
 * @author Dave Hollander
 * 
 */
public class OtmDomain {
    private static Log log = LogFactory.getLog( OtmDomain.class );

    /**
     * Utility to create domain name from a base namespace. Name is the portion after the last slash.
     * 
     * @param baseNamespace
     * @return
     */
    public static String getDomainName(String baseNamespace) {
        String name = baseNamespace;
        if (baseNamespace != null) {
            int lastSlash = baseNamespace.lastIndexOf( '/' );
            if (lastSlash >= baseNamespace.length())
                name = "";
            else if (lastSlash > 0)
                name = baseNamespace.substring( lastSlash + 1 );
        }
        return name;
    }

    public static String getDomainPath(String baseNamespace) {
        String name = "";
        if (baseNamespace != null) {
            int lastSlash = baseNamespace.lastIndexOf( '/' );
            if (lastSlash > 0)
                name = baseNamespace.substring( 0, lastSlash );
        }
        return name;
    }

    public static String getDomainSubPath(String fullName, String baseNamespace) {
        String remainder = "";
        if (baseNamespace != null && baseNamespace.length() < fullName.length()) {
            if (fullName.startsWith( baseNamespace )) {
                // Strip off the domain name
                remainder = fullName.substring( baseNamespace.length(), fullName.length() );
                // Strip off leading slash
                if (remainder.startsWith( "/" ))
                    remainder = remainder.substring( 1 );
            }
        }
        return remainder;
    }

    private OtmModelManager modelManager;

    private String baseNamespace;
    private List<OtmLibrary> libraries = null;
    // private List<OtmVersionChain> chains = null;
    private List<OtmDomain> subDomains = null;

    private List<String> providerDomains = null;
    private List<String> userDomains = null;

    /**
     * Build a domain representing the base namespace.
     * 
     * @param project
     * @param baseNamespace
     */
    public OtmDomain(String baseNamespace, OtmModelManager modelManager) {
        this.baseNamespace = baseNamespace;
        this.modelManager = modelManager;

        if (baseNamespace == null || baseNamespace.isEmpty())
            throw new IllegalArgumentException( "Domain must have a base namespace." );
        if (modelManager == null)
            throw new IllegalArgumentException( "Domain must have access to model manager." );


        // Called while loading libraries and the model may not be complete.
        // Lazy evaluate: buildSubDomains();
        // libraries = modelManager.getLibraries( baseNamespace );
    }

    private void buildLibraries() {
        libraries = modelManager.getLibraries( baseNamespace );
    }

    private void buildSubDomains() {
        if (subDomains == null)
            subDomains = new ArrayList<>();
        for (OtmDomain d : modelManager.getDomains()) {
            String subPath = getDomainSubPath( d.getDomain(), getDomain() );
            if (!subPath.isEmpty() && !subPath.contains( "/" ))
                subDomains.add( d );
        }
        subDomains.remove( this );
    }

    public String getBaseNamespace() {
        return baseNamespace;
    }

    public String getDescription() {
        return "Domain for " + baseNamespace;
    }

    /**
     * 
     * @return the full domain name (baseNamespace)
     */
    public String getDomain() {
        return getBaseNamespace();
    }

    public Image getIcon() {
        return ImageManager.getImage( Icons.DOMAIN );
    }

    public List<OtmLibrary> getLibraries() {
        if (libraries == null)
            buildLibraries();
        return libraries;
    }

    /**
     * The name of the domain is the portion after the last slash.
     * 
     * @see #getDomainName(String)
     * @return
     */
    public String getName() {
        return getDomainName( baseNamespace );
    }

    /**
     * 
     * @return new list of sub-domain full names
     */
    public List<String> getSubDomainNames() {
        List<String> names = new ArrayList<>();
        getSubDomains().forEach( s -> names.add( s.getBaseNamespace() ) );
        return names;
    }

    /**
     * 
     * @return new list of sub-domain names
     */
    public List<OtmDomain> getSubDomains() {
        if (subDomains == null)
            buildSubDomains();
        return subDomains;
    }

    public String toString() {
        return getName() + " = " + getBaseNamespace();
    }


    public List<String> getUserDomains() {
        if (userDomains == null) {
            userDomains = new ArrayList<>();
            // Get a list of all the libraries that use types from any library in this domain
            List<OtmLibrary> userLibs = new ArrayList<>();
            for (OtmLibrary lib : libraries) {
                userLibs.addAll( lib.getUsersMap().keySet() );
            }
            List<OtmLibrary> externalLibs = filterList( userLibs );
            // De-dup and add to list field
            for (OtmLibrary lib : externalLibs)
                if (!userDomains.contains( lib.getBaseNamespace() ))
                    userDomains.add( lib.getBaseNamespace() );
        }
        return userDomains;
    }

    // Remove all user libraries in this domain or its sub-domains
    private List<OtmLibrary> filterList(List<OtmLibrary> libList) {
        List<OtmLibrary> externalLibs = new ArrayList<>();
        for (OtmLibrary lib : libList) {
            if (!lib.getBaseNamespace().startsWith( baseNamespace ))
                externalLibs.add( lib );
        }
        return externalLibs;
    }

    // consider creating task for this and updating count on task complete
    public List<String> getProviderDomains() {
        if (providerDomains == null) {
            providerDomains = new ArrayList<>();

            // Get a list of all the libraries that provide types to any library in this domain
            List<OtmLibrary> pLibs = new ArrayList<>();
            for (OtmLibrary lib : libraries) {
                pLibs.addAll( lib.getProvidersMap().keySet() );
            }
            List<OtmLibrary> externalLibs = filterList( pLibs );
            // De-dup and add to list field
            for (OtmLibrary lib : externalLibs)
                if (!providerDomains.contains( lib.getBaseNamespace() ))
                    providerDomains.add( lib.getBaseNamespace() );
        }
        return providerDomains;
    }

}
