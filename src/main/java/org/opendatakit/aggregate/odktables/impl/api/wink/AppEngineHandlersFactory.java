/*
 * Copyright (C) 2014 University of Washington
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

package org.opendatakit.aggregate.odktables.impl.api.wink;

import java.util.ArrayList;
import java.util.List;

import org.apache.wink.server.handlers.HandlersFactory;
import org.apache.wink.server.handlers.ResponseHandler;
import org.opendatakit.aggregate.odktables.entity.serialization.NotModifiedHandler;

public class AppEngineHandlersFactory extends HandlersFactory {

  public AppEngineHandlersFactory() {
    // super();
  }

  @Override
  public List<? extends ResponseHandler> getResponseHandlers() {
    ArrayList<ResponseHandler> myHandlers = new ArrayList<ResponseHandler>();
    myHandlers.add(new NotModifiedHandler());
    myHandlers.addAll(super.getResponseHandlers());
    return myHandlers;
  }
}
