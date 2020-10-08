package org.apache.hadoop.examples;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Condition_D extends Configured implements Tool{
	 static Configuration conf = null;
	//step 1: map class
		public static class WordCountMap extends Mapper<LongWritable, Text, Text,LongWritable>{
			private Text mapOutputKey = new Text();
			private LongWritable mapOutputValue = new LongWritable(1);
			public void map(LongWritable mapInputKey,Text mapInputValue,Context context) throws IOException, InterruptedException{
//				System.out.print("Before Mapper:" + mapInputKey + "   " + mapInputValue +"\n");
//				System.out.print(  id +" "+mapInputValue.getLength()+"  "+mapOutputValue  +"\n");
				
				String lineValue = mapInputValue.toString();

				String[] lines = lineValue.split(",");
			
				Configuration temp = context.getConfiguration();
				String attr1 = temp.get("numD");
//				System.out.println("##################"+attr1);
				attr1 = (String) attr1.subSequence(1, attr1.length()-1);
				String [] attr = attr1.split(", ");
				
				ArrayList<Integer> toBeUsedConditions = new ArrayList<Integer>(0);
		         
		        for(String word : attr){
		            int tt = Integer.parseInt(word);
		            toBeUsedConditions.add(tt);
		        }
		        
		        StringBuilder sb = new StringBuilder();
		        for(int i = 0 ; i < toBeUsedConditions.size() ;i++ ){
		        	int number = toBeUsedConditions.get(i);
		        	sb.append(lines[number-1]+",");
//		        	System.out.println(" "+ number+" "+lines[number-1] );
		        }
//		        System.out.println();
		        String tem = sb.substring(0,sb.length()-1);
//	            System.out.println("--------->"+tem);
                    
	        	
				mapOutputKey.set(tem);
//				System.out.print( mapOutputKey+" "+mapOutputValue +"\n");
				context.write(mapOutputKey, mapOutputValue);
			}
		}	
			
	//step 2: reduce class
	public static class WordCountReduce extends Reducer<Text, LongWritable, Text,  FloatWritable >{
		private Text reduceOutputkey = null;
		private FloatWritable reduceOutputValue = new FloatWritable();
		
		public void reduce(Text key,Iterable<LongWritable> values,Context context) throws IOException, InterruptedException{
			long sum = 0;
			
			for(LongWritable value : values){
				sum = sum + value.get();
				
			}
			Configuration temp = context.getConfiguration();
			String Str_num_E = temp.get("NumofE");
			float numofE  = Float.valueOf(Str_num_E);
			
			float ans =  (sum /numofE)*
					(    (float)Math.log(  (double)sum /numofE   )/ (float)Math.log ((double)2)  );	
			
			reduceOutputValue.set(ans);
			context.write(reduceOutputkey,reduceOutputValue);
			
		}
	}
		
	// step3: Driver
	
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
		job.setOutputValueClass(FloatWritable.class);
//		job.setOutputN
		//3.4 output
		Path outputPath = new Path(args[1]);
		org.apache.hadoop.fs.FileSystem  fileSystem = outputPath.getFileSystem(conf);
		    if(fileSystem.exists(outputPath)){
		        fileSystem.delete(outputPath,true);
		    }
		FileOutputFormat.setOutputPath(job, outputPath);
		
		//4. submit job
		boolean isSuccess = job.waitForCompletion(true);
				
		return isSuccess ? 0 : 1;
	}
		
	// run program
	public static Float start(String[] addr,int AttributeNum,float numberOfElements) throws Exception {
		conf = new Configuration();
		
		Value2sum relyDegreeOfDToC = null;
		
		ArrayList<String> toBeUsedCondition_D = new ArrayList<String>(0);

		toBeUsedCondition_D.add(String.valueOf(AttributeNum));

		String conditionD = toBeUsedCondition_D.toString();
		String numOfe = String.valueOf(numberOfElements);
		
		conf.set("numD", conditionD);
		
		conf.set("NumofE", numOfe);
		
		FileSystem fileSystem = FileSystem.get(conf);
		
		
		ToolRunner.run(conf, new Condition_D(), addr);
	
		relyDegreeOfDToC = new Value2sum(fileSystem);
		
		relyDegreeOfDToC.read(fileSystem, addr[1]);
		
		float Sig_D_redu = relyDegreeOfDToC.calculate();
		
		return Sig_D_redu;
	}
	
	

}