/*
 * Copyright (C) 2016 University of Washington.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.common.persistence;

import java.math.BigDecimal;

/**
 * Wrapper to encapsulate and pass through the special double values of NaN,
 * -Infinity and Infinity while still preserving decimal precision returned from
 * the database layer.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class WrappedBigDecimal implements Comparable<WrappedBigDecimal> {

  private static final String S_NaN = Double.toString(Double.NaN);
  private static final String S_NegInf = Double.toString(Double.NEGATIVE_INFINITY);
  private static final String S_PosInf = Double.toString(Double.POSITIVE_INFINITY);

  public final BigDecimal bd;
  public final Double d;

  private WrappedBigDecimal(WrappedBigDecimal wbdArg) {
    this.d = wbdArg.d;
    this.bd = wbdArg.bd;
  }

  private WrappedBigDecimal(BigDecimal bdArg) {
    if (bdArg == null) {
      throw new IllegalStateException("Unexpected null value");
    } else {
      bd = bdArg;
      d = null;
    }
  }

  public static WrappedBigDecimal fromDouble(Double value) {
    if (value == null) {
      throw new IllegalStateException("Unexpected null value");
    } else {
      return new WrappedBigDecimal(Double.toString(value));
    }
  }

  public WrappedBigDecimal(String value) {
    if (value == null) {
      throw new IllegalStateException("Unexpected null value");
    } else if (value.equals(S_NaN)) {
      d = Double.NaN;
      bd = null;
    } else if (value.equals(S_NegInf)) {
      d = Double.NEGATIVE_INFINITY;
      bd = null;
    } else if (value.equals(S_PosInf)) {
      d = Double.POSITIVE_INFINITY;
      bd = null;
    } else {
      d = null;
      bd = new BigDecimal(value);
    }
  }

  public boolean isSpecialValue() {
    return (d != null);
  }

  public WrappedBigDecimal setScale(int scale, int roundingMode) {
    if (isSpecialValue()) {
      // immutable
      return this;
    }
    return new WrappedBigDecimal(bd.setScale(scale, roundingMode));
  }

  public double doubleValue() {
    if (isSpecialValue()) {
      return d;
    }
    return bd.doubleValue();
  }

  @Override
  public String toString() {
    if (d != null) {
      return Double.toString(d);
    } else {
      return bd.toString();
    }
  }

  @Override
  public int compareTo(WrappedBigDecimal that) {
    // from Double.class
    // ordering is:
    // NEGATIVE_INFINITY
    // decimal value
    // POSITIVE_INFINITY
    // NaN

    if (d == null && that.d == null) {
      // neither is a special value

      // deal with two decimal values
      return bd.compareTo(that.bd);
    }

    if (d == null) {
      // other is special value
      if (that.d == Double.NEGATIVE_INFINITY) {
        // self is greater than negative infinity
        return 1;
      }
      // otherwise, self is less than all other special values
      return -1;
    }

    if (that.d == null) {
      // self is special value
      if (d == Double.NEGATIVE_INFINITY) {
        // self of negative infinity is less than any value
        return -1;
      }
      // otherwise, self is greater than all other values
      return 1;
    }

    // otherwise, two special values
    if (Double.isInfinite(d) && Double.isInfinite(that.d)) {
      return Double.compare(d, that.d);
    } else if (Double.isNaN(d)) {
      if (Double.isNaN(that.d)) {
        return 0;
      } else {
        return 1;
      }
    } else {
      return -1;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (obj instanceof WrappedBigDecimal) {
      WrappedBigDecimal other = (WrappedBigDecimal) obj;
      return other.compareTo(this) == 0;
    }

    WrappedBigDecimal other = new WrappedBigDecimal(obj.toString());
    return other.compareTo(this) == 0;
  }

  @Override
  public int hashCode() {
    if (isSpecialValue()) {
      return (d.hashCode() << 1);
    } else {
      return (bd.hashCode() << 1) + 1;
    }
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return new WrappedBigDecimal(this);
  }

}
