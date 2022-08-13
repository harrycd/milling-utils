/**
 * 
 */
package uk.ac.cf.milling.utils.plotting;

import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jzy3d.chart.Chart;

import uk.ac.cf.milling.objects.SettingsSingleton;

/**
 * Methods for registering and de-registering charts in the results panel.
 * @author Theocharis Alexopoulos
 *
 */
public class PlotterUtils {
	public static void disposeAttachedCharts(String name) {
		String[] ids = name.split(",");
		Map<String, Object> chartRegistry = SettingsSingleton.getInstance().chartRegistry;
		for (String id : ids) {
			Object chartObj = chartRegistry.get(id);
			if (chartObj != null) {
				Chart chart = (Chart) chartObj;
				chart.dispose();
				chartRegistry.remove(id);
				System.out.println("Removed");
			}
		}
	}
	
	public static void registerChart(JPanel chartPanel, Chart chart) {
		String name = Long.toString(System.currentTimeMillis());
		chartPanel.setName(name);
		SettingsSingleton.getInstance().chartRegistry.put(name, chart);
	}

	public static void registerChart(JScrollPane scrollPanel, List<JPanel> panels) {
		String name = "";
		for (JPanel panel : panels) {
			name += panel.getName() + ",";
		}
		scrollPanel.setName(name);
	}
}

