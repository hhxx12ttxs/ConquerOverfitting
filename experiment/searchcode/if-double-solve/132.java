package com.bamcore.deal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.json.simple.JSONObject;

import com.bamcore.pricing.PricingEngine;
import com.bamcore.pricing.PricingInput;
import com.bamcore.pricing.PricingMethod;
import com.bamcore.printing.PricingGraph;
import com.bamcore.util.BAMDate;
import com.bamcore.util.BobConstants;
import com.bamcore.util.BobUtil;
import com.bamcore.util.BondMath;
import com.bamcore.util.Money;
import com.bamcore.util.ReferenceDataException;

/** 
 * An object representing a policy, which includes a set of insured maturities, a bob schedule, and all the data necessary
 * to price and value a policy.
 * 
 * @author davidmcintyre
 *
 */
public class DealParams {
	
	public static final String defaultVersionDK = new String("proposed");
	
	private String commitID = null;
	
	private BAMDate computeDate = null;
	private boolean freeze = false;
	private PricingMethod method = PricingMethod.ALLUPFRONT;
	private int yearsUpfront;
	private int version = 1;
	private String versionDK = "proposed";
	private PricingInput inputDK = PricingInput.SPREAD;
	private String comment = new String("");
	
	private double actualMscRate;
	private double targetMscRate;
	private double mscFundingPct;
	private Money msc = Money.Zero;
	private double mscPA;
	private double mscPctTds;
	private Money targetMscDollars = Money.Zero;
	
	private Money upfrontPremium = Money.Zero;
	private double riskPremPA;
	private double riskPremPAInstall;
	private boolean singleRiskPrem = true;
	private boolean newCusip = false;
	
	private double cedingPct;
	private double cedingCommission;
	
	private double discountRate;
	private double earningsRate;
	
	private Money parInsured = Money.Zero;
	private double actualCreditSpread;
	private double modelCreditSpread;
	private double capturedNC;
	private double capturedC;
	private double totalPA;
	private double actualSpreadCapture;
	private double floorRatio = 0.75;
	
	private double baseRac;
	private double crmFactor;
	private double crmAdjustedRacPct;

	private double rac;
	private double preTax10RoRac;
	private double afterTax10RoRac;
	private double snpRap;

	private double preTaxLifeRoRac;
	private double afterTaxLifeRoRac;
	
	private double bamOpExp;
	private double hgreOpExp;
	private double premTaxAmt;
	private double exciseTaxAmt;
	private double corpTaxAmt;
	
	private double bamExpenseRate;
	private double hgExpenseRate;
	
	private double corpTaxRate;
	private double exciseTaxRate;
	private double premTaxRate;
	
	private Money totalUpfront = Money.Zero;
	private double totalUpfrontPctPar;
	private double totalUpfrontPctTDS;
	
	private double riskPremPctPar;
	private double riskPremPctTds;
	private double minRiskPremPctPar;
	private double minTotalPctPar;
	private Money minRiskPrem;
	private Money minTotalPrem;
	
	private Money pvfip = Money.Zero;
	private Money totalBAMPrem = Money.Zero;
	private double custSavings;
	
	private Money contResvAmt = Money.Zero;
	private Money contResvAmtQ = Money.Zero;
	private double contResvTerm;
	private double contResvFactor;
	
	private Money dollarsPerBond = Money.Zero;
	private Money feeSnp = Money.Zero;
	private Money feeCusip = Money.Zero;
	private Money feeBony = Money.Zero;
	private Money feeDtc = Money.Zero;
	private Money feeTotal = Money.Zero;
	private Money totalGrossUpfront = Money.Zero;
	
	//
	// Not stored.
	//
	private PricingGraph pricingGraph;
	private boolean roundBps;

	/**
	 * Create a policy from a set of insured maturities.
	 * 
	 * @param commitID
	 * @param policyName
	 * @param sector
	 * @param datedDate
	 * @param firstInstallDate
	 * @param parInsured
	 * @param mscRate
	 * @param riskPremRate
	 * @param yearsUpFront
	 * @param installPremRate
	 * @param insuredMats
	 * @param cedingPct
	 * @param cedingCommission
	 * @param discountRate
	 * @param actualCreditSpread
	 * @param capturedNC
	 * @throws PricingException
	 * @throws ScheduleException
	 */
	public DealParams() {
	}
	
	/**
	 * Create an empty deal pricing with only a deal ID.
	 * 
	 * @param dealID
	 */
	public DealParams(String dealID) {
		this(dealID, null);
	}
	
	/**
	 * Create an empty deal pricing with only a deal ID and a dated date.  Only copy the dated date
	 * if it's valid.
	 * 
	 * @param dealID
	 */
	public DealParams(String dealID, BAMDate computeDate) {
		super();
		
		this.commitID = new String(dealID);
		if (computeDate != null && computeDate.isValid()) {
			this.computeDate = new BAMDate(computeDate);
		}
	}
	
	/**
	 * Create a maturity from a result set that is already pointing to the row from the maturity
	 * table that we want to parse
	 * 
	 * 
	 * @param rs
	 * 
	 */
	public DealParams(Connection c, String commitID, String versionDK) throws SQLException{
		super();
		this.commitID = commitID;
		
		load(c, versionDK);
	}
	
	/**
	 * Copy construtor.
	 * 
	 */
	public DealParams(DealParams other) {
		this.setCommitID(other.getCommitID());
		this.setComputeDate(new BAMDate(other.getComputeDate()));
		this.setFreeze(other.isFreeze());
		this.setYearsUpfront(other.getYearsUpfront());
		this.setMethod(other.getMethod());
		this.setVersion(other.getVersion());
		this.setVersionDK(other.getVersionDK());
		this.setInputDK(other.getInputDK());
		this.setComment(other.getComment());
		
		this.setActualCreditSpread(other.getActualCreditSpread());
		this.setActualMscRate(other.getActualMscRate());
		this.setCapturedNC(other.getCapturedNC());
		this.setCapturedC(other.getCapturedC());
		this.setTargetMscRate(other.getTargetMscRate());
		this.setMscFundingPct(other.getMscFundingPct());
		this.setTargetMscDollars(other.getTargetMscDollars());
		this.setActualSpreadCapture(other.getActualSpreadCapture());
		this.setFloorRatio(other.getFloorRatio());

		this.setCrmFactor(other.getCrmFactor());
		this.setBaseRac(other.getBaseRac());
		
		this.setBamExpenseRate(other.getBamExpenseRate());
		this.setHgExpenseRate(other.getHgExpenseRate());
		this.setEarningsRate(other.getEarningsRate());
		this.setDiscountRate(other.getDiscountRate());
		this.setExciseTaxRate(other.getExciseTaxRate());
		this.setPremTaxRate(other.getPremTaxRate());
		this.setCorpTaxRate(other.getCorpTaxRate());
		
		this.setTotalPA(other.getTotalPA());
		this.setRiskPremPA(other.getRiskPremPA());
		this.setRiskPremPAInstall(other.getRiskPremPAInstall());
		this.setSingleRiskPrem(other.isSingleRiskPrem());
		this.setNewCusip(other.isNewCusip());
		this.setMinRiskPremPctPar(other.getMinRiskPremPctPar());
		this.setMinTotalPctPar(other.getMinTotalPctPar());
		this.setMinTotalPrem(other.getMinTotalPrem());
		this.setMinRiskPrem(other.getMinRiskPrem());
		
		this.setMscPA(other.getMscPA());
		this.setUpfrontPremium(other.getUpfrontPremium());
		this.setMsc(other.getMsc());
		this.setMscPctTds(other.getMscPctTds());
		
		this.setBamOpExp(other.getBamOpExp());
		this.setHgreOpExp(other.getHgreOpExp());
		this.setPremTaxAmt(other.getPremTaxAmt());
		this.setExciseTaxAmt(other.getExciseTaxAmt());
		this.setCorpTaxAmt(other.getCorpTaxAmt());
		this.setCedingPct(other.getCedingPct());
		
		this.setCrmAdjustedRacPct(other.getCrmAdjustedRacPct());
		
		this.setPreTax10RoRac(other.getPreTax10RoRac());
		this.setAfterTax10RoRac(other.getAfterTax10RoRac());
		this.setPreTaxLifeRoRac(other.getPreTaxLifeRoRac());
		this.setAfterTaxLifeRoRac(other.getAfterTaxLifeRoRac());
		
		this.setRac(other.getRac());
		this.setSnpRap(other.getSnpRap());
		
		this.setTotalUpfront(other.getTotalUpfront());
		this.setTotalUpfrontPctPar(other.getTotalUpfrontPctPar());
		this.setTotalUpfrontPctTDS(other.getTotalUpfrontPctTDS());
		this.setRiskPremPctPar(other.getRiskPremPctPar());
		this.setRiskPremPctTds(other.getRiskPremPctTds());
		this.setPvfip(other.getPvfip());
		this.setTotalBAMPrem(other.getTotalBAMPrem());
		this.setCustSavings(other.getCustSavings());
		
		this.setContResvAmt(other.getContResvAmt());
		this.setContResvAmtQ(other.getContResvAmtQ());
		this.setContResvFactor(other.getContResvFactor());
		this.setContResvTerm(other.getContResvTerm());
		
		this.setDollarsPerBond(other.getDollarsPerBond());
		this.setFeeSnp(other.getFeeSnp());
		this.setFeeCusip(other.getFeeCusip());
		this.setFeeBony(other.getFeeBony());
		this.setFeeDtc(other.getFeeDtc());
		this.setFeeTotal(other.getFeeTotal());
		this.setTotalGrossUpfront(other.getTotalGrossUpfront());
		
		this.setPricingGraph(other.getPricingGraph());  // TODO: OK if we don't use a copy constructor here??
		this.setRoundBps(other.isRoundBps());
	}
	
	/**
	 * Load up deal pricing part of this deal.  Loads up the version with the
	 * highest px_version value.
	 * 
	 *  
	 * @param c
	 * @throws SQLException
	 */
	protected void load(Connection c, String versionDK) throws SQLException {
		//
		// A) commitment table
		//
		String select = "Select p.px_compute_date, p.px_final_pricing, p.px_version, p.px_version_dk, p.px_input_dk, "
				+ " p.px_i_spread_actual, p.px_i_captured_nc, p.px_i_crm_factor, "
				+ " p.px_i_base_rac, p.px_i_target_msc_rate, p.px_o_actual_msc_rate, p.px_i_bam_exp_rate, p.px_i_hgre_exp_rate, "
				+ " p.px_i_earnings_rate, p.px_i_discount_rate, "
				+ " p.px_i_excise_tax_rate, p.px_i_prem_tax_rate, p.px_i_corp_tax_rate, p.px_i_single_risk_prem, "
				+ " p.px_i_riskprem_pa_install, p.px_i_cont_resv_factor, p.px_i_cont_resv_term, p.px_i_floor_ratio, "
				+ " p.px_i_new_cusip, "
				
				+ " p.px_o_total_pa, p.px_o_riskprem_pa, p.px_o_msc_pa, p.px_o_uf_riskprem, p.px_o_msc, "
				+ " p.px_o_bam_opexp, p.px_o_hgre_opexp, p.px_o_prem_tax, p.px_o_excise_tax, p.px_o_corp_tax, "
				+ " p.px_o_crm_adj_rac, p.px_o_rorac_10_pretax, p.px_o_rorac_10_aftertax, "
				+ " p.px_o_rorac_life_pretax, p.px_o_rorac_life_aftertax, "
				+ " p.px_o_rac, "
				+ " p.px_i_years_upfront, p.px_i_method_dk, p.px_o_total_uf, p.px_o_total_uf_pct_par, "
				+ " p.px_o_pvfip, p.px_o_total_bam_premium, "
				+ " p.px_o_cont_resv_amt, p.px_o_cont_resv_amtQ, p.px_o_total_uf_pct_tds, p.px_o_snp_rap, "
				+ " p.px_o_uf_riskprem_pct_par,  p.px_o_uf_riskprem_pct_tds, p.px_o_msc_pct_tds, "
				+ " p.px_i_spread_model, p.px_i_captured_c, p.px_o_msc_funding_pct, p.px_o_target_msc_dollars, "
				+ " p.px_o_min_risk_prem_pct_par, p.px_o_min_total_pct_par, p.px_o_actual_spread_capture, " 
				
				+ " p.px_o_dollars_per_bond, p.px_o_fee_snp, p.px_o_fee_cusip, p.px_o_fee_bony, p.px_o_fee_dtc, "
				+ " p.px_o_fee_total, p.px_o_total_gross_uf, p.px_o_min_risk_prem_dollars, p.px_o_min_total_dollars, "
				+ " px_comment "
				
				+ " from deal_pricing p " 
				+ " where p.px_deal_ID = ? "
				+ " and p.px_version_dk = ? "
				+ " order by p.px_version desc ";
		PreparedStatement sel = c.prepareStatement(select);
		sel.setString(1, commitID);
		sel.setString(2,  versionDK);
		ResultSet rs = sel.executeQuery();
		
		rs.first();
		
		computeDate = BobUtil.readBAMDate(rs, "px_compute_date");
		freeze = rs.getString("px_final_pricing").equals("Y");
		yearsUpfront = rs.getInt("px_i_years_upfront");
		method = DealParams.codeToPricingMethod(rs.getString("px_i_method_dk"));
		version = rs.getInt("px_version");
		versionDK = rs.getString("px_version_dk");
		inputDK = PricingInput.codeToPricingInput(rs.getString("px_input_dk"));
		singleRiskPrem = rs.getString("px_i_single_risk_prem").equals("Y");
		newCusip = rs.getString("px_i_new_cusip").equals("Y");

		actualCreditSpread = rs.getDouble("px_i_spread_actual");
		modelCreditSpread = rs.getDouble("px_i_spread_model");
		capturedNC = rs.getDouble("px_i_captured_nc");
		capturedC = rs.getDouble("px_i_captured_c");
		
		crmFactor = rs.getDouble("px_i_crm_factor");
		baseRac = rs.getDouble("px_i_base_rac");
		targetMscRate = rs.getDouble("px_i_target_msc_rate");
		actualMscRate = rs.getDouble("px_o_actual_msc_rate");
		bamExpenseRate = rs.getDouble("px_i_bam_exp_rate");
		hgExpenseRate = rs.getDouble("px_i_hgre_exp_rate");
		earningsRate = rs.getDouble("px_i_earnings_rate");
		discountRate = rs.getDouble("px_i_discount_rate");
		exciseTaxRate = rs.getDouble("px_i_excise_tax_rate");
		premTaxRate = rs.getDouble("px_i_prem_tax_rate");
		corpTaxRate = rs.getDouble("px_i_corp_tax_rate");
		riskPremPAInstall = rs.getDouble("px_i_riskprem_pa_install");
		contResvFactor = rs.getDouble("px_i_cont_resv_factor");
		contResvTerm = rs.getDouble("px_i_cont_resv_term");
		floorRatio = rs.getDouble("px_i_floor_ratio");

		totalPA = rs.getDouble("px_o_total_pa");
		riskPremPA = rs.getDouble("px_o_riskprem_pa");
		mscPA = rs.getDouble("px_o_msc_pa");
		mscPctTds = rs.getDouble("px_o_msc_pct_tds");
		upfrontPremium = new Money(rs.getDouble("px_o_uf_riskprem"));
		msc = new Money(rs.getDouble("px_o_msc"));
		mscFundingPct = rs.getDouble("px_o_msc_funding_pct");
		targetMscDollars = new Money(rs.getDouble("px_o_target_msc_dollars"));
		bamOpExp = rs.getDouble("px_o_bam_opexp");
		hgreOpExp = rs.getDouble("px_o_hgre_opexp");
		premTaxAmt = rs.getDouble("px_o_prem_tax");
		exciseTaxAmt = rs.getDouble("px_o_excise_tax");
		corpTaxAmt = rs.getDouble("px_o_corp_tax");
		crmAdjustedRacPct = rs.getDouble("px_o_crm_adj_rac");
		
		preTax10RoRac = rs.getDouble("px_o_rorac_10_pretax");
		afterTax10RoRac = rs.getDouble("px_o_rorac_10_aftertax");
		preTaxLifeRoRac = rs.getDouble("px_o_rorac_life_pretax");
		afterTaxLifeRoRac = rs.getDouble("px_o_rorac_life_aftertax");
		
		rac = rs.getDouble("px_o_rac");
		
		totalUpfront = new Money(rs.getDouble("px_o_total_uf"));
		totalUpfrontPctPar = rs.getDouble("px_o_total_uf_pct_par");
		pvfip = new Money(rs.getDouble("px_o_pvfip"));
		totalBAMPrem = new Money(rs.getDouble("px_o_total_bam_premium"));
		
		contResvAmt = new Money(rs.getDouble("px_o_cont_resv_amt"));
		contResvAmtQ = new Money(rs.getDouble("px_o_cont_resv_amtQ"));
		
		snpRap = rs.getDouble("px_o_snp_rap");
		totalUpfrontPctTDS = rs.getDouble("px_o_total_uf_pct_tds");
		riskPremPctPar = rs.getDouble("px_o_uf_riskprem_pct_par");
		riskPremPctTds = rs.getDouble("px_o_uf_riskprem_pct_tds");
		minRiskPremPctPar = rs.getDouble("px_o_min_risk_prem_pct_par");
		minTotalPctPar = rs.getDouble("px_o_min_total_pct_par");
		actualSpreadCapture = rs.getDouble("px_o_actual_spread_capture");
		
		dollarsPerBond = new Money(rs.getDouble("px_o_dollars_per_bond"));
		feeSnp = new Money(rs.getDouble("px_o_fee_snp"));
		feeCusip = new Money(rs.getDouble("px_o_fee_cusip"));
		feeBony = new Money(rs.getDouble("px_o_fee_bony"));
		feeDtc = new Money(rs.getDouble("px_o_fee_dtc"));
		feeTotal = new Money(rs.getDouble("px_o_fee_total"));
		totalGrossUpfront = new Money(rs.getDouble("ps_o_total_gross_uf"));
		minRiskPrem = new Money(rs.getDouble("px_o_min_risk_prem_dollars"));
		minTotalPrem = new Money(rs.getDouble("px_o_min_total_dollars"));
		
		comment = rs.getString("px_comment");
		if (comment == null) {
			comment = new String("");
		}
	}
	
	/**
	 * Stores this deal pricing object, uses "replace into"
	 * 
	 * @param c
	 * @throws SQLException
	 */
	public void storeDealPricing(Connection c) throws SQLException {
		String query = "replace into deal_pricing "
				+ " set px_compute_date = ?, px_final_pricing = ?, px_i_years_upfront = ?, px_version = ?, px_version_dk = ?, "
				+ " px_input_dk = ?, px_o_msc = ?, px_o_msc_pa = ?, px_o_riskprem_pa = ?, "
				+ " px_o_total_pa = ?, px_o_uf_riskprem = ?, px_i_captured_nc = ?, px_i_spread_actual = ?, "
				+ " px_o_rac_pct_par = ?, px_o_rac = ?, px_o_rorac_10_pretax = ?, px_o_rorac_10_aftertax = ?, "
				+ " px_o_rorac_life_pretax = ?, px_o_rorac_life_aftertax = ?, "
				+ " px_i_base_rac = ?, px_o_crm_adj_rac = ?, px_i_crm_factor = ?, px_i_bam_exp_rate = ?, "
				+ " px_o_bam_opexp = ?, px_o_hgre_opexp = ?, px_o_prem_tax = ?, px_o_excise_tax = ?, px_o_corp_tax = ?, " 
				+ " px_i_corp_tax_rate = ?, px_i_excise_tax_rate = ?, px_i_prem_tax_rate =?, "
				+ " px_i_earnings_rate = ?, px_i_discount_rate = ?, "
				+ " px_i_target_msc_rate = ?, px_o_actual_msc_rate = ?, px_i_hgre_exp_rate = ?, px_i_method_dk = ?, "
				+ " px_o_total_uf = ?, px_o_total_uf_pct_par = ?, px_o_pvfip = ?, px_o_total_bam_premium = ?, "
				+ " px_i_single_risk_prem = ?, px_i_riskprem_pa_install = ?, "
				+ " px_i_cont_resv_factor = ?, px_i_cont_resv_term = ?, px_o_cont_resv_amt = ?, px_o_cont_resv_amtQ = ?, "
				+ " px_o_total_uf_pct_tds = ?, px_o_snp_rap = ?, px_o_uf_riskprem_pct_par = ?, px_o_uf_riskprem_pct_tds = ?, "
				+ " px_o_msc_pct_tds = ?, "
				+ " px_i_spread_model = ?, px_i_captured_c = ?, px_o_msc_funding_pct = ?, px_o_target_msc_dollars = ?, "
				+ " px_o_min_risk_prem_pct_par = ?, px_o_min_total_pct_par = ?, px_o_actual_spread_capture = ?, "
				+ " px_i_floor_ratio = ?, "
				+ " px_o_dollars_per_bond = ?, px_o_fee_snp = ?, px_o_fee_cusip = ?, px_o_fee_bony = ?, "
				+ " px_o_fee_dtc = ?, px_o_fee_total = ?, px_o_total_gross_uf = ?, "
				+ " px_comment = ?, px_i_new_cusip = ?, px_o_min_risk_prem_dollars = ?, px_o_min_total_dollars = ?, "
				+ " px_deal_ID = ? ";
		
		PreparedStatement ps = c.prepareStatement(query);
		
		int column = 1;
		ps.setDate(column++, computeDate.toJavaSQLDate());
		ps.setString(column++, freeze ? "Y" : "N");
		ps.setInt(column++, yearsUpfront);
		ps.setInt(column++, version);
		ps.setString(column++, versionDK);
		ps.setString(column++, inputDK.toString());
		
		ps.setDouble(column++, msc.doubleValue());
		ps.setDouble(column++, mscPA);
		ps.setDouble(column++, riskPremPA);
		ps.setDouble(column++, totalPA);
		ps.setDouble(column++, upfrontPremium.doubleValue());
		if (capturedNC < 0.0) {
			ps.setNull(column++, Types.DECIMAL);
		} else {
			ps.setDouble(column++, capturedNC);		// store null if less than 0
		}
		ps.setDouble(column++, actualCreditSpread);
		ps.setDouble(column++, crmAdjustedRacPct);		// TODO maybe wrong, or may need to store more
		ps.setDouble(column++, rac);
		
		//
		// For the RoRACs, make sure they are valid numbers before they are stored.
		//
		if (Double.isNaN(preTax10RoRac)) {
			ps.setNull(column++, Types.DECIMAL);
		} else {
			ps.setDouble(column++, preTax10RoRac);
		}
		if (Double.isNaN(afterTax10RoRac)) {
			ps.setNull(column++, Types.DECIMAL);
		} else {
			ps.setDouble(column++, afterTax10RoRac);
		}
		if (Double.isNaN(preTaxLifeRoRac)) {
			ps.setNull(column++, Types.DECIMAL);
		} else {
			ps.setDouble(column++, preTaxLifeRoRac);
		}
		if (Double.isNaN(afterTaxLifeRoRac)) {
			ps.setNull(column++, Types.DECIMAL);
		} else {
			ps.setDouble(column++, afterTaxLifeRoRac);
		}
		
		ps.setDouble(column++, baseRac);
		ps.setDouble(column++, crmAdjustedRacPct);
		ps.setDouble(column++, crmFactor);
		ps.setDouble(column++, bamExpenseRate);
		
		ps.setDouble(column++, bamOpExp);
		ps.setDouble(column++, hgreOpExp);
		ps.setDouble(column++, premTaxAmt);
		ps.setDouble(column++, exciseTaxAmt);
		ps.setDouble(column++, corpTaxAmt);

		ps.setDouble(column++, corpTaxRate);
		ps.setDouble(column++, exciseTaxRate);
		ps.setDouble(column++, premTaxRate);
		
		ps.setDouble(column++, earningsRate);
		ps.setDouble(column++, discountRate);
		
		ps.setDouble(column++, targetMscRate);
		ps.setDouble(column++, actualMscRate);
		ps.setDouble(column++, hgExpenseRate);
		ps.setString(column++, method.name());
		
		ps.setDouble(column++, totalUpfront.doubleValue());
		ps.setDouble(column++, totalUpfrontPctPar);
		ps.setDouble(column++, pvfip.doubleValue());
		ps.setDouble(column++, totalBAMPrem.doubleValue());
		
		ps.setString(column++, singleRiskPrem ? "Y" : "N");
		ps.setDouble(column++, riskPremPAInstall);
		
		ps.setDouble(column++, contResvFactor);
		ps.setDouble(column++, contResvTerm);
		ps.setDouble(column++, contResvAmt.doubleValue());
		ps.setDouble(column++, contResvAmtQ.doubleValue());
		
		ps.setDouble(column++, totalUpfrontPctTDS);
		ps.setDouble(column++, snpRap);
		ps.setDouble(column++, riskPremPctPar);
		ps.setDouble(column++, riskPremPctTds);
		ps.setDouble(column++, mscPctTds);
		
		ps.setDouble(column++, modelCreditSpread);
		if (capturedC < 0.0) {
			ps.setNull(column++, Types.DECIMAL);
		} else {
			ps.setDouble(column++, capturedC);
		}
		ps.setDouble(column++, mscFundingPct);
		ps.setDouble(column++, targetMscDollars.doubleValue());
		ps.setDouble(column++, minRiskPremPctPar);
		ps.setDouble(column++, minTotalPctPar);
		ps.setDouble(column++, actualSpreadCapture);
		ps.setDouble(column++, floorRatio);

		ps.setDouble(column++, dollarsPerBond.doubleValue());
		ps.setDouble(column++, feeSnp.doubleValue());
		ps.setDouble(column++, feeCusip.doubleValue());
		ps.setDouble(column++, feeBony.doubleValue());
		ps.setDouble(column++, feeDtc.doubleValue());
		ps.setDouble(column++, feeTotal.doubleValue());
		ps.setDouble(column++, totalGrossUpfront.doubleValue());
		
		ps.setString(column++, comment);
		ps.setString(column++, newCusip ? "Y" : "N");
		ps.setDouble(column++, minRiskPrem.doubleValue());
		ps.setDouble(column++, minTotalPrem.doubleValue());
		
		ps.setString(column++, commitID);
		ps.executeUpdate();
		
		storePricingGrid(c);
		if (pricingGraph != null) {
			pricingGraph.store(c, commitID, versionDK);
		} else {
			// System.err.println("Unable to save pricing graph: it was null.");
		}
		// System.out.println(this.produceJSON());
	}
	
	/**
	 * Load up the output values from a pricing engine's results.
	 * 
	 * @param engine
	 */
	public void loadFromEngine(PricingEngine engine) {
		DealParams px = engine.getPricing();
		
		crmAdjustedRacPct = px.getCrmAdjustedRacPct();
		rac = px.getRac();
		
		preTax10RoRac = px.getPreTax10RoRac();
		afterTax10RoRac = px.getAfterTax10RoRac();
		
		preTaxLifeRoRac = px.getPreTaxLifeRoRac();
		afterTaxLifeRoRac = px.getAfterTaxLifeRoRac();
		
		snpRap = px.getSnpRap();
		
		mscPA = px.getMscPA();
		msc = px.getMsc();
		mscPctTds = px.getMscPctTds();
		actualMscRate = px.getActualMscRate();
		targetMscRate = px.getTargetMscRate();
		mscFundingPct = px.getMscFundingPct();
		targetMscDollars = px.getTargetMscDollars();
		
		riskPremPA = px.getRiskPremPA();
		riskPremPAInstall = px.getRiskPremPAInstall();
		riskPremPctPar = px.getRiskPremPctPar();
		riskPremPctTds = px.getRiskPremPctTds();
		minRiskPremPctPar = px.getMinRiskPremPctPar();
		minTotalPctPar = px.getMinTotalPctPar();
		minRiskPrem = px.getMinRiskPrem();
		minTotalPrem = px.getMinTotalPrem();
		
		upfrontPremium = px.getUpfrontPremium();
		totalPA = px.getTotalPA();
		actualSpreadCapture = px.getActualSpreadCapture();
		capturedC = px.getCapturedC();
		capturedNC = px.getCapturedNC();
		
		bamOpExp = px.getBamOpExp();
		hgreOpExp = px.getHgreOpExp();
		premTaxAmt = px.getPremTaxAmt();
		exciseTaxAmt = px.getExciseTaxAmt();
		corpTaxAmt = px.getCorpTaxAmt();
		
		totalUpfront = px.getTotalUpfront();
		totalUpfrontPctPar = px.getTotalUpfrontPctPar();
		totalUpfrontPctTDS = px.getTotalUpfrontPctTDS();
		
		pvfip = px.getPvfip();
		totalBAMPrem = px.getTotalBAMPrem();
		custSavings = px.getCustSavings();
		
		contResvAmt = px.getContResvAmt();
		contResvAmtQ = px.getContResvAmtQ();
		
		pricingGraph = px.getPricingGraph();   // TODO: OK if we don't use a copy constructor here??
		roundBps = px.isRoundBps();
		inputDK = px.getInputDK();
		versionDK = px.getVersionDK();
		
		dollarsPerBond = px.getDollarsPerBond();
		feeSnp = px.getFeeSnp();
		feeCusip = px.getFeeCusip();
		feeBony = px.getFeeBony();
		feeDtc = px.getFeeDtc();
		feeTotal = px.getFeeTotal();
		totalGrossUpfront = px.getTotalGrossUpfront();
		
		comment = new String(px.getComment());
	}
	
	public void printOutputs() {
		System.out.println("\tBAM Operating Expenses = " + BobUtil.nf.format(bamOpExp));
		System.out.println("\tHG Re Operating Expenses = " + BobUtil.nf.format(hgreOpExp));
		System.out.println("\tPremium Tax = " + BobUtil.nf.format(premTaxAmt));
		System.out.println("\tExcise Tax = " + BobUtil.nf.format(exciseTaxAmt));
		System.out.println("\tExcise Tax Rate = " + BobUtil.pf.format(exciseTaxRate));
		System.out.println("\tPremium Tax Rate = " + BobUtil.pf.format(premTaxRate));
		System.out.println("\tCorp Tax Rate = " + BobUtil.pf.format(corpTaxRate));
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject produceJSON() {
		boolean debug = false;
		System.out.println("Producing JSON");
		
		JSONObject obj = new JSONObject();
		
		obj.put("px_deal_ID", commitID);
		obj.put("px_comment", comment);
		
		obj.put("px_compute_date",  computeDate.fmt(BAMDate.FMT_ISO));
		obj.put("px_final_pricing", freeze ? "Y" : "N");
		obj.put("px_i_method_dk", method.toString());
		obj.put("px_i_years_upfront", yearsUpfront);
		obj.put("px_version", version);
		obj.put("px_version_dk", versionDK);
		obj.put("px_input", inputDK.toString());
		
		obj.put("px_o_actual_msc_rate", BobUtil.d8f.format(actualMscRate));
		obj.put("px_i_target_msc_rate", BobUtil.d8f.format(targetMscRate));
		obj.put("px_o_msc", msc.toJSONString());
		obj.put("px_o_msc_pa", BobUtil.d8f.format(mscPA));
		obj.put("px_o_msc_pct_tds", BobUtil.d8f.format(mscPctTds));
		obj.put("px_o_msc_funding_pct", BobUtil.d8f.format(mscFundingPct));
		obj.put("px_o_target_msc_dollars", targetMscDollars.toJSONString());
		
		// System.out.println("toJSON, msc = \"" + BobUtil.d2f.format(msc) + "\"");
		
		obj.put("px_o_uf_riskprem", upfrontPremium.toJSONString());
		obj.put("px_o_riskprem_pa", BobUtil.d8f.format(riskPremPA));  
		System.out.println("Building json, risk prem pa = " + BobUtil.d8f.format(riskPremPA * 10000.0));
		
		obj.put("px_o_riskprem_pa_install", BobUtil.d8f.format(riskPremPAInstall));
		obj.put("px_o_min_risk_prem_pct_par", BobUtil.d8f.format(minRiskPremPctPar));
		obj.put("px_o_min_total_pct_par", BobUtil.d8f.format(minTotalPctPar));
		obj.put("px_o_min_risk_prem_dollars", minRiskPrem.toJSONString());
		obj.put("px_o_min_total_dollars", minTotalPrem.toJSONString());
		obj.put("px_i_single_risk_prem", singleRiskPrem);
		obj.put("px_i_new_cusip", newCusip ? "Y" : "N");
		
		obj.put("CedingPct", BobUtil.d8f.format(cedingPct));
		obj.put("CedingCommission", BobUtil.d8f.format(cedingCommission));
		
		obj.put("px_i_discount_rate", BobUtil.d8f.format(discountRate));
		obj.put("px_i_earnings_rate", BobUtil.d8f.format(earningsRate));
		
		obj.put("ParInsured", parInsured.toJSONString());
		obj.put("px_i_spread_actual", BobUtil.d8f.format(actualCreditSpread));
		obj.put("px_i_spread_model", BobUtil.d8f.format(modelCreditSpread));
		
		//
		// For spread capture rates, if we computed less than 0 (probably because we solve for %TDS
		// or total upfront), send a null
		//
		obj.put("px_i_captured_nc", capturedNC < 0.0 ? "" : BobUtil.d8f.format(capturedNC));
		obj.put("px_i_captured_c", capturedC < 0.0 ? "" : BobUtil.d8f.format(capturedC));
		obj.put("px_i_floor_ratio", BobUtil.d6f.format(floorRatio));
		
		obj.put("px_i_base_rac", BobUtil.d8f.format(baseRac));
		obj.put("px_i_crm_factor", BobUtil.d6f.format(crmFactor));
		obj.put("px_o_crm_adj_rac", BobUtil.d8f.format(crmAdjustedRacPct));
		
		obj.put("px_o_rac", BobUtil.d8f.format(rac));
		obj.put("px_o_rorac_life_aftertax",BobUtil.d8f.format(afterTaxLifeRoRac));
		
		obj.put("px_o_bam_opexp", BobUtil.d2f.format(bamOpExp));
		obj.put("px_o_hgre_opexp", BobUtil.d2f.format(hgreOpExp));
		obj.put("px_o_prem_tax", BobUtil.d2f.format(premTaxAmt));
		obj.put("px_o_excise_tax", BobUtil.d2f.format(exciseTaxAmt));
		obj.put("px_o_corp_tax", BobUtil.d2f.format(corpTaxAmt));
		
		obj.put("px_i_bam_exp_rate", BobUtil.d5f.format(bamExpenseRate));
		obj.put("px_i_hgre_exp_rate", BobUtil.d5f.format(hgExpenseRate));
		obj.put("px_i_corp_tax_rate", BobUtil.d5f.format(corpTaxRate));
		obj.put("px_i_excise_tax_rate", BobUtil.d5f.format(exciseTaxRate));
		obj.put("px_i_prem_tax_rate", BobUtil.d5f.format(premTaxRate));
		
		obj.put("px_o_total_uf", totalUpfront.toJSONString());
		obj.put("px_o_total_uf_pct_par", BobUtil.d8f.format(totalUpfrontPctPar));
		obj.put("px_o_total_uf_pct_tds", BobUtil.d8f.format(totalUpfrontPctTDS));
		obj.put("px_o_total_pa", BobUtil.d8f.format(totalPA));	
		obj.put("px_o_uf_riskprem_pct_par", BobUtil.d8f.format(riskPremPctPar));
		obj.put("px_o_uf_riskprem_pct_tds", BobUtil.d8f.format(riskPremPctTds));
		
		obj.put("px_o_pvfip", pvfip.toJSONString());
		obj.put("px_o_total_bam_premium", totalBAMPrem.toJSONString());
		//obj.put("CustSaves", custSavings);
		obj.put("px_o_snp_rap", BobUtil.d5f.format(snpRap));
		//obj.put("px_o_CIB_equiv_par", BobUtil.d2f.format(cibEquivPar));
		obj.put("px_o_actual_spread_capture", BobUtil.d8f.format(actualSpreadCapture));
		
		obj.put("px_o_cont_resv_amt", contResvAmt.toJSONString());
		obj.put("px_o_cont_resv_amtQ", contResvAmtQ.toJSONString());
		obj.put("px_o_cont_resv_term", BobUtil.d5f.format(contResvTerm));
		obj.put("px_o_cont_resv_factor", BobUtil.d5f.format(contResvFactor));
		
		obj.put("px_o_dollars_per_bond", dollarsPerBond.toJSONString());
		obj.put("px_o_fee_snp", feeSnp.toJSONString());
		obj.put("px_o_fee_cusip", feeCusip.toJSONString());
		obj.put("px_o_fee_bony", feeBony.toJSONString());
		obj.put("px_o_fee_dtc", feeDtc.toJSONString());
		obj.put("px_o_fee_total", feeTotal.toJSONString());
		obj.put("px_o_total_gross_uf", totalGrossUpfront.toJSONString());
		
		//obj.put("AGMPctTds", BobUtil.d5f.format(agmPctTds));
		
		//
		// New values added.
		//
		if (debug || BobUtil.masterDebug) {
			System.out.println("Sending: " + obj.toString() + ", RiskPremPA = " + obj.get("px_o_riskprem_pa"));
		}
		
		return obj;
	}
	
	/**
	 * @return the commitID
	 */
	public String getCommitID() {
		return commitID;
	}

	/**
	 * @param commitID the commitID to set
	 */
	public void setCommitID(String commitID) {
		this.commitID = commitID;
	}

	/**
	 * @return the irr10
	 */
	public double getPreTax10RoRac() {
		return preTax10RoRac;
	}

	/**
	 * @return the irrLife
	 */
	public double getAfterTax10RoRac() {
		return afterTax10RoRac;
	}

	/**
	 * @return the parInsured
	 */
	public Money getParInsured() {
		return parInsured;
	}

	/**
	 * @return the actualMscRate
	 */
	public double getActualMscRate() {
		return actualMscRate;
	}

	/**
	 * @param actualMscRate the actualMscRate to set
	 */
	public void setActualMscRate(double actualMscRate) {
		this.actualMscRate = actualMscRate;
	}

	/**
	 * @return the targetMscRate
	 */
	public double getTargetMscRate() {
		return targetMscRate;
	}

	/**
	 * @param targetMscRate the targetMscRate to set
	 */
	public void setTargetMscRate(double targetMscRate) {
		this.targetMscRate = targetMscRate;
	}

	/**
	 * @return the msc
	 */
	public Money getMsc() {
		return msc;
	}

	/**
	 * @param msc the msc to set
	 */
	public void setMsc(Money msc) {
		this.msc = msc;
	}

	/**
	 * @return the mscPA
	 */
	public double getMscPA() {
		return mscPA;
	}

	/**
	 * @param mscPA the mscPA to set
	 */
	public void setMscPA(double mscPA) {
		this.mscPA = mscPA;
	}

	/**
	 * @return the upfrontPremium
	 */
	public Money getUpfrontPremium() {
		return upfrontPremium;
	}

	/**
	 * @param upfrontPremium the upfrontPremium to set
	 */
	public void setUpfrontPremium(Money upfrontPremium) {
		this.upfrontPremium = upfrontPremium;
	}

	/**
	 * @return the riskPremPA
	 */
	public double getRiskPremPA() {
		return riskPremPA;
	}

	/**
	 * @param riskPremPA the riskPremPA to set
	 */
	public void setRiskPremPA(double riskPremPA) {
		this.riskPremPA = riskPremPA;
	}

	/**
	 * @return the cedingPct
	 */
	public double getCedingPct() {
		return cedingPct;
	}

	/**
	 * @param cedingPct the cedingPct to set
	 */
	public void setCedingPct(double cedingPct) {
		this.cedingPct = cedingPct;
	}

	/**
	 * @return the cedingCommission
	 */
	public double getCedingCommission() {
		return cedingCommission;
	}

	/**
	 * @param cedingCommission the cedingCommission to set
	 */
	public void setCedingCommission(double cedingCommission) {
		this.cedingCommission = cedingCommission;
	}

	/**
	 * @return the discountRate
	 */
	public double getDiscountRate() {
		return discountRate;
	}

	/**
	 * @param discountRate the discountRate to set
	 */
	public void setDiscountRate(double discountRate) {
		this.discountRate = discountRate;
	}

	/**
	 * @return the earningsRate
	 */
	public double getEarningsRate() {
		return earningsRate;
	}

	/**
	 * @param earningsRate the earningsRate to set
	 */
	public void setEarningsRate(double earningsRate) {
		this.earningsRate = earningsRate;
	}

	/**
	 * @return the creditSpread
	 */
	public double getActualCreditSpread() {
		return actualCreditSpread;
	}

	/**
	 * @param creditSpread the creditSpread to set
	 */
	public void setActualCreditSpread(double creditSpread) {
		this.actualCreditSpread = creditSpread;
	}

	/**
	 * @return the modelCreditSpread
	 */
	public double getModelCreditSpread() {
		return modelCreditSpread;
	}

	/**
	 * @param modelCreditSpread the modelCreditSpread to set
	 */
	public void setModelCreditSpread(double modelCreditSpread) {
		this.modelCreditSpread = modelCreditSpread;
	}

	/**
	 * @return the mscFundingPct
	 */
	public double getMscFundingPct() {
		return mscFundingPct;
	}

	/**
	 * @param mscFundingPct the mscFundingPct to set
	 */
	public void setMscFundingPct(double mscFundingPct) {
		this.mscFundingPct = mscFundingPct;
	}

	/**
	 * @return the targetMscDollars
	 */
	public Money getTargetMscDollars() {
		return targetMscDollars;
	}

	/**
	 * @param targetMscDollars the targetMscDollars to set
	 */
	public void setTargetMscDollars(Money targetMscDollars) {
		this.targetMscDollars = targetMscDollars;
	}

	/**
	 * @return the captured
	 */
	public double getCapturedNC() {
		return capturedNC;
	}

	/**
	 * @param captured the captured to set
	 */
	public void setCapturedNC(double captured) {
		this.capturedNC = captured;
	}

	/**
	 * @return the capturedC
	 */
	public double getCapturedC() {
		return capturedC;
	}

	/**
	 * @param capturedC the capturedC to set
	 */
	public void setCapturedC(double capturedC) {
		this.capturedC = capturedC;
	}

	/**
	 * @return the bamPremium
	 */
	public double getTotalPA() {
		return totalPA;
	}

	/**
	 * @param bamPremium the bamPremium to set
	 */
	public void setTotalPA(double bamPremium) {
		this.totalPA = bamPremium;
	}

	/**
	 * @return the baseRac
	 */
	public double getBaseRac() {
		return baseRac;
	}

	/**
	 * @param baseRac the baseRac to set
	 */
	public void setBaseRac(double baseRac) {
		this.baseRac = baseRac;
	}

	/**
	 * @return the crmFactor
	 */
	public double getCrmFactor() {
		return crmFactor;
	}

	/**
	 * @param crmFactor the crmFactor to set
	 */
	public void setCrmFactor(double crmFactor) {
		this.crmFactor = crmFactor;
	}

	/**
	 * @return the crmAdjustedRacPct
	 */
	public double getCrmAdjustedRacPct() {
		return crmAdjustedRacPct;
	}

	/**
	 * @param crmAdjustedRacPct the crmAdjustedRacPct to set
	 */
	public void setCrmAdjustedRacPct(double crmAdjustedRacPct) {
		this.crmAdjustedRacPct = crmAdjustedRacPct;
	}

	/**
	 * @return the rac
	 */
	public double getRac() {
		return rac;
	}

	/**
	 * @param rac the rac to set
	 */
	public void setRac(double rac) {
		this.rac = rac;
	}

	/**
	 * @return the bamExpenseRate
	 */
	public double getBamExpenseRate() {
		return bamExpenseRate;
	}

	/**
	 * @param bamExpenseRate the bamExpenseRate to set
	 */
	public void setBamExpenseRate(double bamExpenseRate) {
		this.bamExpenseRate = bamExpenseRate;
	}

	/**
	 * @return the hgExpenseRate
	 */
	public double getHgExpenseRate() {
		return hgExpenseRate;
	}

	/**
	 * @param hgExpenseRate the hgExpenseRate to set
	 */
	public void setHgExpenseRate(double hgExpenseRate) {
		this.hgExpenseRate = hgExpenseRate;
	}
	
	/**
	 * @param parInsured the parInsured to set
	 */
	public void setParInsured(Money parInsured) {
		this.parInsured = parInsured;
	}

	/**
	 * @param afterTaxRoRac the afterTaxRoRac to set
	 */
	public void setAfterTax10RoRac(double afterTaxRoRac) {
		this.afterTax10RoRac = afterTaxRoRac;
	}

	/**
	 * @param preTaxRoRac the preTaxRoRac to set
	 */
	public void setPreTaxLifeRoRac(double preTaxLifeRoRac) {
		this.preTaxLifeRoRac = preTaxLifeRoRac;
	}

	/**
	 * @param afterTaxRoRac the afterTaxRoRac to set
	 */
	public void setAfterTaxLifeRoRac(double afterTaxRoRac) {
		this.afterTaxLifeRoRac = afterTaxRoRac;
	}

	/**
	 * @return the preTaxLifeRoRac
	 */
	public double getPreTaxLifeRoRac() {
		return preTaxLifeRoRac;
	}

	/**
	 * @return the afterTaxLifeRoRac
	 */
	public double getAfterTaxLifeRoRac() {
		return afterTaxLifeRoRac;
	}

	/**
	 * @param preTax10RoRac the preTax10RoRac to set
	 */
	public void setPreTax10RoRac(double preTax10RoRac) {
		this.preTax10RoRac = preTax10RoRac;
	}

	/**
	 * @return the bamOpExp
	 */
	public double getBamOpExp() {
		return bamOpExp;
	}

	/**
	 * @param bamOpExp the bamOpExp to set
	 */
	public void setBamOpExp(double bamOpExp) {
		this.bamOpExp = bamOpExp;
	}

	/**
	 * @return the hgreOpExp
	 */
	public double getHgreOpExp() {
		return hgreOpExp;
	}

	/**
	 * @param hgreOpExp the hgreOpExp to set
	 */
	public void setHgreOpExp(double hgreOpExp) {
		this.hgreOpExp = hgreOpExp;
	}

	/**
	 * @return the corpTaxRate
	 */
	public double getCorpTaxRate() {
		return corpTaxRate;
	}

	/**
	 * @param corpTaxRate the corpTaxRate to set
	 */
	public void setCorpTaxRate(double corpTaxRate) {
		this.corpTaxRate = corpTaxRate;
	}

	/**
	 * @return the exciseTaxRate
	 */
	public double getExciseTaxRate() {
		return exciseTaxRate;
	}

	/**
	 * @param exciseTaxRate the exciseTaxRate to set
	 */
	public void setExciseTaxRate(double exciseTaxRate) {
		this.exciseTaxRate = exciseTaxRate;
	}

	/**
	 * @return the premTaxRate
	 */
	public double getPremTaxRate() {
		return premTaxRate;
	}

	/**
	 * @param premTaxRate the premTaxRate to set
	 */
	public void setPremTaxRate(double premTaxRate) {
		this.premTaxRate = premTaxRate;
	}

	/**
	 * @return the computeDate
	 */
	public BAMDate getComputeDate() {
		return computeDate;
	}

	/**
	 * @param computeDate the computeDate to set
	 */
	public void setComputeDate(BAMDate computeDate) {
		this.computeDate = computeDate;
	}

	/**
	 * @return the freeze
	 */
	public boolean isFreeze() {
		return freeze;
	}

	/**
	 * @param freeze the freeze to set
	 */
	public void setFreeze(boolean freeze) {
		this.freeze = freeze;
	}

	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * @return the premTaxAmt
	 */
	public double getPremTaxAmt() {
		return premTaxAmt;
	}

	/**
	 * @param premTaxAmt the premTaxAmt to set
	 */
	public void setPremTaxAmt(double premTaxAmt) {
		this.premTaxAmt = premTaxAmt;
	}

	/**
	 * @return the exciseTaxAmt
	 */
	public double getExciseTaxAmt() {
		return exciseTaxAmt;
	}

	/**
	 * @param exciseTaxAmt the exciseTaxAmt to set
	 */
	public void setExciseTaxAmt(double exciseTaxAmt) {
		this.exciseTaxAmt = exciseTaxAmt;
	}

	/**
	 * @return the corpTaxAmt
	 */
	public double getCorpTaxAmt() {
		return corpTaxAmt;
	}

	/**
	 * @param corpTaxAmt the corpTaxAmt to set
	 */
	public void setCorpTaxAmt(double corpTaxAmt) {
		this.corpTaxAmt = corpTaxAmt;
	}

	/**
	 * @return the fold
	 */
	public int getYearsUpfront() {
		return yearsUpfront;
	}

	/**
	 * @param fold the fold to set
	 */
	public void setYearsUpfront(int yearsUpfront) {
		this.yearsUpfront = yearsUpfront;
	}

	/**
	 * @return the method
	 */
	public PricingMethod getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 */
	public void setMethod(PricingMethod method) {
		this.method = method;
	}
	
	
	/**
	 * @return the totalUpfront
	 */
	public Money getTotalUpfront() {
		return totalUpfront;
	}

	/**
	 * @param totalUpfront the totalUpfront to set
	 */
	public void setTotalUpfront(Money totalUpfront) {
		this.totalUpfront = totalUpfront;
	}

	/**
	 * @return the totalUpfrontPctPar
	 */
	public double getTotalUpfrontPctPar() {
		return totalUpfrontPctPar;
	}

	/**
	 * @param totalUpfrontPctPar the totalUpfrontPctPar to set
	 */
	public void setTotalUpfrontPctPar(double totalUpfrontPctPar) {
		this.totalUpfrontPctPar = totalUpfrontPctPar;
	}

	
	/**
	 * @return the pvfip
	 */
	public Money getPvfip() {
		return pvfip;
	}

	/**
	 * @param pvfip the pvfip to set
	 */
	public void setPvfip(Money pvfip) {
		this.pvfip = pvfip;
	}

	/**
	 * @return the totalBAMPrem
	 */
	public Money getTotalBAMPrem() {
		return totalBAMPrem;
	}

	/**
	 * @param totalBAMPrem the totalBAMPrem to set
	 */
	public void setTotalBAMPrem(Money totalBAMPrem) {
		this.totalBAMPrem = totalBAMPrem;
	}

	/**
	 * @return the custSavings
	 */
	public double getCustSavings() {
		return custSavings;
	}

	/**
	 * @param custSavings the custSavings to set
	 */
	public void setCustSavings(double custSavings) {
		this.custSavings = custSavings;
	}

	/**
	 * @return the riskPremPAInstall
	 */
	public double getRiskPremPAInstall() {
		return riskPremPAInstall;
	}

	/**
	 * @param riskPremPAInstall the riskPremPAInstall to set
	 */
	public void setRiskPremPAInstall(double riskPremPAInstall) {
		this.riskPremPAInstall = riskPremPAInstall;
	}

	/**
	 * @return the singleRiskPrem
	 */
	public boolean isSingleRiskPrem() {
		return singleRiskPrem;
	}

	/**
	 * @param singleRiskPrem the singleRiskPrem to set
	 */
	public void setSingleRiskPrem(boolean singleRiskPrem) {
		this.singleRiskPrem = singleRiskPrem;
	}

	/**
	 * @return the contResvAmt
	 */
	public Money getContResvAmt() {
		return contResvAmt;
	}

	/**
	 * @param contResvAmt the contResvAmt to set
	 */
	public void setContResvAmt(Money contResvAmt) {
		this.contResvAmt = contResvAmt;
	}

	/**
	 * @return the contResvAmtQ
	 */
	public Money getContResvAmtQ() {
		return contResvAmtQ;
	}

	/**
	 * @param contResvAmtQ the contResvAmtQ to set
	 */
	public void setContResvAmtQ(Money contResvAmtQ) {
		this.contResvAmtQ = contResvAmtQ;
	}

	/**
	 * @return the contResvTerm
	 */
	public double getContResvTerm() {
		return contResvTerm;
	}

	/**
	 * @param contResvTerm the contResvTerm to set
	 */
	public void setContResvTerm(double contResvTerm) {
		this.contResvTerm = contResvTerm;
	}

	/**
	 * @return the contResvFactor
	 */
	public double getContResvFactor() {
		return contResvFactor;
	}

	/**
	 * @param contResvFactor the contResvFactor to set
	 */
	public void setContResvFactor(double contResvFactor) {
		this.contResvFactor = contResvFactor;
	}

	/**
	 * @return the snpRap
	 */
	public double getSnpRap() {
		return snpRap;
	}

	/**
	 * @param snpRap the snpRap to set
	 */
	public void setSnpRap(double snpRap) {
		this.snpRap = snpRap;
	}

	/**
	 * @return the totalUpfrontPctTDS
	 */
	public double getTotalUpfrontPctTDS() {
		return totalUpfrontPctTDS;
	}

	/**
	 * @param totalUpfrontPctTDS the totalUpfrontPctTDS to set
	 */
	public void setTotalUpfrontPctTDS(double totalUpfrontPctTDS) {
		this.totalUpfrontPctTDS = totalUpfrontPctTDS;
	}
	
	/**
	 * @return the riskPremPctPar
	 */
	public double getRiskPremPctPar() {
		return riskPremPctPar;
	}

	/**
	 * @param riskPremPctPar the riskPremPctPar to set
	 */
	public void setRiskPremPctPar(double riskPremPctPar) {
		this.riskPremPctPar = riskPremPctPar;
	}

	/**
	 * @return the riskPremPctTds
	 */
	public double getRiskPremPctTds() {
		return riskPremPctTds;
	}

	/**
	 * @param riskPremPctTds the riskPremPctTds to set
	 */
	public void setRiskPremPctTds(double riskPremPctTds) {
		this.riskPremPctTds = riskPremPctTds;
	}

	/**
	 * @return the minRiskPremPctPar
	 */
	public double getMinRiskPremPctPar() {
		return minRiskPremPctPar;
	}

	/**
	 * @param minRiskPremPctPar the minRiskPremPctPar to set
	 */
	public void setMinRiskPremPctPar(double minRiskPremPctPar) {
		this.minRiskPremPctPar = minRiskPremPctPar;
	}

	/**
	 * @return the minTotalPctPar
	 */
	public double getMinTotalPctPar() {
		return minTotalPctPar;
	}

	/**
	 * @param minTotalPctPar the minTotalPctPar to set
	 */
	public void setMinTotalPctPar(double minTotalPctPar) {
		this.minTotalPctPar = minTotalPctPar;
	}

	/**
	 * @return the mscPctTds
	 */
	public double getMscPctTds() {
		return mscPctTds;
	}

	/**
	 * @param mscPctTds the mscPctTds to set
	 */
	public void setMscPctTds(double mscPctTds) {
		this.mscPctTds = mscPctTds;
	}

	/**
	 * @return the actualSpreadCapture
	 */
	public double getActualSpreadCapture() {
		return actualSpreadCapture;
	}

	/**
	 * @param actualSpreadCapture the actualSpreadCapture to set
	 */
	public void setActualSpreadCapture(double actualSpreadCapture) {
		this.actualSpreadCapture = actualSpreadCapture;
	}

	/**
	 * @return the pricingGraph
	 */
	public PricingGraph getPricingGraph() {
		return pricingGraph;
	}

	/**
	 * @param pricingGraph the pricingGraph to set
	 */
	public void setPricingGraph(PricingGraph pricingGraph) {
		this.pricingGraph = pricingGraph;
	}

	/**
	 * @return the versionDK
	 */
	public String getVersionDK() {
		return versionDK;
	}

	/**
	 * @param versionDK the versionDK to set
	 */
	public void setVersionDK(String versionDK) {
		this.versionDK = versionDK;
	}

	/**
	 * @return the roundBps
	 */
	public boolean isRoundBps() {
		return roundBps;
	}

	/**
	 * @param roundBps the roundBps to set
	 */
	public void setRoundBps(boolean roundBps) {
		this.roundBps = roundBps;
	}

	/**
	 * @return the input
	 */
	public PricingInput getInputDK() {
		return inputDK;
	}

	/**
	 * @param input the input to set
	 */
	public void setInputDK(PricingInput input) {
		this.inputDK = input;
	}

	
	/**
	 * @return the floorRatio
	 */
	public double getFloorRatio() {
		return floorRatio;
	}

	/**
	 * @param floorRatio the floorRatio to set
	 */
	public void setFloorRatio(double floorRatio) {
		this.floorRatio = floorRatio;
	}
	
	/**
	 * @return the dollarsPerBond
	 */
	public Money getDollarsPerBond() {
		return dollarsPerBond;
	}

	/**
	 * @param dollarsPerBond the dollarsPerBond to set
	 */
	public void setDollarsPerBond(Money dollarsPerBond) {
		this.dollarsPerBond = dollarsPerBond;
	}

	/**
	 * @return the feeSnp
	 */
	public Money getFeeSnp() {
		return feeSnp;
	}

	/**
	 * @param feeSnp the feeSnp to set
	 */
	public void setFeeSnp(Money feeSnp) {
		this.feeSnp = feeSnp;
	}

	/**
	 * @return the feeCusip
	 */
	public Money getFeeCusip() {
		return feeCusip;
	}

	/**
	 * @param feeCusip the feeCusip to set
	 */
	public void setFeeCusip(Money feeCusip) {
		this.feeCusip = feeCusip;
	}

	/**
	 * @return the feeBony
	 */
	public Money getFeeBony() {
		return feeBony;
	}

	/**
	 * @param feeBony the feeBony to set
	 */
	public void setFeeBony(Money feeBony) {
		this.feeBony = feeBony;
	}

	/**
	 * @return the feeDtc
	 */
	public Money getFeeDtc() {
		return feeDtc;
	}

	/**
	 * @param feeDtc the feeDtc to set
	 */
	public void setFeeDtc(Money feeDtc) {
		this.feeDtc = feeDtc;
	}

	/**
	 * @return the feeTotal
	 */
	public Money getFeeTotal() {
		return feeTotal;
	}

	/**
	 * @param feeTotal the feeTotal to set
	 */
	public void setFeeTotal(Money feeTotal) {
		this.feeTotal = feeTotal;
	}

	/**
	 * @return the totalGrossUpfront
	 */
	public Money getTotalGrossUpfront() {
		return totalGrossUpfront;
	}

	/**
	 * @param totalGrossUpfront the totalGrossUpfront to set
	 */
	public void setTotalGrossUpfront(Money totalGrossUpfront) {
		this.totalGrossUpfront = totalGrossUpfront;
	}

	
	/**
	 * @return the newCusip
	 */
	public boolean isNewCusip() {
		return newCusip;
	}

	/**
	 * @param newCusip the newCusip to set
	 */
	public void setNewCusip(boolean newCusip) {
		this.newCusip = newCusip;
	}

	/**
	 * @return the minRiskPrem
	 */
	public Money getMinRiskPrem() {
		return minRiskPrem;
	}

	/**
	 * @param minRiskPrem the minRiskPrem to set
	 */
	public void setMinRiskPrem(Money minRiskPrem) {
		this.minRiskPrem = minRiskPrem;
	}

	/**
	 * @return the minTotalPrem
	 */
	public Money getMinTotalPrem() {
		return minTotalPrem;
	}

	/**
	 * @param minTotalPrem the minTotalPrem to set
	 */
	public void setMinTotalPrem(Money minTotalPrem) {
		this.minTotalPrem = minTotalPrem;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Convert a string into the appropriate PricingMethod enum value.
	 * 
	 * @param code
	 * @return
	 */
	public static PricingMethod codeToPricingMethod(String code) {
		if (code != null && code.equals("ALLUPFRONT")) {
			return PricingMethod.ALLUPFRONT;
		} else if (code != null && code.equals("SECONDARY")) {
			return PricingMethod.SECONDARY;
		} else {
			return PricingMethod.ORIGINAL;
		}
	}
	
	/**
	 * return true if we have a pricing with this commit/version already, false if not.
	 * 
	 * @param c
	 * @return
	 */
	private boolean versionExists(Connection c) throws SQLException {
		String query = " select count(*) 'count' from deal_pricing where px_deal_ID = ? and px_version = ? ";
		PreparedStatement ps = c.prepareStatement(query);
		int col = 1;
		ps.setString(col++, commitID);
		ps.setInt(col++, version);

		ResultSet rs = ps.executeQuery();
		rs.first();
		
		int count = rs.getInt("count");
		System.out.println("Count of verson " + commitID + "/" + version + " = " + count);
		return (count > 0);
		
	}

	/**
	 * Compute or lookup all the various reference values used in the pricing process, including
	 * creditSpread and captured % for the sector/rating, and CRM factor.
	 * 
	 * We also look for 0 values for expense rate, earning rate and others to protect ourselves
	 * from bad test data.  In a live system, these values being 0 would cause a pricing exception.
	 * 
	 * Maturity is also used to scale RAC if we're doing that.
	 * 
	 */
	public void fillInMissingValues(String bamRatingID, String snpRatingID, String sectorID, BAMDate datedDate, BAMDate finalMat) throws ReferenceDataException {

		double term = BondMath.days30360(datedDate, finalMat) / 360.0;
		
		if (method == null) {
			setMethod(PricingMethod.ORIGINAL);
		}

		if (computeDate == null || !computeDate.isValid()) {
			computeDate = new BAMDate(datedDate);
			//System.out.println("*** Compute date was invalid, setting to " + computeDate);
		}
		
		if (yearsUpfront <= 0) {
			setYearsUpfront(10);
			//System.out.println("*** Years Upfront was 0, setting to " + BobUtil.idf.format(getYearsUpfront()) + " ***");
		}

		if (targetMscRate == 0.0) {
			setTargetMscRate(BobConstants.computeMSC(sectorID, bamRatingID, term));
			//System.out.println("*** Target MSC Rate was 0, setting to " + getTargetMscRate());
		}
		
		if (modelCreditSpread == 0.0) {
			setModelCreditSpread(BobConstants.lookupCreditSpread(sectorID, snpRatingID));
			//System.out.println("*** Model spread was 0, setting to " 
			//		+ BobUtil.d5f.format(getModelCreditSpread()) + " ***");
		}
		
		if (actualCreditSpread == 0.0) {
			setActualCreditSpread(getModelCreditSpread());
			//System.out.println("*** Market spread was 0, setting to model ***");
		}
		
		//if (capturedNC == 0.0) {
		//	setCapturedNC(BobConstants.lookupSpreadCaptured(sectorID, snpRatingID));
		//	System.out.println("*** Non-Call Spread Captured was 0, setting to " + BobUtil.d5f.format(getCapturedNC()) + " ***");
		//}
		//if (capturedC == 0.0) {
		//	setCapturedC(getCapturedNC());
		//	System.out.println("*** Callable Spread Captured was 0, setting to " + BobUtil.d5f.format(getCapturedC()) + " ***");
		//}
		
		if (bamExpenseRate == 0.0) {
			setBamExpenseRate(0.20);
			//System.out.println("*** BAM Expense Rate was 0, setting to 20% of UFRP ***");
		}
		if (hgExpenseRate == 0.0) {
			setHgExpenseRate(0.02);
			//System.out.println("*** HGRe Expense Rate was 0, setting to 2% of UFRP ***");
		}
		if (earningsRate == 0.0) {
			setEarningsRate(0.02);
			//System.out.println("*** Earnings Rate was 0, setting to 2% ***");
		}
		if (discountRate == 0.0) {
			setDiscountRate(0.03);
			//System.out.println("*** Earnings Rate was 0, setting to 3% ***");
		}
		if (exciseTaxRate == 0.0) {
			setExciseTaxRate(0.01);
			//System.out.println("*** Excise Tax Rate was 0, setting to 1% ***");
		}
		if (premTaxRate == 0.0) {
			setPremTaxRate(0.02);
			//System.out.println("*** Premium Tax Rate was 0, setting to 2% ***");
		}
		if (corpTaxRate == 0.0) {
			setCorpTaxRate(0.35);
			//System.out.println("*** Corporate Tax Rate was 0, setting to 35% ***");
		}

		//
		// If we change either the base rac or the factor, make sure we recompute the adjusted rac.
		//
		// TODO FUDGE always reload the RAC?
		//
		boolean recomputeAdjRac = false;
		if (baseRac == 0.0 || BobUtil.ALWAYS_NEW_RAC) {
			recomputeAdjRac = true;
			setBaseRac(BobConstants.lookupBaseRAC(sectorID, bamRatingID, term));
			//System.out.println("*** RAC: looking up and setting to " + BobUtil.dbf.format(getBaseRac()));
		}

		if (crmFactor == 0.0) {
			recomputeAdjRac = true;
			setCrmFactor(1.0);
			//System.out.println("*** CRM Factor was 0, setting to " + getCrmFactor());
		}
		
		if (crmAdjustedRacPct == 0.0 || recomputeAdjRac) { 
			setCrmAdjustedRacPct(getBaseRac() * getCrmFactor());
			//System.out.println("*** RAC percent computed = " + BobUtil.dbf.format(getCrmAdjustedRacPct()));
		}

		if (contResvFactor == 0.0) {
			setContResvFactor(BobConstants.lookupContResvFactor(sectorID, snpRatingID));
			//System.out.println("*** Cont Resv Factor was 0, setting to " + getContResvFactor());
		}
		
		if (contResvTerm == 0.0) {
			setContResvTerm(BobConstants.lookupContResvTerm(sectorID, snpRatingID));
			//System.out.println("*** Cont Resv Term was 0, setting to " + getContResvTerm());
		}
	}
	
	/**
	 * To compute this table, we'll need:
	 * 	 totalUpfrontPctTDS
	 *   totalPA
	 *   actualCreditSpread
	 *   snpCapCharge (which we'll compute back out)
	 *  
	 * @param c
	 * @param totalUpfrontPctTDS
	 */
	private void storePricingGrid(Connection c) throws SQLException {
		double snpCapCharge = totalUpfrontPctTDS / snpRap;
		
		double midPoint = totalUpfrontPctTDS * 10000.0;
		double highEnd = midPoint + 15.0;
		double lowEnd = Math.max(0.0, midPoint - 15.0);
		
		double upfront = lowEnd;
		int row = 1;
		while (upfront < highEnd) {
			double annual = (totalPA / totalUpfrontPctTDS) * upfront;
			double pctSpread = (annual / 10000.0) / actualCreditSpread;
			double snp = (upfront / 10000.0) / snpCapCharge;
			
			// System.out.println(upfront + "\t" + BobUtil.d2f.format(annual) + "\t" + BobUtil.pf.format(pctSpread) + "\t" + BobUtil.pf.format(snp));
			String key = (Math.abs(upfront - midPoint) < 0.001) ? "Y" : "N";
			storePricingGridRow(c, row, key, upfront, annual, pctSpread, snp);
			upfront += 1.0;
			row += 1;
		}
	}
	
	/**
	 * Store one row in the pricing grid for this version of this deal.
	 * 
	 * @param c
	 * @param upfrontBps
	 * @param annualBps
	 * @param pctSpread
	 * @param snpRap
	 * @throws SQLException
	 */
	private void storePricingGridRow(Connection c, int row, String key, double upfrontBps, double annualBps, double pctSpread, double snpRap) throws SQLException {
		String query = "replace into price_sheet_grid "
				+ " set psg_deal_ID = ?, psg_version_dk = ?, psg_key = ?, psg_row = ?, "
				+ " psg_upfront_bps = ?, psg_annual_bps = ?, psg_pct_spread = ?, psg_snp_rap = ? ";
		
		PreparedStatement ps = c.prepareStatement(query);
		
		int column = 1;
		ps.setString(column++, commitID);
		ps.setString(column++, versionDK);
		ps.setString(column++, key);
		ps.setInt(column++, row);
		ps.setDouble(column++, upfrontBps);
		ps.setDouble(column++, annualBps);
		ps.setDouble(column++, pctSpread);
		ps.setDouble(column++,  snpRap);
		
		ps.executeUpdate();
	}
}


