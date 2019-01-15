package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class OriginalCluster {
	
	private String id=null;
	private class OriginalSample{
		ArrayList<Double> values=null;
		ArrayList<Double> low_bounds=null;
		ArrayList<Double> up_bounds=null;
	}
	private HashMap<String, OriginalSample> samples=new HashMap<>();
	private HashSet<String> genes=null;
	private int size = 0;
	
	public double getAverage() {
		double out = 0.0;
		for (Entry<String, OriginalSample> entry : samples.entrySet()) {
			out += Method.median(entry.getValue().values);
		}
		out /= samples.size();
		return out;
	}
	
	public ArrayList<Cluster> toCluster(ArrayList<OriginalCluster> clusters){
		ArrayList<Cluster> out = new ArrayList<>();
		for (int i = 0; i < clusters.size(); ++i) {
			OriginalCluster cluster = clusters.get(i);
			HashMap<String, Sample> samples = new HashMap<>();
			for (Entry<String, OriginalSample> sample_entry : cluster.samples.entrySet()) {
				ArrayList<Double> values = sample_entry.getValue().values;
				ArrayList<Double> low_bounds = sample_entry.getValue().low_bounds;
				ArrayList<Double> up_bounds = sample_entry.getValue().up_bounds;
				double value = Method.median(values);
				if (value < FileRW.min_value) {
					value = 0.0;
				}
				double[] error = Method.getError(values, up_bounds, value);
				double up_cluster = error[0];
				error = Method.getError(values, low_bounds, value);
				Sample sample = new Sample(sample_entry.getKey(), value, up_cluster, error[1], 0, 1);
				samples.put(sample_entry.getKey(), sample);
			}
			Cluster c = new Cluster();
			c.setSize(cluster.size);
			c.setId(cluster.id);
			c.setSamples(samples);
			c.setGenes(cluster.genes);
			out.add(c);
		}
		return out;
	}
	
	public ArrayList<Cluster> bootStrapCluster(ArrayList<OriginalCluster> clusters, double up_clusters, double max_remove_samples) {
		ArrayList<Cluster> out = new ArrayList<>();
		HashSet<OriginalCluster> adjust_clus = new HashSet<>();
		Method.getNoneRepeat(clusters, Method.randIntReach(0, (int) (clusters.size() * up_clusters)), adjust_clus);
		for (int i = 0; i < clusters.size(); ++i) {
			OriginalCluster cluster = clusters.get(i);
			HashSet<Integer> adjust_ids = new HashSet<>();
			boolean remove_flag = true;
			if (adjust_clus.contains(cluster)) {
				int random_size = Method.randIntReach(0, (int) (cluster.size * max_remove_samples));
				if (random_size > cluster.size / 2) {
					random_size = cluster.size - random_size;
					remove_flag = false;
				}
				while (adjust_ids.size() < random_size) {
					adjust_ids.add(Method.randIntReach(0, cluster.size - 1));
				}
			}
			HashMap<String, Sample> samples = new HashMap<>();
			int size = 0;
			for (Entry<String, OriginalSample> sample_entry : cluster.samples.entrySet()) {
				ArrayList<Double> values = sample_entry.getValue().values;
				ArrayList<Double> low_bounds = sample_entry.getValue().low_bounds;
				ArrayList<Double> up_bounds = sample_entry.getValue().up_bounds;
				if (adjust_ids.size() > 0) {
					values = new ArrayList<>();
					low_bounds = new ArrayList<>();
					up_bounds = new ArrayList<>();
					for (int k = 0; k < cluster.size; ++k) {
						if (adjust_ids.contains(k) ^ remove_flag) {
							values.add(sample_entry.getValue().values.get(k));
							low_bounds.add(sample_entry.getValue().low_bounds.get(k));
							up_bounds.add(sample_entry.getValue().up_bounds.get(k));
						}
					}
				}
				size = values.size();
				double value = Method.median(values);
				if (value < FileRW.min_value) {
					value = 0.0;
				}
				double[] error = Method.getError(values, up_bounds, value);
				double up_cluster = error[0];
				error = Method.getError(values, low_bounds, value);
				Sample sample = new Sample(sample_entry.getKey(), value, up_cluster, error[1], 0, 1);
				samples.put(sample_entry.getKey(), sample);
			}
			Cluster c = new Cluster();
			c.setSize(size);
			c.setId(cluster.id);
			c.setSamples(samples);
			c.setGenes(cluster.genes);
			out.add(c);
		}
		return out;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public HashMap<String, OriginalSample> getSamples() {
		return samples;
	}

	public void setSamples(HashMap<String, OriginalSample> samples) {
		this.samples = samples;
	}
	
	public void addSample(String id, ArrayList<Double> values, ArrayList<Double> low_bounds, ArrayList<Double> up_bounds) {
		OriginalSample sample = new OriginalSample();
		sample.values = values;
		sample.low_bounds = low_bounds;
		sample.up_bounds = up_bounds;
		this.samples.put(id, sample);
	}

	public HashSet<String> getGenes() {
		return genes;
	}

	public void setGenes(HashSet<String> genes) {
		this.genes = genes;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
}
