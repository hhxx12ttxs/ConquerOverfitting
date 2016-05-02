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


public class BayesTest1 implements Tool {

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
			
			if (mfCnt == null) {
				mfCnt = new HashMap<String, Double>();
				
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(context.getConfiguration().get("train.res"))));
				
				String str;
				while ((str = br.readLine()) != null) {
					String[] splits = str.split("\\p{Blank}+");
					mfCnt.put(splits[0], Double.parseDouble(splits[1]));	
				}
				br.close();
			}
			
			final int TOP_NUM = Integer.parseInt(context.getConfiguration().get("threshold.top"));
			final int BOTTOM_NUM = Integer.parseInt(context.getConfiguration().get("threshold.bottom"));
			
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
				          if (b2.equals(b1) ) return 0;
				          return -1;
				   }
				});
			
			double rate = 1.0;
			
			
			if(femaleRates.size() >= BOTTOM_NUM + TOP_NUM) {
				for(int i = 0; i < TOP_NUM; i++) {
					rate*=femaleRates.get(i);
				}
				for(int i = femaleRates.size() - 1; i >  femaleRates.size() - 1 - BOTTOM_NUM; i--) {
					rate*=femaleRates.get(i);
				}
				context.write(new Text(rate+" "+gender), new Text());
			}
		}
	}

	@Override
	public int run(String[] args) throws Exception {

		Job job = new Job(_conf, "Bayes Test");

		job.setJarByClass(BayesTest1.class);
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
		int ret = ToolRunner.run(new BayesTest1(), args);
		if (ret != 0) {
			System.err.println("Job Failed!");
			System.exit(ret);
		}
	}
}

