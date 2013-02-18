package org.pentaho.reporting.engine.classic.extensions.datasources.bigdata.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleDataFactoryWriterHandler;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriterException;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriterState;
import org.pentaho.reporting.engine.classic.extensions.datasources.bigdata.BigDataDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.bigdata.BigDataDataFactoryModule;
import org.pentaho.reporting.engine.classic.extensions.datasources.bigdata.BigDataQueryTransformationProducer;
import org.pentaho.reporting.libraries.docbundle.BundleUtilities;
import org.pentaho.reporting.libraries.docbundle.WriteableDocumentBundle;
import org.pentaho.reporting.libraries.xmlns.common.AttributeList;
import org.pentaho.reporting.libraries.xmlns.parser.Base64;
import org.pentaho.reporting.libraries.xmlns.writer.DefaultTagDescription;
import org.pentaho.reporting.libraries.xmlns.writer.XmlWriter;

public class BigDataDataFactoryBundleWriteHandler implements BundleDataFactoryWriterHandler
{
  public BigDataDataFactoryBundleWriteHandler()
  {
  }

  /**
   * Writes a data-source into a own file. The name of file inside the bundle is returned
   * as string. The file name returned is always absolute and can be made relative by using the IOUtils of
   * LibBase. If the writer-handler did not generate a file on its own, it should return null.
   *
   * @param bundle      the bundle where to write to.
   * @param dataFactory the data factory that should be written.
   * @param state       the writer state to hold the current processing information.
   * @return the name of the newly generated file or null if no file was created.
   * @throws java.io.IOException if any error occured
   * @throws org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriterException
   *                             if a bundle-management error occured.
   */
  public String writeDataFactory(final WriteableDocumentBundle bundle,
                                 final DataFactory dataFactory,
                                 final BundleWriterState state)
      throws IOException, BundleWriterException
  {
    final String fileName = BundleUtilities.getUniqueName(bundle, state.getFileName(), "datasources/big-data-ds{0}.xml");
    if (fileName == null)
    {
      throw new IOException("Unable to generate unique name for Inline-Data-Source");
    }

    final OutputStream outputStream = bundle.createEntry(fileName, "text/xml");
    final DefaultTagDescription tagDescription = new DefaultTagDescription();
    tagDescription.setDefaultNamespace(BigDataDataFactoryModule.NAMESPACE);
    tagDescription.setNamespaceHasCData(BigDataDataFactoryModule.NAMESPACE, false);

    final XmlWriter xmlWriter = new XmlWriter(new OutputStreamWriter(outputStream, "UTF-8"), tagDescription, "  ", "\n");

    final BigDataDataFactory factory = (BigDataDataFactory) dataFactory;

    final AttributeList rootAttrs = new AttributeList();
    rootAttrs.addNamespaceDeclaration("data", BigDataDataFactoryModule.NAMESPACE);
    xmlWriter.writeTag(BigDataDataFactoryModule.NAMESPACE, "big-data-datasource", rootAttrs, XmlWriter.OPEN);

    final String[] queryNames = factory.getQueryNames();
    for (int i = 0; i < queryNames.length; i++)
    {
      final String queryName = queryNames[i];
      final BigDataQueryTransformationProducer prod = factory.getQuery(queryName);
      writeQuery(xmlWriter, queryName, prod);
    }
    xmlWriter.writeCloseTag();
    xmlWriter.close();
    return fileName;
  }

  private void writeQuery(final XmlWriter xmlWriter,
                          final String queryName,
                          final BigDataQueryTransformationProducer fileProducer)
      throws IOException
  {
    final AttributeList coreAttrs = new AttributeList();
    // the name is static for now
    coreAttrs.setAttribute(BigDataDataFactoryModule.NAMESPACE, "name", queryName);
    coreAttrs.setAttribute(BigDataDataFactoryModule.NAMESPACE, "plugin-id", fileProducer.getPluginId());

    final String[] definedArgumentNames = fileProducer.getDefinedArgumentNames();
    final ParameterMapping[] parameterMappings = fileProducer.getDefinedVariableNames();
    xmlWriter.writeTag(BigDataDataFactoryModule.NAMESPACE, "query", coreAttrs, XmlWriter.OPEN);

    xmlWriter.writeTag(BigDataDataFactoryModule.NAMESPACE, "resource", XmlWriter.OPEN);
    xmlWriter.writeText(new String(Base64.encode(fileProducer.getBigDataTransformationRaw())));
    xmlWriter.writeCloseTag();

    for (int i = 0; i < definedArgumentNames.length; i++)
    {
      final String argumentName = definedArgumentNames[i];
      xmlWriter.writeTag(BigDataDataFactoryModule.NAMESPACE, "argument", "datarow-name", argumentName, XmlWriter.CLOSE);
    }

    for (int i = 0; i < parameterMappings.length; i++)
    {
      final ParameterMapping parameterMapping = parameterMappings[i];
      final AttributeList paramAttr = new AttributeList();
      paramAttr.setAttribute(BigDataDataFactoryModule.NAMESPACE, "datarow-name", parameterMapping.getName());
      if (parameterMapping.getName().equals(parameterMapping.getAlias()) == false)
      {
        paramAttr.setAttribute(BigDataDataFactoryModule.NAMESPACE, "variable-name", parameterMapping.getAlias());
      }
      xmlWriter.writeTag(BigDataDataFactoryModule.NAMESPACE, "variable", paramAttr, XmlWriter.CLOSE);
    }
    xmlWriter.writeCloseTag();
  }

}
