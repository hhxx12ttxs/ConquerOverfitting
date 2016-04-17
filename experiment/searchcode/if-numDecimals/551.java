package es.caib.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ValidacionesUtil
{
	private static String RE_CUENTA = "^[0-9]{20}$";
	private static final String  DEFAULT_FORMAT = "dd/MM/yyyy";
	
	private static String SEPARADOR_PAR = ";@;";
	private static String SEPARADOR_CAMPOS = "#@#";

	
	
    
	  /*************************************************************************************************/
	  /* esNif (String cadena)								           */
	  /*											           */
	  /* Funci�n que verifica que la cadena alfanum�rica suministrada se corresponde con un NIF v�lido.*/
	  /*                                                                                               */								         
	  /* Los PAR�METROS DE ENTRADA que requiere la funci�n son los siguientes:		           */
	  /*   * cadena -----> par�metro del tipo STRING referente al n�mero del NIF a validar.            */
	  /*												   */
	  /* Los PAR�METROS DE SALIDA que proporciona la funci�n son los siguientes:			   */
	  /*   * resultado ----> par�metro del tipo BOOLEAN que verifica la condici�n.			   */
	  /*												   */
	  /* Autor�a:										           */
	  /*   * Guillermo Corull�n Lago. (01/01/2007)						   */
	  /*************************************************************************************************/

	  public static boolean esNif (String cadena)
	  {
	    return NifCif.esNIF (cadena);
	  }

	  /*************************************************************************************************/
	  /* esCif (String cadena)								           */
	  /*											           */
	  /* Funci�n que verifica que la cadena alfanum�rica suministrada se corresponde con un CIF v�lido.*/
	  /*                                                                                               */								         
	  /* Los PAR�METROS DE ENTRADA que requiere la funci�n son los siguientes:		           */
	  /*   * cadena -----> par�metro del tipo STRING referente al n�mero del CIF a validar.            */
	  /*												   */
	  /* Los PAR�METROS DE SALIDA que proporciona la funci�n son los siguientes:			   */
	  /*   * resultado ----> par�metro del tipo BOOLEAN que verifica la condici�n.		           */
	  /*												   */
	  /* Autor�a:										           */
	  /*   * Guillermo Corull�n Lago. (01/01/2007)						   */
	  /*************************************************************************************************/

	  public static boolean esCif (String cadena)
	  {
	    return NifCif.esCIF (cadena);
	  }

	  /*************************************************************************************************/
	  /* esNie (String cadena)								           */
	  /*											           */
	  /* Funci�n que verifica que la cadena alfanum�rica suministrada se corresponde con un NIE v�lido.*/
	  /*                                                                                               */								         
	  /* Los PAR�METROS DE ENTRADA que requiere la funci�n son los siguientes:		           */
	  /*   * cadena -----> par�metro del tipo STRING referente al n�mero del NIE a validar.            */
	  /*												   */
	  /* Los PAR�METROS DE SALIDA que proporciona la funci�n son los siguientes:			   */
	  /*   * resultado ----> par�metro del tipo BOOLEAN que verifica la condici�n.			   */
	  /*												   */
	  /* Autor�a:										           */
	  /*   * Guillermo Corull�n Lago. (01/01/2007)						   */
	  /*************************************************************************************************/

	  public static boolean esNie (String cadena)
	  {
	    return NifCif.esNIE (cadena);
	  }

	  /*************************************************************************************************/
	  /* esNumeroSeguridadSocial (String cadena)						           */
	  /*											           */
	  /* Funci�n que verifica que la cadena alfanum�rica suministrada se corresponde con un N�mero de  */
	  /* la Seguridad Social v�lido.								   */
	  /*                                                                                               */								         
	  /* Los PAR�METROS DE ENTRADA que requiere la funci�n son los siguientes:		           */
	  /*   * cadena -----> par�metro del tipo STRING referente al n�mero de la Seguridad Social a      */
	  /*                   validar.									   */
	  /*												   */
	  /* Los PAR�METROS DE SALIDA que proporciona la funci�n son los siguientes:			   */
	  /*   * resultado ----> par�metro del tipo BOOLEAN que verifica la condici�n.			   */
	  /*												   */
	  /* Autor�a:										           */
	  /*   * Guillermo Corull�n Lago. (01/01/2007)					           */
	  /*************************************************************************************************/

	  public static boolean esNumeroSeguridadSocial (String cadena) 
	  {
	    return NifCif.esNumeroSeguridadSocial (cadena); 
	  }
	
	  
	 
	  /**
	   * Valida numero IBAN
	   * @param code codigo IBAN (no debe empezar por IBAN). Admite espacios y guiones.
	   * @return true/false
	   */
	public static boolean esNumeroIBANValido(String code) {
		
		if (code == null) {
			return false;
		}
		
		try {
			code = StringUtil.replace(code, " ", "");
			code = StringUtil.replace(code, "-", "");
		}catch (Exception ex) {
			return false;
		}
		
		long MODULUS = 97;
		long MAX = 999999999;

		if (code.length() < 5) {
			return false;
		}

		String reformattedCode = code.substring(4) + code.substring(0, 4);
		long total = 0;
		for (int i = 0; i < reformattedCode.length(); i++) {
			int charValue = Character
					.getNumericValue(reformattedCode.charAt(i));
			if (charValue < 0 || charValue > 35) {
				return false;
			}
			total = (charValue > 9 ? total * 100 : total * 10) + charValue;
			if (total > MAX) {
				total = (total % MODULUS);
			}
		}

		boolean ok = ((total % MODULUS) == 1);
		
		// Para cuentas de Espa�a seguimos validando numero de cuenta
		if (ok && code.startsWith("ES")) {
			ok = esNumeroCuentaValido(code.substring(4));
		}
		
		return ok;
	}

	  
	/**
	 * Valida un n�mero de cuenta que viene todo junto en formato [entidad][sucursal][dc][cuenta]
	 * No admite ning�n tipo de separador, tan s�lo las cifras del n�mero de cuenta
	 * @param numeroCuenta
	 * @return
	 */
	public static boolean esNumeroCuentaValido( String numeroCuenta )
	{
		//String numeroCuentaNormalizado = NifCif.normalizarDocumento( numeroCuenta );
			
		
		if ( !Pattern.matches(RE_CUENTA, numeroCuenta)) return false;
		
		String entidad = numeroCuenta.substring( 0,4 );
		String sucursal = numeroCuenta.substring( 4,8 );
		String dc = numeroCuenta.substring( 8, 10 );
		String cuenta = numeroCuenta.substring( 10 );
		
		return esNumeroCuentaValido( entidad, sucursal, dc, cuenta );
	}
	
	/**
	 * Valida un n�mero de cuenta a partir de la entidad, sucursal, dc y cuenta
	 * @param entidad
	 * @param sucursal
	 * @param dc
	 * @param cuenta
	 * @return
	 */
	public static boolean esNumeroCuentaValido( String entidad, String sucursal, String dc, String cuenta )
	{
		if ( Integer.parseInt( entidad, 10 ) < 1 )
			return false;
		
		
        int calculo0 = 11 - 
        		( 
        				4 * Integer.parseInt( entidad.substring( 0, 1 ), 10 ) + 8 * Integer.parseInt( entidad.substring( 1, 2 ), 10 ) + 5*Integer.parseInt( entidad.substring( 2, 3 ), 10 ) + 10*Integer.parseInt( entidad.substring( 3, 4 ), 10 )
        				+ 9 * Integer.parseInt( sucursal.substring( 0, 1 ), 10 ) + 7*Integer.parseInt( sucursal.substring( 1, 2 ), 10 ) + 3*Integer.parseInt( sucursal.substring( 2, 3 ), 10 ) + 6*Integer.parseInt( sucursal.substring( 3, 4 ), 10 )
                ) % 11;
        
        int calculo1 = 11 - (1*Integer.parseInt( cuenta.substring( 0, 1 ), 10 ) + 2*Integer.parseInt( cuenta.substring( 1, 2 ), 10 ) + 4*Integer.parseInt( cuenta.substring( 2, 3 ), 10 )
                + 8*Integer.parseInt( cuenta.substring( 3, 4 ), 10 ) + 5*Integer.parseInt( cuenta.substring( 4, 5 ), 10 ) + 10*Integer.parseInt( cuenta.substring( 5, 6 ), 10 )
                + 9*Integer.parseInt( cuenta.substring( 6, 7 ), 10 ) + 7*Integer.parseInt( cuenta.substring( 7, 8 ), 10 ) + 3*Integer.parseInt( cuenta.substring( 8, 9 ), 10 )
                + 6*Integer.parseInt( cuenta.substring( 9, 10 ), 10 )) % 11;
        
        if(calculo0>=10) calculo0 = 11 - calculo0;
        if(calculo1>=10) calculo1 = 11 - calculo1;
              
        
        if(calculo0 != Integer.parseInt( dc.substring( 0,1 ), 10 ) || calculo1 != Integer.parseInt( dc.substring( 1,2 ), 10 ) )
        {
            return false;
        }
		
		return true;
	}
	
	  public static boolean hayAniosDistancia (String fechaIni, String fechaFin, int aniosDist, String formatoFecha)
	  {
	    // Declaraci�n de variables.
	       boolean  resultado;

	       Date     iniDate;
	       Date     finDate;

	       Calendar iniCalendar;
	       Calendar finCalendar;
	  
	    // Inicializaci�n de variables.
	       resultado = false;
	     
	       iniDate = DataUtil.data (fechaIni, formatoFecha);
	       finDate = DataUtil.data (fechaFin, formatoFecha);
	     
	       iniCalendar = Calendar.getInstance ();
	       if (iniDate == null) 		// No se proporciona una fechaIni v�lida.
	       {
	       	 return resultado; 		// Salida de la funci�n.
	       }
	       else		  		// S� se proporciona una fechaIni v�lida.
	       {
	       	 iniCalendar.setTime (iniDate); // Asignaci�n de la fechaIni.
	       }
	     
	       finCalendar = Calendar.getInstance ();
	       if (finDate == null) 		// No se proporciona una fechaFin v�lida.
	       {
	         return resultado; 		// Salida de la funci�n.
	       }
	       else		  		// S� se proporciona una fechaFin v�lida.
	       {
	       	 finCalendar.setTime (finDate); // Asignaci�n de la fechaFin.
	       }
	       
	       if (aniosDist < 0)		// No se proporciona una distancia en a�os v�lida.
	       {
	       	 return resultado; 		// Salida de la funci�n.
	       }
	       
	       if (finCalendar.before (iniCalendar)) // La fecha menor de la comparaci�n no es v�lida.
	       {
	       	 return resultado;		     // Salida de la funci�n.
	       }

	    // Evaluaci�n de la condici�n.
	       iniCalendar.add (Calendar.YEAR, aniosDist);   // Actualizaci�n de la fecha de comparaci�n.
	       resultado = finCalendar.before (iniCalendar); // Comparaci�n de fechas.
	       return !resultado;			     // Salida de la funci�n.
	     						     //  TRUE --> S� se cumple la separaci�n de a�os entre las dos fechas.
	     						     //  FALSE -> NO se cumple la separaci�n de a�os entre las dos fechas.
	  }

	  /*************************************************************************************************/
	  /* hayAniosDistancia (String fechaIni, String fechaFin, int aniosDist)     			   */
	  /*											           */
	  /* Funci�n que verifica que la distancia en a�os entre dos fechas dadas es MAYOR O IGUAL         */
	  /* que un valor concreto. 									   */
	  /* El formato asociado a las fechas que se comparan ser� el definido por defecto dd/mm/aaaa.     */
	  /*                                                                                               */								         
	  /* Los PAR�METROS DE ENTRADA que requiere la funci�n son los siguientes:		           */
	  /*   * fechaIni ------> par�metro del tipo STRING referente a la fecha MENOR de la comparaci�n.  */
	  /*   * fechaFin ------> par�metro del tipo STRING referente a la fecha MAYOR de la comparaci�n.  */
	  /*   * aniosDist -----> par�metro del tipo INT referente a la distancia en a�os entre las fechas.*/
	  /*												   */
	  /* Los PAR�METROS DE SALIDA que proporciona la funci�n son los siguientes:			   */
	  /*   * resultado -----> par�metro del tipo BOOLEAN que verifica la condici�n.			   */
	  /*												   */
	  /* Autor�a:										           */
	  /*   * Guillermo Corull�n Lago. (01/01/2007)						   */
	  /*************************************************************************************************/

	  public static boolean hayAniosDistancia (String fechaIni, String fechaFin, int aniosDist) 
	  {
	    return hayAniosDistancia (fechaIni, fechaFin, aniosDist, DEFAULT_FORMAT);
	  }

	  /*************************************************************************************************/
	  /* hayAniosDistancia (String fechaEvl, int aniosDist, String formatoFecha)      		   */
	  /*											           */
	  /* Funci�n que verifica que la distancia en a�os entre una fecha dada y la fecha actual del 	   */
	  /* sistema es MAYOR O IGUAL que un valor concreto. 						   */
	  /*                                                                                               */								         
	  /* Los PAR�METROS DE ENTRADA que requiere la funci�n son los siguientes:		           */
	  /*   * fechaEvl ------> par�metro del tipo STRING referente a la fecha objeto de la comparaci�n. */
	  /*   * aniosDist -----> par�metro del tipo INT referente a la distancia en a�os entre las fechas.*/
	  /*   * formatoFecha --> par�metro del tipo STRING referente al formato utilizado en las fechas.  */
	  /*												   */
	  /* Los PAR�METROS DE SALIDA que proporciona la funci�n son los siguientes:			   */
	  /*   * resultado -----> par�metro del tipo BOOLEAN que verifica la condici�n.			   */
	  /*												   */
	  /* Autor�a:										           */
	  /*   * Guillermo Corull�n Lago. (01/01/2007)						   */
	  /*************************************************************************************************/

	  public static boolean hayAniosDistancia (String fechaEvl, int aniosDist, String formatoFecha) 
	  {
	    // Declaraci�n de variables.
	       boolean  resultado;

	       Date     evlDate;
	       Date     sisDate;

	       Calendar evlCalendar;
	       Calendar sisCalendar;
	  
	    // Inicializaci�n de variables.
	       resultado = false;
	     
	       evlDate = DataUtil.data (fechaEvl, formatoFecha);
	       sisDate = new Date ();
	     
	       evlCalendar = Calendar.getInstance ();
	       if (evlDate == null) 		// No se proporciona una fechaEvl v�lida.
	       {
	         return resultado; 		// Salida de la funci�n.
	       }
	       else		  		// S� se proporciona una fechaEvl v�lida.
	       {
	     	 evlCalendar.setTime (evlDate); // Asignaci�n de la fechaEvl.
	       }
	     
	       sisCalendar = Calendar.getInstance ();
	       sisCalendar.setTime (sisDate);
	       
	       if (aniosDist < 0)		// No se proporciona una distancia en a�os v�lida.
	       {
	       	 return resultado; 		// Salida de la funci�n.
	       }
	       
	       if (sisCalendar.before (evlCalendar)) // La fecha menor de la comparaci�n no es v�lida.
	       {
	       	 return resultado;		     // Salida de la funci�n.
	       }

	    // Evaluaci�n de la condici�n.
	       evlCalendar.add (Calendar.YEAR, aniosDist);   // Actualizaci�n de la fecha de comparaci�n.
	       resultado = sisCalendar.before (evlCalendar); // Comparaci�n de fechas.
	       return !resultado;			     // Salida de la funci�n.
	     						     //  TRUE --> S� se cumple la separaci�n de a�os entre las dos fechas.
	     						     //  FALSE -> NO se cumple la separaci�n de a�os entre las dos fechas.
	  }

	  /*************************************************************************************************/
	  /* hayDiasDistancia (String fechaEvl, int aniosDist)			        		   */
	  /*											           */
	  /* Funci�n que verifica que la distancia en a�os entre una fecha dada y la fecha actual del 	   */
	  /* sistema es MAYOR O IGUAL que un valor concreto. 						   */
	  /* El formato asociado a las fechas que se comparan ser� el definido por defecto dd/mm/aaaa.     */
	  /*                                                                                               */								         
	  /* Los PAR�METROS DE ENTRADA que requiere la funci�n son los siguientes:		           */
	  /*   * fechaEvl ------> par�metro del tipo STRING referente a la fecha objeto de la comparaci�n. */
	  /*   * aniosDist -----> par�metro del tipo INT referente a la distancia en a�os entre las fechas.*/
	  /*												   */
	  /* Los PAR�METROS DE SALIDA que proporciona la funci�n son los siguientes:			   */
	  /*   * resultado -----> par�metro del tipo BOOLEAN que verifica la condici�n.			   */
	  /*												   */
	  /* Autor�a:										           */
	  /*   * Guillermo Corull�n Lago. (01/01/2007)						   */
	  /*************************************************************************************************/

	  public static boolean hayAniosDistancia (String fechaEvl, int aniosDist)
	  {
	    return hayAniosDistancia (fechaEvl, aniosDist, DEFAULT_FORMAT);
	  }

	  /*************************************************************************************************/
	  /* hayDiasDistancia (String fechaIni, String fechaFin, int diasDist, String formatoFecha)        */
	  /*											           */
	  /* Funci�n que verifica que la distancia en d�as entre dos fechas dadas es MAYOR O IGUAL         */
	  /* que un valor concreto. 									   */
	  /*                                                                                               */								         
	  /* Los PAR�METROS DE ENTRADA que requiere la funci�n son los siguientes:		           */
	  /*   * fechaIni -----> par�metro del tipo STRING referente a la fecha MENOR de la comparaci�n.   */
	  /*   * fechaFin -----> par�metro del tipo STRING referente a la fecha MAYOR de la comparaci�n.   */
	  /*   * diasDist -----> par�metro del tipo INT referente a la distancia en d�as entre las fechas. */
	  /*   * formatoFecha -> par�metro del tipo STRING referente al formato utilizado en las fechas.   */
	  /*												   */
	  /* Los PAR�METROS DE SALIDA que proporciona la funci�n son los siguientes:			   */
	  /*   * resultado ----> par�metro del tipo BOOLEAN que verifica la condici�n.			   */
	  /*												   */
	  /* Autor�a:										           */
	  /*   * Guillermo Corull�n Lago. (01/01/2007)						   */
	  /*   * Vicent Ferrer i P�rez.   (24/01/2007)							   */
	  /*************************************************************************************************/

	  public static boolean hayDiasDistancia (String fechaIni, String fechaFin, int diasDist, String formatoFecha)
	  {
	    // Declaraci�n de variables.
	       boolean  resultado;

	       Date     iniDate;
	       Date     finDate;

	       Calendar iniCalendar;
	       Calendar finCalendar;
	  
	    // Inicializaci�n de variables.
	       resultado = false;
	     
	       iniDate = DataUtil.data (fechaIni, formatoFecha);
	       finDate = DataUtil.data (fechaFin, formatoFecha);
	       
	       iniCalendar = Calendar.getInstance ();
	       if (iniDate == null) 		// No se proporciona una fechaIni v�lida.
	       {
	         return resultado; 		// Salida de la funci�n.
	       }
	       else		  		// S� se proporciona una fechaIni v�lida.
	       {
	         iniCalendar.setTime (iniDate); // Asignaci�n de la fechaIni.
	       }
	     
	       finCalendar = Calendar.getInstance ();
	       if (finDate == null) 		// No se proporciona una fechaFin v�lida.
	       {
	         return resultado; 		// Salida de la funci�n.
	       }
	       else		  		// S� se proporciona una fechaFin v�lida.
	       {
	         finCalendar.setTime (finDate); // Asignaci�n de la fechaFin.
	       }
	       
	       if (diasDist < 0)		// No se proporciona una distancia en d�as v�lida.
	       {
	       	 return resultado; 		// Salida de la funci�n.
	       }
	       
	       if (finCalendar.before (iniCalendar)) // La fecha menor de la comparaci�n no es v�lida.
	       {
	       	 return resultado;		     // Salida de la funci�n.
	       }

	    // Evaluaci�n de la condici�n.
	       iniCalendar.add (Calendar.DATE, diasDist);    // Actualizaci�n de la fecha de comparaci�n.
	       resultado = finCalendar.before (iniCalendar); // Comparaci�n de fechas.
	       return !resultado;			     // Salida de la funci�n.
	     						     //  TRUE --> S� se cumple la separaci�n de d�as entre las dos fechas.
	     						     //  FALSE -> NO se cumple la separaci�n de d�as entre las dos fechas.
	  }

	  /*************************************************************************************************/
	  /* hayDiasDistancia (String fechaIni, String fechaFin, int diasDist)				   */
	  /*											           */
	  /* Funci�n que verifica que la distancia en d�as entre dos fechas dadas es MAYOR O IGUAL         */
	  /* que un valor concreto. 									   */
	  /* El formato asociado a las fechas que se comparan ser� el definido por defecto dd/mm/aaaa.     */
	  /*                                                                                               */								         
	  /* Los PAR�METROS DE ENTRADA que requiere la funci�n son los siguientes:		           */
	  /*   * fechaIni -----> par�metro del tipo STRING referente a la fecha MENOR de la comparaci�n.   */
	  /*   * fechaFin -----> par�metro del tipo STRING referente a la fecha MAYOR de la comparaci�n.   */
	  /*   * diasDist -----> par�metro del tipo INT referente a la distancia en d�as entre las fechas. */
	  /*												   */
	  /* Los PAR�METROS DE SALIDA que proporciona la funci�n son los siguientes:			   */
	  /*   * resultado ----> par�metro del tipo BOOLEAN que verifica la condici�n.			   */
	  /*												   */
	  /* Autor�a:										           */
	  /*   * Guillermo Corull�n Lago. (01/01/2007)					           */
	  /*   * Vicent Ferrer i P�rez.   (24/01/2007)							   */
	  /*************************************************************************************************/

	  public static boolean hayDiasDistancia (String fechaIni, String fechaFin, int diasDist) 
	  {
	    return hayDiasDistancia (fechaIni, fechaFin, diasDist, DEFAULT_FORMAT);
	  }

	  /*************************************************************************************************/
	  /* hayDiasDistancia (String fechaEvl, int diasDist, String formatoFecha)        		   */
	  /*											           */
	  /* Funci�n que verifica que la distancia en d�as entre una fecha dada y la fecha actual del 	   */
	  /* sistema es MAYOR O IGUAL que un valor concreto. 						   */
	  /*                                                                                               */								         
	  /* Los PAR�METROS DE ENTRADA que requiere la funci�n son los siguientes:		           */
	  /*   * fechaEvl -----> par�metro del tipo STRING referente a la fecha objeto de la comparaci�n.  */
	  /*   * diasDist -----> par�metro del tipo INT referente a la distancia en d�as entre las fechas. */
	  /*   * formatoFecha -> par�metro del tipo STRING referente al formato utilizado en las fechas.   */
	  /*												   */
	  /* Los PAR�METROS DE SALIDA que proporciona la funci�n son los siguientes:			   */
	  /*   * resultado ----> par�metro del tipo BOOLEAN que verifica la condici�n.			   */
	  /*												   */
	  /* Autor�a:										           */
	  /*   * Guillermo Corull�n Lago. (01/01/2007)						   */
	  /*   * Vicent Ferrer i P�rez.   (24/01/2007)							   */
	  /*************************************************************************************************/

	  public static boolean hayDiasDistancia (String fechaEvl, int diasDist, String formatoFecha) 
	  {
	    // Declaraci�n de variables.
	       boolean  resultado;

	       Date     evlDate;
	       Date     sisDate;

	       Calendar evlCalendar;
	       Calendar sisCalendar;
	  
	    // Inicializaci�n de variables.
	       resultado = false;
	     
	       evlDate = DataUtil.data (fechaEvl, formatoFecha);
	       sisDate = new Date ();
	     
	       evlCalendar = Calendar.getInstance ();
	       if (evlDate == null) 		// No se proporciona una fechaEvl v�lida.
	       {
	         return resultado; 		// Salida de la funci�n.
	       }
	       else		  		// S� se proporciona una fechaEvl v�lida.
	       {
	         evlCalendar.setTime (evlDate); // Asignaci�n de la fechaEvl.
	       }
	     
	       sisCalendar = Calendar.getInstance ();
	       sisCalendar.setTime (sisDate);

	       if (diasDist < 0)		// No se proporciona una distancia en d�as v�lida.
	       {
	       	 return resultado; 		// Salida de la funci�n.
	       }
	       
	       if (sisCalendar.before (evlCalendar)) // La fecha menor de la comparaci�n no es v�lida.
	       {
	       	 return resultado;		     // Salida de la funci�n.
	       }

	    // Evaluaci�n de la condici�n.
	       evlCalendar.add (Calendar.DATE, diasDist);    // Actualizaci�n de la fecha de comparaci�n.
	       resultado = sisCalendar.before (evlCalendar); // Comparaci�n de fechas.
	       return !resultado;			     // Salida de la funci�n.
	     						     //  TRUE --> S� se cumple la separaci�n de d�as entre las dos fechas.
	     						     //  FALSE -> NO se cumple la separaci�n de d�as entre las dos fechas.
	  }

	  /*************************************************************************************************/
	  /* hayDiasDistancia (String fechaEvl, int diasDist)			        		   */
	  /*											           */
	  /* Funci�n que verifica que la distancia en d�as entre una fecha dada y la fecha actual del 	   */
	  /* sistema es MAYOR O IGUAL que un valor concreto. 						   */
	  /* El formato asociado a las fechas que se comparan ser� el definido por defecto dd/mm/aaaa.     */
	  /*                                                                                               */								         
	  /* Los PAR�METROS DE ENTRADA que requiere la funci�n son los siguientes:		           */
	  /*   * fechaEvl -----> par�metro del tipo STRING referente a la fecha objeto de la comparaci�n.  */
	  /*   * diasDist -----> par�metro del tipo INT referente a la distancia en d�as entre las fechas. */
	  /*												   */
	  /* Los PAR�METROS DE SALIDA que proporciona la funci�n son los siguientes:			   */
	  /*   * resultado ----> par�metro del tipo BOOLEAN que verifica la condici�n.			   */
	  /*												   */
	  /* Autor�a:										           */
	  /*   * Guillermo Corull�n Lago. (01/01/2007)					           */
	  /*   * Vicent Ferrer i P�rez.   (24/01/2007)							   */
	  /*************************************************************************************************/

	  public static boolean hayDiasDistancia (String fechaEvl, int diasDist)
	  {
	    return hayDiasDistancia (fechaEvl, diasDist, DEFAULT_FORMAT);
	  }
	  
	  /**
	   * Esta funcion devuelve el valor del campo pasado como parametro buscandolo en
	   * la cadena de entrada. El formato de la cadena de entrada es el siguiente:
	   * 		campo1;@;valor1#@#campo2;@;valor2#@#campo3.......
	   * En el caso de no encontrar el campo devuelve cadena vacia.
	 * @param cadena
	 * @param campo
	 * @return un string que representa el valor del campo pasado como parametro buscandolo en la cadena 
	 *         como parametro
	 */
	public static String getCampo(String cadena, String campo)
	  {
			String[] pares = cadena.split(ValidacionesUtil.SEPARADOR_CAMPOS);
			for(int i=0; i<pares.length; i++)
			{
				String[] campoValor = pares[i].split(ValidacionesUtil.SEPARADOR_PAR);
				if((campoValor.length == 2) && (campoValor[0].equals(campo))) 
				{
					return campoValor[1];
				}

			}
			return "";
	  }

	  /**
	   * Esta funcion comprueba que se recibe un String con todos los codigos rellenos. 
	   * Es decir, se espera un string con el siguiente formato:
	   * 	valor1separadorvalor2separador......
	   * En el caso de que llegue un string en el que falte cualquier valor devuelve false.
	 * @param datos
	 * @return true o false segun detecta que contiene o no todos los valores esperados. 
	 */
	public static boolean validaLleno(String extension, String separador)
	{
		int idx = extension.indexOf(separador+separador);
		if(idx != -1)
		{
			return false;
		}
		else
		{
			idx = extension.indexOf(separador);
			if(idx == 0) return false;
			idx = extension.lastIndexOf(separador);
			if(idx == (extension.length() - separador.length())) return false;
			return true;
		}
	}
	
	/**
	 * Devuelve true si encuentra que hay huecos entre los codigos rellenos
	 * @param extension
	 * @param separador
	 * @return
	 */
	public static boolean validaHuecos(String extension, String separador)
	{
		String[] campos = extension.split(separador);
		boolean vacio = false;
		for(int i=0; i<campos.length;i++)
		{
			if((vacio == false) && campos[i].equals("")) vacio = true;
			else
			{
				if(vacio && !campos[i].equals("")) return true;
			}
		}
		return false;
	}
	
	/**
	 * Comprueba si el campo pasado como par�metro est� en la lista o no.
	 * @param lista
	 * @param campo
	 * @return true o false indicando si esta o no en la lista.
	 */
	private static boolean exists(List lista, String campo)
	{
		for(Iterator it = lista.iterator(); it.hasNext();)
		{
			String cp = (String) it.next();
			if(cp.equals(campo)) return true;
		}
		return false;
	}

	/**
	 * Comprueba si el campo pasado como par�metro est� en la lista o no.
	 * @param lista
	 * @param campo
	 * @param separador
	 * @return Devuelve una lista con las posiciones de los repetidos. Si no esta repetido devuelve un 0.
	 */
	public static List validaRepetido(String cadenaLista, String campo, String separador)
	{
		ArrayList lista = new ArrayList();
		String[] campos = cadenaLista.split(separador);
		ArrayList repetidos = new ArrayList();
		for(int i=0; i<campos.length;i++)
		{
			lista.add(campos[i]);
		}
		
		int i = 1;
		for(Iterator it = lista.iterator(); it.hasNext();i++)
		{
			String cp = (String) it.next();
			if(cp.equals(campo)) repetidos.add(String.valueOf(i));
		}
		// Si no esta repetido, o si solo aparece un elemento es porque es �l mismo, devuelve un 0
		if(repetidos.size() == 1)
		{
			repetidos = new ArrayList();
			repetidos.add("0");
		}
		if(repetidos.size() == 0) repetidos.add("0");
		return repetidos;
	}
	
	/**
	 * Comprueba si hay repetidos en los valores de la cadena. La cadena que se espera tiene
	 * el siguiente formato:
	 * 		valor1separadorvalor2separadorvalor3,.....
	 * 
	 * @param extension
	 * @return true o false segun haya detectado si hay valores repetidos o no.
	 */
	public static boolean validaRepetidos(String cadena, String separador)
	{
		String[] campos = cadena.split(separador);
		ArrayList lista = new ArrayList();
		for(int i=0; i<campos.length; i++)
		{
			if(!campos[i].equals(""))
			{
				if(exists(lista,campos[i])) return true;
				else lista.add(campos[i]);
			}
		}
		return false;
	}
	
	  /*************************************************************************************************/
	  /* esCIAS (String cadena)								           */
	  /*											           */
	  /* Funci�n que verifica que la cadena alfanum�rica suministrada se corresponde con un CIAS v�lido.*/
	  /*                                                                                               */								         
	  /* Los PAR�METROS DE ENTRADA que requiere la funci�n son los siguientes:		           */
	  /*   * cadena -----> par�metro del tipo STRING referente al n�mero del CIAS a validar.            */
	  /*												   */
	  /* Los PAR�METROS DE SALIDA que proporciona la funci�n son los siguientes:			   */
	  /*   * resultado ----> par�metro del tipo BOOLEAN que verifica la condici�n.			   */
	  /*												   */
	  /*************************************************************************************************/

	  public static boolean esCIAS (String cadena)
	  {
	    return NifCif.esCIAS(cadena);
	  }

	
	 /**
	 * Devuelve el texto completo reemplazando el separador por un salto de l�nea. Cada item empezar� con '- ' 
	 */
	  public static String getTextoCompleto(String texto,String separador)
	  {
		  String[] cadenas = texto.split(separador);
		  StringBuffer result = new StringBuffer(texto.length() + 100);
		  result.append("");
		  
		  for (int i=0;i<cadenas.length;i++){
			  result.append("-" + cadenas[i] + "\r\n");			  			 
		  }
		  		  
		  return result.toString();
	  }  
	  
		/**
		 * Realiza la validacion del email que se pasa como parametro
		 * @param email
		 * @return
		 */
		public static boolean validarEmail( String email )
		{  
			// validar email 
			Pattern p = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
			Matcher m = p.matcher(email);
			boolean b = m.matches();
			return b;
		}
		
		/**
		 * Realiza la validacion del telefono movil que se pasa como parametro.Solo valida
		 * aquellos telefonos con 9 cifras que empiecen por 6 o 7
		 * @param movil
		 * @return
		 */
		public static boolean validarMovil( String movil)
		{  
			// validar email 
			Pattern p = Pattern.compile("[6-7][0-9]{8}");
			Matcher m = p.matcher(movil);
			boolean b = m.matches();
			return b;
		}

		/**
		 * Valida si un cif donat es d'alguna entitat balear.
		 * @param cif de la entitat
		 * @return boolean si es entitat balear
		 */
		public static boolean esEntidadBalear(String cif){
			
			//CIF del govern
			if ("S0711001H".equalsIgnoreCase(cif)) return true;
			
			return false;
			
		}


		/**
		 * Calcula l'import de l'iva a partir de la base imponible i el valor aplicable d'iva
		 * amb el redondeig de 2 decimals
		 * @param string base imponible
		 * @param string iva imponible
		 * @return string calcul del iva com a percertatge de la base
		 */
		public static String calcularIva(String base, String iva){

			double dIva;
			double dBase;
			try {
				dIva = Double.parseDouble(iva);
				dBase = Double.parseDouble(base);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return null;			
			}
			
			double dImportIva = dIva * dBase / (double)100.0d;

			
			DecimalFormat fmt = new DecimalFormat("0.00");  
			
			return fmt.format(Math.round(dImportIva*100.0d)/100.0d);  
		}
		
		/**
		 * Calcula l'import total com a resulat d'aplicar un iva a una base imponible, amb 
		 * precissi� de 2 decimals.
		 * @param string base imponible
		 * @param string iva imponible
		 * @return string Total calcular a partir de la base + iva
		 */
		public static String calcularBaseConIva(String base, String iva){

			double dIva;
			double dBase;
			try {
				dIva = Double.parseDouble(iva);
				dBase = Double.parseDouble(base);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return null;			
			}
			
			double dImportIva = dBase + dIva * dBase / (double)100.0d;
			
			
			DecimalFormat fmt = new DecimalFormat("0.00");  
			
			return fmt.format(Math.round(dImportIva*100.0d)/100.0d);  
		}
		
		/**
	 * Formatea un numero.
	 * 
	 * @param number Numero a formatear.
	 * @param numDecimals Numero de decimales con los que quedara el numero.
	 * @param lang Idioma que determinar� el formato (puntos, comas para decimales o miles)
	 * @param signe Si coge el valor S se a�ade el simbolo + a los numeros positivos
	 * @param separadorDecimals Separador de decimales
	 * @param separdorMilers Separador de miles (opcional)
	 * @return Numero formateado segun las especificaciones o NaN en caso de error.
	 */

	public static String formatNumber(String number, String numDecimals,
			String lang, String signe,
			String separadorDecimals, String separdorMilers) {

		if (separadorDecimals == null) {

			return "NaN";

		}

		if (separadorDecimals.equals(separdorMilers)) {

			return "NaN";

		}

		Locale locale = null;

		if (lang == null) {

			locale = new Locale("es");

		} else {

			locale = new Locale(lang);

		}

		try {

			String numberNormalitzat = new String(number);

			/*Eliminar separador de milers*/

			if (separdorMilers != null) {

				numberNormalitzat = numberNormalitzat.replace(separdorMilers
						.charAt(0), ' ');

				numberNormalitzat = numberNormalitzat.replaceAll(" ", "");

			}

			/*Assignar el punt com a separadors de decimals*/

			numberNormalitzat = numberNormalitzat.replace(separadorDecimals
					.charAt(0), '.');

			double doubleValue = Double.parseDouble(numberNormalitzat);

			int doubleNumDecimals;

			if (numDecimals != null)

				doubleNumDecimals = Integer.parseInt(numDecimals);

			else {

				doubleNumDecimals = numberNormalitzat.length()
						- (numberNormalitzat.indexOf(".") != -1 ? numberNormalitzat
								.indexOf(".")
								: 0) - 1;

			}

			doubleValue = Math.round(doubleValue
					* Math.pow(10, doubleNumDecimals))
					/ Math.pow(10, doubleNumDecimals);

			NumberFormat nf = NumberFormat.getNumberInstance(locale);

			nf.setMinimumFractionDigits(doubleNumDecimals);

			nf.setMaximumFractionDigits(doubleNumDecimals);

			String formatedNumber = String.valueOf(nf.format(doubleValue));

			if ("S".equals(signe) && doubleValue > 0.0d) {

				return "+" + formatedNumber;

			} else {

				return formatedNumber;

			}

		} catch (NumberFormatException e) {

			e.printStackTrace();

			return "NaN";

		}

	}

	/**
	 * Valida si la cadena es una URL.
	 * @param pUrl URL
	 * @return true si es una URL
	 */
	public static boolean esURL(final String pUrl) {
		URL u = null;
        try {
            u = new URL(pUrl);
        } catch (MalformedURLException e) {
            return false;
        }
        try {
            u.toURI();
        } catch (URISyntaxException e) {
            return false;
        }
        return true;
    }


}
