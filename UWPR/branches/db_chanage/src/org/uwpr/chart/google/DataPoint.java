package org.uwpr.chart.google;

public class DataPoint implements Comparable<DataPoint>{

	private float value;
	private String label;
	private String color = "000000";
	
    public DataPoint(String label, float value) {
		this.label = label;
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}
	
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	
	public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

	@Override
	public int compareTo(DataPoint that) {
		return (this.value < that.value) ? -1 : (this.value == that.value ? 0 : 1);
	}
	
	public String toString() {
		return label+", "+value;
	}
}
