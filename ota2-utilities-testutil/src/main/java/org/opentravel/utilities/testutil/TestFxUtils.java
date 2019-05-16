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

import org.loadui.testfx.utils.KeyCodeUtils;
import org.testfx.api.FxRobot;
import org.testfx.api.FxRobotInterface;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;

/**
 * Static utility methods used for JavaFx testing.
 */
public class TestFxUtils {

    private static final Map<Character,KeyCode> SPECIAL_CHARS_MAP;

    /**
     * Private constructor to prevent instantiation.
     */
    private TestFxUtils() {}

    /**
     * Types the given text into the control currently in focus.
     * 
     * @param robot the robot to use for typing the text
     * @param textFxQuery the query string for the JavaFX text control
     * @param text the text to be typed
     */
    public static void typeText(FxRobotInterface robot, String textFxQuery, String text) {
        Object control = robot.lookup( textFxQuery ).query();

        robot.clickOn( textFxQuery );

        if (control instanceof TextInputControl) {
            int charCount = ((TextInputControl) control).getText().length();

            for (int i = 0; i < charCount; i++) {
                robot.type( KeyCode.BACK_SPACE );
            }
        }

        for (int i = 0; i < text.length(); i++) {
            if (SPECIAL_CHARS_MAP.containsKey( text.charAt( i ) )) {
                robot.press( KeyCode.SHIFT ).press( SPECIAL_CHARS_MAP.get( text.charAt( i ) ) )
                    .release( SPECIAL_CHARS_MAP.get( text.charAt( i ) ) ).release( KeyCode.SHIFT );
            } else {
                char typed = text.charAt( i );
                KeyCode identified = KeyCodeUtils.findKeyCode( typed );

                if (Character.isUpperCase( typed )) {
                    robot.press( KeyCode.SHIFT ).type( identified ).release( KeyCode.SHIFT );
                } else {
                    robot.type( identified );
                }
            }
        }
    }

    /**
     * Expands and navigates all members of the specified tree view on the UI.
     * 
     * @param robot the TestFX robot that will perform the navigation
     * @param treeviewFxid the JavaFX query ID of the tree view to navigate
     */
    public static void navigateTreeView(FxRobot robot, String treeviewFxid) {
        TreeView<?> treeView = robot.lookup( treeviewFxid ).query();
        TreeItem<?> lastSelectedItem = null;
        TreeItem<?> selectedItem;

        robot.clickOn( treeviewFxid );
        WaitForAsyncUtils.waitForFxEvents();
        treeView.getSelectionModel().select( 0 );
        selectedItem = treeView.getSelectionModel().getSelectedItem();

        while (selectedItem != lastSelectedItem) {
            robot.type( KeyCode.RIGHT );
            robot.type( KeyCode.DOWN );
            lastSelectedItem = selectedItem;
            selectedItem = treeView.getSelectionModel().getSelectedItem();
        }
    }

    /**
     * Uses the TestFX robot provided to select the various target values from the tree view. Each target value is
     * selected in sequence and expanded to continue traversing that node's children. The last item in the list will be
     * the selected item when this method returns.
     * 
     * @param robot the TestFX robot to use for the selection
     * @param treeviewFxid the JavaFX ID of the tree view
     * @param targetValues the target values to be selected and navigated in the tree view
     * @throws Exception thrown if the tree view or one of its target values does not exist
     */
    public static void selectTreeItem(FxRobot robot, String treeviewFxid, String... targetValues) throws Exception {
        if (targetValues.length == 0) {
            throw new IllegalArgumentException( "No tree node selection value provided" );
        }
        TreeView<?> treeView = robot.lookup( treeviewFxid ).query();

        treeView.getSelectionModel().select( 0 );

        for (int i = 0; i < targetValues.length; i++) {
            TreeItem<?> selectedItem = treeView.getSelectionModel().getSelectedItem();
            String selectedValue = selectedItem.getValue().toString();
            TreeItem<?> lastSelectedItem = selectedItem;

            while (!selectedValue.equals( targetValues[i] )) {
                robot.type( KeyCode.DOWN );
                selectedItem = treeView.getSelectionModel().getSelectedItem();
                selectedValue = selectedItem.getValue().toString();

                if (selectedItem == lastSelectedItem) {
                    break;
                }
                lastSelectedItem = selectedItem;
            }

            if (selectedValue.equals( targetValues[i] )) {
                robot.type( KeyCode.RIGHT );
            } else {
                throw new AssertionError( "Target value not found in tree view: " + targetValues[i] );
            }
        }
    }

    /**
     * Adjusts the scroll position of a scroll pane.
     * 
     * @param robot the robot to use for control lookups
     * @param fxScrollPaneQuery the FX query string for the scroll pane
     * @param verticalPct the percentage where the pane's scroll bar should be set
     */
    public static void setScrollPosition(FxRobot robot, String fxScrollPaneQuery, double verticalPct) {
        ScrollPane scrollPane = (ScrollPane) robot.lookup( fxScrollPaneQuery ).query();
        scrollPane.setVvalue( (scrollPane.getVmax() - scrollPane.getVmin()) * verticalPct );
    }

    /**
     * Sets the UI focus on the specified control.
     * 
     * @param robot the robot to use for control lookups
     * @param fxQuery
     */
    public static void setFocus(FxRobot robot, String fxQuery) {
        WaitForAsyncUtils.asyncFx( () -> robot.lookup( fxQuery ).query().requestFocus() );
    }

    /**
     * Waits until the specified JavaFX control is enabled. For OTM Utility applications, this is often useful for
     * detecting when a background task has been completed. If the control becomes enabled before the timeout period is
     * reached, this method will return true (false otherwise).
     * 
     * @param robot the TestFX robot that will be used to find the UI component
     * @param fxQuery the TestFX query for locating the UI component
     * @param timeout the number of seconds to wait before timing out
     * @return boolean
     */
    public static boolean waitUntilEnabled(FxRobot robot, String fxQuery, int timeout) {
        boolean result = false;
        try {
            WaitForAsyncUtils.waitFor( timeout, TimeUnit.SECONDS,
                (Callable<Boolean>) () -> NodeMatchers.isEnabled().matches( robot.lookup( fxQuery ).query() ) );
            result = true;

        } catch (TimeoutException e) {
            // No action - return false
        }
        WaitForAsyncUtils.waitForFxEvents();
        return result;
    }

    /**
     * Static initialization tasks.
     */
    static {
        Map<Character,KeyCode> specialChars = new HashMap<Character,KeyCode>();

        specialChars.put( '~', KeyCode.BACK_QUOTE );
        specialChars.put( '!', KeyCode.DIGIT1 );
        specialChars.put( '@', KeyCode.DIGIT2 );
        specialChars.put( '#', KeyCode.DIGIT3 );
        specialChars.put( '$', KeyCode.DIGIT4 );
        specialChars.put( '%', KeyCode.DIGIT5 );
        specialChars.put( '^', KeyCode.DIGIT6 );
        specialChars.put( '&', KeyCode.DIGIT7 );
        specialChars.put( '*', KeyCode.DIGIT8 );
        specialChars.put( '(', KeyCode.DIGIT9 );
        specialChars.put( ')', KeyCode.DIGIT0 );
        specialChars.put( '_', KeyCode.MINUS );
        specialChars.put( '+', KeyCode.EQUALS );
        specialChars.put( '{', KeyCode.BRACELEFT );
        specialChars.put( '}', KeyCode.BRACERIGHT );
        specialChars.put( ':', KeyCode.SEMICOLON );
        specialChars.put( '"', KeyCode.QUOTE );
        specialChars.put( '<', KeyCode.COMMA );
        specialChars.put( '>', KeyCode.PERIOD );
        specialChars.put( '?', KeyCode.SLASH );
        SPECIAL_CHARS_MAP = Collections.unmodifiableMap( specialChars );
    }

}
