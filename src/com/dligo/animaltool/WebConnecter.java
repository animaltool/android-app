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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dligo.animaltool.DatabaseHandler.Record;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class WebConnecter {

	static abstract private class Item {
		protected JSONObject intern;

		protected Item(JSONObject json) {
			this.intern = json;
		}

		public abstract String toString();

		public String getId() {
			try {
				return this.intern.getString("__identity");
			} catch (JSONException e) {
				return "";
			}
		}

	}

	static class Gender extends Item {
		String id;
		String text;

		Gender(String id, String text) {
			super(new JSONObject());
			this.id = id;
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}

		public String getId() {
			return id;
		}
	}

	static class User extends Item {
		private long lastID;

		private User(JSONObject json) {
			super(json);
			try {
				this.lastID = this.intern.getInt("lastBoxID");
			} catch (JSONException e) {
			}
		}

		public String getTeamID() {
			String id = "";
			try {
				id = intern.getString("teamID");
			} catch (JSONException e) {
			}
			return id;
		}

		public String getLastBoxID() {
			return getTeamID() + "-" + String.valueOf(lastID);
		}

		public String getNextBoxID() {
			return getTeamID() + "-" + String.valueOf(lastID + 1);
		}

		private void setLastID(long id) {
			lastID = id;
			instance.db.changeLoginLastID(instance.loginData, lastID);
		}

		public String toString() {
			try {
				return this.intern.getString("name");
			} catch (JSONException e) {
				return "";
			}
		}
	}

	static class Location extends Item {

		protected Location(JSONObject json) {
			super(json);
		}

		public String toString() {
			try {
				return this.intern.getString("fullName");
			} catch (JSONException e) {
				return "";
			}
		}

		public String getCity() {
			try {
				return this.intern.getString("city");
			} catch (JSONException e) {
				return "";
			}
		}

		public String getZip() {
			try {
				return this.intern.getString("zipCode");
			} catch (JSONException e) {
				return "";
			}
		}

		public String getRegion() {
			try {
				return this.intern.getString("region");
			} catch (JSONException e) {
				return "";
			}
		}

		public String getCountry() {
			try {
				return this.intern.getString("country");
			} catch (JSONException e) {
				return "";
			}
		}
	}

	static class Species extends Item {

		private ItemArray<Bread> breads = null;

		protected Species(JSONObject json) {
			super(json);
		}

		public String toString() {
			try {
				return this.intern.getString("name");
			} catch (JSONException e) {
				return "";
			}
		}

		public boolean getUseTag() {
			try {
				return this.intern.getBoolean("useTag");
			} catch (JSONException e) {
				return false;
			}
		}

		public ItemArray<Bread> getBreads() {
			if (breads == null)
				try {
					breads = new ItemArray<Bread>(intern.getJSONArray("breads"));
				} catch (JSONException e) {
				}
			return breads;
		}
	}

	static class Bread extends Item {

		protected Bread(JSONObject json) {
			super(json);
		}

		public String toString() {
			try {
				return this.intern.getString("name");
			} catch (JSONException e) {
				return "";
			}
		}
	}

	static class Color extends Item {

		protected Color(JSONObject json) {
			super(json);
		}

		public String toString() {
			try {
				return this.intern.getString("name");
			} catch (JSONException e) {
				return "";
			}
		}
	}

	static class Animal extends Item {
		private Bitmap photo;
		private Marker marker = null;
		private GoogleMap map = null;
		private boolean releasing = false;

		protected Animal(JSONObject json) throws JSONException {
			super(json.getJSONObject("animal"));
			if (json.has("photo")) {
				byte[] content = Base64.decode(json.getString("photo"),
						Base64.DEFAULT);
				photo = BitmapFactory.decodeByteArray(content, 0,
						content.length);
			}
			;
		}

		public Bitmap getPhoto() {
			return photo;
		}

		public double getLat() {
			try {
				return this.intern.getJSONObject("lastAction").getDouble("lat");
			} catch (JSONException e) {
				return 0;
			}
		}

		public double getLng() {
			try {
				return this.intern.getJSONObject("lastAction").getDouble("lng");
			} catch (JSONException e) {
				return 0;
			}
		}

		public String getBread() {
			try {
				return this.intern.getJSONObject("bread").getString("name");
			} catch (JSONException e) {
				return "";
			}
		}

		public String getColor() {
			try {
				return this.intern.getJSONObject("color").getString("name");
			} catch (JSONException e) {
				return "";
			}
		}

		public String toString() {
			try {
				return this.intern.getString("boxID");
			} catch (JSONException e) {
				return "";
			}
		}

		public boolean isUsers() {
			String user = instance.user.getId();
			String team = "";
			try {
				team = intern.getJSONObject("lastAction").getJSONObject("team")
						.getString("__identity");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return user.compareTo(team) == 0;
		}

		public void setMapInfo(GoogleMap map, Marker marker) {
			this.map = map;
			this.marker = marker;
		}

		public boolean focus() {
			if (map == null || marker == null)
				return false;
			LatLng pos = marker.getPosition();
			if (pos.latitude == 0 && pos.longitude == 0)
				return false;
			marker.setIcon(BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			map.animateCamera(CameraUpdateFactory.newLatLng(pos));
			return true;
		}

		public void defocus() {
			if (map == null || marker == null)
				return;
			LatLng pos = marker.getPosition();
			if(releasing) return;
			if (pos.latitude == 0 && pos.longitude == 0)
				return;
			BitmapDescriptor green = BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
			BitmapDescriptor blue = BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
			marker.setIcon(isUsers() ? green : blue);
			return;
		}

		public String getAddress() {
			String address = "";
			JSONObject owner = null;
			try {
				owner = intern.getJSONObject("owner");
				if (owner != null) {
					String firstname = owner.getString("firstName");
					if (firstname.length() > 0)
						address = firstname + ' ';
					address += owner.getString("name");
					if (address.length() > 0)
						address += "\n";
					String street = owner.getString("street");
					String hnr = owner.getString("houseNumber");
					if (street.length() > 0)
						street += " " + hnr;
					else
						street = hnr;
					if (street.length() > 0)
						address += street + "\n";
					String city = owner.getString("city");
					String zipCode = owner.getString("zipCode");
					if (zipCode.length() > 0)
						city = zipCode + " " + city;
					if (city.length() > 0)
						address += city + "\n";
					String tel = owner.getString("phone");
					if (tel.length() > 0)
						address += "\n"
								+ instance.context.getResources().getString(
										R.string.phone_title) + " " + tel;
					String comment = owner.getString("comment");
					if (comment.length() > 0)
						address += "\n" + comment;
					if (address.trim().length() == 0)
						address = intern.getString("comment");
				}
				;
			} catch (JSONException e) {
				return "";
			}
			return address;
		}

		public boolean hasAddress() {
			try {
				JSONObject owner = intern.getJSONObject("owner");
				if (owner != null)
					return true;
			} catch (JSONException e) {
				return false;
			}
			return false;
		}

		private void remove() {
			if (marker != null)
				marker.remove();
		}

		public boolean hasGPS() {
			if (getLng() != 0)
				return true;
			if (getLat() != 0)
				return true;
			return false;
		}

		private boolean doRelease() {
			if (!releasing) {
				releasing = true;
				return true;
			}
			return false;
		}

		public boolean isReleased(){
			return releasing;
		}
	}

	static class ItemArray<T extends Item> {
		protected JSONArray intern;
		protected List<T> list;

		private ItemArray(JSONArray json) {
			this.intern = json;
		}

		public List<T> toList(Class<T> cls) {
			if (list != null)
				return list;
			list = new ArrayList<T>();
			for (int i = 0; i < intern.length(); i++) {
				try {
					JSONObject it = intern.getJSONObject(i);
					T item = cls.getDeclaredConstructor(JSONObject.class)
							.newInstance(it);
					list.add(item);
				} catch (JSONException e) {
					e.printStackTrace();
					continue;
				} catch (InstantiationException e) {
					e.printStackTrace();
					continue;
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					continue;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					continue;
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					continue;
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
					continue;
				}
			}
			return list;
		}

		public int getPosition(String id) {
			for (int i = 0; i < intern.length(); i++) {
				JSONObject row;
				String id2 = "";
				try {
					row = intern.getJSONObject(i);
					id2 = row.getString("__identity");
				} catch (JSONException e) {
					return 0;
				}
				if (id.compareTo(id2) == 0)
					return i;
			}
			return 0;
		}

		public T getObject(String id, Class<T> cls) {
			for (int i = 0; i < intern.length(); i++) {
				JSONObject row;
				String id2 = "";
				try {
					row = intern.getJSONObject(i);
					id2 = row.getString("__identity");
				} catch (JSONException e) {
					return null;
				}
				if (id.compareTo(id2) == 0) {
					T item;
					try {
						item = cls.getDeclaredConstructor(JSONObject.class)
								.newInstance(row);
					} catch (InstantiationException e) {
						return null;
					} catch (IllegalAccessException e) {
						return null;
					} catch (IllegalArgumentException e) {
						return null;
					} catch (InvocationTargetException e) {
						return null;
					} catch (NoSuchMethodException e) {
						return null;
					}
					return item;
				}
			}
			return null;
		}

	}

	static class AnimalList extends ItemArray<Animal> {
		protected List<Animal> listMy;
		protected JSONArray notReleased;

		private AnimalList(JSONArray json) {
			super(json);
			notReleased=json;
		}

		public List<Animal> toList(Class<Animal> cls) {
			return toList(true);
		}

		public List<Animal> toList(boolean all) {
			if (all && list != null)
				return list;
			if (!all && listMy != null)
				return listMy;
			list = new ArrayList<Animal>();
			listMy = new ArrayList<Animal>();
			for (int i = 0; i < intern.length(); i++) {
				try {
					JSONObject it = intern.getJSONObject(i);
					Animal item = new Animal(it);
					if (item.isUsers())
						listMy.add(item);
					list.add(item);
				} catch (JSONException e) {
					e.printStackTrace();
					continue;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					continue;
				}
			}
			return all ? list : listMy;
		}

		public int getPosition(String id, boolean all) {
			List<Animal> tmpList = toList(all);
			int i = 0;
			for (Animal animal : tmpList) {
				if (animal.getId().compareTo(id) == 0)
					return i;
				i++;
			};
			return -1;
		}

		private void remove(String id) {
			int pos = getPosition(id, false);
			int posall = getPosition(id, true);
			if (posall != -1)
				list.remove(posall);
			if (pos != -1)
				listMy.remove(pos);
		}

		private void remove(Animal animal) {
			String id = animal.getId();
			remove(id);
		}

		public Animal get(String animalid) {
			toList(true);
			for (Animal animal : list) {
				if (animal.getId().compareTo(animalid) == 0)
					return animal;
			}
			return null;
		}

		public ArrayList<String> getIds() {
			ArrayList<String> ids = new ArrayList<String>();
			toList(true);
			for (Animal animal : list) {
				ids.add(animal.getId());
			}
			return ids;
		}

		public String addReleased(String id) {
			JSONArray tmp = new JSONArray();
			for (int i = 0; i < notReleased.length(); i++) {
				try {
					JSONObject it = notReleased.getJSONObject(i);
					if(!it.getJSONObject("animal").getString("__identity").equals(id)) tmp.put(it);
				} catch (JSONException e) {
					e.printStackTrace();
					continue;
				}
			}
			notReleased=tmp;
			JSONObject json = new JSONObject();
			try {
				json.put("animals",notReleased);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return json.toString();
		}
	}

	public static final int ERROR_NOCONNECT = -1;

	static private WebConnecter instance = null;
	private SharedPreferences preferences;
	private User user = null;
	private String username;
	private String pw;
	private ItemArray<Location> locations;
	private ItemArray<Species> species;
	private ItemArray<Color> colors;
	private AnimalList animals;
	private DatabaseHandler db;
	private Location lastLocation;
	private ListView list;
	private ListView listall;
	private Location curLocation;
	private Context context;

	private NetworkTask nettask = null;

	private String[] lastOwner = new String[13];
	private boolean hasLastOwner = false;

	private DatabaseHandler.LoginData loginData;

	public static AbstractActivity currentActivity = null;

	private WebConnecter(SharedPreferences preferences) {
		this.preferences = preferences;
	}

	static public WebConnecter get() {
		return instance;
	}

	static public void login(Context context, String name, String pw)
			throws IOException, JSONException, WebConnecterException {
		instance = new WebConnecter(
				PreferenceManager.getDefaultSharedPreferences(context));
		stopNetwork();
		instance.db = new DatabaseHandler(context);
		instance.context = context;

		instance.username = name;
		instance.pw = pw;
		JSONObject json = instance.connect("info");
		instance.user = new User(json.getJSONObject("user"));
		instance.locations = new ItemArray<Location>(
				json.getJSONArray("locations"));
		instance.species = new ItemArray<Species>(json.getJSONArray("species"));
		instance.colors = new ItemArray<Color>(json.getJSONArray("colors"));
		instance.loginData = instance.db.addLogin(instance.getUrl("info")
				.toString(), name, pw, json.toString());
		instance.user.setLastID(instance.user.lastID
				+ instance.db.getAnimalCount());
	}

	static public void loginOffline(Context context, String name, String pw)
			throws IOException, JSONException, WebConnecterException {
		instance = new WebConnecter(
				PreferenceManager.getDefaultSharedPreferences(context));
		stopNetwork();
		instance.db = new DatabaseHandler(context);
		instance.context = context;

		instance.username = name;
		instance.pw = pw;
		instance.loginData = instance.db.getLoginData(instance.getUrl("info")
				.toString(), name, pw);
		if (instance.loginData == null)
			throw new WebConnecterException();
		JSONObject json = new JSONObject(instance.loginData.getJSON());
		instance.user = new User(json.getJSONObject("user"));
		instance.locations = new ItemArray<Location>(
				json.getJSONArray("locations"));
		instance.species = new ItemArray<Species>(json.getJSONArray("species"));
		instance.colors = new ItemArray<Color>(json.getJSONArray("colors"));
		instance.user.setLastID(instance.loginData.getLastID());
	}

	static public AnimalList getAnimals(boolean force) throws IOException,
			JSONException, WebConnecterException {
		if (force)
			instance.animals = null;
		getAnimals();
		instance.loginData.setLastReleased(instance.animals.getIds());
		for(Record rec :instance.db.getAllRelease()) {
			String id = rec.getAnimalId();
			instance.animals.get(id).doRelease();
		}
		return instance.animals;
	}

	static public AnimalList getAnimals() throws IOException, JSONException,
			WebConnecterException {
		if (instance.animals == null) {
			JSONObject json = instance.connect("animals/"
					+ instance.lastLocation.getId());
			instance.loginData = instance.db.changeLoginLastAnimals(
					instance.loginData, instance.lastLocation.getId(),
					json.toString());
			instance.animals = new AnimalList(json.getJSONArray("animals"));
		}
		return instance.animals;
	}

	static public AnimalList getAnimalsOffline() throws IOException,
			JSONException, WebConnecterException {
		if (instance.animals == null) {
			JSONObject json = new JSONObject(
					instance.loginData.getLastAnimals(instance.lastLocation
							.getId()));
			instance.animals = new AnimalList(json.getJSONArray("animals"));
			ArrayList<String> ids = instance.loginData.getLastReleased(instance.lastLocation.getId());
			for(Record rec :instance.db.getAllRelease()) {
				String id = rec.getAnimalId();
				if(!ids.contains(id)){
					instance.animals.remove(id);
				} else {
					instance.animals.get(id).doRelease();
				}
			}

		}
		return instance.animals;
	}

	static public User getUser() {
		return instance.user;
	}

	static public ItemArray<Location> getLocations() {
		return instance.locations;
	}

	private URL getUrl(String part) throws MalformedURLException {
		String s = preferences.getString("pref_api_url",
				"https://database.emfodo.org/") + "app/" + part + "/";
		URL url = new URL(s);
		return url;
	}

	private JSONObject connect(String part) throws IOException, JSONException,
			WebConnecterException {
		return connect(part, "GET");
	}

	private JSONObject connect(String part, String method) throws IOException,
			JSONException, WebConnecterException {
		HttpURLConnection conn = (HttpURLConnection) this.getUrl(part)
				.openConnection();
		conn.setRequestMethod(method);
		conn.setRequestProperty("Content-Type",
				"application/json;charset=utf-8");
		conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		String userpass = this.username + ":" + this.pw;
		String basicAuth = "Basic "
				+ Base64.encodeToString(userpass.getBytes(), Base64.DEFAULT);
		conn.setRequestProperty("Authorization", basicAuth);
		conn.setRequestProperty("Content-Type",
				"application/json;charset=utf-8");
		conn.setRequestProperty("Accept", "application/json");

		conn.setConnectTimeout(300000);
		conn.setReadTimeout(300000);
		conn.connect();
		int code = conn.getResponseCode();
		if (code == 401 || code == 404)
			throw new WebConnecterException();
		if (!(code == 200 || code == 204))
			throw new IOException();
		if (code == 204)
			return null;
		JSONObject output = new JSONObject(IOUtils.toString(conn
				.getInputStream()));
		if (output.has("error"))
			throw new WebConnecterException();
		conn.disconnect();
		return output;
	}

	private boolean send(JSONObject data, byte[] photo) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) this.getUrl("add").openConnection();
		} catch (Exception e1) {
			return false;
		}
		String userpass = this.username + ":" + this.pw;
		String basicAuth = "Basic "
				+ Base64.encodeToString(userpass.getBytes(), Base64.DEFAULT);
		conn.setRequestProperty("Authorization", basicAuth);
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		try {
			conn.setRequestMethod("POST");
		} catch (ProtocolException e) {
		}
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setConnectTimeout(300000);
		conn.setReadTimeout(300000);
		ByteArrayBody bin = new ByteArrayBody(photo,
				ContentType.create("image/png"), "photo.png");
		StringBody action;
		HttpEntity reqEntity = null;
		try {
			action = new StringBody(data.getString("action"),
					ContentType.APPLICATION_JSON);
			StringBody animal = new StringBody(data.getString("animal"),
					ContentType.APPLICATION_JSON);
			StringBody owner = new StringBody(data.getString("owner"),
					ContentType.APPLICATION_JSON);
			reqEntity = MultipartEntityBuilder.create().addPart("photo", bin)
					.addPart("action", action).addPart("animal", animal)
					.addPart("owner", owner).build();
		} catch (JSONException e1) {
		}
		conn.addRequestProperty("Content-length", reqEntity.getContentLength()
				+ "");
		conn.addRequestProperty(reqEntity.getContentType().getName(), reqEntity
				.getContentType().getValue());
		try {
			conn.connect();
			OutputStream os = conn.getOutputStream();
			reqEntity.writeTo(os);
			os.flush();
			os.close();
			int code = conn.getResponseCode();
			String out = IOUtils.toString(conn.getInputStream());
			JSONObject output = new JSONObject(out);
			int id = output.getInt("lastID");
			if (id > instance.user.lastID)
				instance.user.setLastID(output.getInt("lastID"));
			conn.disconnect();
			if (!(code == 201 || code == 200))
				return false;
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private boolean sendRelease(final Animal animal) {
		try {
			connect("release/" + animal.getId(), "DELETE");
		} catch (Exception e) {
			return false;
		}
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				animal.remove();
				animals.remove(animal);
				((AnimalListAdapter) list.getAdapter()).notifyDataSetChanged();
				((AnimalListAdapter) listall.getAdapter())
						.notifyDataSetChanged();
			}
		});
		return true;
	}
	
	private boolean sendRelease(String animalId) {
		try {
			connect("release/" + animalId, "DELETE");
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	

	public static void setList(ListView list, boolean all) {
		if (all)
			instance.listall = list;
		else
			instance.list = list;
	};

	public static void release(Animal animal) {
		if (animal.doRelease()) {
			instance.db.releaseAnimals(animal.getId(), instance.loginData);
			doNetwork();
		}
	}

	public static void doNetwork() {
		if (isLoggedIn()) {
			if (currentActivity != null && instance.db != null) {
				long count = instance.db.getAnimalCount();
				long countr = instance.db.getReleaseCount();
				currentActivity.setIcon((int) (count + countr));
			}
			;
			if (instance.nettask == null
					|| instance.nettask.getStatus() == AsyncTask.Status.FINISHED) {
				instance.nettask = instance.new NetworkTask();
				instance.nettask.execute();
			}
		}
	}
	
	public static void stopNetwork() {
		if(instance.nettask != null) instance.nettask.cancel(false);
	}

	public static boolean isLoggedIn() {
		if (instance != null && instance.user != null) {
			return true;
		}
		return false;
	}

	public static void logout() {
		instance = null;
	}

	public static void setDefaultLocation(Location loc) {
		Editor editor = instance.preferences.edit();
		editor.putString("last_location", loc.getId());
		instance.lastLocation = loc;
		editor.commit();
	}

	public static int getDefaultLocationPos() {
		String id = instance.preferences.getString("last_location", "");
		if (id == "")
			return 0;
		return instance.locations.getPosition(id);
	}

	public static Location getCurrentLocation() {
		if (instance.curLocation == null) {
			String id = instance.preferences.getString("last_location", "");
			if (id == "")
				return null;
			instance.curLocation = instance.locations.getObject(id,
					Location.class);
		}
		;
		return instance.curLocation;
	}

	public static ItemArray<Species> getSpecies() {
		return instance.species;
	}

	public static ItemArray<Color> getColors() {
		return instance.colors;
	}

	public static void setDefaultSpecies(Species sp) {
		Editor editor = instance.preferences.edit();
		editor.putString("last_species", sp.getId());
		editor.commit();
	}

	public static int getDefaultSpeciesPos() {
		String id = instance.preferences.getString("last_species", "");
		if (id == "")
			return 0;
		return instance.species.getPosition(id);
	}

	public static void add(ArrayList<View> list) {
		JSONObject data = new JSONObject();
		ByteArrayOutputStream photo = new ByteArrayOutputStream();
		Map<String, Integer> widgets = new HashMap<String, Integer>();
		widgets.put("android.widget.TextView", 1);
		widgets.put("android.widget.EditText", 2);
		widgets.put("android.widget.CheckBox", 3);
		widgets.put("com.dligo.animaltool.NoDefaultSpinner", 4);
		widgets.put("android.widget.Spinner", 4);
		widgets.put("android.widget.ImageView", 5);
		try {
			for (View view : list) {
				String[] s = view.getTag().toString().split(":");
				String model = s[0].trim();
				String field = s[1].trim();
				Object value = null;
				int id = widgets.get(view.getClass().getName());
				switch (id) {
				case 1:
					TextView tv = (TextView) view;
					value = tv.getText();
					break;
				case 2:
					EditText ev = (EditText) view;
					value = ev.getText();
					break;
				case 3:
					CheckBox cv = (CheckBox) view;
					value = (Boolean) cv.isChecked() ? true : false;
					break;
				case 4:
					Spinner sv = (Spinner) view;
					value = ((Item) sv.getSelectedItem()).getId();
					break;
				case 5:
					ImageView iv = (ImageView) view;
					Drawable drawable = iv.getDrawable();
					if (drawable instanceof BitmapDrawable) {
						Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, photo);
					}
					break;
				default:
					break;
				}
				if (value != null) {
					if (!data.has(model))
						data.put(model, new JSONObject());
					JSONObject mdata = data.getJSONObject(model);
					mdata.put(field, value);
				}
			}
			JSONObject action = data.getJSONObject("action");
			action.put("team", instance.user.getId());
			action.put("location", instance.lastLocation.getId());
			JSONObject animal = data.getJSONObject("animal");
			animal.put("location", instance.lastLocation.getId());
			instance.user.setLastID(instance.user.lastID + 1);
		} catch (JSONException e) {
		}
		instance.db.addAnimals(data, photo);
		doNetwork();
	}

	public static void setLastOwner(String value, int index) {
		instance.lastOwner[index] = value;
		instance.hasLastOwner = true;
	}

	public static String getLastOwner(int index) {
		return instance.lastOwner[index];
	}

	public static boolean getHasLastOwner() {
		return instance.hasLastOwner;
	}

	public class NetworkTask extends AsyncTask<Void, Long, Void> {

		protected Void doInBackground(Void... params) {
			long count = db.getAnimalCount();
			long countr = db.getReleaseCount();
			publishProgress(count + countr);
			if(instance==null) return null;
			while (count > 0) {
				if(isCancelled())return null;
				Record rec = db.getAnimal();
				if (rec == null) {
					count = 0;
					publishProgress(count + countr);
					break;
				}
				count = db.getAnimalCount();
				publishProgress(count);
				int id = rec.getId();
				JSONObject data = null;
				try {
					data = new JSONObject(rec.getData());
				} catch (JSONException e) {
				}
				byte[] photo = rec.getPhoto();
				if(instance==null) return null;
				boolean r = send(data, photo);
				if (r) {
					db.deleteAnimal(id);
					count = db.getAnimalCount();
					publishProgress(count + countr);
				}
			};
			count = db.getReleaseCount();
			publishProgress(count);
			if (instance.animals != null) {
				while (countr > 0) {
					if(isCancelled())return null;
					Record rec = db.getRelease();
					if (rec == null) {
						countr = 0;
						publishProgress(countr);
						break;
					}
					int id = rec.getId();
					String animalid = rec.getAnimalId();
					Animal animal = instance.animals.get(animalid);
					if (instance.sendRelease(animal)) {
						db.deleteRelease(id);
						String json = instance.animals.addReleased(animalid);
						instance.db.changeLoginLastAnimals(instance.loginData, instance.lastLocation.getId(), json);
					}
					count = db.getReleaseCount();
					publishProgress(count);
				}
			} else { 
				while (countr > 0) {
					if(isCancelled())return null;
					Record rec = db.getRelease();
					if (rec == null) {
						countr = 0;
						publishProgress(countr);
						break;
					}
					int id = rec.getId();
					String animalid = rec.getAnimalId();
					if (instance.sendRelease(animalid)) {
						db.deleteRelease(id);
					}
					count = db.getReleaseCount();
					publishProgress(count);
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Long... value) {
			super.onProgressUpdate(value);
			if (WebConnecter.currentActivity != null)
				WebConnecter.currentActivity.setIcon(value[0].intValue());
		}
	}

}

class WebConnecterException extends Exception {

	private static final long serialVersionUID = -1;

}