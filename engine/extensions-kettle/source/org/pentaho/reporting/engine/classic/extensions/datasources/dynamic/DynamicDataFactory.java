package org.pentaho.reporting.engine.classic.extensions.datasources.dynamic;

import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleDataFactory;

public class DynamicDataFactory extends KettleDataFactory {
  
  /*
  private KettleDataFactory delegate = new KettleDataFactory();

  @Override
  public TableModel queryData(String query, DataRow parameters) throws ReportDataFactoryException {
    return delegate.queryData(query, parameters);
  }

  @Override
  public DataFactory derive() {
    return delegate.derive();
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public boolean isQueryExecutable(String query, DataRow parameters) {
    return delegate.isQueryExecutable(query, parameters);
  }

  @Override
  public String[] getQueryNames() {
    return delegate.getQueryNames();
  }

  @Override
  public void cancelRunningQuery() {
    delegate.cancelRunningQuery();
  }

  @Override
  public void initialize(DataFactoryContext dataFactoryContext) throws ReportDataFactoryException {
    delegate.initialize(dataFactoryContext);
  }

  @Override
  public DataFactory clone(){
    return delegate.clone();
  }
  
  public void setQuery(final String name, final KettleTransformationProducer value){
    delegate.setQuery(name, value);
  }
  
  public KettleTransformationProducer getQuery(final String name)
  {
    return delegate.getQuery(name);
  }
  
  */

  private static final long serialVersionUID = 4068715255925086746L;

  public DynamicDataFactory() {
    super();
  }

}
