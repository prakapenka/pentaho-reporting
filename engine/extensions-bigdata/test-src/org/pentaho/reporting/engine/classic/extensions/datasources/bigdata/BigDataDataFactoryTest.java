package org.pentaho.reporting.engine.classic.extensions.datasources.bigdata;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.DataFactoryPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriter;
import org.pentaho.reporting.libraries.base.util.DebugLog;
import org.pentaho.reporting.libraries.base.util.MemoryByteArrayOutputStream;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

public class BigDataDataFactoryTest extends TestCase
{
  public BigDataDataFactoryTest()
  {
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testLoadSave() throws Exception
  {
    final BigDataDataFactory dataFactory = new BigDataDataFactory();
    final String[] argumentNames = {"arg0", "arg1"};
    final ParameterMapping[] parameterMappings = {new ParameterMapping("name", "alias"),
        new ParameterMapping("name2", "alias2")};
    final byte[] raw = {0, 1, 2, 3, 4, 5};
    dataFactory.setQuery(new BigDataQueryTransformationProducer(argumentNames,
        parameterMappings, "MongoDB", raw));

    final BigDataDataFactory result = (BigDataDataFactory) saveAndLoad(dataFactory);
    final String[] queryNames = result.getQueryNames();
    assertEquals(1, queryNames.length);
    assertEquals("big-data-query", queryNames[0]);

    final BigDataQueryTransformationProducer query = result.getQuery();
    assertTrue(Arrays.equals(raw, query.getBigDataTransformationRaw()));
    assertEquals("MongoDB", query.getPluginId());
    assertTrue(Arrays.equals(argumentNames, query.getDefinedArgumentNames()));
    assertTrue(Arrays.equals(parameterMappings, query.getDefinedVariableNames()));
  }

  public void testPluginList()
  {
    final List<PluginInterface> plugins = PluginRegistry.getInstance().getPlugins(DataFactoryPluginType.class);
    DebugLog.log(plugins);
  }

  private DataFactory saveAndLoad(DataFactory dataFactory)
      throws Exception
  {
    final MasterReport report = new MasterReport();
    report.setDataFactory(dataFactory);

    final MemoryByteArrayOutputStream bout = new MemoryByteArrayOutputStream();
    BundleWriter.writeReportToZipStream(report, bout);
    final ResourceManager mgr = new ResourceManager();
    mgr.registerDefaults();

    final Resource resource = mgr.createDirectly(bout.toByteArray(), MasterReport.class);
    final MasterReport r2 = (MasterReport) resource.getResource();
    return r2.getDataFactory();
  }
}
