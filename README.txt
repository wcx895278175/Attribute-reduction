
@中文：
	
	我们公开的2个程序如下：
		
	1.程序名称
		1.基于MapReduce与ACO的属性约简是算法ARMA
		2.基于云计算和蚁群优化的多目标并行属性约简算法M-ARMA
	
	2.解决问题
		针对传统的粗糙集属性约简算法求得最小属性约简集合中通常存在冗余属性的不足,以及无法有效处理大数据,利用蚁群优化算法和MapReduce相结合的方式用于解决最小属性约简问题.
	
	3.实验配置
		实验环境由4个节点组成的计算机集群构成,其中一个设置为master主节点,其余配置为slaves从节点.每个节点具有4GB主内存,使用Inter Core i7 CPU和Ubantu16.04,Hadoop版本为2.7.1,Java 1.8.0_121,节点之间通信速率为100Mbps.在每个节点中设置4个map和2个reduce任务,并将备份因子设置为1.
	
	4.数据集描述
		采用Mushroom数据集,该数据集包含近8124条记录,22个条件属性和1个决策属性.
		
	5.如何运行
		M-ARMA\src\org\apache\hadoop\examples\Main.java
		ARMA\src\org\apache\hadoop\examples\Main.java
		
如对代码有任何疑问，请给我们留言，我们看到会及时进行回复.


@English：
	
	The two procedures we have made public are as follows:
	
	1.Program name
		ARMA: Attribute reduction based on ACO and MapReduce
		M-ARMA: Multi-objective parallel attribute reduction algorithm based on ACO and MapReduce
	
	2.File description
		Aiming at the deficiency of redundant attributes in the minimum attribute reduction set obtained by traditional rough set attribute reduction algorithm and the inability to process big data effectively, the combination of ant colony optimization algorithm and MapReduce was used to solve the problem of minimum attribute reduction.
	
	3.Experimental configuration
		The experimental environment is composed of four nodes. One of them is set as the master node, and the rest is configured as Slaves slave node. Each node has 4GB of main memory, USES Intel Core I7 CPU and Ubantu16.04, Hadoop version 2.7.1, Java 1.8.0_121, and the communication rate between nodes is 100Mbps. Set 4 Map and 2 Reduce tasks in each node and set the backup factor to 1.
	
	4.Datasets description
		The Mushroom dataset contains nearly 8124 records, 22 condition attributes and 1 decision attribute.
	
	5.How to run it?
		M-ARMA\src\org\apache\hadoop\examples\Main.java
		ARMA\src\org\apache\hadoop\examples\Main.java
		
If you have any questions about the code, please leave us a message and we will reply in time.