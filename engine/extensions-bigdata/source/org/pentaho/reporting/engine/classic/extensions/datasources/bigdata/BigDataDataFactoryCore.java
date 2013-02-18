package org.pentaho.reporting.engine.classic.extensions.datasources.bigdata;

import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryMetaData;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryCore;

public class BigDataDataFactoryCore extends DefaultDataFactoryCore
{
  public BigDataDataFactoryCore()
  {
  }

  @Override
  public String[] getReferencedFields(DataFactoryMetaData metaData,
                                      DataFactory element,
                                      String query,
                                      DataRow parameter)
  {
    BigDataDataFactory dataFactory = (BigDataDataFactory) element;
    return dataFactory.getReferencedFields(query);
  }

  @Override
  public Object getQueryHash(DataFactoryMetaData dataFactoryMetaData, DataFactory element, String queryName,
                             DataRow parameter)
  {
    BigDataDataFactory dataFactory = (BigDataDataFactory) element;
    return dataFactory.getQueryHash(queryName);
  }

}
