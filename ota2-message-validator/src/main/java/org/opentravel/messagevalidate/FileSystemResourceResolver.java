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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Resolves XML resources using the local classpath resources.
 */
public class FileSystemResourceResolver implements LSResourceResolver {
	
	private File sourceFolder;
	private File builtInsFolder;
	private File legacyFolder;
	
	/**
	 * Constructor that provides the location of the original source schema file.
	 * 
	 * @param sourceSchema  the location of the original source schema file
	 */
	public FileSystemResourceResolver(File sourceSchema) {
		this.sourceFolder = sourceSchema.getParentFile();
		this.builtInsFolder = new File( this.sourceFolder, "/built-ins" );
		this.legacyFolder = new File( this.sourceFolder, "/legacy" );
	}
	
    /**
     * @see org.w3c.dom.ls.LSResourceResolver#resolveResource(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public LSInput resolveResource(final String type, final String namespaceURI,
            final String publicID, final String systemID, final String baseURI) {
        LSInput input = null;

        if (systemID != null) {
            final InputStream resourceStream = getResourceStream(systemID);
            final Reader resourceReader = (resourceStream == null) ? null : new InputStreamReader(resourceStream);

            if (resourceStream != null) {
                input = new LSInput() {
                	
                	private String _systemID = systemID;
                	private String _publicID = publicID;
                	
                    @Override
                    public void setSystemId(String systemID) {
                    	this._systemID = systemID;
                    }

                    @Override
                    public void setStringData(String stringData) {
                    }

                    @Override
                    public void setPublicId(String publicID) {
                    	this._publicID = publicID;
                    }

                    @Override
                    public void setEncoding(String encoding) {
                    }

                    @Override
                    public void setCharacterStream(Reader characterStream) {
                    }

                    @Override
                    public void setCertifiedText(boolean certifiedText) {
                    }

                    @Override
                    public void setByteStream(InputStream byteStream) {
                    }

                    @Override
                    public void setBaseURI(String baseURI) {
                    }

                    @Override
                    public String getSystemId() {
                        return _systemID;
                    }

                    @Override
                    public String getStringData() {
                        return null;
                    }

                    @Override
                    public String getPublicId() {
                        return _publicID;
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
        }
        return input;
    }

    /**
     * Returns an input stream to the resource associated with the given systemID, or null if no
     * such resource was defined in the application context.
     * 
     * @param systemID
     *            the systemID for which to return an input stream
     * @return InputStream
     */
    private InputStream getResourceStream(String systemID) {
        InputStream resourceStream = null;
        try {
        	File schemaFile = new File( sourceFolder, "/" + systemID );
        	
        	if (!schemaFile.exists()) {
        		schemaFile = new File( builtInsFolder, "/" + systemID );
        	}
        	if (!schemaFile.exists()) {
        		schemaFile = new File( legacyFolder, "/" + systemID );
        	}
    		resourceStream = new FileInputStream( schemaFile );
    		
        } catch (IOException e) {
            // no error - return a null input stream
        }
        if (resourceStream == null) {
            System.out.println("WARNING: No associated schema resource defined for System-ID: " + systemID);
        }
        return resourceStream;
    }
    
}
