package eqmap;

import java.util.ArrayList;
import java.util.List;

//Processing library
import processing.core.PApplet;

//Unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;

//Parsing library
import parsing.ParseFeed;

public class EarthquakeCityMap extends PApplet{
	
	//keep eclipse from generating a warning.
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFLINE, change the value of this variable to true
	private static final boolean offline = false;
	
	// Less than this threshold is a light earthquake
	public static final float THRESHOLD_MODERATE = 5;
	// Less than this threshold is a minor earthquake
	public static final float THRESHOLD_LIGHT = 4;

	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	// The map
	private UnfoldingMap map;
	
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	public void setup() {
		size(950, 600, OPENGL);

		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 700, 500, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom"; 	//for working offline
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 700, 500, new Google.GoogleMapProvider());
		}
		
	    map.zoomToLevel(2);
	    MapUtils.createDefaultEventDispatcher(this, map);	
	    
	    // The List to populate with new SimplePointMarkers
	    List<Marker> markers = new ArrayList<Marker>();
	    
	    //collect properties for each earthquake by parser
	    //PointFeatures have a getLocation method
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    
	    //add all earthquake to marker list
	    for(PointFeature earthquake: earthquakes) {
	    	markers.add(createMarker(earthquake));
	    }
	    
	    // Add the markers to the map
	    map.addMarkers(markers);	    
		
	}
	
	/* createMarker: helper method that takes in an earthquake 
	 * feature and returns a SimplePointMarker for that earthquake
	*/
	private SimplePointMarker createMarker(PointFeature feature)
	{  
		// print all features in a PointFeature
		// need call createMarekr from setup 
		//System.out.println(feature.getProperties());
		
		// Create a new SimplePointMarker at the location given by the PointFeature
		SimplePointMarker marker = new SimplePointMarker(feature.getLocation());
		
		Object magObj = feature.getProperty("magnitude");
		float mag = Float.parseFloat(magObj.toString());
		
	    int yellow = color(255, 255, 0);
	    int red = color(255, 0, 0);
	    int blue = color(0, 0, 255);
		
		// style the marker's size and color 
	    // according to the magnitude of the earthquake.  
	    if (mag>THRESHOLD_MODERATE) {
	    	marker.setColor(red);
	    	marker.setRadius(mag*3);
	    } else if (mag>THRESHOLD_LIGHT) {
	    	marker.setColor(yellow);
	    	marker.setRadius(mag*3);
	    } else {
	    	marker.setColor(blue);
	    	marker.setRadius(mag*3);
	    }
	    
	    //return the marker
	    return marker;
	}
	
	public void draw() {
		//draw the map
	    background(10);
	    map.draw();
	    addKey();
	}
	
	private void addKey() {	
		//generate key field
		rect(30,50,100,150,7);
	}
}
