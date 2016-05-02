package org.jwall.web.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jwall.audit.EventListener;
import org.jwall.audit.EventProcessor;
import org.jwall.audit.EventProcessorException;
import org.jwall.audit.EventProcessorPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This singleton class encapsulates a global, static queue of event processor
 * instances. The processors can be registered using a priority, affecting their
 * execution order in the queue.
 * </p>
 * <p>
 * For each incoming event, this queue is processed before the event is stored
 * within the event storage.
 * </p>
 * <p>
 * In addition to the processors, this queue also provides a place for
 * registering event-listeners. These are notified after all processors have
 * been applied to the event.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 * 
 */
public class AuditEventProcessorPipeline implements
		EventProcessorPipeline<AuditEvent> {
	/* The global logger for this class */
	static Logger log = LoggerFactory
			.getLogger(AuditEventProcessorPipeline.class);

	/**
	 * This tree-set contains a list of event processors which are called for
	 * incoming events in the order of their priority
	 */
	final LinkedList<EventProcessor<AuditEvent>> eventProcessors = new LinkedList<EventProcessor<AuditEvent>>();

	/**
	 * This is a set of listeners which will be notified about events AFTER they
	 * have been processed by all registered processors
	 */
	final Set<EventListener<AuditEvent>> eventListener = new LinkedHashSet<EventListener<AuditEvent>>();

	Map<EventProcessor<AuditEvent>, Double> priorities = new HashMap<EventProcessor<AuditEvent>, Double>();
	public final static Double DEFAULT_PRIORITY = 100.0d;

	final static boolean profiling = "true".equalsIgnoreCase(System
			.getProperty("eventprocessor.profiling", "false"));

	/**
	 * This method applies all registered processors to the specified event.
	 * 
	 * 
	 * @param event
	 * @return
	 * @throws EventProcessorException
	 */
	public AuditEvent processEvent(AuditEvent event)
			throws EventProcessorException {

		final Long pt0 = System.currentTimeMillis();
		try {
			Map<String, Object> context = new HashMap<String, Object>();
			if (log.isDebugEnabled()) {
				log.debug("Processing next event[{}]...", event.getEventId());
				log.debug(
						"ScriptEvent will be processed by {} event processors:",
						eventProcessors.size());
				for (EventProcessor<AuditEvent> p : eventProcessors) {
					log.debug("   {}", p);
				}
			}
			for (EventProcessor<AuditEvent> p : eventProcessors) {
				try {
					long start = System.currentTimeMillis();
					p.processEvent(event, context);
					long end = System.currentTimeMillis();
					if (profiling)
						log.info("Processing with {} took {}ms", p, end - start);
				} catch (Exception e) {
					log.error("Processing failed: {}", e.getMessage());
					e.printStackTrace();
				}
			}

			if (context.containsKey(EventProcessor.DELETE_FLAG)) {
				log.debug("ScriptEvent has been flagged as 'deleted'! Skipping event listener...");
				return event;
			}

			log.debug("Sending notification to {} event listeners.",
					eventListener.size());
			for (EventListener<AuditEvent> l : eventListener) {
				try {
					long t0 = System.currentTimeMillis();
					l.eventArrived(event);
					long t1 = System.currentTimeMillis();
					if (profiling)
						log.info("Listener {} required {} ms", l, t1 - t0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			for (String key : context.keySet()) {
				Object o = context.get(key);
				if (o instanceof Runnable) {
					log.debug("Need to execute trigger {} ({})", key, o);
				}
			}

			if (profiling) {
				long pt1 = System.currentTimeMillis();
				log.info("event-processor pipeline required {} ms", (pt1 - pt0));
			}
			return event;

		} catch (Exception e) {
			log.error("Failed to process event {}: {}", event, e.getMessage());
			e.printStackTrace();
			if (profiling) {
				long pt1 = System.currentTimeMillis();
				log.info("event-processor pipeline required {} ms", (pt1 - pt0));
			}
			return event;
		}
	}

	/**
	 * This method is used to unregister an event-processor from the
	 * AuditStorage
	 * 
	 * @param p
	 */
	public void unregisterEventProcessor(EventProcessor<AuditEvent> p) {
		log.debug("Unregistering event-processor {} with priority {}", p,
				priorities.get(p.toString()));
		eventProcessors.remove(p);
		priorities.remove(p);
	}

	public void unregisterListener(EventListener<AuditEvent> l) {
		log.debug("Unregistering event-listener {}", l);
		eventListener.remove(l);

		log.debug("EventListener queue now is:");
		for (EventListener<AuditEvent> proc : eventListener) {
			log.debug("   {}", proc);
		}
	}

	public void registerListener(EventListener<AuditEvent> l) {
		log.debug("Registering event-listener {}", l);
		eventListener.add(l);

		log.debug("EventListener queue now is:");
		for (EventListener<AuditEvent> proc : eventListener) {
			log.debug("   {}", proc);
		}
	}

	public void logInfo() {

		log.info("Processor queue:");
		for (EventProcessor<AuditEvent> proc : eventProcessors) {
			log.info("   {}", proc);
		}

		log.info("EventListener queue:");
		for (EventListener<AuditEvent> proc : eventListener) {
			log.info("   {}", proc);
		}
	}

	/**
	 * @see org.jwall.web.audit.console.event.EventProcessorPipeline#process(org.jwall.audit.Event)
	 */
	@Override
	public void process(AuditEvent event) throws EventProcessorException {
		processEvent(event);
	}

	public List<EventProcessor<AuditEvent>> getProcessors() {
		return Collections.unmodifiableList(eventProcessors);
	}

	public List<EventListener<AuditEvent>> getListeners() {
		List<EventListener<AuditEvent>> listener = new ArrayList<EventListener<AuditEvent>>(
				eventListener);
		return Collections.unmodifiableList(listener);
	}

	/**
	 * @see org.jwall.web.audit.console.event.EventProcessorPipeline#unregister(org.jwall.audit.EventProcessor)
	 */
	@Override
	public void unregister(EventProcessor<AuditEvent> proc) {
		this.eventProcessors.remove(proc);
	}

	/**
	 * @see org.jwall.web.audit.console.event.EventProcessorPipeline#register(java.lang.Double,
	 *      org.jwall.audit.EventProcessor)
	 */
	@Override
	public void register(Double priority, EventProcessor<AuditEvent> proc) {
		this.eventProcessors.add(proc);
		this.priorities.put(proc, priority);
		Collections.sort(eventProcessors, new Priority(this.priorities));
		log.debug("Registering processor {} with priority {}", proc, priority);
		log.debug("Processor queue now is:");
		for (EventProcessor<AuditEvent> p : eventProcessors) {
			log.debug("   {}   (prio: {})", p, priorities.get(p.toString()));
		}
	}

	public class Priority implements Comparator<EventProcessor<AuditEvent>> {

		Map<EventProcessor<AuditEvent>, Double> prios = new HashMap<EventProcessor<AuditEvent>, Double>();

		public Priority(Map<EventProcessor<AuditEvent>, Double> p) {
			prios = p;
		}

		@Override
		public int compare(EventProcessor<AuditEvent> arg0,
				EventProcessor<AuditEvent> arg1) {
			if (arg0 == arg1)
				return 0;

			Double p0 = prios.get(arg0);
			if (p0 == null)
				p0 = DEFAULT_PRIORITY;

			Double p1 = prios.get(arg1);
			if (p1 == null)
				p1 = DEFAULT_PRIORITY;

			int rc = p0.compareTo(p1);
			if (rc == 0) {
				return arg0.toString().compareTo(arg1.toString());
			} else
				return rc;
		}

	}

	@Override
	public Double getPriority(EventProcessor<AuditEvent> proc) {
		if (priorities.containsKey(proc))
			return priorities.get(proc);

		return Double.NaN;
	}
}

