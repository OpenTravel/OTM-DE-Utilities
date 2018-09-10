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
import java.net.URL;

import org.opentravel.schemacompiler.task.CommonCompilerTaskOptions;
import org.opentravel.schemacompiler.task.CompileAllTaskOptions;

/**
 * Default compiler options used when generating JSON and XML schemas for
 * message validation.
 */
public class ValidationCompileOptions implements CompileAllTaskOptions {
	
	private File outputFolder;
	
	/**
	 * Constructor that specifies the foler location for generated compiler output.
	 * 
	 * @param outputFolder  the folder where schema files will be compiled
	 */
	public ValidationCompileOptions(File outputFolder) {
		this.outputFolder = outputFolder;
	}
	
	/**
	 * Returns the folder where schema files will be compiled.
	 * 
	 * @return String
	 */
	public String getOutputFolder() {
		return outputFolder.getAbsolutePath();
	}
	
	public boolean isCompileSchemas() {
		return true;
	}
	
	public boolean isCompileJsonSchemas() {
		return true;
	}

	public URL getServiceLibraryUrl() {
		return null;
	}
	
	public String getServiceEndpointUrl() {
		return null;
	}
	
	public boolean isGenerateMaxDetailsForExamples() {
		return true;
	}
	
	public boolean isGenerateExamples() {
		return false;
	}
	
	public Integer getExampleMaxRepeat() {
		return new Integer(2);
	}
	
	public Integer getExampleMaxDepth() {
		return new Integer(2);
	}
	
	public boolean isSuppressOptionalFields() {
		return false;
	}

	public String getExampleContext() {
		return null;
	}
	
	public String getCatalogLocation() {
		return null;
	}
	
	public boolean isCompileServices() {
		return false;
	}
	
	public String getResourceBaseUrl() {
		return null;
	}

	public boolean isCompileSwagger() {
		return false;
	}

	public boolean isSuppressOtmExtensions() {
		return true;
	}
	
	public boolean isCompileHtml() {
		return false;
	}
	
	public void applyTaskOptions(CommonCompilerTaskOptions taskOptions) {
	}

}
