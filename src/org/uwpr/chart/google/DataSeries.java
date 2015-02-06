package org.uwpr.chart.google;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DataSeries {

	private String name = "";
	private List <DataPoint> dataPoints;
	private String seriesColor = "000000";
	private boolean drawLabels = true;
	private boolean useDataPointColors = false;
	
    public DataSeries(String name) {
		if (name != null)
			this.name = name;
		dataPoints = new ArrayList<DataPoint>();
	}
	
    public String getSeriesColor() {
        return seriesColor;
    }

    public void setSeriesColor(String seriesColor) {
        this.seriesColor = seriesColor;
    }

    public boolean drawLabels() {
        return drawLabels;
    }

    public void setDrawLabels(boolean drawLabels) {
        this.drawLabels = drawLabels;
    }
    
    public boolean useDataPointColors() {
        return useDataPointColors;
    }

    public void setUseDataPointColors(boolean useDataPointColors) {
        this.useDataPointColors = useDataPointColors;
    }
    
	public String getName() {
		return this.name;
	}
	
	public void addDataPoint(String label, float value) {
		addDataPoint(new DataPoint(label, value));
	}

	public void addDataPoint(DataPoint dataPoint) {
		if (dataPoint != null)
		    dataPoints.add(dataPoint);
	}
	
	public int getSize() {
		return dataPoints.size();
	}
	
	/**
	 * @param idx
	 * @return
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public DataPoint getDataPoint(int idx) {
		if (idx < 0 || idx >= getSize())
			throw new ArrayIndexOutOfBoundsException("invalid index: "+idx);
		return dataPoints.get(idx);
	}
	
	public List <DataPoint> getDataPoints() {
		return this.dataPoints;
	}
	
	public void scaleSeries(float maxAllowedVal) {
		float maxValue = maxValue();
		float scalingFactor = maxAllowedVal/maxValue;
		
		List <DataPoint> dataPoints = getDataPoints();
		for (DataPoint point: dataPoints) {
			float scaled = point.getValue()*scalingFactor;
			scaled = Math.round(scaled*10)/10.0f;
			point.setValue(scaled);
		}
	}
	
	public float maxValue() {
		if (dataPoints.size() == 0)
			return 0;
		DataPoint maxPoint = Collections.max(dataPoints, new Comparator<DataPoint>() {
			public int compare(DataPoint o1, DataPoint o2) {
				float thisVal = o1.getValue();
				float thatVal = o2.getValue();
				
				return thisVal < thatVal ? -1 : (thisVal == thatVal ? 0 : 1);
				
			}});
		
		return maxPoint.getValue();
	}
	
	public void sort() {
		Collections.sort(dataPoints);
	}
	
	public void sortReverse() {
        Collections.sort(dataPoints, new Comparator<DataPoint>(){
            public int compare(DataPoint o1, DataPoint o2) {
                return o2.compareTo(o1);
            }});
    }
	
	public void limitDataPoints(int count) {
	    int last = Math.min(count, dataPoints.size());
	    dataPoints = dataPoints.subList(0, last);
	}
	
	public boolean containsLabel(String label) {
		for (DataPoint p: dataPoints) {
			if (p.getLabel().equals(label))
				return true;
		}
		return false;
	}
	
	public boolean hasData() {
		return dataPoints.size() > 0;
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder("Series: "+name+"\n");
		for (DataPoint point: dataPoints) {
			buf.append(point.toString());
			buf.append("\n");
		}
		return buf.toString();
	}
}
