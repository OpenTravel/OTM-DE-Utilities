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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.dex.controllers.graphics.sprites.DexSprite;
import org.opentravel.dex.controllers.graphics.sprites.DomainSprite;
import org.opentravel.dex.controllers.graphics.sprites.MemberSprite;
import org.opentravel.model.otmContainers.OtmLibrary;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

/**
 * 
 * @author dmh
 * @param <O>
 *
 */
public class LibraryRectangle extends Rectangle {
    private static Logger log = LogManager.getLogger( LibraryRectangle.class );

    private OtmLibrary library;
    private DexSprite parent;

    public abstract interface RectangleEventHandler {
        public void onRectangleClick(MouseEvent e);
    }

    public LibraryRectangle(DomainSprite parent, OtmLibrary library) {
        super( 0, 0, 0, 0 );
        this.library = library;
        this.parent = parent;
    }

    @Override
    public Rectangle draw(GraphicsContext gc) {
        // Draw Property Name and icon
        LabelRectangle lRect = new LabelRectangle( parent, library.getNameWithPrefix(),
            ImageManager.getImage( Icons.LIBRARY ), false, false, false ).draw( gc, x, y );
        this.setIfWider( lRect.getWidth() );
        this.setIfHigher( lRect.getHeight() );
        return this;
    }

    public String getBaseNamespace() {
        return library.getBaseNS();
    }

    public OtmLibrary getLibrary() {
        return library;
    }

    public boolean contains(MemberSprite<?> sprite) {
        OtmLibrary sLib = sprite.getMember().getLibrary();
        return sLib.getBaseNS().equals( library.getBaseNS() );
    }

    public static boolean contains(String baseNamespace, MemberSprite<?> sprite) {
        OtmLibrary sLib = sprite.getMember().getLibrary();
        return sLib.getBaseNS().equals( baseNamespace );
    }

    @Override
    public String toString() {
        return "Library Rectangle: " + library.getName() + "  x = " + x + " y = " + y + " width = " + width
            + " height = " + height;
    }

}
