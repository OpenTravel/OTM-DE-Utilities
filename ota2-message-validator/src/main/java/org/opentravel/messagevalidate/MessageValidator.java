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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;
import org.opentravel.schemacompiler.util.FileUtils;
import org.w3._2001.xmlschema.OpenAttrs;
import org.w3._2001.xmlschema.TopLevelElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.load.uri.URITranslatorConfiguration;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * Handles the validation of XML and JSON messages against the generated schemas.
 */
public class MessageValidator {

    private static final Logger log = LogManager.getLogger( MessageValidator.class );
    private static DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    private static JAXBContext jaxbContext;

    private File codegenFolder;
    private PrintStream out;

    /**
     * Constructor that specifies the print stream to which validation output should be directed.
     * 
     * @param codegenFolder the folder location that contains the generated XML and JSON schemas
     * @param out the print stream to which validation output should be directed
     */
    public MessageValidator(File codegenFolder, PrintStream out) {
        this.codegenFolder = codegenFolder;
        this.out = out;
    }

    /**
     * Perform a format-specific validation check of the given message file.
     * 
     * @param messageFile the message file to be validated
     */
    public void validate(File messageFile) {
        try {
            String filename = messageFile.getName().toLowerCase();

            if (filename.endsWith( ".xml" )) {
                validateXMLDocument( messageFile );

            } else if (filename.endsWith( ".json" )) {
                validateJSONDocument( messageFile );

            } else {
                out.print( "ERROR: Unrecognized file format (.json or .xml expected)" );
            }

        } catch (Exception e) {
            log.error( "Unexpected exception during message validation.", e );
        }
    }

    /**
     * Validates that the given XML file is syntactically and symantically correct.
     * 
     * @param xmlFile the XML file to validate
     */
    private void validateXMLDocument(File xmlFile) {
        try {
            DocumentBuilder dBuilder = domFactory.newDocumentBuilder();
            Document xmlDoc = dBuilder.parse( xmlFile );
            File schemaFile = getSchemaLocation( xmlDoc );

            if (schemaFile != null) {
                Schema vSchema = getValidationSchema( schemaFile );
                Validator validator = vSchema.newValidator();
                final List<String> errorList = new ArrayList<>();

                validator.setErrorHandler( new ErrorHandler() {
                    public void warning(SAXParseException ex) throws SAXException {
                        // No action for warnings
                    }

                    public void error(SAXParseException ex) throws SAXException {
                        addError( ex );
                    }

                    public void fatalError(SAXParseException ex) throws SAXException {
                        addError( ex );
                    }

                    private void addError(SAXParseException ex) {
                        errorList.add( ex.getMessage() );
                    }
                } );
                validator.validate( new StreamSource( xmlFile ) );
                out.println( "Validation Results: " + xmlFile.getName() + "\n" );

                if (errorList.isEmpty()) {
                    out.println( "No validation errors or warnings found." );

                } else {
                    for (String message : errorList) {
                        out.println( message );
                    }
                }

            } else {
                out.println( "No qualifying XML schema found for this document." );
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            out.println( "Error validating XML document: " + xmlFile.getAbsolutePath() );
            e.printStackTrace( out );
        }
    }

    /**
     * Returns a new validation schema that resolves resource references based on relative file system paths.
     * 
     * @return SchemaFactory
     * @throws SAXException thrown if the new validation schema cannot be created
     */
    private Schema getValidationSchema(File schemaFile) throws SAXException {
        SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );

        sf.setResourceResolver( new FileSystemResourceResolver( schemaFile ) );
        return sf.newSchema( schemaFile );
    }

    /**
     * Returns the path of the schema location from the given XML document. If a schema location is not specified, this
     * method will return null.
     * 
     * @param xmlDoc the XML document for which to return a schema location
     * @return File
     */
    private File getSchemaLocation(Document xmlDoc) {
        File schemaFolder = new File( codegenFolder.getAbsolutePath() + "/schemas" );
        Element rootElement = xmlDoc.getDocumentElement();
        QName rootElementName = new QName( rootElement.getNamespaceURI(), rootElement.getLocalName() );
        File schemaFile = null;

        for (File xsdFile : schemaFolder.listFiles()) {
            if (xsdFile.getName().endsWith( ".xsd" )) {
                Collection<QName> schemaElements = getGlobalElements( xsdFile );

                if (schemaElements.contains( rootElementName )) {
                    schemaFile = xsdFile;
                    break;
                }
            }
        }
        return schemaFile;
    }

    /**
     * Returns the list of global elements defined in the given XSD file. If the schema file cannot be parsed for any
     * reason, this method will fail silently and return an empty collection.
     * 
     * @param xsdFile the XML schema file
     * @return Collection&lt;QName&gt;
     */
    private Collection<QName> getGlobalElements(File xsdFile) {
        Set<QName> globalElements = new HashSet<>();

        try {
            Unmarshaller u = jaxbContext.createUnmarshaller();
            org.w3._2001.xmlschema.Schema schema =
                (org.w3._2001.xmlschema.Schema) FileUtils.unmarshalFileContent( xsdFile, u );

            for (OpenAttrs schemaItem : schema.getSimpleTypeOrComplexTypeOrGroup()) {
                if (schemaItem instanceof TopLevelElement) {
                    TopLevelElement xsdElement = (TopLevelElement) schemaItem;
                    QName elementName = xsdElement.getRef();

                    if (elementName == null) {
                        elementName = new QName( schema.getTargetNamespace(), xsdElement.getName() );
                    }
                    globalElements.add( elementName );
                }
            }

        } catch (JAXBException | IOException e) {
            // No action - return an empty collection
        }
        return globalElements;
    }

    /**
     * Validates that the given JSON file is syntactically and symantically correct.
     * 
     * @param jsonFile the JSON file to validate
     */
    private void validateJSONDocument(File jsonFile) {
        try {
            JsonNode jsonNode = JsonLoader.fromFile( jsonFile );
            File jsonSchemaFile = findJsonSchema( jsonNode );

            if (jsonSchemaFile != null) {
                JsonNode schemaNode = JsonLoader.fromFile( jsonSchemaFile );
                JsonSchema schema = newJsonSchemaFactory( jsonSchemaFile.getParentFile() ).getJsonSchema( schemaNode );
                ProcessingReport report = schema.validate( jsonNode );
                List<ProcessingMessage> errors = getValidationErrors( report );

                out.println( "Validation Results: " + jsonFile.getName() + "\n" );

                if (errors.isEmpty()) {
                    out.println( "No validation errors or warnings found." );

                } else {
                    for (ProcessingMessage error : errors) {
                        out.println( error );
                    }
                    out.println( "ERROR COUNT: " + errors.size() );
                }

            } else {
                out.println( "No qualifying JSON schema found for this document." );
            }

        } catch (ProcessingException | IOException e) {
            out.println( "Error validating JSON document: " + jsonFile.getAbsolutePath() );
            e.printStackTrace( out );
        }
    }

    /**
     * Returns a new <code>JsonSchemaFactory</code> instance.
     * 
     * @param schemaFolder the folder location where JSON schemas are located
     * @return JsonSchemaFactory
     */
    private static JsonSchemaFactory newJsonSchemaFactory(File schemaFolder) {
        return JsonSchemaFactory.newBuilder().setLoadingConfiguration( LoadingConfiguration.newBuilder()
            .setURITranslatorConfiguration( URITranslatorConfiguration.newBuilder()
                .setNamespace( "http://opentravel.org/schemas/json/" )
                .addPathRedirect( "http://opentravel.org/schemas/json/", schemaFolder.toURI().toString() ).freeze() )
            .freeze() ).freeze();

    }

    /**
     * Configures all of the types in the given JSON schema to disallow additional properties. This is not normally
     * enforced for OTM JSON schemas, but it is often useful when performing off-line message validation.
     * 
     * @param jsonSchema the JSON schema to be configured
     * @return JsonNode
     */
    protected JsonNode prohibitAdditionalProperties(JsonNode jsonSchema) {
        // TODO: Look for ways to make additional properties get flagged in the validation report
        // (JSON schema has trouble with substitution since discriminators are not supported)
        if (jsonSchema instanceof ObjectNode) {
            ObjectNode schemaObj = (ObjectNode) jsonSchema;

            if (jsonSchema.has( "properties" )) {
                schemaObj.put( "additionalProperties", false );
            }

            Iterator<JsonNode> iterator = schemaObj.elements();

            while (iterator.hasNext()) {
                prohibitAdditionalProperties( iterator.next() );
            }

        } else if (jsonSchema instanceof ArrayNode) {
            ArrayNode jsonArray = (ArrayNode) jsonSchema;
            Iterator<JsonNode> iterator = jsonArray.elements();

            while (iterator.hasNext()) {
                prohibitAdditionalProperties( iterator.next() );
            }
        }
        return jsonSchema;
    }

    /**
     * Returns the JSON schema file that should be used to validate the given JSON document. If no qualifying schema can
     * be located, this method will return null.
     * 
     * @param jsonDocument the JSON document to be validated
     * @return File
     */
    private File findJsonSchema(JsonNode jsonDocument) {
        Iterator<String> fieldNames = jsonDocument.fieldNames();
        String rootElement = fieldNames.hasNext() ? fieldNames.next() : null;
        File schemaFolder = new File( codegenFolder.getAbsolutePath() + "/json" );
        File schemaFile = null;

        for (File candidateFile : schemaFolder.listFiles()) {
            if (candidateFile.isFile()
                && candidateFile.getName().endsWith( JsonSchemaCodegenUtils.JSON_SCHEMA_FILENAME_EXT )) {
                try {
                    JsonNode schemaNode = JsonLoader.fromFile( candidateFile );

                    if (canValidate( schemaNode, rootElement )) {
                        schemaFile = candidateFile;
                        break;
                    }

                } catch (IOException e) {
                    // Ignore error and skip this file
                }
            }
        }
        return schemaFile;
    }

    /**
     * Returns true if the given JSON schema can be used to validate a JSON document with the specified root element.
     * 
     * @param jsonSchema the JSON schema that may be used to validate the document
     * @param rootElement the root element name of the JSON document to be validated
     * @return boolean
     */
    private boolean canValidate(JsonNode jsonSchema, String rootElement) {
        boolean validatable = false;

        if (rootElement != null) {
            JsonNode schemaOneOf = jsonSchema.get( "oneOf" );

            if (schemaOneOf instanceof ArrayNode) {
                for (JsonNode oneOfEntry : (ArrayNode) schemaOneOf) {
                    JsonNode oneOfProperties = oneOfEntry.get( "properties" );

                    if ((oneOfProperties != null) && (oneOfProperties.get( rootElement ) != null)) {
                        validatable = true;
                        break;
                    }
                }
            }
        }
        return validatable;
    }

    /**
     * Flattens the contents of the given JSON validation report and populates the given list of messages with only
     * those that are relevant message validation errors.
     * 
     * @param report the JSON validation report
     * @return List&lt;ProcessingMessage&gt;
     * @throws ProcessingException thrown if an error occurs during JSON validation
     */
    private List<ProcessingMessage> getValidationErrors(ProcessingReport report) throws ProcessingException {
        Iterator<ProcessingMessage> iterator = report.iterator();
        List<ProcessingMessage> errors = new ArrayList<>();

        while (iterator.hasNext()) {
            ProcessingMessage message = iterator.next();
            JsonNode messageNode = message.asJson();
            JsonNode reportsNode = messageNode.get( "reports" );

            if (reportsNode == null) {
                if ((message.getLogLevel() == LogLevel.ERROR) && !isSuperfluousReportNode( messageNode )) {
                    errors.add( message );
                }

            } else {
                Iterator<String> rnIterator = reportsNode.fieldNames();

                while (rnIterator.hasNext()) {
                    ArrayNode reportJson = (ArrayNode) reportsNode.get( rnIterator.next() );
                    errors.addAll( getValidationErrors( buildReport( reportJson ) ) );
                }
            }
        }
        return errors;
    }

    /**
     * Returns true if the given JSON report contains a single error that is not relevant to the overall validation
     * findings.
     * 
     * @param reportJson the JSON contents of the validation processing message
     * @return boolean
     */
    private boolean isSuperfluousReportNode(JsonNode reportJson) {
        JsonNode schemaJson = reportJson.get( "schema" );
        boolean result = false;

        if (schemaJson != null) {
            JsonNode pointerJson = schemaJson.get( "pointer" );
            result = (pointerJson != null) && pointerJson.asText( "" ).startsWith( "/oneOf/" );
        }
        return result;
    }

    /**
     * Reconstructs a <code>ProcessingReport</code> instance from the JSON content provided.
     * 
     * @param reportJson the JSON content of the validation report
     * @return ProcessingReport
     * @throws ProcessingException thrown if an error occurs while reconstructing the report instance
     */
    private ProcessingReport buildReport(ArrayNode reportJson) throws ProcessingException {
        ProcessingReport report = new ListProcessingReport();
        Iterator<JsonNode> iterator = reportJson.iterator();

        while (iterator.hasNext()) {
            ProcessingMessage message = new ProcessingMessage();
            JsonNode messageJson = iterator.next();
            Iterator<String> fnIterator = messageJson.fieldNames();

            while (fnIterator.hasNext()) {
                String fieldName = fnIterator.next();
                JsonNode fieldValue = messageJson.get( fieldName );

                if (fieldName.equals( "level" )) {
                    message.setLogLevel( LogLevel.valueOf( fieldValue.asText().toUpperCase() ) );
                } else {
                    message.put( fieldName, fieldValue );
                }
            }
            switch (message.getLogLevel()) {
                case FATAL:
                    report.fatal( message );
                    break;
                case ERROR:
                    report.error( message );
                    break;
                case WARNING:
                    report.warn( message );
                    break;
                case INFO:
                    report.info( message );
                    break;
                case DEBUG:
                    report.debug( message );
                    break;
                default:
                    break;
            }
        }
        return report;
    }

    /**
     * Initializes the JAXB context for the XML schema-for-schemas.
     */
    static {
        try {
            jaxbContext = JAXBContext.newInstance( "org.w3._2001.xmlschema" );
            domFactory.setNamespaceAware( true );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }
}
