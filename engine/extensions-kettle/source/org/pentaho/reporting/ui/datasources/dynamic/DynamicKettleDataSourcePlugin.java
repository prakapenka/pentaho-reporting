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
 * Copyright (c) 2008 - 2009 Pentaho Corporation, .  All rights reserved.
 */

package org.pentaho.reporting.ui.datasources.dynamic;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.DataFactoryPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.datafactory.DynamicDatasource;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.trans.step.BaseStepGenericXulDialog;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.designtime.DataFactoryChangeRecorder;
import org.pentaho.reporting.engine.classic.core.designtime.DataSourcePlugin;
import org.pentaho.reporting.engine.classic.core.designtime.DesignTimeContext;
import org.pentaho.reporting.engine.classic.core.designtime.DesignTimeUtil;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryMetaData;
import org.pentaho.reporting.engine.classic.core.metadata.DataFactoryRegistry;
import org.pentaho.reporting.engine.classic.extensions.datasources.dynamic.DynamicDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleQueryEntry;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;

/**
 * @author Gretchen Moran
 */
public class DynamicKettleDataSourcePlugin implements DataSourcePlugin {
  
  private static final String config_datafactory_class = "org.pentaho.reporting.ui.datasources.dynamic.class";
  private static final String config_plugin_class_id = "org.pentaho.reporting.ui.datasources.dynamic.pluginid";

  private Class<? extends DataFactory> factory;
  
  @SuppressWarnings("unchecked")
  public DynamicKettleDataSourcePlugin()  throws ReportDataFactoryException{
    
    Configuration configuration = ClassicEngineBoot.getInstance().getGlobalConfig();

    try {
      factory =  (Class<? extends DataFactory>) Class.forName(configuration.getConfigProperty(config_datafactory_class));
      
    } catch (ClassNotFoundException e) {
      
      throw new ReportDataFactoryException("Data factory class cannot be loaded. Check the configuration.properties file for the data factory module.");
    
    }

  }

  public boolean canHandle(final DataFactory dataFactory) {
    return factory.isAssignableFrom(dataFactory.getClass());
  }

  private static final Log logger = LogFactory.getLog(DynamicKettleDataSourcePlugin.class);

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public DataFactory performEdit(final DesignTimeContext context, final DataFactory input, final String queryName,
      final DataFactoryChangeRecorder changeRecorder) {
    
    boolean stagedResource = false;
    
    Configuration configuration = ClassicEngineBoot.getInstance().getGlobalConfig();
    String id = configuration.getConfigProperty(config_plugin_class_id);
    
    PluginInterface plugin;
    DynamicDatasource cls;

    // Load the main plugin class, as it holds all of the relevant details we need to realize this 
    // datasource
    try{
      
      plugin = PluginRegistry.getInstance().getPlugin(DataFactoryPluginType.class, id);
      
      cls = (DynamicDatasource)PluginRegistry.getInstance().loadClass(plugin);
    
    }catch(KettlePluginException e){

      logger.error("Critical error, unable to load classes from Kettle plugin. This datasource will not be available.", e);
      return null;
      
    }

    // Load the Kettle template as a resource to the report bundle... 
    TransformationResource resource = new TransformationResource(context, cls.getResourceEntryName());
    
    if (!resource.exists()){
      
      InputStream in = cls.getClass().getResourceAsStream(cls.getTemplate());
      
      try{

        resource.setInputStream(in);
        resource.write();
        stagedResource = true;
        
      }catch(Exception ex){

        logger.error("Critical error, unable to load resource for datasource. This datasource will not be available.", ex);
        return null;
        
      } finally {

        try {
        
          in.close();
          
        } catch (IOException e) {

          logger.error("Non-critical error closing input stream containg Kettle transformation. This should be fixed, the problem can cause applicaton memory leaks.", e);
        
        }
      }
    }


    final String stepName = cls.getStepName();

    final String resourceEntryName = cls.getResourceEntryName();
    
    final String query = cls.getQueryName();

    StepMeta step = null;
    KettleQueryEntry entry = new KettleQueryEntry(query);
    entry.setFile(resourceEntryName);
    entry.setSelectedStep(stepName);

    final MasterReport masterReport = DesignTimeUtil.getMasterReport(context.getReport());
    final ResourceKey key = (masterReport == null) ? null : masterReport.getContentBase();

    BaseStepGenericXulDialog dlg = null;

    // Render datasource specific dialog for editing step details... 
    try {
      
      step = entry.getStep(stepName, context.getReport().getResourceManager(), key);
      
      
      Class<? extends BaseStepGenericXulDialog> dialog = (Class <? extends BaseStepGenericXulDialog>) 
          Class.forName(cls.getDialogClass(), true, cls.getClass().getClassLoader());
      
      Class[] argTypes = {Object.class, BaseStepMeta.class, TransMeta.class, String.class};
      
      Constructor<? extends BaseStepGenericXulDialog> constructor = dialog.getDeclaredConstructor(argTypes);
      
      Object[] args = {context.getParentWindow(), (BaseStepMeta) step.getStepMetaInterface(),
           step.getParentTransMeta(), stepName};
      
      dlg = (BaseStepGenericXulDialog)constructor.newInstance(args);
    
    }  catch (Exception e){
      
      logger.error("Critical error attempting to dynamically create dialog. This datasource will not be available.", e);
      return null;

    }

    // dialog OK button clicked ...
    try {
      if (dlg.open() != null) {

        entry.putStep(step);
        final InputStream fin = entry.getXML();
        resource.setInputStream(fin);
        resource.write();

        
        DataFactory in = null;
        // Return a new Descendant of the dynamic factory for data generation... 
        try {
          in = factory.newInstance();
          ((DynamicDataFactory)in).setQuery(entry.getName(), entry.createProducer());
        
        } catch (Exception e) {

          throw new ReportDataFactoryException("Critical error attempting to dynamically create data factory instance.", e);

        }
        
        return in;
      }else{
        // Cancel button clicked... 
        if (stagedResource){
          resource.remove();
        }
        
      }
    } catch (ReportDataFactoryException e) {
      
      logger.error("Failed to create datasource. This datasource will not be available.", e);
      return null;
    }
    return input;
  }

  public DataFactoryMetaData getMetaData() {
    return DataFactoryRegistry.getInstance().getMetaData(factory.getName());
  }
}

