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

package org.opentravel.dex.actions.resource;

import org.opentravel.dex.actions.string.DexStringAction;
import org.opentravel.model.OtmObject;
import org.opentravel.model.resource.OtmActionFacet;
import org.opentravel.model.resource.OtmActionRequest;
import org.opentravel.schemacompiler.model.TLHttpMethod;

import javafx.beans.value.ObservableValue;

public class SetRequestMethodAction extends DexStringAction {
    // private static Log log = LogFactory.getLog( SetAbstractAction.class );

    // Setting method to get will remove old payload
    private OtmActionFacet oldPayload = null;

    /**
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        if (subject instanceof OtmActionRequest)
            return subject.isEditable();
        return false;
    }

    public SetRequestMethodAction() {
        // actionType = DexActions.SETREQUESTMETHOD;
    }

    public String get() {
        return getSubject().getMethodString();
    }

    @Override
    public OtmActionRequest getSubject() {
        return (OtmActionRequest) otm;
    }

    public void set(String value) {
        getSubject().setMethodString( value );
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (!(subject instanceof OtmActionRequest))
            return false;
        otm = subject;
        return true;
    }

    /**
     * @see org.opentravel.dex.actions.string.DexStringAction#doIt(javafx.beans.value.ObservableValue, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public String doIt(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        // If it is a GET, save the payload for undo
        if (TLHttpMethod.valueOf( newValue ).equals( TLHttpMethod.GET ))
            oldPayload = getSubject().getPayloadActionFacet();

        return super.doIt( observable, oldValue, newValue );
    }

    /**
     * @see org.opentravel.dex.actions.string.DexStringAction#undoIt()
     */
    @Override
    public String undoIt() {
        if (oldPayload instanceof OtmActionFacet)
            getSubject().setPayloadType( oldPayload );
        return super.undoIt();
    }

    @Override
    public String toString() {
        return "Request method set to " + get();
    }

}
