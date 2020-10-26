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

package org.opentravel.dex.controllers.graphics.sprites;

import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Margins;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager.Offsets;
import org.opentravel.dex.controllers.graphics.sprites.connections.ResourceTypeConnection;
import org.opentravel.dex.controllers.graphics.sprites.retangles.FacetRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.LabelRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.PropertyRectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.Rectangle;
import org.opentravel.dex.controllers.graphics.sprites.retangles.ResourceSubjectRectangle;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.resource.OtmAction;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;

/**
 * Graphics Display Object (Sprite) for containing OTM Emumeration object.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class ResourceSprite extends MemberSprite<OtmResource> implements DexSprite<OtmLibraryMember> {

    PropertyRectangle subjectRectangle = null;

    public ResourceSprite(OtmResource member, SpriteManager manager, SettingsManager settingsManager) {
        super( member, manager, settingsManager );
    }

    /**
     * Connect this resource sprite to its subject.
     * <p>
     * Needed to allow both base and subject property connections. Only sprites for objects that have multiple property
     * relationships need create additional connect methods.
     * 
     * @param subject
     * @return
     */
    // @Override
    public DexSprite<?> connectSubject() {
        if (member.getSubject() == null)
            return null;

        DexSprite<?> subjectSprite = manager.get( (OtmLibraryMember) member.getSubject() );
        if (subjectSprite == null) {
            subjectSprite = manager.add( (OtmLibraryMember) member.getSubject(), getColumn().getNext() );
        } else
            subjectSprite.setCollapsed( !subjectSprite.isCollapsed() );
        if (subjectSprite != null) {
            manager.addAndDraw( new ResourceTypeConnection( this, subjectSprite ) );
            subjectSprite.getCanvas().toFront();
            subjectSprite.refresh();
        }
        return subjectSprite;
    }

    public Point2D getSubjectCP() {
        return subjectRectangle.getConnectionPoint();
    }

    @Override
    public Rectangle drawContents(GraphicsContext gc, Font font, final double x, final double y) {
        Rectangle rect = null;

        double dx = settingsManager.getOffset( Offsets.ID );
        double width = getBoundaries().getWidth();
        double margin = settingsManager.getMargin( Margins.FACET );
        double fy = y + margin;

        // Display subject as property
        if (member.getSubject() != null) {
            // subjectRectangle = new PropertyRectangle( this, getMember(), width );
            subjectRectangle = new ResourceSubjectRectangle( this, getMember(), width );
            subjectRectangle.set( x + dx, y ).draw( gc, false );
            fy += subjectRectangle.getHeight() + margin;
            width = computeWidth( gc == null, width, subjectRectangle, 0 );
        } else {
            rect = new LabelRectangle( this, "Abstract", null, false, false, false ).draw( gc, x + dx, fy );
            // rect = GraphicsUtils.drawLabel( "Abstract", null, false, false, gc, font, x + dx, fy );
            // rect.set( x + dx, fy ).draw( gc, false );
            fy += rect.getHeight() + margin;
            width = computeWidth( gc == null, width, rect, 0 );
        }

        // Display Actions
        if (!isCollapsed())
            for (OtmAction action : member.getActions()) {
                rect = new FacetRectangle( action, this, width - margin );
                rect.set( x + dx, fy ).draw( gc, true );
                fy += rect.getHeight() + margin;
                width = computeWidth( gc == null, width, rect, 0 );
            }

        // rect = GraphicsUtils.drawLabel( "TODO", null, gc, font, x + dx, fy );
        // width = computeWidth( gc == null, width, rect, 0 );
        // fy += rect.getHeight();

        // if (!isCollapsed()) {
        // Demos.postSmileyFace( gc, x + dx, fy );
        // width += 300;
        // fy += 300;
        // }


        // Show base type
        // // Show open's Other property
        // if (getMember() instanceof OtmEnumerationOpen) {
        // rect = GraphicsUtils.drawLabel( "Other", null, false, gc, font, x + dx, fy );
        // fy += rect.getHeight() + margin;
        // }
        //
        // // Show values
        // if (!isCollapsed()) {
        // rect = new FacetRectangle( getMember(), this, width - dx );
        // rect.set( x + dx, fy ).draw( gc, true );
        // fy += rect.getHeight() + margin;
        // width = computeWidth( compute, width, rect, dx );
        // }
        // // Return the enclosing rectangle
        // // Rectangle sRect = new Rectangle( x, y, width + margin, fy - y );
        // // log.debug( "Drew choice contents into " + sRect );
        // // fRect.draw( gc, false );
        // // return sRect;
        return new Rectangle( x, y, width + margin, fy - y );
    }

}
