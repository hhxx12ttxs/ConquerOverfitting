package com.jpsycn.print.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFUtils {

	private static final String TAG = "PDFUtils";

	/**
	 * 创建河南省重点工业产品风险监测采样单
	 * 
	 * @param mContext
	 * @param file
	 *            生成pdf文件路径
	 * @param map
	 *            需要向表格中填入的数据
	 * @return
	 */
	public static boolean createSamplingPdf(Context mContext, File file,
			Map<String, String> map) {
		try {

			Font simfang12 = FontUtil.getFont(mContext, 12, "simfang.ttf");
			Font bf = FontUtil.getFont(mContext, 12, "simfang.ttf", Font.BOLD,
					null);

			Document document = setHeader(file, mContext, simfang12, bf,
					"河南省重点工业产品风险监测采样单",
					map.get("sno") == null ? "   " : map.get("sno"));

			// 水平方向，把表格分为100份
			int cols = 100;
			// 经过对表格的分析，在水平方向上，表格可以分为5段
			// 经过多次测试，分别计算出5段的值
			// 根据这5段的值，便可以构造出整个表格
			int m = 7;
			int n = 28;
			int o = 27;
			int p = 10;
			int q = 28;

			PdfPTable table1 = new PdfPTable(cols);
			// 表格默认占页面可编辑空间的80%，这里设置为100%
			table1.setWidthPercentage(100);
			table1.setSpacingBefore(3f);

			table1.addCell(ItextUtil.getCell(simfang12, "样品类型", m + n));
			table1.addCell(ItextUtil.getCell(
					simfang12,
					ItextUtil.checked(new String[] { "原料", "辅料", "半成品", "待检成品",
							"成品", "其他" }, map.get("sample_type")), o + p + q));

			table1.addCell(ItextUtil.getCell(simfang12, "采样地点", m + n));

			table1.addCell(ItextUtil.getCell(
					simfang12,
					ItextUtil.checked(new String[] { "原辅料库所", "生产线", "半成品库",
							"待检区", "成品库", "超市", "经销店", "商场", "其他" },
							map.get("sampling_address")), o + p + q, false));

			table1.addCell(ItextUtil.getCell(simfang12, "采样地点", m, 3));

			table1.addCell(ItextUtil.getCell(simfang12, "采样地点（或企业）名称", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("name"), o + p + q));
			table1.addCell(ItextUtil.getCell(simfang12, "采样地点（或企业）地址", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("address"), o + p + q));
			table1.addCell(ItextUtil.getCell(simfang12, "联系人及电话", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("contact_and_phone"),
					o + p + q));

			table1.addCell(ItextUtil.getCell(simfang12, "产品信息", m, 8));

			table1.addCell(ItextUtil.getCell(simfang12, "产品名称", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("product_name"), o + p
					+ q));

			table1.addCell(ItextUtil.getCell(simfang12, "规格型号", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("product_type"), o));
			table1.addCell(ItextUtil.getCell(simfang12, "产品等级", p));
			table1.addCell(ItextUtil.getCell(bf, map.get("product_level"), q));

			table1.addCell(ItextUtil.getCell(simfang12, "商标", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("product_brand"), o));
			table1.addCell(ItextUtil.getCell(simfang12, "执行标准", p));
			table1.addCell(ItextUtil.getCell(bf, map.get("product_standard"), q));

			table1.addCell(ItextUtil.getCell(simfang12, "生产许可证编号", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("product_sno"), o + p
					+ q));

			table1.addCell(ItextUtil.getCell(simfang12, "生产日期/批号", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("product_date"), o + p
					+ q));

			table1.addCell(ItextUtil.getCell(simfang12, "保质期", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("product_expired"), o
					+ p + q));
			table1.addCell(ItextUtil.getCell(simfang12, "采样数量", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("number"), o + p + q));
			table1.addCell(ItextUtil.getCell(simfang12, "采样日期", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("date"), o + p + q));

			table1.addCell(ItextUtil.getCell(
					simfang12,
					"生产企业是否同采样地点："
							+ ItextUtil.checked(new String[] { "是", "否" },
									map.get("company_equal_sampling_ground"))
							+ "　（如果是，以下生产企业信息可不填）", cols));

			table1.addCell(ItextUtil.getCell(simfang12, "生产企业", m, 3));

			table1.addCell(ItextUtil.getCell(simfang12, "企业名称", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("company_name"), o + p
					+ q));
			table1.addCell(ItextUtil.getCell(simfang12, "企业地址", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("company_address"), o
					+ p + q));
			table1.addCell(ItextUtil.getCell(simfang12, "联系人及电话", n));
			table1.addCell(ItextUtil.getCell(bf,
					map.get("company_linkman_and_phone"), o + p + q));

			table1.addCell(ItextUtil.getCell(simfang12, "采样机构", m, 4));

			table1.addCell(ItextUtil.getCell(simfang12, "机构名称", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("depart_name"), o + p
					+ q));
			table1.addCell(ItextUtil.getCell(simfang12, "联系人", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("depart_people"), o
					+ p + q));
			table1.addCell(ItextUtil.getCell(simfang12, "联系电话", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("depart_phone"), o + p
					+ q));
			table1.addCell(ItextUtil.getCell(simfang12, "传真/Email", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("depart_email"), o + p
					+ q));

			// 白色字体，为了生成空格
			Font blackFont = FontUtil.getFont(mContext, 12, "simfang.ttf",
					Font.NORMAL, BaseColor.WHITE);

			String remark = map.get("remark") == null ? "" : map.get("remark");
			int nn = 25 + 43 * 6;
			if (remark != null && remark.length() < 25 + 43 * 4) {
				nn = nn - remark.length();
			}

			table1.addCell(ItextUtil.getRemarkCell(simfang12, blackFont,
					"备注（需要说明其他问题）：", bf, remark, cols, nn));

			table1.addCell(ItextUtil.getMultiCell3(simfang12, blackFont,
					"受检企业盖章：", "（买样不用盖章）", "年  月  日", 35, 7, 6 + 14 * 4 + 14));
			table1.addCell(ItextUtil.getMultiCell2(simfang12, blackFont,
					"采样机构（盖章）：", "年  月  日", 33, 5 + 14 * 5 + 9));
			table1.addCell(ItextUtil.getMultiCell2(simfang12, blackFont,
					"采样人（签名）：", "年  月  日", 32, 5 + 13 * 5 + 8));
			document.add(table1);

			Font simfang8 = FontUtil.getFont(mContext, 8, "simfang.ttf");
			Paragraph r1 = new Paragraph(
					"注:1.采集原料样品时,“采样地点”填写生产企业信息,“生产企业”填写原料的生产企业信息。", simfang8);
			document.add(r1);
			Paragraph r2 = new Paragraph("2.采样人须各自签名。3.市场买样需要附购物小票、发票。",
					simfang8);
			document.add(r2);

			zxing(map.get("zxing"), mContext, document);

			stmp(mContext, document,220, 40);
			
			Paragraph bbb = new Paragraph(ItextUtil.getBlackStr(88), blackFont);
			document.add(bbb);
			// 关闭文档
			document.close();
			return true;
		} catch (Exception e) {
			Log.e(TAG, "", e);
			return false;
		}
	}

	private static Document setHeader(File file, Context mContext,
			Font simfang12, Font simfangBlod12, String title, String sno)
			throws DocumentException, IOException {
		Document document = new Document(PageSize.A4, 30, 30, 20, 0);

		PdfWriter.getInstance(document, new FileOutputStream(file));
		document.open();

		// 标题
		Font simhei18 = FontUtil.getFont(mContext, 18, "simhei.ttf");
		Paragraph b = new Paragraph(title, simhei18);
		b.setAlignment(Element.ALIGN_CENTER);
		document.add(b);
		// 编号

		Chunk m1 = new Chunk("编号：", simfang12);
		Chunk m2 = new Chunk(sno, simfangBlod12);
		Paragraph p2 = new Paragraph();
		p2.add(m1);
		p2.add(m2);
		p2.setAlignment(Element.ALIGN_RIGHT);
		document.add(p2);
		return document;
	}

	/**
	 * 产品质量监督检查/复查抽样单
	 * 
	 * @param mContext
	 * @param file
	 * @param map
	 * @return
	 */
	public static boolean createReSamplePdf(Context mContext, File file,
			Map<String, String> map) {
		try {

			Font simfang12 = FontUtil.getFont(mContext, 12, "simfang.ttf");
			Font bf = FontUtil.getFont(mContext, 12, "simfang.ttf", Font.BOLD,
					null);

			Document document = setHeader(file, mContext, simfang12, bf,
					"产品质量监督检查/复查抽样单",
					map.get("sno") == null ? "   " : map.get("sno"));

			// 创建表格
			int cols = 100;
			int m = 5;
			int n = 12;
			int o1 = 11;
			int o2 = 11;
			int o3 = 11;
			int p = 5;
			int q = 5;
			int r = 20;
			int s = 20;

			int o = o1 + o2 + o3;

			PdfPTable table1 = new PdfPTable(cols);
			// 表格默认占页面可编辑空间的80%，这里设置为100%
			table1.setWidthPercentage(100);
			table1.setSpacingBefore(3f);

			table1.addCell(ItextUtil.getCell(simfang12, "任务来源", m + n));
			table1.addCell(ItextUtil.getCell(bf, map.get("source"), o));
			table1.addCell(ItextUtil.getCell(simfang12, "检查类别", p + q));
			table1.addCell(ItextUtil.getCell(bf, map.get("check_type"), r + s));

			table1.addCell(ItextUtil.getCell(simfang12, "受检单位", m, 3, false,
					true));
			table1.addCell(ItextUtil.getCell(simfang12, " 名称及  通讯地址", n, 3,
					false, true));

			table1.addCell(ItextUtil.getCell(bf, map.get("name"), o));
			table1.addCell(ItextUtil.getCell(simfang12, "法人代表", p + q));
			table1.addCell(ItextUtil.getCell(bf,
					map.get("legal_representative"), r + s));
			table1.addCell(ItextUtil.getCell(bf, map.get("address"), o));
			table1.addCell(ItextUtil.getCell(simfang12, "联系人    及电话", p + q, 2,
					false, true));
			table1.addCell(ItextUtil.getCell(bf, map.get("contact_and_phone"),
					r + s, 2));
			table1.addCell(ItextUtil.getCell(simfang12, "", o));

			table1.addCell(ItextUtil.getCell(simfang12, "生产单位", m, 10, false,
					true));
			table1.addCell(ItextUtil.getCell(simfang12, "单位名称", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("company_name"), o));

			table1.addCell(ItextUtil.getCell(simfang12, "经济类型", p, 10, false,
					true));

			String companyType = map.get("company_type");

			table1.addCell(ItextUtil
					.getCell(simfang12, "内资", q, 4, false, true));
			table1.addCell(ItextUtil.getCell(simfang12,
					ItextUtil.checked("国有", companyType), r));
			table1.addCell(ItextUtil.getCell(simfang12,
					ItextUtil.checked("私营（含个体）", companyType), s));

			table1.addCell(ItextUtil.getCell(simfang12, "单位地址", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("company_address"), o));

			table1.addCell(ItextUtil.getCell(simfang12,
					ItextUtil.checked("集体", companyType), r));
			table1.addCell(ItextUtil.getCell(simfang12,
					ItextUtil.checked("有限公司", companyType), s));

			table1.addCell(ItextUtil.getCell(simfang12, "邮政编码", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("company_zip"), o));

			table1.addCell(ItextUtil.getCell(simfang12,
					ItextUtil.checked("联营", companyType), r));
			table1.addCell(ItextUtil.getCell(simfang12,
					ItextUtil.checked("股份有限公司", companyType), s));

			table1.addCell(ItextUtil.getCell(simfang12, "法人代表", n));
			table1.addCell(ItextUtil.getCell(bf,
					map.get("company_legal_representative"), o));

			table1.addCell(ItextUtil.getCell(simfang12,
					ItextUtil.checked("股份合作", companyType), r));
			table1.addCell(ItextUtil.getCell(simfang12,
					ItextUtil.checked("其他企业", companyType), s));

			table1.addCell(ItextUtil.getCell(simfang12, "联系人", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("company_linkman"), o));

			table1.addCell(ItextUtil.getCell(simfang12, "港澳台", q, 3, false,
					true));

			table1.addCell(ItextUtil.getCell(simfang12,
					ItextUtil.checked("合资经营", companyType), r));
			table1.addCell(ItextUtil.getCell(simfang12,
					ItextUtil.checked("合作经营", companyType), s));

			table1.addCell(ItextUtil.getCell(simfang12, "联系电话", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("company_phone"), o));

			table1.addCell(ItextUtil.getCell(simfang12,
					ItextUtil.checked("港澳台独资经营", companyType), r, 2));
			table1.addCell(ItextUtil.getCell(simfang12,
					ItextUtil.checked("港澳投资股份有限公司", companyType), s, 2));

			table1.addCell(ItextUtil.getCell(simfang12, "营业执照", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("company_license"), o));
			table1.addCell(ItextUtil.getCell(simfang12, "机构代码", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("company_code"), o));

			table1.addCell(ItextUtil
					.getCell(simfang12, "外资", q, 3, false, true));
			table1.addCell(ItextUtil.getCell(simfang12,
					ItextUtil.checked("中外合资", companyType), r));
			table1.addCell(ItextUtil.getCell(simfang12,
					ItextUtil.checked("中外合作", companyType), s));

			table1.addCell(ItextUtil.getCell(simfang12, "企业规模", n, 2));

			table1.addCell(ItextUtil.getCell(simfang12, "人数", o1, false));

			table1.addCell(ItextUtil.getCell(simfang12, "产值", o2));
			table1.addCell(ItextUtil.getCell(simfang12, "产量", o3));

			table1.addCell(ItextUtil.getCell(simfang12,
					ItextUtil.checked("外资企业", companyType), r, 2));
			table1.addCell(ItextUtil.getCell(simfang12,
					ItextUtil.checked("外商投资股份有限公司", companyType), s, 2));

			table1.addCell(ItextUtil.getCell(bf,
					map.get("company_person_number"), o1));
			table1.addCell(ItextUtil.getCell(bf,
					map.get("company_output_value"), o2));
			table1.addCell(ItextUtil.getCell(bf, map.get("company_production"),
					o3));

			table1.addCell(ItextUtil.getCell(simfang12, "受检产品信息", m, 8, false,
					true));

			table1.addCell(ItextUtil.getCell(
					simfang12,
					ItextUtil.checked(new String[] { "工业产品生产许可证", "QS", "CCC",
							"其他" }, map.get("product_certificate_type")), n
							+ o1 + o2 + o3 + p + q));
			table1.addCell(ItextUtil.getCell(simfang12, "证书编号", r));
			table1.addCell(ItextUtil.getCell(bf,
					map.get("product_certificate_sno"), s));

			table1.addCell(ItextUtil.getCell(simfang12, "产品名称", n + o1));
			table1.addCell(ItextUtil.getCell(bf, map.get("product_name"), o2
					+ o3));

			table1.addCell(ItextUtil.getCell(simfang12, "规格型号", p + q + r));
			table1.addCell(ItextUtil.getCell(bf, map.get("product_type"), s));

			table1.addCell(ItextUtil.getCell(simfang12, "生产日期/批号", n + o1));
			table1.addCell(ItextUtil.getCell(bf, map.get("product_date"), o2
					+ o3));

			table1.addCell(ItextUtil.getCell(simfang12, "商标", p + q + r));
			table1.addCell(ItextUtil.getCell(bf, map.get("product_brand"), s));

			//
			gg(map, simfang12, bf, table1);

			//
			table1.addCell(ItextUtil.getCell(simfang12, "抽样日期", n + o1));
			table1.addCell(ItextUtil.getCell(bf, map.get("samplint_date"), o2
					+ o3));

			table1.addCell(ItextUtil.getCell(simfang12, "封样状态", p + q + r));
			table1.addCell(ItextUtil.getCell(bf, map.get("sampling_state"), s));

			table1.addCell(ItextUtil.getCell(simfang12, "备样量及封存地点", n + o1));
			table1.addCell(ItextUtil.getCell(bf,
					map.get("samplint_volumn_and_storage_address"), o2 + o3));

			table1.addCell(ItextUtil.getCell(simfang12, "寄送样地点", p + q + r));
			table1.addCell(ItextUtil.getCell(bf,
					map.get("sampling_send_address"), s));

			table1.addCell(ItextUtil.getCell(simfang12, "是否为出口产品", n + o1));
			table1.addCell(ItextUtil.getCell(
					simfang12,
					ItextUtil.checked(new String[] { "是", "否" },
							map.get("is_export_product")), o2 + o3));

			table1.addCell(ItextUtil.getCell(simfang12, "寄送样截止日期", p + q + r));
			table1.addCell(ItextUtil.getCell(bf,
					map.get("sampling_send_expired"), s));

			table1.addCell(ItextUtil.getCell(simfang12, "抽样单位", m, 3, false,
					true));

			table1.addCell(ItextUtil.getCell(simfang12, "单位名称", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("sampling_name"), o1
					+ o2 + o3 + p + q));
			table1.addCell(ItextUtil.getCell(simfang12, "联系人", r));
			table1.addCell(ItextUtil.getCell(bf, map.get("sampling_people"), s));

			table1.addCell(ItextUtil.getCell(simfang12, "单位地址", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("sampling_address"),
					o1 + o2 + o3 + p + q));
			table1.addCell(ItextUtil.getCell(simfang12, "联系电话", r));
			table1.addCell(ItextUtil.getCell(bf, map.get("sampling_phone"), s));

			table1.addCell(ItextUtil.getCell(simfang12, "邮政编码", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("sampling_zip"), o1
					+ o2 + o3 + p + q));
			table1.addCell(ItextUtil.getCell(simfang12, "传真/Email", r));
			table1.addCell(ItextUtil.getCell(bf, map.get("sampling_email"), s));

			Font blackFont = FontUtil.getFont(mContext, 12, "simfang.ttf",
					Font.NORMAL, BaseColor.WHITE);

			String remark = map.get("remark") == null ? "" : map.get("remark");
			int nn = 30 + 43 * 3;
			if (remark != null && remark.length() < 25 + 43 * 4) {
				nn = nn - remark.length();
			}

			table1.addCell(ItextUtil.getRemarkCell(simfang12, blackFont,
					"备注（需要说明其他问题）：", bf, remark, cols, nn));

			table1.addCell(ItextUtil.getMultiCell3(simfang12, blackFont,
					"受检单位对上述内容无异议", "受检单位签名（盖章）：", "年  月  日", 32, 1,
					2 + 13 * 2 + 6));
			table1.addCell(ItextUtil.getMultiCell3(simfang12, blackFont,
					"生产单位对上述内容无异议", "生产单位签名（盖章）：", "年  月  日", 32, 1,
					2 + 13 * 2 + 6));
			table1.addCell(ItextUtil.getMultiCell3(simfang12, blackFont,
					"抽样人（签名）：", "抽样单位（盖章）", "年  月  日", 36, 7 + 15 * 2 + 6, 9));

			document.add(table1);

			Font simfang8 = FontUtil.getFont(mContext, 8, "simfang.ttf");
			Paragraph r1 = new Paragraph(
					"注：1.技术文件指执行标准外的图纸、技术合同、产品说明书等有关产品技术的文件。", simfang8);
			document.add(r1);
			Paragraph r2 = new Paragraph(
					"2.选择许可证、QS、CCC等类别后，填写相应证书编号。3.从生产单位抽样受检单位栏不填。", simfang8);
			document.add(r2);

			
			zxing(map.get("zxing"), mContext, document);
			stmp(mContext, document,380, 40);
			Paragraph bbb = new Paragraph(ItextUtil.getBlackStr(44), blackFont);
			document.add(bbb);
			// 步骤 5:关闭文档
			document.close();
			return true;
		} catch (Exception e) {
			Log.e(TAG, "", e);
			return false;
		}
	}

	private static void stmp(Context mContext, Document document,int x,int y)
			throws MalformedURLException, IOException, DocumentException {

		Resources resources = mContext.getResources();
		int identifier = resources.getIdentifier("stamp", "drawable", mContext.getPackageName());
		Bitmap bitmap = BitmapFactory.decodeResource(resources,identifier);
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		Image image = Image.getInstance(stream.toByteArray());
		
		Chunk c1 = new Chunk(image, x,y, false);
		Paragraph pp = new Paragraph();
		pp.add(c1);
		document.add(pp);
	}

	private static void zxing(String zxing, Context mContext, Document document)
			throws MalformedURLException, IOException, DocumentException {
		
		if (!TextUtils.isEmpty(zxing)) {
			Bitmap bitmap = BarcodeCreater.creatBarcode(mContext, zxing, 100,
					20, false);

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			Image image = Image.getInstance(stream.toByteArray());

			Chunk c1 = new Chunk(image, 0, 0, false);
			float width = image.getWidth();
			Chunk c2 = new Chunk(image, 595 - 30 * 2 - width * 2, 0, false);
			Paragraph pp = new Paragraph();
			pp.setLeading(30f);
			pp.add(c1);
			pp.add(c2);
			document.add(pp);
		}
	}

	/**
	 * 食品等产品生产质量安全监督抽查/复查抽样单
	 * 
	 * @param mContext
	 * @param file
	 * @param map
	 * @return
	 */
	public static boolean createSpaqPdf(Context mContext, File file,
			Map<String, String> map) {
		try {

			/*
			 * String[] ss = new String[] { "一",".", "抽", "样", "单", "位", "（",
			 * "白", "）", "二",".", "生", "产", "单", "位", "（", "红", "）", "三",".",
			 * "组", "织", "抽", "查", "部", "门", "（", "绿", "）", "四", ".","负", "责",
			 * "监", "督", "抽", "查", "后", "处", "理", "的", "质", "量", "技", "术", "监",
			 * "督", "部", "门", "（", "黄", "）" };
			 */
			/*
			 * PdfContentByte cb = writer.getDirectContent(); BaseFont simfang =
			 * FontUtil.getBaseFont(mContext, "simfang.ttf"); cb.beginText();
			 * cb.setFontAndSize(simfang, 12); float w = 595 - 30; for (int i =
			 * 0; i < ss.length; i++) { String temp = ss[i]; if
			 * (temp.equals("（") || temp.equals("）")) {
			 * cb.showTextAligned(PdfContentByte.ALIGN_CENTER, temp, w+2,
			 * (680f-i*10)+3, 270); } else { cb.setTextMatrix(w, 680f - i * 10);
			 * cb.showText(temp); } } cb.endText();
			 */

			Font simfang12 = FontUtil.getFont(mContext, 12, "simfang.ttf");

			Font bf = FontUtil.getFont(mContext, 12, "simfang.ttf", Font.BOLD,
					null);

			Document document = setHeader(file, mContext, simfang12, bf,
					"食品等产品生产质量安全监督抽查/复查抽样单", map.get("sno") == null ? "   "
							: map.get("sno"));

			// 创建表格
			int cols = 100;

			int m = 5;
			int n = 12;
			int o1 = 11;
			int o2 = 11;
			int o3 = 11;
			int p = 5;
			int q = 5;
			int r = 20;
			int s = 20;

			int o = o1 + o2 + o3;

			PdfPTable table1 = new PdfPTable(cols);
			// 表格默认占页面可编辑空间的80%，这里设置为100%
			table1.setWidthPercentage(100);
			table1.setSpacingBefore(3f);

			table1.addCell(ItextUtil.getCell(20f, simfang12, "任务来源", m + n));
			table1.addCell(ItextUtil.getCell(20f, bf, map.get("source"), o));
			table1.addCell(ItextUtil.getCell(20f, simfang12, "检查类别", p + q));
			table1.addCell(ItextUtil.getCell(20f, bf, map.get("check_type"), r
					+ s));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "受检单位", m, 3,
					false, true));
			table1.addCell(ItextUtil.getCell(20f, simfang12, "名称及通讯地址", n, 3));

			table1.addCell(ItextUtil.getCell(20f, bf, map.get("name"), o));
			table1.addCell(ItextUtil.getCell(20f, simfang12, "法人代表", p + q));
			table1.addCell(ItextUtil.getCell(20f, bf,
					map.get("legal_representative"), r + s));
			table1.addCell(ItextUtil.getCell(20f, bf, map.get("address"), o));
			table1.addCell(ItextUtil
					.getCell(20f, simfang12, "联系人及电话", p + q, 2));
			table1.addCell(ItextUtil.getCell(20f, bf,
					map.get("contact_and_phone"), r + s, 2));
			table1.addCell(ItextUtil.getCell(20f, simfang12, "", o));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "生产单位", m, 10,
					false, true));
			table1.addCell(ItextUtil.getCell(20f, simfang12, "单位名称", n));
			table1.addCell(ItextUtil.getCell(20f, bf, map.get("company_name"),
					o));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "经济类型", p, 10,
					false, true));

			String companyType = map.get("company_type");

			table1.addCell(ItextUtil
					.getCell(simfang12, "内资", q, 4, false, true));
			table1.addCell(ItextUtil.getCell(20f, simfang12,
					ItextUtil.checked("国有", companyType), r));
			table1.addCell(ItextUtil.getCell(20f, simfang12,
					ItextUtil.checked("私营（含个体）", companyType), s));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "单位地址", n));
			table1.addCell(ItextUtil.getCell(20f, bf,
					map.get("company_address"), o));

			table1.addCell(ItextUtil.getCell(20f, simfang12,
					ItextUtil.checked("集体", companyType), r));
			table1.addCell(ItextUtil.getCell(20f, simfang12,
					ItextUtil.checked("有限公司", companyType), s));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "邮政编码", n));
			table1.addCell(ItextUtil.getCell(20f, bf, map.get("company_zip"), o));

			table1.addCell(ItextUtil.getCell(20f, simfang12,
					ItextUtil.checked("联营", companyType), r));
			table1.addCell(ItextUtil.getCell(20f, simfang12,
					ItextUtil.checked("股份有限公司", companyType), s));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "法人代表", n));
			table1.addCell(ItextUtil.getCell(20f, bf,
					map.get("company_legal_representative"), o));

			table1.addCell(ItextUtil.getCell(20f, simfang12,
					ItextUtil.checked("股份合作", companyType), r));
			table1.addCell(ItextUtil.getCell(20f, simfang12,
					ItextUtil.checked("其他企业", companyType), s));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "联系人", n));
			table1.addCell(ItextUtil.getCell(20f, bf,
					map.get("company_linkman"), o));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "港澳台", q, 3,
					false, true));

			table1.addCell(ItextUtil.getCell(20f, simfang12,
					ItextUtil.checked("合资经营", companyType), r));
			table1.addCell(ItextUtil.getCell(20f, simfang12,
					ItextUtil.checked("合作经营", companyType), s));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "联系电话", n));
			table1.addCell(ItextUtil.getCell(20f, bf, map.get("company_phone"),
					o));

			table1.addCell(ItextUtil.getCell(20f, simfang12,
					ItextUtil.checked("港澳台独资经营", companyType), r, 2));
			table1.addCell(ItextUtil.getCell(20f, simfang12,
					ItextUtil.checked("港澳投资股份有限公司", companyType), s, 2));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "营业执照", n));
			table1.addCell(ItextUtil.getCell(20f, bf,
					map.get("company_license"), o));
			table1.addCell(ItextUtil.getCell(20f, simfang12, "机构代码", n));
			table1.addCell(ItextUtil.getCell(20f, bf, map.get("company_code"),
					o));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "外资", q, 3));
			table1.addCell(ItextUtil.getCell(20f, simfang12,
					ItextUtil.checked("中外合资", companyType), r));
			table1.addCell(ItextUtil.getCell(20f, simfang12,
					ItextUtil.checked("中外合作", companyType), s));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "企业规模", n, 2));

			StringBuilder sb3 = new StringBuilder();
			sb3.append("人数：");
			sb3.append(map.get("company_person_number") == null ? " " : map
					.get("company_person_number"));
			sb3.append(" 产值：");
			sb3.append(map.get("company_output_value") == null ? " " : map
					.get("company_output_value"));
			sb3.append(" 产量：");
			sb3.append(map.get("company_production") == null ? " " : map
					.get("company_production"));
			// sb3.append("人数：10000 产值：5000万 产量：1000吨");

			Font simfang10 = FontUtil.getFont(mContext, 9, "simfang.ttf");
			table1.addCell(ItextUtil.getCell(20f, simfang10, sb3.toString(), o1
					+ o2 + o3));

			table1.addCell(ItextUtil.getCell(20f, simfang12,
					ItextUtil.checked("外资企业", companyType), r, 2));
			table1.addCell(ItextUtil.getCell(20f, simfang12,
					ItextUtil.checked("外商投资股份有限公司", companyType), s, 2));

			table1.addCell(ItextUtil.getCell(
					20f,
					simfang12,
					ItextUtil.checked(new String[] { "大型", "中型", "小型" },
							map.get("company_scope")), o1 + o2 + o3));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "受检产品信息", m, 9,
					false, true));

			table1.addCell(ItextUtil.getCell(
					20f,
					simfang12,
					ItextUtil.checked(new String[] { "食品", "食品相关产品", "化妆品",
							"小作坊" }, map.get("food_type")), n + o1 + o2 + o3
							+ p + q));
			table1.addCell(ItextUtil.getCell(20f, simfang12, "证书编号", r));
			table1.addCell(ItextUtil.getCell(20f, bf,
					map.get("product_certificate_sno"), s));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "产品名称", n + o1));
			table1.addCell(ItextUtil.getCell(20f, bf, map.get("product_name"),
					o2 + o3));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "规格型号", p + q + r));
			table1.addCell(ItextUtil.getCell(20f, bf, map.get("product_type"),
					s));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "生产日期/批号", n + o1));
			table1.addCell(ItextUtil.getCell(20f, bf, map.get("product_date"),
					o2 + o3));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "商标", p + q + r));
			table1.addCell(ItextUtil.getCell(20f, bf, map.get("product_brand"),
					s));

			//
			gg(map, simfang12, bf, table1);
			//
			table1.addCell(ItextUtil.getCell(20f, simfang12, "抽样日期", n + o1));
			table1.addCell(ItextUtil.getCell(20f, bf, map.get("samplint_date"),
					o2 + o3));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "封样状态", p + q + r));
			table1.addCell(ItextUtil.getCell(20f, bf,
					map.get("sampling_state"), s));

			table1.addCell(ItextUtil
					.getCell(20f, simfang12, "备样量及封存地点", n + o1));
			table1.addCell(ItextUtil.getCell(20f, bf,
					map.get("samplint_volumn_and_storage_address"), o2 + o3));

			table1.addCell(ItextUtil
					.getCell(20f, simfang12, "寄送样地点", p + q + r));
			table1.addCell(ItextUtil.getCell(20f, bf,
					map.get("sampling_send_address"), s));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "寄送样截止日期", n + o1));
			table1.addCell(ItextUtil.getCell(20f, bf,
					map.get("sampling_send_expired"), o2 + o3));

			table1.addCell(ItextUtil.getCell(
					20f,
					simfang12,
					ItextUtil.checked(new String[] { "中国名牌", "省名牌" },
							map.get("is_famous_brand")), p + q + r + s));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "采样地点", n));
			table1.addCell(ItextUtil.getCell(20f, simfang12, ItextUtil.checked(
					new String[] { "企业成品库", "原料库", "生产线" },
					map.get("sample_address_1")), o1 + o2 + o3 + p + q));
			table1.addCell(ItextUtil.getCell(
					simfang12,
					ItextUtil.checked(new String[] { "城市", "城乡结合部", "农村" },
							map.get("sample_address_2")), r + s));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "抽样单位", m, 3,
					false, true));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "单位名称", n));
			table1.addCell(ItextUtil.getCell(20f, bf, map.get("sampling_name"),
					o1 + o2 + o3 + p + q));
			table1.addCell(ItextUtil.getCell(20f, simfang12, "联系人", r));
			table1.addCell(ItextUtil.getCell(20f, bf,
					map.get("sampling_people"), s));

			table1.addCell(ItextUtil.getCell(20f, simfang12, "单位地址", n));
			table1.addCell(ItextUtil.getCell(20f, bf,
					map.get("sampling_address"), o1 + o2 + o3 + p + q));
			table1.addCell(ItextUtil.getCell(simfang12, "联系电话", r));
			table1.addCell(ItextUtil.getCell(bf, map.get("sampling_phone"), s));

			table1.addCell(ItextUtil.getCell(simfang12, "邮政编码", n));
			table1.addCell(ItextUtil.getCell(bf, map.get("sampling_zip"), o1
					+ o2 + o3 + p + q));
			table1.addCell(ItextUtil.getCell(simfang12, "传真/Email", r));
			table1.addCell(ItextUtil.getCell(bf, map.get("sampling_email"), s));

			table1.addCell(ItextUtil.getCell(simfang12, "现场抽查情况", m + n, 2));

			StringBuilder sb = new StringBuilder();
			sb.append("出厂检验落实情况：");
			sb.append(ItextUtil.checked(new String[] { "检验", "未检验" },
					map.get("sampling_result_1")));
			sb.append("食品添加剂使用情况：");
			sb.append(ItextUtil.checked(new String[] { "正常", "违规" },
					map.get("sampling_result_2")));
			sb.append("\n");
			sb.append("出产场所卫生状况：");
			sb.append(ItextUtil.checked(new String[] { "好", "一般", "差" },
					map.get("sampling_result_3")));
			sb.append("微生物指标不合格不得复查");
			sb.append(ItextUtil.checked(new String[] { "是", "否" },
					map.get("sampling_result_4")));

			table1.addCell(ItextUtil.getCell(simfang12, sb.toString(), o1 + o2
					+ o3 + p + q + r + s, 2, false));

			Font blackFont = FontUtil.getFont(mContext, 12, "simfang.ttf",
					Font.NORMAL, BaseColor.WHITE);

			table1.addCell(ItextUtil.getCell(simfang12, "备注", m + n, 2));

			StringBuilder sb2 = new StringBuilder();
			sb2.append("《企业监督检查记录》登记情况：");
			sb2.append(ItextUtil.checked(new String[] { "登记", "未登记" },
					map.get("remark_1")));
			sb2.append("\n");
			sb2.append("其他情况：");
			// sb2.append(map.get("remark_2") == null ? "" :
			// map.get("remark_2"));
			String ss = map.get("remark_2") == null ? "" : map.get("remark_2");
			table1.addCell(ItextUtil.getCell2(simfang12, sb2.toString(), bf,
					ss, o1 + o2 + o3 + p + q + r + s));

			table1.addCell(ItextUtil.getMultiCell3(simfang12, blackFont,
					"受检单位对上述内容无异议", "受检单位签名（盖章）：", "  年  月  日", 32, 1,
					2 + 13 * 2 + 6));
			table1.addCell(ItextUtil.getMultiCell3(simfang12, blackFont,
					"生产单位对上述内容无异议", "生产单位签名（盖章）：", "  年  月  日", 32, 1,
					2 + 13 * 2 + 6));
			table1.addCell(ItextUtil.getMultiCell3(simfang12, blackFont,
					"抽样人（签名）：", "抽样单位（盖章）", "  年  月  日", 36, 7 + 15 * 2 + 6, 9));

			document.add(table1);

			Font simfang8 = FontUtil.getFont(mContext, 8, "simfang.ttf");
			Paragraph r1 = new Paragraph(
					"注：1、此单一式4联（在生产领域抽样时使用）。抽样单位、生产单位、组织抽查的部门、负责监督抽查后处理的质量技术监督部门各一份。2、产量，填写上年度的实际年产量；证书编号，填写食品等产品生产许可证编号。3、抽样人员将抽样信息登记到《企业监督检查记录》上。",
					simfang8);
			document.add(r1);

			zxing(map.get("zxing"), mContext, document);
			stmp(mContext, document,380, 40);
			Paragraph bbb = new Paragraph(ItextUtil.getBlackStr(44), blackFont);
			document.add(bbb);

			// 步骤 5:关闭文档
			document.close();
			return true;
		} catch (Exception e) {
			Log.e(TAG, "", e);
			return false;
		}
	}

	private static void gg(Map<String, String> map, Font simfang12, Font bf,
			PdfPTable table1) {
		table1.addCell(ItextUtil.getCell(simfang12, "抽样数量", 23));
		table1.addCell(ItextUtil.getCell(bf, map.get("sampling_number"), 11));

		table1.addCell(ItextUtil.getCell(simfang12, "产品等级", 11));
		table1.addCell(ItextUtil.getCell(bf, map.get("product_level"), 10));

		table1.addCell(ItextUtil.getCell(simfang12, "抽样基数/批量", 20));
		table1.addCell(ItextUtil.getCell(bf, map.get("sampling_total_number"),
				20));

		table1.addCell(ItextUtil.getCell(simfang12, "标注执行标准/技术文件", 23 + 11));
		table1.addCell(ItextUtil.getCell(bf, map.get("product_standard"), 61));

	}

}

