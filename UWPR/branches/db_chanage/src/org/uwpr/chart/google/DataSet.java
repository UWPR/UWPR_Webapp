package org.uwpr.chart.google;

import java.util.ArrayList;
import java.util.List;

public class DataSet {

	private String title;
	
	private List <DataSeries> seriesList;
	
	public DataSet() {
		this(null);
	}
	
	public DataSet(String title) {
		this.title = title;
		seriesList = new ArrayList<DataSeries>();
	}
	
	public void addSeries(DataSeries series) {
		if (series != null)
			seriesList.add(series);
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public List <DataSeries> getSeriesList() {
		return seriesList;
	}
	
	public int seriesCount() {
		return seriesList.size();
	}
	
	public void sort() {
		if (seriesList == null)
			return;
		for (DataSeries series: seriesList)
			series.sort();
	}
	
	public void sortReverse() {
        if (seriesList == null)
            return;
        for (DataSeries series: seriesList)
            series.sortReverse();
    }
	
	public boolean hasData() {
		for (DataSeries series: seriesList)
			if (series.hasData())
				return true;
		return false;
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if (title != null)
			buf.append(title+"\n");
		for (DataSeries series: seriesList) {
			buf.append(series.toString());
			buf.append("\n");
		}
		return buf.toString();
	}
}
