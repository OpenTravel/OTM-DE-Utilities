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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.graphics.sprites.GraphicsUtils;
import org.opentravel.dex.controllers.graphics.sprites.MemberSprite;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import javafx.scene.input.MouseEvent;

/**
 * Graphics utility for containing property regions.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class BaseTypeRectangle extends PropertyRectangle {
    private static Log log = LogFactory.getLog( BaseTypeRectangle.class );

    /**
     * Render methods that create rectangles may set the event to run if the implement this interface.
     * <p>
     * Example: r.setOnMouseClicked( e -> manager.remove( this ) );
     */
    public abstract interface RectangleEventHandler {
        public void onRectangleClick(MouseEvent e);
    }

    private OtmTypeProvider baseType = null;

    /**
     * Create a base type property. Throws exception if base type is not a OtmTypeProvider.
     * 
     * @param parentSprite
     * @param member whose base type will be displayed as a property
     * @param width
     */
    public BaseTypeRectangle(MemberSprite<?> parentSprite, OtmLibraryMember member, double width) {
        super( parentSprite, width, "Extends", null, member.isEditable(), false );

        if (member.getBaseType() instanceof OtmTypeProvider) {
            baseType = (OtmTypeProvider) member.getBaseType();
            if (member.getBaseType() instanceof OtmTypeProvider)
                setProvider( (OtmTypeProvider) member.getBaseType() );
            this.providerLabel = member.getBaseType().getNameWithPrefix();

            if (member.getBaseType() instanceof OtmLibraryMember
                && !member.sameBaseNamespace( (OtmLibraryMember) member.getBaseType() ))
                this.providerColor = GraphicsUtils.CONNECTOR_COLOR;
        }

        // Compute the size
        draw( null, font );

        if (getConnectionPoint() != null && parent != null && baseType != null) {
            this.setOnMouseClicked( e -> parent.connect() );
            parent.add( this );
        }

        // log.debug( "Created base type rectangle:" + this );
    }

    public OtmLibraryMember get() {
        return (OtmLibraryMember) baseType;
    }

    @Override
    public String toString() {
        return "BaseType: " + label + " x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }
}
