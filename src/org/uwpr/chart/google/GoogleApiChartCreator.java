package org.uwpr.chart.google;

import java.util.List;

public class GoogleApiChartCreator {

	public static final int BAR_CHART_HS = 0;
	public static final int BAR_CHART_V = 3;
	public static final int LINE_CHART = 1;
	public static final int PIE_CHART = 2;
	
	
	private static final String[] CHART_MAPPING = new String[4];
	static {
		CHART_MAPPING[BAR_CHART_HS] = "bhs";
		CHART_MAPPING[BAR_CHART_V] = "bvs";
		CHART_MAPPING[LINE_CHART] = "lc";
		CHART_MAPPING[PIE_CHART] = "p";
	}
	
	private int myChartType = BAR_CHART_HS; // default chart type
	private DataSet myData;
	private int width = 350;
	private int height = 150;
	
	/**
	 * @param chartType
	 * @param dataset
	 * @throws IllegalArgumentException -- if the chartType is not valid
	 */
	public GoogleApiChartCreator(int chartType, DataSet dataset) {
		if (chartType < 0 || chartType >= CHART_MAPPING.length)
			throw new IllegalArgumentException("Invalid chart type: "+chartType);
		this.myChartType = chartType;
		this.myData = dataset;
//		scaleData();
	}
	
	public static String getPieChartURL(DataSet dataset, int width, int height) {
		GoogleApiChartCreator creator = new GoogleApiChartCreator(PIE_CHART, dataset);
		creator.setSize(width, height);
		return creator.getChartURL();
	}
	
	public static String getLineChartURL(DataSet dataset, int width, int height) {
		GoogleApiChartCreator creator = new GoogleApiChartCreator(LINE_CHART, dataset);
		creator.setSize(width, height);
		return creator.getChartURL();
	}
	
	public static String getStackedBarChartHorizURL(DataSet dataset, int width, int height) {
        GoogleApiChartCreator creator = new GoogleApiChartCreator(BAR_CHART_HS, dataset);
        creator.setSize(width, height);
        return creator.getChartURL();
    }
	
	public static String getBarChartVertURL(DataSet dataset, int width, int height) {
        GoogleApiChartCreator creator = new GoogleApiChartCreator(BAR_CHART_V, dataset);
        creator.setSize(width, height);
        return creator.getChartURL();
    }
	
	public String getChartURL() {
		StringBuilder buf = new StringBuilder("http://chart.apis.google.com/chart?");
		buf.append(chartType());
		buf.append(chartTitle());
		buf.append(chartSize());
		buf.append(chartColor());
		buf.append(datasetParams());
		return buf.toString();
	}
	
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	private String chartType() {
		return "cht="+CHART_MAPPING[myChartType]+"&";
	}
	
	private String chartTitle() {
		String title = myData.getTitle();
		if (title == null || title.length() == 0)
			return "";
		title = title.replaceAll("\\s", "+");
		return "chtt="+title+"&";
	}
	
	private String chartSize() {
		return "chs="+width+"x"+height+"&";
	}
	
	private String chartColor() {
		if (myData.seriesCount() == 0)
			return "";
		
			StringBuilder buf = new StringBuilder("chco=");
			if(myChartType == LINE_CHART) {
			    for (DataSeries series: myData.getSeriesList())
			        buf.append(series.getSeriesColor()+",");
			
			    removeCharAtEnd(buf, ',');
			}
			else if(myChartType == PIE_CHART) {
			    buf.append(myData.getSeriesList().get(0).getSeriesColor());
			}
			else if (myChartType == BAR_CHART_HS || myChartType == BAR_CHART_V) {
			    for(DataSeries series: myData.getSeriesList()) {
			        if(series.useDataPointColors()) {
			            for(DataPoint dp: series.getDataPoints()) {
			                buf.append(dp.getColor()+"|");
			            }
			            removeCharAtEnd(buf, '|');
			        }
			        else {
			            buf.append(series.getSeriesColor());
			        }
			        buf.append(",");
			    }
			    removeCharAtEnd(buf, ',');
			}
			buf.append("&");
			return buf.toString();
	}
	
	private String datasetParams() {
		if (myData.seriesCount() == 0)	return "";
		switch (myChartType) {
		case BAR_CHART_HS:
			return barChartStackedHorizParams();
		case BAR_CHART_V:
		    return barChartVertParams();
		case PIE_CHART:
			return pieChartParams();
		case LINE_CHART:
			return lineChartParams();
		default:
			return "";
		}
	}
	
	private String barChartStackedHorizParams() {
		
		StringBuilder data = new StringBuilder("chd=t:");
		StringBuilder labels = new StringBuilder();
		StringBuilder markers = new StringBuilder("chm=");
		
		int seriesIdx = 0;
		float[] comboValues = new float[myData.getSeriesList().get(0).getDataPoints().size()];
		for(DataSeries series: myData.getSeriesList()) {
		    int pointIdx = 0;
		    for (DataPoint point: series.getDataPoints()) {
		        data.append(point.getValue());
		        data.append(",");

		        if(seriesIdx == 0)
                    labels.insert(0, point.getLabel()+"|");
		        
		        comboValues[pointIdx] = point.getValue() + comboValues[pointIdx];
		        pointIdx++;
		    }
		    seriesIdx++;
		    removeCharAtEnd(data, ',');
		    data.append("|");
		}
		
		for(int i = 0; i < comboValues.length; i++) {
		    int sIdx = myData.getSeriesList().size() - 1;
		    DataSeries series = myData.getSeriesList().get(sIdx);
		    if(series.getDataPoint(i).getValue() == 0) {
		        sIdx = Math.max(0, sIdx - 1);
		    }
		    // markers.append(String.format("t%.1f,000000,%d,%d,12.0,1|", comboValues[i],sIdx,i));
		    markers.append("t"+Math.round(comboValues[i])+"%,000000,"+sIdx+","+i+",12.0,1|");
		}
		removeCharAtEnd(data, '|');
		removeCharAtEnd(labels, '|');
		removeCharAtEnd(markers, '|');
		
		data.append("&");
		data.append("chxt=x,y&chxl=1:|");
		data.append(labels.toString());
		data.append("&");
		data.append(markers.toString());
		data.append("&chg=20,20,2,3"); // grid lines
		return data.toString();
	}
	
	private String barChartVertParams() {
        
	    // http://chart.apis.google.com/chart?chs=300x200&chxt=x,x&
	    // chxp=1,50&
	    // chxl=0:|23|101|123|345|4456|789|1:|Project+ID|&
	    // chtt=Usage+by+Project&
	    // chds=0,135&
	    // cht=bvs&
	    // chd=t:100.0,135.0,36.0,33.0,17.0,10.0&
	    // chco=0000FF&
	    // chm=N,000000,0,-1,11&
	    // chg=20,20,2,3
        StringBuilder data = new StringBuilder("chd=t:");
        StringBuilder labels = new StringBuilder();
        
        int max = 0;
        
        int seriesIdx = 0;
        for(DataSeries series: myData.getSeriesList()) {
            int pointIdx = 0;
            for (DataPoint point: series.getDataPoints()) {
                max = (int) Math.max(max, point.getValue());
                data.append(point.getValue());
                data.append(",");

                if(seriesIdx == 0)
                    labels.append(point.getLabel()+"|");
                pointIdx++;
            }
            removeCharAtEnd(data, ',');
            data.append("|");
            seriesIdx++;
        }
        removeCharAtEnd(data, '|');
        removeCharAtEnd(labels, '|');
        
        data.append("&");
        data.append("chbh=a"); // make the bars fit
        data.append("&");
        data.append("chds=0,"+max);
        data.append("&");
        data.append("chxt=x,x&chxl=0:|");
        data.append(labels.toString());
        data.append("|1:|"+myData.getSeriesList().get(0).getName()+"|");
        data.append("&chxp=1,50");
        data.append("&");
        if(myData.getSeriesList().size() > 0) {
            int i = 0;
            for(DataSeries series: myData.getSeriesList()) {
                if(series.drawLabels()) {
                    if(i == 0)
                        data.append("chm=");
                    if(i > 0)
                        data.append("|");
                    data.append("N,000000,"+i+",-1,11");
                }
                i++;
            }
        }
        data.append("&chg=20,20,2,3"); // grid lines
        return data.toString();
    }
	
	private String pieChartParams() {
		
		StringBuilder data = new StringBuilder("chd=t:");
		StringBuilder labels = new StringBuilder("chl=");
		// pie chart should only have one data series
		DataSeries series = myData.getSeriesList().get(0);
		for (DataPoint point: series.getDataPoints()) {
			data.append(point.getValue());
			data.append(",");
			
			labels.append(point.getLabel());
			labels.append("|");
		}
		removeCharAtEnd(data, ',');
		removeCharAtEnd(labels, '|');
		
		data.append("&");
		data.append(labels.toString());
		return data.toString();
	}
	
	private String lineChartParams() {
		StringBuilder data = new StringBuilder("chd=t:");
		StringBuilder labels = new StringBuilder("chxt=x,y&chxl=0:|");
		StringBuilder labelPos = new StringBuilder("chxp=0,");
		StringBuilder legend = new StringBuilder("chdl=");
	
		// iterate over all data series
		List <DataSeries> seriesList = myData.getSeriesList();
		
		// Control the number of lables on the x-axis.
		int seriesIdx = 0;
		float position = 0.0f;
		float posIncr = 0.0f;
		int labelIdx = 0;
		int labelSkip = 0;
		int numLabels = width/75;
		if (seriesList.size() > 0) {
			if (seriesList.get(0).getSize() > 0) {
				int numPoints = seriesList.get(0).getSize();
				posIncr = 100.0f/(numPoints-1);
				labelSkip = (int) Math.ceil(numPoints/(double)numLabels);
			}
		}
		
		for (DataSeries series: seriesList) {
			
			legend.append(series.getName()+"|");
			
			float pointIdx = 0.0f;
			for (DataPoint point: series.getDataPoints()) {
				data.append(point.getValue());
				data.append(",");
				
				if (seriesIdx == 0) {
					if (labelSkip > 0 && labelIdx%labelSkip == 0) {
						labels.append(point.getLabel());
						labels.append("|");
					
						labelPos.append(position);
						labelPos.append(",");
					}
					position += posIncr;
					labelIdx++;
				}
				pointIdx++;
			}
			removeCharAtEnd(data, ',');
			data.append("|");
			seriesIdx++;
		}
		
		removeCharAtEnd(legend, '|');
		removeCharAtEnd(data, '|');
		removeCharAtEnd(labels, '|');
		removeCharAtEnd(labelPos, ',');
		
		data.append("&");
		data.append(labels.toString());
		data.append("&");
		data.append(labelPos.toString());
		data.append("&");
		data.append(legend.toString());
		data.append("&chg=20,20,2,3"); // grid lines
		return data.toString();
	}
	
	private void removeCharAtEnd(StringBuilder buf, char toRemove) {
		if (buf.charAt(buf.length() -1) == toRemove)
			buf.deleteCharAt(buf.length() -1);
	}
	
//	private void scaleData() {
//		List <DataSeries> seriesList = myData.getSeriesList();
//		for (DataSeries series: seriesList) {
//			series.scaleSeries(100.0f);
//		}
//	}
	
	public static void main(String[] args) {
		DataSet data = new DataSet("Pie Chart");
		DataSeries series1 = new DataSeries("All");
		series1.addDataPoint("Jan'08", 10);
		series1.addDataPoint("Feb'08", 20);
		series1.addDataPoint("Mar'08", 120);
		series1.addDataPoint("Apr'08", 50);
		series1.addDataPoint("May'08", 30);
		series1.addDataPoint("June'08", 80);
		series1.addDataPoint("Jul'08", 10);
		series1.addDataPoint("Aug'08", 20);
		series1.addDataPoint("Sep'08", 120);
		series1.addDataPoint("Oct'08", 50);
		series1.addDataPoint("Nov'08", 30);
		series1.addDataPoint("Dec'08", 80);
		series1.addDataPoint("Jan'09", 120);
		series1.addDataPoint("Fev'09", 50);
		series1.addDataPoint("Mar'09", 30);
		series1.addDataPoint("Apr'09", 80);
		data.addSeries(series1);
		
//		DataSeries series2 = new DataSeries("One");
//		series2.addDataPoint("MacCoss", 30);
//		series2.addDataPoint("Goodlett", 10);
//		series2.addDataPoint("Sharma", 0);
//		series2.addDataPoint("Riffle", 80);
//		series2.addDataPoint("Baker", 60);
//		series2.addDataPoint("Eng", 50);
//		data.addSeries(series2);
		
		series1.setSeriesColor("0000FF");
		String url = GoogleApiChartCreator.getPieChartURL(data, 350, 150);
		System.out.println(url);
		
		url = GoogleApiChartCreator.getStackedBarChartHorizURL(data, 350, 150);
		System.out.println(url);
		
		url = GoogleApiChartCreator.getLineChartURL(data, 450, 150);
		System.out.println(url);
	}
}
