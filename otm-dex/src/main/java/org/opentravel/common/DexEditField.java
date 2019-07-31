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

package org.opentravel.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;

/**
 * Edit fields are presented by the GUI for the user to see and edit properties.
 * <P>
 * The fxNode should be fully configured with appropriate handlers.
 * 
 * @author dmh
 *
 */
public class DexEditField {
    private static Log log = LogFactory.getLog( DexEditField.class );

    public Node fxNode;
    public String label;
    public Tooltip tooltip;
    public int row;
    public int column;

    @Deprecated
    public DexEditField(String label, Node fxNode, int row) {
        this.label = label;
        this.fxNode = fxNode;
        this.row = row;
        this.column = 1;
    }


    /**
     * 
     * @param row integer 0 to n. 0 is placed right after description row
     * @param column integer 0 to n. Fields with label will require 2 columns.
     * @param label string for first column or if null no label column is used
     * @param tooltip added to label and fxNode
     * @param fxNode actual Java FX control node or if null no node column is used
     */
    public DexEditField(int row, int column, String label, String tooltip, Node fxNode) {
        this.label = label;
        this.fxNode = fxNode;
        this.row = row;
        this.column = column;
        this.tooltip = new Tooltip( tooltip );
    }
}
