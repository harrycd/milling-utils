/**
 * 
 */
package uk.ac.cf.milling.utils.simulation;

import java.util.List;

import uk.ac.cf.milling.objects.KPIs;
import uk.ac.cf.milling.objects.Nc;
import uk.ac.cf.milling.objects.SimulatorConfig;
import uk.ac.cf.milling.utils.data.DataManipulationUtils;
//import uk.ac.cf.milling.utils.data.GCodeAnalyser;
import uk.ac.cf.milling.utils.data.GCodeAnalyserUGS;
import uk.ac.cf.milling.utils.data.IoUtils;
import uk.ac.cf.milling.utils.db.NcUtils;

/**
 * Top level methods to start a simulator run.
 * @author Theocharis Alexopoulos
 *
 */
public class SimulatorUtils {
	
	
	public static KPIs simulateGCodeFileUGS(KPIs kpis, SimulatorConfig config) {
		String inputFilePath = config.getInputFilePath();
		
				System.out.print("Generating CSV data file from G-Code...");
		//parse gcode and generate the intermediate csv file
		List<String[]> csvList = new GCodeAnalyserUGS().parseGCode(inputFilePath);
		inputFilePath += ".csv";
		IoUtils.writeCSVFile(inputFilePath, csvList);
				System.out.println("done");
		
		//TODO add separate field in config so when gcode file is setup, an analysis file is set automatically.
		config.setInputFilePath(inputFilePath);
		simulateAnalysisFile(kpis, config);
		
		return kpis;
		
	}

	/**
	 * @param kpis
	 * @param config
	 */
	public static KPIs simulateAnalysisFile(KPIs kpis, SimulatorConfig config) {
				//Clean the file from duplicates
				System.out.print("Cleaning CSV Data File...");
		String csvFilePath = config.getInputFilePath();
		String cleanData = DataManipulationUtils.getCleanFromDuplicateCoordinatesData(csvFilePath);
		csvFilePath = csvFilePath.substring(0, csvFilePath.length() - 4) + "_clean.csv";
		IoUtils.writeFile(csvFilePath, cleanData);
				System.out.println("done");
				
				//Interpolate points to create a continuous toolpath
				System.out.print("Interpolating toolpath points...");
		DataManipulationUtils.interpolateToolpathPoints(csvFilePath);
		csvFilePath = csvFilePath.substring(0, csvFilePath.length() - 4) + "_smooth.csv";
				System.out.println("done");
				
				// Parse the analysis file
				System.out.print("Parsing CSV Data File...");
		KPIUtils.updateKPIs(kpis, DataManipulationUtils.parseDataFile(csvFilePath));
				System.out.println("done");
				System.out.println("Preprocessed CSV Data file contains " + kpis.getTimePoints().length + " samples.");
				
				// Calculate material removal rate
				System.out.print("Calculating Material Removal Parameters...");
		KPIUtils.updateKPIs(kpis, MRRCalculator.calculateMRR(kpis, config));
				System.out.println("done");
				
				// Add MRR into the analysis file
				System.out.println("Adding MRR to analysis file");
		IoUtils.copyFile(csvFilePath, csvFilePath.substring(0, csvFilePath.length() - 4) + "_data.csv");
		csvFilePath = csvFilePath.substring(0, csvFilePath.length() - 4) + "_data.csv";
		IoUtils.addColumnToCSVFile(csvFilePath, "MR", kpis.getMr());
		IoUtils.addColumnToCSVFile(csvFilePath, "MRR", kpis.getMrr());
				System.out.println("done");
				
				//TODO Add derivative data into the analysis file
				System.out.println("Calculating derivative data");
		DerivativeCalculator.addDerivativeData(csvFilePath, config.getBillet().getMaterialId());
				System.out.println("done");
				
				// Save the file path of the analysis file
		Nc nc = NcUtils.getCreateNcFile(config.getInputFilePath());
		NcUtils.updateNcAnalysis(nc.getNcId(), csvFilePath, config.getBillet().getBilletId());
				
		return kpis;
	}
	
	/**
	 * @param kpis
	 * @param config
	 * @return
	 
	public static KPIs simulateCsvFile(KPIs kpis, SimulatorConfig config) {
		
				//Clean the file from duplicates
				System.out.print("Cleaning CSV Data File...");
		String csvFilePath = config.getInputFilePath();
		String cleanData = DataManipulationUtils.getCleanFromDuplicateCoordinatesData(csvFilePath);
		csvFilePath += "_clean.csv";
		IoUtils.writeFile(csvFilePath, cleanData);
				System.out.println("done");
				
				//Interpolate points to create a continuous toolpath
				System.out.print("Interpolating toolpath points...");
		DataManipulationUtils.interpolateToolpathPoints(csvFilePath);
		csvFilePath = csvFilePath.substring(0, csvFilePath.length() - 4) + "_smooth.csv";
				System.out.println("done");
		
				// Parse the analysis file
				System.out.print("Parsing CSV Data File...");
		KPIUtils.updateKPIs(kpis, DataManipulationUtils.parseDataFile(csvFilePath));
				System.out.println("done");
				System.out.println("Preprocessed CSV Data file contains " + kpis.getTimePoints().length + " samples.");
		
				// Calculate material removal rate
				System.out.print("Calculating Material Removal Parameters...");
		KPIUtils.updateKPIs(kpis, MRRCalculator.calculateMRR(kpis, config));
				System.out.println("done");
				
				//Add the results to the csv data file
				System.out.print("Adding MRR in CSV Data File...");
		csvFilePath = csvFilePath.substring(0, csvFilePath.length() - 4) + "_data.csv";
		kpis.setAnalysisData(KPIUtils.generateAnalysisFile(kpis));
		IoUtils.writeFile(csvFilePath, kpis.getAnalysisData());
				System.out.println("done");

		// Create nc entry (as it is simulated separately new entry is created) 
		Nc nc = NcUtils.getCreateNcFile(csvFilePath);
		NcUtils.updateNcAnalysis(nc.getNcId(), csvFilePath, config.getBillet().getBilletId());
				

		return kpis;
		
	}
	*/
	
	/**
	 * @param kpis
	 * @param config
	 * @return
	 
	public static KPIs simulateJacobFile(KPIs kpis, SimulatorConfig config){
						// Parse the data file
						System.out.println("Parsing the data file...");
		kpis = DataManipulationUtils.parseDataFile(config.getInputFilePath());
						System.out.println("done");
						
						// Calculate material removal rate
						System.out.print("Calculating Material Removal Parameters...\n");
		KPIUtils.updateKPIs(kpis, MRRCalculator.calculateMRR(kpis, config));
						System.out.println("done");
		return kpis;
	}
	 */
	
	/**
	 * @param kpis
	 * @param config
	 * @return
	 
	public static KPIs simulateGCodeFile(KPIs kpis, SimulatorConfig config){
						// Parse the GCode file
						System.out.print("Parsing GCode file...");
		List<String[]> blocks = new GCodeAnalyser().parseGCode(config.getInputFilePath());
		Nc nc = NcUtils.getCreateNcFile(config.getInputFilePath());
						System.out.println("done");

						// Simulate the GCode and calculate time, distance and produce simulation analysis
						System.out.print("Analyse parsed GCode...");
		KPIUtils.updateKPIs(kpis, new GCodeAnalyser().analyseGCode(blocks, config));
						System.out.println("done");
						System.out.println("Total milling time: " + ((int) kpis.getTime())+ "sec");

						// Write simulation analysis to the specified file
						System.out.print("Creating Simulation Analysis file...");
		String inputFilePath = config.getInputFilePath()+"_data.csv";
		IoUtils.writeFile(inputFilePath, kpis.getAnalysisData());
		NcUtils.updateNcAnalysis(nc.getNcId(), inputFilePath, config.getBillet().getBilletId());
						System.out.println("done");

						// Parse the analysis file
						System.out.print("Parsing Analysis File...");
		KPIUtils.updateKPIs(kpis, DataManipulationUtils.parseDataFile(inputFilePath));
						System.out.println("done");
						System.out.println("GCode analysed in " + kpis.getTimePoints().length + " steps.");
						
						// Calculate material removal rate
						System.out.print("Calculating Material Removal Parameters...");
		KPIUtils.updateKPIs(kpis, MRRCalculator.calculateMRR(kpis, config));
						System.out.println("done");
						
						// Add MRR at the analysis file
						System.out.println("Adding MRR to analysis file");
		IoUtils.addColumnToCSVFile(inputFilePath, "MR", kpis.getMr());
		IoUtils.addColumnToCSVFile(inputFilePath, "MRR", kpis.getMrr());
						System.out.println("done");
		
		return kpis;
	}
	*/
}
