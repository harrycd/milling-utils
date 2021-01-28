/**
 * 
 */
package uk.ac.cf.milling.utils.simulation;

import java.util.List;

import uk.ac.cf.milling.objects.KPIs;
import uk.ac.cf.milling.objects.Nc;
import uk.ac.cf.milling.objects.SimulatorConfig;
import uk.ac.cf.milling.utils.data.DataManipulationUtils;
import uk.ac.cf.milling.utils.data.GCodeAnalyser;
import uk.ac.cf.milling.utils.data.GCodeAnalyserUGS;
import uk.ac.cf.milling.utils.data.IoUtils;
import uk.ac.cf.milling.utils.db.NcUtils;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class SimulatorUtils {
	/**
	 * @param kpis
	 * @param config
	 * @return
	 */
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
	
	public static KPIs simulateGCodeFileUGS(KPIs kpis, SimulatorConfig config) {
		//parse gcode and generate the intermediate csv file
		new GCodeAnalyserUGS().parseGCode(config.getInputFilePath());
		Nc nc = NcUtils.getCreateNcFile(config.getInputFilePath());
				
		//TODO add separate field in config so when gcode file is setup, an analysis file is set automatically.
		config.setInputFilePath(config.getInputFilePath() + "_data.csv");
		simulateAnalysisFile(kpis, config);
		
		NcUtils.updateNcAnalysis(nc.getNcId(), config.getInputFilePath(), config.getBillet().getBilletId());
		
		return kpis;
		
	}

	/**
	 * @param kpis
	 * @param config
	 */
	public static KPIs simulateAnalysisFile(KPIs kpis, SimulatorConfig config) {
						//Clean the file from duplicates
						System.out.print("Cleaning Analysis File...");
		String analysisFilePath  = config.getInputFilePath();
		DataManipulationUtils.cleanDuplicateCoordinates(analysisFilePath);
						System.out.println("done");
						
						//Interpolate points to create a continuous toolpath
						System.out.print("Interpolating toolpath points...");
		DataManipulationUtils.interpolateToolpathPoints(analysisFilePath);
		analysisFilePath = analysisFilePath.substring(0, analysisFilePath.length() - 4) + "_smooth.csv";
						System.out.println("done");
						
						// Parse the analysis file
						System.out.print("Parsing Analysis File...");
		KPIUtils.updateKPIs(kpis, DataManipulationUtils.parseDataFile(analysisFilePath));
						System.out.println("done");
						System.out.println("GCode analysed in " + kpis.getTimePoints().length + " steps.");

						// Calculate material removal rate
						System.out.print("Calculating Material Removal Parameters...\n");
		KPIUtils.updateKPIs(kpis, MRRCalculator.calculateMRR(kpis, config));
						System.out.println("done");
						
						// Add MRR at the analysis file
						System.out.println("Adding MRR to analysis file");
		IoUtils.addColumnToCSVFile(analysisFilePath, "MRR", kpis.getMr());
		IoUtils.addColumnToCSVFile(analysisFilePath, "MRR", kpis.getMrr());
						System.out.println("done");
						
		return kpis;
	}
	
	/**
	 * @param kpis
	 * @param config
	 * @return
	 */
	public static KPIs simulateCsvFile(KPIs kpis, SimulatorConfig config) {
		
				//Clean the file from duplicates
				System.out.print("Cleaning Analysis File...");
		String csvFilePath = config.getInputFilePath();
		DataManipulationUtils.cleanDuplicateCoordinates(csvFilePath);
		csvFilePath += "_clean.csv";
				System.out.println("done");
				
				//Interpolate points to create a continuous toolpath
				System.out.print("Interpolating toolpath points...");
		DataManipulationUtils.interpolateToolpathPoints(csvFilePath);
		csvFilePath = csvFilePath.substring(0, csvFilePath.length() - 4) + "_smooth.csv";
				System.out.println("done");
		
				// Parse the analysis file
				System.out.print("Parsing CSV File...");
		KPIUtils.updateKPIs(kpis, DataManipulationUtils.parseDataFile(csvFilePath));
				System.out.println("done");
				System.out.println("Data file contains " + kpis.getTimePoints().length + " steps.");
		
				// Calculate material removal rate
				System.out.print("Calculating Material Removal Parameters...");
		KPIUtils.updateKPIs(kpis, MRRCalculator.calculateMRR(kpis, config));
				System.out.println("done");

				// Create nc entry (as it is simulated separately new entry is created) 
				System.out.print("Writing Analysis File...");
		Nc nc = NcUtils.getCreateNcFile(csvFilePath);
		csvFilePath = csvFilePath.substring(0, csvFilePath.length() - 4) + "_data.csv";
		kpis.setAnalysisData(KPIUtils.generateAnalysisFile(kpis));
		IoUtils.writeFile(csvFilePath, kpis.getAnalysisData());
		NcUtils.updateNcAnalysis(nc.getNcId(), csvFilePath, config.getBillet().getBilletId());
				System.out.println("done");
				

		return kpis;
		
	}
	
	/**
	 * @param kpis
	 * @param config
	 * @return
	 */
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

}
