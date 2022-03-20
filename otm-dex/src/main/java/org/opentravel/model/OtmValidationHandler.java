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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ValidationUtils;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.ImageView;

/**
 * 
 * @author Dave Hollander
 * 
 */
public class OtmValidationHandler {
    private static Logger log = LogManager.getLogger( OtmValidationHandler.class );

    public static ValidationFindings isValid(TLModelElement tl) {
        if (tl == null)
            throw new IllegalStateException( "Tried to validation with null TL object." );
        ValidationFindings sFindings = null;
        boolean deep = false;
        try {
            sFindings = TLModelCompileValidator.validateModelElement( tl, deep );
        } catch (Exception e) {
            sFindings = null;
            // log.debug( "Validation on " + tl.getValidationIdentity() + " threw error: " + e.getLocalizedMessage() );
        }
        // log.debug(sFindings != null ? sFindings.count() + " sFindings found" : " null" + " findings found.");
        return sFindings;
    }

    private OtmObject obj;
    private ValidationFindings findings = null;
    private StringProperty validationProperty = null;
    private ObjectProperty<ImageView> validationImageProperty = null;

    /**
     * Construct model element. Set its TL object and add a listener.
     * 
     * @param tl
     */
    public OtmValidationHandler(OtmObject object) {
        if (object == null)
            throw new IllegalArgumentException( "Must have a tl element to create facade." );
        obj = object;
    }

    public ValidationFindings getFindings() {
        if (findings == null) {
            isValid( true );
            // log.debug( "Getting findings for " + this );
        }
        return findings;
    }

    public TLModelElement getTL() {
        return obj.getTL();
    }

    public String getValidationFindingsAsString() {
        String msg = "Validation Findings: \n";
        String f = ValidationUtils.getMessagesAsString( getFindings() );
        if (obj.isInherited())
            msg += "Not validated here because it is inherited.";
        else if (!f.isEmpty())
            msg += f;
        else
            msg += "No warnings or errors.";
        return msg;
    }

    public boolean isValid(boolean refresh) {
        if (getTL() == null)
            throw new IllegalStateException( "Tried to validation with null TL object." );

        // Inherited objects should not be validated
        if (obj.isInherited())
            return true;

        if (findings == null || refresh) {
            validationProperty = null;
            validationImageProperty = null;
            findings = isValid( getTL() );

            if (validationProperty != null)
                validationProperty.setValue( ValidationUtils.getCountsString( findings ) );
            if (validationImageProperty() != null)
                validationImageProperty().setValue( validationImage() );
        }
        // log.debug( "Validated " + this.obj );
        // if (findings != null && findings.count() > 0)
        // log.debug( " findings: " + findings.count() + " findings found" );
        // Model change events make the image and tool tip update
        return findings == null || findings.isEmpty();
    }

    public void refresh() {
        findings = null;
        validationImageProperty = null;
        validationProperty = null;
    }

    public ImageView validationImage() {
        if (obj.isInherited())
            return null;

        if (findings != null) {
            if (findings.hasFinding( FindingType.ERROR ))
                return ImageManager.get( ImageManager.Icons.V_ERROR );
            if (findings.hasFinding( FindingType.WARNING ))
                return ImageManager.get( ImageManager.Icons.V_WARN );
        }
        return ImageManager.get( ImageManager.Icons.V_OK );
    }

    public ObjectProperty<ImageView> validationImageProperty() {
        if (validationImageProperty == null)
            validationImageProperty = new SimpleObjectProperty<>( validationImage() );
        return validationImageProperty;
    }

    public StringProperty validationProperty() {
        if (validationProperty == null)
            validationProperty = new ReadOnlyStringWrapper( ValidationUtils.getCountsString( getFindings() ) );
        return validationProperty;
    }
}
