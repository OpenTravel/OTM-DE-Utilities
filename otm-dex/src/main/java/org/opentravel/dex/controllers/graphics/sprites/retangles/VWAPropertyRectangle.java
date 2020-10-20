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

package org.opentravel.dex.controllers.graphics.sprites.retangles;

import org.opentravel.dex.controllers.graphics.sprites.DexSprite;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;

/**
 * Graphics utility for containing property regions.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class VWAPropertyRectangle extends PropertyRectangle {
    // private static Log log = LogFactory.getLog( VWAPropertyRectangle.class );

    private OtmValueWithAttributes vwa;

    public VWAPropertyRectangle(OtmValueWithAttributes vwa, DexSprite<OtmLibraryMember> parentSprite, double width) {
        super( parentSprite, width, "Value", null, vwa.isEditable(), false );

        // Get type information
        setProvider( vwa.getAssignedType() );

        // Compute the size
        draw( null, font );
    }

    public OtmValueWithAttributes get() {
        return vwa;
    }

    @Override
    public String toString() {
        return "VWA Property: " + label + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
