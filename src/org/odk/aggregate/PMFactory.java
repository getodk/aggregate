/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

/**
 * Singleton to prevent unnecessary construction
 *
 * @author wbrunette@gmail.com
 */
public final class PMFactory {
  /**
   * Singleton of the persistence manager factory
   */
  private static final PersistenceManagerFactory pmfInstance =
      JDOHelper.getPersistenceManagerFactory("transactions-optional");

  /**
   * Private constructor 
   */
  private PMFactory() {
  }

  /**
   * Get the persistence manager singleton instance
   * 
   * @return
   *    persistence manager factory instance
   */
  public static PersistenceManagerFactory get() {
    return pmfInstance;
  }
}
