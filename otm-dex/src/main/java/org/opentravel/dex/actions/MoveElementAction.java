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

package org.opentravel.dex.actions;

import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmProperties.OtmElement;
import org.opentravel.model.otmProperties.OtmProperty;


/**
 * Actions that move an element property up or down.
 * <p>
 * 
 * @author dmh
 *
 */
public class MoveElementAction extends DexRunAction {
    public enum MoveDirection {
        UP, DOWN
    }

    public static boolean isEnabled(OtmObject otm) {
        if (otm instanceof OtmElement) {
            if (otm.getOwningMember() == null)
                return false;
            if (((OtmProperty) otm).getParent() == null)
                return false;
        } else
            return false;
        return otm.isEditable();
    }

    public static boolean isEnabled(OtmObject subject, OtmObject value) {
        return isEnabled( subject );
    }

    private OtmElement<?> movedElement = null;
    private MoveDirection direction;

    public MoveElementAction() {
        // Reflection constructor
    }

    public Object doIt() {
        return null;
    }

    public Object doIt(MoveDirection direction) {
        boolean results = false;
        if (isEnabled( otm )) {
            movedElement = getSubject();
            this.direction = direction;
            if (direction == MoveDirection.UP)
                results = movedElement.moveUp();
            else
                results = movedElement.moveDown();
        }
        return results ? get() : null;
    }

    @Override
    public Object doIt(Object data) {
        if (data instanceof MoveDirection)
            return doIt( (MoveDirection) data );
        return doIt();
    }

    @Override
    protected Object get() {
        return movedElement;
    }

    @Override
    public OtmResourceChild undoIt() {
        if (movedElement != null && direction != null)
            if (direction == MoveDirection.UP)
                movedElement.moveDown();
            else
                movedElement.moveUp();
        movedElement = null;
        return null;
    }

    /**
     * @see org.opentravel.dex.actions.DexAction#setSubject(org.opentravel.model.OtmObject)
     */
    @Override
    public boolean setSubject(OtmObject subject) {
        if (subject instanceof OtmElement) {
            otm = subject;
            return true;
        }
        return false;
    }

    @Override
    public OtmElement<?> getSubject() {
        return otm instanceof OtmElement ? (OtmElement<?>) otm : null;
    }

    @Override
    public String toString() {
        return "Moved " + getSubject() + " " + direction;
    }
}
