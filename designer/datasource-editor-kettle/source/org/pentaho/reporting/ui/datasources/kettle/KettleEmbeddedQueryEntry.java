package org.pentaho.reporting.ui.datasources.kettle;

import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.designtime.DesignTimeContext;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.EmbeddedKettleDataFactoryMetaData;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.EmbeddedKettleTransformationProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

public class KettleEmbeddedQueryEntry extends KettleQueryEntry {
  
  private String pluginId;
  private byte[] raw = null;
  private EmbeddedHelper helper = null;

  public KettleEmbeddedQueryEntry(String aName, String pluginId, byte[] raw) {
    super(aName);
    this.pluginId = pluginId;
    this.raw = raw;
    this.helper = new EmbeddedHelper(pluginId);
  }

  @Override
  protected void loadTransformation(ResourceManager resourceManager, ResourceKey contextKey)
      throws ReportDataFactoryException, KettleException {
    final EmbeddedKettleTransformationProducer producer = (EmbeddedKettleTransformationProducer)createProducer();
    final TransMeta transMeta = producer.loadTransformation(contextKey);
    declaredParameters = transMeta.listParameters();
  }

  @Override
  public String getSelectedStep() {
    return EmbeddedKettleDataFactoryMetaData.DATA_RETRIEVAL_STEP;
  }

  @Override
  public KettleTransformationProducer createProducer(){
    
    try {
      update();
    } catch (ReportDataFactoryException e) {
      e.printStackTrace();
    }
    final String[] argumentFields = getArguments();
    final ParameterMapping[] varNames = getParameters();

    return new EmbeddedKettleTransformationProducer(argumentFields, varNames, pluginId, getSelectedStep(),raw);
  }

  public void repaint(JPanel datasourcePanel, DesignTimeContext designTimeContext, PropertyChangeListener l) {
    StepMeta step = helper.findConfigurationStep((EmbeddedKettleTransformationProducer)createProducer());
    datasourcePanel.removeAll();
    datasourcePanel.add(helper.getDialogPanel(step, designTimeContext, l));
    datasourcePanel.revalidate();
    datasourcePanel.getParent().repaint();
  }

  public void update() throws ReportDataFactoryException{
    try {
      raw = helper.update();
    } catch (Exception e) {
      throw new ReportDataFactoryException("Critical error: Not able to update query entry.", e);
    }
  }
  
}
