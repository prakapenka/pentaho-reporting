package org.pentaho.reporting.engine.classic.extensions.datasources.bigdata;

import javax.swing.table.TableModel;

import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.DataFactoryContext;
import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleDataFactory;

public class BigDataDataFactory implements DataFactory
{
  private KettleDataFactory delegate;

  public BigDataDataFactory()
  {
    delegate = new KettleDataFactory();
  }

  @Override
  public TableModel queryData(final String query, final DataRow parameters) throws ReportDataFactoryException
  {
    return delegate.queryData(query, parameters);
  }

  @Override
  public DataFactory derive()
  {
    try
    {
      final BigDataDataFactory dataFactory = (BigDataDataFactory) super.clone();
      dataFactory.delegate = (KettleDataFactory) delegate.derive();
      return dataFactory;
    }
    catch (CloneNotSupportedException e)
    {
      throw new IllegalStateException();
    }
  }

  @Override
  public void close()
  {
    delegate.close();
  }

  @Override
  public boolean isQueryExecutable(final String query, final DataRow parameters)
  {
    return delegate.isQueryExecutable(query, parameters);
  }

  @Override
  public String[] getQueryNames()
  {
    return delegate.getQueryNames();
  }

  @Override
  public void cancelRunningQuery()
  {
    delegate.cancelRunningQuery();
  }

  @Override
  public void initialize(final DataFactoryContext dataFactoryContext) throws ReportDataFactoryException
  {
    delegate.initialize(dataFactoryContext);
  }

  @Override
  public DataFactory clone()
  {
    try
    {
      final BigDataDataFactory dataFactory = (BigDataDataFactory) super.clone();
      dataFactory.delegate = delegate.clone();
      return dataFactory;
    }
    catch (CloneNotSupportedException e)
    {
      throw new IllegalStateException();
    }
  }

  public void setQuery(final String query, final BigDataQueryTransformationProducer value)
  {
    delegate.setQuery(query, value);
  }

  public BigDataQueryTransformationProducer getQuery(final String query)
  {
    return (BigDataQueryTransformationProducer) delegate.getQuery(query);
  }

  public String[] getReferencedFields(final String query)
  {
    final BigDataQueryTransformationProducer transformationProducer = getQuery(query);
    if (transformationProducer == null)
    {
      return new String[0];
    }
    return transformationProducer.getReferencedFields();
  }

  public Object getQueryHash(final String query)
  {
    return delegate.getQueryHash(query);
  }
}
