package at.ac.tuwien.infosys.internal.policies;

import java.util.List;

import at.ac.tuwien.infosys.api.domain.PanoptesConfiguration;
import at.ac.tuwien.infosys.api.domain.SensorState;
import at.ac.tuwien.infosys.api.events.ReportEvent;
import at.ac.tuwien.infosys.api.mapek.KnowledgeBase;
import at.ac.tuwien.infosys.api.policy.SampleSizePolicy;

public class ResetSampleSize extends SampleSizePolicy {

	private static final int MIN_SAMPLE_SIZE = 2;
	private static final int X = 5;

	private KnowledgeBase knowledgeBase;

	public ResetSampleSize(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	public void sample() {
		List<SensorState> vertices = knowledgeBase.loadCurrentState();

		// each sensor has to be measured at least X times
		boolean measuredXtimes = true;
		for (int i = 0; i < vertices.size() && measuredXtimes; i++) {
			measuredXtimes &= vertices.get(i).getMeasured() > X;
		}

		PanoptesConfiguration panoptesConfig = knowledgeBase.loadPanoptesConfig();
		panoptesConfig.setAccurateDeviation(measuredXtimes);
		
		int sampleSize = panoptesConfig.getSampleSize();

		if (measuredXtimes) {
			if (panoptesConfig.getDeviation() > panoptesConfig.getMaxError()) {
				if (sampleSize < vertices.size()) {
					sampleSize = Math.max(MIN_SAMPLE_SIZE, sampleSize + 1);
					for (SensorState state : vertices) {
						state.reset(false);
						knowledgeBase.persistSensorState(state);
					}
				}
			} else {
				if (sampleSize > MIN_SAMPLE_SIZE) {
					sampleSize = Math.min(vertices.size(), Math.max(MIN_SAMPLE_SIZE, sampleSize - 1));
					for (SensorState state : vertices) {
						state.reset(false);
						knowledgeBase.persistSensorState(state);
					}
				}
			}
		} else {
			sampleSize = Math.min(vertices.size(), Math.max(MIN_SAMPLE_SIZE, sampleSize));
		}
		if (sampleSize != panoptesConfig.getSampleSize()) {
			panoptesConfig.setSampleSize(sampleSize);
			knowledgeBase.persistPanoptesConfig(panoptesConfig);
			
			ReportEvent event = new ReportEvent(SampleSizePolicy.class);
			event.setReport("Changed sample size to " + sampleSize + ".");
			knowledgeBase.getEventBus().publish(event);
		}
	}
}

