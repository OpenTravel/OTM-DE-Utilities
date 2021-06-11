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

package org.opentravel.dex.controllers;

import org.apache.http.annotation.Obsolete;

/**
 * Abstract interface for all included OTM-DE FX filter controllers. These controllers must be able to respond if an
 * object is selected or not.
 * <p>
 * <ul>
 * <li>The FXML file for this controller must be included into another FXML file.
 * <li>The controller must have the same name as the FXML file with "Controller" as a suffix.
 * <li>The controller must be declared with an @FXML in the containing controller.
 * </ul>
 * 
 * @author dmh
 *
 */
@Obsolete
public interface DexFilterController<T> extends DexController {

    /**
     * Test to determine if the object is selected by the filter(s).
     * 
     * @param object the object to test
     * @return true if the object matches the filter criteria, false otherwise.
     */
    public boolean isSelected(T object);

    /**
     * Do any post-initialization configuration needed by this controller now that it has full access to program
     * resource. Is invoked when the included controller is
     * {@link DexMainControllerBase#addIncludedController(DexFilterController)} is called.
     * <p>
     * Set the main parent controller. Included controllers will not have access to the parent controller until this
     * method is called. An illegalState exception should be thrown if the parent controller is needed for posting data
     * into the view before the parent is set.
     * <p>
     * This method should retrieve all of the resources it needs from the parent such as the stage or image and model
     * managers.
     * 
     * @param parent
     */
    public void configure(DexMainController parent);

    /**
     * Initialize is called by the FXML loader when the FXML file is loaded. These methods must make the controller
     * ready to "Post" to their view components. Trees must be initialized, table columns set, etc.
     * <p>
     * This method should verify that all views and fields have been injected correctly and throw
     * illegalArgumentException if not.
     * <p>
     * Note: parent controller is not known and business data will not be available when this is called.
     */
    @Override
    public void initialize();

}
