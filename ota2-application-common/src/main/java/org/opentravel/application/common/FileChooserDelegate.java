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
     * Returns the dialog title.
     * 
     * @return String
     * @see javafx.stage.FileChooser#getTitle()
     */
    public String getTitle() {
        return fileChooser.getTitle();
    }

    /**
     * Assigns the dialog title.
     * 
     * @param title the title for the title
     * @see javafx.stage.FileChooser#setTitle(java.lang.String)
     */
    public void setTitle(String title) {
        fileChooser.setTitle( title );
    }

    /**
     * Returns the title property of the dialog.
     * 
     * @return StringProperty
     * @see javafx.stage.FileChooser#titleProperty()
     */
    public StringProperty titleProperty() {
        return fileChooser.titleProperty();
    }

    /**
     * Returns the initial directory of the chooser dialog.
     * 
     * @return File
     * @see javafx.stage.FileChooser#getInitialDirectory()
     */
    public File getInitialDirectory() {
        return fileChooser.getInitialDirectory();
    }

    /**
     * Assigns the initial directory of the chooser dialog.
     * 
     * @param folder the initial directory
     * @see javafx.stage.FileChooser#setInitialDirectory(java.io.File)
     */
    public void setInitialDirectory(File folder) {
        fileChooser.setInitialDirectory( folder );
    }

    /**
     * Returns the initial directory property of the chooser.
     * 
     * @return ObjectProperty&lt;File&gt;
     * @see javafx.stage.FileChooser#initialDirectoryProperty()
     */
    public ObjectProperty<File> initialDirectoryProperty() {
        return fileChooser.initialDirectoryProperty();
    }

    /**
     * Returns the initial filename selection for the chooser.
     * 
     * @return String
     * @see javafx.stage.FileChooser#getInitialFileName()
     */
    public String getInitialFileName() {
        return fileChooser.getInitialFileName();
    }

    /**
     * Assigns the initial filename selection for the chooser.
     * 
     * @param filename the initially selected filename
     * @see javafx.stage.FileChooser#setInitialFileName(java.lang.String)
     */
    public void setInitialFileName(String filename) {
        fileChooser.setInitialFileName( filename );
    }

    /**
     * Returns the initial filename property.
     * 
     * @return ObjectProperty&lt;String&gt;
     * @see javafx.stage.FileChooser#initialFileNameProperty()
     */
    public ObjectProperty<String> initialFileNameProperty() {
        return fileChooser.initialFileNameProperty();
    }

    /**
     * Returns the extension filters for the file dialog.
     * 
     * @return ObservableList&lt;ExtensionFilter&gt;
     * @see javafx.stage.FileChooser#getExtensionFilters()
     */
    public ObservableList<ExtensionFilter> getExtensionFilters() {
        return fileChooser.getExtensionFilters();
    }

    /**
     * Returns the selected extension filter from the dialog.
     * 
     * @return ExtensionFilter
     * @see javafx.stage.FileChooser#getSelectedExtensionFilter()
     */
    public ExtensionFilter getSelectedExtensionFilter() {
        return fileChooser.getSelectedExtensionFilter();
    }

    /**
     * Assigns the selected extension filter from the dialog.
     * 
     * @param filter the selected extension filter
     * @see javafx.stage.FileChooser#setSelectedExtensionFilter(javafx.stage.FileChooser.ExtensionFilter)
     */
    public void setSelectedExtensionFilter(ExtensionFilter filter) {
        fileChooser.setSelectedExtensionFilter( filter );
    }

    /**
     * Returns the selected extension filter property.
     * 
     * @return ObjectProperty&lt;ExtensionFilter&gt;
     * @see javafx.stage.FileChooser#selectedExtensionFilterProperty()
     */
    public ObjectProperty<ExtensionFilter> selectedExtensionFilterProperty() {
        return fileChooser.selectedExtensionFilterProperty();
    }

    /**
     * Displays the file chooser dialog that allows selection of a single file.
     * 
     * @param ownerWindow the owning window for the chooser dialog
     * @return File
     * @see javafx.stage.FileChooser#showOpenDialog(javafx.stage.Window)
     */
    public File showOpenDialog(Window ownerWindow) {
        return fileChooser.showOpenDialog( ownerWindow );
    }

    /**
     * Displays the file chooser dialog that allows selection of multiple files.
     * 
     * @param ownerWindow the owning window for the chooser dialog
     * @return File
     * @see javafx.stage.FileChooser#showOpenMultipleDialog(javafx.stage.Window)
     */
    public List<File> showOpenMultipleDialog(Window ownerWindow) {
        return fileChooser.showOpenMultipleDialog( ownerWindow );
    }

    /**
     * Displays the file chooser dialog used for selecting a single save file.
     * 
     * @param ownerWindow the owning window for the chooser dialog
     * @return File
     * @see javafx.stage.FileChooser#showSaveDialog(javafx.stage.Window)
     */
    public File showSaveDialog(Window ownerWindow) {
        return fileChooser.showSaveDialog( ownerWindow );
    }

    /**
     * Returns the hash code of the directory chooser.
     * 
     * @return int
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return fileChooser.hashCode();
    }

    /**
     * Returns true if the given chooser is equal to this one.
     * 
     * @param obj the other object with which to compare this one
     * @return boolean
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return fileChooser.equals( obj );
    }

    /**
     * Returns a string representation of the object.
     * 
     * @return boolean
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return fileChooser.toString();
    }

}
