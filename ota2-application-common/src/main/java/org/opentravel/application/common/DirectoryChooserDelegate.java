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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

/**
 * Delegate that facilitates mocking of the final <code>DirectoryChooser</code> class for testing purposes.
 */
public class DirectoryChooserDelegate {

    private DirectoryChooser directoryChooser;

    /**
     * Constructs a delegate wrapper for the given directory chooser.
     * 
     * @param directoryChooser the directory chooser instance to be wrapped
     */
    public DirectoryChooserDelegate(DirectoryChooser directoryChooser) {
        this.directoryChooser = directoryChooser;
    }

    /**
     * Returns the dialog title.
     * 
     * @return String
     * @see javafx.stage.DirectoryChooser#getTitle()
     */
    public String getTitle() {
        return directoryChooser.getTitle();
    }

    /**
     * Assigns the dialog title.
     * 
     * @param title the title for the title
     * @see javafx.stage.DirectoryChooser#setTitle(java.lang.String)
     */
    public void setTitle(String title) {
        directoryChooser.setTitle( title );
    }

    /**
     * Returns the title property of the dialog.
     * 
     * @return StringProperty
     * @see javafx.stage.DirectoryChooser#titleProperty()
     */
    public StringProperty titleProperty() {
        return directoryChooser.titleProperty();
    }

    /**
     * Returns the initial directory of the chooser dialog.
     * 
     * @return File
     * @see javafx.stage.DirectoryChooser#getInitialDirectory()
     */
    public File getInitialDirectory() {
        return directoryChooser.getInitialDirectory();
    }

    /**
     * Assigns the initial directory of the chooser dialog.
     * 
     * @param folder the initial directory
     * @see javafx.stage.DirectoryChooser#setInitialDirectory(java.io.File)
     */
    public void setInitialDirectory(File folder) {
        directoryChooser.setInitialDirectory( folder );
    }

    /**
     * Returns the initial directory property of the chooser.
     * 
     * @return ObjectProperty&lt;File&gt;
     * @see javafx.stage.DirectoryChooser#initialDirectoryProperty()
     */
    public ObjectProperty<File> initialDirectoryProperty() {
        return directoryChooser.initialDirectoryProperty();
    }

    /**
     * Displays the directory chooser dialog.
     * 
     * @param ownerWindow the owning window for the chooser dialog
     * @return File
     * @see javafx.stage.DirectoryChooser#showDialog(javafx.stage.Window)
     */
    public File showDialog(Window ownerWindow) {
        return directoryChooser.showDialog( ownerWindow );
    }

    /**
     * Returns the hash code of the directory chooser.
     * 
     * @return int
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return directoryChooser.hashCode();
    }

    /**
     * Returns true if the given chooser is equal to this one.
     * 
     * @param obj the other object with which to compare this one
     * @return boolean
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return directoryChooser.equals( obj );
    }

    /**
     * Returns a string representation of the object.
     * 
     * @return boolean
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return directoryChooser.toString();
    }

}
