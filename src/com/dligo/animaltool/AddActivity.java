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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class AddActivity extends AbstractActivity {

	private static final int ACT_CAMERA = 2;
	static final int DLG_DATE = 3;
	private ImageView photo;
	private View rootView;
	private Boolean photo_taken=false;

	private EditText date;

	public GPS gps;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		photo_taken=false;
		setContentView(R.layout.activity_add);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		Context context = getApplicationContext();
		gps=GPS.init(context);
    	WebConnecter.doNetwork();
	}

	public class GPSListener implements LocationListener {
		public TextView lat = null;
		public TextView lng = null;

		@Override
		public void onLocationChanged(Location loc) {
			if (lat != null)
				lat.setText(loc.getLatitude() + "");
			if (lng != null)
				lng.setText(loc.getLongitude() + "");
		}

		@Override
		public void onProviderDisabled(String arg0) {
		}

		@Override
		public void onProviderEnabled(String arg0) {
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		}
	}	

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			final AddActivity addActivity = ((AddActivity) getActivity());
			addActivity.rootView = inflater.inflate(R.layout.fragment_add,
					container, false);
			final TextView tagLabel=(TextView) addActivity.rootView.findViewById(R.id.eartag_title);
			final EditText tag=(EditText) addActivity.rootView.findViewById(R.id.eartag);
			Spinner species = (Spinner) addActivity.rootView
					.findViewById(R.id.sel_species);
			List<WebConnecter.Species> lists = WebConnecter.getSpecies()
					.toList(WebConnecter.Species.class);
			ArrayAdapter<WebConnecter.Species> dataAdapter = new ArrayAdapter<WebConnecter.Species>(
					addActivity, android.R.layout.simple_spinner_item, lists);
			dataAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			species.setAdapter(dataAdapter);
			//species.setSelection(WebConnecter.getDefaultSpeciesPos());
			species.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long i) {
					WebConnecter.Species item = (WebConnecter.Species) parent
							.getItemAtPosition(position);
					WebConnecter.setDefaultSpecies(item);
					Spinner breads = (Spinner) addActivity.rootView
							.findViewById(R.id.sel_bread);
					List<WebConnecter.Bread> listb = item.getBreads().toList(
							WebConnecter.Bread.class);
					ArrayAdapter<WebConnecter.Bread> dataAdapter = new ArrayAdapter<WebConnecter.Bread>(
							getActivity(),
							android.R.layout.simple_spinner_item, listb);
					dataAdapter
							.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					breads.setAdapter(dataAdapter);
					if(item.getUseTag()){
						tagLabel.setVisibility(View.VISIBLE);
						tag.setVisibility(View.VISIBLE);
					} else {
						tagLabel.setVisibility(View.INVISIBLE);
						tag.setVisibility(View.INVISIBLE);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
			
			Spinner colors = (Spinner) addActivity.rootView
					.findViewById(R.id.sel_color);
			List<WebConnecter.Color> listc = WebConnecter.getColors().toList(WebConnecter.Color.class);
			ArrayAdapter<WebConnecter.Color> dataAdapterC = new ArrayAdapter<WebConnecter.Color>(
					getActivity(),
					android.R.layout.simple_spinner_item, listc);
			dataAdapterC
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			colors.setAdapter(dataAdapterC);
			Spinner genders = (Spinner) addActivity.rootView
					.findViewById(R.id.gender);
			List<WebConnecter.Gender> listg = new ArrayList<WebConnecter.Gender>();
			listg.add(new WebConnecter.Gender("",""));
			listg.add(new WebConnecter.Gender("F",addActivity.getResources().getString(R.string.gender_f)));
			listg.add(new WebConnecter.Gender("M",addActivity.getResources().getString(R.string.gender_m)));
			ArrayAdapter<WebConnecter.Gender> dataAdapterG = new ArrayAdapter<WebConnecter.Gender>(
					getActivity(),
					android.R.layout.simple_spinner_item, listg);
			dataAdapterG
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			genders.setAdapter(dataAdapterG);
			TextView boxid = (TextView) addActivity.rootView
					.findViewById(R.id.box_id);
			boxid.setText(WebConnecter.getUser().getNextBoxID());
			final View owner = (View) addActivity.rootView
					.findViewById(R.id.owner);
			CheckBox isprivate = (CheckBox) addActivity.rootView
					.findViewById(R.id.chk_private);
			isprivate.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					owner.setVisibility(isChecked ? View.VISIBLE
							: View.INVISIBLE);
				}
			});
			final TextView lat = (TextView) addActivity.rootView
					.findViewById(R.id.gps_lat);
			final TextView lng = (TextView) addActivity.rootView
					.findViewById(R.id.gps_lng);
			final ImageView gpsonline = (ImageView) addActivity.rootView
					.findViewById(R.id.gps_online);
			final TextView gpserror = (TextView) addActivity.rootView
					.findViewById(R.id.gps_error);	
			
			final Drawable gps_ok = getResources().getDrawable(R.drawable.ic_gps_ok);
			final Drawable gps_bad = getResources().getDrawable(R.drawable.ic_gps_bad);
			addActivity.gps.setChangeListener(new GPS.OnChangeListener(){
				@Override
				public void changed(Location location) {
					if (lat != null)
						lat.setText(location.getLatitude() + "");
					if (lng != null)
						lng.setText(location.getLongitude() + "");
					if(lat!=null && lng!=null) {
						gpsonline.setImageDrawable(gps_ok);
						gpserror.setVisibility(View.GONE);
					} else {
						gpsonline.setImageDrawable(gps_bad);
					}
				}
			});
			addActivity.photo = (ImageView) addActivity.rootView
					.findViewById(R.id.photo);
			addActivity.photo.setFocusable(true);
			addActivity.photo.setFocusableInTouchMode(true);
			addActivity.photo.requestFocus();
			addActivity.photo.requestFocusFromTouch();
			Animation animPhoto = AnimationUtils.loadAnimation(addActivity,
	                R.anim.photo_fade);
			addActivity.photo.startAnimation(animPhoto);
			addActivity.photo.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent cameraIntent = new Intent(
							android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					addActivity
							.startActivityForResult(cameraIntent, ACT_CAMERA);
					addActivity.photo.clearAnimation();
				}
			});
			addActivity.date = (EditText) addActivity.rootView
					.findViewById(R.id.birthday);
			addActivity.date.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					DialogFragment newFragment = new DatePickerFragment();
				    newFragment.show(addActivity.getFragmentManager(), "datePicker");
				}
			});
			addActivity.date.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
				    if(hasFocus){
						DialogFragment newFragment = new DatePickerFragment();
					    newFragment.show(addActivity.getFragmentManager(), "datePicker");
				    }else {
				    }
				   }
				});
			
			EditText zip=(EditText) addActivity.rootView.findViewById(R.id.zip);
			zip.setText(WebConnecter.getCurrentLocation().getZip());
			EditText city=(EditText) addActivity.rootView.findViewById(R.id.city);
			city.setText(WebConnecter.getCurrentLocation().getCity());
			EditText region=(EditText) addActivity.rootView.findViewById(R.id.region);
			region.setText(WebConnecter.getCurrentLocation().getRegion());
			EditText country=(EditText) addActivity.rootView.findViewById(R.id.country);
			country.setText(WebConnecter.getCurrentLocation().getCountry());
			
			Button fill=(Button) addActivity.rootView.findViewById(R.id.fillast);
			if(WebConnecter.getHasLastOwner()){
				fill.setVisibility(View.VISIBLE);
			} else {
				fill.setVisibility(View.GONE);				
			}
			return addActivity.rootView;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACT_CAMERA && resultCode == RESULT_OK) {
			Bitmap image = (Bitmap) data.getExtras().get("data");
			photo.getDrawable().mutate();
			photo_taken=true;
			photo.setImageBitmap(image);
		}
	}

	public void clickAdd(View view) {
		boolean errors=false;
        String s=date.getText().toString();
        DateFormat formatter=DateFormat.getDateInstance(DateFormat.SHORT);
    	long tstamp = 0;
        if (s!=null && !s.trim().isEmpty()){
        	try {
        		tstamp=(long) Math.ceil(formatter.parse(s).getTime()/1000+86400);
        	} catch (ParseException e) {
        		date.setError(getString(R.string.date_error));
        		errors=true;
	  	  	}
        } else date.setError(null);
        TextView lat=(TextView) findViewById(R.id.gps_lat);
        TextView lng=(TextView) findViewById(R.id.gps_lng);
		ImageView gpsonline = (ImageView) findViewById(R.id.gps_online);
		TextView gpserror = (TextView) findViewById(R.id.gps_error);	
        if(lat.getText().length()==0 || lng.getText().length()==0){
        	/* lat.setError(getString(R.string.gps_error));
        	lat.setText(getString(R.string.gps_error));
        	lng.setError(getString(R.string.gps_error));*/
			gpsonline.setImageDrawable(getResources().getDrawable(R.drawable.ic_gps_bad));
			gpserror.setVisibility(View.VISIBLE);
			gpserror.setError(getString(R.string.gps_error));
    		errors=true;
        } else {
        	/*lat.setError(null);
        	lng.setError(null);*/
			gpsonline.setImageDrawable(getResources().getDrawable(R.drawable.ic_gps_ok));
			gpserror.setVisibility(View.GONE);
			gpserror.setError(null);
        }
        TextView pherr=(TextView) findViewById(R.id.photo_error);
        if(!(photo.getDrawable() instanceof BitmapDrawable && AddActivity.this.photo_taken)) {
            pherr.setError(getString(R.string.photo_error));
            pherr.setText(getString(R.string.photo_error));
    		errors=true;
        } else {
        	pherr.setError(null);
        	pherr.setText("");
        }
        CheckBox isprivate=(CheckBox) findViewById(R.id.chk_private);
        if(isprivate.isChecked()) {
        	int[] ids={R.id.firstname,R.id.lastname,R.id.idnumber,R.id.cnp,R.id.street,R.id.zip,R.id.housenumber,R.id.city,R.id.phone,R.id.region,R.id.country};
        	for (int id : ids) {
				EditText text=(EditText)findViewById(id);
				if(text.getText().toString().trim().length()==0){
					text.setError(getResources().getString(R.string.error_field_required));
					errors=true;
				}
			}
        }
        int[] spinners={R.id.sel_species,R.id.sel_bread,R.id.sel_color,R.id.gender};
        int[] spinners_title={R.id.species_title,R.id.bread_title,R.id.color_title,R.id.gender_title};
        for (int i=0;i<spinners.length;i++) {
			Spinner spinner=(Spinner) findViewById(spinners[i]);
			if(spinner.getSelectedItemPosition()<0){
				TextView title=(TextView) findViewById(spinners_title[i]);
				SpinnerAdapter ad=spinner.getAdapter();
				if(ad!=null) ((TextView) ad.getView(-1, null, null)).setError(getResources().getString(R.string.error_field_required));
				title.setError(getResources().getString(R.string.error_field_required));
				errors=true;
			} else {
				TextView title=(TextView) findViewById(spinners_title[i]);
				((TextView) spinner.getAdapter().getView(-1, null, null)).setError(null);
				title.setError(null);
			}
		}
        
        if(errors)return;
        int[] ownerdata={R.id.firstname,R.id.idnumber,R.id.cnp,R.id.phone,R.id.lastname,R.id.street,R.id.housenumber,R.id.zip,R.id.city,R.id.region,R.id.country,R.id.passId,R.id.ownercomment};
        for (int i=0;i<ownerdata.length;i++) {
			EditText oview=(EditText) findViewById(ownerdata[i]);
			WebConnecter.setLastOwner(oview.getText().toString(), i);
        }
        
    	date.setText(Long.toString(tstamp));
		ArrayList<View> lv = getAllTagedChildren(rootView);
		WebConnecter.add(lv);
		Context context = getApplicationContext();
		CharSequence text = getResources().getString(R.string.added);
		int duration = Toast.LENGTH_LONG;
		for (int i=0; i < 3; i++)
		{
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
		/*getFragmentManager().beginTransaction()
				.replace(R.id.container, new PlaceholderFragment()).commit();*/
		finish();
	}
	
	public void clickFill(View view){
		if(WebConnecter.getHasLastOwner()){
			int[] ownerdata={R.id.firstname,R.id.idnumber,R.id.cnp,R.id.phone,R.id.lastname,R.id.street,R.id.housenumber,R.id.zip,R.id.city,R.id.region,R.id.country,R.id.passId,R.id.ownercomment};
			for (int i=0;i<ownerdata.length;i++) {
				EditText oview=(EditText) findViewById(ownerdata[i]);
				oview.setText(WebConnecter.getLastOwner(i));
			}
		};
	}

	private ArrayList<View> getAllTagedChildren(View v) {

		if (!(v instanceof ViewGroup)) {
			ArrayList<View> viewArrayList = new ArrayList<View>();
			if (v.getTag() != null)
				viewArrayList.add(v);
			return viewArrayList;
		}

		ArrayList<View> result = new ArrayList<View>();

		ViewGroup vg = (ViewGroup) v;
		for (int i = 0; i < vg.getChildCount(); i++) {
			View child = vg.getChildAt(i);

			ArrayList<View> viewArrayList = new ArrayList<View>();
			if (v.getTag() != null)
				viewArrayList.add(v);
			viewArrayList.addAll(getAllTagedChildren(child));

			result.addAll(viewArrayList);
		}
		return result;
	}

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			DatePickerDialog dlg = new DatePickerDialog(getActivity(), this, year, month, day);
			Time now = new Time();
			now.setToNow();
			dlg.getDatePicker().setMaxDate(c.getTime().getTime());
			return dlg;
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			final AddActivity addActivity = ((AddActivity) getActivity());
			final Calendar c = Calendar.getInstance();
	        DateFormat formatter=DateFormat.getDateInstance(DateFormat.SHORT);
	        c.set(year, month, day);
	        String s=formatter.format(c.getTime());
	        addActivity.date.setText(s);
			
		}
	}
}