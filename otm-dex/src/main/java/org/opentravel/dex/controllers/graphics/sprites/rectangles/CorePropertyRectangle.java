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

package org.opentravel.dex.controllers.graphics.sprites.rectangles;

import org.opentravel.dex.controllers.graphics.sprites.MemberSprite;
import org.opentravel.model.otmLibraryMembers.OtmCore;

/**
 * Graphics utility for containing property regions.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class CorePropertyRectangle extends PropertyRectangle {
    // private static Logger log = LogManager.getLogger( CorePropertyRectangle.class );

    private OtmCore core;

    public CorePropertyRectangle(OtmCore core, MemberSprite<OtmCore> parentSprite, double width) {
        super( parentSprite, width, "Simple", null, core.isEditable(), false );

        // Get type information
        setProvider( core.getAssignedType() );

        // Compute the size
        draw( null );
    }

    public OtmCore get() {
        return core;
    }

    @Override
    public String toString() {
        return "Core Property: " + label + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
