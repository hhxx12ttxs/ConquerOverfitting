package com.checaro.pharmacy.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import com.checaro.pharmacy.groups.interfaces.Group;
import com.checaro.pharmacy.model.DeliveryNote;
import com.checaro.pharmacy.model.DeliveryNoteLine;
import com.checaro.pharmacy.service.interfaces.DocumentGenerationService;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service("documentGenerationService")
public class DocumentGenerationServiceImpl implements DocumentGenerationService, Serializable {
	private static final long serialVersionUID = 32592659322049785L;
	private static final float DOC_TOP_MARGIN = 10.0f;
	private static final float DOC_BOTTOM_MARGIN = 10.0f;
	private static final float DOC_LEFT_MARGIN = 40.0f;
	private static final float DOC_RIGHT_MARGIN = 10.0f;
	
	@Override
	public void generateGeneralDeliveryNote(List<DeliveryNote> dnList,
			List<Group> groups, OutputStream outs) {
		try{
			Document document = new Document(PageSize.A4.rotate(), DOC_LEFT_MARGIN, DOC_RIGHT_MARGIN, DOC_TOP_MARGIN, DOC_BOTTOM_MARGIN);
			PdfWriter writer = PdfWriter.getInstance(document, outs);
			document.open();
			// Se concatenan todos los albaranes, uno detrás de otro
			for(DeliveryNote dn: dnList){
				DeliveryNote dnAux = new DeliveryNote(dn);
				PdfPTable table = null;
				if(hasObservations(dnAux)){
					table = generateMarkedTableWithPriceAndObservations(dnAux, groups);
				}
				else{
					table = generateMarkedTableWithPrice(dnAux, groups);
				}
				document.add(table);
				document.newPage();
			}
			// Se genera la tabla resumen final de cantidades
			// Se genera la tabla resumen final de dinero
			List<PdfPTable> generalTables = generateSummaryTables(dnList, groups);
			for(PdfPTable pTable : generalTables){
				document.add(pTable);
				document.add(new Paragraph("                       "));
			}
			// Se cierra el documento
			document.close();
   			outs.flush();
            outs.close();
		}catch(DocumentException de){
			de.printStackTrace();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}

	@Override
	public void generatePDFWithPrice(DeliveryNote dn, OutputStream outs) {
		try {
   			Document document = new Document(PageSize.A4.rotate(), DOC_LEFT_MARGIN, DOC_RIGHT_MARGIN, DOC_TOP_MARGIN, DOC_BOTTOM_MARGIN);
   			PdfWriter writer = PdfWriter.getInstance(document, outs);
   			document.open();
   			PdfPTable table = null;
   			if(hasObservations(dn)){
   				table = generateTableWithPriceAndObservations(dn);
   			}
   			else{
   				table = generateTableWithPrice(dn);
   			}
   			document.add(table);
   			document.add(new Paragraph("Total productos: "+dn.getTotalProducts()));
   			document.add(new Paragraph("Total euros: "+roundTwoDecimals(dn.getTotalEuros())));
   			document.close();
   			outs.flush();
            outs.close();
            
   		} catch (DocumentException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void generatePDFWithoutPrice(DeliveryNote dn, OutputStream outs) {
		try {
			Document document = new Document(PageSize.A4.rotate(), DOC_LEFT_MARGIN, DOC_RIGHT_MARGIN, DOC_TOP_MARGIN, DOC_BOTTOM_MARGIN);
   			PdfWriter writer = PdfWriter.getInstance(document, outs);
   			document.open();
   			PdfPTable table = null;
   			if(hasObservations(dn)){
   				table = generateTableWithoutPriceAndObservations(dn);
   			}
   			else{
   				table = generateTableWithoutPrice(dn);
   			}
   			document.add(table);
   			document.add(new Paragraph("Total productos: "+dn.getTotalProducts()));
   			document.close();
   			outs.flush();
            outs.close();
   		} catch (DocumentException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void generateMarkedPDFWithPrice(DeliveryNote dn, List<Group> groups,
			OutputStream outs) {
		try {
			Document document = new Document(PageSize.A4.rotate(), DOC_LEFT_MARGIN, DOC_RIGHT_MARGIN, DOC_TOP_MARGIN, DOC_BOTTOM_MARGIN);
   			PdfWriter writer = PdfWriter.getInstance(document, outs);
   			document.open();
   			PdfPTable table = null;
   			if(hasObservations(dn)){
   				table = generateMarkedTableWithPriceAndObservations(dn, groups);
   			}
   			else{
   				table = generateMarkedTableWithPrice(dn, groups);
   			}
   			document.add(table);
   			document.add(new Paragraph("Total productos: "+dn.getTotalProducts()));
   			document.add(new Paragraph("Total euros: "+roundTwoDecimals(dn.getTotalEuros())));
   			document.close();
   			outs.flush();
            outs.close();
            
   		} catch (DocumentException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void generateMarkedPDFWithoutPrice(DeliveryNote dn,
			List<Group> groups, OutputStream outs){
		DeliveryNote dnAux = new DeliveryNote(dn);
		try {
			Document document = new Document(PageSize.A4.rotate(), DOC_LEFT_MARGIN, DOC_RIGHT_MARGIN, DOC_TOP_MARGIN, DOC_BOTTOM_MARGIN);
   			PdfWriter writer = PdfWriter.getInstance(document, outs);
   			document.open();
   			PdfPTable table = null;
   			if(hasObservations(dnAux)){
   				table = generateMarkedTableWithoutPriceAndObservations(dnAux, groups);
   			}
   			else{
   				table = generateMarkedTableWithoutPrice(dnAux, groups);
   			}
   			document.add(table);
   			document.add(new Paragraph("Total productos: "+dnAux.getTotalProducts()));
   			document.close();
   			outs.flush();
            outs.close();
   		} catch (DocumentException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 *   MÉTODOS AUXILIARES 
	 */	
	
	private List<PdfPTable> generateSummaryTables(List<DeliveryNote> dnList,
			List<Group> groups) {
		PdfPTable quantityTables = new PdfPTable(new float[]{50.0f, 40.0f});
		// Cabecera
		quantityTables.addCell("Cuadro Resumen Cantidades");
		quantityTables.addCell("");
		// Línea en blanco de separación
		quantityTables.addCell("");
		quantityTables.addCell("");
		// Títulos
		quantityTables.addCell("Concepto");
		quantityTables.addCell("Valor");
		
		PdfPTable moneyTables = new PdfPTable(new float[]{50.0f, 40.0f});
		// Cabecera
		moneyTables.addCell("Cuadro Resumen Euros");
		moneyTables.addCell("");
		// Línea en blanco de separación
		moneyTables.addCell("");
		moneyTables.addCell("");
		// Títulos
		moneyTables.addCell("Concepto");
		moneyTables.addCell("Valor");
		
		for(Group group : groups){
			Double totalEurosGroup = 0.0;
			Integer totalElemsGroup = 0;
			for(DeliveryNote dn : dnList){
				for(DeliveryNoteLine dnl : dn.getLines()){
					if(group.contains(dnl)){
						Integer posLine = dn.getLines().indexOf(dnl);
						DeliveryNoteLine auxLine = dn.getLines().get(posLine);
						if(auxLine.getValido()){
							totalEurosGroup += auxLine.getPvpTotal();
							totalElemsGroup += auxLine.getCantidad();
						}
					}
				}
			}
			quantityTables.addCell("Total productos '"+group.getGroupName()+"'");
			quantityTables.addCell(totalElemsGroup.toString());
			
			moneyTables.addCell("Total dinero '"+group.getGroupName()+"'");
			moneyTables.addCell(Double.toString(roundTwoDecimals(totalEurosGroup)));
		}
		List<PdfPTable> result = new ArrayList<PdfPTable>();
		result.add(quantityTables);
		result.add(moneyTables);
		
		return result;
	}
	
	private double roundTwoDecimals(double d) {
		double d100 = d*100;
		long dtrunc = Math.round(d100);
		double d2 = dtrunc;
		double d3 = d2/100.0;
		return d3;
	}
	
	private PdfPTable generateTableWithoutPrice(DeliveryNote dn){
		PdfPTable result = new PdfPTable(new float[]{13.0f, 65.0f, 18.0f});
		DateTime hoy = dn.getDate();
		
		result.addCell("");
		result.addCell("Albarán"); // Título 
		result.addCell(hoy.getDayOfMonth()+"/"+hoy.getMonthOfYear()+"/"+hoy.getYear()); // Fecha
		
		result.addCell("Código");
		result.addCell("Nombre"); // Línea en blanco
		result.addCell("Cantidad");
		
		for (DeliveryNoteLine dnl : dn.getLines()) {
			if(dnl.getValido()){
				result.addCell(dnl.getCodigo());
				result.addCell(dnl.getDenominacion());
				result.addCell(dnl.getCantidad().toString());
			}
			else{
				result.addCell(crossText(dnl.getCodigo()));
				result.addCell(crossText(dnl.getDenominacion()));
				result.addCell(crossText(dnl.getCantidad().toString()));
			}
		}
		return result;
	}
	
	private PdfPTable generateTableWithoutPriceAndObservations(DeliveryNote dn) {
		PdfPTable result = new PdfPTable(new float[]{13.0f, 65.0f, 18.0f, 20.0f});
		DateTime hoy = dn.getDate();
		
		result.addCell(" ");
		result.addCell("Albarán"); // Título 
		result.addCell(hoy.getDayOfMonth()+"/"+hoy.getMonthOfYear()+"/"+hoy.getYear()); // Fecha
		result.addCell(" ");
		
		result.addCell("Código");
		result.addCell("Nombre"); // Línea en blanco
		result.addCell("Cantidad");
		result.addCell("Observaciones");
		
		for (DeliveryNoteLine dnl : dn.getLines()) {
			if(dnl.getValido()){
				result.addCell(dnl.getCodigo());
				result.addCell(dnl.getDenominacion());
				result.addCell(dnl.getCantidad().toString());
				result.addCell(dnl.getObservaciones());
			}
			else{
				result.addCell(crossText(dnl.getCodigo()));
				result.addCell(crossText(dnl.getDenominacion()));
				result.addCell(crossText(dnl.getCantidad().toString()));
				result.addCell(crossText(dnl.getObservaciones()));
			}
		}
		return result;
	}
	
	private PdfPTable generateMarkedTableWithoutPrice(DeliveryNote dn, List<Group> groups){
		PdfPTable result = new PdfPTable(new float[]{13.0f, 18.0f, 65.0f, 20.0f});
		DateTime hoy = dn.getDate();
		
		result.addCell("");
		result.addCell("");
		result.addCell("Albarán"); // Título 
		result.addCell(hoy.getDayOfMonth()+"/"+hoy.getMonthOfYear()+"/"+hoy.getYear()); // Fecha
		
		result.addCell("Familia");
		result.addCell("Código");
		result.addCell("Nombre"); // Línea en blanco
		result.addCell("Cantidad");
		
		for (DeliveryNoteLine dnl : dn.getLines()) {
			List<String> groupsAux = getGroupFromLine(dnl, groups);
			String groupsFlat = flatGroup(groupsAux);
			if(dnl.getValido()){
				result.addCell(groupsFlat);
				result.addCell(dnl.getCodigo());
				result.addCell(dnl.getDenominacion());
				result.addCell(dnl.getCantidad().toString());
			}
			else{
				result.addCell(crossText(groupsFlat));
				result.addCell(crossText(dnl.getCodigo()));
				result.addCell(crossText(dnl.getDenominacion()));
				result.addCell(crossText(dnl.getCantidad().toString()));
			}
		}	
		return result;
	}
	
	private PdfPTable generateMarkedTableWithoutPriceAndObservations(
			DeliveryNote dnAux, List<Group> groups) {
		PdfPTable result = new PdfPTable(new float[]{13.0f, 18.0f, 65.0f, 20.0f, 20.0f});
		DateTime hoy = dnAux.getDate();
		
		result.addCell("");
		result.addCell("");
		result.addCell("Albarán"); // Título 
		result.addCell(hoy.getDayOfMonth()+"/"+hoy.getMonthOfYear()+"/"+hoy.getYear()); // Fecha
		result.addCell(" ");
		
		result.addCell("Familia");
		result.addCell("Código");
		result.addCell("Nombre"); // Línea en blanco
		result.addCell("Cantidad");
		result.addCell("Observaciones");
		
		for (DeliveryNoteLine dnl : dnAux.getLines()) {
			List<String> groupsAux = getGroupFromLine(dnl, groups);
			String groupsFlat = flatGroup(groupsAux);
			if(dnl.getValido()){
				result.addCell(groupsFlat);
				result.addCell(dnl.getCodigo());
				result.addCell(dnl.getDenominacion());
				result.addCell(dnl.getCantidad().toString());
				result.addCell(dnl.getObservaciones());
			}
			else{
				result.addCell(crossText(groupsFlat));
				result.addCell(crossText(dnl.getCodigo()));
				result.addCell(crossText(dnl.getDenominacion()));
				result.addCell(crossText(dnl.getCantidad().toString()));
				result.addCell(crossText(dnl.getObservaciones()));
			}
		}
		return result;
	}
	
	private PdfPTable generateMarkedTableWithPrice(DeliveryNote dn, List<Group> groups){
		PdfPTable result = null;
		result = new PdfPTable(new float[]{20.0f, 13.0f, 65.0f, 15.0f, 20.0f});
		DateTime hoy = dn.getDate();
		
		result.addCell("");
		result.addCell("");
		result.addCell("Albarán"); // Título 
		result.addCell(" "); // Celda en blanco
		result.addCell(hoy.getDayOfMonth()+"/"+hoy.getMonthOfYear()+"/"+hoy.getYear()); // Fecha
		
		result.addCell("Familia");
		result.addCell("Código");
		result.addCell("Nombre"); // Línea en blanco
		result.addCell("Cantidad");
		result.addCell("Precio");
		
		for (DeliveryNoteLine dnl : dn.getLines()) {
			List<String> groupsAux = getGroupFromLine(dnl, groups);
			String groupsFlat = flatGroup(groupsAux);
			if(dnl.getValido()){
				result.addCell(groupsFlat);
				result.addCell(dnl.getCodigo());
				result.addCell(dnl.getDenominacion());
				result.addCell(dnl.getCantidad().toString());
				result.addCell(dnl.getPvpTotal().toString());
			}
			else{
				result.addCell(crossText(groupsFlat));
				result.addCell(crossText(dnl.getCodigo()));
				result.addCell(crossText(dnl.getDenominacion()));
				result.addCell(crossText(dnl.getCantidad().toString()));
				result.addCell(crossText(dnl.getPvpTotal().toString()));
			}
		}
		return result;
	}
	
	private PdfPTable generateMarkedTableWithPriceAndObservations(DeliveryNote dn, List<Group> groups){
		PdfPTable result = null;
		result = new PdfPTable(new float[]{20.0f, 13.0f, 65.0f, 14.0f, 18.0f, 23.0f});
		DateTime hoy = dn.getDate();
		
		result.addCell("");
		result.addCell("");
		result.addCell("Albarán"); // Título 
		result.addCell(" "); // Celda en blanco
		result.addCell(hoy.getDayOfMonth()+"/"+hoy.getMonthOfYear()+"/"+hoy.getYear()); // Fecha
		result.addCell(" ");
		
		result.addCell("Familia");
		result.addCell("Código");
		result.addCell("Nombre"); // Línea en blanco
		result.addCell("Cantidad");
		result.addCell("Precio");
		result.addCell("Observaciones");
		
		for (DeliveryNoteLine dnl : dn.getLines()) {
			List<String> groupsAux = getGroupFromLine(dnl, groups);
			String groupsFlat = flatGroup(groupsAux);
			if(dnl.getValido()){
				result.addCell(groupsFlat);
				result.addCell(dnl.getCodigo());
				result.addCell(dnl.getDenominacion());
				result.addCell(dnl.getCantidad().toString());
				result.addCell(dnl.getPvpTotal().toString());
				result.addCell(dnl.getObservaciones());
			}
			else{
				result.addCell(crossText(groupsFlat));
				result.addCell(crossText(dnl.getCodigo()));
				result.addCell(crossText(dnl.getDenominacion()));
				result.addCell(crossText(dnl.getCantidad().toString()));
				result.addCell(crossText(dnl.getPvpTotal().toString()));
				result.addCell(crossText(dnl.getObservaciones()));
			}
		}
		return result;
	}

	private PdfPTable generateTableWithPriceAndObservations(DeliveryNote dn){
		PdfPTable result = new PdfPTable(new float[]{15.0f, 65.0f, 18.0f, 23.0f, 20.0f});
		DateTime hoy = dn.getDate();
		
		result.addCell(" ");
		result.addCell("Albarán"); // Título 
		result.addCell(" "); // Celda en blanco
		result.addCell(hoy.getDayOfMonth()+"/"+hoy.getMonthOfYear()+"/"+hoy.getYear()); // Fecha
		result.addCell(" ");
		
		result.addCell("Código");
		result.addCell("Nombre"); // Línea en blanco
		result.addCell("Cantidad");
		result.addCell("Precio");
		result.addCell("Observaciones");
		
		for (DeliveryNoteLine dnl : dn.getLines()) {
			if(dnl.getValido()){
				result.addCell(dnl.getCodigo());
				result.addCell(dnl.getDenominacion());
				result.addCell(dnl.getCantidad().toString());
				result.addCell(dnl.getPvpTotal().toString());
				result.addCell(dnl.getObservaciones());
			}
			else{
				result.addCell(crossText(dnl.getCodigo()));
				result.addCell(crossText(dnl.getDenominacion()));
				result.addCell(crossText(dnl.getCantidad().toString()));
				result.addCell(crossText(dnl.getPvpTotal().toString()));
				result.addCell(crossText(dnl.getObservaciones()));
			}
		}

		return result;
	}
	
	private PdfPTable generateTableWithPrice(DeliveryNote dn){
		PdfPTable result = new PdfPTable(new float[]{15.0f, 65.0f, 18.0f, 23.0f});
			
		DateTime hoy = dn.getDate();
		
		result.addCell(" ");
		result.addCell("Albarán"); // Título 
		result.addCell(" "); // Celda en blanco
		result.addCell(hoy.getDayOfMonth()+"/"+hoy.getMonthOfYear()+"/"+hoy.getYear()); // Fecha
		
		result.addCell("Código");
		result.addCell("Nombre"); // Línea en blanco
		result.addCell("Cantidad");
		result.addCell("Precio");
		
		for (DeliveryNoteLine dnl : dn.getLines()) {
			if(dnl.getValido()){
				result.addCell(dnl.getCodigo());
				result.addCell(dnl.getDenominacion());
				result.addCell(dnl.getCantidad().toString());
				result.addCell(dnl.getPvpTotal().toString());
			}
			else{
				result.addCell(crossText(dnl.getCodigo()));
				result.addCell(crossText(dnl.getDenominacion()));
				result.addCell(crossText(dnl.getCantidad().toString()));
				result.addCell(crossText(dnl.getPvpTotal().toString()));
			}
		}
		return result;
	}
	
	private boolean hasObservations(DeliveryNote dn) {
		List<DeliveryNoteLine> lineList = dn.getLines();
		Integer i = 0;
		Boolean found = false;
		while(!found && i < lineList.size()){
			DeliveryNoteLine aux = lineList.get(i);
			if(aux.getObservaciones().length() > 0){
				found = true;
			}
			i++;
		}
		return found;
	}

	private Phrase crossText(String text){
		Chunk chunk = new Chunk(text);
		Phrase phrase = new Phrase(chunk);
		chunk.setUnderline(2f, 4f);
		return phrase;
	}
	
	private List<String> getGroupFromLine(DeliveryNoteLine dnl, List<Group> groups) {
		List<String> result = new ArrayList<String>();
		for(Group groupAux:groups){
			if(groupAux.contains(dnl)){
				result.add(groupAux.getGroupName());
			}
		}
		return result;
	}
	
	private String flatGroup(List<String> groupsAux) {
		String result = new String("");
		for(Integer i = 0; i < groupsAux.size() ; i++){
			String group = groupsAux.get(i);
			result = result.concat(group);
			if(i < groupsAux.size() - 1){
				result = result.concat("\n");
			}
		}
		return result;
	}
}

