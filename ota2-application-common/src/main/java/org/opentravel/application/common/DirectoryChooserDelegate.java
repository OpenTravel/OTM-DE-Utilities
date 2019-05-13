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
     * @see javafx.stage.DirectoryChooser#setTitle(java.lang.String)
     */
    public final void setTitle(String value) {
        directoryChooser.setTitle( value );
    }

    /**
     * @see javafx.stage.DirectoryChooser#getTitle()
     */
    public final String getTitle() {
        return directoryChooser.getTitle();
    }

    /**
     * @see javafx.stage.DirectoryChooser#titleProperty()
     */
    public final StringProperty titleProperty() {
        return directoryChooser.titleProperty();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return directoryChooser.hashCode();
    }

    /**
     * @see javafx.stage.DirectoryChooser#setInitialDirectory(java.io.File)
     */
    public final void setInitialDirectory(File value) {
        directoryChooser.setInitialDirectory( value );
    }

    /**
     * @see javafx.stage.DirectoryChooser#getInitialDirectory()
     */
    public final File getInitialDirectory() {
        return directoryChooser.getInitialDirectory();
    }

    /**
     * @see javafx.stage.DirectoryChooser#initialDirectoryProperty()
     */
    public final ObjectProperty<File> initialDirectoryProperty() {
        return directoryChooser.initialDirectoryProperty();
    }

    /**
     * @see javafx.stage.DirectoryChooser#showDialog(javafx.stage.Window)
     */
    public File showDialog(Window ownerWindow) {
        return directoryChooser.showDialog( ownerWindow );
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return directoryChooser.equals( obj );
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return directoryChooser.toString();
    }

}
