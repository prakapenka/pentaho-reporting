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
        return;
      }

      inUpdateFromList = true;
      nameTextField.setEnabled(true);

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
  }

  public EmbeddedKettleDataSourceDialog(final DesignTimeContext designTimeContext, final JFrame parent, String id)
  {
    super(designTimeContext, parent);
    datasourceId = id;
  }

  public EmbeddedKettleDataSourceDialog(final DesignTimeContext designTimeContext, String id)
  {
    super(designTimeContext);
    datasourceId = id;
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
      
    }
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
          return dataFactory;
        }
        
      } catch(Exception e){
        context.error(e);
      }
    }
    
    final KettleDataFactory kettleDataFactory = new KettleDataFactory();
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
}
