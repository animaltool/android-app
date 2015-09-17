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

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends AbstractActivity {
	
	private final int ACT_LOGIN=1;
	private Spinner locSp;
	private View mainView;
	private View progressView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
    	WebConnecter.doNetwork();
		if(!WebConnecter.isLoggedIn()){
	        Intent intent = new Intent(this,LoginActivity.class);
			startActivityForResult(intent, ACT_LOGIN);			
		};
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
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			((MainActivity) getActivity()).locSp = (Spinner) rootView.findViewById(R.id.sel_location);
			if(WebConnecter.isLoggedIn()){
				((MainActivity) getActivity()).fillLocations();
			};
			return rootView;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    WebConnecter.currentActivity=this;
    	WebConnecter.doNetwork();
	    if (requestCode == ACT_LOGIN) {
	        if (resultCode == RESULT_OK) {
	        	fillLocations();
	        } else if(resultCode == RESULT_CANCELED) {
	        	WebConnecter.doNetwork();
	        	finish();
	        }
	    }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	    WebConnecter.currentActivity=this;
    	WebConnecter.doNetwork();
	}
	
    public void showAdd(View view){
    	WebConnecter.doNetwork();
		Intent add_intent = new Intent(MainActivity.this,AddActivity.class);
		startActivity(add_intent);			
	}

	public void showRelease(View view){
    	WebConnecter.stopNetwork();
		mainView=findViewById(R.id.maininner);		
		progressView=findViewById(R.id.progress);		
		showProgress(true);
		GetReleasingTask webTask = new GetReleasingTask();
		webTask.execute((Void) null);
	}	
	
	private void fillLocations(){
    	List<WebConnecter.Location> list = WebConnecter.getLocations().toList(WebConnecter.Location.class);
    	ArrayAdapter<WebConnecter.Location> dataAdapter = new ArrayAdapter<WebConnecter.Location>(this,
    		android.R.layout.simple_spinner_item, list);
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	locSp.setAdapter(dataAdapter);
    	locSp.setSelection(WebConnecter.getDefaultLocationPos());
    	locSp.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long i) {
            	WebConnecter.setDefaultLocation((WebConnecter.Location) parent.getItemAtPosition(position));	
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });		
	}
	
	public class GetReleasingTask extends AsyncTask<Void, Void, Integer> {
		private WebConnecter.ItemArray<WebConnecter.Animal> animals;

	     protected Integer doInBackground(Void... params) {

			try {
				animals=WebConnecter.getAnimals(true);
			} catch (IOException e) {
				return WebConnecter.ERROR_NOCONNECT;
			} catch (JSONException e) {
				return WebConnecter.ERROR_NOCONNECT;
           } catch (WebConnecterException e) {
           	return -2;
			};			

			return animals.toList(WebConnecter.Animal.class).size();
		}

		@Override
		protected void onPostExecute(final Integer result) {
			showProgress(false);

			if (result==0) {
				AlertDialog.Builder dlgAlert = new AlertDialog.Builder(
						MainActivity.this);

				dlgAlert.setMessage(R.string.error_noanimals);
				dlgAlert.setTitle(R.string.error_dialog);
				dlgAlert.setPositiveButton(getResources().getString(R.string.ok), null);
				dlgAlert.setCancelable(true);
				dlgAlert.create().show();
			} else if(result==WebConnecter.ERROR_NOCONNECT){
				try {
					WebConnecter.getAnimalsOffline();
					AlertDialog.Builder dlgAlert = new AlertDialog.Builder(
							MainActivity.this);

					dlgAlert.setMessage(R.string.error_noconnect_offline);
					dlgAlert.setTitle(R.string.error_dialog);
					dlgAlert.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							goToRelease();
						}
					});
					dlgAlert.setNegativeButton(getResources().getString(R.string.no), null);
					dlgAlert.setCancelable(true);
					dlgAlert.create().show();

				}catch (Exception e) {
					AlertDialog.Builder dlgAlert = new AlertDialog.Builder(
							MainActivity.this);

					dlgAlert.setMessage(R.string.error_noconnect);
					dlgAlert.setTitle(R.string.error_dialog);
					dlgAlert.setPositiveButton(getResources().getString(R.string.ok), null);
					dlgAlert.setCancelable(true);
					dlgAlert.create().show();
				};
			} else {
				goToRelease();
			}
		}

		@Override
		protected void onCancelled() {
			showProgress(false);
		}
	}

	private void showProgress(final boolean show) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			progressView.setVisibility(View.VISIBLE);
			progressView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							progressView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mainView.setVisibility(View.VISIBLE);
			mainView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mainView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			progressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mainView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	public void goToRelease(){
		Intent release_intent = new Intent(MainActivity.this,ReleaseActivity.class);
		startActivity(release_intent);			
	}
	
	public void showLogin(View view){
		WebConnecter.logout();
        Intent intent = new Intent(this,LoginActivity.class);
		startActivityForResult(intent, ACT_LOGIN);			
	}
}
