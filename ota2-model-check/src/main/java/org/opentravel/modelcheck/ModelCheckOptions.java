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

package org.opentravel.modelcheck;

import java.util.Properties;

/**
 * Options used to specify which rules will be enforced during model-check analysis.
 */
public class ModelCheckOptions {

    private static ModelCheckOptions defaultOptions;

    private boolean checkMissingDocumentation;
    private boolean checkMissingExamples;
    private boolean checkFacetReferences;
    private boolean checkMultiVersionReferences;

    /**
     * Returns the flag indicating whether to check for missing documentation.
     *
     * @return boolean
     */
    public boolean isCheckMissingDocumentation() {
        return checkMissingDocumentation;
    }

    /**
     * Assigns the value of the 'checkMissingDocumentation' field.
     *
     * @param checkMissingDocumentation the field value to assign
     */
    public void setCheckMissingDocumentation(boolean checkMissingDocumentation) {
        this.checkMissingDocumentation = checkMissingDocumentation;
    }

    /**
     * Returns the flag indicating whether to check for missing examples.
     *
     * @return boolean
     */
    public boolean isCheckMissingExamples() {
        return checkMissingExamples;
    }

    /**
     * Assigns the value of the 'checkMissingExamples' field.
     *
     * @param checkMissingExamples the field value to assign
     */
    public void setCheckMissingExamples(boolean checkMissingExamples) {
        this.checkMissingExamples = checkMissingExamples;
    }

    /**
     * Returns the flag indicating whether to check for direct facet references.
     *
     * @return boolean
     */
    public boolean isCheckFacetReferences() {
        return checkFacetReferences;
    }

    /**
     * Assigns the value of the 'checkFacetReferences' field.
     *
     * @param checkFacetReferences the field value to assign
     */
    public void setCheckFacetReferences(boolean checkFacetReferences) {
        this.checkFacetReferences = checkFacetReferences;
    }

    /**
     * Returns the flag indicating whether to check for references to multiple version of a library.
     *
     * @return boolean
     */
    public boolean isCheckMultiVersionReferences() {
        return checkMultiVersionReferences;
    }

    /**
     * Assigns the value of the 'checkMultiVersionReferences' field.
     *
     * @param checkMultiVersionReferences the field value to assign
     */
    public void setCheckMultiVersionReferences(boolean checkMultiVersionReferences) {
        this.checkMultiVersionReferences = checkMultiVersionReferences;
    }

    /**
     * Loads the settings for this options instance from the given properties.
     * 
     * @param props the properties from which to load the comparison options
     */
    public void loadOptions(Properties props) {
        String checkMissingDocumentationStr =
            props.getProperty( "checkMissingDocumentation", defaultOptions.isCheckMissingDocumentation() + "" );
        String checkMissingExamplesStr =
            props.getProperty( "checkMissingExamples", defaultOptions.isCheckMissingExamples() + "" );
        String checkFacetReferencesStr =
            props.getProperty( "checkFacetReferences", defaultOptions.isCheckFacetReferences() + "" );
        String checkMultiVersionReferencesStr =
            props.getProperty( "checkMultiVersionReferences", defaultOptions.isCheckMultiVersionReferences() + "" );

        this.checkMissingDocumentation = Boolean.parseBoolean( checkMissingDocumentationStr );
        this.checkMissingExamples = Boolean.parseBoolean( checkMissingExamplesStr );
        this.checkFacetReferences = Boolean.parseBoolean( checkFacetReferencesStr );
        this.checkMultiVersionReferences = Boolean.parseBoolean( checkMultiVersionReferencesStr );
    }

    /**
     * Saves the current settings of this options instance to the given properties.
     * 
     * @param props the properties to which the comparison options should be saved
     */
    public void saveOptions(Properties props) {
        props.put( "checkMissingDocumentation", checkMissingDocumentation + "" );
        props.put( "checkMissingExamples", checkMissingExamples + "" );
        props.put( "checkFacetReferences", checkFacetReferences + "" );
        props.put( "checkMultiVersionReferences", checkMultiVersionReferences + "" );
    }

    /**
     * Assigns the default option settings for the given instance.
     * 
     * @param options the options instance to be configured
     */
    private static void configureDefaultOptions(ModelCheckOptions options) {
        options.setCheckMissingDocumentation( false );
        options.setCheckMissingExamples( false );
        options.setCheckFacetReferences( false );
        options.setCheckMultiVersionReferences( false );
    }

    /**
     * Initializes the settings for the default comparison options.
     */
    static {
        try {
            defaultOptions = new ModelCheckOptions();
            configureDefaultOptions( defaultOptions );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
