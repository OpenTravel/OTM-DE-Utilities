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

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.concurrent.TimeoutException;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Base class for JavaFX UI tests.
 */
public abstract class AbstractFxTest extends AbstractRepositoryTest {

    protected static File repositoryConfig =
        new File( System.getProperty( "user.dir" ) + "/src/test/resources/ota2-repository-config.xml" );
    protected static RepositoryManager repoManager;

    protected Stage primaryStage;
    protected AbstractOTMApplication application;
    protected FileChooserDelegate mockFileChooser;
    protected DirectoryChooserDelegate mockDirectoryChooser;

    @Rule
    public OtmFxRobot robot = new OtmFxRobot( stage -> {
        Class<? extends AbstractOTMApplication> applicationClass = getApplicationClass();
        try {
            MockNativeComponentBuilder componentBuilder = new MockNativeComponentBuilder();

            if (repoManager != null) {
                Constructor<? extends AbstractOTMApplication> constructor =
                    applicationClass.getConstructor( RepositoryManager.class );
                application = constructor.newInstance( repoManager );

            } else {
                application = applicationClass.getConstructor().newInstance();
            }
            primaryStage = stage;
            application.start( stage );
            application.getController().setNativeComponentBuilder( componentBuilder );
            mockFileChooser = componentBuilder.getMockFileChooser();
            mockDirectoryChooser = componentBuilder.getMockDirectoryChooser();

        } catch (Exception e) {
            throw new AssertionError( "Error initializing JavaFX application: " + applicationClass.getName(), e );
        }
    }, getBackgroundTaskNodeQuery() );

    @After
    public void closeApplication() throws TimeoutException {
        FxToolkit.setupFixture(
            () -> primaryStage.fireEvent( new WindowEvent( primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST ) ) );
        System.out.println( "After: close application ran." );
    }

    /**
     * Returns the OTM utility application class that is being tested.
     * 
     * @return Class&lt;? extends AbstractOTMApplication&gt;
     */
    protected abstract Class<? extends AbstractOTMApplication> getApplicationClass();

    /**
     * Returns the node query to use for locating the node that will indicate the existence or completion of a
     * background task. The node that is specified should be one that is always disabled while the task is running and
     * enabled after it has been completed.
     *
     * @return String
     */
    protected abstract String getBackgroundTaskNodeQuery();

}
