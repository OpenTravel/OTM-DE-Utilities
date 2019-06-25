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

package org.opentravel.model.otmProperties;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * GUI oriented listing of property types
 * 
 * @author Dave Hollander
 * 
 */
public enum UserSelectablePropertyTypes {

    Element("Element"),
    Attribute("Attribute"),
    Indicator("Indicator"),
    IndicatorElement("Indicator Element"),
    Id("XML ID"),
    ElementReference("Reference Element"),
    AttributeReference("Reference Attribute"),
    IdReference("XML ID Reference");
    private String label; // User displayed value

    UserSelectablePropertyTypes(String label) {
        this.label = label;
    }

    /**
     * @return The user displayed label for this node type
     */
    public String label() {
        return label;
    }

    /**
     * @param label
     * @return the value of the label or ELEMENT
     */
    public static UserSelectablePropertyTypes lookup(String label) {
        for (UserSelectablePropertyTypes v : values())
            if (v.label.equals( label ))
                return v;
        return UserSelectablePropertyTypes.Element;
    }

    // TODO - refactor to somewhere out of model since it is fx specific
    public static final ObservableList<String> getObservableList() {
        ObservableList<String> list = FXCollections.observableArrayList();
        for (Object value : UserSelectablePropertyTypes.values()) {
            list.add( value.toString() );
        }
        return list;
    }

    public static final List<String> getList() {
        List<String> list = new ArrayList<>();
        for (Object value : UserSelectablePropertyTypes.values()) {
            list.add( value.toString() );
        }
        return list;
    }

}
