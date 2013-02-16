package org.pentaho.reporting.engine.classic.extensions.datasources.dynamic;

import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleDataFactoryCore;

public class DynamicDataFactoryCore extends KettleDataFactoryCore {
  
  /*
  private KettleDataFactoryCore delegate = new KettleDataFactoryCore();
  
  @Override
  public String[] getReferencedFields(DataFactoryMetaData metaData, DataFactory element, String query, DataRow parameter) {
    return delegate.getReferencedFields(metaData, element, query, parameter);
  }

  @Override
  public ResourceReference[] getReferencedResources(DataFactoryMetaData metaData, DataFactory element,
      ResourceManager resourceManager, String query, DataRow parameter) {
    return delegate.getReferencedResources(metaData, element, resourceManager, query, parameter);
  }

  @Override
  public String getDisplayConnectionName(DataFactoryMetaData metaData, DataFactory dataFactory) {
    return delegate.getDisplayConnectionName(metaData, dataFactory);
  }

  @Override
  public Object getQueryHash(DataFactoryMetaData dataFactoryMetaData, DataFactory dataFactory, String queryName,
      DataRow parameter) {
    return delegate.getQueryHash(dataFactoryMetaData, dataFactory, queryName, parameter);
  }
  */
  private static final long serialVersionUID = 1120825581484916346L;

  public DynamicDataFactoryCore() {
    
  }

}
