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

package org.opentravel.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.StringProperty;

/**
 * Handler for deprecation, description and example values.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmDocHandler {
    private static Log log = LogFactory.getLog( OtmDocHandler.class );

    private static final String EXAMPLE_CONTEXT = "Example";
    protected OtmObject obj;

    // JavaFX Properties
    private StringProperty descriptionProperty = null;
    private StringProperty deprecationProperty = null;
    private StringProperty exampleProperty = null;

    /**
     */
    public OtmDocHandler(OtmObject object) {
        obj = object;
        assert obj != null;
    }

    public StringProperty deprecationProperty() {
        if (deprecationProperty == null && getActionManager() != null) {
            deprecationProperty = getActionManager().add( DexActions.DEPRECATIONCHANGE, getDeprecation(), obj );
        }
        return deprecationProperty;
    }

    public StringProperty descriptionProperty() {
        if (descriptionProperty == null && getActionManager() != null) {
            descriptionProperty = getActionManager().add( DexActions.DESCRIPTIONCHANGE, getDescription(), obj );
        }
        return descriptionProperty;
    }

    public StringProperty exampleProperty() {
        if (exampleProperty == null && getActionManager() != null) {
            exampleProperty = getActionManager().add( DexActions.EXAMPLECHANGE, getExample(), obj );
        }
        return exampleProperty;
    }

    private DexActionManager getActionManager() {
        assert obj.getOwningMember() != null;
        return obj.getOwningMember().getActionManager();
    }

    public String getDeprecation() {
        if (obj.getTL() instanceof TLDocumentationOwner) {
            TLDocumentation doc = ((TLDocumentationOwner) obj.getTL()).getDocumentation();
            if (doc != null && doc.getDeprecations() != null && !doc.getDeprecations().isEmpty())
                return doc.getDeprecations().get( 0 ).getText();
        }
        return "";
    }

    public String getDescription() {
        if (obj.getTL() instanceof TLDocumentationOwner) {
            TLDocumentation doc = ((TLDocumentationOwner) obj.getTL()).getDocumentation();
            if (doc != null)
                return doc.getDescription();
        }
        return "";
    }

    public String getExample() {
        if (obj.getTL() instanceof TLExampleOwner) {
            List<TLExample> exs = ((TLExampleOwner) obj.getTL()).getExamples();
            if (exs != null && !exs.isEmpty())
                return exs.get( 0 ).getValue();
        }
        return "";
    }

    public boolean isDeprecated() {
        if (obj.getTL() instanceof TLDocumentationOwner) {
            TLDocumentation doc = ((TLDocumentationOwner) obj.getTL()).getDocumentation();
            if (doc != null && doc.getDeprecations() != null && !doc.getDeprecations().isEmpty())
                return true;
        }
        return false;
    }

    public void refresh() {
        deprecationProperty = null;
        descriptionProperty = null;
        exampleProperty = null;
    }

    public String setDeprecation(String deprecation) {
        if (obj.getTL() instanceof TLDocumentationOwner) {
            TLDocumentation doc = ((TLDocumentationOwner) obj.getTL()).getDocumentation();
            if (doc != null) {
                // Remove any deprecation
                List<TLDocumentationItem> list = new ArrayList<>( doc.getDeprecations() );
                // list.forEach( d -> doc.removeDeprecation( d ) );
                list.forEach( doc::removeDeprecation );
            }
            // Set deprecation if not null or empty
            if (deprecation != null && !deprecation.isEmpty()) {
                if (doc == null) {
                    // Create new documentation object
                    doc = new TLDocumentation();
                    ((TLDocumentationOwner) obj.getTL()).setDocumentation( doc );
                }
                TLDocumentationItem docItem = new TLDocumentationItem();
                docItem.setText( deprecation );
                doc.addDeprecation( docItem );
            }
        }
        return getDeprecation();
    }


    public String setDescription(String description) {
        if (obj.getTL() instanceof TLDocumentationOwner) {
            TLDocumentation doc = ((TLDocumentationOwner) obj.getTL()).getDocumentation();
            if (doc == null) {
                doc = new TLDocumentation();
                ((TLDocumentationOwner) obj.getTL()).setDocumentation( doc );
            }
            doc.setDescription( description );
        }
        // ModelEvents are only thrown when the documentation element changes.
        if (descriptionProperty != null)
            descriptionProperty.setValue( description );
        return getDescription();
    }

    public String setExample(String value) {
        if (obj.getTL() instanceof TLExampleOwner) {
            // Remove any existing examples
            List<TLExample> examples = new ArrayList<>( ((TLExampleOwner) obj.getTL()).getExamples() );
            examples.forEach( ((TLExampleOwner) obj.getTL())::removeExample );
            // If value is not null and not empty
            if (value != null && !value.isEmpty()) {
                TLExample tlEx = new TLExample();
                tlEx.setValue( value );
                tlEx.setContext( EXAMPLE_CONTEXT );
                ((TLExampleOwner) obj.getTL()).addExample( tlEx );
            }
        }
        return getExample();
    }

}
