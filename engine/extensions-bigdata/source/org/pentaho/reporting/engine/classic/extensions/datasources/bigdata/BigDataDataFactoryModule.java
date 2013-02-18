package org.pentaho.reporting.engine.classic.extensions.datasources.bigdata;

import org.pentaho.reporting.engine.classic.core.metadata.ElementMetaDataParser;
import org.pentaho.reporting.engine.classic.core.modules.parser.base.DataFactoryReadHandlerFactory;
import org.pentaho.reporting.engine.classic.core.modules.parser.base.DataFactoryXmlResourceFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.bigdata.parser.BigDataDataSourceReadHandler;
import org.pentaho.reporting.engine.classic.extensions.datasources.bigdata.parser.BigDataDataSourceXmlFactoryModule;
import org.pentaho.reporting.libraries.base.boot.AbstractModule;
import org.pentaho.reporting.libraries.base.boot.ModuleInitializeException;
import org.pentaho.reporting.libraries.base.boot.SubSystem;

public class BigDataDataFactoryModule extends AbstractModule
{
  public static String NAMESPACE = "http://reporting.pentaho.org/namespaces/datasources/bigdata";

  public BigDataDataFactoryModule() throws ModuleInitializeException
  {
    loadModuleInfo();
  }

  public void initialize(final SubSystem subSystem) throws ModuleInitializeException
  {
    // lookup all big-data implementations via the big-data plugin and register them
    // It is not necessary to create extra modules for each type of data you want to access
    DataFactoryXmlResourceFactory.register(BigDataDataSourceXmlFactoryModule.class);

    DataFactoryReadHandlerFactory.getInstance().setElementHandler
        (NAMESPACE, "big-data-datasource", BigDataDataSourceReadHandler.class);

    ElementMetaDataParser.initializeOptionalDataFactoryMetaData
        ("org/pentaho/reporting/engine/classic/extensions/datasources/bigdata/meta-datafactory.xml");
  }
}
