A Java command line tool for clustering samples into clones and build the tree of these clones.

CloneTree is implemented using the Weka Java library to cluster samples, using three methods as HierarchicalClustering, DBSCAN and KMeans to get the clones.

As of version 0.0.1 (Jan. 2019) CloneTree requires Java 1.8 (jdk8u66).

Using CloneTree

Downloading a released version of this tool in release page, then you can use this tool as a normal runnable jar file.
	
For example, download the example folder, then you can run
	
	java -jar CloneTree.jar -i example/Merge.ccf.mat -o example/ -s Example
	
which make a simple clone tree file using our example data.

	then you can use 