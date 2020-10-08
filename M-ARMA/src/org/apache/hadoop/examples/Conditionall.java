package org.apache.hadoop.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Conditionall extends Configured implements Tool {
	 static Configuration conf = null;
	//step 1: map class
		public static class WordCountMap extends Mapper<LongWritable, Text, Text,LongWritable>{
			private Text mapOutputKey = new Text();
			private LongWritable mapOutputValue = new LongWritable(1);
			public void map(LongWritable mapInputKey,Text mapInputValue,Context context) throws IOException, InterruptedException{
				String lineValue = mapInputValue.toString();
			
				String[] lines = lineValue.split(",");
			
				Configuration temp = context.getConfiguration();
				String attr1 = temp.get("numall");
//				System.out.println("##################"+attr1);
				attr1 = (String) attr1.subSequence(1, attr1.length()-1);
				String [] attr = attr1.split(", ");
				
				ArrayList<Integer> toBeUsedConditions = new ArrayList<Integer>(0);
		         
		        for(String word : attr){
		            int tt = Integer.parseInt(word);
		            toBeUsedConditions.add(tt);
		        }
//		        System.out.println(toBeUsedConditions);
		        StringBuilder sb = new StringBuilder();
		        for(int i = 0 ; i < toBeUsedConditions.size() ;i++ ){
		        	int number = toBeUsedConditions.get(i);
		        	sb.append(lines[number-1]+",");
//		        	System.out.println(i+" "+ number+" "+lines[number-1] );
		        }
//		        System.out.println();
		        String tem = sb.substring(0,sb.length()-1);
//	            System.out.println("--------->"+tem);

				mapOutputKey.set(tem);
				context.write(mapOutputKey, mapOutputValue);
			}
		}	
		
		
	// step3: Driver
		public static class WordCountReduce extends Reducer<Text, LongWritable, Text, LongWritable>{
			private Text reduceOutputkey = new Text();
			private LongWritable reduceOutputValue = new LongWritable();
			
			public void reduce(Text key,Iterable<LongWritable> values,Context context) throws IOException, InterruptedException{
				long sum = 0;
//				System.out.println(key);
				String[] conkey = key.toString().split(",");
				for(LongWritable value : values){
					sum = sum + value.get();
				}
				StringBuilder condition = new StringBuilder();//首先获取条件属性等价类
			    for(int i = 0 ; i < conkey.length -1  ;i++ ){
			    	condition.append(conkey[i]+",");
			    }

			    String tem = condition.substring(0,condition.length()-1);
				reduceOutputkey.set(tem);
				reduceOutputValue.set(sum);
//				System.out.println("outkey:"+reduceOutputkey +"     outvalue:"+reduceOutputValue);
				context.write(reduceOutputkey,reduceOutputValue);
				
			}
		}
		public static class WordCountMap2 extends Mapper<LongWritable, Text, Text,LongWritable>{
			private Text mapOutputKey = new Text();
			private LongWritable mapOutputValue = new LongWritable(1);
			public void map(LongWritable mapInputKey,Text mapInputValue,Context context) throws IOException, InterruptedException{
			
				String[] val  =  mapInputValue.toString().split("\t");
				mapOutputKey.set(val[0]);
				mapOutputValue.set(Integer.valueOf(val[1]));
//				System.out.println("key2: "+mapOutputKey+"   value2: "+mapOutputValue);
				context.write(mapOutputKey, mapOutputValue);
			}
		}
		public static class WordCountReduce2 extends Reducer<Text, LongWritable, Text, FloatWritable>{
			private Text reduceOutputkey = null;
			private FloatWritable reduceOutputValue = new FloatWritable();
			public void reduce(Text key,Iterable<LongWritable> values,Context context) throws IOException, InterruptedException{				
				float sum = 0f;			
				ArrayList<Float> val = new ArrayList<Float>(0);
				for(LongWritable value : values){
//						System.out.print(value+" ");
					val.add((float)value.get());
					sum = sum + (float)value.get();
				}

				float sig_t = 0;
				for(int i = 0  ; i < val.size(); i++){
//						System.out.println("     "+val.get(i)+" "+(val.get(i)/sum)+" "+ (   (float)  Math.log((val.get(i) / sum) ) / Math.log((double)2) ));
					sig_t += (val.get(i)/sum)*(   (float)  Math.log((double)(val.get(i) / sum) ) / Math.log((double)2) );
				}
				
				Configuration temp = context.getConfiguration();
				String Str_num_E = temp.get("NumofE");
				float numofE  = Float.valueOf(Str_num_E);
				sig_t = sig_t * (sum /numofE );
				reduceOutputValue.set(sig_t);
				context.write(reduceOutputkey,reduceOutputValue);

//				System.out.println("outkey:"+reduceOutputkey +"     outvalue:"+reduceOutputValue);
			}
			
		
		}
		
	public int run(String[] args) throws Exception {
		//1. get configuration
		Configuration conf = getConf();
		
		//2. create job
		Job job = Job.getInstance(conf,this.getClass().getSimpleName());
		// run jar
		job.setJarByClass(this.getClass());
		
		//3.set job
		// input ,map,reduce,output
		//3.1 input
		
		Path inPath = new Path(args[0]);
		FileInputFormat.addInputPath( job, inPath);
		
		//3.2 map
		job.setMapperClass(WordCountMap.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(LongWritable.class);
		
		//3.3 reduce
		job.setReducerClass(WordCountReduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);
		
		//3.4 output
		Path outputPath = new Path(args[1]);
		org.apache.hadoop.fs.FileSystem  fileSystem = outputPath.getFileSystem(conf);
		if(fileSystem.exists(outputPath)){
		    fileSystem.delete(outputPath,true);
		}
		FileOutputFormat.setOutputPath(job, outputPath);
		
		//4. submit job
		job.waitForCompletion(true);
				

		Configuration jobH_C_D = getConf();//1. get configuration
		//传递参数
	
	    Job job1 = new Job(jobH_C_D , "jobH");
	    //3.set jobinput ,map,reduce,output	
	    //3.1 set jar
		job1.setJarByClass(this.getClass());	// run jar
		//3.2 map
		job1.setMapperClass(WordCountMap2.class);
		job1.setMapOutputKeyClass(Text.class);
		job1.setMapOutputValueClass(LongWritable.class);
		//3.3 reduce
		job1.setReducerClass(WordCountReduce2.class);
		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(FloatWritable.class);
		//3.4 output
		Path outputPath2 = new Path(args[2]);
		FileSystem  fileSystem2 = outputPath2.getFileSystem(jobH_C_D);
		if(fileSystem.exists(outputPath2)){
			fileSystem.delete(outputPath2,true);
		}
		FileInputFormat.addInputPath( job1,  new Path(args[1]));//3.1 input
		FileOutputFormat.setOutputPath(job1, outputPath2);
		
		//4. submit job
	    job1.waitForCompletion(true);

	    boolean isSuccess = job1.waitForCompletion(true);
		return isSuccess ? 0 : 1;
	}
		
	public static Float start(String[] args,int AttributeNum,float numberOfElements) throws Exception {
		conf = new Configuration();
	
		Value2sum value2sum3 = null;
		
		ArrayList<String> toBeUsedConditionsAll = new ArrayList<String>(0);
		for (int i = 1; i <= AttributeNum ; i++) {	//这里的attributenum包括决策属性
			toBeUsedConditionsAll.add(String.valueOf(i));
		}
		
		String conditionAll = toBeUsedConditionsAll.toString();
		FileSystem fileSystem = FileSystem.get(conf);
		
		conf.set("numall", conditionAll);//条件属性（１,2,3...）,单个值
		String numOfe = String.valueOf(numberOfElements);
		conf.set("NumofE", numOfe);
		
		int status1 = ToolRunner.run(conf,new Conditionall(), args);
		value2sum3 = new Value2sum(fileSystem);
     
		value2sum3.read(fileSystem, args[2]);				
		float sig_D_C  = value2sum3.calculate();

		return sig_D_C;
	}
	

}