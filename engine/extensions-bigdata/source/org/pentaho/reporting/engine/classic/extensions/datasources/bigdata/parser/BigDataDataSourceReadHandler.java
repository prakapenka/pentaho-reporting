package org.pentaho.reporting.engine.classic.extensions.datasources.bigdata.parser;

import java.util.ArrayList;

import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.modules.parser.base.DataFactoryReadHandler;
import org.pentaho.reporting.engine.classic.extensions.datasources.bigdata.BigDataDataFactory;
import org.pentaho.reporting.libraries.xmlns.parser.AbstractXmlReadHandler;
import org.pentaho.reporting.libraries.xmlns.parser.XmlReadHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class BigDataDataSourceReadHandler extends AbstractXmlReadHandler implements DataFactoryReadHandler
{
  private ArrayList<BigDataTransformationProducerReadHandler> queries;
  private BigDataDataFactory factory;

  public BigDataDataSourceReadHandler()
  {
    queries = new ArrayList<BigDataTransformationProducerReadHandler>();
  }

  protected XmlReadHandler getHandlerForChild(final String uri,
                                              final String tagName,
                                              final Attributes atts) throws SAXException
  {
    if (isSameNamespace(uri) == false)
    {
      return null;
    }

    if ("query".equals(tagName))
    {
      final BigDataTransformationProducerReadHandler queryReadHandler = new BigDataTransformationProducerReadHandler();
      queries.add(queryReadHandler);
      return queryReadHandler;
    }
    return null;
  }

  protected void doneParsing() throws SAXException
  {
    factory = new BigDataDataFactory();

    for (int i = 0; i < queries.size(); i++)
    {
      final BigDataTransformationProducerReadHandler handler = queries.get(i);
      factory.setQuery(handler.getName(), handler.getTransformationProducer());
    }
  }

  public DataFactory getDataFactory()
  {
    return factory;
  }

  public Object getObject() throws SAXException
  {
    return factory;
  }
}
