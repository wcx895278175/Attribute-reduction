package org.apache.hadoop.examples;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class Value2sum2 {

	private FileSystem fileSystem;
	// 构造方法，传入一个fileSystem
	public Value2sum2(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	// 将一个具体文件进行浏览次数的解析
	public float[][] readFile(FileSystem fileSystem, String path,int AttributeNum,int currentAttribute ) throws NumberFormatException, IOException {
		Path outpath = new Path(path+"/part-r-00000");

		BufferedReader br = new BufferedReader(new InputStreamReader(fileSystem.open(outpath)));
		String temp = "";
//		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&7"+path);
		float[][] importance = new float[AttributeNum+1][AttributeNum+1] ;//保存重要度矩阵
		while ((temp = br.readLine()) != null) {

			String[] ss = temp.split("\t");
			Float d = Float.parseFloat(ss[1]);
			int j = Integer.valueOf(ss[0].replace("C", ""));
//			System.out.println(j+" "+d);
			importance[currentAttribute][j]= d;
		}

		return importance;
		
	}

}
