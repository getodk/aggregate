/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.odktables;

import org.apache.commons.lang3.StringUtils;
import org.opendatakit.aggregate.server.ServerPreferencesProperties;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * Impose a strict ordering on the log entries associated with a file. The
 * ordering consists of two concatentated counters:
 *
 * sequenceBase.counter
 *
 * Where sequenceBase is a value that is fetched, incremented and written to the
 * datastore via
 *
 * ServerPreferencesProperties.unsafeIncOdkTablesSequencerBase(cc).
 *
 * This is obtained when the Sequencer is created, which is at the time the
 * update lock is obtained.
 *
 * Counter is simply a counter held within this object instance to order the
 * updates occuring during the holding of the lock.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class Sequencer {

  private static final int DECIMAL_PLACES = 10;
  private final String sequenceBase;
  private int counter;

  /**
   * Obtain a sequencer from the datastore. Should be called immediately after
   * the update lock has been obtained, and released whenever the lock is
   * released.
   *
   * @param cc
   * @throws ODKEntityNotFoundException
   * @throws ODKOverQuotaException
   */
  Sequencer(CallingContext cc) throws ODKEntityNotFoundException, ODKOverQuotaException {
    sequenceBase = ServerPreferencesProperties.unsafeIncOdkTablesSequencerBase(cc);
    counter = 0;
  }

  /**
   * Generate the next sequence value.
   *
   * Use seq1.compareTo(seq2) as the natural ordering of these values.
   *
   * @return
   */
  public String getNextSequenceValue() {
    ++counter;
    return sequenceBase + "." + StringUtils.leftPad(Integer.toString(counter), DECIMAL_PLACES, '0');
  }
}
