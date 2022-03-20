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

package org.opentravel.application.common;

import javafx.scene.image.Image;

/**
 * Constant definitions for images that are shared across OTM applications.
 */
public class Images {

    public static final Image rootIcon = new Image( Images.class.getResourceAsStream( "/common-images/root.gif" ) );
    public static final Image repositoryIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/repository.gif" ) );
    public static final Image rootNSIcon = new Image( Images.class.getResourceAsStream( "/common-images/rootNS.gif" ) );
    public static final Image baseNSIcon = new Image( Images.class.getResourceAsStream( "/common-images/baseNS.gif" ) );
    public static final Image assemblyIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/assembly.gif" ) );
    public static final Image releaseIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/release.gif" ) );
    public static final Image libraryIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/library.png" ) );
    public static final Image folderIcon = new Image( Images.class.getResourceAsStream( "/common-images/folder.gif" ) );
    public static final Image businessObjectIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/business-object.png" ) );
    public static final Image choiceObjectIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/choice-object.gif" ) );
    public static final Image coreObjectIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/core-object.gif" ) );
    public static final Image roleValueIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/role-value.jpg" ) );
    public static final Image vwaIcon = new Image( Images.class.getResourceAsStream( "/common-images/vwa.gif" ) );
    public static final Image enumerationIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/enumeration.gif" ) );
    public static final Image simpleTypeIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/simple-type.gif" ) );
    public static final Image facetIcon = new Image( Images.class.getResourceAsStream( "/common-images/facet.gif" ) );
    public static final Image contextualFacetIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/facet-contextual.gif" ) );
    public static final Image actionFacetIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/facet-action.gif" ) );
    public static final Image extensionPointFacetIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/facet-extension-point.gif" ) );
    public static final Image aliasIcon = new Image( Images.class.getResourceAsStream( "/common-images/alias.gif" ) );
    public static final Image attributeIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/attribute.gif" ) );
    public static final Image elementIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/element.gif" ) );
    public static final Image indicatorIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/indicator.gif" ) );
    public static final Image resourceIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/resource.gif" ) );
    public static final Image parentRefIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/parent-ref.png" ) );
    public static final Image paramGroupIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/param-group.gif" ) );
    public static final Image parameterIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/parameter.gif" ) );
    public static final Image actionIcon = new Image( Images.class.getResourceAsStream( "/common-images/action.gif" ) );
    public static final Image requestIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/request.gif" ) );
    public static final Image responseIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/response.gif" ) );
    public static final Image serviceIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/service.gif" ) );
    public static final Image infoIcon = new Image( Images.class.getResourceAsStream( "/common-images/info.gif" ) );
    public static final Image warningIcon =
        new Image( Images.class.getResourceAsStream( "/common-images/warning.gif" ) );
    public static final Image errorIcon = new Image( Images.class.getResourceAsStream( "/common-images/error.gif" ) );

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private Images() {}

}
