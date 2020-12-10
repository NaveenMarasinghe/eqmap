package eqmap;

import java.util.ArrayList;
import java.util.List;

//Processing library
import processing.core.PApplet;

//Unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
//import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
//import de.fhpotsdam.unfolding.marker.MultiMarker;
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
	
	// country data file location
	private String countryFile = "../data/countries.geo.json";
	
	// A List of country markers
	private List<Marker> countryMarkers;
	
	public void setup() {
		size(950, 600, OPENGL);

		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 700, 500, new MBTilesMapProvider(mbTilesString));
		    //earthquakesURL = "../data/2.5_week.atom"; 	//for working offline
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 700, 500, new Google.GoogleMapProvider());
		}
		
	    map.zoomToLevel(2);
	    MapUtils.createDefaultEventDispatcher(this, map);	
	    
	    // The List to populate with new SimplePointMarkers
	    List<Marker> markers = new ArrayList<Marker>();
	    List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
	    countryMarkers = MapUtils.createSimpleMarkers(countries);
	    
	    //collect properties for each earthquake by parser
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    
	    //add all earthquake to marker list
	    for(PointFeature earthquake: earthquakes) {
	    	if(isLand(earthquake)) {
	    		markers.add(new LandQuakeMarker(earthquake));
	    	}else {
	    		markers.add(new OceanQuakeMarker(earthquake));
	    	}
	    	
	    }
	    
	    // Add the markers to the map
	    map.addMarkers(markers);	    
		
	}
	
	/* createMarker: helper method that takes in an earthquake 
	 * feature and returns a SimplePointMarker for that earthquake
	*/
	private SimplePointMarker createLandMarker(PointFeature feature)
	{  
		// print all features in a PointFeature
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
		fill(255, 250, 240);
		rect(25, 50, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", 50, 75);
		
//		fill(color(255, 0, 0));
//		ellipse(50, 125, 15, 15);
		fill(color(255, 255, 0));
		ellipse(50, 175, 10, 10);
		fill(color(0, 0, 255));
		ellipse(50, 225, 10, 10);
		
		fill(0, 0, 0);
//		text("5.0+ Magnitude", 75, 125);
		text("In land", 75, 175);
		text("In ocean", 75, 225);
	}
	
	// return boolean value earthquake is located in land or not
	private boolean isLand(PointFeature earthquake) {
		
		for(Marker m : countryMarkers) {
			if(isCountry(earthquake, m)) {
				return true;
			}
		}
		return false;
	}
	
	// check earthquake is located in a country
	private boolean isCountry(PointFeature earthquake, Marker country) {

		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
						
					// return if is inside one
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}
}
