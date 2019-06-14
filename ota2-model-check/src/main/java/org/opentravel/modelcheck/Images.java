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

package org.opentravel.modelcheck;

import javafx.scene.image.Image;

/**
 * Constant definitions for images that are used in the launcher application.
 */
public class Images {

    public static final Image launcherIcon =
        new Image( Images.class.getResourceAsStream( "/images/otm_model_check.png" ) );

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private Images() {}

}
