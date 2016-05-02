package com.checaro.pharmacy.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.checaro.pharmacy.model.ComparisonError;
import com.checaro.pharmacy.model.DeliveryNote;
import com.checaro.pharmacy.model.DeliveryNoteLine;
import com.checaro.pharmacy.service.interfaces.DeliveryNoteService;
import com.checaro.pharmacy.utils.GenericSorter;
import com.checaro.pharmacy.utils.Utils;

@Service("deliveryNoteService")
public class DeliveryNoteServiceImpl implements DeliveryNoteService, Serializable {
	private static final long serialVersionUID = 1L;
	private static final int BUFFER_SIZE = 2048;
	//--                   650426 ACETILCISTEINA CINF 600M 20S I                0     0     0     3,12 CINFA SOCIEDAD      10        31,20
	private static final String REG_EXP = "^(.+)\\s{1,}(\\d{6})\\s(.+)\\s{2,}(-?\\d+)\\s{1,}(\\d+)\\s{1,}(\\d+)\\s{1,}(\\d+,\\d\\d)\\s(.+)\\s{1,}(\\d+)\\s{1,}(\\d+,\\d\\d)";
	
	@Override
	public DeliveryNote getDeliveryNoteFromFile(InputStream input) {
		String fileContent = inputStreamToString(input);
		fileContent = fileContent.replaceAll("&nbsp;", " ").
								  replaceAll("<.+?>", "").
								  replaceAll("\\.","");
		String[] lines = fileContent.split("\\n");
		return new DeliveryNote(deliveryNoteLines(lines));
	}
	
	private List<DeliveryNoteLine> deliveryNoteLines(String[] lines) {
		List<DeliveryNoteLine> deliveryNoteLines = new ArrayList<DeliveryNoteLine>();
		Pattern pattern = Pattern.compile(REG_EXP);
		for (String lin : lines) {
			Matcher matcher = pattern.matcher(lin);
			if(matcher.find()){
				//System.out.println(lin);
				DeliveryNoteLine dnl = new DeliveryNoteLine();
				dnl.setFamilia(matcher.group(1).replaceAll("\\s{2,}", ""));
				dnl.setCodigo(matcher.group(2).replaceAll("\\s{2,}", ""));
				dnl.setDenominacion(matcher.group(3).replaceAll("\\s{2,}", ""));
				dnl.setNumExist(Integer.parseInt(matcher.group(4).replaceAll("\\s{2,}", "")));
				dnl.setStMin(Integer.parseInt(matcher.group(5).replaceAll("\\s{2,}", "")));
				dnl.setLote(Integer.parseInt(matcher.group(6).replaceAll("\\s{2,}", "")));
				dnl.setPvp(Double.parseDouble(matcher.group(7).replaceAll("\\s{2,}", "").replace(",", ".")));
				dnl.setLaboratorio(matcher.group(8).replaceAll("\\s{2,}", ""));
				dnl.setCantidad(Integer.parseInt(matcher.group(9).replaceAll("\\s{2,}", "")));
				dnl.setPvpTotal(Double.parseDouble(matcher.group(10).replaceAll("\\s{2,}", "").replace(",", ".")));
				
				deliveryNoteLines.add(dnl);
			}
		}
		return deliveryNoteLines;
	}

	private String inputStreamToString(InputStream input) {
		try{
			Reader reader = new BufferedReader(
						new InputStreamReader(input));
			Writer writer = new StringWriter();
			char[] buffer = new char[BUFFER_SIZE];
			int sizeRead;
			while((sizeRead = reader.read(buffer)) != -1){
				writer.write(buffer, 0, sizeRead);
			}
			return writer.toString();
		}catch(Exception e){
			return "";
		}
	}

	@Override
	public DeliveryNote addDeliveryNotes(DeliveryNote dn1, DeliveryNote dn2) {
		DeliveryNote result = new DeliveryNote();
		
		if(dn1 != null){
			List<DeliveryNoteLine> lines1 = dn1.getLines();
			for (DeliveryNoteLine dnl : lines1) {
				result.addDeliveryNoteLine(new DeliveryNoteLine(dnl));
			}
		}
		
		if(dn2 != null){
			List<DeliveryNoteLine> lines2 = dn2.getLines();
			for (DeliveryNoteLine dnl2 : lines2) {
				result.addDeliveryNoteLine(new DeliveryNoteLine(dnl2));
			}
		}
		return result;
	}

	// COMPROBACIONES:
	//	* Unidades totales de un producto (OK)
	//  * PVP total de un producto (OK)
	//  * PVP unitario de un producto (OK)
	//  * Unidades totales de todos los productos (OK)
	//  * Precio total de todos los productos (OK)
	@Override
	public List<ComparisonError> compareDeliveryNotes(DeliveryNote referencia,
			DeliveryNote generado) {
		List<ComparisonError> errors = new ArrayList<ComparisonError>();
		List<String> codigosDelGenerado = new ArrayList<String>(generado.getCodeMap().keySet());
		List<String> codigosDelReferencia = new ArrayList<String>(referencia.getCodeMap().keySet());
		Collections.sort(codigosDelGenerado);
		Collections.sort(codigosDelReferencia);
		
		List<String> codigos = (codigosDelGenerado.size() > codigosDelReferencia.size())?codigosDelGenerado:codigosDelReferencia;
		DeliveryNote referenciaAux = new DeliveryNote(referencia);
		
		ComparisonError errNotFound = null;
		ComparisonError errNotValid = null;
		ComparisonError errNotSameQuantity = null;
		ComparisonError errNotSamePvpTotal = null;
		//ComparisonError errNotSameUnitPvp = null;
		ComparisonError errBadCalculus = null;
		ComparisonError errNotFoundInGen = null;
		
		Double totalMoneyRef = 0.00;
		Double totalMoneyGen = 0.00;
		Integer totalElemsRef = 0;
		Integer totalElemsGen = 0;
		
		for (String code : codigos) {
			DeliveryNoteLine lineGen = generado.getCodeMap().get(code);
			DeliveryNoteLine lineRef = referenciaAux.getCodeMap().get(code);
			
			/**
			 * Error de no existencia en albarán de referencia
			 * */
			if(lineRef == null){
				if(errNotFound == null){
					errNotFound = new ComparisonError("comparisonError.notFoundInDeliveryNoteRef",
													  new ArrayList<DeliveryNoteLine>());
				}
				errNotFound.getDnlList().add(lineGen);
			}
			else if(lineGen == null){
				if(errNotFoundInGen == null){
					errNotFoundInGen = new ComparisonError("comparisonError.notFoundInGenDeliveryNote",
													new ArrayList<DeliveryNoteLine>());
				}
				errNotFoundInGen.getDnlList().add(lineRef);
			}
			else if(!lineGen.getValido()){
				if(errNotValid == null){
					errNotValid = new ComparisonError("comparisonError.notValid", 
													  new ArrayList<DeliveryNoteLine>());
				}
				errNotValid.getDnlList().add(lineGen);
			}
			else{
				totalMoneyGen += lineGen.getPvpTotal();
				totalElemsGen += lineGen.getCantidad();
				
				totalMoneyRef += lineRef.getPvpTotal();
				totalElemsRef += lineRef.getCantidad();
				lineRef.setDenominacion(lineRef.getDenominacion()+" [REF]");
				/**
				 * Error de falta de concordancia en el número de unidades de un producto
				 * */
				if(!lineRef.getCantidad().equals(lineGen.getCantidad())){
					if(errNotSameQuantity == null){
						errNotSameQuantity = new ComparisonError("comparisonError.notSameQuantity", 
																new ArrayList<DeliveryNoteLine>());
					}
					errNotSameQuantity.getDnlList().add(lineRef);
					errNotSameQuantity.getDnlList().add(lineGen);
				}
				/**
				 * Error de precio total de un producto
				 * */
				if(!lineRef.getPvpTotal().equals(lineGen.getPvpTotal())){
					if(errNotSamePvpTotal == null){
						errNotSamePvpTotal = new ComparisonError("comparisonError.notSamePvpTotal", 
																new ArrayList<DeliveryNoteLine>());
					}
					errNotSamePvpTotal.getDnlList().add(lineRef);
					errNotSamePvpTotal.getDnlList().add(lineGen);
				}
				/**
				 * Error de precio unitario de un producto
				 * */
				/*if(!lineRef.getPvp().equals(lineGen.getPvp())){
					if(errNotSameUnitPvp == null){
						errNotSameUnitPvp = new ComparisonError("comparisonError.notSameUnitPvp", 
																new ArrayList<DeliveryNoteLine>());
					}
					errNotSameUnitPvp.getDnlList().add(lineRef);
					errNotSameUnitPvp.getDnlList().add(lineGen);
				}*/
				/**
				 * Error en el cálculo de precio unitario
				 * */
				Double aux1 = lineRef.getPvpTotal()/(new Double(lineRef.getCantidad()));
				Double aux2 = lineGen.getPvpTotal()/(new Double(lineGen.getCantidad()));
				
				Double auxRef = Utils.roundTwoDecimals(aux1);
				Double auxGen = Utils.roundTwoDecimals(aux2);
				if(auxRef.doubleValue() != auxGen.doubleValue()){
					if(errBadCalculus == null){
						errBadCalculus = new ComparisonError("comparisonError.badCalculus", 
															new ArrayList<DeliveryNoteLine>());
					}
					lineRef.setPvp(auxRef);
					errBadCalculus.getDnlList().add(lineRef);
					lineGen.setDenominacion(lineGen.getDenominacion());
					lineGen.setPvp(auxGen);
					errBadCalculus.getDnlList().add(lineGen);
				}
			}
		}
		
		/**
		 * Incluímos los errores de no existencia
		 * */
		if(errNotFound != null){
			GenericSorter sorter = new GenericSorter(errNotFound.getDnlList());
			sorter.sortByAttribute("denominacion", GenericSorter.ASCENDING_ORDER);
			errors.add(errNotFound);
		}
		if(errNotFoundInGen != null){
			GenericSorter sorter = new GenericSorter(errNotFoundInGen.getDnlList());
			sorter.sortByAttribute("denominacion", GenericSorter.ASCENDING_ORDER);
			errors.add(errNotFoundInGen);
		}
		/**
		 * Añadimos los errores de no validez
		 * */
		if(errNotValid != null){
			GenericSorter sorter = new GenericSorter(errNotValid.getDnlList());
			sorter.sortByAttribute("denominacion", GenericSorter.ASCENDING_ORDER);
			errors.add(errNotValid);
		}
		/**
		 * Incluímos los errores de falta de concordancia en el número de unidades de productos
		 * */
		if(errNotSameQuantity != null){
			GenericSorter sorter = new GenericSorter(errNotSameQuantity.getDnlList());
			sorter.sortByAttribute("denominacion", GenericSorter.ASCENDING_ORDER);
			errors.add(errNotSameQuantity);
		}
		/**
		 * Error de falta de concordancia en el dinero total de un producto
		 * */
		if(errNotSamePvpTotal != null){
			GenericSorter sorter = new GenericSorter(errNotSamePvpTotal.getDnlList());
			sorter.sortByAttribute("denominacion", GenericSorter.ASCENDING_ORDER);
			errors.add(errNotSamePvpTotal);
		}
		/**
		 * Error en los precios unitarios
		 * */
		/*if(errNotSameUnitPvp != null){
			GenericSorter sorter = new GenericSorter(errNotSameUnitPvp.getDnlList());
			sorter.sortByAttribute("denominacion", GenericSorter.ASCENDING_ORDER);
			errors.add(errNotSameUnitPvp);
		}*/
		/**
		 * Error en el cálculo de precios unitarios
		 * */
		if(errBadCalculus != null){
			GenericSorter sorter = new GenericSorter(errBadCalculus.getDnlList());
			sorter.sortByAttribute("denominacion", GenericSorter.ASCENDING_ORDER);
			errors.add(errBadCalculus);
		}
		/**
		 * Descuadre en el dinero total
		 * */
		if(!totalMoneyRef.equals(totalMoneyGen)){
			errors.add(new ComparisonError("comparisonError.notSameTotalMoney", 
											new ArrayList<DeliveryNoteLine>()));
		}
		/**
		 * No coincide el número total de elementos
		 * */
		if(!totalElemsRef.equals(totalElemsGen) || 
		   !totalElemsRef.equals(referencia.getTotalProducts())){
			errors.add(new ComparisonError("comparisonError.notSameTotalQuantity", 
											new ArrayList<DeliveryNoteLine>()));
		}
		
		return errors;
	}
}
	
