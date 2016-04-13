/*
 * Copyright (C) 2012-2013 University of Washington
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

package org.opendatakit.aggregate.odktables.impl.api;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.apache.wink.common.internal.providers.entity.ByteArrayProvider;
import org.apache.wink.common.internal.providers.entity.FileProvider;
import org.apache.wink.common.internal.providers.multipart.BufferedInMultiPartProvider;
import org.apache.wink.common.internal.providers.multipart.OutMultiPartProvider;
import org.opendatakit.aggregate.odktables.entity.serialization.SimpleHTMLMessageWriter;
import org.opendatakit.aggregate.odktables.entity.serialization.SimpleJSONMessageReaderWriter;
import org.opendatakit.aggregate.odktables.entity.serialization.SimpleXMLMessageReaderWriter;

public class ODKTablesAPIApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    final java.util.HashSet<java.lang.Class<?>> classes = new HashSet<Class<?>>();
    
    // the root of the REST services hierarchy
    classes.add(OdkTablesImpl.class);
    
    // standard content stream reader/writer
    classes.add(SimpleJSONMessageReaderWriter.class);
    classes.add(SimpleXMLMessageReaderWriter.class);
    classes.add(SimpleHTMLMessageWriter.class);
    classes.add(BufferedInMultiPartProvider.class);
    classes.add(OutMultiPartProvider.class);
    classes.add(FileProvider.class);
    classes.add(ByteArrayProvider.class);
    
    // exception response generators - 3 flavors of each because MessageContext is not available
    classes.add(ODKDatastoreExceptionJsonMapper.class);
    classes.add(ODKDatastoreExceptionTextXmlMapper.class);
    classes.add(ODKDatastoreExceptionApplicationXmlMapper.class);
    
    classes.add(ODKTablesExceptionJsonMapper.class);
    classes.add(ODKTablesExceptionTextXmlMapper.class);
    classes.add(ODKTablesExceptionApplicationXmlMapper.class);
    
    classes.add(ODKTaskLockExceptionJsonMapper.class);
    classes.add(ODKTaskLockExceptionTextXmlMapper.class);
    classes.add(ODKTaskLockExceptionApplicationXmlMapper.class);
    
    classes.add(IOExceptionJsonMapper.class);
    classes.add(IOExceptionTextXmlMapper.class);
    classes.add(IOExceptionApplicationXmlMapper.class);
    
    classes.add(RuntimeExceptionJsonMapper.class);
    classes.add(RuntimeExceptionTextXmlMapper.class);
    classes.add(RuntimeExceptionApplicationXmlMapper.class);
    return classes;
  }
}