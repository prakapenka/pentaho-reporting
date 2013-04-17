/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.reporting.ui.datasources.kettle;

import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.designtime.DesignTimeContext;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryMetaData;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryRegistry;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.EmbeddedKettleTransformationProducer;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleTransformationProducer;
import org.pentaho.reporting.libraries.base.util.StackableRuntimeException;

/**
 * @author Gretchen Moran
 */
public class EmbeddedKettleDataSourceDialog extends KettleDataSourceDialog
{
  private static final long serialVersionUID = 5030572665265231736L;

  private static final Log logger = LogFactory.getLog(EmbeddedKettleDataSourceDialog.class);

  private String datasourceId = null;
  private JPanel datasourcePanel;
  
  /**
   * This listener is registered with the XUL dialog. A XUL binding 
   * on the fields collection will send a propertyChange notification
   * when the number of fields configured changes... if more than zero, 
   * enable the preview button, otherwise disable. 
   * 
   * @author gmoran
   *
   */
  protected class PreviewChangeListener implements PropertyChangeListener
  {

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
      
      if (evt.getPropertyName().equals("fields"))
      {
        previewAction.setEnabled(((Integer)evt.getNewValue()) > 0);
      }
      
    }
    
  }
  
  private class EmbeddedQueryNameListSelectionListener implements ListSelectionListener
  {
    private EmbeddedQueryNameListSelectionListener()
    {
    }

    public void valueChanged(final ListSelectionEvent e)
    {
      final Object value = queryNameList.getSelectedValue();
      if (value == null)
      {
        nameTextField.setEnabled(false);
        editParameterAction.setEnabled(false);
        setPanelEnabled(false, datasourcePanel);
        return;
      }

      inUpdateFromList = true;
      nameTextField.setEnabled(true);
      setPanelEnabled(true, datasourcePanel);
      
      try
      {

        final KettleEmbeddedQueryEntry selectedQuery = (KettleEmbeddedQueryEntry) value;
        nameTextField.setText(selectedQuery.getName());
        selectedQuery.repaint(datasourcePanel, designTimeContext, new PreviewChangeListener());

        editParameterAction.setEnabled(true);
      }
      catch (Exception e1)
      {
        designTimeContext.error(e1);
        editParameterAction.setEnabled(false);
      }
      catch (Throwable t1)
      {
        designTimeContext.error(new StackableRuntimeException("Fatal error", t1));
        editParameterAction.setEnabled(false);
      }
      finally
      {
        inUpdateFromList = false;
      }
    }
  }

  public EmbeddedKettleDataSourceDialog(final DesignTimeContext designTimeContext, final JDialog parent, String id)
  {
    super(designTimeContext, parent);
    datasourceId = id;
    setTitle(getDialogTitle());
  }

  public EmbeddedKettleDataSourceDialog(final DesignTimeContext designTimeContext, final JFrame parent, String id)
  {
    super(designTimeContext, parent);
    datasourceId = id;
    setTitle(getDialogTitle());
  }

  public EmbeddedKettleDataSourceDialog(final DesignTimeContext designTimeContext, String id)
  {
    super(designTimeContext);
    datasourceId = id;
    setTitle(getDialogTitle());
  }

  @Override
  protected JPanel createDatasourcePanel() 
  {
    datasourcePanel = new JPanel();
    return datasourcePanel;
  }

  private void paintQuery()
  {
    if (datasourcePanel.getComponentCount() <= 0 )
    {
  
      KettleEmbeddedQueryEntry entry = new KettleEmbeddedQueryEntry(null,datasourceId,null);
      entry.repaint(datasourcePanel, designTimeContext, new PreviewChangeListener());
      setPanelEnabled(false, datasourcePanel);
      
    }
  }
  
  protected String getDialogTitle(){

    if (datasourceId == null)
    {
      return "";
    }

    DataFactoryMetaData meta = DataFactoryRegistry.getInstance().getMetaData(datasourceId);
    String displayName = meta.getDisplayName(getLocale());
    return Messages.getString("KettleEmbeddedDataSourceDialog.Title", displayName);
    
  }

  protected String getDialogId()
  {
    return "EmbeddedKettleDataSourceDialog";
  }

  public KettleDataFactory performConfiguration(DesignTimeContext context, final KettleDataFactory dataFactory,
                                                final String queryName)
  {
    queryListModel.clear();

    loadData(dataFactory, queryName);
    if ((dataFactory == null) || (!dataFactory.queriesAreHomogeneous()))
    {
      // allow caller to render the default dialog... we are done here
      return super.performConfiguration(context, dataFactory, queryName);
    } else
    {
      try {
        
        paintQuery();
        if (performEdit() == false)
        {
          return null;
        }
        
      } catch(Exception e){
        context.error(e);
      }
    }
    
    final KettleDataFactory kettleDataFactory = new KettleDataFactory();
    kettleDataFactory.setMetadata(dataFactory.getMetaData());
    for (int i = 0; i < queryListModel.getSize(); i++)
    {
      final KettleQueryEntry queryEntry = (KettleQueryEntry) queryListModel.getElementAt(i);
      final KettleTransformationProducer producer = queryEntry.createProducer();
      kettleDataFactory.setQuery(queryEntry.getName(), producer);
    }

    return kettleDataFactory;

  }
  
  protected KettleQueryEntry getQueryEntry(String queryName, KettleTransformationProducer producer)
  {
    KettleQueryEntry entry = null;
    
    if (datasourceId == null)
    {
      entry = new KettleQueryEntry(queryName);
    }
    else
    {
      byte[] raw = null;
      if ((producer != null) && (producer instanceof EmbeddedKettleTransformationProducer))
      {
        EmbeddedKettleTransformationProducer prod = (EmbeddedKettleTransformationProducer) producer;
        raw = prod.getTransformationRaw();
      }
      
      entry = new KettleEmbeddedQueryEntry(queryName, datasourceId, raw);
    }
    return entry;
  }
  
  protected ListSelectionListener getQueryNameListener()
  {
    return new EmbeddedQueryNameListSelectionListener();
  }
  
  /**
   * This method makes it possible t control any panel that gets rendered via XUL, without 
   * having to create hooks or listeners intot he XUL dialog. The presence of a query object 
   * dictates whether the panel should be anabled or disabled.
   * 
   * @param enable enable/disable the configuration panel
   * @param c 
   */
  private void setPanelEnabled(boolean enable, Component c)
  {
    if (null == c)
    {
        return;
    }
        
    Container container = null;
    if (c instanceof Container)
    {
      container = (Container)c;
    }
    
    if (container != null)
    {
      Component[] components = container.getComponents();
      for (int i = 0; i < container.getComponentCount(); i++) 
      {
        Component component = components[i];
        setPanelEnabled(enable, component);
      }
      
    }
    c.setEnabled(enable);
  }

  @Override
  protected void clearComponents() {
    final KettleEmbeddedQueryEntry kettleQueryEntry = (KettleEmbeddedQueryEntry) queryNameList.getSelectedValue();
    kettleQueryEntry.clear();
    super.clearComponents();
    
  }
  
  
}
