package org.apache.hadoop.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.GenericOptionsParser;
import com.sun.org.apache.xerces.internal.util.URI;

public class Main {
	// 初始化信息素矩阵，为0.5
	private static void init_phe(int AttributeNum, String file_path_phe) {

		Path path = new Path(file_path_phe);
		Configuration conf = new Configuration();// 取得系统参数
		org.apache.hadoop.fs.FileSystem fs = null;
		FSDataOutputStream output = null;
		try {
			fs = path.getFileSystem(conf);
			output = fs.create(path);// 创件文件
			for (int i = 1; i <= AttributeNum; i++) {
				for (int j = 1; j <= AttributeNum; j++) {
					float x = (float) 0.5;
					output.write("0.5".getBytes("UTF-8"));
					if (j != AttributeNum)
						output.write(",".getBytes("UTF-8"));
				}
				output.write("\n".getBytes("UTF-8"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 初始化重要度矩阵
	public static void init_ifo(int AttributeNum, String[] args ,float numberOfElements)  throws Exception {
		
		Vector<Integer> allowedAttributes = new Vector<Integer>();//未访问节点，属性
		for (int i = 1; i <= AttributeNum; i++) {
    		Integer integer = new Integer(i);
    		allowedAttributes.add(integer);//初始化所有属性编号都未访问
    	}
		
		float[] importance_i = new float[AttributeNum+1];
		float[][] importance_i_j = new float[AttributeNum+1][AttributeNum+1] ;
		float[][] importance_i_j_rec = new float[AttributeNum+1][AttributeNum+1] ;
		float[][] importance = new float[AttributeNum+1][AttributeNum+1] ;//保存重要度矩阵
		
		for(int i = 1 ; i <= AttributeNum; i++){

			new Condition1();
			importance_i[i] =  Condition1.start(args,i, AttributeNum,numberOfElements);
			importance_i_j[i][i]  =  0.0f ;
			for (Integer k:allowedAttributes) {   		
	    		 if (k.intValue() == i ) { 			 
	    			 allowedAttributes.remove(k);
	    			 break;
	    		 }
	    	 }
			if(i != AttributeNum){
				importance_i_j_rec = Condition3.start(args,i, AttributeNum,numberOfElements, allowedAttributes);
			
			}
			for (Integer k:allowedAttributes) {   		
				importance_i_j[i][k] = importance_i_j[k][i] = importance_i_j_rec[i][k];
	    	}
		
			
//			System.out.println(importance_i_j);
		}
		for(int i =1 ; i <= AttributeNum ; i++){
			for(int j = 1 ; j <= AttributeNum ; j++){
				System.out.print(importance_i_j[i][j]+"("+i+","+j+")"+"    ");
			}
			System.out.println();
		}
		for(int i = 1 ; i <= AttributeNum ; i++){
			importance[i][i]  =  0.0f ;
			for(int j = 1 ; j <= AttributeNum ; j++){
				if(i != j ){
					float Imp_c = (importance_i[i]  - importance_i_j[i][j])/importance_i[j];
					if(Imp_c < 0.001f) 
						Imp_c = 0.001f;
	 			    importance[i][j] = Imp_c;
				}
			}
		}	
		

		
		// 输出保存
		FileSystem fileSystem = getFileSystem();
		FSDataOutputStream dos = fileSystem.create(new Path(args[2]));

		for (int i = 1; i <= AttributeNum; i++) {
			for (int j = 1; j <= AttributeNum; j++) {
				if (j <= AttributeNum - 1) {
					dos.writeBytes(String.valueOf(importance[i][j])+",");				
				} else {
					dos.writeBytes(String.valueOf(importance[i][j]));
				}
			}

			dos.writeBytes("\n");
		}
		fileSystem.close();
	}

	// transfer list
	public static String[][] transfer(ArrayList<Float> list1,ArrayList<Float> list2,ArrayList<Float> list3, int AttributeNum) {

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(AttributeNum);

		String[][] array = new String[AttributeNum][AttributeNum];
		for (int i = 0; i < AttributeNum; i++) {
			array[i][i] =nf.format(0f);
			for (int j = 0; j < AttributeNum; j++) {
				if(i!=j){
					float Imp_c = (list1.get(AttributeNum * i + j)- list2.get(AttributeNum * i + j))
							/ list3.get(AttributeNum * i + j) ;
					if(Imp_c < 0.001f ) Imp_c = 0.001f;
					array[i][j] = nf.format( Imp_c  );
				}
			}
		}
		return array;
	}

	// 获取hdfs文件系统
	public static FileSystem getFileSystem() throws IOException {
		Configuration conf = new Configuration();
		FileSystem fileSystem = FileSystem.get(conf);
		return fileSystem;
	}

	public static float function_relyDtoAllcon(Path path_All, Path path_D,Configuration conf) throws IOException {

		FileSystem fs = FileSystem.get(conf);
		String lineTXT_all = null;
		String lineTXT_D = null;
		BufferedReader br_ALL = null;
		BufferedReader br_D = null;
		ArrayList<MySet> equclass_D = new ArrayList<MySet>(0);
		ArrayList<MySet> equclass_All = new ArrayList<MySet>(0);
		// 读取决策属性和所有条件属性的等价类
		try {
			br_D = new BufferedReader(new InputStreamReader(fs.open(path_D)));
			while ((lineTXT_D = br_D.readLine()) != null) {
				MySet temp_d = new MySet();
				String[] ss = lineTXT_D.split(",");
				for (int i = 0; i < ss.length; i++) {
					temp_d.add(Integer.parseInt(ss[i]));
				}
				equclass_D.add(temp_d);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br_D != null) {
				try {
					br_D.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			br_ALL = new BufferedReader(
					new InputStreamReader(fs.open(path_All)));
			while ((lineTXT_all = br_ALL.readLine()) != null) {
				MySet temp_all = new MySet();
				String[] ss = lineTXT_all.split(",");
				for (int j = 0; j < ss.length; j++) {
					temp_all.add(Integer.parseInt(ss[j]));
					// System.out.print(temp_all.toIntArrayList()+" ");
				}
				equclass_All.add(temp_all);
				// System.out.println();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br_ALL != null) {
				try {
					br_ALL.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// 计算决策属性D对条件属性的下近似
		Iterator<MySet> it_setD = equclass_D.iterator();
		MySet lowAppSet = new MySet();
		while (it_setD.hasNext()) {
			Iterator<MySet> it_setAll = equclass_All.iterator();
			MySet temp_All = new MySet();
			MySet temp_D = new MySet();
			temp_D = it_setD.next();
			while (it_setAll.hasNext()) {
				temp_All = it_setAll.next();
				if (temp_All.belongTo(temp_D)) {
					lowAppSet.union(temp_All);
				}
			}
		}
		lowAppSet.sort();
		return lowAppSet.card();

	}
	public static float SigofC(String[] arg_D,int AttributeNum,float numberOfElements) throws Exception {
		
		new Condition_D();
		//计算Ｈ(D)
		float H_D = Condition_D.start(arg_D, AttributeNum,numberOfElements);
		return H_D;
		
	}
	// 对所有属性的依赖度
	public static float SigofC_D(String[] arg_all,int AttributeNum,float numberOfElements) throws Exception {

		
		new Conditionall();
		//计算Ｈ(D|C)，Ｃ为所有条件属性
		float SigofC_D = Conditionall.start(arg_all, AttributeNum,numberOfElements);//属性个数包括决策属性
		return SigofC_D;
	}

	// 对某条件属性的依赖度
	public static float rely_condition(ArrayList<Integer> conditions,float numberOfElements,int AttributeNum)	throws Exception {

		String[] arg_reduction = new String[] { "/2k_20/2k_20.txt","/2k_20/redu_condition_temp.txt","/2k_20/H_D_redu.txt" }; // 保存当前属性集合的等价类
		new Reduction_con();
		conditions.add(AttributeNum+1);
		float SigofC_D = Reduction_con.start(arg_reduction, conditions,numberOfElements);//属性个数包括决策属性
		conditions.remove(conditions.size()-1);
		return SigofC_D;
	}

	private static void solve(int AttributeNum, float alpha, float beta,float relyDtoC, int ant_Num,float numberOfElements,int MAX_GEN) throws Exception {
		new solve_job1();
		solve_job1.start(AttributeNum, alpha, beta, relyDtoC, ant_Num,numberOfElements,MAX_GEN);

	}

	public static void main(String[] args) throws Exception {

		long startTime = System.currentTimeMillis(); // 获取开始时间

		int AttributeNum  = 20 ;// 条件属性个数
		float numberOfElements = 20000000f;// 样本总数
		int ant_Num = 1 ;// 蚂蚁数量
		int MAX_GEN = 1 ;//迭代次数
		// 初始化信息素矩阵，参数为条件属性个数
		String file_path_phe = "/2k_20/2k_20_phe.txt";// 生产信息度矩阵
		init_phe(AttributeNum,file_path_phe);

		// 初始化重要度矩阵，参数为条件属性个数+ 决策属性
		String[] file_path_ifo = new String[] { "/2k_20/2k_20.txt","/2k_20/temp_ifo.txt", "/2k_20/ifo.txt" ,"/2k_20/temp_ifo_D","/2k_20/out"};
		init_ifo(AttributeNum, file_path_ifo,numberOfElements);

		//计算依赖度，即决策属性对所有条件属性的依赖度，参数分别表示属性个数和样本数
		//所有决策属性对所有条件属性的依赖度I(C;D)=H(D)-H(D|C)
	    //这里以互信息作为依赖度
//		String[] arg_D = new String[] { "/2k_20/2k_20.txt","/2k_20/condition_D" };//决策属性的等价类	
//		float H_D = SigofC(arg_D,AttributeNum + 1 ,  numberOfElements);
//
		String[] arg_all = new String[] { "/2k_20/2k_20.txt","/2k_20/conditionAll_temp", "/2k_20/H_D_C" };// 分别表示文件路径，所有条件属性的等价类
		float H_D_C = SigofC_D(arg_all,AttributeNum + 1, numberOfElements);// 参数表示所有属性个数和样本总个数
//		
		
//		float H_D = 5.12928f; 
//		System.out.println("Ｈ（D）:"+ H_D);
//		float H_D_C = -0.0f;
		float relyDtoC = H_D_C;
//		float relyDtoC = H_D - 9.491579E-4f;
	
//		
//		System.out.println("Ｈ（D_C）:"+ H_D_C);
//
		solve(AttributeNum, 1f, 0.1f, relyDtoC, ant_Num,numberOfElements,MAX_GEN);// 条件属性个数，阿尔法，贝塔，依赖度，蚂蚁数,决策熵，样本数
		long endTime=System.currentTimeMillis(); //获取结束时间
		
		System.out.println("4-25  2k_20  1*job  node * 2  antnum:1  gen= 1   程序运行时间： "+  (endTime-startTime)+"ms");
	}

}
