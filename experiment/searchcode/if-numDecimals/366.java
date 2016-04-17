/**
 *
 * Copyright (C) FACTORIA ETSIA S.L.
 * All Rights Reserved.
 * www.factoriaetsia.com
 *
 */
package com.comparadorad.bet.comparer.synchro.securebet.writer;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.comparadorad.bet.comparer.communication.surebets.send.ISendSureBetEmail;
import com.comparadorad.bet.comparer.model.bet.bean.RtBet;
import com.comparadorad.bet.comparer.model.bet.bean.RtMatch;
import com.comparadorad.bet.comparer.model.config.bean.CfgBetType;
import com.comparadorad.bet.comparer.model.securebet.bean.HistoricInfo.Cause;
import com.comparadorad.bet.comparer.model.securebet.bean.InfoMatch;
import com.comparadorad.bet.comparer.model.securebet.bean.SecureBeanBenefit;
import com.comparadorad.bet.comparer.model.securebet.bean.SecureBeanData;
import com.comparadorad.bet.comparer.model.securebet.exception.SureBetNotFoundException;
import com.comparadorad.bet.comparer.model.securebet.repository.SecureBetHistoricRepository;
import com.comparadorad.bet.comparer.model.securebet.repository.SecureBetRepository;
import com.comparadorad.bet.comparer.synchro.securebet.core.beans.SureBetsMarket;
import com.comparadorad.bet.comparer.synchro.securebet.core.beans.SureBetsMatch;
import com.comparadorad.bet.comparer.util.logger.core.ComparerWrapperLog;

/**
 * The Class SecureBetCalculateWriter.
 * 
 * @param <T>
 *            the generic type
 */
@Service
public final class SureBetWriter<T extends SureBetsMatch> {

	/** The Constant NUM_DECIMALS. */
	private static final Integer NUM_DECIMALS = 2;

	/** The LOG. */
	@Inject
	private ComparerWrapperLog LOG;;

	/** The secure bet historic repository. */
	@Inject
	private SecureBetHistoricRepository secureBetHistoricRepository;

	/** The secure bet service. */
	@Inject
	private SecureBetRepository secureBetRepository;

	/** The send sure bet email. */
	@Inject
	private ISendSureBetEmail sendSureBetEmail;

	/**
	 * Associate bet type to bets.
	 * 
	 * @param bets
	 *            the bets
	 * @param pBetType
	 *            the bet type
	 */
	void associateBetTypeToBets(Set<RtBet> bets, CfgBetType pBetType) {
		for (RtBet bet : bets) {
			bet.setBetType(pBetType);
		}
	}

	/**
	 * Check repository exists.
	 * 
	 * @param secureBeanData
	 *            the secure bean data
	 * @return the secure bean data
	 * @throws SureBetNotFoundException
	 *             the sure bet not found exception
	 */
	private SecureBeanData checkRepositoryExists(SecureBeanData secureBeanData)
			throws SureBetNotFoundException {
		List<SecureBeanData> result = secureBetRepository.exist(secureBeanData);
		if (result.isEmpty()) {
			throw new SureBetNotFoundException();
		}
		return result.get(0);
	}

	/**
	 * Convert rt match to info match.
	 * 
	 * @param match
	 *            the match
	 * @return the info match
	 */
	private InfoMatch convertRtMatchToInfoMatch(RtMatch match) {
		InfoMatch infoMatch = new InfoMatch();

		infoMatch.setCompetition(match.getCompetition());
		infoMatch.setDate(match.getMatchId().getStartDate()
				.getZeroGmtMatchDate());
		infoMatch.setName(match.getI18n());
		infoMatch.setObjectId(match.getObjectId());

		return infoMatch;
	}

	/**
	 * Convert secure bet bean to secure bean data.
	 * 
	 * @param secureBetBean
	 *            the secure bet bean
	 * @return the secure bean data
	 */
	private SecureBeanData convertSecureBetBeanToSecureBeanData(
			SureBetsMarket secureBetBean) {
		SecureBeanData result = new SecureBeanData();
		result.setBetType(secureBetBean.getBetType());
		InfoMatch infoMatch = convertRtMatchToInfoMatch(secureBetBean
				.getMatch());
		result.setInfoMatch(infoMatch);
		result.setModificationDate(secureBetBean.getModificationDate());
		result.setCreateDate(secureBetBean.getCreateDate());
		result.setBetTypeEvent(secureBetBean.getBetTypeEvent());
		return result;
	}

	/**
	 * Round benefit.
	 * 
	 * @param benefit
	 *            the benefit
	 * @param numDecimals
	 *            the num decimals
	 * @return the secure bean benefit
	 */
	SecureBeanBenefit roundBenefit(Double benefit, Integer numDecimals) {
		Double factor = Math.pow(10, numDecimals);
		Double result = Math.round(benefit * factor) / factor;
		return new SecureBeanBenefit(result);
	}

	/**
	 * Save.
	 * 
	 * @param secureBeanData
	 *            the secure bean data
	 */
	private void save(SecureBeanData secureBeanData) {
		try {
			LOG.info(Thread.currentThread(),
					"---------------------------------------");
			SecureBeanData surebetDB = checkRepositoryExists(secureBeanData);
			LOG.info(Thread.currentThread(),
					"La apuesta segura ya existe, se procede a actualizar");
			updateSureBets(secureBeanData, surebetDB);
			LOG.debug(Thread.currentThread(),
					"Se ha actualizado la apuesta segura.");
		} catch (SureBetNotFoundException e) {
			LOG.info(Thread.currentThread(),
					"La apuesta segura no existe, se procede a insertar");
			secureBetRepository.save(secureBeanData);
			LOG.debug(Thread.currentThread(),
					"Se ha insertado la apuesta segura.");
			try {
				sendSureBetEmail.send(secureBeanData);
			} catch (Exception rabbit) {
				LOG.error(Thread.currentThread(),
						"Se ha producido un error con la cola de env�o de surebet.");
			}
		} finally {
			LOG.info(Thread.currentThread(),
					"---------------------------------------");
		}

	}

	/**
	 * Update sure bets.
	 * 
	 * @param secureBeanData
	 *            the secure bean data
	 * @param surebetDB
	 *            the surebet db
	 */
	private void updateSureBets(SecureBeanData secureBeanData,
			SecureBeanData surebetDB) {
		Boolean update = Boolean.FALSE;
		for (RtBet bet : secureBeanData.getBets()) {
			if (!surebetDB.containEqualsBet(bet)) {
				update = Boolean.TRUE;
				break;
			}
		}
		if (update) {
			secureBetRepository.update(surebetDB, secureBeanData.getBets(),
					secureBeanData.getBenefit());
			secureBetHistoricRepository
					.saveWithOutValidation(surebetDB
							.convertToHistoric(Cause.CambioDeCuotas_SigueSiendoSureBet));
		}
	}

	/**
	 * Write.
	 * 
	 * @param calculateSecureBetBean
	 *            the calculate secure bet bean {@inheritDoc}
	 */
	public void write(SureBetsMatch sureBetsMatch) {
		SecureBeanData beanData;
		Map<SecureBeanBenefit, List<RtBet>> secureBetAgrupation;
		for (SureBetsMarket sureBetsMarket : sureBetsMatch.getSureBetsMarket()) {
			secureBetAgrupation = sureBetsMarket.getSecureBetAgrupation();
			if (secureBetAgrupation != null) {
				for (SecureBeanBenefit secureBeanBenefit : secureBetAgrupation
						.keySet()) {
					beanData = convertSecureBetBeanToSecureBeanData(sureBetsMarket);
					beanData.setBenefit(roundBenefit(
							secureBeanBenefit.getValue(), NUM_DECIMALS));
					beanData.setBets(new HashSet<RtBet>(secureBetAgrupation
							.get(secureBeanBenefit)));
					associateBetTypeToBets(beanData.getBets(),
							beanData.getBetType());
					save(beanData);
				}
			}
		}
	}

}

