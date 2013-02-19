package org.pentaho.reporting.engine.classic.extensions.datasources.bigdata.parser;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.reporting.engine.classic.extensions.datasources.bigdata.BigDataQueryTransformationProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.parser.AbstractKettleTransformationProducerReadHandler;
import org.pentaho.reporting.libraries.xmlns.parser.Base64;
import org.pentaho.reporting.libraries.xmlns.parser.ParseException;
import org.pentaho.reporting.libraries.xmlns.parser.StringReadHandler;
import org.pentaho.reporting.libraries.xmlns.parser.XmlReadHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class BigDataTransformationProducerReadHandler extends AbstractKettleTransformationProducerReadHandler
{
  private String pluginId;
  private StringReadHandler resourceReadHandler;
  private String name;
  private String stepName;

  public BigDataTransformationProducerReadHandler()
  {
  }

  protected void startParsing(final Attributes attrs) throws SAXException
  {
    // note: We do not call super here
    pluginId = attrs.getValue(getUri(), "plugin-id");
    name = attrs.getValue(getUri(), "name");
    stepName = attrs.getValue(getUri(), "stepname");
  }


  public String getName()
  {
    return name;
  }

  protected XmlReadHandler getHandlerForChild(final String uri,
                                              final String tagName,
                                              final Attributes atts) throws SAXException
  {
    if (getUri().equals(uri) && "resource".equals(tagName))
    {
      resourceReadHandler = new StringReadHandler();
      return resourceReadHandler;
    }
    return super.getHandlerForChild(uri, tagName, atts);
  }

  public BigDataQueryTransformationProducer getObject() throws SAXException
  {
    if (resourceReadHandler == null)
    {
      throw new ParseException("Missing tag 'resource'", getLocator());
    }

    final String result = resourceReadHandler.getResult();
    final byte[] bytes = Base64.decode(result.toCharArray());
    return new BigDataQueryTransformationProducer
        (getDefinedArgumentNames(), getDefinedVariableNames(), pluginId, stepName, bytes);
  }

  public BigDataQueryTransformationProducer getTransformationProducer() throws SAXException
  {
    return getObject();
  }
}
