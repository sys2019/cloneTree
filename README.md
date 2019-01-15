A Java command line tool for clustering samples into clones and build the tree of these clones.

CloneTree is implemented using the Weka Java library to cluster samples, using three methods as HierarchicalClustering, DBSCAN and KMeans to get the clones.

As of version 0.0.1 (Jan. 2019) CloneTree requires Java 1.8 (jdk8u66).

Using CloneTree

Downloading this tool by using command 
	
	git clone https://github.com/sys2019/cloneTree.git
	cd cloneTree/
	
then you can use this tool as a normal runnable jar file.
	
For example, download the example folder, then you can run
	
	java -jar CloneTree.jar -i example/Merge.ccf.mat -o example/ -s Example
	
which make a simple clone tree file using our example data.

Then you can use the Rscript provided by us to draw the clone tree in a visual way.
this Rscript requires R version 3.4+, and a R library called clonevol.
The command of this in our example is

	cd example/Example_DB/
	Rscript Draw_clonal_tree.R Merge.txt T.txt M.txt L.txt ./
	
A visualization created.