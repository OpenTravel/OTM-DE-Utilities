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
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.event.ValueChangeEvent;

/**
 * @author dmh
 *
 */
public class OtmModelElementListener implements ModelElementListener {
    private static Log log = LogFactory.getLog( OtmModelElementListener.class );

    OtmObject otm;

    public OtmModelElementListener(OtmObject otmModelElement) {
        otm = otmModelElement;
    }

    public OtmObject get() {
        return otm;
    }

    @Override
    public void processOwnershipEvent(OwnershipEvent<?,?> event) {
        // log.debug( otm.getName() + " ownership event: " + event.getType() );
    }

    @Override
    public void processValueChangeEvent(ValueChangeEvent<?,?> event) {
        // log.debug( otm.getName() + " value change event: " + event.getType() );
        switch (event.getType()) {
            case NAME_MODIFIED:
                // if (event.getNewValue() instanceof String && otm.nameProperty() != null)
                // otm.nameProperty().setValue( (String) event.getNewValue() );
                break;
            case DOCUMENTATION_MODIFIED:
                // Only happens when the documentation container is changed, not it's contents.
                // Description and other documentation types must update their own observable properties in setters.
                break;
            default:
        }
    }

}
