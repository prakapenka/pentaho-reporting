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

package org.pentaho.reporting.engine.classic.extensions.datasources.mongodb;

import org.pentaho.reporting.engine.classic.extensions.datasources.dynamic.DynamicDataFactoryModule;
import org.pentaho.reporting.engine.classic.extensions.datasources.mongodb.parser.MongoDbDataSourceXmlFactoryModule;
import org.pentaho.reporting.libraries.base.boot.ModuleInitializeException;
import org.pentaho.reporting.libraries.xmlns.parser.XmlFactoryModule;

/**
 * The next step is to dynamically generate this class and its dependent classes/resources/properties. 
 * 
 * TODO: Talk with Thomas about how to handle NAMESPACE and other datasource specific attributes in the 
 * parser and writer classes.
 *  
 * @author gmoran
 *
 */
public class MongoDbDataFactoryModule extends DynamicDataFactoryModule
{


  public MongoDbDataFactoryModule() throws ModuleInitializeException
  {
    super();
  }

  @Override
  public String getNameSpace() {
    return "http://jfreereport.sourceforge.net/namespaces/datasources/mongodb";
  }

  @Override
  public String getTagDefPrefix() {
    return "org.pentaho.reporting.engine.classic.extensions.datasources.mongodb.tag-def.";
  }

  @Override
  public Class<? extends XmlFactoryModule> getXmlFactoryModule() {
    return MongoDbDataSourceXmlFactoryModule.class;
  }

  @Override
  public String getMetaDataFactory() {
    return "org/pentaho/reporting/engine/classic/extensions/datasources/mongodb/meta-datafactory.xml";
  }

  @Override
  public String getTagName() {
    return "mongodb-datasource";
  }
}
