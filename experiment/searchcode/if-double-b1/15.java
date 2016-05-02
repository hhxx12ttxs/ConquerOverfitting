package bayes;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PredictOnline implements Tool {

	protected Configuration _conf = new Configuration();

	@Override
	public Configuration getConf() {
		return _conf;
	}

	@Override
	public void setConf(Configuration conf) {
		_conf = conf;

	}

	
	public static void readStrength(Map<String, Double> strengthMap,
			String filename, int uvThreshold) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(filename)));
		int userM = 0, userF = 0;
		String str;
		while ((str = br.readLine()) != null) {
			String[] splits = str.split("\\p{Blank}+");
			if(splits.length < 5) continue;
			int uvF = Integer.parseInt(splits[3]);
			int uvM = Integer.parseInt(splits[4]);
			userM += uvM;
			userF += uvF;
			if (uvF + uvM < uvThreshold || uvF == 0 || uvM == 0)
				continue;
			double rate = ((double) uvF) / uvM;
			strengthMap.put(splits[0], rate);
		}
		br.close();
		
		for(String pageid:strengthMap.keySet()) {
			strengthMap.put(pageid, strengthMap.get(pageid) / (((double) userF) / userM));
		}

	}

	public static class PredictMapper extends Mapper<Object, Text, Text, Text> {

		static Map<String, Double> strengthAll = null;
		static Map<String, Double> strengthM = null;
		static Map<String, Double> strengthF = null;

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {

			final double thresholdF = Double.parseDouble(context.getConfiguration().get("rate.thresholdF", "0.0"));
			final double thresholdM = Double.parseDouble(context.getConfiguration().get("rate.thresholdM", "0.0"));
			final double thresholdTotal = Double.parseDouble(context.getConfiguration().get("rate.thresholdTotal", "0.0"));
			final int topF = Integer.parseInt(context.getConfiguration().get("num.topF", "0"));
			final int topM = Integer.parseInt(context.getConfiguration().get("num.topM", "0"));
			final int topTotal = Integer.parseInt(context.getConfiguration().get("num.topTotal", "0"));
			final int uvThreshold = Integer.parseInt(context.getConfiguration().get("num.uvThreshold", "100"));

			if (strengthAll == null) {
				strengthAll=new HashMap<String, Double>();
				strengthM=new HashMap<String, Double>();
				strengthF=new HashMap<String, Double>();
				readStrength(strengthAll,context.getConfiguration().get("file.pvuv"),uvThreshold);
				for(String pageid:strengthAll.keySet()) {
					double rate = strengthAll.get(pageid);
					if(rate > 1) strengthF.put(pageid, rate); 
					else strengthM.put(pageid, 1.0/rate); 
				}
			}
			
			
			
			List<Double> femaleRates = new ArrayList<Double>();
			List<Double> maleRates = new ArrayList<Double>();
			List<Double> totalRates = new ArrayList<Double>();

			String line = value.toString();
			String[] splits = line.split("\\p{Blank}+");
			String userid = splits[0];
			
//			String regex = "\\(([^,]+),(\\d+),(\\d+)\\)";
			String regex = "\\((\\d+),(\\d+)\\)";
			Pattern pattern = Pattern.compile(regex);
			Matcher match = pattern.matcher(splits[1]);
			while (match.find()) {
			    String pageid = match.group(2);
			    if (strengthF.containsKey(pageid)) {
					double strength = strengthF.get(pageid);
					femaleRates.add(strength);
					totalRates.add(strength);
				}
				if (strengthM.containsKey(pageid)) {
					double strength = strengthM.get(pageid);
					maleRates.add(strength);
					totalRates.add(strength);
				}
			    
			}
			
			Collections.sort(femaleRates, new Comparator<Double>() {
				public int compare(Double b1, Double b2) {
					if (b2 > b1)
						return 1;
					if (b2.equals(b1))
						return 0;
					return -1;
				}
			});

			Collections.sort(maleRates, new Comparator<Double>() {
				public int compare(Double b1, Double b2) {
					if (b2 > b1)
						return 1;
					if (b2 == b1)
						return 0;
					return -1;
				}
			});

			Collections.sort(totalRates, new Comparator<Double>() {
				public int compare(Double b1, Double b2) {
					if (b2 > b1)
						return 1;
					if (b2 == b1)
						return 0;
					return -1;
				}
			});

			String calStrengthMethod = context.getConfiguration().get(
					"cal.method");

			double strength = 1.0;
			if ("topTotal".equals(calStrengthMethod)) {
				if (totalRates.size() >= topTotal) {
					for (int i = 0; i < topTotal; i++) {
						strength *= totalRates.get(i);
					}
					writeTag(context,userid,strength);
//					context.write(new Text(userid), new Text(Double.toString(strength)));
				}

			} else if ("thresholdTotal".equals(calStrengthMethod)) {
				boolean changed = false;
				for (double pageStrength : totalRates) {
					if (pageStrength < thresholdTotal)
						break;
					strength *= pageStrength;
					changed = true;
				}
				if (changed) {
//					context.write(new Text(userid), new Text(Double.toString(strength)));
				}
			} else if ("topMF".equals(calStrengthMethod)) {
				if (femaleRates.size() >= topF && maleRates.size() >= topM) {
					for (int i = 0; i < topF; i++) {
						strength *= femaleRates.get(i);
					}
					for (int i = 0; i < topM; i++) {
						strength /= maleRates.get(i);
					}
//					context.write(new Text(userid), new Text(Double.toString(strength)));
				}

			} else if ("thresholdMF".equals(calStrengthMethod)) {
				boolean changed = false;
				for (double pageStrength : femaleRates) {
					if (pageStrength < thresholdF)
						break;
					strength *= pageStrength;
					changed = true;
				}
				for (double pageStrength : maleRates) {
					if (pageStrength < thresholdM)
						break;
					strength /= pageStrength;
					changed = true;
				}
				if (changed) {
//					context.write(new Text(userid), new Text(Double.toString(strength)));
				}
			}
		}

		private void writeTag(Context context, String userid, double strength) {			
			// TODO Auto-generated method stub
			final double thresholdTag1 = Double.parseDouble(context.getConfiguration().get("rate.thresholdTag1", "300"));
			final double thresholdTag2 = Double.parseDouble(context.getConfiguration().get("rate.thresholdTag2", "800"));
			if(strength >= thresholdTag1) {
				context.write(key, value);
			}
			if(strength >= thresholdTag2) {
				context.write(key, value);
			}
			
		}
	}

	@Override
	public int run(String[] args) throws Exception {

		Job job = new Job(_conf, "[Demographic] Bayes Predict");

		job.setJarByClass(PredictOnline.class);
		job.setMapperClass(PredictMapper.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setNumReduceTasks(0);

		System.out.println(_conf.get("dir.input"));
		System.out.println(_conf.get("dir.output"));

		FileInputFormat.addInputPaths(job, _conf.get("dir.input"));
		FileOutputFormat.setOutputPath(job,
				new Path(_conf.get("dir.output")));

		// System.out.println(job.waitForCompletion(true) ? 0 : 1);
		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		int ret = ToolRunner.run(new PredictOnline(), args);
		if (ret != 0) {
			System.err.println("Job Failed!");
			System.exit(ret);
		}
	}
}

