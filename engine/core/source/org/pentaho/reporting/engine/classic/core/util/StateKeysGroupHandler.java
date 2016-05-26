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

package org.pentaho.reporting.engine.classic.core.util;

import org.pentaho.reporting.engine.classic.core.event.ReportEvent;
import org.pentaho.reporting.engine.classic.core.states.ReportStateKey;

/**
 * Created by dima.prokopenko@gmail.com on 5/26/2016.
 */
public abstract class StateKeysGroupHandler<T> implements Cloneable {

  private static final int CAPACITY = 30;

  private transient BulkArrayList<ReportStateKey> stateKeys;
  private transient BulkArrayList<Sequence<T>> sequences;

  private transient Sequence<T> result;

  private transient int lastGroupSequenceNumber;

  private transient ReportStateKey globalStateKey;
  private transient ReportStateKey currentGroupKey;

  public StateKeysGroupHandler() {
    this( CAPACITY );
  }

  public StateKeysGroupHandler( final int capacity ) {
    stateKeys = new BulkArrayList<>( capacity );
    sequences = new BulkArrayList<>( capacity );
    this.result = null;
  }

  public void reportInitialized( final ReportStateKey key, final boolean isPrepareRunLevel ) {
    this.globalStateKey = key;

    if ( isPrepareRunLevel ) {
      result = new Sequence<>();

      this.stateKeys.clear();
      this.sequences.clear();

      // first element is always global state key.
      this.stateKeys.add( globalStateKey );
      // and it's result
      this.sequences.add( result );
    } else {

      if ( sequences.size() > 0 ) {
        result = sequences.get( 0 );
      } else {
        result = null;
      }
    }
    lastGroupSequenceNumber = 0;
  }

  public void groupStarted( final ReportStateKey key, final boolean isPrepareRunLevel ) {
    currentGroupKey = key;

    // assuming most of key is not a global state keys.
    boolean isGlobalKey = ( currentGroupKey.hashCode() == key.hashCode() && currentGroupKey.equals( globalStateKey ) );

    if ( isPrepareRunLevel ) {
      clearResult();

      if ( isGlobalKey ) {
        // global state key is always 0 position
        sequences.set( 0, result );
      } else {
        int pos = groupPos( stateKeys, currentGroupKey );
        if ( pos == -1 ) {
          stateKeys.add( currentGroupKey );
          sequences.add( result );
        } else {
          sequences.set( pos, result );
        }
      }

    } else {
      // Activate the current group, which was filled in the prepare run.
      if ( isGlobalKey ) {
        result = sequences.get( 0 );
      } else {
        int found = groupPos( stateKeys, currentGroupKey );
        if ( found < 0 ) {
          result = null;
        } else {
          result = sequences.get( found );
        }
      }
    }
  }

  public void setLastGroupSequenceNumber( final int number ) {
    this.lastGroupSequenceNumber = number;
  }

  public void setLastGroupResult( T value ) {
    result.set( lastGroupSequenceNumber, value );
  }

  public Sequence<T> getResult() {
    return this.result;
  }

  public ReportStateKey getCurrentGroupKey() {
    return this.currentGroupKey;
  }

  public T getValue() {
    if ( result == null ) {
      return null;
    }

    return result.get( lastGroupSequenceNumber );
  }

  private int groupPos( final BulkArrayList<ReportStateKey> list, final ReportStateKey key ) {
    if ( list.size() == 0 || key == null ) {
      return -1;
    }
    ReportStateKey current;
    for ( int j = list.size() - 1; j > -1; j-- ) {
      current = list.get( j );

      // equal is more expensive then hashCode
      if ( current.hashCode() == key.hashCode() && current.equals( key ) ) {
        return j;
      }
    }
    return -1;
  }

  public abstract void itemsAdvanced( final ReportEvent event );

  @Override
  public Object clone() throws CloneNotSupportedException {

    StateKeysGroupHandler clone = (StateKeysGroupHandler) super.clone();

    // next iteration most probably will be added not much then 2 new keys.
    clone.stateKeys = new BulkArrayList<>( stateKeys.size() + 3 );
    clone.sequences = new BulkArrayList<>( sequences.size() + 3 );

    boolean resultNotNull = result != null;

    if ( resultNotNull ) {
      clone.result = result.clone();
      clone.stateKeys.set( 0, globalStateKey );
      clone.sequences.set( 0, clone.result );
    }

    int shift = 0;
    int currentGroupKeyPosition = -1;
    for ( int i = 1; i <= stateKeys.size() - 1; i++ ) {
      ReportStateKey key = stateKeys.get( i );
      // do not copy current group key
      // a note: 2 objects can not be (==), but still be equal to each other.
      if ( currentGroupKey != key && globalStateKey != key ) {
        // the only key we are really interested in is currentGroupKey.
        // hash code is faster then equals, but according to contract hashCode does not guarantee equality.
        if ( key != null && key.hashCode() == currentGroupKey.hashCode() && key.equals( currentGroupKey ) ) {
          currentGroupKeyPosition = i;
        }
        // this is not a current group key neither global - just put at the tail.
        clone.stateKeys.set( i + shift, key );
        clone.sequences.set( i + shift, sequences.get( i ).clone() );
      } else {
        shift--;
      }
    }

    if ( resultNotNull ) {
      if ( currentGroupKeyPosition == -1 ) {
        clone.stateKeys.add( currentGroupKey );
        clone.sequences.add( clone.result );
      } else if ( currentGroupKeyPosition > 0 ) {
        clone.sequences.set( currentGroupKeyPosition, clone.result );
      }
    }

    return clone;
  }

  public void clearResult() {
    result = new Sequence<>();
    lastGroupSequenceNumber = 0;
  }
}
