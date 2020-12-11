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

import org.opentravel.dex.actions.DexActions;
import org.opentravel.model.OtmObject;
import org.opentravel.model.resource.OtmActionResponse;
import org.opentravel.model.resource.RestStatusCodes;

import java.util.Map.Entry;

import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;

/**
 * Create HBoxs to contain check box buttons. Provide event handlers for each check box.
 * 
 * @author dmh
 *
 */
public class DexRestStatusCodesHandler {
    // private static Log log = LogFactory.getLog( DexRestStatusCodesHandler.class );

    private OtmObject object;
    private RestStatusCodesMap codes;
    private static final int BOXSIZE = 4;

    public DexRestStatusCodesHandler(OtmObject object) {
        this.object = object;
        codes = new RestStatusCodesMap( object );
    }

    /**
     * Create a JavaFX HBox containing 4 individual check boxes for each status code.
     * 
     * @return HBox
     */
    public HBox makeMimeTypeBox1() {
        HBox hb = new HBox();
        hb.setSpacing( 10 );
        int cnt = 0;
        for (Entry<RestStatusCodes,Boolean> t : codes.get().entrySet()) {
            if (cnt++ < BOXSIZE) {
                CheckBox box = new CheckBox( t.getKey().toString() );
                box.setSelected( t.getValue() );
                box.setDisable( !object.getOwningMember().isEditable() );
                hb.getChildren().add( box );
                box.setOnAction( this::eventHandler );
            }
        }
        return hb;
    }

    /**
     * Create a JavaFX HBox containing 4 individual check boxes for each status code.
     * 
     * @return HBox
     */
    public HBox makeMimeTypeBox2() {
        HBox hb = new HBox();
        hb.setSpacing( 10 );
        int cnt = 0;
        for (Entry<RestStatusCodes,Boolean> t : codes.get().entrySet()) {
            if (cnt++ >= 4) {
                CheckBox box = new CheckBox( t.getKey().toString() );
                box.setSelected( t.getValue() );
                box.setDisable( !object.getOwningMember().isEditable() );
                hb.getChildren().add( box );
                box.setOnAction( this::eventHandler );
            }
        }
        return hb;
    }

    public void eventHandler(ActionEvent e) {
        if (e.getTarget() instanceof CheckBox) {
            CheckBox cb = (CheckBox) e.getTarget();
            // boolean v = values.get().get( cb.getText() );

            // Set the value into the status code map
            codes.set( cb.getText(), cb.isSelected() );
            // log.debug( "Check box " + cb.getText() + " = " + cb.isSelected() );
            // values.print();

            // Create and run the action
            object.getActionManager().run( DexActions.SETRESTSTATUSCODES, object, this.codes );
            ((OtmActionResponse) object).setRestStatusCodes( codes.getTLList() );
        }
    }


}
