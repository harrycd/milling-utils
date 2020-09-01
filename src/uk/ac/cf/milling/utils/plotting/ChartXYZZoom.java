/**
 * 
 */
package uk.ac.cf.milling.utils.plotting;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.camera.AbstractCameraController;
import org.jzy3d.chart.controllers.thread.camera.CameraThreadController;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.rendering.view.View;

import com.jogamp.newt.event.InputEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class ChartXYZZoom extends AbstractCameraController implements MouseListener{
	
	Chart chart;
	
	public ChartXYZZoom(){
	}
	
	public ChartXYZZoom(Chart chart){
	    this.chart = chart;
		register(chart);
		addSlaveThreadController(new CameraThreadController(chart));
		
	}
	
	@Override
    public void register(Chart chart){
		super.register(chart);
		chart.getCanvas().addMouseController(this);
	}
	
	@Override
    public void dispose(){
		for(Chart c: targets){
			c.getCanvas().removeMouseController(this);
		}
		super.dispose();
	}
	
	/** Handles toggle between mouse rotation/auto rotation: double-click starts the animated
	 * rotation, while simple click stops it.*/
	@Override
    public void mousePressed(MouseEvent e) {
		// 
		if(handleSlaveThread(e))
		    return;
			
		prevMouse.x  = e.getX();
		prevMouse.y  = e.getY();
	}
	
    public boolean handleSlaveThread(MouseEvent e) {
        if(isDoubleClick(e)){
			if(threadController!=null){
				threadController.start();
				return true;
			}
		}
		if(threadController!=null)
			threadController.stop();
		return false;
    }

	/** Compute shift or rotate*/
	@Override
    public void mouseDragged(MouseEvent e) {
		Coord2d mouse = new Coord2d(e.getX(),e.getY());
		// Rotate
				if(isLeftDown(e)){
					Coord2d move  = mouse.sub(prevMouse).div(100);
					rotate( move );
				}
				// Shift
				else if(isRightDown(e)){
					Coord2d move  = mouse.sub(prevMouse);
					if(move.y!=0) shiftXYZ(move.x/2, move.y/2);
//						shift(0,move.y/500);
				}
		
		prevMouse = mouse;
	}
	
	public static boolean isLeftDown(MouseEvent e){
    	return (e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK;
	}

	public static boolean isRightDown(MouseEvent e){
		return (e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK; 
	}
	
	public static boolean isDoubleClick(MouseEvent e){
    	return (e.getClickCount() > 1);
	}
	
	/**
	 * Custom way to shifting part towards all dimensions
	 * Non accurate shift but better than no shift 
	 * @param factorX - how much to shift based on X dimension of the window displaying the chart
	 * @param factorY - how much to shift based on Y dimension of the window displaying the chart
	 */
	private void shiftXYZ(final float factorX, final float factorY) {
		View view = chart.getView();
		Coord3d viewPoint = view.getViewPoint();
        
        BoundingBox3d bounds = chart.getView().getBounds();
        
        float sinX = (float) Math.sin(viewPoint.x);
        
        bounds.setXmin(bounds.getXmin() + factorX * sinX);
        bounds.setXmax(bounds.getXmax() + factorX * sinX);
        
        bounds.setYmin(bounds.getYmin() + factorX * (1-sinX));
        bounds.setYmax(bounds.getYmax() + factorX * (1-sinX));
        
        bounds.setZmin(bounds.getZmin()+factorY);
        bounds.setZmax(bounds.getZmax()+factorY);
        
        chart.getView().lookToBox(bounds);
    }
	
	/** Compute zoom */
	@Override
    public void mouseWheelMoved(MouseEvent e) {
		stopThreadController();
		float factor = ((e.getRotation()[1]*e.getRotationScale()));
		
		BoundingBox3d bounds = chart.getView().getBounds();
		bounds.setXmin(bounds.getXmin() - factor);
        bounds.setXmax(bounds.getXmax() + factor);
        
        bounds.setYmin(bounds.getYmin() - factor);
        bounds.setYmax(bounds.getYmax() + factor);
        
        bounds.setZmin(bounds.getZmin() - factor);
        bounds.setZmax(bounds.getZmax() + factor);
        
        if(bounds.getXmin() < bounds.getXmax() || bounds.getYmin() < bounds.getYmax() || bounds.getZmin() < bounds.getZmax() )
        	chart.getView().lookToBox(bounds);
		
	}
	
	@Override
    public void mouseClicked(MouseEvent e) {}  
	@Override
    public void mouseEntered(MouseEvent e) {}
	@Override
    public void mouseExited(MouseEvent e) {}
	@Override
    public void mouseReleased(MouseEvent e) {} 
	@Override
    public void mouseMoved(MouseEvent e) {}
}
