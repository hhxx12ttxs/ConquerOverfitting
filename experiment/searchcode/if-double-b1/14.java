package bayes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


public class BayesTest3 implements Tool {

	protected Configuration _conf = new Configuration();

	@Override
	public Configuration getConf() {
		return _conf;
	}

	@Override
	public void setConf(Configuration conf) {
		_conf = conf;

	}
	
	public static void readStrength(Map<String, Double> strengthMap, String filename) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		
		String str;
		while ((str = br.readLine()) != null) {
			String[] splits = str.split("\\p{Blank}+");
			double rate=Double.parseDouble(splits[1]);
			strengthMap.put(splits[0], rate);
		}
		br.close();
	}

	public static class TestMapper extends Mapper<Object, Text, Text, Text> {
		
		static Map<String, Double> strengthF = null;
		static Map<String, Double> strengthM = null;
		
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			
			
			final double thresholdF = Double.parseDouble(context.getConfiguration().get("rate.thresholdF","0.0"));
			final double thresholdM = Double.parseDouble(context.getConfiguration().get("rate.thresholdM","0.0"));
			final double thresholdTotal = Double.parseDouble(context.getConfiguration().get("rate.thresholdTotal","0.0"));
			final int topF = Integer.parseInt(context.getConfiguration().get("num.topF","0"));
			final int topM = Integer.parseInt(context.getConfiguration().get("num.topM","0"));
			final int topTotal = Integer.parseInt(context.getConfiguration().get("num.topTotal","0"));
			
			
			if (strengthF == null) {
				strengthF = new HashMap<String, Double>();
				readStrength(strengthF, context.getConfiguration().get("file.strengthF"));
			}
			if (strengthM == null) {
				strengthM = new HashMap<String, Double>();
				readStrength(strengthM, context.getConfiguration().get("file.strengthM"));
			}
			
			
			
			int NUM_ALL = Integer.parseInt(context.getConfiguration().get("num.divide").split("_")[0]);
			int NUM_TEST = Integer.parseInt(context.getConfiguration().get("num.divide").split("_")[1]);
			int lineno = Integer.parseInt(value.toString().split("#", 2)[0]);
			
			if (lineno % NUM_ALL != NUM_TEST)
				return;
			
			
			List<Double> femaleRates = new ArrayList<Double>();
			List<Double> maleRates = new ArrayList<Double>();
			List<Double> totalRates = new ArrayList<Double>();
			
			String line = value.toString().split("#", 2)[1];
			String[] splits = line.split("\\p{Blank}+");
			String gender = splits[0];
			
			gender = "0".equals(gender) ? "M" : "F";
			
			for (String seg : splits) {
				String[] kv = seg.split(":");
				if (kv.length != 2)
					continue;
				if (strengthF.containsKey(kv[0])) {
					double strength = strengthF.get(kv[0]);
					femaleRates.add(strength);
					totalRates.add(strength);
				}
				if(strengthM.containsKey(kv[0])) {
					double strength = strengthM.get(kv[0]);
					maleRates.add(strength);
					totalRates.add(strength);
				}
			}
			
			Collections.sort(femaleRates,new Comparator<Double>(){
				   public int compare(Double b1, Double b2) {
				          if( b2 > b1) return 1;
				          if (b2 == b1 ) return 0;
				          return -1;
				   }
				});
			
			Collections.sort(maleRates,new Comparator<Double>(){
				   public int compare(Double b1, Double b2) {
				          if( b2 > b1) return 1;
				          if (b2 == b1 ) return 0;
				          return -1;
				   }
				});
			
			Collections.sort(totalRates,new Comparator<Double>(){
				   public int compare(Double b1, Double b2) {
				          if( b2 > b1) return 1;
				          if (b2 == b1 ) return 0;
				          return -1;
				   }
				});
			
			String calStrengthMethod = context.getConfiguration().get("cal.method");
			
			double strength = 1.0;
			if("topTotal".equals(calStrengthMethod)) {
				if(totalRates.size() >= topTotal) {
					for(int i = 0; i < topTotal; i++) {
						strength*=totalRates.get(i);
					}
					context.write(new Text(strength+" "+gender), new Text());
				}
				
			} else if ("thresholdTotal".equals(calStrengthMethod)) {
				boolean changed = false;
				for(double pageStrength:totalRates) {
					if(pageStrength < thresholdTotal) break;
					strength *= pageStrength;
					changed=true;
				}
				if(changed) {
					context.write(new Text(strength+" "+gender), new Text());
				}
			} else if("topMF".equals(calStrengthMethod)) {
				if(femaleRates.size() >= topF && maleRates.size() >= topM) {
					for(int i = 0; i < topF; i++) {
						strength*=femaleRates.get(i);
					}
					for(int i = 0; i < topM; i++) {
						strength /= maleRates.get(i);
					}
					context.write(new Text(strength+" "+gender), new Text());
				}
				
			} else if ("thresholdMF".equals(calStrengthMethod)) {
				boolean changed = false;
				for(double pageStrength:femaleRates) {
					if(pageStrength < thresholdF) break;
					strength *= pageStrength;
					changed=true;
				}
				for(double pageStrength:maleRates) {
					if(pageStrength < thresholdM) break;
					strength /= pageStrength;
					changed=true;
				}
				if(changed) {
					context.write(new Text(strength+" "+gender), new Text());
				}
			}
		}
	}

	@Override
	public int run(String[] args) throws Exception {

		Job job = new Job(_conf, "Bayes Test");

		job.setJarByClass(BayesTest3.class);
		job.setMapperClass(TestMapper.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setNumReduceTasks(0);
		
		System.out.println(_conf.get("bayes.testinput"));
		System.out.println(_conf.get("bayes.testoutput"));
		
		FileInputFormat.addInputPaths(job, _conf.get("bayes.testinput"));
		FileOutputFormat.setOutputPath(job, new Path(_conf.get("bayes.testoutput")));
		
//		System.out.println(job.waitForCompletion(true) ? 0 : 1);
		return job.waitForCompletion(true) ? 0 : 1;
	}
	
	public static void main(String[] args) throws Exception {
		int ret = ToolRunner.run(new BayesTest3(), args);
		if (ret != 0) {
			System.err.println("Job Failed!");
			System.exit(ret);
		}
	}
}

