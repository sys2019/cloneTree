package main;

import java.util.ArrayList;
import java.util.HashMap;

public class Sample {
	
	private String id=null;
	private double value=0.0;
	private double error_positive=0.0;
	private double error_negative=0.0;
	private double adjust_value=0.0;
	private double top_value=1.0;
	private double occupied=0.0;
	private double free=0.0;
	
	public Sample() {
	}
	
	public Sample(String id, double value, double error_positive, double error_negative, double adjust_value,
			double top_value) {
		super();
		this.id = id;
		this.value = value;
		this.error_positive = error_positive;
		this.error_negative = error_negative;
		this.adjust_value = adjust_value;
		this.top_value = top_value;
	}
	
	public Sample clone() {
		Sample out = new Sample();
		out.id = this.id;
		out.value = this.value;
		out.error_positive = this.error_positive;
		out.error_negative = this.error_negative;
		out.adjust_value = this.adjust_value;
		out.top_value = this.top_value;
		return out;
	}
	
	public boolean getRelation(ArrayList<Cluster> error, HashMap<Cluster, ArrayList<Cluster>> child, HashMap<Cluster, ArrayList<Cluster>> no_child, ArrayList<Cluster> all, String sample_id) {
		boolean out = true;
		for (int i = 0; i < all.size(); i++) {
			Cluster c = all.get(i);
			for (int j = i + 1; j < all.size(); j++) {
				Cluster c2 = all.get(j);
				Sample s1 = c.getSamples().get(sample_id);
				Sample s2 = c2.getSamples().get(sample_id);
				double top = s2.top_value;
				if (top < s1.top_value) {
					top = s1.top_value;
				}
				if (s1.value + s1.error_negative + s2.value + s2.error_negative > top) {
					if (no_child.containsKey(c2) && no_child.get(c2).contains(c)) {
						if (no_child.containsKey(c) && no_child.get(c).contains(c2)) {
							out = this.addError(error, c, c2, sample_id, s1.value + s1.error_negative + s2.value + s2.error_negative - top);
						}
						else {
							this.addInMap(child, c2, c);
						}
					}
					else if (no_child.containsKey(c) && no_child.get(c).contains(c2)) {
						this.addInMap(child, c, c2);
					}
					else {
						this.addInMap(child, c2, c);
						this.addInMap(child, c, c2);
					}
				}
				if (s1.value + s1.error_negative > s2.value + s2.error_positive) {
					this.addInMap(no_child, c, c2);
					if (child.containsKey(c2) && child.get(c2).contains(c)) {
						if (child.containsKey(c) && child.get(c2).contains(c)) {
							child.get(c2).remove(c);
							resetTop_value(c, c2, s1, s2);
						}
						else {
							out = this.addError(error, c, c2, sample_id, s1.value + s1.error_negative - s2.value - s2.error_positive);
						}
					}
				}
				else if (s1.value + s1.error_positive < s2.value + s2.error_negative) {
					this.addInMap(no_child, c2, c);
					if (child.containsKey(c) && child.get(c).contains(c2)) {
						if (child.containsKey(c2) && child.get(c2).contains(c)) {
							child.get(c).remove(c2);
							resetTop_value(c2, c, s2, s1);
						}
						else {
							out = this.addError(error, c, c2, sample_id, s1.value + s1.error_positive - s2.value - s2.error_negative);
						}
					}
				}

			}
		}
		return out;
	}
	
	public boolean addError(ArrayList<Cluster> error, Cluster c1, Cluster c2, String s, double d) {
//		error.add(c1.getId() + "\t" + c2.getId() + "\t" + s + "\t" + d);
		if (c1.hashCode() > c2.hashCode()) {
			if (!error.contains(c2)) {
				error.add(c2);
			}
		}
		else if (!error.contains(c1)) {
			error.add(c1);
		}
		c2.addError(c1);
		c1.addError(c2);
		return false;
	}
	
	public void addInMap(HashMap<Cluster, ArrayList<Cluster>> map, Cluster c1, Cluster c2) {
		if (map.containsKey(c1)) {
			if (!map.get(c1).contains(c2)) {
				map.get(c1).add(c2);
			}
		}
		else {
			ArrayList<Cluster> temp = new ArrayList<>();
			temp.add(c2);
			map.put(c1, temp);
		}
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getError_positive() {
		return error_positive;
	}

	public void setError_positive(double error_positive) {
		this.error_positive = error_positive;
	}

	public double getError_negative() {
		return error_negative;
	}

	public void setError_negative(double error_negative) {
		this.error_negative = error_negative;
	}

	public double getAdjust_value() {
		return adjust_value;
	}

	public void setAdjust_value(double adjust_value) {
		this.adjust_value = adjust_value;
	}

	public double getTop_value() {
		return top_value;
	}

	public void setTop_value(double top_value) {
		this.top_value = top_value;
	}

	public double getOccupied() {
		return occupied;
	}

	public void setOccupied(double occupied) {
		this.occupied = occupied;
	}

	public double getFree() {
		return free;
	}

	public void setFree(double free) {
		this.free = free;
	}

	public void resetTop_value(Cluster c1, Cluster c2, Sample s1, Sample s2) {
		if (s1.value + s1.error_positive < s2.top_value) {
			s2.top_value = s1.value + s1.error_positive;
			c2.setParent(c1);
		}
	}
	
	public String toString() {
		StringBuffer out = new StringBuffer();
		out.append(this.id);
		out.append(":\n\t");
		out.append(this.value);
		out.append("\n\t");
		out.append(this.error_positive);
		out.append("\n\t");
		out.append(this.error_negative);
		return out.toString();
	}
}
