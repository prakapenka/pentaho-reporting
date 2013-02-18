package org.pentaho.reporting.engine.classic.extensions.datasources.bigdata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.datafactory.DynamicDatasource;
import org.pentaho.reporting.libraries.xmlns.parser.LoggingErrorHandler;
import org.pentaho.reporting.libraries.xmlns.parser.ParserEntityResolver;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class BigDataHelper
{
  private BigDataHelper()
  {
  }

  public static Document loadDocumentFromBytes (final byte[] bytes) throws KettlePluginException
  {
    return loadDocument(new ByteArrayInputStream(bytes));
  }

  public static Document loadDocumentFromPlugin(final DynamicDatasource dynamicDataSource)
      throws KettlePluginException
  {
    // Load the main plugin class, as it holds all of the relevant details we need to realize this
    // datasource
    final InputStream in = dynamicDataSource.getClass().getResourceAsStream(dynamicDataSource.getTemplate());

    return loadDocument(in);

  }

  public static Document loadDocument(final InputStream in) throws KettlePluginException
  {
    try
    {
      final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      dbf.setValidating(false);

      final DocumentBuilder db = dbf.newDocumentBuilder();
      db.setEntityResolver(ParserEntityResolver.getDefaultResolver());
      db.setErrorHandler(new LoggingErrorHandler());
      final InputSource input = new InputSource(in);
      return db.parse(input);
    }
    catch (ParserConfigurationException e)
    {
      throw new KettlePluginException("Unable to initialize the XML-Parser", e);
    }
    catch (SAXException e)
    {
      throw new KettlePluginException("Unable to parse the document.", e);
    }
    catch (IOException e)
    {
      throw new KettlePluginException("Unable to read the document from stream.", e);
    }
    finally
    {
      try
      {
        in.close();
      }
      catch (IOException e)
      {
        // ignored ..
      }
    }
  }
}
