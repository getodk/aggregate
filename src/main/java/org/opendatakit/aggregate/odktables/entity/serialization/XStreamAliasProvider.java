package org.opendatakit.aggregate.odktables.entity.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.entity.api.RowResource;
import org.opendatakit.aggregate.odktables.entity.api.TableDefinition;
import org.opendatakit.aggregate.odktables.entity.api.TableResource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

@Produces(MediaType.TEXT_XML)
@Consumes(MediaType.TEXT_XML)
@Provider
public class XStreamAliasProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

  private static final Set<Class<?>> processed = new HashSet<Class<?>>();
  private static final XStream xstream;
  static {
    xstream = new XStreamGae(new StaxDriver());
    xstream.processAnnotations(new Class[] { Column.class, Row.class, TableEntry.class,
        RowResource.class, TableDefinition.class, TableResource.class });
  }
  private static final String DEFAULT_ENCODING = "utf-8";

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[],
      MediaType mediaType) {
    return true;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[],
      MediaType mediaType) {
    return true;
  }

  @Override
  public Object readFrom(Class<Object> aClass, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> map, InputStream stream)
      throws IOException, WebApplicationException {
    String encoding = getCharsetAsString(mediaType);
    processAnnotations(aClass);
    XStream xStream = getXStream(aClass);
    return xStream.fromXML(new InputStreamReader(stream, encoding));
  }

  @Override
  public void writeTo(Object o, Class<?> aClass, Type type, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> map, OutputStream stream)
      throws IOException, WebApplicationException {
    String encoding = getCharsetAsString(mediaType);
    XStream xStream = getXStream(o.getClass());
    xStream.toXML(o, new OutputStreamWriter(stream, encoding));
  }

  @Override
  public long getSize(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
    return -1;
  }

  protected static String getCharsetAsString(MediaType m) {
    if (m == null) {
      return DEFAULT_ENCODING;
    }
    String result = m.getParameters().get("charset");
    return (result == null) ? DEFAULT_ENCODING : result;
  }

  protected XStream getXStream(Class<?> type) {
    processAnnotations(type);
    return xstream;
  }

  private void processAnnotations(Class<?> type) {
    synchronized (processed) {
      if (!processed.contains(type)) {
        xstream.processAnnotations(type);
        processed.add(type);
      }
    }
  }

}
