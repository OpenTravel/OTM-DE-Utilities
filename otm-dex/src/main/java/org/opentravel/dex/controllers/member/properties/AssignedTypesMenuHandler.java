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

package org.opentravel.dex.controllers.member.properties;

import org.opentravel.common.DialogBox;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Add handler with a listener for a combo box or choice box.
 * 
 * @author dmh
 *
 */
@Deprecated
// -technique is OK,no longer needed.
public class AssignedTypesMenuHandler {
    static final String CHANGE = "Change";
    static final String GOTO = "Go To";
    static final String REMOVE = "Remove (future)";
    static final String STRING = "xsd:String (future)";

    /**
     * @return an observable list of values for the assigned type actions
     */
    public static ObservableList<String> getAssignedTypeList() {
        ObservableList<String> list = FXCollections.observableArrayList();
        list.add( GOTO );
        list.add( CHANGE );
        list.add( REMOVE );
        list.add( STRING );
        return list;
    }

    public void handle(String selection, PropertiesDAO prop) {
        // if (selection.equals(CHANGE)) {
        // prop.getValue().getActionManager().addAction(DexActions.TYPECHANGE, prop);
        // } else if (selection.equals(GOTO)) {
        // OtmLibraryMember otm = findAssignedType(prop);
        // if (otm != null)
        // prop.getController().fireEvent(new DexMemberSelectionEvent(otm));
        // else
        // prop.getController().getMainController().postError(null, "The type assigned could not be found.");
        // } else {
        DialogBox.notify( "Assigned Type Menu", selection + " is not implemented yet." );
        // }
    }

    public OtmLibraryMember findAssignedType(PropertiesDAO prop) {
        if (prop.getValue() instanceof OtmTypeUser) {
            OtmTypeUser user = (OtmTypeUser) prop.getValue();
            OtmObject otm = OtmModelElement.get( (TLModelElement) user.getAssignedTLType() );
            if (otm != null && !(otm instanceof OtmLibraryMember))
                otm = otm.getOwningMember();
            return (OtmLibraryMember) otm;
        }
        return null;
    }

}
