package org.pentaho.reporting.ui.datasources.kettle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.trans.step.BaseStepGenericXulDialog;
import org.pentaho.reporting.engine.classic.core.designtime.DesignTimeContext;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.DocumentHelper;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.EmbeddedKettleDataFactoryMetaData;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.EmbeddedKettleTransformationProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulVbox;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class EmbeddedHelper
{
  private static final Log logger = LogFactory.getLog(EmbeddedHelper.class);
  private String id;
  private TransMeta cachedMeta;
  private StepMeta step;
  private BaseStepGenericXulDialog dialog;

  public EmbeddedHelper(String id) 
  {
    this.id = id; 
  }

  public JPanel getDialogPanel(KettleTransformationProducer query, final DesignTimeContext context, PropertyChangeListener l)
  {
    findConfigurationStep(query);
    
    // TODO - throw exception here if step not found... template designer error!
    
    dialog = (BaseStepGenericXulDialog) createDialog(step, context);
    if (l != null)
    {
      dialog.addPropertyChangeListener(l);

      // TODO: FIXME... this is not good... not all XUL definitions will have a  'fieldsTable'
      XulTree fieldsTable = (XulTree) dialog.getXulDomContainer().getDocumentRoot().getElementById("fieldsTable");
      if (fieldsTable != null)
      {
        l.propertyChange(new PropertyChangeEvent(this, "fields",null, fieldsTable.getRows()));
      }
    }
    
    
    XulVbox root = (XulVbox) dialog.getXulDomContainer().getDocumentRoot().getElementById("root");
    JPanel panel = (JPanel)root.getManagedObject();
    return panel;
  }

  private StepDialogInterface createDialog(final StepMeta step, final DesignTimeContext context)
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

  private StepMeta findConfigurationStep(KettleTransformationProducer query)
  {
    
    try
    {
      TransMeta transMeta = (query == null) ? loadTemplate() : loadQuery(query);

      step = transMeta.findStep(EmbeddedKettleDataFactoryMetaData.DATA_CONFIGURATION_STEP);
      step.setParentTransMeta(transMeta);

      cachedMeta = transMeta;

    }catch (Exception e){
      
      cachedMeta = null;
      return null;
      
    }
    
    return step;
  }

  public byte[] update() throws UnsupportedEncodingException, KettleException
  {
    
    dialog.onAccept();
    
    final byte[] rawData = cachedMeta.getXML().getBytes("UTF8");
    return rawData;
    
  }

  public void clear() {
    if (dialog != null)
    {
      dialog.clear();
    }
  }
  
  private TransMeta loadTemplate() throws KettlePluginException, KettleMissingPluginsException, KettleXMLException
  {
    final Document document = DocumentHelper.loadDocumentFromPlugin(id);
    final Node node = XMLHandler.getSubNode(document, TransMeta.XML_TAG);
    final TransMeta meta = new TransMeta();
    meta.loadXML(node, null, true, null, null);
    return meta;
  }

  private TransMeta loadQuery(KettleTransformationProducer query) throws KettlePluginException, KettleMissingPluginsException, KettleXMLException
  {
    final Document document = DocumentHelper.loadDocumentFromBytes(((EmbeddedKettleTransformationProducer)query).getTransformationRaw());
    final Node node = XMLHandler.getSubNode(document, TransMeta.XML_TAG);
    final TransMeta meta = new TransMeta();
    meta.loadXML(node, null, true, null, null);
    return meta;
  }

}
