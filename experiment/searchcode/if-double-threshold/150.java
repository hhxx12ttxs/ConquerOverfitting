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


public class BayesTest2 implements Tool {

	protected Configuration _conf = new Configuration();

	@Override
	public Configuration getConf() {
		return _conf;
	}

	@Override
	public void setConf(Configuration conf) {
		_conf = conf;

	}

	public static class TestMapper extends Mapper<Object, Text, Text, Text> {
		
		static Map<String, Double> mfCnt = null;
		
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			
			
//			final int THRESHOLD = Integer.parseInt(context.getConfiguration().get("feacnt.threshold", "200"));
//			final int randomRate = Integer.parseInt(context.getConfiguration().get("randomRate", "10"));
			
//			Random random = new Random();
//			int n = random.nextInt(randomRate);
//			if( n != 1) {
//				return;
//			}
			
			final double threshold = Double.parseDouble(context.getConfiguration().get("rate.threshold"));
			
			if (mfCnt == null) {
				mfCnt = new HashMap<String, Double>();
				
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(context.getConfiguration().get("train.res"))));
				
				String str;
				while ((str = br.readLine()) != null) {
					String[] splits = str.split("\\p{Blank}+");
					double rate=Double.parseDouble(splits[1]);
					if(rate > threshold) {
						mfCnt.put(splits[0], rate);
					} else {
						break;
					}
				}
				br.close();
			}
			
			
			
			int NUM_ALL = Integer.parseInt(context.getConfiguration().get("num.divide").split("_")[0]);
			int NUM_TEST = Integer.parseInt(context.getConfiguration().get("num.divide").split("_")[1]);
			int lineno = Integer.parseInt(value.toString().split("#", 2)[0]);
			
			if (lineno % NUM_ALL != NUM_TEST)
				return;
			
			String line = value.toString().split("#", 2)[1];
			String[] splits = line.split("\\p{Blank}+");
			String gender = splits[0];
			List<Double> femaleRates = new ArrayList<Double>();
			gender = "0".equals(gender) ? "M" : "F";
			
			for (String seg : splits) {
				String[] kv = seg.split(":");
				if (kv.length != 2)
					continue;
				if (mfCnt.containsKey(kv[0])) {
					femaleRates.add(mfCnt.get(kv[0]));
				}
			}
			
			Collections.sort(femaleRates,new Comparator<Double>(){
				   public int compare(Double b1, Double b2) {
				          if( b2 > b1) return 1;
				          if (b2 == b1 ) return 0;
				          return -1;
				   }
				});
			
			if(femaleRates.size() > 0) {
				double rate = 1.0;
				
				for(int i = 0; i < femaleRates.size(); i++) {
					rate*=femaleRates.get(i);
				}
					
				context.write(new Text(rate+" "+gender), new Text());
			}
		}
	}

	@Override
	public int run(String[] args) throws Exception {

		Job job = new Job(_conf, "Bayes Test");

		job.setJarByClass(BayesTest2.class);
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
		int ret = ToolRunner.run(new BayesTest2(), args);
		if (ret != 0) {
			System.err.println("Job Failed!");
			System.exit(ret);
		}
	}
}

