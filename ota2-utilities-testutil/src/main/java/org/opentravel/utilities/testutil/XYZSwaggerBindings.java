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

import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.swagger.CodeGenerationSwaggerBindings;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerHeader;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerParamType;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerParameter;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerResponse;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerScheme;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerSecurityScheme;
import org.opentravel.schemacompiler.codegen.swagger.model.SwaggerSecurityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides an example swagger binding extension for testing of the swagger code generator.
 */
public class XYZSwaggerBindings implements CodeGenerationSwaggerBindings {

    private static final List<SwaggerSecurityScheme> securitySchemes;
    private static final List<SwaggerParameter> globalParameters;
    private static final List<SwaggerHeader> globalResponseHeaders;
    private static final List<SwaggerResponse> globalResponses;

    /**
     * @see org.opentravel.schemacompiler.codegen.swagger.CodeGenerationSwaggerBindings#getGlobalParameters()
     */
    @Override
    public List<SwaggerParameter> getGlobalParameters() {
        return globalParameters;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.swagger.CodeGenerationSwaggerBindings#getGlobalResponseHeaders()
     */
    @Override
    public List<SwaggerHeader> getGlobalResponseHeaders() {
        return globalResponseHeaders;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.swagger.CodeGenerationSwaggerBindings#getGlobalResponses()
     */
    @Override
    public List<SwaggerResponse> getGlobalResponses() {
        return globalResponses;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.swagger.CodeGenerationSwaggerBindings#getSupportedSchemes()
     */
    @Override
    public List<SwaggerScheme> getSupportedSchemes() {
        return Collections.emptyList();
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.swagger.CodeGenerationSwaggerBindings#getSecuritySchemes()
     */
    @Override
    public List<SwaggerSecurityScheme> getSecuritySchemes() {
        return securitySchemes;
    }

    /**
     * Initializes the list of global security schemes.
     * 
     * @return List&lt;SwaggerSecurityScheme&gt;
     */
    private static List<SwaggerSecurityScheme> initSecuritySchemes() {
        List<SwaggerSecurityScheme> securitySchemes = new ArrayList<>();
        SwaggerSecurityScheme basicAuthScheme = new SwaggerSecurityScheme();

        basicAuthScheme.setName( "BasicAuthScheme" );
        basicAuthScheme.setType( SwaggerSecurityType.BASIC );
        securitySchemes.add( basicAuthScheme );
        return securitySchemes;
    }

    /**
     * Initializes the list of global parameters.
     * 
     * @return List&lt;SwaggerParameter&gt;
     */
    protected static List<SwaggerParameter> initGlobalParameters() {
        List<SwaggerParameter> paramList = new ArrayList<>();

        paramList.add( newParameter( "TEST-PARAM-HEADER", SwaggerParamType.HEADER, false, JsonType.JSON_STRING,
            "Description of the test header parameter." ) );
        return paramList;
    }

    /**
     * Constructs a new Swagger parameter using the information provided.
     * 
     * @param name the parameter name
     * @param in the position of the parameter (e.g. header, query, path)
     * @param required flag indicating whether the parameter is required
     * @param type the parameter type
     * @param documentation the parameter documentation
     * @return SwaggerParameter
     */
    protected static SwaggerParameter newParameter(String name, SwaggerParamType in, boolean required, JsonType type,
        String documentation) {
        JsonDocumentation jsonDoc = (documentation == null) ? null : new JsonDocumentation( documentation );
        SwaggerParameter param = new SwaggerParameter();
        JsonSchema paramSchema = new JsonSchema();

        paramSchema.setType( type );
        param.setName( name );
        param.setIn( in );
        param.setRequired( required );
        param.setType( paramSchema );
        param.setDocumentation( jsonDoc );
        return param;
    }

    /**
     * Initializes the list of global response headers.
     * 
     * @return List&lt;SwaggerHeader&gt;
     */
    private static List<SwaggerHeader> initGlobalResponseHeaders() {
        List<SwaggerHeader> headerList = new ArrayList<>();

        headerList
            .add( newHeader( "TEST-RESPONSE-HEADER", JsonType.JSON_STRING, "Description of test response header" ) );
        return headerList;
    }

    /**
     * Constructs a new Swagger header using the information provided.
     * 
     * @param name the name of the header
     * @param type the header type
     * @param documentation the header documentation
     * @return SwaggerHeader
     */
    protected static SwaggerHeader newHeader(String name, JsonType type, String documentation) {
        JsonDocumentation jsonDoc = (documentation == null) ? null : new JsonDocumentation( documentation );
        SwaggerHeader header = new SwaggerHeader();
        JsonSchema headerSchema = new JsonSchema();

        headerSchema.setType( type );
        header.setName( name );
        header.setType( headerSchema );
        header.setDocumentation( jsonDoc );
        return header;
    }

    /**
     * Initializes the list of global responses.
     * 
     * @return List&lt;SwaggerResponse&gt;
     */
    private static List<SwaggerResponse> initGlobalResponses() {
        List<SwaggerResponse> responseList = new ArrayList<>();
        List<SwaggerHeader> headers = initGlobalResponseHeaders();
        JsonSchema schema = new JsonSchema();

        schema.setType( JsonType.JSON_STRING );
        responseList.add( newResponse( 401, schema, headers, "Unauthorized" ) );
        responseList.add( newResponse( 429, schema, headers, "Too Many Requests" ) );
        return responseList;
    }

    /**
     * Constructs a new Swagger response using the information provided.
     * 
     * @param statusCode the status code for the response
     * @param schema the schema for the response payload
     * @param responseHeaders the headers for the response
     * @param documentation the response documentation
     * @return SwaggerResponse
     */
    private static SwaggerResponse newResponse(int statusCode, JsonSchema schema, List<SwaggerHeader> responseHeaders,
        String documentation) {
        JsonDocumentation jsonDoc = (documentation == null) ? null : new JsonDocumentation( documentation );
        SwaggerResponse response = new SwaggerResponse();

        response.setStatusCode( statusCode );
        response.setSchema( new JsonSchemaReference( schema ) );
        response.getHeaders().addAll( responseHeaders );
        response.setDocumentation( jsonDoc );
        return response;
    }

    /**
     * Initializes the list of global parameters for all API's generated with the TVP binding style.
     */
    static {
        try {
            securitySchemes = initSecuritySchemes();
            globalParameters = initGlobalParameters();
            globalResponseHeaders = initGlobalResponseHeaders();
            globalResponses = initGlobalResponses();

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
