package com.marketdata.marvin.midtier.runtime.lifecycle;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.marketdata.marvin.midtier.domain.core.ProcessingRequest;
import com.marketdata.marvin.midtier.domain.rate.Attribute;
import com.marketdata.marvin.midtier.domain.rate.AttributeUpdate;
import com.marketdata.marvin.midtier.domain.rate.DataType;
import com.marketdata.marvin.midtier.domain.rate.RateInstance;
import com.marketdata.marvin.midtier.domain.rate.RateStatus;
import com.marketdata.marvin.midtier.domain.rate.RateType;
import com.marketdata.marvin.midtier.framework.core.ItemReference;

public class TestManualAdjustments extends BaseRateEventsTC {

	private static final Log LOG = LogFactory.getLog(TestManualAdjustments.class);

	@Before
	public void before() throws Throwable {
		super.doBefore();

	}

	@After
	public void after() {
		doAfter();
	}

	List<AttributeUpdate> randomUpdates(final RateInstance rateInstance) {

		final List<AttributeUpdate> updates = new ArrayList<AttributeUpdate>();

		for (final Attribute att : rateInstance.getAttributes()) {
			final AttributeUpdate au = new AttributeUpdate(att);
			if (DataType.DOUBLE.equals(att.getDataType())) {
				au.setNewValue("" + (Math.random() * 2000.0));
			}
			updates.add(au);
		}

		return updates;
	}

	/**
	 * Basic test of a non-ready reliable rate for manual adjustment.
	 */
	@Test
	public void testReliableManualAdjustment() throws Throwable {

		this.setupRawReliableData();

		RateInstance reliableRate = ratesService.find(RATE_REL1, RateType.RELIABLE);
		final List<RateInstance> raws = ratesService.findRawSourceRates(reliableRate);
		RateInstance raw0 = raws.get(0);

		final ProcessingRequest pr = this.oe.manualAdjustment(reliableRate, randomUpdates(reliableRate), "UT", true, false);

		// allow things time to process
		sleep(2000);

		// re-find as Version counters can change in OM
		reliableRate = find(reliableRate);

		Assert.assertEquals(RateStatus.READY_MANUAL_ADJUSTED, reliableRate.getRateStatus());
		Assert.assertEquals(1, completedCounter);
		Assert.assertTrue(completedRates.contains(reliableRate));

		raw0 = find(raw0);
		Assert.assertEquals(RateStatus.IGNORED, raw0.getRateStatus());
	}

	/**
	 * Basic test of a non-ready raw rate for manual adjustment.
	 */
	@Test
	public void testRawManualAdjustment() throws Throwable {

		this.setupRawReliableData();

		final RateInstance reliableRate = ratesService.find(RATE_REL1, RateType.RELIABLE);
		final List<RateInstance> raws = ratesService.findRawSourceRates(reliableRate);
		RateInstance raw0 = raws.get(0);

		final ProcessingRequest pr = this.oe.manualAdjustment(raw0, randomUpdates(raw0), "UT", false, false);

		// allow things time to process
		sleep(1000);

		raw0 = find(raw0);
		Assert.assertEquals(RateStatus.READY_MANUAL_ADJUSTED, raw0.getRateStatus());
		Assert.assertEquals(1, completedCounter);

		assertSuccessful(pr);
	}

	@Test
	public void testRawManualAdjustmentPropagateUp() throws Throwable {

		this.setupRawReliableData();

		RateInstance reliableRate = ratesService.find(RATE_REL1, RateType.RELIABLE);
		final List<RateInstance> raws = ratesService.findRawSourceRates(reliableRate);
		RateInstance raw0 = raws.get(0);

		final ProcessingRequest pr = this.oe.manualAdjustment(raw0, randomUpdates(raw0), "UT", false, true);

		// allow things time to process
		sleep(1000);

		// re-find as Version counters can change in OM
		reliableRate = find(reliableRate);

		Assert.assertEquals(RateStatus.READY, reliableRate.getRateStatus());
		Assert.assertTrue(completedRates.contains(reliableRate));

		raw0 = find(raw0);
		Assert.assertEquals(RateStatus.READY_MANUAL_ADJUSTED, raw0.getRateStatus());
		Assert.assertEquals(2, completedCounter);

		assertSuccessful(pr);
	}

	/**
	 * Test where 1 raw is sourced normally, other fails causing reliable to fail, 
	 * and sceanrio is fixed by manual adjust of error'd raw
	 */
	@Test
	public void testManualAdjustmentFixRaw() throws Throwable {

		this.setupRawReliableData();

		RateInstance reliableRate = ratesService.find(RATE_REL2, RateType.RELIABLE);
		final List<RateInstance> raws = ratesService.findRawSourceRates(reliableRate);
		final RateInstance raw0 = raws.get(0);
		RateInstance raw1 = raws.get(1);

		final ProcessingRequest pr = this.oe.userInitiatedSourceRate(reliableRate.getRateInstanceId(), false, false);

		// allow things time to process
		sleep(1000);

		fakeSourcing(raw0);
		fakeSourcing(raw1, "RawData Not avaliable"); // will put into error

		sleep(1000);

		// now reliable should complete
		Assert.assertTrue(completedRates.contains(reliableRate));

		assertReady(raw0);
		assertException(reliableRate);
		assertException(raw1);

		assertNotSuccessful(pr);

		resetCompleted();

		raw1 = find(raw1);

		final ItemReference ir = new ItemReference(raw1);

		Mockito.reset(this.oe.errrorService);

		// now manually adjust raw1
		final ProcessingRequest pr2 = this.oe.manualAdjustment(raw1, randomUpdates(raw1), "UT", false, false);

		sleep(1000);

		reliableRate = find(reliableRate);

		assertReady(reliableRate);
		Assert.assertEquals(2, completedCounter);
		Assert.assertTrue(completedRates.contains(reliableRate));

		raw1 = find(raw1);
		Assert.assertEquals(RateStatus.READY_MANUAL_ADJUSTED, raw1.getRateStatus());

		assertSuccessful(pr2);

		// errors were cleared. TODO - proper mock error service that can test what errors remain etc (integrate in RLC-160)
		verify(oe.errrorService, times(2)).removeOldErrors(eq(ir));
	}

	/**
	 * Confirm Derived is *not* sourced when underlyer becomes ready via Manual adjustment
	 * @throws Throwable 
	 */
	@Test
	public void testManualAdjustmentOfUnderlyerNotForcing() throws Throwable {

		this.setupDerivedReliableData();

		RateInstance reliableRate = find(RATE_REL3, RateType.RELIABLE);
		final RateInstance der6 = find(RATE_DER6, RateType.DERIVED);

		final ProcessingRequest pr = this.oe.manualAdjustment(reliableRate, randomUpdates(reliableRate), "UT", false, false);

		// now wait for process
		sleep(2000);

		reliableRate = find(reliableRate);

		// check reliable is completed
		Assert.assertTrue(completedRates.contains(reliableRate));

		assertStatus(RateStatus.READY_MANUAL_ADJUSTED, reliableRate);

		assertInit(der6);

		assertSuccessful(pr);
	}

	/**
	 * Confirm Derived is sourced when underlyer becomes ready via Manual adjustment
	 * @throws Throwable 
	 */
	@Test
	public void testManualAdjustmentOfUnderlyerForcingUp() throws Throwable {

		this.setupDerivedReliableData();

		RateInstance reliableRate = find(RATE_REL3, RateType.RELIABLE);
		final RateInstance der6 = find(RATE_DER6, RateType.DERIVED);

		final ProcessingRequest pr = this.oe.manualAdjustment(reliableRate, randomUpdates(reliableRate), "UT", false, true);

		// now wait for process
		sleep(2000);

		verify(oe.calculationService, times(1)).calculateDerivedRate(Matchers.any(RateInstance.class));
		oe.rateLifecycleOrchestrator.derivedRateProcessor.rateCalculated(find(der6));

		reliableRate = find(reliableRate);

		// check reliable is completed
		Assert.assertTrue(completedRates.contains(reliableRate));

		assertStatus(RateStatus.READY_MANUAL_ADJUSTED, reliableRate);

		assertReady(der6);

		assertSuccessful(pr);
	}

	/**
	 * basic test with list of adjusted items
	 * @throws Throwable 
	 */
	@Test
	public void testManualAdjustmentReliablesList() throws Throwable {

		this.setupRawReliableData();

		final List<RateInstance> adjustedInstances = new ArrayList<RateInstance>();

		final RateInstance rel1 = this.findForUpdate(RATE_REL1, RateType.RELIABLE);
		final List<RateInstance> rel1Raws = this.ratesService.findRawSourceRates(rel1);

		final RateInstance rel2 = this.find(RATE_REL2, RateType.RELIABLE);

		final String rel1Bid = "2.0";
		final String rel1Ask = "3.0";

		rel1.getAttribute("BID").setValue(rel1Bid);
		rel1.getAttribute("ASK").setValue(rel1Ask);

		adjustedInstances.add(rel1);

		final List<RateInstance> rel2Raws = this.ratesService.findRawSourceRates(rel2);
		adjustedInstances.add(findForUpdate(rel2Raws.get(0)));

		final ProcessingRequest pr = this.oe.manualAdjustment(adjustedInstances, false, true);

		// this should cause rel1 to set to manually Adjusted, its raws to Ignored &
		// rel2.raw0 to be set to adjusted, and rel2.raw1 to sourcing, and rel2 to sourcing
		sleep(1000);

		assertStatus(RateStatus.READY_MANUAL_ADJUSTED, rel1);
		assertStatus(RateStatus.IGNORED, rel1Raws.get(0));

		verify(oe.rawDataService, times(1)).sourceRateInstance(Matchers.any(RateInstance.class));

		assertStatus(RateStatus.SOURCING, rel2);
		assertStatus(RateStatus.SOURCING, rel2Raws.get(1));

		fakeSourcing(find(rel2Raws.get(1)));

		sleep(1000);

		assertCompletedReady(rel2);
		assertCompletedReady(rel2Raws.get(1));

		assertSuccessful(pr);

		// check Rel1 values
		final RateInstance rel1Latest = find(rel1);
		Assert.assertEquals(rel1Bid, rel1Latest.getAttribute("BID").getValue());
		Assert.assertEquals(rel1Ask, rel1Latest.getAttribute("ASK").getValue());

	}

	// test with cleansing rules issues (individual & list adjust)

	// test where some items in list are out of date

	// test MA to trigger re-source dependency

	/**
	 * Basic test where workflow notify fails (must put rate into exception)
	 */
	@Test
	public void testManualAdjustmentWorkflowFailure() throws Throwable {

		this.setupRawReliableData();

		RateInstance reliableRate = ratesService.find(RATE_REL1, RateType.RELIABLE);
		final List<RateInstance> raws = ratesService.findRawSourceRates(reliableRate);
		final RateInstance raw0 = raws.get(0);

		final String reason = "UT";

		Mockito.when(
				oe.runtimeWorkflowService.requestManualAdjustment(eq(reliableRate), Mockito.anyList(), eq(reason),
						Mockito.anyLong())).thenThrow(new Exception("Workflow request failed!"));

		final ProcessingRequest pr = this.oe.manualAdjustment(reliableRate, randomUpdates(reliableRate), reason, true, false);

		// allow things time to process
		sleep(2000);

		// re-find as Version counters can change in OM
		reliableRate = find(reliableRate);

		assertNotSuccessful(pr);

		Assert.assertEquals(RateStatus.EXCEPTION, reliableRate.getRateStatus());
		Assert.assertTrue(completedRates.contains(reliableRate));

		Mockito.reset(oe.runtimeWorkflowService);
	}
}

