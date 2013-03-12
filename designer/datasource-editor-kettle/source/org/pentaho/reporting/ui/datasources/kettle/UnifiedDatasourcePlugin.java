package org.pentaho.reporting.ui.datasources.kettle;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.trans.step.BaseStepGenericXulDialog;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.designtime.DataFactoryChangeRecorder;
import org.pentaho.reporting.engine.classic.core.designtime.DataSourcePlugin;
import org.pentaho.reporting.engine.classic.core.designtime.DesignTimeContext;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryMetaData;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryRegistry;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.DocumentHelper;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.EmbeddedKettleDataFactoryMetaData;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.EmbeddedKettleTransformationProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleDataFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class UnifiedDatasourcePlugin implements DataSourcePlugin
{
  private static final Log logger = LogFactory.getLog(UnifiedDatasourcePlugin.class);
  private String id;
  

  public UnifiedDatasourcePlugin(String id) throws ReportDataFactoryException
  {
    this.id = id; 
  }

  public boolean canHandle(final DataFactory dataFactory)
  {
    return dataFactory instanceof KettleDataFactory;
  }

  private TransMeta loadTransformation() throws KettlePluginException, KettleMissingPluginsException, KettleXMLException
  {
    final Document document = DocumentHelper.loadDocumentFromPlugin(id);
    final Node node = XMLHandler.getSubNode(document, TransMeta.XML_TAG);
    final TransMeta meta = new TransMeta();
    meta.loadXML(node, null, true, null, null);
    return meta;
  }

  private StepDialogInterface createDialog(final StepMeta step,
                                                final DesignTimeContext context)
  {
    // Render datasource specific dialog for editing step details...
    try
    {
      final String dlgClassName = step.getStepMetaInterface().getDialogClassName().replace("Dialog","XulDialog");
      
      final Class<StepDialogInterface> dialog = 
          (Class<StepDialogInterface>) Class.forName(dlgClassName, true, step.getStepMetaInterface().getClass().getClassLoader());

      final Constructor <StepDialogInterface> constructor =
          dialog.getDeclaredConstructor(Object.class, BaseStepMeta.class, TransMeta.class, String.class);
      
      return constructor.newInstance(context.getParentWindow(), step.getStepMetaInterface(),
          step.getParentTransMeta(), EmbeddedKettleDataFactoryMetaData.DATA_CONFIGURATION_STEP);

    }
    catch (Exception e)
    {

      logger.error("Critical error attempting to dynamically create dialog. This datasource will not be available.", e);
      return null;

    }
  }

  public DataFactory performEdit(final DesignTimeContext context,
                                 final DataFactory input,
                                 final String queryName,
                                 DataFactoryChangeRecorder recorder)
  {

    final KettleDataFactory kettleDataFactory = (KettleDataFactory) input;
    try
    {
      TransMeta transMeta;
      if (kettleDataFactory == null)
      {
        transMeta = loadTransformation();
      }
      else
      {
        final EmbeddedKettleTransformationProducer query = 
                      (EmbeddedKettleTransformationProducer) kettleDataFactory.getQuery(queryName);
        if (query == null)
        {
          transMeta = loadTransformation();
        }
        else
        {
          final Document document = DocumentHelper.loadDocumentFromBytes(query.getBigDataTransformationRaw());
          final Node node = XMLHandler.getSubNode(document, TransMeta.XML_TAG);
          transMeta = new TransMeta();
          transMeta.loadXML(node, null, true, null, null);
        }
      }

      final StepMeta step = transMeta.findStep(EmbeddedKettleDataFactoryMetaData.DATA_CONFIGURATION_STEP);

      // dialog OK button clicked ...
      final StepDialogInterface dlg = createDialog(step, context);
      if (dlg.open() != null)
      {
        transMeta.addOrReplaceStep(step);
        final byte[] rawData = transMeta.getXML().getBytes("UTF8");
        final KettleDataFactory retval = new KettleDataFactory();
        retval.setMetadata(getMetaData());
        
        // TODO: No parameter definitions here!
        retval.setQuery(queryName,
            new EmbeddedKettleTransformationProducer(new String[0], new ParameterMapping[0], id, 
                            EmbeddedKettleDataFactoryMetaData.DATA_CONFIGURATION_STEP, rawData));
        return retval;
      }

      return input;
    }
    catch (Exception e)
    {
      context.error(e);
      return input;
    }
  }

  public DataFactoryMetaData getMetaData()
  {
    return DataFactoryRegistry.getInstance().getMetaData(id);
  }
}
