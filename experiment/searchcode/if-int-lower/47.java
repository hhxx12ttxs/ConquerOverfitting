package com.ntscorp.jenkins.plugin;

import hudson.model.Build;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerProxy;

import com.ntscorp.jenkins.plugin.model.Constant;
import com.ntscorp.jenkins.plugin.model.Counting;

/**
 * BuildAction은 Publisher의 perform 메소드 안에서 호출되며, 각 빌드마다의 정보를 저장한다.
 * 
 * @author 김민영
 */
public final class CountingBuildAction implements HealthReportingAction, StaplerProxy {
	private static Logger logger = Logger.getLogger(CountingBuildAction.class.getName());
	public final Build<?, ?> owner;
	private final Counting result;
	private HealthReport healthReport = null;

	/**
	 * <b>생성자</b><br/>
	 * 결과와 health 설정에 대한 metric 정보를 파라미터로 받는다.
	 * 
	 * @param build
	 *            {@link Build}
	 * @param result 요약
	 * @param target 결과
	 */
	private CountingBuildAction(Build<?, ?> build, Counting result) {
		this.owner = build;
		this.result = result;
	}

	public static CountingBuildAction load(Build<?, ?> build, Counting result) {
		return new CountingBuildAction(build, result);
	}

//	private CountingPublisher getPublisher() {
//		return (CountingPublisher) owner.getProject().getPublisher(CountingPublisher.DESCRIPTOR);
//	}

	/**
	 * 날씨 정보를 표시<br/> {@link HealthReport}는 두 개의 파라미터로 생성할 수 있는데, 첫번째 파라미터는
	 * minValue이며, 두번째 파라미터는 minValue에 대한 설명이다.<br/>
	 * minValue는 정수 값이며, 값의 크기에 따라 다른 날씨가 표시된다. 예를 들어, 80 이상의 값이면 <u>맑음</u>, 60
	 * 이상의 값이면 <u>구름 낀 맑음</u>, 40 이상의 값이면 <u>구름</u>, 20 이상의 값이면 <u>비</u>, 그 이하의
	 * 값이면 <u>번개</u>이다.<br/>
	 * <br/>
	 * Hudson의 프로젝트 대시보드에서의 날씨 정보는, 여러 개의 플러그인의 Health Report에서 가장 최소값을 기준으로 한다.
	 * 즉 어떤 프로젝트가 A 플러그인에서는 minValue가 80이고, B 플러그인에서는 50이라면, 날씨는 <b>구름</b>으로
	 * 나타난다.<br/>
	 * 만약 null을 리턴하면, 아무런 정보도 표시하지 않는다.<br/>
	 * <br/>
	 * 여기에서는 플러그인 설정에서 지정된 값보다 작으면 100%, 2배 이하이면 60%, 3배 이하이면 45%, 4배 이하이면 30%,
	 * 5배 이하이면 15%, 5배 이상이면 0%로 표시된다.<br/>
	 * 예를 들어, 기준 값이 1.0이고, 실제 complexity 값이 3.5라면 3배~4배 사이의 값이므로 30%(<u>비</u>)로
	 * 나타난다.
	 */
	@SuppressWarnings("deprecation")
	public HealthReport getBuildHealth() {
		if (healthReport != null) {
			return healthReport;
		}

		StringBuffer sb = new StringBuffer(Constant.PLUGIN_NAME + ": ");
		sb.append("method count : " );

		healthReport = new HealthReport(100, sb.toString());

		return healthReport;
	}
	
	public Counting getResult() {
		return result;
	}

	/**
	 * summary 페이지 (Build History를 클릭했을 때 나오는 페이지)에서 나타나는 플러그인의 이름을 리턴한다.
	 */
	public String getDisplayName() {
		return Constant.PLUGIN_NAME;
	}

	/**
	 * summary 페이지 (Build History를 클릭했을 때 나오는 페이지)에서 나타나는 아이콘의 이름을 리턴한다.
	 */
	public String getIconFileName() {
		return Constant.ICON_FILENAME;
	}

	/**
	 * summary 페이지 (Build History를 클릭했을 때 나오는 페이지)에서 각 빌드로 연결되는 URL을 리턴한다.
	 */
	public String getUrlName() {
		return Constant.URL;
	}

	/**
	 * {@link CountingBuildAction}의 N'SIQ Collector 결과를 리턴한다.
	 * 
	 * @return N'SIQ Collector 결과
	 */
	public Counting getTarget() {

		Counting target = null;

		synchronized (this) {
			try {
				target = (Counting) CountingUtil.getDataFile(owner).read();
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.toString());
			}

			if (target != null) {
				return target;
			} else {
				return null;
			}
		}
	}

	/**
	 * 그래프(차트)를 표시하기 위해 이전 Build 정보를 리턴한다.
	 * 
	 * @return 이전 Build 정보를 갖는 {@link CountingBuildAction} 인스턴스
	 */
	public CountingBuildAction getPreviousResult() {
		return getPreviousResult(owner);
	}

	/**
	 * 그래프(차트)를 표시하기 위해 build 파라미터의 이전 Build 정보를 리턴한다.
	 * 
	 * @param owner
	 *            {@link Build} 인스턴스
	 * @return 이전 Build 정보를 갖는 {@link CountingBuildAction} 인스턴스
	 */
	static CountingBuildAction getPreviousResult(Build<?, ?> owner) {
		Build<?, ?> build = owner;

		while (true) {
			build = build.getPreviousNotFailedBuild();

			if (build == null) {
				return null;
			}

			CountingBuildAction action = build.getAction(CountingBuildAction.class);

			if (action != null) {
				return action;
			}
		}
	}

	public long getTimestamp() {
		return owner.getTimestamp().getTimeInMillis();
	}
	
	public Counting getSummary()
	{
		return result;
	}

	public Graph getMethodGraph() {
		return new Graph(getTimestamp(), 490, 200) {
			@Override
			protected JFreeChart createGraph() {
				int lower = Integer.MAX_VALUE;
				int upper = Integer.MIN_VALUE;
				String previous = "";
				DataSetBuilder<String, Comparable<?>> dsb = new DataSetBuilder<String, Comparable<?>>();
				
				for (CountingBuildAction build = CountingBuildAction.this; build != null; build = build.getPreviousResult()) {
					Comparable<?> label = new BuildDateLabel(build.owner);
					
//					Comparable<?> label = new NumberOnlyBuildLabel(build.owner);
					
					String date = ((BuildDateLabel) label).getDate();
					if (previous.equals(date)) {
						continue;
					}
					previous = date;
					
					Counting result = build.getTarget();
					lower = Math.min(lower, result.getClassCount() - 200);
					upper = Math.max(upper, result.getClassCount() + 200);
					dsb.add(result.getClassCount(), "Class", label);
				}
				return CountingUtil.createLineChart(dsb.build(), "Class Count", lower, upper);
			}
		};
	}
	
	public Graph getClassGraph() {
		return new Graph(getTimestamp(), 490, 200) {
			@Override
			protected JFreeChart createGraph() {
				int lower = Integer.MAX_VALUE;
				int upper = Integer.MIN_VALUE;
				String previous = "";
				DataSetBuilder<String, Comparable<?>> dsb = new DataSetBuilder<String, Comparable<?>>();
				
				for (CountingBuildAction build = CountingBuildAction.this; build != null; build = build.getPreviousResult()) {
					Comparable<?> label = new BuildDateLabel(build.owner);
					
//					Comparable<?> label = new NumberOnlyBuildLabel(build.owner);
					
					String date = ((BuildDateLabel) label).getDate();
					if (previous.equals(date)) {
						continue;
					}
					previous = date;
					
					Counting result = build.getTarget();
					lower = Math.min(lower, result.getMethodCount() - 50);
					upper = Math.max(upper, result.getMethodCount() + 50);
					dsb.add(result.getMethodCount(), "Method", label);
				}
				return CountingUtil.createLineChart(dsb.build(), "Method Count", lower, upper);
			}
		};
	}
}

