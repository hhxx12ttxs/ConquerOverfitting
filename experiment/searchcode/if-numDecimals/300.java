/**============================================================
 * ��Ȩ��  ��Ȩ���� (c) 2002 - 2012
 * �� com.spark.report.intf.constant
 * �޸ļ�¼��
 * ����                ����           ����
 * =============================================================
 * 2011-12-12       zhongxin        
 * ============================================================*/

package com.spark.psi.report.constant;

/**
 * @author zhongxin
 *
 */
public class OldReportEnums{
	public final static String AMOUNT_UNIT = "Ԫ";
	public final static String HUAN_BI = "����";

	/**
	 * �ɹ�������ö��
	 * @author zhongxin
	 *
	 */
	public enum BuyOrSale{
		/** �ɹ�  */
		BUY("01"),
		/** ����  */
		SALE("02");

		String code;

		private BuyOrSale(String code){
			this.code = code;
		}

		public String getCode(){
			return this.code;
		}
	}

	/**
	 * �ͻ�����Ӧ��ö��
	 * @author zhongxin
	 *
	 */
	public enum CustomOrProvider{
		/** �ɹ�  */
		CUSTOM("01"),
		/** ����  */
		PROVIDER("02");

		String code;

		private CustomOrProvider(String code){
			this.code = code;
		}

		public String getCode(){
			return this.code;
		}
	}

	/**
	 * ��������ʾ������ͣ����۽������տ�ö��
	 * @author zhongxin
	 *
	 */
	public enum ScreenType{
		/** ���۽��  */
		SALE_AMOUNT("01", "���۽��"),
		/** �����տ�  */
		SALE_RECEIPT("02", "�����տ�");
		private String code;
		private String title;
		private ScreenType(String code, String title){
			this.code = code;
			this.title = title;
		}
		public String getCode(){
			return this.code;
		}
        public String getTitle(){
        	return title;
        }

		public static ScreenType getScreenTypeByCode(String code){
			if("01".equals(code)){
				return SALE_AMOUNT;
			}
			else if("02".equals(code)){
				return SALE_RECEIPT;
			}
			else{
				return null;
			}
		}
		
		public String getUrl(){
			switch (this) {
			case SALE_AMOUNT:
				return "sales";
			case SALE_RECEIPT:
				return "receipt";
			}
			return null;
		}
	}

	/**
	 * <p>����ͼ������</p>
	 *
	 * <p>Copyright: ��Ȩ���� (c) 2002 - 2008<br>
	
	 *
	 * @author tangchengguo
	 * @version 2012-1-7
	 */

	public enum ReportTypeEnum{
		LINE("����"),
		BAR("����"),
		LINE_BAR_EQUAL("ֱ�ߺ�����һ�����һ��"),
		LINE_BAR_NEQUAL("ֱ�ߺ�����һ����ݲ�һ��"),
		PIE("��ͼ");
		private String title;

		ReportTypeEnum(String title){
			this.title = title;
		}

		public String getTitle(){
			return title;
		}
	}

	/**
	 * 
	 * <p>ҳǩö��</p>
	 *
	 * <p>Copyright: ��Ȩ���� (c) 2002 - 2008<br>
	
	 *
	 * @author ������
	 * @version 2012-12-12
	 */
	public enum PageType{
		/**
		 * ����
		 */
		QUARTER,
		/**
		 * ��
		 */
		MONTH,
	}

	/**
	 * �������
	 * @author zhongxin
	 *
	 */
	public enum ReportDataType{
		/** ���۷��� */
		SALE_ANALYSIS,
		/** �ɹ����� */
		BUY_ANALYSIS,
		/** ������ */
		STORAGE_ANALYSIS,
		/** ��֧��� */
		FINANCE_ANALYSIS,
		/** �����˻����� */
		SALE_RTN_ANALYSIS,
		/** �ͻ����� */
		CUSTOM_ANALYSIS,
	}

	/**
	 * ���۷�������
	 * @author zhongxin
	 *
	 */
	public enum FenXiType{
		GONG_SI, // ��˾
		BU_MEN, // ����
		YUAN_GONG, // Ա��
		
		/** ���ž��?����Ա��ʱ�����۹ܿؽ��洦��ҵ������ *******/
		XS_DUIBI, // ���۶Ա�
		KH_PAIMING // �ͻ�����
	}

	/**
	 * ��ѯ���¡����¡�����
	 * @author ������
	 */
	public enum DayMonQuarter{
		/**
		 * ����
		 */
		DAY,
		/**
		 * ����
		 */
		MONTH,
		/**
		 * ����
		 */
		QUARTER
	}
	
	/**
	 * 
	 * <p>��Ӫ����������Լ���ʾ�ַ�������ʾλ��</p>
	 *
	 * <p>Copyright: ��Ȩ���� (c) 2002 - 2008<br>
	
	 *
	 * @author ���ɹ�
	 * @version 2011-12-20
	 */
	public enum LabelFormatType {
		DECEIMAL_ZERO("{%Value}{numDecimals:0}", "{numDecimals:0}"), // 0λ
		DECEIMAL_TWO("{%Value}{numDecimals:2}", "{numDecimals:2}"); // 2λ
		private String labelFormat;
		private String hintFormat;
		LabelFormatType(String labelFormat, String hintFormat) {
			this.labelFormat = labelFormat;
			this.hintFormat = hintFormat;
		}
        public String getLabelFormat(){
        	return labelFormat;
        }
        public String getHintFormat(){
        	return hintFormat;
        }
	}
	
	/**
	 * ����������
	 * @author ���ɹ�
	 */
	public enum FloatWindowType{
		ONLY_SALE("������"),
		ONLY_RTN("���˻�"),
		SALE_RTN("�����˻�"),
		BUY_RTN("�ɹ��˻�"),
		STORE_SALE_BUY("������۲ɹ�");
		FloatWindowType(String title) {
		}
	}
	public class RreportConstant{
		
		/**
		 * ����Ϊ��ʱ���õ�������ɫ
		 * item.getCell(index).setFontColor();
		 */
		public static final int huanbiColor = 0xFF3200;
		
		
		// ��ѯ���Ų����Լ���һ�����ŵĽڵ�
		public static final String DEPT_YIJI_JIEDAIN = " JOIN ( SELECT h1.RECID AS recid,s.RECID AS srecid  FROM SA_CORE_TREE AS s  JOIN" +
											" SA_CORE_TREE AS h1 ON h1.PATH > s.PATH AND (h1.PATH < s.PATH || bytes'ff')" +
											" and len(h1.PATH) = len(s.PATH) + 34 " +
											" WHERE s.RECID =  @deptGuid) AS Y ON y.recid=a.deptGuid ";
		// ��ѯ���Ű��Լ���һ�����ŵĽڵ�
		public static final String DEPT_YIJI_JIEDAIN_AND_SELF = " JOIN ( SELECT h1.RECID AS recid,s.RECID AS srecid  FROM SA_CORE_TREE AS s  JOIN" +
											" SA_CORE_TREE AS h1 ON h1.PATH > s.PATH AND (h1.PATH < s.PATH || bytes'ff')" +
											" and len(h1.PATH) = len(s.PATH) + 34 OR h1.RECID=@deptGuid " +
											" WHERE s.RECID =  @deptGuid) AS Y ON y.recid=a.deptGuid ";
		
		// ��ѯ���Ų����Լ������ﲿ�ŵĽڵ�
		public static final String DEPT_ZISUN_JIEDAIN = " JOIN ( SELECT h1.RECID AS recid,s.RECID AS srecid  FROM SA_CORE_TREE AS s  JOIN" +
											" SA_CORE_TREE AS h1 ON h1.PATH > s.PATH AND (h1.PATH < s.PATH || bytes'ff')" +
											" and len(h1.PATH) = len(s.PATH) + 34 " +
											" WHERE s.RECID =  @deptGuid) AS Y ON y.recid=a.deptGuid ";
		
		// ��ѯ���Ű��Լ������ﲿ�ŵĽڵ�
		public static final String DEPT_ZISUN_JIEDAIN_AND_SELF = " JOIN ( SELECT h1.RECID AS recid,s.RECID AS srecid  FROM SA_CORE_TREE AS s  JOIN" +
											" SA_CORE_TREE AS h1 ON h1.PATH > s.PATH AND (h1.PATH < s.PATH || bytes'ff')" +
											" and len(h1.PATH) = len(s.PATH) + 34 OR h1.RECID=@deptGuid " +
											" WHERE s.RECID =  @deptGuid) AS Y ON y.recid=a.deptGuid ";
		// ��ѯ���Ű��Լ������ﲿ�ŵĽڵ�
		public static final String DEPT_ZISUN_JIEDAIN_AND_SELF_NOJOIN = " SELECT h1.RECID AS recid,s.RECID AS srecid  FROM SA_CORE_TREE AS s  JOIN" +
											" SA_CORE_TREE AS h1 ON h1.PATH > s.PATH AND (h1.PATH < s.PATH || bytes'ff')" +
											" and len(h1.PATH) = len(s.PATH) + 34 OR h1.RECID=@deptGuid " +
											" WHERE s.RECID =  @deptGuid ";
		/**
		 * 
		 * <p>���۷�������</p>
		 *
		 * <p>Copyright: ��Ȩ���� (c) 2002 - 2008<br>
		
		 *
		 * @author ���ɹ�
		 * @version 2011-12-14
		 */
		public  class SaleReportConstant {
			public final static String SALE_AMOUNT = "���۽��";
			public final static String SALE_RECEIT = "�����տ�";
			public final static String RECEIT_AMOUNT = "�ؿ��տ�";
			public final static String SALE_TARGET = "����Ŀ��";
			public final static String RECEIT_TARGET = "�ؿ�Ŀ��";
		}
		
		/**
		 * 
		 * <p>�ɹ���������</p>
		 *
		 * <p>Copyright: ��Ȩ���� (c) 2002 - 2008<br>
		
		 *
		 * @author ���ɹ�
		 * @version 2011-12-14
		 */
		public  class BuyReportConstant {
			public final static String BUY_AMOUNT = "�ɹ����";
			public final static String BUY_RECEIT = "�ɹ�����";
		}
		 
	}
}

