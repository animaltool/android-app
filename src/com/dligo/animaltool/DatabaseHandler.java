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
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 10;
 
    // Database Name
    private static final String DATABASE_NAME = "animalManager";
 
    // Contacts table name
    private static final String TABLE_ANIMALS = "animals";
    private static final String TABLE_RELEASE = "release";
    private static final String TABLE_LOGIN = "login";
 
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_DATA = "data";
    private static final String KEY_PHOTO = "PHOTO";
    private static final String[] COLS = {KEY_ID,KEY_DATA,KEY_PHOTO};

    private static final String KEY_ANIMAL = "ANIMAL";
    private static final String[] COLSR = {KEY_ID,KEY_ANIMAL};
    
    private static final String KEY_URL = "url";
    private static final String KEY_USER = "user";
    private static final String KEY_PASS = "pass";
    private static final String KEY_JSON = "json";
    private static final String KEY_LASTID = "lastid";
    private static final String KEY_LASTLOCATION = "lastlocation";
    private static final String KEY_LASTANIMALS = "lastanimals";
    private static final String KEY_LASTRELEASED = "lastreleased";
    private static final String[] COLSL = {KEY_ID,KEY_URL,KEY_USER,KEY_PASS,KEY_JSON,KEY_LASTID,KEY_LASTLOCATION,KEY_LASTANIMALS,KEY_LASTRELEASED};

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ANIMALS_TABLE = "CREATE TABLE " + TABLE_ANIMALS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DATA + " TEXT,"
                + KEY_PHOTO + " BLOB" + ")";
        db.execSQL(CREATE_ANIMALS_TABLE);
        String CREATE_RELEASE_TABLE = "CREATE TABLE " + TABLE_RELEASE + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_ANIMAL + " TEXT" + ")";
        db.execSQL(CREATE_RELEASE_TABLE);
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_URL + " TEXT," + KEY_USER + " TEXT,"
                + KEY_PASS + " TEXT," + KEY_JSON + " TEXT," + KEY_LASTID  + " INTEGER,"
                + KEY_LASTLOCATION + " TEXT,"+ KEY_LASTANIMALS + " TEXT,"
                + KEY_LASTRELEASED + " TEXT"+ ")";
        db.execSQL(CREATE_LOGIN_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANIMALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RELEASE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
        onCreate(db);
    }

    public void addAnimals(JSONObject data,ByteArrayOutputStream photo) {
        SQLiteDatabase db = this.getWritableDatabase();
     
        ContentValues values = new ContentValues();
        values.put(KEY_DATA, data.toString());
        values.put(KEY_PHOTO, photo.toByteArray()); 
     
        db.insert(TABLE_ANIMALS, null, values);
    }
    
    public Record getAnimal(){
    	Record rec=null;	
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor=db.query(TABLE_ANIMALS, COLS, null, null, null, null, null);
    	cursor.moveToFirst();
    	if(!cursor.isAfterLast()) {
    		rec=new Record(cursor.getInt(0),cursor.getString(1),cursor.getBlob(2));
    	}
    	cursor.close();
    	return rec;
    }
    
    public long getAnimalCount(){
    	SQLiteDatabase db = this.getReadableDatabase();
    	long count=DatabaseUtils.queryNumEntries(db, TABLE_ANIMALS);
    	return count;
    }
    
    public boolean deleteAnimal(int id) 
    {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean ok=db.delete(TABLE_ANIMALS, KEY_ID + "=" + id, null) > 0;
        return ok;
    }    

    class Record {
    	private int id;
    	private String data;
		private byte[] photo;
		private String animalid;
    	
    	public Record(int aid,String adata,byte[] aphoto){
    		id=aid;
    		data=adata;
    		photo=aphoto;
    	}
    	public Record(int aid,String aanimalid){
    		id=aid;
    		animalid=aanimalid;
    	}
    	
    	public int getId(){
    		return id;
    	}
    	public String getData() {
			return data;
		}
		public byte[] getPhoto() {
			return photo;
		}    	
    	public String getAnimalId(){
    		return animalid;
    	}
    }

	public void releaseAnimals(String id, LoginData ld) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(KEY_ANIMAL, id);
     
        db.insert(TABLE_RELEASE, null, values);
        
        if(ld!=null){
            ld.lastReleased.remove(id);
            values = new ContentValues();
            String s=new String();
            for(String r:ld.lastReleased){
            	if(!s.isEmpty())s+=",";
            	s+=r;
            }
            values.put(KEY_LASTRELEASED, s);
         
        	String[] params={String.valueOf(ld.id)};
            ld.id=db.update(TABLE_LOGIN, values,"id=?",params);
        }
	}

	public long getReleaseCount() {
    	SQLiteDatabase db = this.getReadableDatabase();
    	long count=DatabaseUtils.queryNumEntries(db, TABLE_RELEASE);
    	return count;
	}

	public Record getRelease() {
    	Record rec=null;	
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor=db.query(TABLE_RELEASE, COLSR, null, null, null, null, null);
    	cursor.moveToFirst();
    	if(!cursor.isAfterLast()) {
    		rec=new Record(cursor.getInt(0),cursor.getString(1));
    	}
    	cursor.close();
    	return rec;
	}

	public ArrayList<Record> getAllRelease() {
		ArrayList<Record> rec=new ArrayList<Record>();	
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor=db.query(TABLE_RELEASE, COLSR, null, null, null, null, null);
    	while(cursor.moveToNext()){
    		rec.add(new Record(cursor.getInt(0),cursor.getString(1)));
    	}
    	cursor.close();
    	return rec;
	}


	public  boolean deleteRelease(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean ok=db.delete(TABLE_RELEASE, KEY_ID + "=" + id, null) > 0;
        return ok;
	}

	class LoginData {
		private long id;
		private String json;
		private long lastID;
		private String lastLocation;
		private String lastAnimals;
		public ArrayList<String> lastReleased;
		public String getJSON(){
			return json;
		}
		public long getLastID(){
			return lastID;
		}
		public String getLastAnimals(String id){
			if(id.equals(lastLocation)){
				return lastAnimals;
			} else {
				return null;
			}
		}
		public ArrayList<String> getLastReleased(String id){
			if(id.equals(lastLocation)){
				return lastReleased;
			} else {
				return null;
			}
		}
		public void setLastReleased(ArrayList<String> ids){
			lastReleased=ids;
		}
	}
	
	public LoginData addLogin(String url, String user, String pass, String json) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOGIN,null, null);
        
        ContentValues values = new ContentValues();
        values.put(KEY_URL, url);
        values.put(KEY_USER, user);
        values.put(KEY_PASS, pass);
        values.put(KEY_JSON, json);

        LoginData ld=new LoginData();
        ld.id=db.insert(TABLE_LOGIN, null, values);
        ld.json=json;
        return ld;
	}
	
	public LoginData getLoginData(String url, String user, String pass){
        LoginData ld=null;
    	SQLiteDatabase db = this.getReadableDatabase();
    	String[] params={url,user,pass};
    	Cursor cursor=db.query(TABLE_LOGIN, COLSL, "url=? AND user=? AND pass=?", params, null, null, null);
    	cursor.moveToFirst();
    	if(!cursor.isAfterLast()) {
            ld=new LoginData();
    		ld.id=cursor.getInt(0);
    		ld.json=cursor.getString(4);
    		ld.lastID=cursor.getInt(5);
    		ld.lastLocation=cursor.getString(6);
    		ld.lastAnimals=cursor.getString(7);
    		String lr = cursor.getString(8);
    		if(lr==null){
    			ld.lastReleased=new ArrayList<String>();
    		} else {
    			ld.lastReleased=new ArrayList<String>(Arrays.asList(lr.split(",")));
    		}
    	}
    	cursor.close();
		return ld;
	}
	
	public LoginData changeLoginLastID(LoginData ld, long lastid) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(KEY_LASTID, lastid);
     
        ld.lastID=lastid;
    	String[] params={String.valueOf(ld.id)};
        ld.id=db.update(TABLE_LOGIN, values,"id=?",params);
        return ld;
	}

	public LoginData changeLoginLastAnimals(LoginData ld, String locationId, String animalsJson) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(KEY_LASTLOCATION, locationId);
        values.put(KEY_LASTANIMALS, animalsJson);
     
        ld.lastLocation=locationId;
        ld.lastAnimals=animalsJson;
    	String[] params={String.valueOf(ld.id)};
        ld.id=db.update(TABLE_LOGIN, values,"id=?",params);
        return ld;
	}
}