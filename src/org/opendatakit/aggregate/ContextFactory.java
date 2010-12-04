/*
 * Copyright (C) 2010 University of Washington
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


package org.opendatakit.aggregate;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Server Context creates a singleton for application context to prevent unnecessary construction
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class ContextFactory {
  
    private static final String APP_CONTEXT_PATH = "odk-settings.xml";
  
    /**
     * Singleton of the application context
     */
    private static final ApplicationContext applicationContext = new ClassPathXmlApplicationContext(APP_CONTEXT_PATH);


    /**
     * Private constructor 
     */
    private ContextFactory() {}

    /**
     * Get the application context factory singleton instance
     * 
     * @return
     *    application context instance
     */
    public static ApplicationContext get() {
      return applicationContext;
    }
  
}
