package eqmap;

import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;

public class OceanQuakeMarker extends SimplePointMarker{
	
	private boolean isOnLand;
	// Less than this threshold is a light earthquake
	public static final float THRESHOLD_MODERATE = 5;
	// Less than this threshold is a minor earthquake
	public static final float THRESHOLD_LIGHT = 4;
	/** Greater than or equal to this threshold is an intermediate depth */
	public static final float THRESHOLD_INTERMEDIATE = 70;
	/** Greater than or equal to this threshold is a deep depth */
	public static final float THRESHOLD_DEEP = 300;
	
	public OceanQuakeMarker(PointFeature feature) {
		// calling EarthquakeMarker constructor
		super(feature.getLocation());
		Object magObj = feature.getProperty("magnitude");
		float mag = Float.parseFloat(magObj.toString());
		isOnLand = false;
	}

	public void draw(PGraphics pg, float x, float y) {
		// save previous styling
		pg.pushStyle();	
		
		pg.fill(0, 0, 255);
		pg.ellipse(x, y, 2*radius, 2*radius);
		// reset to previous styling
		pg.popStyle();
		
	}
}
