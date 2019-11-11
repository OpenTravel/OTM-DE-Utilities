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

package org.opentravel.dex.tasks;

/**
 * OTM-DE-JavaFX singleton task interface.
 * <p>
 * This interface indicates that only one task of this class should run at a time. Older task will be cancelled if this
 * type of task is started while one is already running.
 * 
 * @author dmh
 *
 */
public interface DexTaskSingleton extends DexTask {

}