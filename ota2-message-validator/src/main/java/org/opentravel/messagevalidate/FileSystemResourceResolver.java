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

package org.opentravel.messagevalidate;

import org.opentravel.application.common.OtmApplicationRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Resolves XML resources using the local classpath resources.
 */
public class FileSystemResourceResolver implements LSResourceResolver {

    private static final Logger log = LoggerFactory.getLogger( FileSystemResourceResolver.class );

    private File sourceFolder;
    private File builtInsFolder;
    private File legacyFolder;

    /**
     * Constructor that provides the location of the original source schema file.
     * 
     * @param sourceSchema the location of the original source schema file
     */
    public FileSystemResourceResolver(File sourceSchema) {
        this.sourceFolder = sourceSchema.getParentFile();
        this.builtInsFolder = new File( this.sourceFolder.getAbsolutePath() + "/built-ins" );
        this.legacyFolder = new File( this.sourceFolder.getAbsolutePath() + "/legacy" );
    }

    /**
     * @see org.w3c.dom.ls.LSResourceResolver#resolveResource(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public LSInput resolveResource(final String type, final String namespaceURI, final String publicID,
        final String systemID, final String baseURI) {
        LSInput input = null;

        if (systemID != null) {
            final InputStream resourceStream = getResourceStream( systemID );

            try (Reader resourceReader = (resourceStream == null) ? null : new InputStreamReader( resourceStream )) {
                if (resourceStream != null) {
                    input = new LSInput() {

                        private String sid = systemID;
                        private String pid = publicID;

                        @Override
                        public void setSystemId(String systemID) {
                            this.sid = systemID;
                        }

                        @Override
                        public void setStringData(String stringData) {
                            // No action required
                        }

                        @Override
                        public void setPublicId(String publicID) {
                            this.pid = publicID;
                        }

                        @Override
                        public void setEncoding(String encoding) {
                            // No action required
                        }

                        @Override
                        public void setCharacterStream(Reader characterStream) {
                            // No action required
                        }

                        @Override
                        public void setCertifiedText(boolean certifiedText) {
                            // No action required
                        }

                        @Override
                        public void setByteStream(InputStream byteStream) {
                            // No action required
                        }

                        @Override
                        public void setBaseURI(String baseURI) {
                            // No action required
                        }

                        @Override
                        public String getSystemId() {
                            return sid;
                        }

                        @Override
                        public String getStringData() {
                            return null;
                        }

                        @Override
                        public String getPublicId() {
                            return pid;
                        }

                        @Override
                        public String getEncoding() {
                            return "UTF-8";
                        }

                        @Override
                        public Reader getCharacterStream() {
                            return resourceReader;
                        }

                        @Override
                        public boolean getCertifiedText() {
                            return false;
                        }

                        @Override
                        public InputStream getByteStream() {
                            return resourceStream;
                        }

                        @Override
                        public String getBaseURI() {
                            return baseURI;
                        }
                    };
                }

            } catch (IOException e) {
                throw new OtmApplicationRuntimeException( e );
            }

        }
        return input;
    }

    /**
     * Returns an input stream to the resource associated with the given systemID, or null if no such resource was
     * defined in the application context.
     * 
     * @param systemID the systemID for which to return an input stream
     * @return InputStream
     */
    private InputStream getResourceStream(String systemID) {
        InputStream resourceStream = null;
        try {
            String sidPath = File.separator + systemID;

            File schemaFile = new File( sourceFolder, sidPath );

            if (!schemaFile.exists()) {
                schemaFile = new File( builtInsFolder, sidPath );
            }
            if (!schemaFile.exists()) {
                schemaFile = new File( legacyFolder, sidPath );
            }
            resourceStream = new FileInputStream( schemaFile );

        } catch (IOException e) {
            // no error - return a null input stream
        }
        if ((resourceStream == null) && log.isWarnEnabled()) {
            log.warn( String.format( "No associated schema resource defined for System-ID: %s", systemID ) );
        }
        return resourceStream;
    }

}
