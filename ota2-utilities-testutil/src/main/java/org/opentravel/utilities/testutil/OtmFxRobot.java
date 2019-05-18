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

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

import org.hamcrest.Matcher;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationAdapter;
import org.testfx.framework.junit.ApplicationFixture;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.robot.Motion;
import org.testfx.service.query.PointQuery;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javafx.geometry.Bounds;
import javafx.geometry.HorizontalDirection;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Wraps an existing <code>FxRobot</code> to provide additional functions.
 */
public class OtmFxRobot extends FxRobot implements ApplicationFixture, TestRule {

    public static final int DEFAULT_BACKGROUND_TASK_TIMEOUT = 10;

    private final Consumer<Stage> start;
    private final Consumer<Stage> stop;

    private Stage stage;
    private String backgroundTaskNodeQuery;

    /**
     * Constructor that specifies the consumer that will initialize the stage of the JavaFx application.
     * 
     * @param start the consumer to use when initializing the application stage
     */
    public OtmFxRobot(Consumer<Stage> start, String backgroundTaskNodeQuery) {
        this( start, doNothing -> {
        }, backgroundTaskNodeQuery );
    }

    /**
     * Constructor that specifies the consumer that will initialize and shut down the stage of the JavaFx application.
     * 
     * @param start the consumer to use when initializing the application stage
     * @param stop the consumer to use when shutting down the application stage
     */
    public OtmFxRobot(Consumer<Stage> start, Consumer<Stage> stop, String backgroundTaskNodeQuery) {
        this.backgroundTaskNodeQuery = backgroundTaskNodeQuery;
        this.start = start;
        this.stop = stop;
    }

    /**
     * Returns the node query to use for locating the node that will indicate the existence or completion of a
     * background task. The node that is specified should be one that is always disabled while the task is running and
     * enabled after it has been completed.
     *
     * @return String
     */
    public String getBackgroundTaskNodeQuery() {
        return backgroundTaskNodeQuery;
    }

    /**
     * Assigns the node query to use for locating the node that will indicate the existence or completion of a
     * background task. The node that is specified should be one that is always disabled while the task is running and
     * enabled after it has been completed.
     *
     * @param backgroundTaskNodeQuery the TestFX node query string to assign
     */
    public void setBackgroundTaskNodeQuery(String backgroundTaskNodeQuery) {
        this.backgroundTaskNodeQuery = backgroundTaskNodeQuery;
    }

    /**
     * Waits for a background task to complete.
     * 
     * @return OtmFxRobot
     */
    public OtmFxRobot waitForBackgroundTask() {
        waitForBackgroundTask( DEFAULT_BACKGROUND_TASK_TIMEOUT );
        return this;
    }

    /**
     * Waits for a background task to complete using a non-default timeout value.
     * 
     * @param timeout the timeout interval to wait for task completion
     * @return OtmFxRobot
     */
    public OtmFxRobot waitForBackgroundTask(int timeout) {
        return waitForBackgroundTask( backgroundTaskNodeQuery, timeout );
    }

    /**
     * Waits for a background task using a non-default UI node to determine the task's completion.
     * 
     * @param nodeQuery the timeout interval to wait for task completion
     * @return OtmFxRobot
     */
    public OtmFxRobot waitForBackgroundTask(String nodeQuery) {
        return waitForBackgroundTask( nodeQuery, DEFAULT_BACKGROUND_TASK_TIMEOUT );
    }

    /**
     * Waits for a background task by using a UI node's enabled status to determine the task's completion.
     * 
     * @param nodeQuery the TestFX query for the UI component that will determine the task's completion
     * @param timeout the number of seconds to wait before timing out
     * @return OtmFxRobot
     */
    public OtmFxRobot waitForBackgroundTask(String nodeQuery, int timeout) {
        try {
            WaitForAsyncUtils.waitFor( timeout, TimeUnit.SECONDS,
                (Callable<Boolean>) () -> NodeMatchers.isEnabled().matches( lookup( nodeQuery ).query() ) );

        } catch (TimeoutException e) {
            // No action - return false
        }
        waitForFxEvents();
        return this;
    }

    /**
     * Writes the text to the specified text control after first erasing the existing text value.
     * 
     * @param textInputQuery the node query for the JavaFX text input control
     * @param text the text to be typed
     * @return OtmFxRobot
     */
    public OtmFxRobot write(String textInputQuery, String text) {
        write( textInputQuery, text, true );
        return this;
    }

    /**
     * Writes the text to the specified text control after optionally erasing the existing text value.
     * 
     * @param textInputQuery the node query for the JavaFX text input control
     * @param text the text to be typed
     * @param eraseExistingText flag indicating whether the existing text should be deleted before writing
     * @return OtmFxRobot
     */
    public OtmFxRobot write(String textInputQuery, String text, boolean eraseExistingText) {
        Node control = lookup( textInputQuery ).query();

        clickOn( control );

        if (eraseExistingText && (control instanceof TextInputControl)) {
            int charCount = ((TextInputControl) control).getText().length();

            for (int i = 0; i < charCount; i++) {
                type( KeyCode.BACK_SPACE );
            }
        }
        write( text );
        return this;
    }

    /**
     * Expands and navigates all members of the specified tree view on the UI.
     * 
     * @param treeviewQuery the JavaFX query ID of the tree view to navigate
     * @return OtmFxRobot
     */
    public OtmFxRobot navigateTreeView(String treeviewQuery) {
        TreeView<?> treeView = lookup( treeviewQuery ).query();
        TreeItem<?> lastSelectedItem = null;
        TreeItem<?> selectedItem;

        clickOn( treeviewQuery );
        WaitForAsyncUtils.waitForFxEvents();
        treeView.getSelectionModel().select( 0 );
        selectedItem = treeView.getSelectionModel().getSelectedItem();

        while (selectedItem != lastSelectedItem) {
            type( KeyCode.RIGHT ).type( KeyCode.DOWN );
            lastSelectedItem = selectedItem;
            selectedItem = treeView.getSelectionModel().getSelectedItem();
        }
        return this;
    }

    /**
     * Selects the various target values from the tree view. Each target value is selected in sequence and expanded to
     * continue traversing that node's children. The last item in the list will be the selected item when this method
     * returns.
     * 
     * @param treeviewFxid the JavaFX ID of the tree view
     * @param targetValues the target values to be selected and navigated in the tree view
     * @return OtmFxRobot
     */
    public OtmFxRobot selectTreeItem(String treeviewFxid, String... targetValues) {
        if (targetValues.length == 0) {
            throw new IllegalArgumentException( "No tree node selection value provided" );
        }
        TreeView<?> treeView = lookup( treeviewFxid ).query();

        treeView.getSelectionModel().select( 0 );

        for (int i = 0; i < targetValues.length; i++) {
            TreeItem<?> selectedItem = treeView.getSelectionModel().getSelectedItem();
            String selectedValue = selectedItem.getValue().toString();
            TreeItem<?> lastSelectedItem = selectedItem;

            while (!selectedValue.equals( targetValues[i] )) {
                type( KeyCode.DOWN );
                selectedItem = treeView.getSelectionModel().getSelectedItem();
                selectedValue = selectedItem.getValue().toString();

                if (selectedItem == lastSelectedItem) {
                    break;
                }
                lastSelectedItem = selectedItem;
            }

            if (selectedValue.equals( targetValues[i] )) {
                type( KeyCode.RIGHT );
            } else {
                throw new AssertionError( "Target value not found in tree view: " + targetValues[i] );
            }
        }
        return this;
    }

    /**
     * Adjusts the scroll position of a scroll pane.
     * 
     * @param fxScrollPaneQuery the FX query string for the scroll pane
     * @param verticalPct the percentage where the pane's scroll bar should be set
     * @return OtmFxRobot
     */
    public OtmFxRobot setScrollPosition(String fxScrollPaneQuery, double verticalPct) {
        ScrollPane scrollPane = (ScrollPane) lookup( fxScrollPaneQuery ).query();

        scrollPane.setVvalue( (scrollPane.getVmax() - scrollPane.getVmin()) * verticalPct );
        return this;
    }

    /**
     * Sets the UI focus on the specified control.
     * 
     * @param fxQuery the query string for the control that will receive the focus
     * @return OtmFxRobot
     */
    public OtmFxRobot setFocus(FxRobot robot, String fxQuery) {
        WaitForAsyncUtils.asyncFx( () -> robot.lookup( fxQuery ).query().requestFocus() );
        return this;
    }

    /**
     * @see org.testfx.framework.junit.ApplicationFixture#init()
     */
    @Override
    public void init() throws Exception {}

    /**
     * @see org.testfx.framework.junit.ApplicationFixture#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage stage) throws Exception {
        start.accept( stage );
        this.stage = stage;
    }

    /**
     * @see org.testfx.framework.junit.ApplicationFixture#stop()
     */
    @Override
    public void stop() throws Exception {
        stop.accept( stage );
    }

    /**
     * @see org.junit.rules.TestRule#apply(org.junit.runners.model.Statement, org.junit.runner.Description)
     */
    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                FxToolkit.registerPrimaryStage();
                FxToolkit.setupApplication( () -> new ApplicationAdapter( OtmFxRobot.this ) );
                try {
                    base.evaluate();
                } finally {
                    FxToolkit.cleanupApplication( new ApplicationAdapter( OtmFxRobot.this ) );
                }
            }
        };
    }

    /**
     * @see org.testfx.api.FxRobot#targetWindow(javafx.stage.Window)
     */
    @Override
    public OtmFxRobot targetWindow(Window window) {
        super.targetWindow( window );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#targetWindow(java.util.function.Predicate)
     */
    @Override
    public OtmFxRobot targetWindow(Predicate<Window> predicate) {
        super.targetWindow( predicate );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#targetWindow(int)
     */
    @Override
    public OtmFxRobot targetWindow(int windowNumber) {
        super.targetWindow( windowNumber );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#targetWindow(java.lang.String)
     */
    @Override
    public OtmFxRobot targetWindow(String stageTitleRegex) {
        super.targetWindow( stageTitleRegex );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#targetWindow(java.util.regex.Pattern)
     */
    @Override
    public OtmFxRobot targetWindow(Pattern stageTitlePattern) {
        super.targetWindow( stageTitlePattern );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#targetWindow(javafx.scene.Scene)
     */
    @Override
    public OtmFxRobot targetWindow(Scene scene) {
        super.targetWindow( scene );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#targetWindow(javafx.scene.Node)
     */
    @Override
    public OtmFxRobot targetWindow(Node node) {
        super.targetWindow( node );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#targetPos(javafx.geometry.Pos)
     */
    @Override
    public OtmFxRobot targetPos(Pos pointPosition) {
        super.targetPos( pointPosition );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#interact(java.lang.Runnable)
     */
    @Override
    public OtmFxRobot interact(Runnable runnable) {
        super.interact( runnable );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#interact(java.util.concurrent.Callable)
     */
    @Override
    public <T> OtmFxRobot interact(Callable<T> callable) {
        super.interact( callable );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#interactNoWait(java.lang.Runnable)
     */
    @Override
    public OtmFxRobot interactNoWait(Runnable runnable) {
        super.interactNoWait( runnable );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#interactNoWait(java.util.concurrent.Callable)
     */
    @Override
    public <T> OtmFxRobot interactNoWait(Callable<T> callable) {
        super.interactNoWait( callable );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#interrupt()
     */
    @Override
    public OtmFxRobot interrupt() {
        super.interrupt();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#interrupt(int)
     */
    @Override
    public OtmFxRobot interrupt(int attemptsCount) {
        super.interrupt( attemptsCount );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#push(javafx.scene.input.KeyCode[])
     */
    @Override
    public OtmFxRobot push(KeyCode... combination) {
        super.push( combination );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#push(javafx.scene.input.KeyCodeCombination)
     */
    @Override
    public OtmFxRobot push(KeyCodeCombination combination) {
        super.push( combination );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#type(javafx.scene.input.KeyCode[])
     */
    @Override
    public OtmFxRobot type(KeyCode... keyCodes) {
        super.type( keyCodes );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#type(javafx.scene.input.KeyCode, int)
     */
    @Override
    public OtmFxRobot type(KeyCode keyCode, int times) {
        super.type( keyCode, times );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#eraseText(int)
     */
    @Override
    public OtmFxRobot eraseText(int amount) {
        super.eraseText( amount );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#write(char)
     */
    @Override
    public OtmFxRobot write(char character) {
        super.write( character );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#write(java.lang.String)
     */
    @Override
    public OtmFxRobot write(String text) {
        super.write( text );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#write(java.lang.String, int)
     */
    @Override
    public OtmFxRobot write(String text, int sleepMillis) {
        super.write( text, sleepMillis );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#sleep(long)
     */
    @Override
    public OtmFxRobot sleep(long milliseconds) {
        super.sleep( milliseconds );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#sleep(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public OtmFxRobot sleep(long duration, TimeUnit timeUnit) {
        super.sleep( duration, timeUnit );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#scroll(int, javafx.geometry.VerticalDirection)
     */
    @Override
    public OtmFxRobot scroll(int amount, VerticalDirection direction) {
        super.scroll( amount, direction );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#scroll(javafx.geometry.VerticalDirection)
     */
    @Override
    public OtmFxRobot scroll(VerticalDirection direction) {
        super.scroll( direction );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#scroll(int, javafx.geometry.HorizontalDirection)
     */
    @Override
    public OtmFxRobot scroll(int amount, HorizontalDirection direction) {
        super.scroll( amount, direction );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#scroll(javafx.geometry.HorizontalDirection)
     */
    @Override
    public OtmFxRobot scroll(HorizontalDirection direction) {
        super.scroll( direction );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#press(javafx.scene.input.KeyCode[])
     */
    @Override
    public OtmFxRobot press(KeyCode... keys) {
        super.press( keys );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#release(javafx.scene.input.KeyCode[])
     */
    @Override
    public OtmFxRobot release(KeyCode... keys) {
        super.release( keys );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#press(javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot press(MouseButton... buttons) {
        super.press( buttons );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#release(javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot release(MouseButton... buttons) {
        super.release( buttons );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#clickOn(javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot clickOn(MouseButton... buttons) {
        super.clickOn( buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#clickOn(org.testfx.service.query.PointQuery, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot clickOn(PointQuery pointQuery, Motion motion, MouseButton... buttons) {
        super.clickOn( pointQuery, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#doubleClickOn(javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot doubleClickOn(MouseButton... buttons) {
        super.doubleClickOn( buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#doubleClickOn(org.testfx.service.query.PointQuery, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot doubleClickOn(PointQuery pointQuery, Motion motion, MouseButton... buttons) {
        super.doubleClickOn( pointQuery, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#clickOn(double, double, org.testfx.robot.Motion, javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot clickOn(double x, double y, Motion motion, MouseButton... buttons) {
        super.clickOn( x, y, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#clickOn(javafx.geometry.Point2D, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot clickOn(Point2D point, Motion motion, MouseButton... buttons) {
        super.clickOn( point, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#clickOn(javafx.geometry.Bounds, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot clickOn(Bounds bounds, Motion motion, MouseButton... buttons) {
        super.clickOn( bounds, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#clickOn(javafx.scene.Node, org.testfx.robot.Motion, javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot clickOn(Node node, Motion motion, MouseButton... buttons) {
        super.clickOn( node, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#clickOn(javafx.scene.Scene, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot clickOn(Scene scene, Motion motion, MouseButton... buttons) {
        super.clickOn( scene, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#clickOn(javafx.stage.Window, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot clickOn(Window window, Motion motion, MouseButton... buttons) {
        super.clickOn( window, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#clickOn(java.lang.String, org.testfx.robot.Motion, javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot clickOn(String query, Motion motion, MouseButton... buttons) {
        super.clickOn( query, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#clickOn(org.hamcrest.Matcher, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public <T extends Node> OtmFxRobot clickOn(Matcher<T> matcher, Motion motion, MouseButton... buttons) {
        super.clickOn( matcher, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#clickOn(java.util.function.Predicate, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public <T extends Node> OtmFxRobot clickOn(Predicate<T> predicate, Motion motion, MouseButton... buttons) {
        super.clickOn( predicate, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#rightClickOn()
     */
    @Override
    public OtmFxRobot rightClickOn() {
        super.rightClickOn();
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#rightClickOn(org.testfx.service.query.PointQuery, org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot rightClickOn(PointQuery pointQuery, Motion motion) {
        super.rightClickOn( pointQuery, motion );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#rightClickOn(double, double, org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot rightClickOn(double x, double y, Motion motion) {
        super.rightClickOn( x, y, motion );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#rightClickOn(javafx.geometry.Point2D, org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot rightClickOn(Point2D point, Motion motion) {
        super.rightClickOn( point, motion );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#rightClickOn(javafx.geometry.Bounds, org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot rightClickOn(Bounds bounds, Motion motion) {
        super.rightClickOn( bounds, motion );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#rightClickOn(javafx.scene.Node, org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot rightClickOn(Node node, Motion motion) {
        super.rightClickOn( node, motion );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#rightClickOn(javafx.scene.Scene, org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot rightClickOn(Scene scene, Motion motion) {
        super.rightClickOn( scene, motion );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#rightClickOn(javafx.stage.Window, org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot rightClickOn(Window window, Motion motion) {
        super.rightClickOn( window, motion );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#rightClickOn(java.lang.String, org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot rightClickOn(String query, Motion motion) {
        super.rightClickOn( query, motion );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#rightClickOn(org.hamcrest.Matcher, org.testfx.robot.Motion)
     */
    @Override
    public <T extends Node> OtmFxRobot rightClickOn(Matcher<T> matcher, Motion motion) {
        super.rightClickOn( matcher, motion );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#rightClickOn(java.util.function.Predicate, org.testfx.robot.Motion)
     */
    @Override
    public <T extends Node> OtmFxRobot rightClickOn(Predicate<T> predicate, Motion motion) {
        super.rightClickOn( predicate, motion );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#doubleClickOn(double, double, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot doubleClickOn(double x, double y, Motion motion, MouseButton... buttons) {
        super.doubleClickOn( x, y, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#doubleClickOn(javafx.geometry.Point2D, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot doubleClickOn(Point2D point, Motion motion, MouseButton... buttons) {
        super.doubleClickOn( point, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#doubleClickOn(javafx.geometry.Bounds, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot doubleClickOn(Bounds bounds, Motion motion, MouseButton... buttons) {
        super.doubleClickOn( bounds, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#doubleClickOn(javafx.scene.Node, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot doubleClickOn(Node node, Motion motion, MouseButton... buttons) {
        super.doubleClickOn( node, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#doubleClickOn(javafx.scene.Scene, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot doubleClickOn(Scene scene, Motion motion, MouseButton... buttons) {
        super.doubleClickOn( scene, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#doubleClickOn(javafx.stage.Window, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot doubleClickOn(Window window, Motion motion, MouseButton... buttons) {
        super.doubleClickOn( window, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#doubleClickOn(java.lang.String, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot doubleClickOn(String query, Motion motion, MouseButton... buttons) {
        super.doubleClickOn( query, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#doubleClickOn(org.hamcrest.Matcher, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public <T extends Node> OtmFxRobot doubleClickOn(Matcher<T> matcher, Motion motion, MouseButton... buttons) {
        super.doubleClickOn( matcher, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#doubleClickOn(java.util.function.Predicate, org.testfx.robot.Motion,
     *      javafx.scene.input.MouseButton[])
     */
    @Override
    public <T extends Node> OtmFxRobot doubleClickOn(Predicate<T> predicate, Motion motion, MouseButton... buttons) {
        super.doubleClickOn( predicate, motion, buttons );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#drag(javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot drag(MouseButton... buttons) {
        super.drag( buttons );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#drag(org.testfx.service.query.PointQuery, javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot drag(PointQuery pointQuery, MouseButton... buttons) {
        super.drag( pointQuery, buttons );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#drop()
     */
    @Override
    public OtmFxRobot drop() {
        super.drop();
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#dropTo(org.testfx.service.query.PointQuery)
     */
    @Override
    public OtmFxRobot dropTo(PointQuery pointQuery) {
        super.dropTo( pointQuery );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#dropBy(double, double)
     */
    @Override
    public OtmFxRobot dropBy(double x, double y) {
        super.dropBy( x, y );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#drag(double, double, javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot drag(double x, double y, MouseButton... buttons) {
        super.drag( x, y, buttons );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#drag(javafx.geometry.Point2D, javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot drag(Point2D point, MouseButton... buttons) {
        super.drag( point, buttons );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#drag(javafx.geometry.Bounds, javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot drag(Bounds bounds, MouseButton... buttons) {
        super.drag( bounds, buttons );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#drag(javafx.scene.Node, javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot drag(Node node, MouseButton... buttons) {
        super.drag( node, buttons );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#drag(javafx.scene.Scene, javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot drag(Scene scene, MouseButton... buttons) {
        super.drag( scene, buttons );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#drag(javafx.stage.Window, javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot drag(Window window, MouseButton... buttons) {
        super.drag( window, buttons );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#drag(java.lang.String, javafx.scene.input.MouseButton[])
     */
    @Override
    public OtmFxRobot drag(String query, MouseButton... buttons) {
        super.drag( query, buttons );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#drag(org.hamcrest.Matcher, javafx.scene.input.MouseButton[])
     */
    @Override
    public <T extends Node> OtmFxRobot drag(Matcher<T> matcher, MouseButton... buttons) {
        super.drag( matcher, buttons );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#drag(java.util.function.Predicate, javafx.scene.input.MouseButton[])
     */
    @Override
    public <T extends Node> OtmFxRobot drag(Predicate<T> predicate, MouseButton... buttons) {
        super.drag( predicate, buttons );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#dropTo(double, double)
     */
    @Override
    public OtmFxRobot dropTo(double x, double y) {
        super.dropTo( x, y );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#dropTo(javafx.geometry.Point2D)
     */
    @Override
    public OtmFxRobot dropTo(Point2D point) {
        super.dropTo( point );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#dropTo(javafx.geometry.Bounds)
     */
    @Override
    public OtmFxRobot dropTo(Bounds bounds) {
        super.dropTo( bounds );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#dropTo(javafx.scene.Node)
     */
    @Override
    public OtmFxRobot dropTo(Node node) {
        super.dropTo( node );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#dropTo(javafx.scene.Scene)
     */
    @Override
    public OtmFxRobot dropTo(Scene scene) {
        super.dropTo( scene );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#dropTo(javafx.stage.Window)
     */
    @Override
    public OtmFxRobot dropTo(Window window) {
        super.dropTo( window );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#dropTo(java.lang.String)
     */
    @Override
    public OtmFxRobot dropTo(String query) {
        super.dropTo( query );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#dropTo(org.hamcrest.Matcher)
     */
    @Override
    public <T extends Node> OtmFxRobot dropTo(Matcher<T> matcher) {
        super.dropTo( matcher );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#dropTo(java.util.function.Predicate)
     */
    @Override
    public <T extends Node> OtmFxRobot dropTo(Predicate<T> predicate) {
        super.dropTo( predicate );
        waitForFxEvents();
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#moveTo(org.testfx.service.query.PointQuery, org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot moveTo(PointQuery pointQuery, Motion motion) {
        super.moveTo( pointQuery, motion );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#moveBy(double, double, org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot moveBy(double x, double y, Motion motion) {
        super.moveBy( x, y, motion );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#moveTo(double, double, org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot moveTo(double x, double y, Motion motion) {
        super.moveTo( x, y, motion );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#moveTo(javafx.geometry.Point2D, org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot moveTo(Point2D point, Motion motion) {
        super.moveTo( point, motion );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#moveTo(javafx.geometry.Bounds, org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot moveTo(Bounds bounds, Motion motion) {
        super.moveTo( bounds, motion );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#moveTo(javafx.scene.Node, javafx.geometry.Pos, javafx.geometry.Point2D,
     *      org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot moveTo(Node node, Pos offsetReferencePos, Point2D offset, Motion motion) {
        super.moveTo( node, offsetReferencePos, offset, motion );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#moveTo(javafx.scene.Scene, org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot moveTo(Scene scene, Motion motion) {
        super.moveTo( scene, motion );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#moveTo(javafx.stage.Window, org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot moveTo(Window window, Motion motion) {
        super.moveTo( window, motion );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#moveTo(java.lang.String, org.testfx.robot.Motion)
     */
    @Override
    public OtmFxRobot moveTo(String query, Motion motion) {
        super.moveTo( query, motion );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#moveTo(org.hamcrest.Matcher, org.testfx.robot.Motion)
     */
    @Override
    public <T extends Node> OtmFxRobot moveTo(Matcher<T> matcher, Motion motion) {
        super.moveTo( matcher, motion );
        return this;
    }

    /**
     * @see org.testfx.api.FxRobot#moveTo(java.util.function.Predicate, org.testfx.robot.Motion)
     */
    @Override
    public <T extends Node> OtmFxRobot moveTo(Predicate<T> predicate, Motion motion) {
        super.moveTo( predicate, motion );
        return this;
    }

}
