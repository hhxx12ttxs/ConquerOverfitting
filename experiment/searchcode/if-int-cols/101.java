package com.jpsycn.print.util;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;

public class ItextUtil {

	public static String checked(String s1, String s2) {
		if (s1.equals(s2)) {
			return "√" + s1;
		} else {
			return "□" + s1;
		}
	}

	/**
	 * 复选框
	 * 
	 * @param str
	 * @param checked
	 * @return
	 */
	public static String checked(String[] str, String checked) {
		StringBuilder sb = new StringBuilder();
		for (String s : str) {
			if (s.equals(checked)) {
				sb.append("√").append(s);
			} else {
				sb.append("□").append(s);
			}
			sb.append(" ");
		}
		return sb.toString();
	}

	/**
	 * 构造备注单元格 没有没有备注内容，备注单元格也要保存一定的高度，这里使用N个白色的“正”字来实现空格的效果
	 * 
	 * @param font
	 * @param blackFont
	 * @param content
	 * @param colspan
	 * @param blackspace
	 *            空格数
	 * @return
	 */
	public static PdfPCell getRemarkCell(Font font, Font blackFont,
			String content,Font bf,String con, int cols, int blackspace) {
		Phrase phrase = new Phrase();
		Chunk c1 = new Chunk(content, font);
		phrase.add(c1);
		Chunk c2 = new Chunk(con, bf);
		phrase.add(c2);
		Chunk c3 = new Chunk(getBlackStr(blackspace), blackFont);
		phrase.add(c3);
		Paragraph p = new Paragraph(phrase);
		PdfPCell cell = new PdfPCell(p);
		cell.setColspan(cols);
		return cell;
	}
	public static PdfPCell getCell2(Font font1,String content1,Font font2,String content2,int cols) {
		Phrase phrase = new Phrase();
		Chunk c1 = new Chunk(content1, font1);
		phrase.add(c1);
		
		Chunk c2 = new Chunk(content2, font2);
		phrase.add(c2);
		
		
		Paragraph p = new Paragraph(phrase);
		
		return getCell(p, cols, 2, false,22f);
	}

	public static PdfPCell getMultiCell3(Font font, Font blackFont,
			String contextPrefix, String contextMiddle, String contextSuffix,
			int cols, int blackspace, int blackspace2) {
		Phrase phrase = new Phrase();
		Chunk c1 = new Chunk(contextPrefix, font);
		phrase.add(c1);
		Chunk c2 = new Chunk(getBlackStr(blackspace), blackFont);
		phrase.add(c2);
		Chunk c3 = new Chunk(contextMiddle, font);
		phrase.add(c3);
		Chunk c4 = new Chunk(getBlackStr(blackspace2), blackFont);
		phrase.add(c4);
		Chunk c5 = new Chunk(contextSuffix, font);
		phrase.add(c5);
		Paragraph p = new Paragraph(phrase);
		PdfPCell cell = new PdfPCell(p);
		cell.setColspan(cols);
		return cell;
	}

	public static PdfPCell getMultiCell2(Font font, Font blackFont,
			String contextPrefix, String contextSuffix, int cols, int blackspace) {
		Phrase phrase = new Phrase();

		Chunk c1 = new Chunk(contextPrefix, font);
		phrase.add(c1);

		Chunk c2 = new Chunk(getBlackStr(blackspace), blackFont);
		phrase.add(c2);

		Chunk c3 = new Chunk(contextSuffix, font);
		phrase.add(c3);

		Paragraph p = new Paragraph(phrase);
		PdfPCell cell = new PdfPCell(p);
		cell.setColspan(cols);
		return cell;
	}

	/**
	 * 构造单元格，因为使用汉字，所以需要指定字体
	 * 
	 * @param font
	 * @param content
	 * @param cols
	 *            单元格的宽度
	 * @param rows
	 *            单元格的高度
	 * @return
	 */

	/**
	 * 得到n个正字，通过把正设置为白色，即可实现空格的效果
	 * 
	 * @param num
	 * @return
	 */
	public static String getBlackStr(int num) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < num; i++) {
			sb.append("一");
		}
		return sb.toString();
	}

	public static PdfPCell getCell(Font font, String content, int cols) {
		Paragraph p = new Paragraph(content, font);
		return getCell(p, cols, 1, true,22f);
	}
	public static PdfPCell getCell(float height,Font font, String content, int cols) {
		Paragraph p = new Paragraph(content, font);
		return getCell(p, cols, 1, true,height);
	}

	public static PdfPCell getCell(Font font, String content, int cols,
			boolean fixedHeight) {
		Paragraph p = new Paragraph(content, font);
		return getCell(p, cols, 1, fixedHeight,22f);
	}

	public static PdfPCell getCell(float height,Font font, String content, int cols, int rows) {
		Paragraph p = new Paragraph(content, font);
		return getCell(p, cols, rows, true,height);
	}
	public static PdfPCell getCell(Font font, String content, int cols, int rows) {
		Paragraph p = new Paragraph(content, font);
		return getCell(p, cols, rows, true,22f);
	}

	public static PdfPCell getCell(Font font, String content, int cols,
			int rows, boolean fixedHeight) {
		Paragraph p = new Paragraph(content, font);
		return getCell(p, cols, rows, fixedHeight,22f);
	}

	private static PdfPCell getCell(Paragraph p, int cols, int rows,
			boolean fixedHeight,float height) {
		PdfPCell cell = new PdfPCell(p);
		cell.setColspan(cols);
		cell.setRowspan(rows);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);// 垂直
		if (fixedHeight) {
			cell.setFixedHeight(height);
		}
		return cell;
	}

	private static PdfPCell getCell(Paragraph p, int cols, int rows,
			boolean fixedHeight, boolean v,float height) {
		PdfPCell cell = new PdfPCell(p);
		cell.setColspan(cols);
		cell.setRowspan(rows);
		if (v) {
			cell.setVerticalAlignment(Element.ALIGN_CENTER);
		}
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);// 垂直
		if (fixedHeight) {
			cell.setFixedHeight(height);
		}
		return cell;
	}

	public static PdfPCell getCell(Font font, String content, int cols,
			int rows, boolean fixedHeight, boolean v) {
		Paragraph p = new Paragraph(content, font);
		return getCell(p, cols, rows, fixedHeight, v,22f);
	}
	public static PdfPCell getCell(float height,Font font, String content, int cols,
			int rows, boolean fixedHeight, boolean v) {
		Paragraph p = new Paragraph(content, font);
		return getCell(p, cols, rows, fixedHeight, v,height);
	}
}

