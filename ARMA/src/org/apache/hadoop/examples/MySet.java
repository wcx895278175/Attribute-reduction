package org.apache.hadoop.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
public class MySet implements Cloneable
{
	public MySet(){}
	//用数组初始化集合
	public MySet(float A[])
	{
		for(float e : A)
		{
			arrayList.add(e);
		}
	}
	
	//用集合初始化集合
	public MySet(MySet s)
	{
		arrayList = s.arrayList;
	}
	
	//用ArrayList<Integer>初始化集合
	public MySet(ArrayList<Float> al)
	{
		arrayList = al;
	}
	
	//实现克隆功能
	public MySet clone() throws CloneNotSupportedException
	{
		MySet cloned = new MySet();
				
		Iterator<Float> itArrayList = this.arrayList.iterator();
		while(itArrayList.hasNext())
		{
			cloned.arrayList.add(itArrayList.next());
		}
		
		return cloned;
	}
	
	//判断集合是否为空集
	boolean isEmpty()
	{
		return arrayList.isEmpty();
	}
	
	//计算集合A的模
	int card()
	{
		return arrayList.size();
	}
	
	//集合清空
	void clear()
	{
		arrayList.clear();
	}
	
	//在集合中查找元素,存在则返回位置，不存在返回-1
	int find(float a)
	{
		for(int i=0; i<this.card();i++)
		{
			if(a == this.keyAt(i))
			{
				return i;
			}
		}
		return -1;
	}
	
	//返回集合中loc位置的值
	float keyAt(int loc)
	{
		return arrayList.get(loc);
	}
	
	//设置loc位置的值为val
	void setKey(int loc,float val)
	{
		arrayList.set(loc, val);
	}
	
	//对集合中的元素按从小到大排序
	void sort()
	{
		Collections.sort(arrayList);
	}
	
	//在集合中删除元素得到新的集合
	void remove(float a)
	{
		if(-1 != find(a))
		{
			ArrayList<Float> temp = new ArrayList<Float>(0);
			for(int i=0;i<arrayList.size();i++)
			{
				if(arrayList.get(i) != a)
				{
					temp.add(arrayList.get(i));
				}
			}
			arrayList = temp;
		}
	}
	
	//在集合中添加元素得到新的集合
	void add(float i)
	{
		arrayList.add(i);
	}
	
	//判断当前集合是不是s集合的子集
	boolean belongTo(MySet s)
	{
		for(float e : arrayList)
		{
			if(-1 == s.find(e))
				return false;
		}
		return true;
	}
	
	//判断两个集合是否相等
	boolean isEqual(MySet s)
	{
		if(this.belongTo(s) && s.belongTo(this))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	//当前集合中去掉子集s,s必须是当前集合的子集
	MySet sub(MySet s)
	{
		if(s.belongTo(this))
		{
			MySet temp = new MySet(arrayList);
			for(int i=0; i<s.card(); i++)
			{
				temp.remove(s.keyAt(i));
			}
			return temp;
		}
		else
		{
			System.out.println("参数中集合并非当前集的子集，不能进行该运算");
			return null;
		}
	}
	
	//当前集并上另一个集合
	MySet union(MySet s)
	{
		MySet temp = new MySet(arrayList);
		for(int i=0;i<s.card();i++)
			if(-1 == temp.find(s.keyAt(i)))
			{
				temp.add(s.keyAt(i));
			}
		return temp;
	}
	
	//当前集与另一个集合的交集
	MySet interSect(MySet s)
	{
		MySet temp = new MySet(arrayList);
		for(int i=0; i<s.card();i++)
			if(-1 == s.find(s.keyAt(i)))
			{
				temp.remove(s.keyAt(i));
			}
		MySet VOfA = temp.valueOfAttribute();
		return VOfA;
	}
	
	//求某属性值集的值域
	MySet valueOfAttribute()
	{
		MySet temp = new MySet();
		for(int i=0; i<this.card();i++)
		{
			if(this.keyAt(i) != Integer.MAX_VALUE)
			{
				temp.add(this.keyAt(i));
				for(int j=i+1; j<this.card();j++)
				{
					if(this.keyAt(i) == this.keyAt(j))
					{
						this.setKey(j, Integer.MAX_VALUE);
					}
				}
			}
			else
				continue;
		}
		return temp;
	}
	
	//把该集合转化成整型数组
	ArrayList<Float> toIntArrayList()
	{
		ArrayList<Float> temp = new ArrayList<Float>(0);
		for(int i=0; i<this.card(); i++)
		{
			temp.add(this.keyAt(i));
		}
		return temp;
	}
	
	//在屏幕打印集合A
	void print()
	{
		int i=0;
		System.out.print("{");
		if(!arrayList.isEmpty())
		{
			System.out.print(arrayList.get(i));
		}
		i++;
		for(; i<arrayList.size(); ++i)
		{
			System.out.print(","+arrayList.get(i));
		}
		System.out.print("}");
	}
	
	private ArrayList<Float> arrayList = new ArrayList<Float>(0);
}

