package org.apache.hadoop.examples;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class Value2sum {

	private ArrayList<Float> list = new ArrayList();
	private float sum = 0.0f;
	private FileSystem fileSystem;
	//private ArrayList<Double> resultList = new ArrayList();

	// 构造方法，传入一个fileSystem
	public Value2sum(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	// 扫描path下所有的文件
	public void read(FileSystem fileSystem, String path)
			throws FileNotFoundException, IllegalArgumentException, IOException {

		FileStatus array[] = fileSystem.listStatus(new Path(path));

		for (int i = 0; i < array.length; i++) {

			readFile(fileSystem, array[i].getPath());
		}

	}

	// 将一个具体文件进行浏览次数的解析
	public void readFile(FileSystem fileSystem, Path path) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(fileSystem.open(path)));
			String temp = "";

			while ((temp = br.readLine()) != null) {
				Float d = Float.parseFloat(temp);
				sum += d;
				list.add(d);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//最终计算
	public float calculate() {
		float result = 0.0f;
		for (Float d : list) {
//			System.out.println("sig_t:"+ d);
			result += d;
		}
		//resultList.add(result*(-1));
		return result*(-1);
	}

	

}
