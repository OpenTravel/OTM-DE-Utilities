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

package org.opentravel.objecteditor;

import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Persists settings for the <code>DE-x Compiler</code> between sessions.
 * <p>
 */
public class UserCompilerSettings {
    private static final Logger log = LoggerFactory.getLogger( UserCompilerSettings.class );

    private static final int EXAMPLE_MAX_REPEAT = 3;
    private static final int EXAMPLE_MAX_DEPTH = 3;
    private static final String BINDING_STYLE = "compiler_binding_style";

    /**
     * Returns the default user settings.
     * 
     * @return UserSettings
     */
    public static void getDefaultSettings(UserSettings settings) {
        // Done on instantiation
    }

    // Compiler Options
    private String bindingStyle = "";
    private boolean compileSchemas = true;
    private boolean compileJsonSchemas = true;
    private boolean compileServices = true;
    private boolean compileSwagger = true;
    private boolean compileOpenApi = true;
    private boolean compileHtml = true;
    private String serviceEndpointUrl = "http://example.com/resource";
    private String resourceBaseUrl = "http://example.com/resource";
    private boolean suppressOtmExtensions = false;
    private boolean generateExamples = true;

    private boolean generateMaxDetailsForExamples = true;
    private String defaultMimeTypes = "APPLICATION_JSON;APPLICATION_XML";
    private String defaultRequestPayload = "";

    private String defaultResponsePayload = "";
    private String exampleContext = "example.com";
    private int exampleMaxRepeat = EXAMPLE_MAX_REPEAT;
    private int exampleMaxDepth = EXAMPLE_MAX_DEPTH;
    private int repeatCount;


    private boolean suppressOptionalFields = false;


    public String getBindingStyle() {
        return bindingStyle;
    }

    /**
     * Default resource mime types
     */
    public String getDefaultMimeTypes() {
        return defaultMimeTypes;
    }

    /**
     * Default request payload
     */
    public OtmObject getDefaultRequestPayload(OtmModelManager mgr) {
        return mgr.getMember( defaultRequestPayload );
    }

    /**
     * Default response payload
     */
    public OtmObject getDefaultResponsePayload(OtmModelManager mgr) {
        return mgr.getMember( defaultResponsePayload );
    }

    public String getExampleContext() {
        return exampleContext;
    }

    public int getExampleMaxDepth() {
        return exampleMaxDepth;
    }

    public int getExampleMaxRepeat() {
        return exampleMaxRepeat;
    }

    /**
     * Returns the value of the repeat-count spinner.
     */
    public int getRepeatCount() {
        return repeatCount;
    }

    public String getResourceBaseUrl() {
        return resourceBaseUrl;
    }

    public String getServiceEndpointUrl() {
        return serviceEndpointUrl;
    }

    public boolean isCompileHtml() {
        return compileHtml;
    }

    public boolean isCompileJsonSchemas() {
        return compileJsonSchemas;
    }

    public boolean isCompileSchemas() {
        return compileSchemas;
    }

    public boolean isCompileServices() {
        return compileServices;
    }

    public boolean isCompileSwagger() {
        return compileSwagger;
    }

    public boolean isCompileOpenApi() {
        return compileOpenApi;
    }

    public boolean isGenerateExamples() {
        return generateExamples;
    }

    public boolean isGenerateMaxDetailsForExamples() {
        return generateMaxDetailsForExamples;
    }

    public boolean isSuppressOptionalFields() {
        return suppressOptionalFields;
    }

    public boolean isSuppressOtmExtensions() {
        return suppressOtmExtensions;
    }

    /**
     * Load the properties from the settings
     */
    protected void load(Properties settingsProps) {

        // Resource defaults
        setDefaultMimeTypes( settingsProps.getProperty( "defaultMimeTypes" ) );
        setDefaultRequestPayload( settingsProps.getProperty( "defaultRequestPayload" ) );
        setDefaultResponsePayload( settingsProps.getProperty( "defaultResponsePayload" ) );

        // Compiler Options
        setBindingStyle( settingsProps.getProperty( BINDING_STYLE ) );
        setCompileSchemas( Boolean.valueOf( settingsProps.getProperty( "compileSchemas" ) ) );
        setCompileJsonSchemas( Boolean.valueOf( settingsProps.getProperty( "compileJsonSchemas" ) ) );
        setCompileServices( Boolean.valueOf( settingsProps.getProperty( "compileServices" ) ) );
        setCompileSwagger( Boolean.valueOf( settingsProps.getProperty( "compileSwagger" ) ) );
        setCompileOpenApi( Boolean.valueOf( settingsProps.getProperty( "compileOpenApi" ) ) );
        setCompileHtml( Boolean.valueOf( settingsProps.getProperty( "compileHtml" ) ) );
        setSuppressOtmExtensions( Boolean.valueOf( settingsProps.getProperty( "suppressOtmExtensions" ) ) );
        setGenerateExamples( Boolean.valueOf( settingsProps.getProperty( "generateExamples" ) ) );
        setSuppressOptionalFields( Boolean.valueOf( settingsProps.getProperty( "suppressOptionalFields" ) ) );
        setResourceBaseUrl( settingsProps.getProperty( "resourceBaseUrl" ) );
        setServiceEndpointUrl( settingsProps.getProperty( "serviceEndpointUrl" ) );
        setExampleContext( settingsProps.getProperty( "exampleContext" ) );
        setExampleMaxRepeat( settingsProps.getProperty( "exampleMaxRepeat" ) );
        setExampleMaxDepth( settingsProps.getProperty( "exampleMaxDepth" ) );
    }

    private void putString(Properties settingsProps, String key, String value) {
        if (key == null)
            return;
        if (value == null)
            value = "";
        settingsProps.put( key, value );
    }

    /**
     * Save the properties into the settings
     */
    protected void save(Properties settingsProps) {

        // Resource Options
        putString( settingsProps, "defaultMimeTypes", defaultMimeTypes );
        putString( settingsProps, "defaultResponsePayload", defaultResponsePayload );
        putString( settingsProps, "defaultRequestPayload", defaultRequestPayload );

        // Compiler Options
        putString( settingsProps, BINDING_STYLE, bindingStyle );
        settingsProps.put( "compileSchemas", Boolean.toString( compileSchemas ) );
        settingsProps.put( "compileJsonSchemas", Boolean.toString( compileJsonSchemas ) );
        settingsProps.put( "compileServices", Boolean.toString( compileServices ) );
        settingsProps.put( "compileSwagger", Boolean.toString( compileSwagger ) );
        settingsProps.put( "compileOpenApi", Boolean.toString( compileOpenApi ) );
        settingsProps.put( "compileHtml", Boolean.toString( compileHtml ) );
        putString( settingsProps, "resourceBaseUrl", resourceBaseUrl );
        putString( settingsProps, "serviceEndpointUrl", serviceEndpointUrl );
        putString( settingsProps, "suppressOtmExtensions", Boolean.toString( suppressOtmExtensions ) );
        settingsProps.put( "generateExamples", Boolean.toString( generateExamples ) );
        settingsProps.put( "generateMaxDetailsForExamples", Boolean.toString( generateMaxDetailsForExamples ) );
        putString( settingsProps, "exampleContext", exampleContext );
        settingsProps.put( "exampleMaxRepeat", exampleMaxRepeat + "" );
        settingsProps.put( "exampleMaxDepth", exampleMaxDepth + "" );
        settingsProps.put( "suppressOptionalFields", Boolean.toString( suppressOptionalFields ) );
    }


    public void setBindingStyle(String bindingStyle) {
        this.bindingStyle = bindingStyle;
    }

    public void setCompileHtml(boolean compileHtml) {
        this.compileHtml = compileHtml;
    }

    public void setCompileJsonSchemas(boolean compileJson) {
        this.compileJsonSchemas = compileJson;
    }

    public void setCompileSchemas(boolean compileSchemas) {
        this.compileSchemas = compileSchemas;
    }

    public void setCompileServices(boolean compileServices) {
        this.compileServices = compileServices;
    }

    public void setCompileSwagger(boolean compileSwagger) {
        this.compileSwagger = compileSwagger;
    }

    public void setCompileOpenApi(boolean compileOpenApi) {
        this.compileOpenApi = compileOpenApi;
    }

    public void setDefaultMimeTypes(String values) {
        defaultMimeTypes = values;
    }

    public void setDefaultRequestPayload(OtmObject payload) {
        if (payload != null)
            setDefaultRequestPayload( payload.getNameWithPrefix() );
    }

    public void setDefaultRequestPayload(String nameWithPrefix) {
        this.defaultRequestPayload = nameWithPrefix;
    }


    public void setDefaultResponsePayload(OtmObject payload) {
        setDefaultResponsePayload( payload.getNameWithPrefix() );
    }

    public void setDefaultResponsePayload(String nameWithPrefix) {
        this.defaultResponsePayload = nameWithPrefix;
    }

    public void setExampleContext(String exampleContext) {
        this.exampleContext = exampleContext;
    }

    public void setExampleMaxDepth(int exampleMaxDepth) {
        this.exampleMaxDepth = exampleMaxDepth;
    }

    public void setExampleMaxDepth(String exampleMaxDepthString) {
        try {
            if (exampleMaxDepthString != null && !exampleMaxDepthString.isEmpty())
                this.exampleMaxDepth = Integer.parseInt( exampleMaxDepthString );
        } catch (NumberFormatException e) {
            exampleMaxDepth = EXAMPLE_MAX_DEPTH;
        }
    }

    public void setExampleMaxRepeat(int exampleMaxRepeat) {
        this.exampleMaxRepeat = exampleMaxRepeat;
    }

    public void setExampleMaxRepeat(String exampleMaxRepeatString) {
        try {
            if (exampleMaxRepeatString != null && !exampleMaxRepeatString.isEmpty())
                this.exampleMaxRepeat = Integer.parseInt( exampleMaxRepeatString );
        } catch (NumberFormatException e) {
            exampleMaxRepeat = EXAMPLE_MAX_REPEAT;
        }
    }

    public void setGenerateExamples(boolean generateExamples) {
        this.generateExamples = generateExamples;
    }

    public void setGenerateMaxDetailsForExamples(boolean generateMaxDetailsForExamples) {
        this.generateMaxDetailsForExamples = generateMaxDetailsForExamples;
    }

    /**
     * Assigns the value of the repeat-count spinner.
     *
     * @param repeatCount the repeat count value to assign
     */
    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public void setResourceBaseUrl(String resourceBaseUrl) {
        this.resourceBaseUrl = resourceBaseUrl;
    }

    public void setServiceEndpointUrl(String serviceEndpointUrl) {
        this.serviceEndpointUrl = serviceEndpointUrl;
    }

    public void setSuppressOptionalFields(boolean suppressOptionalFields) {
        this.suppressOptionalFields = suppressOptionalFields;
    }

    public void setSuppressOtmExtensions(boolean suppressOtmExtensions) {
        this.suppressOtmExtensions = suppressOtmExtensions;
    }

}
