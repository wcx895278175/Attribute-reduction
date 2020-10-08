package org.apache.hadoop.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.examples.Condition1.WordCountMap;
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
import org.apache.hadoop.mapreduce.lib.map.MultithreadedMapper;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Condition3  extends Configured implements Tool {
	 static Configuration conf = null;
	//step 1: map class
		public static class WordCountMap extends Mapper<LongWritable, Text, Text,LongWritable>{
			private Text mapOutputKey = new Text();
			private LongWritable mapOutputValue = new LongWritable(1);
			public void map(LongWritable mapInputKey,Text mapInputValue,Context context) throws IOException, InterruptedException{
				String lineValue = mapInputValue.toString();
			
				String[] lines = lineValue.split(",");
			
				Configuration temp = context.getConfiguration();
				String attr1 = temp.get("curCondition");
				String decision = temp.get("decision");
				String AttributeNum = temp.get("AttributeNum");
				String nextCon = temp.get("nextConditon");
				nextCon =  (String) nextCon.subSequence(1, nextCon.length()-1);
//				System.out.println("map:"+attr1+"\n"+decision+"\n"+nextCon);
			   
				String [] nextCondition = nextCon.split(", ");
				ArrayList<Integer> toBeUsedConditions = new ArrayList<Integer>(0);
		         
		        for(String word : nextCondition){
		            toBeUsedConditions.add(Integer.parseInt(word));
		        }
//		        System.out.println("next condition:"+toBeUsedConditions+" "+toBeUsedConditions.size());
		        
		        for(int i = 0 ; i < toBeUsedConditions.size(); i++){
		        	StringBuilder sb = new StringBuilder();
				    sb.append("C"+attr1+",");				  
				    sb.append(lines[Integer.parseInt(attr1)-1]+",");		
				    sb.append("C"+toBeUsedConditions.get(i)+",");				    
		        	sb.append(lines[toBeUsedConditions.get(i)-1]+" ");
		        	sb.append(lines[Integer.parseInt(AttributeNum)]);
		        	mapOutputKey.set(sb.toString());
		        	context.write(mapOutputKey, mapOutputValue);
//		        	System.out.println("mapOutputKey:"+mapOutputKey+"     value:"+mapOutputValue);
		        }
		       
				
			}
		}	
		
		
	// step3: Driver
		public static class WordCountReduce extends Reducer<Text, LongWritable, Text, LongWritable>{
			private Text reduceOutputkey = new Text();
			private LongWritable reduceOutputValue = new LongWritable();
			
			public void reduce(Text key,Iterable<LongWritable> values,Context context) throws IOException, InterruptedException{
				long sum = 0;

				for(LongWritable value : values){
					sum = sum + value.get();
					
				}
				String[] conkey = key.toString().split(" ");
			
				reduceOutputkey.set(conkey[0]);
				
				reduceOutputValue.set(sum);
//				System.out.println("reduce outputkey:" + reduceOutputkey);
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
			private Text reduceOutputkey = new Text();
			private FloatWritable reduceOutputValue = new FloatWritable();
			public void reduce(Text key,Iterable<LongWritable> values,Context context) throws IOException, InterruptedException{				
				float sum = 0f;			
//				System.out.println("key3:"+key);
			
				String[] conkey = key.toString().split(",");
				ArrayList<Float> val = new ArrayList<Float>(0);
				for(LongWritable value : values){
					val.add((float)value.get());
					sum = sum + (float)value.get();
				}
			

//				System.out.println("sum:"+sum + " ");
				float sig_t = 0;
				for(int i = 0  ; i < val.size(); i++){
//						System.out.println("     "+val.get(i)+" "+(val.get(i)/sum)+" "+ (   (float)  Math.log((val.get(i) / sum) ) / Math.log((double)2) ));
					sig_t += (val.get(i)/sum)*(   (float)  Math.log((double)(val.get(i) / sum) ) / Math.log((double)2) );
				}

				Configuration temp = context.getConfiguration();
				String Str_num_E = temp.get("NumofE");
				float numofE  = Float.valueOf(Str_num_E);
				sig_t = sig_t * (sum /numofE );
//				System.out.println("ans: "+sig_t);
				reduceOutputkey.set(key);
				reduceOutputValue.set(sig_t);
				context.write(reduceOutputkey,reduceOutputValue);

//				System.out.println("outkey:"+reduceOutputkey +"     outvalue:"+reduceOutputValue);
			}
			
		
		}
		public static class WordCountMap3 extends Mapper<LongWritable, Text, Text,FloatWritable>{
			private Text mapOutputKey = new Text();
			private FloatWritable mapOutputValue = new FloatWritable();
			public void map(LongWritable mapInputKey,Text mapInputValue,Context context) throws IOException, InterruptedException{
//				System.out.println("map3:"+mapInputKey+"              map3value:"+mapInputValue);
				String[] val  =  mapInputValue.toString().split("\t");
				String[] tem = val[0].toString().split(",");
//				System.out.println(tem[2]);
			
				Float value = Float.valueOf(val[1]);
				if(value < 0 ){
					value *=-1;
				}
				mapOutputKey.set(tem[2]);
				mapOutputValue.set(value);
//				System.out.println("key3: "+mapOutputKey+"   value3: "+mapOutputValue);
				context.write(mapOutputKey, mapOutputValue);
			}
		}
		public static class WordCountReduce3 extends Reducer<Text, FloatWritable, Text, FloatWritable>{
			private Text reduceOutputkey = new Text();
			private FloatWritable reduceOutputValue = new FloatWritable();
			public void reduce(Text key,Iterable<FloatWritable> values,Context context) throws IOException, InterruptedException{				
				float sum = 0f;			
//				System.out.println("key:"+key);
			
				String conkey = key.toString();
				ArrayList<Float> val = new ArrayList<Float>(0);
				for(FloatWritable value : values){
//						System.out.print(value+" ");
					val.add((float)value.get());
					sum = sum + (float)value.get();
				}
//				String outkey3 = "";
//				if(sum > max_sum){
//					max_sum = sum;
//					outkey3 = conkey;
//				}

//				System.out.println("maxsum:"+sum + " "+"key3:"+conkey);
	            
				reduceOutputkey.set(conkey);
				reduceOutputValue.set(sum);
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
//		conf.set("mapred.map.multithreadedrunner.class", WordCountMap.class.getCanonicalName());
//		conf.set("mapred.map.multithreadedrunner.threads", "8");
//		job.setMapperClass(MultithreadedMapper.class);
			
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
//		FileInputFormat.setMinInputSplitSize(job,67108864);
////		FileInputFormat.setMinInputSplitSize(job, 301349250);   
//		FileInputFormat.setMaxInputSplitSize(job, 10000);
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
//	    conf.set("mapred.map.multithreadedrunner.class", WordCountMap2.class.getCanonicalName());
//		conf.set("mapred.map.multithreadedrunner.threads", "8");
//	    job1.setMapperClass(MultithreadedMapper.class);
		

	    //3.set jobinput ,map,reduce,output	
	    //3.1 set jar
		job1.setJarByClass(solve_job1.class);	// run jar
		//3.2 map
		job1.setMapperClass(WordCountMap2.class);
		job1.setMapOutputKeyClass(Text.class);
		job1.setMapOutputValueClass(LongWritable.class);
		//3.3 reduce
		job1.setReducerClass(WordCountReduce2.class);
		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(FloatWritable.class);
		//3.4 output
		Path outputPath2 = new Path(args[3]);
		FileSystem  fileSystem2 = outputPath2.getFileSystem(jobH_C_D);
		if(fileSystem.exists(outputPath2)){
			fileSystem.delete(outputPath2,true);
		}
//		FileInputFormat.setMinInputSplitSize(job1,67108864);
//		FileInputFormat.setMinInputSplitSize(job1, 301349250);   
//		FileInputFormat.setMaxInputSplitSize(job1, 10000);
		FileInputFormat.addInputPath( job1,  new Path(args[1]));//3.1 input
		FileOutputFormat.setOutputPath(job1, outputPath2);
	
		//4. submit job
	    job1.waitForCompletion(true);

	  
	    
		Configuration job3 = getConf();
	    Job job2 = new Job(job3 , "job2");
//	    conf.set("mapred.map.multithreadedrunner.class", WordCountMap2.class.getCanonicalName());
//		conf.set("mapred.map.multithreadedrunner.threads", "8");
//	    job1.setMapperClass(MultithreadedMapper.class);
		

	    //3.set jobinput ,map,reduce,output	
	    //3.1 set jar
		job2.setJarByClass(solve_job1.class);	// run jar
		//3.2 map
		job2.setMapperClass(WordCountMap3.class);
		job2.setMapOutputKeyClass(Text.class);
		job2.setMapOutputValueClass(FloatWritable.class);
		//3.3 reduce
		job2.setReducerClass(WordCountReduce3.class);
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(FloatWritable.class);
		//3.4 output
		Path outputPath3 = new Path(args[4]);
		FileSystem  fileSystem3 = outputPath3.getFileSystem(job3);
		if(fileSystem.exists(outputPath3)){
			fileSystem.delete(outputPath3,true);
		}
//		FileInputFormat.setMinInputSplitSize(job2,67108864);
//		FileInputFormat.setMinInputSplitSize(job, 301349250);   
//		FileInputFormat.setMaxInputSplitSize(job2, 10000);
		FileInputFormat.addInputPath( job2,  new Path(args[3]));//3.1 input
		FileOutputFormat.setOutputPath(job2, outputPath3);
	
		//4. submit job
	    job2.waitForCompletion(true);

	    boolean isSuccess = job2.waitForCompletion(true);
	    
		return isSuccess ? 0 : 1;
	}
		
	// run program
	public static float[][] start(String[] args,int currentAttribute,int AttributeNum,float numberOfElements,Vector<Integer> allowedAttributes) throws Exception {
		conf = new Configuration();

		
		Value2sum2 relyDegreeOfDToC = null;
	
		ArrayList<String> toBeUsedConditions = new ArrayList<String>(0);
		for (Integer i:allowedAttributes) {
			toBeUsedConditions.add(String.valueOf(i));
	     }
//		toBeUsedConditions.add(String.valueOf(AttributeNum+1));
		String nextcondition = toBeUsedConditions.toString();
		
		FileSystem fileSystem = FileSystem.get(conf);
	    conf.set("curCondition", String.valueOf(currentAttribute));//条件属性（１,2,3...）,单个值
	    conf.set("nextConditon",nextcondition );
	    conf.set("AttributeNum",String.valueOf(AttributeNum) );
	    conf.set("decision",String.valueOf(AttributeNum+1) );
		conf.set("NumofE", String.valueOf(numberOfElements));
		
		int status1 = ToolRunner.run(conf, new Condition3(), args); 
		
		
		relyDegreeOfDToC = new Value2sum2(fileSystem);

		return relyDegreeOfDToC.readFile(fileSystem, args[4],AttributeNum, currentAttribute);
		
	}
	

}