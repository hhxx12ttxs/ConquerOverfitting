/*******************************************************************************
 * Copyright (c) 2003 - 2011 Jeffrey Cox, Dustin O'brien.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.txt
 * 
 * Contributors:
 *     Jeffrey Cox, Dustin O'brien - initial API and implementation
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.daddysgarage.casc.summercampplanner.portlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.portlet.PortletContext;

import com.daddysgarage.casc.summercampplanner.model.Activity;
import com.daddysgarage.casc.summercampplanner.model.ActivitySection;
import com.daddysgarage.casc.summercampplanner.model.CampSession;
import com.daddysgarage.casc.summercampplanner.model.Council;
import com.daddysgarage.casc.summercampplanner.model.District;
import com.daddysgarage.casc.summercampplanner.model.Enrollment;
import com.daddysgarage.casc.summercampplanner.model.Unit;
import com.daddysgarage.casc.summercampplanner.model.UnitMember;
import com.daddysgarage.casc.summercampplanner.model.UnitReservation;
import com.daddysgarage.casc.summercampplanner.service.ActivitySectionLocalServiceUtil;
import com.daddysgarage.casc.summercampplanner.service.CampSessionLocalServiceUtil;
import com.daddysgarage.casc.summercampplanner.service.CouncilLocalServiceUtil;
import com.daddysgarage.casc.summercampplanner.service.DistrictLocalServiceUtil;
import com.daddysgarage.casc.summercampplanner.service.EnrollmentLocalServiceUtil;
import com.daddysgarage.casc.summercampplanner.service.UnitLocalServiceUtil;
import com.daddysgarage.casc.summercampplanner.service.UnitMemberLocalServiceUtil;
import com.daddysgarage.casc.summercampplanner.service.UnitReservationLocalServiceUtil;
import com.daddysgarage.casc.summercampplanner.service.UnitReservationService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDestination;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfOutline;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
//import com.lowagie.text.HeaderFooter;

public class ReportGenerator
{

	private BaseFont				baseFont		= null;
	private Font					headerFont		= null;
	private Font					font			= null;
	private HeaderFooter			headerFooter	= new HeaderFooter();
	private static ReportGenerator	instance		= null;

	private ReportGenerator()
	{
		try
		{
			baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
			headerFont = new Font(baseFont, 10, Font.BOLD);
			font = new Font(baseFont, 8, Font.NORMAL);
		} catch (DocumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ReportGenerator getInstance()
	{
		if (instance == null)
			instance = new ReportGenerator();

		return instance;
	}
	
	public ByteArrayOutputStream getUnitMemberActivityRecord(UnitReservation reservation, UnitMember unitMember, int sessionId) throws PortalException, SystemException, DocumentException, IOException
	{
		Unit unit = UnitLocalServiceUtil.getUnit(reservation.getUnitId());
		CampSession campSession = CampSessionLocalServiceUtil.getCampSession(sessionId);
		District district = DistrictLocalServiceUtil.getDistrict(unit.getDistrictId());
		Council council = CouncilLocalServiceUtil.getCouncil(district.getCouncilId());
		
		List<UnitMember> unitMembers = new ArrayList<UnitMember>();
		unitMembers.add(unitMember);

		ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
		Document document = new Document(PageSize.LETTER, 10, 10, 36, 36);

		PdfWriter writer = PdfWriter.getInstance(document, memoryStream);
		writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);
		writer.setPageEvent(headerFooter);

		headerFooter.setCouncil(council);
		headerFooter.setDistrict(district);
		headerFooter.setUnit(unit);

		document.open();
		PdfOutline parent = writer.getRootOutline();
		
		addUnitActivityRecord(unitMembers, document, parent, writer, campSession.getPrimaryKey());

		document.close();

		return memoryStream;
	}
	
	public ByteArrayOutputStream getAllUnitActivityReportsByUnitMemberPdf(CampSession campSession) throws PortalException, SystemException, DocumentException, IOException
	{
		
		List<UnitReservation> reservations = UnitReservationLocalServiceUtil.getReservationsBySession(campSession.getSessionKey());

		ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
		Document document = new Document(PageSize.LETTER, 10, 10, 36, 36);
	
		PdfWriter writer = PdfWriter.getInstance(document, memoryStream);
		headerFooter.resetPageNumber();
		writer.setPageEvent(headerFooter);
		document.open();
		
		for(UnitReservation reservation: reservations)
		{
			headerFooter.resetPageNumber();
			Unit unit = UnitLocalServiceUtil.getUnit(reservation.getUnitId());
			District district = DistrictLocalServiceUtil.getDistrict(unit.getDistrictId());
			Council council = CouncilLocalServiceUtil.getCouncil(district.getCouncilId());
	
			List<UnitMember> unitMembers = UnitMemberLocalServiceUtil.getRegisteredMembers(reservation.getPrimaryKey());
	
			headerFooter.setCouncil(council);
			headerFooter.setDistrict(district);
			headerFooter.setUnit(unit);
	
			PdfOutline parent = writer.getRootOutline();
			PdfOutline unitBookMark = new PdfOutline(parent, new PdfDestination(PdfDestination.FITH, writer.getVerticalPosition(true)), "Unit " + unit.getUnitNumber(), true);
			unitBookMark.setStyle(Font.BOLD);
			addUnitActivityRecord(unitMembers, document, unitBookMark, writer, campSession.getPrimaryKey());
			
			if((headerFooter.getPagenumber() % 2) != 0)
			{
				document.newPage();
				writer.setPageEmpty(false);
				document.newPage();
			}
			document.newPage();
		}
		
		document.close();
		return memoryStream;

	}
	
	public ByteArrayOutputStream getUnitActivityReportByUnitMemberPdf(UnitReservation reservation, int sessionId) throws PortalException, SystemException,
			DocumentException, IOException
	{

		Unit unit = UnitLocalServiceUtil.getUnit(reservation.getUnitId());
		CampSession campSession = CampSessionLocalServiceUtil.getCampSession(sessionId);
		District district = DistrictLocalServiceUtil.getDistrict(unit.getDistrictId());
		Council council = CouncilLocalServiceUtil.getCouncil(district.getCouncilId());

		List<UnitMember> unitMembers = UnitMemberLocalServiceUtil.getRegisteredMembers(reservation.getPrimaryKey());

		ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
		Document document = new Document(PageSize.LETTER, 10, 10, 36, 36);

		PdfWriter writer = PdfWriter.getInstance(document, memoryStream);
		writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);
		writer.setPageEvent(headerFooter);

		headerFooter.setCouncil(council);
		headerFooter.setDistrict(district);
		headerFooter.setUnit(unit);

		document.open();
		PdfOutline parent = writer.getRootOutline();

		addUnitActivityRecord(unitMembers, document, parent, writer, campSession.getPrimaryKey());

		document.close();

		return memoryStream;

	}
	
	public ByteArrayOutputStream getAllUnitRosterPdf(CampSession campSession) throws PortalException, SystemException, DocumentException, IOException
	{
		
		List<UnitReservation> reservations = UnitReservationLocalServiceUtil.getReservationsBySession(campSession.getSessionKey());

		ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
		Document document = new Document(PageSize.LETTER, 10, 10, 36, 36);
	
		PdfWriter writer = PdfWriter.getInstance(document, memoryStream);
		writer.setPageEvent(headerFooter);
		document.open();
		
		for(UnitReservation reservation: reservations)
		{
			headerFooter.resetPageNumber();
			Unit unit = UnitLocalServiceUtil.getUnit(reservation.getUnitId());
			District district = DistrictLocalServiceUtil.getDistrict(unit.getDistrictId());
			Council council = CouncilLocalServiceUtil.getCouncil(district.getCouncilId());
	
			List<UnitMember> unitMembers = UnitMemberLocalServiceUtil.getRegisteredMembers(reservation.getPrimaryKey());
	
	
			headerFooter.setCouncil(council);
			headerFooter.setDistrict(district);
			headerFooter.setUnit(unit);
	
			PdfOutline parent = writer.getRootOutline();
			PdfOutline unitBookMark = new PdfOutline(parent, new PdfDestination(PdfDestination.FITH, writer.getVerticalPosition(true)), "Unit " + unit.getUnitNumber(), true);
			unitBookMark.setStyle(Font.BOLD);
			
			if (unitMembers.isEmpty())
				document.add(new Paragraph("Empty Record: There are no registered unit members"));
			else
				writeUnitRoster(unitMembers, document);

			if((headerFooter.getPagenumber() % 2) != 0)
			{
				document.newPage();
				writer.setPageEmpty(false);
				document.newPage();
			}
			document.newPage();
		}
		
		document.close();
		return memoryStream;

	}

	public ByteArrayOutputStream getUnitRosterPdf(UnitReservation reservation) throws PortalException, SystemException, DocumentException, IOException
	{

		Unit unit = UnitLocalServiceUtil.getUnit(reservation.getUnitId());
		District district = DistrictLocalServiceUtil.getDistrict(unit.getDistrictId());
		Council council = CouncilLocalServiceUtil.getCouncil(district.getCouncilId());

		List<UnitMember> unitMembers = UnitMemberLocalServiceUtil.getRegisteredMembers(reservation.getPrimaryKey());

		ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
		Document document = new Document(PageSize.LETTER, 10, 10, 36, 36);

		PdfWriter writer = PdfWriter.getInstance(document, memoryStream);

		writer.setPageEvent(headerFooter);

		headerFooter.setCouncil(council);
		headerFooter.setDistrict(district);
		headerFooter.setUnit(unit);

		document.open();

		if (unitMembers.isEmpty())
			document.add(new Paragraph("Empty Record: There are no registered unit members"));
		else
			writeUnitRoster(unitMembers, document);

		document.close();
		return memoryStream;
	}

	private void addUnitActivityRecord(List<UnitMember> unitMembers, Document document, PdfOutline parent, PdfWriter writer, int sessionId)
			throws DocumentException, SystemException, PortalException
	{
		boolean hasRecords = false;
		
		if (unitMembers.isEmpty())
		{
			document.add(new Paragraph("Empty Record: There are no registered unit members"));
		} else
		{
			for (UnitMember unitMember : unitMembers)
			{
				List<Object[]> results = EnrollmentLocalServiceUtil.getUnitMemberEnrollments(unitMember.getPrimaryKey(), sessionId);
				hasRecords = addUnitMemberActivityRecord(unitMember, document, results);
					if (parent != null)
					{
						PdfOutline unitMemberBookMark = new PdfOutline(parent, new PdfDestination(PdfDestination.FITH, writer.getVerticalPosition(true)),
								unitMember.getFirstName().toUpperCase() + " " + unitMember.getLastName().toUpperCase(), true);
						unitMemberBookMark.setStyle(Font.BOLD);
					}
			}
		}
		if(!hasRecords)
			document.add(new Paragraph("Empty Record: The are no activity records"));
	}

	private boolean addUnitMemberActivityRecord(UnitMember unitMember, Document document, List<Object[]> results) throws DocumentException, PortalException,
			SystemException
	{
		boolean hasRecords = false;
		Phrase memberPhrase = new Phrase("\n" + unitMember.getFirstName().toUpperCase() + " " + unitMember.getLastName().toUpperCase(), headerFont);
		Paragraph memberHeader = new Paragraph(memberPhrase);
		memberHeader.setAlignment(Paragraph.ALIGN_LEFT);
		memberHeader.setKeepTogether(true);

		PdfPTable memberTable = new PdfPTable(1);
		memberTable.setWidthPercentage(100f);

		PdfPCell headerCell = new PdfPCell(memberHeader);
		headerCell.setBorderWidth(0.0f);

		memberTable.addCell(headerCell);

		memberTable.setHeaderRows(1);
		memberTable.setSplitRows(false);

		for (Object[] result : results)
		{
			Activity activity = (Activity) result[0];
			ActivitySection section = (ActivitySection) result[1];
			Enrollment enrollment = (Enrollment) result[2];

			UnitActivityRecordTableModel tableModel = new UnitActivityRecordTableModel(unitMember, enrollment.getSessionActivityId());

			PdfPTable activityTable = new PdfPTable(tableModel.getColumnCount());
			activityTable.setWidthPercentage(100f);

			PdfPCell headerRow = new PdfPCell(new Phrase(activity.getName() + "     " + ActivitySectionLocalServiceUtil.getString(section)));
			headerRow.setColspan(tableModel.getColumnCount());
			headerRow.setGrayFill(0.9f);
			headerRow.setBorderWidth(0.0f);

			activityTable.addCell(headerRow);

			int columnWidths[] = new int[tableModel.getColumnCount()];
			for (int i = 0; i < tableModel.getColumnCount(); i++)
			{
				columnWidths[i] = 1;
				PdfPCell cell = new PdfPCell(new Phrase(tableModel.getColumnName(i), font));
				activityTable.addCell(cell);
			}
			activityTable.setWidths(columnWidths);
			for (int i = 0; i < tableModel.getRowCount(); i++)
			{
				for (int m = 0; m < tableModel.getColumnCount(); m++)
				{
					if (m == 5 || m == 7)
					{
						activityTable.addCell(" ");
					} else if (((Boolean) tableModel.getValueAt(i, m)).booleanValue())
					{
						activityTable.addCell("X");
					} else
					{
						activityTable.addCell(" ");
					}
				}
			}
			memberTable.addCell(activityTable);
			hasRecords = true;
		}
		document.add(memberTable);
		return hasRecords;
	}

	private void writeUnitRoster(List<UnitMember> unitMembers, Document document) throws DocumentException
	{
		int columnWidths[];
		PdfPTable table = null;
		int numAdults = 0;
		int numYouth = 0;
		UnitRosterTableModel tableModel = new UnitRosterTableModel();

		columnWidths = new int[tableModel.getColumnCount()];
		columnWidths = new int[tableModel.getColumnCount()];
		table = new PdfPTable(tableModel.getColumnCount());
		table.setWidthPercentage(100f);
		addColumnHeaderToRoster(tableModel, columnWidths, table, font);
		table.setWidths(columnWidths);

		for (UnitMember unitMember : unitMembers)
		{
			tableModel = new UnitRosterTableModel(unitMember);

			for (int i = 0; i < tableModel.getRowCount(); i++)
			{
				for (int m = 0; m < tableModel.getColumnCount(); m++)
				{
					if (m == 0)
					{
						table.addCell(new Phrase((String) tableModel.getValueAt(i, m), font));
					} else if (m == 1)
					{
						String memberType = (String) tableModel.getValueAt(i, m);

						if (memberType.equals("SCOUT"))
							numYouth++;
						else
							numAdults++;

						table.addCell(new Phrase(memberType, font));
					} else if (m >= 2 && m <= 8)
					{
						table.addCell(" ");
					} else if (((Boolean) tableModel.getValueAt(i, m)).booleanValue())
					{
						table.addCell("X");
					} else
					{
						table.addCell(" ");
					}
				}
			}
		}

		if (table != null)
		{
			PdfPCell numYouthCell = new PdfPCell(new Phrase("Total Youth = " + numYouth, headerFont));
			numYouthCell.setHorizontalAlignment(numYouthCell.ALIGN_RIGHT);
			numYouthCell.setBorderWidth(0.0f);
			numYouthCell.setColspan(table.getNumberOfColumns());
			table.addCell(numYouthCell);

			PdfPCell numAdultCell = new PdfPCell(new Phrase("Total Adults = " + numAdults, headerFont));
			numAdultCell.setHorizontalAlignment(numAdultCell.ALIGN_RIGHT);
			numAdultCell.setBorderWidth(0.0f);
			numAdultCell.setColspan(table.getNumberOfColumns());
			table.addCell(numAdultCell);

			document.add(table);
		}
	}

	private void addColumnHeaderToRoster(UnitRosterTableModel tableModel, int columnWidths[], PdfPTable table, Font font)
	{
		PdfPCell unitCell = null;
		if (headerFooter.getDistrict().equals("OC"))
			unitCell = new PdfPCell(new Phrase("Troop " + headerFooter.getUnit().getUnitNumber() + " " + headerFooter.getCouncil().getName() + " Council",
					headerFont));
		else
			unitCell = new PdfPCell(new Phrase("Troop " + headerFooter.getUnit().getUnitNumber() + " " + headerFooter.getDistrict().getName() + " District",
					headerFont));

		unitCell.setHorizontalAlignment(unitCell.ALIGN_CENTER);
		unitCell.setBorderWidth(0.0f);
		unitCell.setColspan(tableModel.getColumnCount());
		table.addCell(unitCell);

		PdfPCell memberCell = new PdfPCell(new Phrase(tableModel.getColumnName(0), font));
		memberCell.setGrayFill(0.9f);
		table.addCell(memberCell);

		PdfPCell typeCell = new PdfPCell(new Phrase(tableModel.getColumnName(1), font));
		typeCell.setGrayFill(0.9f);
		table.addCell(typeCell);

		PdfPCell aquaticsCell = new PdfPCell(new Phrase("Aquatics", font));
		aquaticsCell.setHorizontalAlignment(aquaticsCell.ALIGN_CENTER);
		aquaticsCell.setGrayFill(0.9f);
		aquaticsCell.setColspan(6);
		table.addCell(aquaticsCell);

		PdfPCell availabilityCell = new PdfPCell(new Phrase("Availability", font));
		availabilityCell.setHorizontalAlignment(availabilityCell.ALIGN_CENTER);
		availabilityCell.setGrayFill(0.9f);
		availabilityCell.setColspan(8);
		table.addCell(availabilityCell);

		PdfPCell blankCell = new PdfPCell(new Phrase(" ", font));
		blankCell.setGrayFill(0.9f);
		table.addCell(blankCell);
		table.addCell(blankCell);

		columnWidths[0] = 15;
		columnWidths[1] = 10;
		for (int i = 2; i < tableModel.getColumnCount(); i++)
		{
			columnWidths[i] = 1;
			PdfPCell cell = new PdfPCell(new Phrase(tableModel.getColumnName(i), font));
			cell.setGrayFill(0.9f);
			table.addCell(cell);
		}
	}
	
	public ByteArrayOutputStream getUnitMemberMeritbadgeCards(UnitReservation reservation, UnitMember unitMember, CampSession campSession, PortletContext pc) throws PortalException, SystemException,
			DocumentException, IOException
	{
		ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
		Unit unit = UnitLocalServiceUtil.getUnit(reservation.getUnitId());
		List<UnitMember> unitMembers = new ArrayList<UnitMember>();
		unitMembers.add(unitMember);
		
		
		BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);

		InputStream is = new FileInputStream(new File(pc.getRealPath("/resources/HoneyScript-Light.ttf/")));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte buf[] = new byte[1024];
		while (true)
		{
			int size = is.read(buf);
			if (size < 0)
				break;
			out.write(buf, 0, size);
		}
		is.close();
		buf = out.toByteArray();

		BaseFont sf = BaseFont.createFont("Ginga.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED, true, buf, null);

		PdfReader reader = new PdfReader(pc.getRealPath("/resources/meritbadgecard.pdf"));
		Rectangle psize = reader.getPageSize(1);
		Document document = new Document(psize, 50, 50, 50, 50);
		PdfWriter writer = PdfWriter.getInstance(document, memoryStream);

		writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);

		document.open();
		PdfOutline parent = writer.getRootOutline();
		PdfContentByte cb = writer.getDirectContent();
		PdfImportedPage page1 = writer.getImportedPage(reader, 1);
		PdfImportedPage page2 = writer.getImportedPage(reader, 2);

		addUnitMeritBadgeCards(unit.getUnitNumber(), campSession, unitMembers, document, writer, parent, cb, page1, page2, bf, sf);
		
		document.close();

		return memoryStream;
	}

	public ByteArrayOutputStream getAllUnitMeritbadgeCards(CampSession campSession, PortletContext pc) throws PortalException, SystemException, DocumentException, IOException
	{
		
		List<UnitReservation> reservations = UnitReservationLocalServiceUtil.getReservationsBySession(campSession.getSessionKey());

		ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
		PdfReader reader = new PdfReader(pc.getRealPath("/resources/meritbadgecard.pdf"));
		Rectangle psize = reader.getPageSize(1);
		Document document = new Document(psize, 50, 50, 50, 50);
		PdfWriter writer = PdfWriter.getInstance(document, memoryStream);
	
		document.open();
		
		for(UnitReservation reservation: reservations)
		{
			Unit unit = UnitLocalServiceUtil.getUnit(reservation.getUnitId());
			List<UnitMember> unitMembers = UnitMemberLocalServiceUtil.getRegisteredMembers(reservation.getPrimaryKey());
			BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
	
			InputStream is = new FileInputStream(new File(pc.getRealPath("/resources/HoneyScript-Light.ttf/")));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte buf[] = new byte[1024];
			while (true)
			{
				int size = is.read(buf);
				if (size < 0)
					break;
				out.write(buf, 0, size);
			}
			is.close();
			buf = out.toByteArray();
	
			BaseFont sf = BaseFont.createFont("Ginga.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED, true, buf, null);
	
	
			writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);
	
			PdfOutline parent = writer.getRootOutline();
			PdfContentByte cb = writer.getDirectContent();
			PdfImportedPage page1 = writer.getImportedPage(reader, 1);
			PdfImportedPage page2 = writer.getImportedPage(reader, 2);
			
			PdfOutline unitBookMark = new PdfOutline(parent, new PdfDestination(PdfDestination.FITH, writer.getVerticalPosition(true)), "Unit " + unit.getUnitNumber(), true);
			unitBookMark.setStyle(Font.BOLD);
			addUnitMeritBadgeCards(unit.getUnitNumber(), campSession, unitMembers, document, writer, unitBookMark, cb, page1, page2, bf, sf);
		}
		
		document.close();
		return memoryStream;

	}

	public ByteArrayOutputStream getUnitMeritbadgeCards(UnitReservation reservation, CampSession campSession, PortletContext pc) throws PortalException, SystemException,
			DocumentException, IOException
	{
		ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
		Unit unit = UnitLocalServiceUtil.getUnit(reservation.getUnitId());
		List<UnitMember> unitMembers = UnitMemberLocalServiceUtil.getRegisteredMembers(reservation.getPrimaryKey());
		BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);

		InputStream is = new FileInputStream(new File(pc.getRealPath("/resources/HoneyScript-Light.ttf/")));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte buf[] = new byte[1024];
		while (true)
		{
			int size = is.read(buf);
			if (size < 0)
				break;
			out.write(buf, 0, size);
		}
		is.close();
		buf = out.toByteArray();

		BaseFont sf = BaseFont.createFont("Ginga.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED, true, buf, null);

		PdfReader reader = new PdfReader(pc.getRealPath("/resources/meritbadgecard.pdf"));
		Rectangle psize = reader.getPageSize(1);
		Document document = new Document(psize, 50, 50, 50, 50);
		PdfWriter writer = PdfWriter.getInstance(document, memoryStream);

		writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);

		document.open();
		PdfOutline parent = writer.getRootOutline();
		PdfContentByte cb = writer.getDirectContent();
		PdfImportedPage page1 = writer.getImportedPage(reader, 1);
		PdfImportedPage page2 = writer.getImportedPage(reader, 2);

		addUnitMeritBadgeCards(unit.getUnitNumber(), campSession, unitMembers, document, writer, parent, cb, page1, page2, bf, sf);
		
		document.close();

		return memoryStream;
	}

	private void addUnitMeritBadgeCards(int unitNumber, CampSession campSession, List<UnitMember> unitMembers, Document document, PdfWriter writer, PdfOutline parent,
			PdfContentByte cb, PdfImportedPage page1, PdfImportedPage page2, BaseFont baseFont, BaseFont signatureFont) throws DocumentException,
			SystemException, PortalException
	{
		boolean hasRecords = false;
		
		if (unitMembers.isEmpty())
		{
			document.add(new Paragraph("Empty Record: There are no registered unit members"));
		} else
		{
			for (UnitMember unitMember : unitMembers)
			{
				List<Object[]> results = EnrollmentLocalServiceUtil.getUnitMemberEnrollments(unitMember.getPrimaryKey(), campSession.getPrimaryKey());
			
				if(unitMember.getType() == 1)
				{
					hasRecords = addUnitMemberMB(unitNumber, unitMember, campSession, document, results, cb, page1, page2, baseFont, signatureFont);
					if (parent != null)
					{
						PdfOutline unitMemberBookMark = new PdfOutline(parent, new PdfDestination(PdfDestination.FITH, writer.getVerticalPosition(true)),
								unitMember.getFirstName().toUpperCase() + " " + unitMember.getLastName().toUpperCase(), true);
						unitMemberBookMark.setStyle(Font.BOLD);
					}
				}
			}
			
			if(!hasRecords)
			{
//				document.newPage();
//				document.add(new Paragraph("Empty Record: No requirements have been completed for any merit badges"));
			}
		}
	}

	private boolean addUnitMemberMB(int unitNumber, UnitMember unitMember, CampSession campSession, Document document, List<Object[]> results, PdfContentByte cb, PdfImportedPage page1,
			PdfImportedPage page2, BaseFont bf, BaseFont sf) throws PortalException, SystemException
	{
		boolean hasRecords = false;
		int cardCount = 0;
		Vector<Object> cardVector = new Vector<Object>();

		for (Object[] result : results)
		{
			Activity activity = (Activity) result[0];
			ActivitySection section = (ActivitySection) result[1];
			Enrollment enrollment = (Enrollment) result[2];

			UnitActivityRecordTableModel tableModel = new UnitActivityRecordTableModel(unitMember, enrollment.getSessionActivityId());

			if (activity.getType() == 1)
			{
				if (tableModel != null)
				{
					for (int i = 0; i < tableModel.getRowCount(); i++)
					{
						TreeMap<String, Object> cardTree = new TreeMap<String, Object>();
						Vector<Object> requirements = new Vector<Object>();
						cardTree.put("groupNumber", String.valueOf(unitNumber));
						cardTree.put("activityName", activity.getName());
						cardTree.put("memberName", (unitMember.getFirstName() + " " + unitMember.getLastName()).toUpperCase());
						cardTree.put("isComplete", tableModel.getValueAt(i, 6));
						String counselorName[] = tableModel.getSessionActivity().getCounselorsName().split(" ");
						if(counselorName.length == 2)
						{
							cardTree.put("counselorFirstName", counselorName[0]);
							cardTree.put("counselorLastName", counselorName[1]);
						}
						else
						{
							cardTree.put("counselorFirstName", "Charles");
							cardTree.put("counselorLastName", "Busenburg");
						}

						
						for (int j = 8; j < tableModel.getColumnCount(); j++)
						{
							Boolean isCompleted = (Boolean) tableModel.getValueAt(i, j);
							if (isCompleted.booleanValue())
								requirements.add(tableModel.getColumnName(j));
						}

						cardTree.put("requirements", requirements);
						if (!requirements.isEmpty())
						{
							cardVector.add(cardTree);

							if (cardCount == 2)
							{
								document.newPage();
								cb.addTemplate(page1, 0, 0);
								createMBFront(cb, cardVector, campSession, bf, sf);
								document.newPage();
								cb.addTemplate(page2, 0, 0);
								createMBBack(cb, cardVector, campSession, bf, sf);
								cardVector = new Vector<Object>();
								cardCount = -1;
								hasRecords = true;
							}
							cardCount++;
						}
					}
				}
			}
		}

		if (cardCount != 0)
		{
			document.newPage();
			cb.addTemplate(page1, 0, 0);
			createMBFront(cb, cardVector, campSession, bf, sf);
			document.newPage();
			cb.addTemplate(page2, 0, 0);
			createMBBack(cb, cardVector, campSession, bf, sf);
			cardVector = new Vector<Object>();
			cardCount = -1;
			hasRecords = true;
		}
		
		return hasRecords;
	}

	private void createMBFront(PdfContentByte cb, Vector data, CampSession campSession, BaseFont baseFont, BaseFont signatureFont)
	{
		SimpleDateFormat day = new SimpleDateFormat("dd");
		SimpleDateFormat month = new SimpleDateFormat("MM");
		SimpleDateFormat year = new SimpleDateFormat("yy");
		Date completeDate = campSession.getEndDate();

		for (int i = 0; i < data.size(); i++)
		{
			TreeMap dataMap = (TreeMap) data.get(i);
			String activityName = (String) dataMap.get("activityName");
			String groupNumber = (String) dataMap.get("groupNumber");
			String counselorsFname = (String) dataMap.get("counselorFirstName");
			String counselorsLname = (String) dataMap.get("counselorLastName");
			String counselorsName = counselorsFname + " " + counselorsLname;
			String address1 = "7599 E. Waldrip Creek Road";
			String address2 = "Bloomington, IN 47401";
			String phoneNumber = "(812) 837 - 9423";
			String memberName = (String) dataMap.get("memberName");
			String checkCharacter = "X";
			Boolean isCompleted = (Boolean) dataMap.get("isComplete");
			memberName = memberName.toUpperCase();
			String capCounselorsName = counselorsName.toUpperCase();
			

			int rowOffset = 258;
			if (i == 2)
				rowOffset = rowOffset + 3;
			rowOffset = rowOffset * i;

			cb.beginText();
			cb.setFontAndSize(baseFont, 8);
			cb.setTextMatrix(30, (739 - rowOffset));
			cb.showText(activityName);
			cb.setTextMatrix(30, 719 - rowOffset);
			cb.showText(capCounselorsName);
			cb.setTextMatrix(30, 697 - rowOffset);
			cb.showText(address1);
			cb.setTextMatrix(30, 677 - rowOffset);
			cb.showText(address2);
			cb.setTextMatrix(30, 656 - rowOffset);
			cb.showText(phoneNumber);
			cb.setFontAndSize(signatureFont, 18);
			if (isCompleted.booleanValue())
			{
				cb.setTextMatrix(30, 635 - rowOffset);
				cb.showText(counselorsName);
				cb.setFontAndSize(baseFont, 8);
				cb.setTextMatrix(141, 635 - rowOffset);
				cb.showText(month.format(completeDate));
				cb.setTextMatrix(160, 635 - rowOffset);
				cb.showText(day.format(completeDate));
				cb.setTextMatrix(179, 635 - rowOffset);
				cb.showText(year.format(completeDate));
			}
			cb.setFontAndSize(baseFont, 8);
			cb.setTextMatrix(241, 744 - rowOffset);
			cb.showText(memberName);
			cb.setTextMatrix(221, 695 - rowOffset);
			cb.showText(activityName);
			cb.setFontAndSize(signatureFont, 18);
			if (isCompleted.booleanValue())
			{
				cb.setTextMatrix(221, 638 - rowOffset);
				cb.showText(counselorsName);
				cb.setFontAndSize(baseFont, 8);
				cb.setTextMatrix(290, 662 - rowOffset);
				cb.showText(month.format(completeDate));
				cb.setTextMatrix(329, 662 - rowOffset);
				cb.showText(day.format(completeDate));
				cb.setTextMatrix(366, 662 - rowOffset);
				cb.showText(year.format(completeDate));
			}
			cb.setFontAndSize(baseFont, 8);
			cb.setTextMatrix(455, 744 - rowOffset);
			cb.showText(memberName);
			cb.setTextMatrix(424, 719 - rowOffset);
			cb.showText(checkCharacter);
			cb.setTextMatrix(515, 704 - rowOffset);
			cb.showText(groupNumber);
			cb.setTextMatrix(425, 665 - rowOffset);
			cb.showText(activityName);
			cb.setTextMatrix(499, 632 - rowOffset);
			cb.showText(month.format(completeDate));
			cb.setTextMatrix(537, 632 - rowOffset);
			cb.showText(day.format(completeDate));
			cb.setTextMatrix(576, 632 - rowOffset);
			cb.showText(year.format(completeDate));
			cb.endText();
		}
	}

	private void createMBBack(PdfContentByte cb, Vector data, CampSession campSession, BaseFont baseFont, BaseFont signatureFont)
	{
		SimpleDateFormat day = new SimpleDateFormat("dd");
		SimpleDateFormat month = new SimpleDateFormat("MM");
		SimpleDateFormat year = new SimpleDateFormat("yy");
		Date completeDate = campSession.getEndDate();

		for (int i = 0; i < data.size(); i++)
		{
			TreeMap dataMap = (TreeMap) data.get(i);
			String activityName = (String) dataMap.get("activityName");
			String groupNumber = (String) dataMap.get("groupNumber");
			String counselorsFname = (String) dataMap.get("counselorFirstName");
			String counselorsLname = (String) dataMap.get("counselorLastName");
			String memberName = (String) dataMap.get("memberName");
			String checkCharacter = "X";
			String unitType = "Troop";
			Vector requirements = (Vector) dataMap.get("requirements");

			int lineOffset = 0;
			int rowOffset = 258;
			if (i == 2)
				rowOffset = rowOffset + 3;
			rowOffset = rowOffset * i;

			cb.setRGBColorStroke(0, 0, 0);

			for (int j = 0; j < 11; j++)
			{
				if (i == 2)
				{
					cb.moveTo(243 + lineOffset, 540 - rowOffset + 4);
					cb.lineTo(243 + lineOffset, 773 - rowOffset + 4);
				} else if (i == 1)
				{
					cb.moveTo(243 + lineOffset, 540 - rowOffset);
					cb.lineTo(243 + lineOffset, 773 - rowOffset + 2);
				} else
				{
					cb.moveTo(243 + lineOffset, 540 - rowOffset);
					cb.lineTo(243 + lineOffset, 773 - rowOffset);
				}
				cb.stroke();
				lineOffset = lineOffset + 15;
				if (j == 2 || j == 9)
					lineOffset = lineOffset - 2;

			}

			lineOffset = 0;
			cb.beginText();

			for (int k = 0; k < requirements.size(); k++)
			{
				cb.setFontAndSize(baseFont, 8);
				String requirement = (String) requirements.get(k);
				int tableRowOffset = rowOffset;

				if (i == 2)
					tableRowOffset = tableRowOffset - 3;

				if (k > 21)
					cb.setTextMatrix(0, 1, -1, 0, 242 + lineOffset, 540 - tableRowOffset + 107);
				else
					cb.setTextMatrix(0, 1, -1, 0, 242 + lineOffset, 540 - tableRowOffset);

				cb.showText(requirement);

				if (k > 21)
					cb.setTextMatrix(0, 1, -1, 0, 242 + lineOffset, 540 - tableRowOffset + 163);
				else
					cb.setTextMatrix(0, 1, -1, 0, 242 + lineOffset, 540 - tableRowOffset + 42);

				cb.showText(month.format(completeDate) + "/" + day.format(completeDate) + "/" + year.format(completeDate));

				if (k > 21)
					cb.setTextMatrix(0, 1, -1, 0, 242 + lineOffset, 540 - tableRowOffset + 205);
				else
					cb.setTextMatrix(0, 1, -1, 0, 242 + lineOffset, 540 - tableRowOffset + 80);

				cb.setFontAndSize(signatureFont, 8);
				cb.showText(counselorsFname.substring(0, 1) + counselorsLname.substring(0, 1));

				lineOffset = lineOffset + 7;
				if (k == 3 || k == 25 || k == 13 || k == 35)
					lineOffset = lineOffset + 2;
				if (k == 2 || k == 24 || k == 10 || k == 32 || k == 16 || k == 38)
					lineOffset = lineOffset + 1;

				if (k == 21)
					lineOffset = 0;
			}

			cb.setFontAndSize(baseFont, 8);
			cb.setTextMatrix(450, 733 - rowOffset);
			cb.showText(memberName);
			cb.setTextMatrix(423, 684 - rowOffset);
			cb.showText(checkCharacter);
			cb.setTextMatrix(433, 673 - rowOffset);
			cb.showText(unitType);
			cb.setTextMatrix(566, 673 - rowOffset);
			cb.showText(groupNumber);
			cb.endText();
		}
	}

	public ByteArrayOutputStream getAreaEnrollmentPdf(CampSession campSession) throws PortalException, SystemException, DocumentException, IOException
	{
		
		List<UnitReservation> reservations = UnitReservationLocalServiceUtil.getReservationsBySession(campSession.getSessionKey());

		ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
//		Document document = new Document(PageSize.LETTER, 10, 10, 36, 36);
//	
//		PdfWriter writer = PdfWriter.getInstance(document, memoryStream);
//		writer.setPageEvent(headerFooter);
//		document.open();
//		
//		for(UnitReservation reservation: reservations)
//		{
//			Unit unit = UnitLocalServiceUtil.getUnit(reservation.getUnitId());
//			District district = DistrictLocalServiceUtil.getDistrict(unit.getDistrictId());
//			Council council = CouncilLocalServiceUtil.getCouncil(district.getCouncilId());
//	
//			List<UnitMember> unitMembers = UnitMemberLocalServiceUtil.getRegisteredMembers(reservation.getPrimaryKey());
//	
//			headerFooter.setCouncil(council);
//			headerFooter.setDistrict(district);
//			headerFooter.setUnit(unit);
//	
//			PdfOutline parent = writer.getRootOutline();
//			PdfOutline unitBookMark = new PdfOutline(parent, new PdfDestination(PdfDestination.FITH, writer.getVerticalPosition(true)), "Unit " + unit.getUnitNumber(), true);
//			unitBookMark.setStyle(Font.BOLD);
//			addUnitActivityRecord(unitMembers, document, unitBookMark, writer, campSession.getPrimaryKey());
//			
//			document.newPage();
//			headerFooter.resetPageNumber();
//		}
//		
//		document.close();
		return memoryStream;

	}
	
	private void createEnrollmentReportPdf(CampSession campSession, Document document)
	{
//		try
//		{
//			baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
//			com.lowagie.text.Font font = new com.lowagie.text.Font(baseFont, 8, com.lowagie.text.Font.NORMAL);
//			Document document = new Document(PageSize.LETTER, 10, 10, 36, 36);
//			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(FILE_NAME));

//			HeaderFooter footer = new HeaderFooter(new Phrase("Page: ", font), true);
//			footer.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
//			footer.setAlignment(com.lowagie.text.Rectangle.ALIGN_RIGHT);
//			document.setFooter(footer);

//			document.addTitle("Area Enrollment Schedule");

//			document.open();

//			java.util.Set sessionKeySet = sessionTree.keySet();
//			java.util.Iterator j = sessionKeySet.iterator();
//			while (j.hasNext())
//			{
//				CampSession session = (CampSession) j.next();
//				java.util.TreeMap areaTree = (java.util.TreeMap) sessionTree.get(session);
//				java.util.Set areaKeySet = areaTree.keySet();
//				java.util.Iterator n = areaKeySet.iterator();
//				while (n.hasNext())
//				{
//					String area = (String) n.next();
//					java.util.TreeMap activityTree = (java.util.TreeMap) areaTree.get(area);
//					java.util.Set activityKeySet = activityTree.keySet();
//					java.util.Iterator k = activityKeySet.iterator();
//					while (k.hasNext())
//					{
//						Activity activity = (Activity) k.next();
//						java.util.TreeSet sectionSet = (java.util.TreeSet) activityTree.get(activity);
//						java.util.Iterator l = sectionSet.iterator();
//						while (l.hasNext())
//						{
//							ActivitySection section = (ActivitySection) l.next();
//
//							ActivityRecordTableModel tableModel = new ActivityRecordTableModel(section, session, activity, true);
//							PdfPTable table = new PdfPTable(tableModel.getColumnCount());
//							table.setWidthPercentage(100f);
//							PdfPCell header = new PdfPCell(new Phrase(activity.getName() + "     " + section.toString()));
//							header.setColspan(tableModel.getColumnCount());
//							header.setGrayFill(0.9f);
//							table.addCell(header);
//							int columnWidths[] = new int[tableModel.getColumnCount()];
//							columnWidths[0] = 15;
//							for (int i = 0; i < tableModel.getColumnCount(); i++)
//							{
//								if (i != 0)
//									columnWidths[i] = 1;
//
//								PdfPCell cell = new PdfPCell(new Phrase(tableModel.getColumnName(i), font));
//								table.addCell(cell);
//							}
//
//							table.setWidths(columnWidths);
//							for (int i = 0; i < tableModel.getRowCount(); i++)
//							{
//								for (int m = 0; m < tableModel.getColumnCount(); m++)
//								{
//									if (m == 0)
//									{
//										table.addCell(new Phrase((String) tableModel.getValueAt(i, m), font));
//									} else
//										table.addCell(" ");
//								}
//							}
//							Phrase area_header = new Phrase(area, font);
//							Paragraph top = new Paragraph(area_header);
//							top.setAlignment(Paragraph.ALIGN_RIGHT);
//							document.add(top);
//							document.add(table);
//							document.newPage();
//						}
//					}
//				}
//			}
//			document.close();
//		} catch (Exception exception)
//		{
//			exception.printStackTrace();
//		}
	}


	class HeaderFooter extends PdfPageEventHelper
	{
		District	district	= null;
		Council		council		= null;
		Unit		unit		= null;
		int			pagenumber	= 0;
		Phrase		header		= new Phrase();

		public District getDistrict()
		{
			return district;
		}

		public void setDistrict(District district)
		{
			this.district = district;
		}

		public Council getCouncil()
		{
			return council;
		}

		public void setCouncil(Council council)
		{
			this.council = council;
		}

		public Unit getUnit()
		{
			return unit;
		}

		public void setUnit(Unit unit)
		{
			this.unit = unit;
		}

		public int getPagenumber()
		{
			return pagenumber;
		}

		public void setPagenumber(int pagenumber)
		{
			this.pagenumber = pagenumber;
		}

		public void addHeader(Phrase header)
		{
			this.header = header;
		}

		public void resetPageNumber()
		{
			pagenumber = 1;
		}

		public void onStartPage(PdfWriter writer, Document document)
		{
			pagenumber++;
		}

		public void onEndPage(PdfWriter writer, Document document)
		{
			if (district.getAbbreviation().equals("OC"))
				header = new Phrase(district.getAbbreviation() + council.getAbbreviation() + unit.getUnitNumber(), headerFont);
			else
				header = new Phrase(district.getAbbreviation() + unit.getUnitNumber(), headerFont);

			Rectangle rect = writer.getPageSize();

			// Header
			ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, header, rect.getRight() - 5, rect.getTop() - 10, 0);

			// Footer
			ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase(String.format("page %d", pagenumber)),
					(rect.getLeft() + rect.getRight()) / 2, rect.getBottom() + 6, 0);
		
		}
	}
}

