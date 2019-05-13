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
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/**
 * Delegate that facilitates mocking of the final <code>FileChooser</code> class for testing purposes.
 */
public class FileChooserDelegate {

    private FileChooser fileChooser;

    /**
     * Constructs a delegate wrapper for the given file chooser.
     * 
     * @param fileChooser the file chooser instance to wrap
     */
    public FileChooserDelegate(FileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return fileChooser.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return fileChooser.equals( obj );
    }

    /**
     * @see javafx.stage.FileChooser#setTitle(java.lang.String)
     */
    public final void setTitle(String value) {
        fileChooser.setTitle( value );
    }

    /**
     * @see javafx.stage.FileChooser#getTitle()
     */
    public final String getTitle() {
        return fileChooser.getTitle();
    }

    /**
     * @see javafx.stage.FileChooser#titleProperty()
     */
    public final StringProperty titleProperty() {
        return fileChooser.titleProperty();
    }

    /**
     * @see javafx.stage.FileChooser#setInitialDirectory(java.io.File)
     */
    public final void setInitialDirectory(File value) {
        fileChooser.setInitialDirectory( value );
    }

    /**
     * @see javafx.stage.FileChooser#getInitialDirectory()
     */
    public final File getInitialDirectory() {
        return fileChooser.getInitialDirectory();
    }

    /**
     * @see javafx.stage.FileChooser#initialDirectoryProperty()
     */
    public final ObjectProperty<File> initialDirectoryProperty() {
        return fileChooser.initialDirectoryProperty();
    }

    /**
     * @see javafx.stage.FileChooser#setInitialFileName(java.lang.String)
     */
    public final void setInitialFileName(String value) {
        fileChooser.setInitialFileName( value );
    }

    /**
     * @see javafx.stage.FileChooser#getInitialFileName()
     */
    public final String getInitialFileName() {
        return fileChooser.getInitialFileName();
    }

    /**
     * @see javafx.stage.FileChooser#initialFileNameProperty()
     */
    public final ObjectProperty<String> initialFileNameProperty() {
        return fileChooser.initialFileNameProperty();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return fileChooser.toString();
    }

    /**
     * @see javafx.stage.FileChooser#getExtensionFilters()
     */
    public ObservableList<ExtensionFilter> getExtensionFilters() {
        return fileChooser.getExtensionFilters();
    }

    /**
     * @see javafx.stage.FileChooser#selectedExtensionFilterProperty()
     */
    public final ObjectProperty<ExtensionFilter> selectedExtensionFilterProperty() {
        return fileChooser.selectedExtensionFilterProperty();
    }

    /**
     * @see javafx.stage.FileChooser#setSelectedExtensionFilter(javafx.stage.FileChooser.ExtensionFilter)
     */
    public final void setSelectedExtensionFilter(ExtensionFilter filter) {
        fileChooser.setSelectedExtensionFilter( filter );
    }

    /**
     * @see javafx.stage.FileChooser#getSelectedExtensionFilter()
     */
    public final ExtensionFilter getSelectedExtensionFilter() {
        return fileChooser.getSelectedExtensionFilter();
    }

    /**
     * @see javafx.stage.FileChooser#showOpenDialog(javafx.stage.Window)
     */
    public File showOpenDialog(Window ownerWindow) {
        return fileChooser.showOpenDialog( ownerWindow );
    }

    /**
     * @see javafx.stage.FileChooser#showOpenMultipleDialog(javafx.stage.Window)
     */
    public List<File> showOpenMultipleDialog(Window ownerWindow) {
        return fileChooser.showOpenMultipleDialog( ownerWindow );
    }

    /**
     * @see javafx.stage.FileChooser#showSaveDialog(javafx.stage.Window)
     */
    public File showSaveDialog(Window ownerWindow) {
        return fileChooser.showSaveDialog( ownerWindow );
    }

}
