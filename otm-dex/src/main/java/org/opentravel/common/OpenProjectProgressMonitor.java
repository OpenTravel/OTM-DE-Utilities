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

package org.opentravel.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.schemacompiler.loader.LoaderProgressMonitor;

/**
 * @author dmh
 *
 */
public class OpenProjectProgressMonitor implements LoaderProgressMonitor {
    private static Log log = LogFactory.getLog( OpenProjectProgressMonitor.class );

    private DexStatusController controller;
    private double percentDone = 0;
    private double increment;

    /**
     * @param objectEditorController
     * 
     */
    public OpenProjectProgressMonitor(DexStatusController statusController) {
        controller = statusController;
        increment = 0.90F;
    }

    @Override
    public void beginLoad(int libraryCount) {
        // Library count is not always accurate due to includes
        increment = increment / libraryCount * 0.7F;
        // log.debug( "Progress: begin with " + libraryCount + " increment = " + increment );
    }

    @Override
    public void loadingLibrary(String libraryFilename) {
        // log.debug("Progress: loading " + libraryFilename);
        controller.postStatus( "Loading " + libraryFilename );
    }

    @Override
    public void libraryLoaded() {
        // log.debug("Progress: library loaded. Percent done = " + percentDone);
        percentDone += increment;
        controller.postProgress( percentDone );
    }

    @Override
    public void done() {
        // log.debug( "Progress: done" );
        controller.postStatus( "Done" );
    }

}
