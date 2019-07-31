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

package org.opentravel.model.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.codegen.impl.QualifiedAction;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLResourceParentRef;

import java.util.List;

/**
 * Handle building paths for resource action requests.
 * 
 * @author dmh
 *
 */
public class DexResourcePathHandler {
    private static Log log = LogFactory.getLog( DexResourcePathHandler.class );

    private static final String SYSTEM = "http://example.com";
    private OtmResource resource;
    // private String basePath;

    public DexResourcePathHandler(OtmResource resource) {
        log.debug( "Resource path handler initialized." );
        this.resource = resource;
        // this.basePath = getBasePathContribution();
    }

    public String get(OtmAction action) {
        StringBuilder builder = new StringBuilder();
        builder.append( getResourceBaseURL() );
        builder.append( getBasePathContribution() );
        builder.append( getParentContribution( action ) );
        builder.append( getCollectionContribution( action.getRequest() ) );
        builder.append( getPathParameterContributions( action.getRequest() ) );
        builder.append( getQueryParameterContributions( action.getRequest() ) );
        builder.append( "  " + getPayloadExample( action.getRequest() ) );
        return builder.toString();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        resource.getActions().forEach( a -> builder.append( toString( a ) + "\n" ) );
        return builder.toString();
    }

    public String toString(OtmAction action) {
        StringBuilder builder = new StringBuilder();
        builder.append( get( action ) );
        builder.append( "\n\tBaseURL: " + getResourceBaseURL() );
        builder.append( " -BasePath:" + getBasePathContribution() );
        builder.append( " -ParentContrib:" + getParentContribution( action ) );
        builder.append( " -PathParams:" + getPathParameterContributions( action.getRequest() ) );
        return builder.toString();
    }

    /*
     * Get base path and assure it starts with slash and ends without slash
     */
    protected String getBasePathContribution() {
        String path = resource.getBasePath();
        if (path == null)
            path = "";
        if (!path.isEmpty()) {
            if (!path.startsWith( "/" ))
                path = "/" + path;
            if (path.endsWith( "/" ))
                path = path.substring( 0, path.lastIndexOf( "/" ) );
        }
        return path;
    }

    protected String getCollectionContribution(OtmActionRequest request) {
        String path = "";
        // If the request has a path template, use it
        if (request != null && request.getPathTemplate() != null && !request.getPathTemplate().isEmpty())
            path = correct( request.getPathTemplate() ); // override may correct template
        else {
            // else Use the resource subject
            path = "/" + resource.getSubjectName();
            if (!path.endsWith( "s" ))
                path += "s";
        }
        return path;
    }

    // Fix a path template from a request
    public String correct(String value) {
        // Strip off anything after the first parameter's {
        value = value.trim();
        if (value.contains( "{" ))
            value = value.substring( 0, value.indexOf( '{' ) ).trim();
        if (value.endsWith( "/" ))
            value = value.substring( 0, value.length() - 1 );
        if (!value.startsWith( "/" ))
            value = "/" + value;
        return value;
    }

    protected String getPayloadExample(OtmActionRequest request) {
        String payload = "";
        if (request != null && request.getTL().getPayloadType() != null)
            payload = request.getPayloadTypeName();
        return !payload.isEmpty() ? " <" + payload + ">...</" + payload + ">" : "";
    }

    private String getResourceBaseURL() {
        String resourceBaseURL;
        // FIXME - how is the base URL accessed in DEX?
        // final CompilerPreferences compilePreferences = new CompilerPreferences(
        // CompilerPreferences.loadPreferenceStore());
        // resourceBaseURL = compilePreferences.getResourceBaseUrl();
        // In junits the resource base URL will be empty
        // if (resourceBaseURL.isEmpty())
        resourceBaseURL = SYSTEM;
        return resourceBaseURL;
    }

    protected String getPathParameterContributions(OtmActionRequest request) {
        StringBuilder path = new StringBuilder();
        String separator = "/";
        if (request != null && request.getParamGroup() != null) {
            for (OtmParameter param : request.getParamGroup().getParameters()) {
                if (param.isPathParam())
                    path.append( separator + param.getPathContribution() );
            }
        }
        return path.toString();
    }

    protected String getQueryParameterContributions(OtmActionRequest request) {
        StringBuilder path = new StringBuilder();
        String separator = "?";
        if (request != null && request.getParamGroup() != null) {
            for (OtmParameter param : request.getParamGroup().getParameters()) {
                if (param.isQueryParam()) {
                    path.append( param.getQueryContribution( separator ) );
                    separator = "&";
                }
            }
        }
        return path.toString();
    }

    // Copied from OTM-DE on 7/30/2019
    private String getParentContribution(OtmAction action) {
        StringBuilder contribution = new StringBuilder();
        if (action != null) {

            // // Pick any one of these
            // List<QualifiedAction> qa = ResourceCodegenUtils.getQualifiedActions(getTLModelObject());
            // if (qa.isEmpty()) return contribution;
            // String template = qa.get(0).getPathTemplate();

            // From hotfix: https://github.com/OpenTravel/OTM-DE/commit/a9be4859740aaeb9e5607485d844bf67786b4816
            // Since numerous combinations of parent reference paths are possible, pick
            // the last entry in the list of qualified actions. It is the most likely to
            // have an "interesting" path that contains parent references.
            //
            List<QualifiedAction> qa = ResourceCodegenUtils.getQualifiedActions( action.getTL() );
            if (!qa.isEmpty()) {
                List<TLResourceParentRef> parentRefs = qa.get( qa.size() - 1 ).getParentRefs();
                for (TLResourceParentRef tlRef : parentRefs) {
                    if (tlRef.getPathTemplate() != null)
                        contribution.append( tlRef.getPathTemplate() );
                }
            }
        }
        return contribution.toString();
    }
}
