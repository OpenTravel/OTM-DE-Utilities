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

package org.opentravel.utilities.testutil;

import org.opentravel.schemacompiler.extension.CompilerExtension;
import org.opentravel.schemacompiler.extension.CompilerExtensionProvider;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the <code>CompilerExtensionProvider</code> service that provides extensions used for
 * unit/integration testing purposes.
 */
public class XYZCompilerExtensionProvider implements CompilerExtensionProvider {

    /** The unique ID of the XYZ compiler extension. */
    public static final String XYZ_COMPILER_EXTENSION_ID = "XYZ";

    private static final Collection<CompilerExtension> compilerExtensions;
    private static final Map<String,List<String>> extensionUrls;

    /**
     * @see org.opentravel.schemacompiler.extension.CompilerExtensionProvider#getCompilerExtensions()
     */
    @Override
    public Collection<CompilerExtension> getCompilerExtensions() {
        return compilerExtensions;
    }

    /**
     * @see org.opentravel.schemacompiler.extension.CompilerExtensionProvider#isSupportedExtension(java.lang.String)
     */
    @Override
    public boolean isSupportedExtension(String extensionId) {
        return extensionUrls.containsKey( extensionId );
    }

    /**
     * @see org.opentravel.schemacompiler.extension.CompilerExtensionProvider#loadCompilerExtension(org.springframework.context.support.GenericApplicationContext,
     *      java.lang.String)
     */
    @Override
    public void loadCompilerExtension(GenericApplicationContext context, String extensionId) {
        if (!isSupportedExtension( extensionId )) {
            throw new IllegalArgumentException( "Unsupported compiler extension: " + extensionId );
        }
        XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader( context );

        beanReader.setBeanClassLoader( getClass().getClassLoader() );
        beanReader.setValidating( false );

        for (String configUrl : extensionUrls.get( extensionId )) {
            InputStream configStream = getClass().getResourceAsStream( configUrl );

            if (configStream == null) {
                throw new BeanDefinitionStoreException( "Unable to load configuration file: " + configUrl );
            }
            beanReader.loadBeanDefinitions( new InputStreamResource( configStream ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.extension.CompilerExtensionProvider#loadGeneralCompilerExtensions(org.springframework.context.support.GenericApplicationContext)
     */
    @Override
    public void loadGeneralCompilerExtensions(GenericApplicationContext context) {
        // No action required for this extension provider
    }

    /**
     * @see org.opentravel.schemacompiler.extension.CompilerExtensionProvider#getExtensionResource(java.lang.String)
     */
    @Override
    public InputStream getExtensionResource(String resourcePath) {
        return getClass().getResourceAsStream( resourcePath );
    }

    /**
     * Registers a single binding style using the information provided.
     * 
     * @param extensions the list that will contain the registered extensions for this provider
     * @param urlMap associates each extension ID with a list of URL locations for the Spring configuration files for
     *        the binding
     * @param extensionId the unique ID of the extension to register
     * @param rank the ranking of the extension to register
     * @param configLocations the classpath URL for the Spring configuration files that define the binding style
     */
    private static void registerBindingStyle(Collection<CompilerExtension> extensions, Map<String,List<String>> urlMap,
        String extensionId, int rank, String... configLocations) {
        List<String> configUrls = new ArrayList<>();

        for (String configLocation : configLocations) {
            configUrls.add( configLocation );
        }
        extensions.add( new CompilerExtension( extensionId, rank ) );
        urlMap.put( extensionId, configUrls );
    }

    /**
     * Initializes the static collection of compiler extensions.
     */
    static {
        try {
            Collection<CompilerExtension> extensions = new ArrayList<>();
            Map<String,List<String>> urlMap = new HashMap<>();

            registerBindingStyle( extensions, urlMap, XYZ_COMPILER_EXTENSION_ID, 100,
                "/test-context/xyzBaseCompilerExtensions.xml", "/ota2-context/defaultCompilerExtensions.xml" );
            compilerExtensions = Collections.unmodifiableCollection( extensions );
            extensionUrls = Collections.unmodifiableMap( urlMap );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
