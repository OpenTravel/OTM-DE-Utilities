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

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Component builder that constructs normal JavaFX components for use in live applications.
 */
public class DefaultNativeComponentBuilder implements NativeComponentBuilder {

    /**
     * @see org.opentravel.application.common.NativeComponentBuilder#newFileChooser(java.lang.String, java.io.File,
     *      javafx.stage.FileChooser.ExtensionFilter[])
     */
    @Override
    public FileChooserDelegate newFileChooser(String title, File initialDirectory,
        ExtensionFilter... extensionFilters) {
        FileChooserDelegate chooser = new FileChooserDelegate( new FileChooser() );
        File directory = initialDirectory;

        // Make sure the initial directory for the chooser exists
        while ((directory != null) && !directory.exists()) {
            directory = directory.getParentFile();
        }
        if (directory == null) {
            directory = new File( System.getProperty( "user.home" ) );
        }

        chooser.setTitle( title );
        chooser.setInitialDirectory( directory );

        for (ExtensionFilter filter : extensionFilters) {
            chooser.getExtensionFilters().add( filter );
        }
        return chooser;
    }

    /**
     * @see org.opentravel.application.common.NativeComponentBuilder#newDirectoryChooser(java.lang.String, java.io.File)
     */
    @Override
    public DirectoryChooserDelegate newDirectoryChooser(String title, File initialDirectory) {
        DirectoryChooserDelegate chooser = new DirectoryChooserDelegate( new DirectoryChooser() );
        File directory = initialDirectory;

        // Make sure the initial directory for the chooser exists
        while ((directory != null) && !directory.exists()) {
            directory = directory.getParentFile();
        }
        if (directory == null) {
            directory = new File( System.getProperty( "user.home" ) );
        }

        chooser.setTitle( title );
        chooser.setInitialDirectory( directory );
        return chooser;
    }

}
