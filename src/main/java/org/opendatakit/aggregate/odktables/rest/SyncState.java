/*
 * Copyright (C) 2012 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.odktables.rest;

/**
 * The contents of the SYNC_STATE column. The state of the table with regards
 * to sync'ing to the webserver.
 * <p>
 * NB: Lowercase due to considerations regarding javascript.
 * </p>
 * <p>
 * Here is a brief overview of the rules for transitions between states on basic
 * write operations:
 *
 * <pre>
 * insert:
 *     state = INSERTING
 *
 * update:
 *     if state == REST:
 *        state = UPDATING
 *
 * delete:
 *     if state == REST or state == UPDATING:
 *        state = DELETING
 *        don't actually delete yet (defer until sync)
 *     else if state == INSERTING:
 *        actually delete
 * </pre>
 * </p>
 *
 * @author sudar.sam@gmail.com
 *
 */
public enum SyncState {
  rest, inserting, updating, deleting, conflicting, rest_pending_files;
}
