package com.kyu.excel.test.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import com.kyu.common.Conf;
import com.kyu.common.DateUtil;
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
public class IncrossCardBill {

	/** 하나SK카드 승인 금액 */
	private final String FILE_PATH = "C:/card.xls";

	/** 핸드폰 지원 금액 */
	private final int PHONE_MAX_AMOUNT = 50000;

	/** 승인 상태 (정상) */
	private final String STATUS_NORMALITY = "정상";

	private final String LUNCH = "중식";
	private final String DINNER = "석식";
	private final String NIGHT_TAXI = "심야";
	private final String BUSINESS_TAXI = "업무";
	private final String CELL_PHONE = "휴대폰";
	private final String BREAKFAST = "조식";
	private final String MEETING_COST = "의욕";


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

		IncrossCardBill main = new IncrossCardBill();
		main.job();

//		1. card 사 홈페이지에서 승인 내역 excel로 출력
//		2. 첫 번째 행 생성
//			- title : 결제 구분
//			- 중식, 석식, 심야, 업무, 휴대폰, 의욕, 조식
//		3. 두 번째 행 생성
//			- 석식 초과 분에 대한 값 입력 (양의 정수)
//		4. c:\\ 디렉토리에 card.xml이름으로 excel 옮기기
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
		String paymentKind = vo.getPaymentKind();
		String status = vo.getStatus();
		int amount = vo.getAmount();

		// 취소 상태인 경우
		if (STATUS_NORMALITY.equals(status) == false) {
			return;
		}
		// 점심
		else if (LUNCH.equals(paymentKind)) {
			excelMappingVO.setTotalLunchAmount(amount);
			excelMappingVO.setLunchList(vo);
		}
		// 저녁
		else if (DINNER.equals(paymentKind)) {
			setLunchData(excelMappingVO, vo);
		}
		// 핸드폰
		else if (CELL_PHONE.equals(paymentKind)) {
			setPhoneAmount(excelMappingVO, amount);
		}
		// 심야 택시
		else if (NIGHT_TAXI.equals(paymentKind)) {
			String yesterday = getYesterday(vo.getDate()); // 심야 택시는 실제 업무한 날짜를 지정한다.
			vo.setDate(yesterday);
			excelMappingVO.setTaxiNightList(vo);
		}
		// 업무 택시
		else if (BUSINESS_TAXI.equals(paymentKind)) {
			excelMappingVO.setTaxiBusinessList(vo);
		}
		// 조식
		else if (BREAKFAST.equals(paymentKind)) {
			excelMappingVO.setOtherList(vo);
		}
		// 의욕관리비
		else if (MEETING_COST.equals(paymentKind)) {
			excelMappingVO.setMeetingCostList(vo);
		}
		else {
			System.out.println("##job## (is not same) vo=" + vo);
			throw new RuntimeException("결제 구분을 확인해 주시기 바랍니다.");
		}
	}

	/**
	 * <pre>
	 * setLunchData
	 *
	 * <pre>
	 * @param excelMappingVO
	 * @param vo
	 * @param amount
	 * @throws CloneNotSupportedException
	 */
	private void setLunchData(ExcelMappingVO excelMappingVO, ParseVO vo) {
		try {
			int amount = vo.getAmount();
			int dinnerExceed = vo.getDinnerExceed();

			if (dinnerExceed > 0) { // 석식 초과 분이 있다면
				amount = amount - dinnerExceed; // 결제 금액 - 석식 초과 금액
				vo.setAmount(amount);

				// 의욕관리비 데이터 set
				ParseVO cloneVO = (ParseVO) vo.clone();
				cloneVO.setAmount(dinnerExceed);
				excelMappingVO.setMeetingCostList(cloneVO);
			}

			// 석식 데이터 set
			excelMappingVO.setDinnerList(vo);

		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	/**
	 * <pre>
	 * setYesterday
	 *
	 * <pre>
	 * @param vo
	 */
	private String getYesterday(String date) {
		String replaceDate = date.replaceAll("-", "");
		String yesterday = DateUtil.getDayInterval(replaceDate, "yyyy-MM-dd", -1);
		return yesterday;
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
}

