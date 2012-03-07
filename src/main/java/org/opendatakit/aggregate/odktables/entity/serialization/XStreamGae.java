package org.opendatakit.aggregate.odktables.entity.serialization;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.basic.BigDecimalConverter;
import com.thoughtworks.xstream.converters.basic.BigIntegerConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import com.thoughtworks.xstream.converters.basic.ByteConverter;
import com.thoughtworks.xstream.converters.basic.CharConverter;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.converters.basic.DoubleConverter;
import com.thoughtworks.xstream.converters.basic.FloatConverter;
import com.thoughtworks.xstream.converters.basic.IntConverter;
import com.thoughtworks.xstream.converters.basic.LongConverter;
import com.thoughtworks.xstream.converters.basic.NullConverter;
import com.thoughtworks.xstream.converters.basic.ShortConverter;
import com.thoughtworks.xstream.converters.basic.StringBufferConverter;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import com.thoughtworks.xstream.converters.basic.URIConverter;
import com.thoughtworks.xstream.converters.basic.URLConverter;
import com.thoughtworks.xstream.converters.collections.ArrayConverter;
import com.thoughtworks.xstream.converters.collections.BitSetConverter;
import com.thoughtworks.xstream.converters.collections.CharArrayConverter;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.converters.collections.PropertiesConverter;
import com.thoughtworks.xstream.converters.collections.SingletonCollectionConverter;
import com.thoughtworks.xstream.converters.collections.SingletonMapConverter;
import com.thoughtworks.xstream.converters.collections.TreeMapConverter;
import com.thoughtworks.xstream.converters.collections.TreeSetConverter;
import com.thoughtworks.xstream.converters.extended.EncodedByteArrayConverter;
import com.thoughtworks.xstream.converters.extended.FileConverter;
import com.thoughtworks.xstream.converters.extended.GregorianCalendarConverter;
import com.thoughtworks.xstream.converters.extended.JavaClassConverter;
import com.thoughtworks.xstream.converters.extended.JavaFieldConverter;
import com.thoughtworks.xstream.converters.extended.JavaMethodConverter;
import com.thoughtworks.xstream.converters.extended.LocaleConverter;
import com.thoughtworks.xstream.converters.reflection.ExternalizableConverter;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.SelfStreamingInstanceChecker;
import com.thoughtworks.xstream.converters.reflection.SerializableConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.KXml2Driver;
import com.thoughtworks.xstream.mapper.Mapper;

public class XStreamGae extends XStream {
  public XStreamGae() {
    super(new PureJavaReflectionProvider(), new KXml2Driver());
  }

  public XStreamGae(HierarchicalStreamDriver hierarchicalStreamDriver) {
    super(new PureJavaReflectionProvider(), hierarchicalStreamDriver);
  }

  protected void setupConverters() {
    Mapper mapper = getMapper();
    ReflectionProvider reflectionProvider = getReflectionProvider();
    ClassLoader classLoader = getClassLoader();

    final ReflectionConverter reflectionConverter = new ReflectionConverter(mapper,
        reflectionProvider);
    registerConverter(reflectionConverter, PRIORITY_VERY_LOW);

    registerConverter(new SerializableConverter(mapper, reflectionProvider, classLoader),
        PRIORITY_LOW);
    registerConverter(new ExternalizableConverter(mapper, classLoader), PRIORITY_LOW);

    registerConverter(new NullConverter(), PRIORITY_VERY_HIGH);
    registerConverter(new IntConverter(), PRIORITY_NORMAL);
    registerConverter(new FloatConverter(), PRIORITY_NORMAL);
    registerConverter(new DoubleConverter(), PRIORITY_NORMAL);
    registerConverter(new LongConverter(), PRIORITY_NORMAL);
    registerConverter(new ShortConverter(), PRIORITY_NORMAL);
    registerConverter((Converter) new CharConverter(), PRIORITY_NORMAL);
    registerConverter(new BooleanConverter(), PRIORITY_NORMAL);
    registerConverter(new ByteConverter(), PRIORITY_NORMAL);

    registerConverter(new StringConverter(), PRIORITY_NORMAL);
    registerConverter(new StringBufferConverter(), PRIORITY_NORMAL);
    registerConverter(new DateConverter(), PRIORITY_NORMAL);
    registerConverter(new BitSetConverter(), PRIORITY_NORMAL);
    registerConverter(new URIConverter(), PRIORITY_NORMAL);
    registerConverter(new URLConverter(), PRIORITY_NORMAL);
    registerConverter(new BigIntegerConverter(), PRIORITY_NORMAL);
    registerConverter(new BigDecimalConverter(), PRIORITY_NORMAL);

    registerConverter(new ArrayConverter(mapper), PRIORITY_NORMAL);
    registerConverter(new CharArrayConverter(), PRIORITY_NORMAL);
    registerConverter(new CollectionConverter(mapper), PRIORITY_NORMAL);
    registerConverter(new MapConverter(mapper), PRIORITY_NORMAL);
    registerConverter(new TreeMapConverter(mapper), PRIORITY_NORMAL);
    registerConverter(new TreeSetConverter(mapper), PRIORITY_NORMAL);
    registerConverter(new SingletonCollectionConverter(mapper), PRIORITY_NORMAL);
    registerConverter(new SingletonMapConverter(mapper), PRIORITY_NORMAL);
    registerConverter(new PropertiesConverter(), PRIORITY_NORMAL);
    registerConverter((Converter) new EncodedByteArrayConverter(), PRIORITY_NORMAL);

    registerConverter(new FileConverter(), PRIORITY_NORMAL);
    registerConverter(new JavaClassConverter(classLoader), PRIORITY_NORMAL);
    registerConverter(new JavaMethodConverter(classLoader), PRIORITY_NORMAL);
    registerConverter(new JavaFieldConverter(classLoader), PRIORITY_NORMAL);
    registerConverter(new LocaleConverter(), PRIORITY_NORMAL);
    registerConverter(new GregorianCalendarConverter(), PRIORITY_NORMAL);

    registerConverter(new SelfStreamingInstanceChecker(reflectionConverter, this), PRIORITY_NORMAL);
  }
}
