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
import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.OtmObject;

import java.util.Map.Entry;

import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;

/**
 * Create HBox to contain check box buttons. Provide event handlers for each check box.
 * 
 * @author dmh
 *
 */
public class DexMimeTypeHandler {
    private static Log log = LogFactory.getLog( DexMimeTypeHandler.class );

    private OtmObject object;
    private MimeTypeMap values;

    public DexMimeTypeHandler(OtmObject object) {
        this.object = object;
        values = new MimeTypeMap( object );
    }

    /**
     * Create a JavaFX HBox containing the individual check boxes for each mime type.
     * 
     * @return HBox
     */
    public HBox makeMimeTypeBox() {
        HBox hb = new HBox();
        hb.setSpacing( 10 );
        for (Entry<String,Boolean> t : values.get().entrySet()) {
            CheckBox box = new CheckBox( t.getKey() );
            box.setSelected( t.getValue() );
            box.setDisable( !object.getOwningMember().isEditable() );
            hb.getChildren().add( box );
            box.setOnAction( this::eventHandler );
        }
        return hb;
    }

    public void eventHandler(ActionEvent e) {
        if (e.getTarget() instanceof CheckBox) {
            CheckBox cb = (CheckBox) e.getTarget();
            // boolean v = values.get().get( cb.getText() );
            values.get().put( cb.getText(), cb.isSelected() );
            log.debug( "Check box " + cb.getText() + " = " + cb.isSelected() );
            // values.print();

            // Create and run the action
            object.getActionManager().run( DexActions.SETMIMETYPES, object, this.values );
        }
    }


}
