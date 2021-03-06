/*
Copyright (c) 2010 James Ahlborn

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA

*/

package com.healthmarketscience.jackcess;

import org.apache.commons.lang.ObjectUtils;

/**
 * Simple concrete implementation of ColumnMatcher which test for equality.
 *
 * @author James Ahlborn
 */
public class SimpleColumnMatcher implements ColumnMatcher {

  public static final SimpleColumnMatcher INSTANCE = new SimpleColumnMatcher();

  public SimpleColumnMatcher() {
  }

  public boolean matches(Table table, String columnName, Object value1,
                         Object value2)
  {
    return ObjectUtils.equals(value1, value2);
  }
}
