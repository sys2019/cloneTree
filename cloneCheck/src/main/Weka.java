package main;

import java.util.ArrayList;
import java.util.Map.Entry;

import weka.clusterers.DBSCAN;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class Weka {
	
	public static int[] numC = new int[] {1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233,377,610,987,1597,2584,4181,6765,10946,17711,28657};
	public static Instances iss = null;
	
	public static void run(CliParam args) {
		ArrayList<Data> data = Method.newmatRead(args.getIn_File());
		Weka.iss = Weka.creatInstances(data);
		Weka.runHierarchicalClusterer(data, args);
		Data.writeCell(data, args.getOut_label() + "_Hi.tsv");
		Data.writeCluster(data, args.getOut_label() + "_Hiclu.tsv");
		Weka.runDBScan(data, args);
		Data.writeCell(data, args.getOut_label() + "_DB.tsv");
		Data.writeCluster(data, args.getOut_label() + "_DBclu.tsv");
		Weka.runKMeans(data, args);
		Data.writeCell(data, args.getOut_label() + "_KM.tsv");
		Data.writeCluster(data, args.getOut_label() + "_KMclu.tsv");
	}
	
	public static void runHierarchicalClusterer(ArrayList<Data> data, CliParam args) {
		HierarchicalClusterer hc = new HierarchicalClusterer();
		
		try {
			String[] options = new String[2];
		    options[0] = "-L";                 
		    options[1] = "COMPLETE";
			hc.setOptions(options);
			if (args.getNum_cluster() == 0) {
				hc.setNumClusters(numC[data.get(0).ccfs.size()]);
			}
			else {
				hc.setNumClusters(args.getNum_cluster());
			}	
			hc.buildClusterer(iss);
			for (int i = 0; i < data.size(); i++) {
				data.get(i).label = hc.clusterInstance(iss.instance(i));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void runDBScan(ArrayList<Data> data, CliParam args) {
		DBSCAN ds = new DBSCAN();
		ds.setEpsilon(args.getDB_r());
		ds.setMinPoints(args.getDB_min());
		try {
			ds.buildClusterer(iss);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < data.size(); i++) {
			try {
				data.get(i).label = ds.clusterInstance(iss.instance(i));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				data.get(i).label = -1;
			}
		}
	}
	
	public static void runKMeans(ArrayList<Data> data, CliParam args) {
		SimpleKMeans km = new SimpleKMeans();
		try {
			if (args.getNum_cluster() == 0) {
				km.setNumClusters(numC[data.get(0).ccfs.size()]);
			}
			else {
				km.setNumClusters(args.getNum_cluster());
			}
			km.buildClusterer(iss);
			for (int i = 0; i < data.size(); i++) {
				data.get(i).label = km.clusterInstance(iss.instance(i));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Instance dataToInstance(Data data, Instances iss) {
		Instance out = new Instance(data.ccfs.size());
		out.setDataset(iss);
		for (Entry<String, double[]> entry : data.ccfs.entrySet()) {
			out.setValue(iss.attribute(entry.getKey()), entry.getValue()[0]);
		}
		return out;
	}
	
	public static Instances creatInstances(ArrayList<Data> data) {
		FastVector attrs = new FastVector();
		for (Entry<String, double[]> entry : data.get(0).ccfs.entrySet()) {
			attrs.addElement(new Attribute(entry.getKey()));
		}
		Instances out = new Instances("Data", attrs, 0);
		for (int i = 0; i < data.size(); i++) {
			out.add(dataToInstance(data.get(i), out));
		}
		return out;
	}
}
