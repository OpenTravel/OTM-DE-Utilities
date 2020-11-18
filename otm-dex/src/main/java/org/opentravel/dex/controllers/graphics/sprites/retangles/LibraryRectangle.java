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
import org.opentravel.dex.controllers.graphics.sprites.MemberSprite;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import javafx.scene.input.MouseEvent;

/**
 * Graphics utility for containing regions (x, y, width, height). A rectangle does <b>not</b> have a canvas.
 * <p>
 * Sub-types have contents that can be drawn into the rectangle. These rectangles will compute their size when
 * constructed and when drawn with a null GraphicsContext (GC). A rectangle may be mouse click-able if the parent sprite
 * is passed when constructing the rectangle.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class LibraryRectangle extends Rectangle {
    private static Log log = LogFactory.getLog( LibraryRectangle.class );

    private OtmLibrary library;

    /**
     * Render methods that create rectangles may set the event to run if the implement this interface.
     * <p>
     * Example: r.setOnMouseClicked( e -> manager.remove( this ) );
     */
    public abstract interface RectangleEventHandler {
        public void onRectangleClick(MouseEvent e);
    }

    public LibraryRectangle(MemberSprite<?> sprite) {
        super( 0, 0, 0, 0 );
        OtmLibraryMember member = sprite.getMember();
        library = member.getLibrary();
    }

    public LibraryRectangle(OtmLibrary library) {
        super( 0, 0, 0, 0 );
        this.library = library;
    }

    public String getBaseNamespace() {
        return library.getBaseNamespace();
    }

    public OtmLibrary getLibrary() {
        return library;
    }

    public boolean contains(MemberSprite<?> sprite) {
        OtmLibrary sLib = sprite.getMember().getLibrary();
        return sLib.getBaseNamespace().equals( library.getBaseNamespace() );
    }

    public static boolean contains(String baseNamespace, MemberSprite<?> sprite) {
        OtmLibrary sLib = sprite.getMember().getLibrary();
        return sLib.getBaseNamespace().equals( baseNamespace );
    }

    public String toString() {
        return "Library Rectangle: x = " + x + " y = " + y + " width = " + width + " height = " + height;
    }

}
