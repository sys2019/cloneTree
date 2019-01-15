package main;

public class Mutation {
	
	private String id;
	private double value;
	private double error_positive;
	private double error_negative;
	
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
	
	
	
}
