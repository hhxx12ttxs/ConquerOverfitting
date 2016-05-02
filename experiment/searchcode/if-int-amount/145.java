package com.kyu.excel.test.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import com.kyu.common.Conf;
import com.kyu.excel.ExcelHandler;
import com.kyu.excel.core.parse.ExcelValue;
import com.kyu.excel.enumtype.ExcelBaseType;
import com.kyu.excel.generator.JXLSExcelGenerator;

/**
 * @FileName : Main.java
 * @Project : sample_project
 * @Date : 2012. 9. 24.
 * @작성자 : 이남규
 * @프로그램설명 :
 */
@Deprecated
public class IncrossExpense {

	/** 하나SK카드 승인 금액 */
	private final String FILE_PATH = "C:/card.xls";
	/** 승인 상태 (정상) */
	private final String STATUS_NORMALITY = "정상";

	/** 핸드폰 자동 이체 구분 text */
	private final String[] PHONE_SEPARATOR_ARR = { "자동납부" };
	/** 택시 청구 요금 구분 text */
	private final String[] TAXI_SEPARATOR_ARR = { "주식회사한국스마트카드", "택시" };

	/** 핸드폰 지원 금액 */
	private final int PHONE_MAX_AMOUNT = 50000;

	/** 점심 시작 시간 */
	private final int LUNCH_START_TIME = 1100;
	/** 점심 종료 시간 */
	private final int LUNCH_END_TIME = 1700;
	/** 저녁 시작 시간 */
	private final int DINNER_START_TIME = 1700;

	/** 심야 택시 시작 시간 */
	private final int NIGHT_TAXI_START_TIME = 0000;
	/** 심야 택시 종료 시간 */
	private final int NIGHT_TAXI_END_TIME = 0600;
	/** 업무 택시 시작 시간 */
	private final int BUSINESS_TAXI_START_TIME = 0600;
	/** 업무 택시 종료 시간 */
	private final int BUSINESS_TAXI_END_TIME = 2400;

	/**
	 * <pre>
	 * main
	 *
	 * <pre>
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Conf.init();

		IncrossExpense main = new IncrossExpense();
		main.job();
	}

	/**
	 * <pre>
	 * job
	 *
	 * <pre>
	 */
	public void job() {
		ExcelMappingVO excelMappingVO = new ExcelMappingVO();
		try {
			// excel parsing data
			List<ExcelValue> excelValueList = excelParse();

			// data set
			for (ExcelValue excelValue : excelValueList) {
				ParseVO vo = (ParseVO) excelValue;
				setData(excelMappingVO, vo);
			}

			// create excel
			boolean isSuccess = createExcel(excelMappingVO);
			System.out.println("##job## isSuccess=" + isSuccess);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * <pre>
	 * createExcel
	 *
	 * <pre>
	 * @param excelMappingVO
	 */
	private boolean createExcel(ExcelMappingVO excelMappingVO) {
		JXLSExcelGenerator excelGenerator = new JXLSExcelGenerator();
		boolean isSuccess = ExcelHandler.createExcel(excelMappingVO, excelGenerator, ExcelBaseType.INCROSS_EXPENSE);
		return isSuccess;
	}

	/**
	 * <pre>
	 * setValue
	 *
	 * <pre>
	 * @param excelMappingVO
	 * @param vo
	 */
	private void setData(ExcelMappingVO excelMappingVO, ParseVO vo) {
		String memberStoreName = vo.getMemberStoreName();
		String status = vo.getStatus();
		int amount = vo.getAmount();
		int time = getTime(vo.getTime());

		// 취소 상태인 경우
		if (STATUS_NORMALITY.equals(status) == false) {
			return;
		}
		// 핸드폰
		else if (includeSeparatorTxt(memberStoreName, PHONE_SEPARATOR_ARR)) {
			setPhoneAmount(excelMappingVO, amount);
		}
		// 심야 택시
		else if (isTaxiCheck(memberStoreName, time, true)) {
			excelMappingVO.setTaxiNightList(vo);
		}
		// 업무 택시
		else if (isTaxiCheck(memberStoreName, time, false)) {
			excelMappingVO.setTaxiBusinessList(vo);
		}
		// 점심
		else if (time > LUNCH_START_TIME && time < LUNCH_END_TIME) {
			excelMappingVO.setTotalLunchAmount(amount);
			excelMappingVO.setLunchList(vo);
		}
		// 저녁
		else if (time >= DINNER_START_TIME) {
			excelMappingVO.setDinnerList(vo);
			excelMappingVO.setTotalDinnerAmount(amount);
		}
		else {
			excelMappingVO.setOtherList(vo);
			System.out.println("##job## (is not same) vo=" + vo);
		}

		excelMappingVO.setTotalAmount(amount);
	}

	/**
	 * <pre>
	 * excelParse
	 * 카드 승인 내역 엑셀 파싱
	 *
	 * <pre>
	 * @return
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	private List<ExcelValue> excelParse() throws Exception {
		FileInputStream fileInputStream = new FileInputStream(new File(FILE_PATH));
		List<ExcelValue> excelValueList = ExcelHandler.parse(fileInputStream, ParseVO.class);
		System.out.println("##job## excelValueList=" + excelValueList + ", filePath=" + FILE_PATH);
		return excelValueList;
	}

	/**
	 * <pre>
	 * setPhoneAmount
	 *
	 * <pre>
	 * @param excelMappingVO
	 * @param amount
	 */
	private void setPhoneAmount(ExcelMappingVO excelMappingVO, int amount) {
		int phoneExceptAmount = 0;
		int phoneCalculationAmount = 0;

		if (amount > PHONE_MAX_AMOUNT) {
			phoneExceptAmount = amount - PHONE_MAX_AMOUNT;
			phoneCalculationAmount = PHONE_MAX_AMOUNT;
		} else {
			phoneCalculationAmount = amount;
		}

		excelMappingVO.setPhoneCalculationAmount(phoneCalculationAmount);
		excelMappingVO.setPhoneAmount(amount);
		excelMappingVO.setPhoneExceptAmount(phoneExceptAmount);
	}

	/**
	 * <pre>
	 * includeSeparatorTxt
	 *
	 * <pre>
	 * @param memberStoreName
	 * @return
	 */
	private boolean includeSeparatorTxt(String memberStoreName, String[] separators) {
		boolean existSeparator = false;
		for (int i = 0; i < separators.length; i++) {
			String separator = separators[i];
			if (memberStoreName.indexOf(separator) > -1) {
				existSeparator = true;
			}
		}
		return existSeparator;
	}

	/**
	 * <pre>
	 * isNightTaxi
	 *
	 * <pre>
	 * @param memberStoreName
	 * @param separtor
	 * @param time
	 * @return
	 */
	private boolean isTaxiCheck(String memberStoreName, int time, boolean isNight) {
		boolean existSepTxt = includeSeparatorTxt(memberStoreName, TAXI_SEPARATOR_ARR);
		boolean isTime = false;

		// 야간 택시
		if (isNight && time >= NIGHT_TAXI_START_TIME && time <= NIGHT_TAXI_END_TIME) {
			isTime = true;
		}
		// 업무 택시
		else if (isNight == false && time > BUSINESS_TAXI_START_TIME && time < BUSINESS_TAXI_END_TIME) {
			isTime = true;
		}

		if (existSepTxt && isTime) {
			return true;
		}

		return false;
	}

	/**
	 * <pre>
	 * getTime
	 *
	 * <pre>
	 * @param date
	 * @return
	 */
	private int getTime(String time) {
		String timeStr = time.replaceAll(":", "");
		return Integer.parseInt(timeStr);
	}
}

