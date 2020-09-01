/**
 * 
 */
package uk.ac.cf.milling.utils.plotting;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.camera.AbstractCameraController;
import org.jzy3d.chart.controllers.thread.camera.CameraThreadController;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord2d;

import com.jogamp.newt.event.InputEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

/**
 * @author Theocharis Alexopoulos
 * @date 1 Sep 2020
 *
 */
public class ChartXZoomController extends AbstractCameraController implements MouseListener {
	
	Chart chart;
	BoundingBox3d defaultBounds;
	
	@SuppressWarnings("unused")
	private ChartXZoomController() {
		//to prevent wrong construction
	}
	
	/**
	 * 
	 */
	public ChartXZoomController(Chart chart) {
		this.chart = chart;
		register(chart);
		addSlaveThreadController(new CameraThreadController(chart));
		defaultBounds = chart.getView().getBounds();
	}
	
	@Override
	public void register(Chart chart) {
		super.register(chart);
		chart.getCanvas().addMouseController(this);
	}
	
	@Override
	public void dispose() {
		for (Chart c: targets) {
			c.getCanvas().removeMouseController(this);
		}
		super.dispose();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(handleSlaveThread(e)) {
			return;
		}

		if (isDoubleClick(e)) {
			chart.getView().lookToBox(defaultBounds);
		}

	}
	
	public boolean handleSlaveThread(MouseEvent e) {
        if(isDoubleClick(e)){
			if(threadController != null){
				threadController.start();
				return true;
			}
		}
		if(threadController != null)
			threadController.stop();
		return false;
    }

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(handleSlaveThread(e)) {
			return;
		}
		prevMouse.x  = e.getX();
		prevMouse.y  = e.getY();
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		prevMouse.x = e.getX();
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Coord2d mouse = new Coord2d(e.getX(),e.getY());
		if(isLeftDown(e)) {
			Coord2d move = new Coord2d((e.getX() - prevMouse.x), 0); 
			shiftX( move );
		}
		
		prevMouse = mouse;
		
	}
	
	private void shiftX(Coord2d move) {
		stopThreadController();
		BoundingBox3d bounds = chart.getView().getBounds();
		final float factor = bounds.getXRange().getRange();
		bounds.setXmin(bounds.getXmin() - 0.001f * factor * move.x);
		bounds.setXmax(bounds.getXmax() - 0.001f * factor * move.x);
        chart.getView().lookToBox(bounds);
	}

	private static boolean isLeftDown(MouseEvent e){
    	return (e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK;
	}

	private static boolean isDoubleClick(MouseEvent e){
    	return (e.getClickCount() > 1);
	}
	
	

	@Override
	public void mouseWheelMoved(MouseEvent e) {
				BoundingBox3d bounds = chart.getView().getBounds();
		final float factor = (0.1f * bounds.getXRange().getRange() * e.getRotation()[1]);
		stopThreadController();
		if (bounds.getXmin() + 2*factor < bounds.getXmax()){
			bounds.setXmin(bounds.getXmin() + factor);
			bounds.setXmax(bounds.getXmax() - factor);
			chart.getView().lookToBox(bounds);
		}
	}

}
