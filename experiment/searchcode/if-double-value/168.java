<<<<<<< HEAD
/**
 * Created on 09.11.2002
 * 
 * To change this generated comment edit the template variable "filecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of file
 * comments go to Window>Preferences>Java>Code Generation.
 */
package ru.myx.ae3.help;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author barachta
 * 
 *         To change this generated comment edit the template variable
 *         "typecomment": Window>Preferences>Java>Templates. To enable and
 *         disable the creation of type comments go to
 *         Window>Preferences>Java>Code Generation.
 */
public class Format {
	/**
	 * Inexact, Human readable
	 */
	public static final class Compact {
		private static final class NumberFormatter {
			private static final int			CAPACITY	= 32;
			
			private static volatile int			counter		= 0;
			
			private static final NumberFormat[]	POOL1		= new NumberFormat[NumberFormatter.CAPACITY];
			
			private static final NumberFormat[]	POOL2		= new NumberFormat[NumberFormatter.CAPACITY];
			
			private static final NumberFormat[]	POOL3		= new NumberFormat[NumberFormatter.CAPACITY];
			
			NumberFormatter() {
				for (int i = 0; i < NumberFormatter.CAPACITY; i++) {
					NumberFormatter.POOL1[i] = NumberFormat.getInstance( Locale.ROOT );
					NumberFormatter.POOL1[i].setMaximumFractionDigits( 1 );
					NumberFormatter.POOL1[i].setMinimumFractionDigits( 0 );
					NumberFormatter.POOL1[i].setMinimumIntegerDigits( 1 );
					NumberFormatter.POOL1[i].setGroupingUsed( false );
					NumberFormatter.POOL1[i].setRoundingMode( RoundingMode.HALF_UP );
					
					NumberFormatter.POOL2[i] = NumberFormat.getInstance( Locale.ROOT );
					NumberFormatter.POOL2[i].setMaximumFractionDigits( 2 );
					NumberFormatter.POOL2[i].setMinimumFractionDigits( 0 );
					NumberFormatter.POOL2[i].setMinimumIntegerDigits( 1 );
					NumberFormatter.POOL2[i].setGroupingUsed( false );
					NumberFormatter.POOL2[i].setRoundingMode( RoundingMode.HALF_UP );
					
					NumberFormatter.POOL3[i] = NumberFormat.getInstance( Locale.ROOT );
					NumberFormatter.POOL3[i].setMaximumFractionDigits( 3 );
					NumberFormatter.POOL3[i].setMinimumFractionDigits( 0 );
					NumberFormatter.POOL3[i].setMinimumIntegerDigits( 0 );
					NumberFormatter.POOL3[i].setGroupingUsed( false );
					NumberFormatter.POOL3[i].setRoundingMode( RoundingMode.HALF_UP );
				}
			}
			
			String format1(final double d) {
				final int index = NumberFormatter.counter++;
				final NumberFormat current = NumberFormatter.POOL1[index % NumberFormatter.CAPACITY];
				synchronized (current) {
					return current.format( d );
				}
			}
			
			String format2(final double d) {
				final int index = NumberFormatter.counter++;
				final NumberFormat current = NumberFormatter.POOL2[index % NumberFormatter.CAPACITY];
				synchronized (current) {
					return current.format( d );
				}
			}
			
			String format3(final double d) {
				final int index = NumberFormatter.counter++;
				final NumberFormat current = NumberFormatter.POOL3[index % NumberFormatter.CAPACITY];
				synchronized (current) {
					return current.format( d );
				}
			}
		}
		
		private static final DateFormatterCompact	DATE		= new DateFormatterCompact();
		
		private static final NumberFormatter		FORMATTER	= new NumberFormatter();
		
		/**
		 * @param date
		 * @return string
		 */
		public static final String date(final Date date) {
			return Compact.DATE.format( date );
		}
		
		/**
		 * @param time
		 * @return string
		 */
		public static final String date(final long time) {
			return Compact.DATE.format( time );
		}
		
		/**
		 * @param date
		 * @return string
		 */
		public static final String dateRelative(final Date date) {
			return Compact.DATE.formatRelative( date );
		}
		
		/**
		 * @param time
		 * @return string
		 */
		public static final String dateRelative(final long time) {
			return Compact.DATE.formatRelative( time );
		}
		
		/**
		 * @param value
		 * @return string
		 */
		public static final String toBytes(final double value) {
			if (value < 1000L) {
				if (value < 0) {
					return '-' + Format.Compact.toBytes( -value );
				}
				if (value >= 1) {
					return Compact.FORMATTER.format2( value ) + ' ';
				}
				if (value >= Format.DOUBLE_MILLI) {
					return Compact.FORMATTER.format2( value / Format.DOUBLE_MILLI_BYTES ) + " ml";
				}
				if (value >= Format.DOUBLE_MICRO) {
					return Compact.FORMATTER.format2( value / Format.DOUBLE_MICRO_BYTES ) + " mk";
				}
				return Compact.FORMATTER.format2( value / Format.DOUBLE_NANO_BYTES ) + " n";
			}
			if (value >= 1000L * 1000L * 1000L * 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_TERA_BYTES ) + " T";
			}
			if (value >= 1000L * 1000L * 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_GIGA_BYTES ) + " G";
			}
			if (value >= 1000L * 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_MEGA_BYTES ) + " M";
			}
			if (value >= 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_KILO_BYTES ) + " k";
			}
			return "n/a";
		}
		
		/**
		 * @param value
		 * @return string
		 */
		public static final String toBytes(final long value) {
			if (value < 0) {
				return '-' + Format.Compact.toBytes( -value );
			}
			if (value < 1000L) {
				return String.valueOf( value ) + ' ';
			}
			if (value >= 1000L * 1000L * 1000L * 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_TERA_BYTES ) + " T";
			}
			if (value >= 1000L * 1000L * 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_GIGA_BYTES ) + " G";
			}
			if (value >= 1000L * 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_MEGA_BYTES ) + " M";
			}
			return Compact.FORMATTER.format2( value / Format.DOUBLE_KILO_BYTES ) + " k";
		}
		
		/**
		 * @param value
		 * @return string
		 */
		public static final String toDecimal(final double value) {
			if (Double.isInfinite( value )) {
				return value > 0
						? "+inf"
						: "-inf";
			}
			if (Double.isNaN( value )) {
				return "NaN";
			}
			if (value < 0) {
				return '-' + Format.Compact.toDecimal( -value );
			}
			if (value < Format.DOUBLE_NANO) {
				return String.valueOf( value );
			}
			if (value >= 100L * 1000L * 1000L * 1000L * 1000L) {
				return Compact.FORMATTER.format1( value / Format.DOUBLE_TERA ) + "T";
			}
			if (value >= 1000L * 1000L * 1000L * 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_TERA ) + "T";
			}
			if (value >= 100L * 1000L * 1000L * 1000L) {
				return Compact.FORMATTER.format1( value / Format.DOUBLE_GIGA ) + "G";
			}
			if (value >= 1000L * 1000L * 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_GIGA ) + "G";
			}
			if (value >= 100L * 1000L * 1000L) {
				return Compact.FORMATTER.format1( value / Format.DOUBLE_MEGA ) + "M";
			}
			if (value >= 1000L * 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_MEGA ) + "M";
			}
			if (value >= 100L * 1000L) {
				return Compact.FORMATTER.format1( value / Format.DOUBLE_KILO ) + "k";
			}
			if (value >= 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_KILO ) + "k";
			}
			if (value >= 20L) {
				return Compact.FORMATTER.format1( value );
			}
			if (value >= 1L) {
				return Compact.FORMATTER.format2( value );
			}
			if (value >= Format.DOUBLE_MILLI) {
				if (value >= Format.DOUBLE_MILLI * 10) {
					return Compact.FORMATTER.format3( (int) (value * 1000) / 1000.0 );
				}
				return Compact.FORMATTER.format2( value / Format.DOUBLE_MILLI ) + "ml";
			}
			if (value >= Format.DOUBLE_MICRO) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_MICRO ) + "mk";
			}
			return Compact.FORMATTER.format2( value / Format.DOUBLE_NANO ) + "n";
		}
		
		/**
		 * @param value
		 * @return string
		 */
		public static final String toDecimal(final long value) {
			if (value < 0) {
				return '-' + Format.Compact.toDecimal( -value );
			}
			if (value >= 100L * 1000L * 1000L * 1000L * 1000L) {
				return Compact.FORMATTER.format1( value / Format.DOUBLE_TERA ) + "T";
			}
			if (value >= 1000L * 1000L * 1000L * 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_TERA ) + "T";
			}
			if (value >= 100L * 1000L * 1000L * 1000L) {
				return Compact.FORMATTER.format1( value / Format.DOUBLE_GIGA ) + "G";
			}
			if (value >= 1000L * 1000L * 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_GIGA ) + "G";
			}
			if (value >= 100L * 1000L * 1000L) {
				return Compact.FORMATTER.format1( value / Format.DOUBLE_MEGA ) + "M";
			}
			if (value >= 1000L * 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_MEGA ) + "M";
			}
			if (value >= 100L * 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_KILO ) + "k";
			}
			if (value >= 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_KILO ) + "k";
			}
			if (value >= 100L) {
				return Compact.FORMATTER.format1( value );
			}
			return Compact.FORMATTER.format2( value );
		}
		
		/**
		 * @param value
		 * @return string
		 */
		public static final String toPeriod(final double value) {
			if (value <= 0) {
				return String.valueOf( value );
			}
			if (value >= Format.DOUBLE_WEEK_PERIOD) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_WEEK_PERIOD ) + " week(s)";
			}
			if (value >= Format.DOUBLE_DAY_PERIOD) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_DAY_PERIOD ) + " day(s)";
			}
			if (value >= Format.DOUBLE_HOUR_PERIOD) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_HOUR_PERIOD ) + " hour(s)";
			}
			if (value >= Format.DOUBLE_MINUTE_PERIOD) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_MINUTE_PERIOD ) + " minute(s)";
			}
			if (value >= Format.DOUBLE_SECOND_PERIOD) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_SECOND_PERIOD ) + " second(s)";
			}
			if (value >= Format.DOUBLE_MILLISECOND_PERIOD) {
				// return formatter.format(value / dMILLISECOND_PERIOD) + " ms";
				return Compact.FORMATTER.format2( value ) + " ms";
			}
			if (value >= Format.DOUBLE_MICROSECOND_PERIOD) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_MICROSECOND_PERIOD ) + " mks";
			}
			return Compact.FORMATTER.format2( value / Format.DOUBLE_NANOSECOND_PERIOD ) + " nanos";
		}
		
		/**
		 * @param value
		 * @return string
		 */
		public static final String toPeriod(final long value) {
			if (value <= 0) {
				return String.valueOf( value );
			}
			if (value >= 1000L * 60L * 60L * 24L * 7L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_WEEK_PERIOD ) + " week(s)";
			}
			if (value >= 1000L * 60L * 60L * 24L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_DAY_PERIOD ) + " day(s)";
			}
			if (value >= 1000L * 60L * 60L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_HOUR_PERIOD ) + " hour(s)";
			}
			if (value >= 1000L * 60L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_MINUTE_PERIOD ) + " minute(s)";
			}
			if (value >= 1000L) {
				return Compact.FORMATTER.format2( value / Format.DOUBLE_SECOND_PERIOD ) + " second(s)";
			}
			return Compact.FORMATTER.format2( value ) + " ms";
		}
		
		private Compact() {
			// empty
		}
		
	}
	
	/**
	 * Common web formatting
	 */
	public static final class Ecma {
		private static final DateFormatterEcma	DATE	= new DateFormatterEcma();
		
		/**
		 * @param date
		 * @return string
		 */
		public static final String date(final Date date) {
			return Ecma.DATE.format( date );
		}
		
		/**
		 * @param time
		 * @return string
		 */
		public static final String date(final long time) {
			return Ecma.DATE.format( time );
		}
		
		/**
		 * Returns -1 on error or parsed HTTP date.
		 * 
		 * @param string
		 * @return date
		 */
		public static final long parse(final String string) {
			return Ecma.DATE.parse( string );
		}
		
		private Ecma() {
			// empty
		}
	}
	
	/**
	 * Exact, Machine/Human readable
	 */
	public static final class Exact {
		/**
		 * @param value
		 * @return string
		 */
		public static final String toBytes(double value) {
			if (value <= 0) {
				return String.valueOf( value );
			}
			final StringBuilder result = new StringBuilder( 64 );
			if (value > 1024L * 1024L * 1024L * 1024L) {
				final int ml = (int) (value / (1024L * 1024L * 1024L * 1024L));
				value -= 1024L * 1024L * 1024L * 1024L * ml;
				result.append( ml ).append( 'T' );
			}
			if (value > 1024L * 1024L * 1024L) {
				final int ml = (int) (value / (1024L * 1024L * 1024L));
				value -= 1024L * 1024L * 1024L * ml;
				result.append( ml ).append( 'G' );
			}
			if (value > 1024L * 1024L) {
				final int ml = (int) (value / (1024L * 1024L));
				value -= 1024L * 1024L * ml;
				result.append( ml ).append( 'M' );
			}
			if (value > 1024L) {
				final int ml = (int) (value / 1024L);
				value -= 1024L * ml;
				result.append( ml ).append( 'k' );
			}
			if (value > 0) {
				result.append( value );
			}
			return result.toString();
		}
		
		/**
		 * @param value
		 * @return string
		 */
		public static final String toBytes(long value) {
			if (value <= 0) {
				return String.valueOf( value );
			}
			final StringBuilder result = new StringBuilder( 64 );
			if (value > 1024L * 1024L * 1024L * 1024L) {
				final int ml = (int) (value / (1024L * 1024L * 1024L * 1024L));
				value -= 1024L * 1024L * 1024L * 1024L * ml;
				result.append( ml ).append( 'T' );
			}
			if (value > 1024L * 1024L * 1024L) {
				final int ml = (int) (value / (1024L * 1024L * 1024L));
				value -= 1024L * 1024L * 1024L * ml;
				result.append( ml ).append( 'G' );
			}
			if (value > 1024L * 1024L) {
				final int ml = (int) (value / (1024L * 1024L));
				value -= 1024L * 1024L * ml;
				result.append( ml ).append( 'M' );
			}
			if (value > 1024L) {
				final int ml = (int) (value / 1024L);
				value -= 1024L * ml;
				result.append( ml ).append( 'k' );
			}
			if (value > 0) {
				result.append( value );
			}
			return result.toString();
		}
		
		/**
		 * @param value
		 * @return string
		 */
		public static final String toPeriod(long value) {
			if (value <= 0) {
				return String.valueOf( value );
			}
			final StringBuilder result = new StringBuilder( 64 );
			if (value > 1000L * 60L * 60L * 24L * 7L) {
				final int ml = (int) (value / (1000L * 60L * 60L * 24L * 7L));
				value -= 1000L * 60L * 60L * 24L * 7L * ml;
				result.append( ml ).append( 'w' );
			}
			if (value > 1000L * 60L * 60L * 24L) {
				final int ml = (int) (value / (1000L * 60L * 60L * 24L));
				value -= 1000L * 60L * 60L * 24L * ml;
				result.append( ml ).append( 'd' );
			}
			if (value > 1000L * 60L * 60L) {
				final int ml = (int) (value / (1000L * 60L * 60L));
				value -= 1000L * 60L * 60L * ml;
				result.append( ml ).append( 'h' );
			}
			if (value > 1000L * 60L) {
				final int ml = (int) (value / (1000L * 60L));
				value -= 1000L * 60L * ml;
				result.append( ml ).append( 'm' );
			}
			if (value > 1000L) {
				final int ml = (int) (value / 1000L);
				value -= 1000L * ml;
				result.append( ml ).append( 's' );
			}
			if (value > 0) {
				result.append( value );
			}
			return result.toString();
		}
		
		private Exact() {
			// empty
		}
	}
	
	/**
	 * Common web formatting
	 */
	public static final class Web {
		private static final DateFormatterWeb	DATE	= new DateFormatterWeb();
		
		/**
		 * @param date
		 * @return string
		 */
		public static final String date(final Date date) {
			return Web.DATE.format( date );
		}
		
		/**
		 * @param time
		 * @return string
		 */
		public static final String date(final long time) {
			return Web.DATE.format( time );
		}
		
		/**
		 * Returns -1 on error or parsed HTTP date.
		 * 
		 * @param string
		 * @return date
		 */
		public static final long parse(final String string) {
			return Web.DATE.parse( string );
		}
		
		private Web() {
			// empty
		}
	}
	
	/**
	 * 
	 */
	public static final double	DOUBLE_KILO_BYTES			= 1024L;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_MEGA_BYTES			= 1024L * 1024L;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_GIGA_BYTES			= 1024L * 1024L * 1024L;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_TERA_BYTES			= 1024L * 1024L * 1024L * 1024L;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_MILLI_BYTES			= Format.DOUBLE_KILO_BYTES / Format.DOUBLE_MEGA_BYTES;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_MICRO_BYTES			= Format.DOUBLE_KILO_BYTES / Format.DOUBLE_GIGA_BYTES;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_NANO_BYTES			= Format.DOUBLE_KILO_BYTES / Format.DOUBLE_TERA_BYTES;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_KILO					= 1000L;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_MEGA					= 1000L * 1000L;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_GIGA					= 1000L * 1000L * 1000L;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_TERA					= 1000L * 1000L * 1000L * 1000L;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_MILLI				= 1000L / Format.DOUBLE_MEGA;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_MICRO				= 1000L / Format.DOUBLE_GIGA;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_NANO					= 1000L / Format.DOUBLE_TERA;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_SECOND_PERIOD		= 1000L;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_MINUTE_PERIOD		= 1000L * 60L;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_HOUR_PERIOD			= 1000L * 60L * 60L;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_DAY_PERIOD			= 1000L * 60L * 60L * 24L;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_WEEK_PERIOD			= 1000L * 60L * 60L * 24L * 7L;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_MILLISECOND_PERIOD	= 1.0;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_MICROSECOND_PERIOD	= Format.DOUBLE_MILLISECOND_PERIOD / 1000.0;
	
	/**
	 * 
	 */
	public static final double	DOUBLE_NANOSECOND_PERIOD	= Format.DOUBLE_MICROSECOND_PERIOD / 1000.0;
	
	private Format() {
		// empty
	}
}
=======
/**
 * Copyright (C) 2009-2013 BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bimserver.models.ifc2x3tc1;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Ifc Mechanical Material Properties</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getDynamicViscosity <em>Dynamic Viscosity</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getDynamicViscosityAsString <em>Dynamic Viscosity As String</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getYoungModulus <em>Young Modulus</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getYoungModulusAsString <em>Young Modulus As String</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getShearModulus <em>Shear Modulus</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getShearModulusAsString <em>Shear Modulus As String</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getPoissonRatio <em>Poisson Ratio</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getPoissonRatioAsString <em>Poisson Ratio As String</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getThermalExpansionCoefficient <em>Thermal Expansion Coefficient</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getThermalExpansionCoefficientAsString <em>Thermal Expansion Coefficient As String</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcMechanicalMaterialProperties()
 * @model
 * @generated
 */
public interface IfcMechanicalMaterialProperties extends IfcMaterialProperties {
	/**
	 * Returns the value of the '<em><b>Dynamic Viscosity</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Dynamic Viscosity</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Dynamic Viscosity</em>' attribute.
	 * @see #isSetDynamicViscosity()
	 * @see #unsetDynamicViscosity()
	 * @see #setDynamicViscosity(double)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcMechanicalMaterialProperties_DynamicViscosity()
	 * @model unsettable="true"
	 * @generated
	 */
	double getDynamicViscosity();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getDynamicViscosity <em>Dynamic Viscosity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dynamic Viscosity</em>' attribute.
	 * @see #isSetDynamicViscosity()
	 * @see #unsetDynamicViscosity()
	 * @see #getDynamicViscosity()
	 * @generated
	 */
	void setDynamicViscosity(double value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getDynamicViscosity <em>Dynamic Viscosity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetDynamicViscosity()
	 * @see #getDynamicViscosity()
	 * @see #setDynamicViscosity(double)
	 * @generated
	 */
	void unsetDynamicViscosity();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getDynamicViscosity <em>Dynamic Viscosity</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Dynamic Viscosity</em>' attribute is set.
	 * @see #unsetDynamicViscosity()
	 * @see #getDynamicViscosity()
	 * @see #setDynamicViscosity(double)
	 * @generated
	 */
	boolean isSetDynamicViscosity();

	/**
	 * Returns the value of the '<em><b>Dynamic Viscosity As String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Dynamic Viscosity As String</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Dynamic Viscosity As String</em>' attribute.
	 * @see #isSetDynamicViscosityAsString()
	 * @see #unsetDynamicViscosityAsString()
	 * @see #setDynamicViscosityAsString(String)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcMechanicalMaterialProperties_DynamicViscosityAsString()
	 * @model unsettable="true"
	 * @generated
	 */
	String getDynamicViscosityAsString();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getDynamicViscosityAsString <em>Dynamic Viscosity As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dynamic Viscosity As String</em>' attribute.
	 * @see #isSetDynamicViscosityAsString()
	 * @see #unsetDynamicViscosityAsString()
	 * @see #getDynamicViscosityAsString()
	 * @generated
	 */
	void setDynamicViscosityAsString(String value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getDynamicViscosityAsString <em>Dynamic Viscosity As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetDynamicViscosityAsString()
	 * @see #getDynamicViscosityAsString()
	 * @see #setDynamicViscosityAsString(String)
	 * @generated
	 */
	void unsetDynamicViscosityAsString();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getDynamicViscosityAsString <em>Dynamic Viscosity As String</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Dynamic Viscosity As String</em>' attribute is set.
	 * @see #unsetDynamicViscosityAsString()
	 * @see #getDynamicViscosityAsString()
	 * @see #setDynamicViscosityAsString(String)
	 * @generated
	 */
	boolean isSetDynamicViscosityAsString();

	/**
	 * Returns the value of the '<em><b>Young Modulus</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Young Modulus</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Young Modulus</em>' attribute.
	 * @see #isSetYoungModulus()
	 * @see #unsetYoungModulus()
	 * @see #setYoungModulus(double)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcMechanicalMaterialProperties_YoungModulus()
	 * @model unsettable="true"
	 * @generated
	 */
	double getYoungModulus();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getYoungModulus <em>Young Modulus</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Young Modulus</em>' attribute.
	 * @see #isSetYoungModulus()
	 * @see #unsetYoungModulus()
	 * @see #getYoungModulus()
	 * @generated
	 */
	void setYoungModulus(double value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getYoungModulus <em>Young Modulus</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetYoungModulus()
	 * @see #getYoungModulus()
	 * @see #setYoungModulus(double)
	 * @generated
	 */
	void unsetYoungModulus();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getYoungModulus <em>Young Modulus</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Young Modulus</em>' attribute is set.
	 * @see #unsetYoungModulus()
	 * @see #getYoungModulus()
	 * @see #setYoungModulus(double)
	 * @generated
	 */
	boolean isSetYoungModulus();

	/**
	 * Returns the value of the '<em><b>Young Modulus As String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Young Modulus As String</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Young Modulus As String</em>' attribute.
	 * @see #isSetYoungModulusAsString()
	 * @see #unsetYoungModulusAsString()
	 * @see #setYoungModulusAsString(String)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcMechanicalMaterialProperties_YoungModulusAsString()
	 * @model unsettable="true"
	 * @generated
	 */
	String getYoungModulusAsString();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getYoungModulusAsString <em>Young Modulus As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Young Modulus As String</em>' attribute.
	 * @see #isSetYoungModulusAsString()
	 * @see #unsetYoungModulusAsString()
	 * @see #getYoungModulusAsString()
	 * @generated
	 */
	void setYoungModulusAsString(String value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getYoungModulusAsString <em>Young Modulus As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetYoungModulusAsString()
	 * @see #getYoungModulusAsString()
	 * @see #setYoungModulusAsString(String)
	 * @generated
	 */
	void unsetYoungModulusAsString();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getYoungModulusAsString <em>Young Modulus As String</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Young Modulus As String</em>' attribute is set.
	 * @see #unsetYoungModulusAsString()
	 * @see #getYoungModulusAsString()
	 * @see #setYoungModulusAsString(String)
	 * @generated
	 */
	boolean isSetYoungModulusAsString();

	/**
	 * Returns the value of the '<em><b>Shear Modulus</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Shear Modulus</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Shear Modulus</em>' attribute.
	 * @see #isSetShearModulus()
	 * @see #unsetShearModulus()
	 * @see #setShearModulus(double)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcMechanicalMaterialProperties_ShearModulus()
	 * @model unsettable="true"
	 * @generated
	 */
	double getShearModulus();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getShearModulus <em>Shear Modulus</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Shear Modulus</em>' attribute.
	 * @see #isSetShearModulus()
	 * @see #unsetShearModulus()
	 * @see #getShearModulus()
	 * @generated
	 */
	void setShearModulus(double value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getShearModulus <em>Shear Modulus</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetShearModulus()
	 * @see #getShearModulus()
	 * @see #setShearModulus(double)
	 * @generated
	 */
	void unsetShearModulus();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getShearModulus <em>Shear Modulus</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Shear Modulus</em>' attribute is set.
	 * @see #unsetShearModulus()
	 * @see #getShearModulus()
	 * @see #setShearModulus(double)
	 * @generated
	 */
	boolean isSetShearModulus();

	/**
	 * Returns the value of the '<em><b>Shear Modulus As String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Shear Modulus As String</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Shear Modulus As String</em>' attribute.
	 * @see #isSetShearModulusAsString()
	 * @see #unsetShearModulusAsString()
	 * @see #setShearModulusAsString(String)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcMechanicalMaterialProperties_ShearModulusAsString()
	 * @model unsettable="true"
	 * @generated
	 */
	String getShearModulusAsString();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getShearModulusAsString <em>Shear Modulus As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Shear Modulus As String</em>' attribute.
	 * @see #isSetShearModulusAsString()
	 * @see #unsetShearModulusAsString()
	 * @see #getShearModulusAsString()
	 * @generated
	 */
	void setShearModulusAsString(String value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getShearModulusAsString <em>Shear Modulus As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetShearModulusAsString()
	 * @see #getShearModulusAsString()
	 * @see #setShearModulusAsString(String)
	 * @generated
	 */
	void unsetShearModulusAsString();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getShearModulusAsString <em>Shear Modulus As String</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Shear Modulus As String</em>' attribute is set.
	 * @see #unsetShearModulusAsString()
	 * @see #getShearModulusAsString()
	 * @see #setShearModulusAsString(String)
	 * @generated
	 */
	boolean isSetShearModulusAsString();

	/**
	 * Returns the value of the '<em><b>Poisson Ratio</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Poisson Ratio</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Poisson Ratio</em>' attribute.
	 * @see #isSetPoissonRatio()
	 * @see #unsetPoissonRatio()
	 * @see #setPoissonRatio(double)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcMechanicalMaterialProperties_PoissonRatio()
	 * @model unsettable="true"
	 * @generated
	 */
	double getPoissonRatio();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getPoissonRatio <em>Poisson Ratio</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Poisson Ratio</em>' attribute.
	 * @see #isSetPoissonRatio()
	 * @see #unsetPoissonRatio()
	 * @see #getPoissonRatio()
	 * @generated
	 */
	void setPoissonRatio(double value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getPoissonRatio <em>Poisson Ratio</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetPoissonRatio()
	 * @see #getPoissonRatio()
	 * @see #setPoissonRatio(double)
	 * @generated
	 */
	void unsetPoissonRatio();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getPoissonRatio <em>Poisson Ratio</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Poisson Ratio</em>' attribute is set.
	 * @see #unsetPoissonRatio()
	 * @see #getPoissonRatio()
	 * @see #setPoissonRatio(double)
	 * @generated
	 */
	boolean isSetPoissonRatio();

	/**
	 * Returns the value of the '<em><b>Poisson Ratio As String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Poisson Ratio As String</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Poisson Ratio As String</em>' attribute.
	 * @see #isSetPoissonRatioAsString()
	 * @see #unsetPoissonRatioAsString()
	 * @see #setPoissonRatioAsString(String)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcMechanicalMaterialProperties_PoissonRatioAsString()
	 * @model unsettable="true"
	 * @generated
	 */
	String getPoissonRatioAsString();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getPoissonRatioAsString <em>Poisson Ratio As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Poisson Ratio As String</em>' attribute.
	 * @see #isSetPoissonRatioAsString()
	 * @see #unsetPoissonRatioAsString()
	 * @see #getPoissonRatioAsString()
	 * @generated
	 */
	void setPoissonRatioAsString(String value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getPoissonRatioAsString <em>Poisson Ratio As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetPoissonRatioAsString()
	 * @see #getPoissonRatioAsString()
	 * @see #setPoissonRatioAsString(String)
	 * @generated
	 */
	void unsetPoissonRatioAsString();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getPoissonRatioAsString <em>Poisson Ratio As String</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Poisson Ratio As String</em>' attribute is set.
	 * @see #unsetPoissonRatioAsString()
	 * @see #getPoissonRatioAsString()
	 * @see #setPoissonRatioAsString(String)
	 * @generated
	 */
	boolean isSetPoissonRatioAsString();

	/**
	 * Returns the value of the '<em><b>Thermal Expansion Coefficient</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Thermal Expansion Coefficient</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Thermal Expansion Coefficient</em>' attribute.
	 * @see #isSetThermalExpansionCoefficient()
	 * @see #unsetThermalExpansionCoefficient()
	 * @see #setThermalExpansionCoefficient(double)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcMechanicalMaterialProperties_ThermalExpansionCoefficient()
	 * @model unsettable="true"
	 * @generated
	 */
	double getThermalExpansionCoefficient();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getThermalExpansionCoefficient <em>Thermal Expansion Coefficient</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Thermal Expansion Coefficient</em>' attribute.
	 * @see #isSetThermalExpansionCoefficient()
	 * @see #unsetThermalExpansionCoefficient()
	 * @see #getThermalExpansionCoefficient()
	 * @generated
	 */
	void setThermalExpansionCoefficient(double value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getThermalExpansionCoefficient <em>Thermal Expansion Coefficient</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetThermalExpansionCoefficient()
	 * @see #getThermalExpansionCoefficient()
	 * @see #setThermalExpansionCoefficient(double)
	 * @generated
	 */
	void unsetThermalExpansionCoefficient();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getThermalExpansionCoefficient <em>Thermal Expansion Coefficient</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Thermal Expansion Coefficient</em>' attribute is set.
	 * @see #unsetThermalExpansionCoefficient()
	 * @see #getThermalExpansionCoefficient()
	 * @see #setThermalExpansionCoefficient(double)
	 * @generated
	 */
	boolean isSetThermalExpansionCoefficient();

	/**
	 * Returns the value of the '<em><b>Thermal Expansion Coefficient As String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Thermal Expansion Coefficient As String</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Thermal Expansion Coefficient As String</em>' attribute.
	 * @see #isSetThermalExpansionCoefficientAsString()
	 * @see #unsetThermalExpansionCoefficientAsString()
	 * @see #setThermalExpansionCoefficientAsString(String)
	 * @see org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcMechanicalMaterialProperties_ThermalExpansionCoefficientAsString()
	 * @model unsettable="true"
	 * @generated
	 */
	String getThermalExpansionCoefficientAsString();

	/**
	 * Sets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getThermalExpansionCoefficientAsString <em>Thermal Expansion Coefficient As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Thermal Expansion Coefficient As String</em>' attribute.
	 * @see #isSetThermalExpansionCoefficientAsString()
	 * @see #unsetThermalExpansionCoefficientAsString()
	 * @see #getThermalExpansionCoefficientAsString()
	 * @generated
	 */
	void setThermalExpansionCoefficientAsString(String value);

	/**
	 * Unsets the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getThermalExpansionCoefficientAsString <em>Thermal Expansion Coefficient As String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetThermalExpansionCoefficientAsString()
	 * @see #getThermalExpansionCoefficientAsString()
	 * @see #setThermalExpansionCoefficientAsString(String)
	 * @generated
	 */
	void unsetThermalExpansionCoefficientAsString();

	/**
	 * Returns whether the value of the '{@link org.bimserver.models.ifc2x3tc1.IfcMechanicalMaterialProperties#getThermalExpansionCoefficientAsString <em>Thermal Expansion Coefficient As String</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Thermal Expansion Coefficient As String</em>' attribute is set.
	 * @see #unsetThermalExpansionCoefficientAsString()
	 * @see #getThermalExpansionCoefficientAsString()
	 * @see #setThermalExpansionCoefficientAsString(String)
	 * @generated
	 */
	boolean isSetThermalExpansionCoefficientAsString();

} // IfcMechanicalMaterialProperties
>>>>>>> 76aa07461566a5976980e6696204781271955163

