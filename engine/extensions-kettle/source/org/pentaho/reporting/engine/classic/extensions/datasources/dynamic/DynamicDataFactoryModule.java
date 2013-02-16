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

package org.pentaho.reporting.engine.classic.extensions.datasources.dynamic;

import org.pentaho.reporting.engine.classic.core.metadata.ElementMetaDataParser;
import org.pentaho.reporting.engine.classic.core.modules.parser.base.DataFactoryReadHandlerFactory;
import org.pentaho.reporting.engine.classic.core.modules.parser.base.DataFactoryXmlResourceFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.parser.KettleDataSourceReadHandler;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.parser.KettleTransFromFileReadHandler;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.parser.KettleTransformationProducerReadHandler;
import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.parser.KettleTransformationProducerReadHandlerFactory;
import org.pentaho.reporting.libraries.base.boot.AbstractModule;
import org.pentaho.reporting.libraries.base.boot.ModuleInitializeException;
import org.pentaho.reporting.libraries.base.boot.SubSystem;
import org.pentaho.reporting.libraries.xmlns.parser.XmlFactoryModule;

public abstract class DynamicDataFactoryModule extends AbstractModule
{

  public DynamicDataFactoryModule() throws ModuleInitializeException
  {
    loadModuleInfo();
  }

  /**
   * Initializes the module. Use this method to perform all initial setup operations. This method is called only once in
   * a modules lifetime. If the initializing cannot be completed, throw a ModuleInitializeException to indicate the
   * error,. The module will not be available to the system.
   *
   * @param subSystem the subSystem.
   * @throws ModuleInitializeException if an error ocurred while initializing the module.
   */
  public void initialize(final SubSystem subSystem) throws ModuleInitializeException
  {
    
    DataFactoryXmlResourceFactory.register(getXmlFactoryModule());

    DataFactoryReadHandlerFactory.getInstance().setElementHandler(getNameSpace(), getTagName(), KettleDataSourceReadHandler.class);

    KettleTransformationProducerReadHandlerFactory.getInstance().setElementHandler(getNameSpace(), "query-file", KettleTransFromFileReadHandler.class);
    KettleTransformationProducerReadHandlerFactory.getInstance().setElementHandler(getNameSpace(), "query-repository", KettleTransformationProducerReadHandler.class);

    
    ElementMetaDataParser.initializeOptionalDataFactoryMetaData(getMetaDataFactory());

  }

  /**
   * 
   * @return
   */
  public abstract String getNameSpace();

  /**
   * 
   * @return
   */
  public abstract String getTagDefPrefix();
  
  /**
   * 
   * @return
   */
  public abstract Class <? extends XmlFactoryModule> getXmlFactoryModule();
  
  /**
   * 
   * @return
   */
  public abstract String getMetaDataFactory();
  
  /**
   * 
   * @return
   */
  public abstract String getTagName();
  
}
