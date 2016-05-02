package at.ac.tuwien.infosys.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.time.Duration;
import org.jfree.chart.JFreeChart;
import org.ops4j.pax.wicket.api.PaxWicketBean;

import at.ac.tuwien.infosys.api.domain.SensorChart;
import at.ac.tuwien.infosys.api.domain.SensorState;
import at.ac.tuwien.infosys.api.events.ChartConfigEvent;
import at.ac.tuwien.infosys.api.events.ChartEvent;
import at.ac.tuwien.infosys.api.events.Subscriber;
import at.ac.tuwien.infosys.api.mapek.KnowledgeBase;

public class ChartPanel extends Panel implements Subscriber<ChartEvent> {
	private static final long serialVersionUID = 1L;

	@PaxWicketBean(name = "knowledgeBaseService")
	private transient KnowledgeBase knowledgeBaseService;

	private JFreeChart chart;

	public ChartPanel(String id) {
		super(id);

		knowledgeBaseService.getEventBus().subscribe(ChartEvent.class, this);

		add(createChartImage().add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(15))));

		IModel<List<String>> list = new LoadableDetachableModel<List<String>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<String> load() {
				List<String> list = new ArrayList<String>();
				List<SensorState> vertices = knowledgeBaseService.loadCurrentState();
				for (SensorState state : vertices) {
					list.add("c_" + state.getSensor());
					list.add("m_" + state.getSensor());
				}
				list.add("error");
				list.add("maxError");
				return list;
			}
		};

		GridView<String> sensors = new GridView<String>("sensorRows", new SensorDataProvider(list.getObject())) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(Item<String> item) {
				item.add(new Check<String>("checkbox", item.getModel()));
				item.add(new Label("sensor", item.getModelObject()));
			}

			@Override
			protected void populateEmptyItem(Item<String> item) {
				Check<String> check = new Check<String>("checkbox", item.getModel());
				Label label = new Label("sensor", "empty");
				check.setVisible(false);
				label.setVisible(false);
				item.add(check);
				item.add(label);
			}
		};
		
		sensors.setRows(Long.valueOf(Math.round(Math.ceil(list.getObject().size() / 14D))).intValue());
		sensors.setColumns(14);

		final CheckGroup<String> group = new CheckGroup<String>("group", new ArrayList<String>());

		Form<?> form = new Form<Void>("form");

		form.add(new AjaxButton("button") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				@SuppressWarnings("unchecked")
				List<String> sensors = (List<String>) group.getDefaultModelObject();
				List<SensorChart> sensorCharts = new ArrayList<SensorChart>();
				for (String sensor : sensors) {
					sensorCharts.add(new SensorChart(sensor, true));
				}
				ChartConfigEvent event = new ChartConfigEvent();
				event.setSensorCharts(sensorCharts);
				knowledgeBaseService.getEventBus().publish(event);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
			}

		});
		form.add(group);
		group.add(sensors);
		add(form);
	}

	private Image createChartImage() {
		return new Chart("chart", getChartModel(), 900, 600);
	}

	private IModel<JFreeChart> getChartModel() {
		return new LoadableDetachableModel<JFreeChart>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected JFreeChart load() {
				return chart;
			}
		};
	}

	@Override
	public void notify(ChartEvent event) {
		if (event.getChart() instanceof JFreeChart) {
			chart = (JFreeChart) event.getChart();
		}
	}
}

