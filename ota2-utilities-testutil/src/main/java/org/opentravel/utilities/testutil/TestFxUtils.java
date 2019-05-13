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

import org.testfx.api.FxRobot;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;

/**
 * Static utility methods used for JavaFx testing.
 */
public class TestFxUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private TestFxUtils() {}

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

}
