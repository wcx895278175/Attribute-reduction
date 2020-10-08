package org.apache.hadoop.examples;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class solve_job1 {
	
//	private  Ant[] ants; //蚂蚁
	// run program
	public static void start(int AttributeNum,float alpha, float beta,float relyDtoC,int ant_num,float numberOfElements,float MAX_GEN) throws Exception {
		
		//路径
		String[] job1_path = new String[]  {"/2k_10/antstart_2k_10.txt", "/2k_10/reduction1" };
		String[] job2_path = new String[]  {"/2k_10/reduction2/part-r-00000","/2k_10/reduction3"} ;
		String[] job4_path = new String[]  {"/2k_10/reduction3/part-r-00000","/2k_10/reduction4"} ;
		String[] job5_path = new String[]  {"/2k_10/reduction4/part-r-00000","/2k_10/reduction5"} ;
		String[] job6_path = new String[]  {"/2k_10/reduction5/part-r-00000","/2k_10/reduction6"} ;
		String[] job7_path = new String[]  {"/2k_10/reduction6/part-r-00000","/2k_10/reduction7"} ;
		String[] job8_path = new String[]  {"/2k_10/reduction7/part-r-00000","/2k_10/reduction8"} ;
		String[] job9_path = new String[]  {"/2k_10/reduction8/part-r-00000","/2k_10/reduction9"} ;
		String[] job10_path = new String[] {"/2k_10/reduction9/part-r-00000","/2k_10/reduction10"} ;
		String file_phe = "/2k_10/2k_10_phe.txt";	
		Path path_phe = new Path(file_phe);	
		BufferedReader  br_phe = null; 	
		Configuration conf = new Configuration();
		//将信息素读取pheromone
		float[][] pheromone = new float[AttributeNum+1][AttributeNum+1];
		FileSystem fs = FileSystem.get(conf);
		
//		System.out.println("&&&&&&&&&&&&&&&&&&&");
		String line_phe = null ;			
		try{	
			 br_phe   = new BufferedReader(new InputStreamReader(fs.open(path_phe)));	
			 int i = 1 ;
		     while((line_phe = br_phe.readLine())!= null ){
		    	 String[] ss = line_phe.split(",");
		    	 for(int j = 0 ; j < ss.length ; j++){
		    		 pheromone[i][j+1]= Float.parseFloat(ss[j]);
//		    		 System.out.print( pheromone[i][j+1]+" ");
		    	 }
//		    	 System.out.println();
		    	 i++;
	    	 }
		     
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(br_phe != null  ){
				try{
					br_phe.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
		for(int k = 1 ; k <= AttributeNum ; k++){
			for (int j = 1 ; j <= AttributeNum;  j++){
				System.out.print(pheromone[k][j]+" ");
			}
			System.out.println();
		}
		//读取重要度矩阵
		String file_ifo = "/2k_10/ifo.txt";
		Path path_ifo = new Path(file_ifo);
		BufferedReader  br_ifo = null; 
		String line_ifo = null ; 
		float[][] importance_C = new float[AttributeNum+1][AttributeNum+1];
		
		try{	
			 br_ifo   = new BufferedReader(new InputStreamReader(fs.open(path_ifo)));	
			 int k = 1 ;
		     while((line_ifo = br_ifo.readLine())!= null ){
		    	 String[] ss = line_ifo.split(",");
		    	 for(int j = 0 ; j < ss.length ; j++){
		    		 importance_C[k][j+1]= Float.parseFloat(ss[j]);
//		    		 System.out.print( importance_C[k][j+1]+" ");
		    	 }
		    	 k++;
		     }
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(br_ifo != null  ){
				try{
					br_ifo.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
//		System.out.println("*****************************");
	
		for(int k = 1 ; k <= AttributeNum ; k++){
			for (int j = 1 ; j <= AttributeNum;  j++){
				System.out.print(importance_C[k][j]+" ");
			}
			System.out.println();
		}
		//初始化蚂蚁家族中蚂蚁数量
		Ant[] ants = new Ant[ant_num];
		Random random1 = new Random();
//		System.out.println("AttributeNum: "+AttributeNum);
		
		//初始化每只蚂蚁信息，起点
		for(int i = 0 ; i < ant_num ; i++){
			ants[i] = new Ant(AttributeNum);
			ants[i].init(alpha, beta, random1);
		}
		ArrayList<Integer> best_reduction = new ArrayList<Integer>(0);//最优约简结果
		for(int i = 1 ; i <=  AttributeNum ; i++){
			best_reduction.add(i);
		}
		//每只蚂蚁进行求解
		float SigRec2 = 0.0f,SigRec = 0.0f;//约简重要度
		int bestlength = AttributeNum;
		for (int runtimes = 1; runtimes <= MAX_GEN; runtimes++) {
			
			for(int i = 0 ; i < ant_num ; i++){//蚂蚁个数
				ArrayList<Integer> cur_reduction = new ArrayList<Integer>(0);; //当前约简结果
				cur_reduction.add(ants[i].getTabu().get(0)); //获取第一个起始属性
				SigRec2 = 0.0f;
				SigRec = 0.0f;
				try {
					SigRec = Main.rely_condition(cur_reduction,numberOfElements,AttributeNum);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				for(int j  = 2 ;  j <= AttributeNum ; j++){//剩余属性数量
					// 选择下一个最大概率属性
					int Re_C = ants[i].selectNextCity(pheromone, importance_C);//每只蚂蚁，根据信息素矩阵，完成对所属性的遍历			
					cur_reduction.add(Re_C); //将被选择属性添加到当前约简结果中		

					try {//计算当前约简结果重要度（依赖度）
						 SigRec2  =  Main.rely_condition(cur_reduction,numberOfElements,AttributeNum);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if(SigRec == SigRec2){	//如果重要度未发生改变，删除冗余属性
						ants[i].getTabu().add(Re_C);
						cur_reduction.remove(cur_reduction.size()-1);
					}
					SigRec = SigRec2;
					//当满足重要度或长度大于当前最优约简，结束此次搜索
					System.out.println("当前约简长度＋重要度："+ cur_reduction.size()+" "+SigRec2+" "+cur_reduction.toString());
					System.out.println("重要度："+ relyDtoC);
				
					if(SigRec2 == relyDtoC || cur_reduction.size() >= bestlength){
						break;
					}
//					if(cur_reduction.size() == 10 ){
//						break;
//					}
				}
				//判断是否优于当前最优解
//				if( SigRec2 == relyDtoC && cur_reduction.size() < bestlength ){
//					best_reduction = cur_reduction;					
//					bestlength = cur_reduction.size();
//					System.out.println("####################################   "+bestlength);
//				}
				if(SigRec2 == relyDtoC ){
    				int len =  cur_reduction.size();
    				for(int k = 0 ; k < len ; k++){
    					//依次删除每个非核属性判断重要度结果是否改变
    					int temp_re = cur_reduction.remove(0);
    					float sig = 0.0f;
    					try {//计算当前约简结果重要度（依赖度）
							sig  =  Main.rely_condition(cur_reduction,numberOfElements,AttributeNum);
						} catch (Exception e) {
							e.printStackTrace();
						}
    					if(sig  !=  relyDtoC  ){
    						cur_reduction.add(temp_re);
   				  		}
    				}    		
    				if(SigRec2 == relyDtoC  && cur_reduction.size()< bestlength  ){
    					best_reduction = cur_reduction;
    					bestlength = cur_reduction.size();
    				}
    			}
				for (int j = 0; j < ants[i].getTabu().size()-1; j++) {
    				 //一只蚂蚁。为它经过的每段路程增添信息素
    				 ants[i].getDelta()[ants[i].getTabu().get(j).intValue()][ants[i].getTabu().get(j+1).intValue()] 
    						 = (float) (0.1/bestlength);
    			 }
			}
			
			//更新信息素,信息素挥发  
			FSDataOutputStream dos = fs.create(new Path("/2k_10/2k_10_phe.txt"));
			for(int i = 1; i <= AttributeNum ; i++) {
	    		 for(int j = 1 ; j <= AttributeNum ; j++)  {
	    			 pheromone[i][j]=(float) (pheromone[i][j]*(0.5)); 
	    		    //信息素更新   
		             for (int k = 0; k < ant_num; k++) 
		            	 pheromone[i][j] += ants[k].getDelta()[i][j];	     
		             if(j < AttributeNum){
							dos.writeBytes(String.valueOf(pheromone[i][j])+",");	
					 }else{
							dos.writeBytes(String.valueOf(pheromone[i][j]));
					}
	    		 }
	    		 dos.writeBytes("\n");
	    	}
			dos.close();
			//重新初始化蚂蚁,随机设置蚂蚁
    		Random random2 = new Random();
    		for(int i =  0;i < ant_num;i++){  
    			ants[i].init(alpha, beta,random2);  
    		}  		
    		System.out.println("第"+runtimes+"代，发现新的解"+best_reduction+" "+bestlength);
    		
		}
	}    
}