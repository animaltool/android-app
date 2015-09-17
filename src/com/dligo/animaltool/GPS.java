/*************************************************************************
*  Copyright notice
*
*  (c) 2015 [d] Ligo design+development - All rights reserved
*  (Details on https://github.com/animaltool)
*
*  This script belongs to the TYPO3 Flow package "DLigo.Animaltool".
*  The DLigo Animaltool project is free software; you can redistribute
*  it and/or modify it under the terms of the GNU Lesser General Public
*  License (GPL) as published by the Free Software Foundation; either
*  version 3 of the License, or  (at your option) any later version.
*
*  The GNU General Public License can be found at
*  http://www.gnu.org/copyleft/gpl.html.
*
*  This script is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  This copyright notice MUST APPEAR in all copies of the script!
*************************************************************************/

package com.dligo.animaltool;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

public class GPS implements LocationListener {
	static private GPS instance=null;
	static public LocationManager gpsManager;
	private Location loc;
	private OnChangeListener change=null;

	@Override
	public void onLocationChanged(Location location) {
		loc=location;
		if(change!=null) change.changed(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		loc=null;
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	static public GPS init(Context context){
		if(instance==null) {
			instance=new GPS();
			gpsManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			gpsManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,instance);
			instance.getLocation();			
		}
		return instance;
	}
	
	public void setChangeListener(OnChangeListener listener){
		change=listener;
		instance.getLocation();			
		if(loc!=null) change.changed(loc);
	}
	
	public Location getLocation(){
		if(loc==null) loc = gpsManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
/*		if(loc==null) loc = gpsManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if(loc==null) loc = gpsManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);*/
		return loc;		
	}
	
	public LatLng getLatLng(){
		getLocation();
		if(loc!=null) return new LatLng(loc.getLatitude(),loc.getLongitude());
		return null;
	}
	
	public static GPS get(){
		return instance;
	}
	
	public int getDistance(double lat,double lng){
		getLocation();
		if(loc==null)return 999999;
		float[] result=new float[1];
		Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), lat, lng, result);
		return Math.round(result[0]);
	}
	
	public interface OnChangeListener {
		void changed(Location location);
	}

}