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

package org.opentravel.model.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.DexEditField;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.schemacompiler.model.TLParameter;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.ComboBox;

/**
 * OTM Object for Resource Action objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmParameter extends OtmResourceChildBase<TLParameter> implements OtmResourceChild {
    private static Log log = LogFactory.getLog( OtmParameter.class );

    public OtmParameter(TLParameter tla, OtmParameterGroup parent) {
        super( tla, parent );

        // tla.getFieldRefName();
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE_PARAMETER;
    }

    @Override
    public TLParameter getTL() {
        return (TLParameter) tlObject;
    }

    @Override
    public String getName() {
        return getTL().getFieldRefName();
    }

    @Override
    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        fields.add( new DexEditField( "Location", new ComboBox<String>(), 1 ) );
        return fields;
    }

}
