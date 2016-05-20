package com.hp.processor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.SplittableCompressionCodec;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import com.lqyandpy.DBN.DBNTrain;
import com.lqyandpy.DBN.SimpleDBN;
import com.lqyandpy.RBM.Data;
import com.lqyandpy.RBM.L1;
import com.lqyandpy.RBM.WeightDecay;
import com.lqyandpy.crf.ANN;
import com.lqyandpy.crf.TanhFunction;
import com.lqyandpy.crf.Trainer;


public class DBNpretrain extends Configured {
	
	public static SimpleDBN dbn;
	public static DBN_pretrain_params dbn_pretrain_params;
	public static Path mid_dir; 
	public static Path dst_dir;
	public static FileSystem fs;
	public static class DBN_pretrain_params
	{
		public int[] hidden_nums;
		public double learning_rate;
		public double stop_threshold;
		public WeightDecay wd_func;
		public int max_try;
		public int batch_size;			
	}
	
	public static double[][] data ;
	
	public static void setup_paramas
	(int[] hidden_nums,double learning_rate,
			double stop_threshold,WeightDecay wd_func,int max_try,int batch_size,int input_size,Path mdir,Path ddir)
	{
		DBNpretrain.dbn = new SimpleDBN();
		DBNpretrain.dbn.Layers = hidden_nums.length;
		DBNpretrain.dbn.constructDBN(input_size, hidden_nums, true);
		DBNpretrain.dbn_pretrain_params = new DBN_pretrain_params();
		DBNpretrain.dbn_pretrain_params.hidden_nums = hidden_nums;
		DBNpretrain.dbn_pretrain_params.learning_rate = learning_rate;
		DBNpretrain.dbn_pretrain_params.batch_size = batch_size;
		DBNpretrain.dbn_pretrain_params.max_try = max_try;
		DBNpretrain.dbn_pretrain_params.stop_threshold = stop_threshold;
		DBNpretrain.dbn_pretrain_params.wd_func = wd_func;
		DBNpretrain.mid_dir = mdir;
		DBNpretrain.dst_dir = ddir;
	}
	
	public static class ArrayWritable extends ArrayList<ArrayList<Double>> implements Writable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		//public ArrayList<ArrayList<Double> > content;
		public ArrayWritable()
		{
			//resetArrayList();
			this.clear();
		}
		public void resetArrayList()
		{
			//content = new ArrayList<ArrayList<Double>>();
			this.clear();
		}
		@Override
		public void readFields(DataInput arg0) throws IOException {
			// TODO Auto-generated method stub
			String[] s = arg0.readUTF().split("&");
			this.clear();
			//content = new ArrayList<ArrayList<Double>>(s.length);
			for(int i = 0;i < s.length;++i)
			{
				String[] si = s[i].split("\t"); 
				ArrayList<Double> tmp = new ArrayList<Double>(si.length);
				for(int j = 0;j < si.length;++j)
					tmp.add(Double.parseDouble(si[j]));
				this.add(tmp);
			}
			
		}

		@Override
		public void write(DataOutput arg0) throws IOException {
			// TODO Auto-generated method stub
			StringBuilder sb = new StringBuilder();
			for(int i = 0;i < this.size();++i)
			{
				for(int j = 0;j < this.get(i).size();++j)
				{
					sb.append(this.get(i).get(j));
					sb.append('\t');
				}
				sb.replace(sb.length()-1, 1, "&");
			}
			sb.replace(sb.length()-1, 1, "\n");
			arg0.writeUTF(sb.toString());
		}
		
	}
	public static class MyRecordReader extends RecordReader<LongWritable,ArrayWritable>
	{
		private LineRecordReader line_reader = new LineRecordReader();
		public ArrayWritable value = new ArrayWritable();
		public MyRecordReader()
		{
			line_reader = new LineRecordReader();
			value = new ArrayWritable();
		}
		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			line_reader.close();
		}
		@Override
		public LongWritable getCurrentKey() throws IOException,
				InterruptedException {
			// TODO Auto-generated method stub
			return new LongWritable(1);
		}
		@Override
		public ArrayWritable getCurrentValue() throws IOException,
				InterruptedException {
			// TODO Auto-generated method stub
			return value;
		}
		@Override
		public float getProgress() throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			return line_reader.getProgress();
		}
		@Override
		public void initialize(InputSplit arg0, TaskAttemptContext arg1)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			line_reader.initialize(arg0, arg1);
		}
		@Override
		public boolean nextKeyValue() throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			this.value.resetArrayList();
			while(line_reader.nextKeyValue())
			{
				
				String[] s = line_reader.getCurrentValue().toString().split(",");
				ArrayList<Double> l = new ArrayList<Double>();
				
				for(int i = 0;i < s.length;++i)
					l.add(Double.parseDouble(s[i]));
				this.value.add(l);
			}
			if(this.value.isEmpty())
				return false;
			else
				return true;
		}
		
		
	}
	public static class MyInputFormat extends FileInputFormat<LongWritable, ArrayWritable>
	{
		
		public RecordReader<LongWritable, ArrayWritable> 
	    createRecordReader(InputSplit split,
	                       TaskAttemptContext context) {
	    return new MyRecordReader();
		}

		@Override
		  protected boolean isSplitable(JobContext context, Path file) {
		    CompressionCodec codec = 
		      new CompressionCodecFactory(context.getConfiguration()).getCodec(file);
		    if (null == codec) {
		      return true;
		    }
		    return codec instanceof SplittableCompressionCodec;
		  }
	}
	public static class PretrainMapper extends Mapper<LongWritable,ArrayWritable,IntWritable,Text>
	{
		private IntWritable one = new IntWritable(1);	
		public void map(LongWritable key, ArrayWritable value, Context context) throws IOException, InterruptedException 
        {
			if(value.isEmpty())
				return;
			data = new double[value.size()][value.get(0).size()];
			for(int i = 0;i < value.size();++i)
				for(int j = 0;j < value.get(i).size();++j)
					data[i][j] = value.get(i).get(j);
			Data tempD = new Data(data,false,true);
			
			DBNTrain train = new DBNTrain(tempD,DBNpretrain.dbn);
			train.greedyLayerwiseTraining(dbn_pretrain_params.stop_threshold, dbn_pretrain_params.learning_rate, dbn_pretrain_params.wd_func, dbn_pretrain_params.max_try, dbn_pretrain_params.batch_size);
			context.write(one, new Text(SimpleDBN.toBytes(dbn)));
        }
	}
	public static class PretrainReducer extends Reducer<IntWritable,Text,IntWritable,Text>
	{
		public void reduce(IntWritable key,Iterable<Text> value,Context context) throws IOException, InterruptedException
		{
			dbn = null;
			for(Text v:value)
			{
//				SimpleDBN d = new SimpleDBN();
//				d.RebuildDBNbyBytes(v.getBytes());
//				DBNpretrain.dbn.combineDBN(d);
				if(dbn == null)
					dbn = SimpleDBN.RebuildDBNbyBytes(v.getBytes());
				else
					dbn.combineDBN(SimpleDBN.RebuildDBNbyBytes(v.getBytes()));
			}
			FSDataOutputStream out = fs.create(mid_dir);
			out.write(SimpleDBN.toBytes(dbn));
			out.close();
			//dbn.PermanentDBN(DBNpretrain.mid_dir);
			context.write(new IntWritable(1), new Text(SimpleDBN.toBytes(DBNpretrain.dbn)));
		}
	}
	
	public static class ANNMapper extends Mapper<LongWritable,ArrayWritable,IntWritable,Text>
	{
		public void map(LongWritable key,ArrayWritable value,Context context)
		{
			if(value.isEmpty())
				return;
			data = new double[value.size()][value.get(0).size()];
			for(int i = 0;i < value.size();++i)
				for(int j = 0;j < value.get(i).size();++j)
					data[i][j] = value.get(i).get(j);
			byte[] model = new byte[10000];
			try {
				FSDataInputStream in = fs.open(DBNpretrain.mid_dir);
				int cnt = in.read(model);
				in.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
//			DBNpretrain.dbn = new SimpleDBN();
//			DBNpretrain.dbn.RebuildDBN(DBNpretrain.mid_dir);
			
			DBNpretrain.dbn = SimpleDBN.RebuildDBNbyBytes(model);
			
			double[][] w_for_ann = DBNpretrain.dbn.get_ann_wight(1); 
			ANN ann = new ANN();
			ann.InitAnn(w_for_ann,DBNpretrain.dbn.ann_bias,new TanhFunction(),new TanhFunction());
			
			Trainer tempTR=new Trainer();
			tempTR.setLearningRate(0.2);

			tempTR.Train(ann,data, 0.01,50);
			try {
				context.write(new IntWritable(1), new Text(ANN.serialize(ann)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static class ANNReducer extends Reducer<IntWritable,Text,IntWritable,Text>
	{
		public void reduce(IntWritable key, Iterable<Text> values,Context context)
		{
			ANN ann = null;
			for(Text v:values)
			{
				if(ann == null)
					ann = ANN.deserialize(v.getBytes());
				else
					ann.combineANN(ANN.deserialize(v.getBytes()));
			}
			try {
				
				FSDataOutputStream out = fs.create(dst_dir);
				out.write(ANN.serialize(ann));
				out.close();
				context.write(new IntWritable(1), new Text(ANN.serialize(ann)) );
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
	    Configuration conf = new Configuration();
	    //conf.addResource("/home/hadoop/hadoop-1.2.1/conf/core-site.xml");
	    //conf.addResource("/home/hadoop/hadoop-1.2.1/conf/hdfs-site.xml");
	    DBNpretrain.fs = FileSystem.get(new URI("hdfs://localhost:9000"),conf);
	    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
	    if (otherArgs.length != 2) {
	      System.err.println("Usage: wordcount <in> <out>");
	      System.exit(2);
	    }
	    Path p1 = new Path(otherArgs[1]+"/pretrain");
	    if(fs.exists(p1))
	    	fs.delete(p1,true);
	    Path p2 = new Path(otherArgs[1]+"/ann_train");
	    if(fs.exists(p2))
	    	fs.delete(p2,true);
	    Path p1o_file = new Path(p1.getParent()+"/mid_model");
	    Path final_file = new Path(p2.getParent() + "/dst_model");
	    
	    DBNpretrain.setup_paramas(new int[]{10,10}, 0.001, 0.1, new L1(), 50, 2,6,p1o_file,final_file);

	    Job job1 = new Job(conf, "dbn pretrain");
	    job1.setJarByClass(DBNpretrain.class);
	    job1.setMapperClass(PretrainMapper.class);
	    job1.setReducerClass(PretrainReducer.class);
	    job1.setMapOutputKeyClass(IntWritable.class);
	    job1.setMapOutputValueClass(Text.class);
	    job1.setOutputKeyClass(IntWritable.class);
	    job1.setOutputValueClass(Text.class);
	    job1.setInputFormatClass(MyInputFormat.class);
	    MyInputFormat.addInputPath(job1, new Path(otherArgs[0]));
	    
	    
	    FileOutputFormat.setOutputPath(job1, p1);
	    
	    Job job2 = new Job(conf, "ann train");
	    job2.setJarByClass(DBNpretrain.class);
	    job2.setMapperClass(ANNMapper.class);
	    job2.setReducerClass(ANNReducer.class);
	    job2.setMapOutputKeyClass(IntWritable.class);
	    job2.setMapOutputValueClass(Text.class);
	    job2.setOutputKeyClass(IntWritable.class);
	    job2.setOutputValueClass(Text.class);
	    job2.setInputFormatClass(MyInputFormat.class);
	    
	    MyInputFormat.addInputPath(job2, new Path(otherArgs[0]));
	    
	    FileOutputFormat.setOutputPath(job2, p2);
	    
	    JobControl jc = new JobControl("dbn train");
	    ControlledJob cj1 = new ControlledJob(conf);
	    cj1.setJob(job1);
	    ControlledJob cj2 = new ControlledJob(conf);
	    cj2.setJob(job2);
	    cj2.addDependingJob(cj1);
	    jc.addJob(cj1);
	    jc.addJob(cj2);
	    
	    jc.run();
	    
	    
	    //System.exit(job1.waitForCompletion(true) ? 0 : 1);

	}
}

