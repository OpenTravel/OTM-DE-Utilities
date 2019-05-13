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

import org.junit.After;
import org.junit.Rule;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.application.common.DirectoryChooserDelegate;
import org.opentravel.application.common.FileChooserDelegate;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.testutil.AbstractRepositoryTest;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationRule;

import java.io.File;
import java.lang.reflect.Constructor;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Base class for JavaFX UI tests.
 */
public abstract class AbstractFxTest extends AbstractRepositoryTest {

    protected static File repositoryConfig =
        new File( System.getProperty( "user.dir" ), "/src/test/resources/ota2-repository-config.xml" );
    protected static RepositoryManager repoManager;

    protected Stage primaryStage;
    protected FileChooserDelegate mockFileChooser;
    protected DirectoryChooserDelegate mockDirectoryChooser;

    @Rule
    public ApplicationRule robot = new ApplicationRule( stage -> {
        Class<? extends AbstractOTMApplication> applicationClass = getApplicationClass();
        try {
            MockNativeComponentBuilder componentBuilder = new MockNativeComponentBuilder();
            AbstractOTMApplication application;

            if (repoManager != null) {
                Constructor<? extends AbstractOTMApplication> constructor =
                    applicationClass.getConstructor( RepositoryManager.class );
                application = constructor.newInstance( repoManager );

            } else {
                application = applicationClass.newInstance();
            }
            primaryStage = stage;
            application.start( stage );
            application.getController().setNativeComponentBuilder( componentBuilder );
            mockFileChooser = componentBuilder.getMockFileChooser();
            mockDirectoryChooser = componentBuilder.getMockDirectoryChooser();

        } catch (Exception e) {
            throw new AssertionError( "Error initializing JavaFX application: " + applicationClass.getName(), e );
        }
    } );

    @After
    public void closeApplication() throws Exception {
        FxToolkit.setupFixture(
            () -> primaryStage.fireEvent( new WindowEvent( primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST ) ) );
    }

    protected abstract Class<? extends AbstractOTMApplication> getApplicationClass();

}
