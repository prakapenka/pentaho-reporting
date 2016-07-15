/*
 * This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 *  Foundation.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this
 *  program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *  or from the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  Copyright (c) 2006 - 2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.parameters;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by dima.prokopenko@gmail.com on 7/14/2016.
 */
public class ValidationResultTest {

  @Test
  public void testSkipValidation() {
    ValidationResult result = new ValidationResult();

    result.addError( "someName", new ValidationMessage( "error1" ) );
    result.addError( "someName", new ValidationMessage( "error2" ) );
    result.skipParameterValidation( "someName" );

    assertEquals( 2 , result.getErrors( "someName" ).length );
  }

  @Test
  public void testSkipDontMessWithErrors() {
    ValidationResult result = new ValidationResult();

    result.addError( new ValidationMessage( "error1" ) );
    result.addError( new ValidationMessage( "error2" ) );
    result.addError( new ValidationMessage( ValidationMessage.Type.SKIP ) );

    assertEquals( 2 , result.getErrors().length );
  }

  @Test
  public void dontNullPointerIfNoErrors() {
    ValidationResult result = new ValidationResult();
    assertEquals( 0 , result.getErrors().length );
    assertEquals( 0 , result.getErrors( "someName" ).length );
  }
}
