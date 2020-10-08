package org.apache.hadoop.examples;

import java.util.Random;
import java.util.Vector;
public class Ant implements Cloneable{
	private Vector<Integer> tabu; //禁忌表
	private Vector<Integer> allowedAttributes; //允许搜索的属性
	private float[][] delta; //信息数变化矩阵
	private float[][] importance_C; //属性重要度矩阵
	  
	private float alpha; 
	private float beta;
	 
	private int AttributeLength; //路径长度
	private int AttributeNum; //属性数量
	
	private int firstAttribute; //起始属性
    private int currentAttribute; //当前属性
	  
    public Ant(){
	   AttributeNum = 30;
	   AttributeLength = 0;
	 }
    
    /**
     * Constructor of Ant
     *  @param num 蚂蚁数量
     */
    public Ant(int num){
    	AttributeNum = num;
    	AttributeLength = 0;
    	 
    }
    
    /**
    * 初始化蚂蚁，随机选择起始位置
    * @param importance_C 属性重要度矩阵
    * 随机分配蚂蚁到某个属性中
    * 同时完成蚂蚁包含字段的初始化工作
    * @param a alpha
    * @param b beta
    */
    public void init( float a, float b,Random random){
    	 alpha = a;
    	 beta = b;
    	 allowedAttributes = new Vector<Integer>();//未访问节点，属性
    	 tabu = new Vector<Integer>();
   
    	 delta = new float[AttributeNum+1][AttributeNum+1];
    
    	 for (int i = 1; i <= AttributeNum; i++) {
    		 Integer integer = new Integer(i);
    		 allowedAttributes.add(integer);//初始化所有属性编号都未访问
    		 for (int j = 1; j <= AttributeNum; j++) {
    			 delta[i][j] = 0.f;
    		 }
    	 }

    	 //随机初始一个
    	 firstAttribute = random.nextInt(AttributeNum)+1;
    	 //遍历城市群编号，将第一个城市从未访问数组里删除
    	 for (Integer i:allowedAttributes) {   		
    		 if (i.intValue() ==firstAttribute) { 			 
    			 allowedAttributes.remove(i);
    			 break;
    		 }
    	 }
    	 tabu.add(Integer.valueOf(firstAttribute));
    	 currentAttribute = firstAttribute;
    }
    /**
     * 选择下一个城市
     *  @param pheromone 信息素矩阵
     */
    public int  selectNextCity(float[][] pheromone,float[][] importance_C){
    	float[] p = new float[AttributeNum+1];
        float sum = 0.0f;
        for(int i = 1 ; i <= AttributeNum ;i++){//初始化矩阵
        	p[i]=0f;
        }
        //计算分母部分
//        System.out.println("当前属性    "+currentAttribute);
        for (Integer i:allowedAttributes) {
        	sum += Math.pow(pheromone[currentAttribute][i.intValue()], alpha)*
        			Math.pow(importance_C[currentAttribute][i.intValue()],beta);
        }
        //计算概率矩阵
        //到城市j的概率
        float Max_p = -10000;
        int selectAttribute = -1;
    	for (Integer j:allowedAttributes) {
    		p[j] = (float)((Math.pow(pheromone[currentAttribute][j], alpha)*Math.pow(importance_C[currentAttribute][j], beta))/sum);
    		if(Max_p < p[j]){
    	    	Max_p = p[j];
    	        selectAttribute = j;
    	    }
    	}

        //从允许选择的属性中去除selectAttribute
        for(Integer i:allowedAttributes) {
        	if (i.intValue() == selectAttribute) {
        		allowedAttributes.remove(i);
        		break;
        	}
        }
        //在禁忌表中添加selectAttribute
        tabu.add(Integer.valueOf(selectAttribute));
        //将当前属性改为选择的属性
        currentAttribute = selectAttribute;
        return currentAttribute;

    }

    public Vector<Integer> getallowedAttributes() {
    	return allowedAttributes;
    }
   
    public void setallowedAttributes(Vector<Integer> allowedAttributes) {
       this.allowedAttributes = allowedAttributes;
    }
    
    public int getAttributeNum() {
    	return AttributeNum;
    }
    
    public void setAttributeNum(int AttributeNum) {
       this.AttributeNum = AttributeNum;
    }
    
    public Vector<Integer> getTabu() {
       return tabu;
    }
    
    public void setTabu(Vector<Integer> tabu) {
       this.tabu = tabu;
    }
    
    public float[][] getDelta() {
    	return delta;
    }
    
    public void setDelta(float[][] delta) {
    	this.delta = delta;
    }
    
    public int getfirstAttribute() {
    	return firstAttribute;
    }
    
    public void setfirstAttribute(int firstAttribute) {
    	this.firstAttribute = firstAttribute;
    } 
    
}