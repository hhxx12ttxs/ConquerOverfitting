package at.ac.tuwien.infosys.internal.policies;

import java.util.List;

import at.ac.tuwien.infosys.api.domain.PanoptesConfiguration;
import at.ac.tuwien.infosys.api.domain.SensorState;
import at.ac.tuwien.infosys.api.events.PlainEvent;
import at.ac.tuwien.infosys.api.events.ReportEvent;
import at.ac.tuwien.infosys.api.events.Subscriber;
import at.ac.tuwien.infosys.api.mapek.KnowledgeBase;
import at.ac.tuwien.infosys.api.policy.SampleSizePolicy;

public class JumpingSampleSize extends SampleSizePolicy implements Subscriber<PlainEvent> {

	private static final int MIN_SAMPLE_SIZE = 2;
	private static final int X = 5;
	private int collectionSize;

	private int counter;

	private KnowledgeBase knowledgeBase;

	public JumpingSampleSize(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
		collectionSize = 0;
		counter = 0;
	}

	public void sample() {
		List<SensorState> vertices = knowledgeBase.loadCurrentState();

		if (counter < collectionSize) {
			counter++;
		}
		// each sensor has to be measured at least X times
		boolean measuredXtimes = (counter == collectionSize);

		PanoptesConfiguration panoptesConfig = knowledgeBase.loadPanoptesConfig();
		panoptesConfig.setAccurateDeviation(measuredXtimes);
		int sampleSize = panoptesConfig.getSampleSize();

		double deviation = panoptesConfig.getDeviation();
		double maxError = panoptesConfig.getMaxError();
		if (measuredXtimes) {
			if (deviation > maxError) {
				if (sampleSize < vertices.size()) {
					sampleSize = Math.min(
							vertices.size(),
							Math.max(MIN_SAMPLE_SIZE,
									vertices.size() - Long.valueOf(Math.round(Math.ceil(deviation / maxError))).intValue()));
					for (SensorState state : vertices) {
						knowledgeBase.persistSensorState(state);
					}
				}
			} else {
				if (sampleSize > MIN_SAMPLE_SIZE) {
					sampleSize = Math.min(
							vertices.size(),
							Math.max(MIN_SAMPLE_SIZE,
									vertices.size() - Long.valueOf(Math.round(Math.ceil(maxError / deviation))).intValue()));
					for (SensorState state : vertices) {
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

	@Override
	public void notify(PlainEvent event) {
		if ("FinishedTrainEvent".equals(event.getEvent())) {
			collectionSize = knowledgeBase.loadCurrentState().size() * X;
		}
		if ("ResetEvent".equals(event.getEvent())) {
			counter = 0;
		}
	}

	@Override
	public void init() {
		this.knowledgeBase.getEventBus().subscribe(PlainEvent.class, this);
	}

	@Override
	public void destroy() {
		this.knowledgeBase.getEventBus().unsubscribe(PlainEvent.class, this);
	}
}

