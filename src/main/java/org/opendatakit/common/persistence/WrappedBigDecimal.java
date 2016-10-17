package org.opendatakit.common.persistence;

import java.math.BigDecimal;

public class WrappedBigDecimal implements Comparable<WrappedBigDecimal> {
	private static final String S_NaN = Double.toString(Double.NaN);
	private static final String S_NegInf = Double.toString(Double.NEGATIVE_INFINITY);
	private static final String S_PosInf = Double.toString(Double.POSITIVE_INFINITY);

	public final BigDecimal bd;
	public final Double d;
	
	private WrappedBigDecimal(BigDecimal bdArg) {
		if ( bdArg == null ) {
			throw new IllegalStateException("Unexpected null value");
		} else {
			bd = bdArg;
			d = null;
		}
	}
	
	public static WrappedBigDecimal fromDouble(Double value) {
		if ( value == null ) {
			throw new IllegalStateException("Unexpected null value");
		} else {
			return new WrappedBigDecimal(Double.toString(value));
		}
	}
	
	public WrappedBigDecimal(String value) {
		if ( value == null ) {
			throw new IllegalStateException("Unexpected null value");
		} else if ( value.equals(S_NaN) ) {
			d = Double.NaN;
			bd = null;
		} else if ( value.equals(S_NegInf) ) {
			d = Double.NEGATIVE_INFINITY;
			bd = null;
		} else if ( value.equals(S_PosInf) ) {
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
		if ( isSpecialValue() ) {
			return d;
		}
		return bd.doubleValue();
	}
	
	@Override
	public String toString() {
		if ( d != null ) {
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

		if ( d == null && that.d == null ) {
			// neither is a special value
			
			// deal with two decimal values
			return bd.compareTo(that.bd);
		}
		
		if ( d == null ) {
			// other is special value
			if ( that.d == Double.NEGATIVE_INFINITY ) {
				// self is greater than negative infinity
				return 1;
			}
			// otherwise, self is less than all other special values
			return -1;
		}
		
		if ( that.d == null ) {
			// self is special value
			if ( d == Double.NEGATIVE_INFINITY ) {
				// self of negative infinity is less than any value
				return -1;
			}
			// otherwise, self is greater than all other values
			return 1;
		}
		
		// otherwise, two special values
		if ( d == that.d ) {
			return 0;
		} else if ( d == Double.NEGATIVE_INFINITY ) {
			// self negative is less than all other values (equality not possible)
			return -1;
		} else if ( that.d == Double.NEGATIVE_INFINITY ) {
			// other negative makes self greater than other
			return 1;
		} else if ( d == Double.POSITIVE_INFINITY ) {
			// self positive is less than other NaN
			return -1;
		} else if ( that.d == Double.POSITIVE_INFINITY ) {
			// other positive is less than self NaN
			return 1;
		} else {
			throw new IllegalStateException("should have decided by now");
		}
	}
}
