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

package org.opentravel.application.common;

import java.io.File;

import javafx.stage.FileChooser.ExtensionFilter;

/**
 * In JavaFX applications some UI components are created using native components that cannot be rendered or controlled
 * during automated testing. This interface allows the creation of those native components to be mocked during testing.
 */
public interface NativeComponentBuilder {

    /**
     * Returns a new file chooser that is configured for the selection of specific types of files.
     * 
     * @param title the title of the new file chooser
     * @param initialDirectory the initial directory location for the chooser
     * @param extensionFilters two-element arrays that specify the file extension and extension description (in that
     *        order)
     * @return FileChooserDelegate
     */
    FileChooserDelegate newFileChooser(String title, File initialDirectory, ExtensionFilter... extensionFilters);

    /**
     * Returns a new directory chooser instance.
     * 
     * @param title the title of the new directory chooser
     * @param initialDirectory the initial directory location for the chooser
     * @return DirectoryChooserDelegate
     */
    DirectoryChooserDelegate newDirectoryChooser(String title, File initialDirectory);

}
