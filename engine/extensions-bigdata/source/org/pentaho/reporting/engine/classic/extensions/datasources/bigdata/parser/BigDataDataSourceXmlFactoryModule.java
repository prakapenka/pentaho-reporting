package org.pentaho.reporting.engine.classic.extensions.datasources.bigdata.parser;

import org.pentaho.reporting.engine.classic.extensions.datasources.bigdata.BigDataDataFactoryModule;
import org.pentaho.reporting.libraries.xmlns.parser.XmlDocumentInfo;
import org.pentaho.reporting.libraries.xmlns.parser.XmlFactoryModule;
import org.pentaho.reporting.libraries.xmlns.parser.XmlReadHandler;

public class BigDataDataSourceXmlFactoryModule implements XmlFactoryModule
{
  public BigDataDataSourceXmlFactoryModule()
  {
  }

  public int getDocumentSupport(final XmlDocumentInfo documentInfo)
  {
    final String rootNamespace = documentInfo.getRootElementNameSpace();
    if (rootNamespace != null && rootNamespace.length() > 0)
    {
      if (BigDataDataFactoryModule.NAMESPACE.equals(rootNamespace) == false)
      {
        return NOT_RECOGNIZED;
      }
      else if ("big-data-datasource".equals(documentInfo.getRootElement()))
      {
        return RECOGNIZED_BY_NAMESPACE;
      }
    }
    else if ("big-data-datasource".equals(documentInfo.getRootElement()))
    {
      return RECOGNIZED_BY_TAGNAME;
    }

    return NOT_RECOGNIZED;
  }

  public String getDefaultNamespace(final XmlDocumentInfo documentInfo)
  {
    return BigDataDataFactoryModule.NAMESPACE;
  }

  public XmlReadHandler createReadHandler(final XmlDocumentInfo documentInfo)
  {
    return new BigDataDataSourceReadHandler();
  }
}
