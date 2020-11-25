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

/**
 * Graphics Display Object (Sprite) for containing OTM Emumeration object.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class ResourceSprite extends MemberSprite<OtmResource> implements DexSprite {

    PropertyRectangle subjectRectangle = null;
    private double margin;
    private double dx;

    public ResourceSprite(OtmResource member, SpriteManager manager) {
        super( member, manager );
        dx = settingsManager.getOffset( Offsets.ID );
        margin = settingsManager.getMargin( Margins.FACET );
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
    public DexSprite connectSubject() {
        if (member.getSubject() == null)
            return null;

        DexSprite subjectSprite = manager.get( (OtmLibraryMember) member.getSubject() );
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
    public Rectangle drawContents(GraphicsContext gc, final double x, final double y) {
        Rectangle rect = null;

        double width = getBoundaries().getWidth();
        double fy = y;

        // Display subject as property
        if (member.getSubject() != null) {
            subjectRectangle = new ResourceSubjectRectangle( this, getMember(), width );
            subjectRectangle.set( x + dx, y ).draw( gc );
            fy += subjectRectangle.getHeight() + margin;
            width = computeWidth( width, subjectRectangle, 0 );
        } else {
            rect = new LabelRectangle( this, "Abstract", null, false, false, false ).draw( gc, x + dx, fy );
            fy += rect.getHeight() + margin;
            width = computeWidth( width, rect, 0 );
        }

        // Display Actions
        if (!isCollapsed())
            for (OtmAction action : member.getActions()) {
                rect = new FacetRectangle( action, this, width - margin );
                width = draw( rect, gc, width, x, 0, fy );
                fy += rect.getHeight() + margin;
            }

        // Return the enclosing rectangle
        return new Rectangle( x, y, width + margin, fy - y );
    }

}
