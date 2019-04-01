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

package org.opentravel.exampleupgrade;

import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * Static utility methods for the Example Helper application.
 */
public class HelperUtils {

    private static VersionScheme otaVersionScheme;

    /**
     * Private constructor to prevent instantiation.
     */
    private HelperUtils() {}

    /**
     * Returns a QName for the given DOM element.
     * 
     * @param domElement the DOM element for which to return a qualified name
     * @return QName
     */
    public static QName getElementName(Element domElement) {
        String prefix = domElement.getPrefix();

        return new QName( domElement.getNamespaceURI(), domElement.getLocalName(), (prefix == null) ? "" : prefix );
    }

    /**
     * Returns the base namespace of the given namespace.
     * 
     * @param ns the namespace for which to return the base
     * @return String
     */
    public static String getBaseNamespace(String ns) {
        return otaVersionScheme.getBaseNamespace( ns );
    }

    /**
     * Returns the simple text value of the given DOM element.
     * 
     * @param domElement the DOM element for which to return the text value
     * @return String
     */
    public static String getElementTextValue(Element domElement) {
        Node textNode = domElement.getFirstChild();
        String nodeValue = null;

        while ((textNode != null) && !(textNode instanceof Text)) {
            textNode = textNode.getNextSibling();
        }
        nodeValue = (textNode == null) ? null : ((Text) textNode).getData();

        if ((nodeValue != null) && (nodeValue.trim().length() == 0)) {
            nodeValue = null;
        }
        return nodeValue;
    }

    /**
     * Returns the message associated with the given throwable or a default message if none is defined.
     * 
     * @param t the throwable for which to return an error message
     * @return String
     */
    public static String getErrorMessage(Throwable t) {
        Class<?> errorType = t.getClass();
        String errorMessage = t.getMessage();

        while (((errorMessage == null) || (errorMessage.trim().length() == 0)) && (t.getCause() != null)) {
            t = t.getCause();
            errorMessage = t.getMessage();
        }

        if (errorMessage == null) {
            errorMessage = "An unknown error occurred: " + errorType.getSimpleName() + " (see log for DETAILS).";
        }
        return errorMessage;
    }

    /**
     * Returns the specified OTM entity from the model provided.
     * 
     * @param model the model that owns the OTM entity to be returned
     * @param namespace the namespace of the OTM entity to return
     * @param localName the local name of the OTM entity to return
     * @return NamedEntity
     */
    public static NamedEntity findOTMEntity(TLModel model, String namespace, String localName) {
        List<AbstractLibrary> libraryList = model.getLibrariesForNamespace( namespace );
        NamedEntity builtInType = null;

        for (AbstractLibrary library : libraryList) {
            if ((builtInType = library.getNamedMember( localName )) != null) {
                break;
            }
        }
        return builtInType;
    }

    /**
     * Returns a display name label for the given OTM entity.
     * 
     * @param entity the entity for which to return a display name
     * @param showPrefix flag indicating whether the owning library's prefix should be included in the label
     * @return String
     */
    public static String getDisplayName(NamedEntity entity, boolean showPrefix) {
        TLLibrary library = (TLLibrary) entity.getOwningLibrary();
        QName elementName = XsdCodegenUtils.getGlobalElementName( entity );
        String localName = (elementName != null) ? elementName.getLocalPart() : entity.getLocalName();
        StringBuilder displayName = new StringBuilder();

        if (showPrefix && (library.getPrefix() != null)) {
            displayName.append( library.getPrefix() ).append( ":" );
        }
        displayName.append( localName );

        return displayName.toString();
    }

    /**
     * Initializes the OTA2 version scheme.
     */
    static {
        try {
            VersionSchemeFactory factory = VersionSchemeFactory.getInstance();
            otaVersionScheme = factory.getVersionScheme( factory.getDefaultVersionScheme() );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
