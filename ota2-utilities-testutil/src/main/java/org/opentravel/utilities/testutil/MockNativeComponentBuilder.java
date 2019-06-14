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

package org.opentravel.utilities.testutil;

import static org.mockito.Mockito.mock;

import org.opentravel.application.common.DirectoryChooserDelegate;
import org.opentravel.application.common.FileChooserDelegate;
import org.opentravel.application.common.NativeComponentBuilder;

import java.io.File;

import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Provides mocking of native UI components during unit testing.
 */
public class MockNativeComponentBuilder implements NativeComponentBuilder {

    private FileChooserDelegate mockFileChooser;
    private DirectoryChooserDelegate mockDirectoryChooser;

    /**
     * Default constructor.
     */
    public MockNativeComponentBuilder() {
        mockFileChooser = mock( FileChooserDelegate.class );
        mockDirectoryChooser = mock( DirectoryChooserDelegate.class );
    }

    /**
     * @see org.opentravel.application.common.NativeComponentBuilder#newFileChooser(java.lang.String, java.io.File,
     *      javafx.stage.FileChooser.ExtensionFilter[])
     */
    @Override
    public FileChooserDelegate newFileChooser(String title, File initialDirectory,
        ExtensionFilter... extensionFilters) {
        return mockFileChooser;
    }

    /**
     * @see org.opentravel.application.common.NativeComponentBuilder#newDirectoryChooser(java.lang.String, java.io.File)
     */
    @Override
    public DirectoryChooserDelegate newDirectoryChooser(String title, File initialDirectory) {
        return mockDirectoryChooser;
    }

    /**
     * Returns the mock file chooser that will be returned from each call to 'newFileChooser()'.
     * 
     * @return FileChooserDelegate
     */
    public FileChooserDelegate getMockFileChooser() {
        return mockFileChooser;
    }

    /**
     * Returns the mock directory chooser that will be returned from each call to 'newDirectoryChooser()'.
     * 
     * @return DirectoryChooserDelegate
     */
    public DirectoryChooserDelegate getMockDirectoryChooser() {
        return mockDirectoryChooser;
    }

}
