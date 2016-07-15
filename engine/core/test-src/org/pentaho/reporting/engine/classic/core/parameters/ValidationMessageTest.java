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

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by dima.prokopenko@gmail.com on 7/14/2016.
 */
public class ValidationMessageTest {

  @Test
  public void testValidationMessageType() {
    ValidationMessage[] arr = { new ValidationMessage( "some name" ),
      new ValidationMessage( "some name", ValidationMessage.Type.ERROR ),
      new ValidationMessage( ValidationMessage.Type.ERROR ),
      new ValidationMessage( "some error", new Object() ),
      new ValidationMessage( "some error", new Object(), new Object() ),
      new ValidationMessage( "some error", new Object(), new Object(), new Object() ),
      new ValidationMessage( "some error", new Object[] { new Object(), new Object(), new Object() } ),
      new ValidationMessage( "some error", new Object[] { new Object(), new Object(), new Object() },
        ValidationMessage.Type.ERROR )
    };
    Arrays.asList( arr ).stream().forEach( i -> assertEquals( ValidationMessage.Type.ERROR, i.getType() ) );
  }

  @Test
  public void testSkipValidationCreation() {
    ValidationMessage message = new ValidationMessage( ValidationMessage.Type.SKIP );
    assertEquals( ValidationMessage.Type.SKIP, message.getType() );

    message = new ValidationMessage( "why skip", ValidationMessage.Type.SKIP );
    assertEquals( ValidationMessage.Type.SKIP, message.getType() );
    assertEquals( "why skip", message.getMessage() );
  }
}
