package com.marketdata.marvin.midtier.runtime.lifecycle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.marketdata.marvin.midtier.domain.rate.RateInstance;
import com.marketdata.marvin.midtier.domain.rate.RateStatus;
import com.marketdata.marvin.midtier.domain.rate.RateType;
import com.marketdata.marvin.midtier.domain.rate.filters.RateTypeFilter;
import com.marketdata.marvin.midtier.domain.sourcing.SourceGroupInstance;
import com.marketdata.marvin.midtier.domain.sourcing.SourceGroupStatus;
import com.marketdata.marvin.midtier.framework.ContextHelper;
import com.marketdata.marvin.midtier.framework.core.filters.FilterUtil;
import com.marketdata.marvin.midtier.framework.util.MemoryStats;
import com.marketdata.marvin.midtier.runtime.BaseRuntimeIT;
import com.marketdata.marvin.midtier.runtime.data.group.ISourceGroupRuntimeService;
import com.marketdata.marvin.runtime.api.data.rate.IRatesService;
import com.marketdata.marvin.runtime.api.lifecycle.IOrchestrationEngine;
import com.marketdata.marvin.runtime.api.lifecycle.IRateCompletionListener;
import com.marketdata.marvin.runtime.api.lifecycle.ISourceGroupCompletionListener;
import com.marketdata.marvin.runtime.api.lifecycle.RateInstanceLifecycleInfo;
import com.marketdata.marvin.runtime.api.lifecycle.SourceGroupInstanceLifecycleInfo;
import com.marketdata.marvin.runtime.api.lifecycle.StartupMode;
import com.marketdata.marvin.runtime.api.lifecycle.SystemStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {ContextHelper.RUNTIME_CONTEXT_TEST_ALL_IN_ONE})
@DirtiesContext
public class PieChartUtil extends BaseRuntimeIT implements ISourceGroupCompletionListener {

	public static final Log LOG = LogFactory.getLog(PieChartUtil.class);

	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ISourceGroupRuntimeService sourceGroupRuntimeService;

	@Autowired
	private IOrchestrationEngine orchestrationEngine;

	@Autowired
	private IRatesService ratesService;

	@Autowired
	private AuthenticationManager authenticationManager;

	protected Long sourceGroupId = 10000l;

	@Autowired
	public void setJdbcTemplate(final DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Test
	public void testGetAllRates() throws Throwable {

		final Authentication authRequest = new UsernamePasswordAuthenticationToken("SUPER", "PASSWORD");

		final Authentication auth = authenticationManager.authenticate(authRequest);
		SecurityContextHolder.getContext().setAuthentication(auth);
		this.systemStateService.setSystemStatus(SystemStatus.MARKET_CLOSED);

		this.orchestrationEngine.start(StartupMode.STANDARD);

		System.out.println("rates:" + ratesService.findAll().size());

		final Collection<RateInstance> allRates = ratesService.findAll();

		final Map<RateStatus, Integer> ratesStatusesCount = new HashMap<RateStatus, Integer>();
		for (final RateInstance rateInstance : allRates) {
			final RateStatus rateStatus = rateInstance.getRateStatus();

			Integer existingCount = ratesStatusesCount.get(rateStatus);

			if (existingCount == null) {
				ratesStatusesCount.put(rateStatus, 1);
			} else {
				ratesStatusesCount.put(rateStatus, ++existingCount);
			}
		}

		final DefaultPieDataset pieDataset = new DefaultPieDataset();
		final JFreeChart chart = ChartFactory.createPieChart3D("Rates statuses snapshot", pieDataset, true, true, true);
		final PiePlot3D plot = (PiePlot3D) chart.getPlot();
		plot.setForegroundAlpha(0.5f);

		for (final RateStatus rateStatus : ratesStatusesCount.keySet()) {

			final int statusCount = ratesStatusesCount.get(rateStatus);
			pieDataset.setValue(rateStatus.toString() + "(" + statusCount + ")", statusCount);
			if (rateStatus.toString().startsWith("INIT")) {
				plot.setSectionPaint("INIT", new Color(30, 50, 215));
			}
		}

		plot.setSectionPaint("READY", new Color(0, 255, 25));
		plot.setSectionPaint("READY_RAW_IGNORED", new Color(185, 255, 190));
		plot.setSectionPaint("SOURCING_EXCEPTION", new Color(255, 0, 0));
		plot.setSectionPaint("CLEANSING_VIOLATION", new Color(255, 0, 235));
		final BufferedImage bi = chart.createBufferedImage(500, 300, BufferedImage.TRANSLUCENT, null);
		final byte[] bytes = EncoderUtil.encode(bi, ImageFormat.PNG, true);
		final FileOutputStream out = new FileOutputStream("temp/generateRatesPieChart.png");
		IOUtils.write(bytes, out);
		IOUtils.closeQuietly(out);

	}

	// @Test
	public void testGroupSource() throws Throwable {

		final Authentication authRequest = new UsernamePasswordAuthenticationToken("SUPER", "PASSWORD");
		// SystemParamsUtil.getSystemUser(),
		// SystemParamsUtil.getSystemPassword());

		final Authentication auth = authenticationManager.authenticate(authRequest);
		SecurityContextHolder.getContext().setAuthentication(auth);

		this.setup(true);

		// so we get SG notifications
		this.orchestrationEngine.addSourceGroupCompletionListener(this);

		final long startTime = System.currentTimeMillis();

		SourceGroupInstance sgi = sourceGroupRuntimeService.findById(sourceGroupId);
		final List<RateInstance> members = this.sourceGroupRuntimeService.getMembers(sgi);
		final List<RateInstance> raws = FilterUtil.filter(members, new RateTypeFilter(RateType.RAW));
		members.removeAll(raws);
		final int expectedMin = members.size();

		LOG.info("Starting source of SourceGroup id [" + sgi.getLabel() + "], expecting min of [" + expectedMin
				+ "] rates to complete");

		orchestrationEngine.userInitiatedSourceGroup(this.sourceGroupId, true);

		final long initTime = System.currentTimeMillis() - startTime;

		final Long sleepTime = (expectedMin * 2l) * 100l;// Long.parseLong(System.getProperty("sleepTime", "2500000"));

		final long remainingSleepTime = sleepTime - initTime;

		LOG.info(" Only sleeping for [" + remainingSleepTime + "]. Wait time was [" + sleepTime + "], but took [" + initTime
				+ "]ms to init sourcing.");

		final InfoThread infoThread = new InfoThread(5000, this.ratesService, this.orchestrationEngine, expectedMin);

		new Thread(infoThread).start();

		long waitTime = 0;

		if (remainingSleepTime > 0) {
			waitTime = this.wait(this.readyFlag, remainingSleepTime);
		}

		final long totalSourcingTime = (initTime + waitTime);

		infoThread.stop = true;
		infoThread.doOutput();

		LOG.info("Sourcing Time up/completed. Total time [" + totalSourcingTime + "], timeout - " + !this.readyFlag.flag);

		final Integer readyCount = count();

		printMemoryInfo();

		LOG.info("Time per RI [" + (totalSourcingTime / (double) readyCount) + "]ms");

		sgi = this.sourceGroupRuntimeService.findById(sgi.getId());

		Assert.assertEquals(SourceGroupStatus.READY, sgi.getStatus());

		// this.orchestrationEngine.doCloseMarket();
		//
		// Assert.assertTrue("Rates completed [" + rateCompletedCount.intValue() + "] expectedMin[" + expectedMin + "]",
		// rateCompletedCount.intValue() >= expectedMin);

		Assert.assertTrue("Rates completed [" + infoThread.rateCompletedCount.intValue() + "] expectedMin[" + expectedMin + "]",
				infoThread.rateCompletedCount.intValue() >= expectedMin);

		sleep(30000); // sleep long enough for all things to run
	}

	@Override
	public void completed(final SourceGroupInstance sgInstance, final SourceGroupInstanceLifecycleInfo lifecycle) {
		LOG.info("SourceGroup processed [" + sgInstance.summaryString() + "]");
		if (this.sourceGroupId == sgInstance.getId().longValue()) {
			if (sgInstance.processedSuccessfully()) {
				this.readyFlag.flag = true;
				signalForEvent();
				LOG.info("*** Test target sourceGroup processed [" + sgInstance.summaryString() + "]");
				return;
			} else {
				LOG.fatal("******************************************************************");
				LOG.fatal("******************************************************************");
				LOG.fatal("******************************************************************");
				LOG.fatal("******************************************************************");
				LOG.fatal("***************** target test sourcegroup failed with errors - please check your data ******************");
				LOG.fatal("******************************************************************");
				LOG.fatal("******************************************************************");
				LOG.fatal("******************************************************************");
				LOG.fatal("******************************************************************");
				LOG.fatal("******************************************************************");

			}
		}

	}

	public int count() {
		final List<Integer> counts = this.jdbcTemplate.query("select count(*) from rateinstance where RATESTATUS = 'READY'",
				new RowMapper() {
					@Override
					public Object mapRow(final ResultSet rs, final int rowNum) throws SQLException {
						return rs.getInt(1);
					}
				});

		final Integer readyCount = counts.get(0);
		return readyCount.intValue();
	}

	class InfoThread implements Runnable, IRateCompletionListener {

		private final long frequencyToRun;
		public boolean stop;
		public int lastCount;
		private final IRatesService ratesService;
		private final IOrchestrationEngine orchestrationEngine;
		protected int expectedMin;
		protected AtomicInteger rateCompletedCount = new AtomicInteger();
		protected Set<RateInstance> successSet = Collections.synchronizedSet(new HashSet<RateInstance>());

		int count = 0;

		public InfoThread(final long frequencyToRun, final IRatesService ratesService,
				final IOrchestrationEngine orchestrationEngine, final int expectedMin) {
			this.frequencyToRun = frequencyToRun;
			this.ratesService = ratesService;
			this.orchestrationEngine = orchestrationEngine;
			this.orchestrationEngine.addRateCompletionListener(this);
			this.expectedMin = expectedMin;
		}

		@Override
		public void completed(final RateInstance rateInstance, final RateInstanceLifecycleInfo lifecycle) {
			rateCompletedCount.incrementAndGet();
			successSet.add(rateInstance);
		}

		@Override
		public void rateOutOfException(final RateInstance rateInstance, final RateInstanceLifecycleInfo lifecycle) {
		}

		@Override
		public void run() {

			while (!stop) {
				doOutput();

				if (!stop) {
					try {
						Thread.sleep(frequencyToRun);
					} catch (final InterruptedException e) {
					}

					count++;
				}
			}

		}

		public void doOutput() {

			final int succ = successSet.size();

			if (succ >= expectedMin) {
				stop = true;
				PieChartUtil.LOG.info("******** Got expected min count [" + expectedMin + "], completed [" + succ
						+ "] notifications [" + rateCompletedCount.intValue() + "] (inc dups). Will stop stats collection");
			}

			final int currentCount = count();

			final int completedInInterval = currentCount - lastCount;

			final double rate = (double) frequencyToRun / (double) (completedInInterval > 0 ? completedInInterval : 1);

			PieChartUtil.LOG.info("Current Ready count [" + currentCount + "], interval rate [" + ((int) rate) + "]ms");

			try {
				ratesService.printStats(new PrintWriter(System.out));
			} catch (final Exception e1) {
				e1.printStackTrace();
			}

			this.lastCount = currentCount;

			if (count % 3 == 0) {
				MemoryStats.logMemoryInfo("INFO", "RR Mem:");
			}
		}

	}
}

