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

package org.opentravel.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.model.OtmObject;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author dmh
 *
 */
public class ValidationUtils {
    private static Log log = LogFactory.getLog( ValidationUtils.class );

    private ValidationUtils() {
        // NO-OP - static methods only. Do not instantiate this class.
    }

    /**
     * Get a string with warningCount/errorCount format.
     * 
     * @param findings
     * @return
     */
    public static String getCountsString(ValidationFindings findings) {
        String errMsg = "-/-";
        if (findings != null) {
            int warnings = findings.count( FindingType.WARNING );
            int errors = findings.count( FindingType.ERROR );
            errMsg = Integer.toString( warnings ) + "/" + Integer.toString( errors );
        }
        return errMsg;
    }

    public static String getMessagesAsString(ValidationFindings findings) {
        // log.debug("formatting " + findings.count() + " findings.");
        StringBuilder messages = new StringBuilder();
        if (findings != null)
            findings.getAllFindingsAsList().forEach(
                f -> messages.append( f.getFormattedMessage( FindingMessageFormat.MESSAGE_ONLY_FORMAT ) + "\n" ) );
        return messages.toString();
    }

    /**
     * Trim key to leave last dot and remainder of key
     * 
     * @param key
     * @return
     */
    private static String trim(String key) {
        if (key.isEmpty())
            return "";
        return key.substring( key.lastIndexOf( '.' ), key.length() );
    }

    /**
     * @param keyArray
     * @param findings
     * @return findings containing only those matching the veto keys if any.
     */
    public static ValidationFindings getRelevantFindings(String[] keyArray, ValidationFindings findings) {
        List<String> longKeys = new ArrayList<>( Arrays.asList( keyArray ) );
        List<String> keys = new ArrayList<>();
        longKeys.forEach( k -> keys.add( trim( k ) ) );

        ValidationFindings relevant = new ValidationFindings();
        if (findings != null && !findings.isEmpty())
            for (ValidationFinding f : findings.getAllFindingsAsList()) {
                String key = trim( f.getMessageKey() );
                if (keys.contains( key )) {
                    relevant.addFinding( f );
                } // else
                  // log.debug( "Unrelevant Finding: " + f.getMessageKey() );
            }
        return relevant;
    }

    /**
     * @param findings
     * @return true if the findings contains errors
     */
    public static boolean hasErrors(ValidationFindings findings) {
        return findings != null ? !findings.getFindingsAsList( FindingType.ERROR ).isEmpty() : false;
    }

    /**
     * Is subject vetoed? Force validation and try to match the keys.
     * 
     * @param vetoKeys
     * @param subject
     * @return true if any of the validation findings matches the veto keys.
     */
    public static boolean isVetoed(String[] vetoKeys, OtmObject subject) {
        boolean veto = false;
        ValidationFindings findings = null;
        if (!subject.isValid( true )) {
            findings = getRelevantFindings( vetoKeys, subject.getFindings() );
            if (!findings.isEmpty()) {
                veto = true;
            }
        }
        // if (subject instanceof OtmTypeUser)
        // log.debug( "Veto " + subject.getName() + ((OtmTypeUser) subject).getAssignedType() + "? " + veto );
        // if (veto)
        // log.debug( getMessagesAsString( findings ) );
        return veto;
    }
}
