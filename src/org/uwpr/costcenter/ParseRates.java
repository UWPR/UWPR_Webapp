package org.uwpr.costcenter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class ParseRates {

	public static void main(String[] args) throws IOException {
		
		// String file = "/Users/vagisha/WORK/UWPR/trunk/schema/uw_internal_total_rates.csv";
		// String outfile = "/Users/vagisha/WORK/UWPR/trunk/schema/uw_internal_total_rates.txt";
		
		// String outfile = "/Users/vagisha/WORK/UWPR/trunk/schema/all_rates.txt";
		
		String outfile = "/Users/vagisha/WORK/UWPR/trunk/schema/nanomate_hplc_rates.txt";

		
		String[] inputFiles = new String[6];
		inputFiles[0] = "/Users/vagisha/WORK/UWPR/trunk/schema/uw_internal_total_rates.csv";
		inputFiles[1] = "/Users/vagisha/WORK/UWPR/trunk/schema/nonprofit_total_rates.csv";
		inputFiles[2] = "/Users/vagisha/WORK/UWPR/trunk/schema/commercial_total_rates.csv";
		inputFiles[3] = "/Users/vagisha/WORK/UWPR/trunk/schema/uw_internal_ffs_total_rates.csv";
		inputFiles[4] = "/Users/vagisha/WORK/UWPR/trunk/schema/nonprofit_ffs_total_rates.csv";
		inputFiles[5] = "/Users/vagisha/WORK/UWPR/trunk/schema/commercial_ffs_total_rates.csv";
		
		int[] rateTypeIds = new int[inputFiles.length];
		rateTypeIds[0] = 1;
		rateTypeIds[1] = 2;
		rateTypeIds[2] = 3;
		rateTypeIds[3] = 4;
		rateTypeIds[4] = 5;
		rateTypeIds[5] = 6;
		
		
		// order of instruments
		// ,FT,OT1/OT2,LTQ,ETD,TSQA,TSQV
		
		int freeRateTypeId = 7;
		
		boolean writeFreeRates = false;
		

		// int auto_incr_id = 1;
		int auto_incr_id = 1017 + 1;
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
		
		// for the old blocks;
//		for(int i = 2; i <= 10; i++) {
//			writer.write(auto_incr_id+"\t"+i+"\t25\t"+freeRateTypeId+"\t0\t1\n");
//			auto_incr_id++;
//		}
		
		
		
		for(int j = 0; j < inputFiles.length; j++) {
			
			String file = inputFiles[j];
			int rateTypeId = rateTypeIds[j];
			
			BufferedReader reader = new BufferedReader(new FileReader(file));


			String line = reader.readLine(); // ,FT,OT1/OT2,LTQ,ETD,TSQA,TSQV
			line = reader.readLine(); // hrs,total,total,total,total,total,total




			while((line = reader.readLine()) != null) {

				String[] tokens = line.split(",");

				int i = 0;

				int hours = Integer.parseInt(tokens[i++]); // this should be the same as the id in the timeBlock table

				String ft_rate = tokens[i++];

				String ot1_rate = tokens[i++];

				String ot2_rate = ot1_rate;

				String ltq_rate = tokens[i++];

				String etd_rate = tokens[i++];

				String tsqa_rate = tokens[i++];

				String tsqv_rate = tokens[i++];


//				// instrumentID for FT: 6
//				writer.write(auto_incr_id+"\t6\t"+hours+"\t"+rateTypeId+"\t"+ft_rate+"\t1");
//				writer.newLine();
//				auto_incr_id++;
//				if(writeFreeRates) {
//					writer.write(auto_incr_id+"\t6\t"+hours+"\t"+freeRateTypeId+"\t0\t1");
//					writer.newLine();
//					auto_incr_id++;
//				}
//
//				// instrumentID for OT1: 4
//				writer.write(auto_incr_id+"\t4\t"+hours+"\t"+rateTypeId+"\t"+ot1_rate+"\t1");
//				writer.newLine();
//				auto_incr_id++;
//				if(writeFreeRates) {
//					writer.write(auto_incr_id+"\t4\t"+hours+"\t"+freeRateTypeId+"\t0\t1");
//					writer.newLine();
//					auto_incr_id++;
//				}
//
//				// instrumentID for OT2: 5
//				writer.write(auto_incr_id+"\t5\t"+hours+"\t"+rateTypeId+"\t"+ot2_rate+"\t1");
//				writer.newLine();
//				auto_incr_id++;
//				if(writeFreeRates) {
//					writer.write(auto_incr_id+"\t5\t"+hours+"\t"+freeRateTypeId+"\t0\t1");
//					writer.newLine();
//					auto_incr_id++;
//				}
//
//				// instrumentID for LTQ: 2
//				writer.write(auto_incr_id+"\t2\t"+hours+"\t"+rateTypeId+"\t"+ltq_rate+"\t1");
//				writer.newLine();
//				auto_incr_id++;
//				if(writeFreeRates) {
//					writer.write(auto_incr_id+"\t2\t"+hours+"\t"+freeRateTypeId+"\t0\t1");
//					writer.newLine();
//					auto_incr_id++;
//				}
//
//				// instrumentID for ETD: 3
//				writer.write(auto_incr_id+"\t3\t"+hours+"\t"+rateTypeId+"\t"+etd_rate+"\t1");
//				writer.newLine();
//				auto_incr_id++;
//				if(writeFreeRates) {
//					writer.write(auto_incr_id+"\t3\t"+hours+"\t"+freeRateTypeId+"\t0\t1");
//					writer.newLine();
//					auto_incr_id++;
//				}
//
//				// instrumentID for TSQ_Access: 7
//				writer.write(auto_incr_id+"\t7\t"+hours+"\t"+rateTypeId+"\t"+tsqa_rate+"\t1");
//				writer.newLine();
//				auto_incr_id++;
//				if(writeFreeRates) {
//					writer.write(auto_incr_id+"\t7\t"+hours+"\t"+freeRateTypeId+"\t0\t1");
//					writer.newLine();
//					auto_incr_id++;
//				}
//				
//				// instrumentID for TSQ_Vantage: 10
//				writer.write(auto_incr_id+"\t10\t"+hours+"\t"+rateTypeId+"\t"+tsqv_rate+"\t1");
//				writer.newLine();
//				auto_incr_id++;
//				if(writeFreeRates) {
//					writer.write(auto_incr_id+"\t10\t"+hours+"\t"+freeRateTypeId+"\t0\t1");
//					writer.newLine();
//					auto_incr_id++;
//				}

				// instrumentID for nanoMate: 8
				writer.write(auto_incr_id+"\t8\t"+hours+"\t"+rateTypeId+"\t0.0\t1");
				writer.newLine();
				auto_incr_id++;
				
				
				// instrumentID for Agilent HPLC: 9
				writer.write(auto_incr_id+"\t9\t"+hours+"\t"+rateTypeId+"\t0.0\t1");
				writer.newLine();
				auto_incr_id++;
				


			}
			
			reader.close();
		}
		
		writer.close();
	}
}
