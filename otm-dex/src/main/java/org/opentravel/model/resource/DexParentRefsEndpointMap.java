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
import org.opentravel.schemacompiler.model.TLHttpMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Resources can supply Actions with multiple endpoint paths; one for each 1st class parent reference.
 * <p>
 * This map has an entry for each 1st class parent ref. The strings are just the path contribution--they do not contain
 * system or action contributions.
 * <p>
 * Note: this resource's contribution is not in the paths. Its contribution depends on the action.
 * 
 * @author Dave Hollander
 * 
 */
// Testbo Parent Reference Paths
// Path from parent Parent1 is /Parent1Path/{idAttr_Parent1}
// Path from parent GGP_Parent2 is
// /GGP_Parent2Path/{idAttr_GGP_Parent2}/GP_Parent2Path/{idAttr_GP_Parent2}/Parent2Path/{idAttr_Parent2}
// Path from parent Parent2 is /Parent2Path/{idAttr_Parent2}
// Path from parent Parent3 is ThisIsFromParentRefTemplate_Parent3
// Path from parent GP_Parent1 is /GP_Parent1Path/{idAttr_GP_Parent1}/Parent1Path/{idAttr_Parent1}
// -- Testbo Parent Reference Paths
//
public class DexParentRefsEndpointMap {
    /**
     * 
     */
    public static final String NO_PATH_NOTFIRSTCLASS_AND_NOPARENTREFS =
        "No path -- make 1st class or add parent references.";
    public static final String NO_PATH = "No Path -- only accessed using parent path.";
    public static final String NO_PAYLOAD = "None";
    private static final String SYSTEM = "http://example.com";

    private static Log log = LogFactory.getLog( DexParentRefsEndpointMap.class );
    private OtmResource resource = null;
    private Map<OtmParentRef,String> endpoints = new HashMap<>();

    public DexParentRefsEndpointMap(OtmResource resource) {
        this.resource = resource;
        build();
    }

    /**
     * Build the map if empty then return it.
     * 
     * @return
     */
    public Map<OtmParentRef,String> get() {
        if (endpoints.isEmpty())
            build();
        return endpoints;
    }

    /**
     * Get the string associated with the parent reference.
     * 
     * @param parentRef
     * @return
     */
    public String get(OtmParentRef parentRef) {
        if (endpoints.containsKey( parentRef ))
            return endpoints.get( parentRef );
        return "not found";
    }

    /**
     * Get the string associated with the resource. Resource must parent reference of a parentRef in the map.
     * 
     * @param resource
     * @return associated path string or empty string
     */
    public String get(OtmResource resource) {
        for (Entry<OtmParentRef,String> es : endpoints.entrySet()) {
            if (es.getKey().getParentResource() == resource)
                return es.getValue();
        }
        return "";
    }

    /**
     * Each action can have multiple endpoint paths.
     * <ul>
     * <li>If 1st class, then just it's template and path parameters
     * <li>One for each 1st class parentRef path with this path path from the ref's ID parameters and its ancestors
     * 
     * @return this endpoint map
     */
    public DexParentRefsEndpointMap build() {
        endpoints.clear();

        // Add path for each 1st class parent referenced
        for (OtmParentRef parentRef : resource.getParentRefs())
            getEndpointRefs( "", endpoints, parentRef );

        return this;
    }

    protected void getEndpointRefs(String parentPart, Map<OtmParentRef,String> endpoints, OtmParentRef parentRef) {
        if (parentRef != null && parentRef.getParentResource() != null) {
            if (parentRef.isParentFirstClass())
                endpoints.put( parentRef, getEndpointPath( parentRef ) + parentPart );

            parentPart = getEndpointPath( parentRef ) + parentPart;

            for (OtmParentRef ref : parentRef.getParentResource().getParentRefs())
                getEndpointRefs( parentPart, endpoints, ref );
        }
    }

    /**
     * Utility to get the path from the parent ref's template or, if null or empty, use the base path and path parameter
     * contributions.
     * 
     * @param parentRef
     * @return
     */
    public static String getEndpointPath(OtmParentRef parentRef) {
        // Path template from action's request. It should have parameters already in it!
        StringBuilder builder = new StringBuilder();
        if (parentRef.getPathTemplate() == null || parentRef.getPathTemplate().isEmpty()) {
            builder.append( getEndpointPath( parentRef.getParentResource(), parentRef.getParameterGroup() ) );
        } else {
            builder.append( parentRef.getPathTemplate() );
        }
        return builder.toString();
    }

    public static String getEndpointPath(OtmResource resource, OtmParameterGroup group) {
        StringBuilder builder = new StringBuilder();
        builder.append( getBasePathContribution( resource ) );
        builder.append( getPathParameterContributions( group ) );
        return builder.toString();
    }

    public static String getPathParameterContributions(OtmParentRef parentRef) {
        return getPathParameterContributions( parentRef.getParameterGroup() );
    }

    public static String getPathParameterContributions(OtmParameterGroup parameterGroup) {
        StringBuilder path = new StringBuilder();
        String separator = "/";
        if (parameterGroup != null) {
            for (OtmParameter param : parameterGroup.getParameters()) {
                if (param.isPathParam())
                    path.append( separator + param.getPathContribution() );
            }
        }
        return path.toString();
    }


    /**
     * Utility to get a path for the action, use it's request path template and parameters.
     * 
     * @param action can be null
     * @return
     */
    public static String getActionContribution(OtmAction action) {
        // Path template from action's request. It should have parameters already in it!
        StringBuilder builder = new StringBuilder();
        if (action != null && action.getRequest() != null) {
            builder.append( getCollectionContribution( action ) );
            builder.append( getPathParameterContributions( action.getRequest() ) );
        }
        return builder.toString();
    }

    /**
     * Utility to get path parameter contributions from the request's parameter group
     * 
     * @param request
     * @return
     */
    public static String getPathParameterContributions(OtmActionRequest request) {
        StringBuilder path = new StringBuilder();
        String separator = "/";
        if (request != null && request.getParamGroup() != null) {
            for (OtmParameter param : request.getParamGroup().getParameters()) {
                if (param.isPathParam())
                    path.append( separator + param.getPathContribution() );
            }
        } else
            path.append( separator );
        return path.toString();
    }

    /**
     * Utility to get the path template from the action request stripped of all parameters. If empty, use the resource's
     * plural subject name.
     * 
     * @param request
     * @return
     */
    public static String getCollectionContribution(OtmAction action) {
        String path = "";
        OtmActionRequest request = action.getRequest();
        // If the request has a path template, use it
        if (request != null && request.getPathTemplate() != null && !request.getPathTemplate().isEmpty())
            path = stripParameters( request.getPathTemplate() ); // override may correct template
        else {
            // else Use the resource subject
            path = "/" + makePlural( action.getOwningMember().getSubjectName() );
        }
        return path;
    }

    /**
     * Utility to strip parameters from string (anything after the first {). Assure starts with slash (/) and does not
     * end with slash.
     * 
     * @param value
     * @return
     */
    public static String stripParameters(String value) {
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

    /**
     * Get base path and assure it starts with slash and ends without slash
     */
    public static String getBasePathContribution(OtmResource resource) {
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

    /**
     * Utility to add s to string if it does not end in s
     * 
     * @param string
     * @return
     */
    public static String makePlural(String string) {
        if (!string.endsWith( "s" ))
            string = string + "s";
        return string;
    }



    /**
     * Debugging Utility
     */
    public void print() {
        log.debug( "-- " + resource + " Parent Reference Paths" );
        if (endpoints == null || endpoints.isEmpty()) {
            log.debug( "EMPTY MAP!" );
            return;
        }
        for (Entry<OtmParentRef,String> set : endpoints.entrySet())
            log.debug( "Path from parent " + set.getKey().getName() + " is " + set.getValue() );
    }

    /**
     * Debugging Utility
     */
    public int size() {
        return endpoints.size();
    }

    /**
     * @param request
     * @return the name from the ActionFacet (not the name of the AF)
     */
    public static String getPayloadExample(OtmActionRequest request) {
        String pn = "";
        String noPayload = NO_PAYLOAD;
        if (request != null) {
            pn = request.getPayloadName();
            // Present "None" only for methods that use payloads
            if (request.getMethod() == TLHttpMethod.GET || request.getMethod() == TLHttpMethod.DELETE)
                noPayload = "--";
        }
        return !pn.isEmpty() ? " <" + pn + ">...</" + pn + ">" : noPayload;
    }

    public static String getPayloadExample(OtmActionResponse response) {
        String payload = "";
        if (response != null)
            payload = response.getPayloadName();
        return !payload.isEmpty() ? " <" + payload + ">...</" + payload + ">" : NO_PAYLOAD;
    }

    public static String getResourceBaseURL() {
        String resourceBaseURL;
        // FIXME - how is the base URL accessed in DEX?
        // final CompilerPreferences compilePreferences = new CompilerPreferences(
        // CompilerPreferences.loadPreferenceStore());
        // resourceBaseURL = compilePreferences.getResourceBaseUrl();
        // In junits the resource base URL will be empty
        // if (resourceBaseURL.isEmpty())
        resourceBaseURL = SYSTEM; // FIXME - this should come from user settings
        return resourceBaseURL;
    }

    public static String getResourceBaseURL(OtmResource resource) {
        String resourceBaseURL;
        // FIXME - how is the base URL accessed in DEX?
        // final CompilerPreferences compilePreferences = new CompilerPreferences(
        // CompilerPreferences.loadPreferenceStore());
        // resourceBaseURL = compilePreferences.getResourceBaseUrl();
        // In junits the resource base URL will be empty
        // if (resourceBaseURL.isEmpty())
        resourceBaseURL = SYSTEM; // FIXME - this should come from user settings
        resourceBaseURL += resource.getBasePath();
        return resourceBaseURL;
    }

    // /**
    // * Get exemplar path. This is the EndpointPath from the most "interesting" action and its parent ref. Goal is to
    // * have the most complete path. The endpoint path will be from the parent reference path template or, if empty,
    // the
    // * base path and parameters from the parent resource.
    // *
    // * @return
    // */
    // @Deprecated
    // protected String setExemplar(OtmAction action) {
    // StringBuilder contribution = new StringBuilder();
    // // The last qualified action should be the most complete
    // List<QualifiedAction> qa = ResourceCodegenUtils.getQualifiedActions( action.getTL() );
    // if (!qa.isEmpty()) {
    // // Get the last parent ref from the last qualified action
    // List<TLResourceParentRef> parentRefs = qa.get( qa.size() - 1 ).getParentRefs();
    // if (!parentRefs.isEmpty()) {
    // OtmParentRef parentRef = (OtmParentRef) OtmModelElement.get( parentRefs.get( parentRefs.size() - 1 ) );
    // contribution.append( get( parentRef ) );
    // }
    // }
    // if (contribution.toString().isEmpty())
    // contribution.append( MAKE_1ST_CLASS_OR_ADD_PARENT_REFERENCES );
    //
    // return contribution.toString();
    // }
}
