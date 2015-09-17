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
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONException;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.widget.ListView;

import com.dligo.animaltool.WebConnecter.Animal;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ReleaseActivity extends AbstractActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	private GoogleMap map = null;
	private GPS gps;
	private WebConnecter.AnimalList animals = null;
	private HashMap<Marker, Pair<Integer, WebConnecter.Animal>> markers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_release);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						int i=position==0?1:0;
						PlaceholderFragment fragment = mSectionsPagerAdapter.getFragment(i);
						if(fragment.active!=null){
							fragment.active.setBackgroundResource(0);
							fragment.active.clearAnimation();
							fragment.active=null;
						};
						if (fragment.activeAnimal != null){
							fragment.activeAnimal.defocus();
							fragment.activeAnimal=null;
						};
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

		try {
			animals = WebConnecter.getAnimals();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (WebConnecterException e) {
			e.printStackTrace();
		}

		gps = GPS.init(getApplicationContext());
		setupMap();
		WebConnecter.doNetwork();
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	protected void setupMap() {
		if (map == null) {
			map = ((MapFragment) getFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			map.setMyLocationEnabled(true);
		}
		LatLng loc = gps.getLatLng();
		if(loc==null)loc=new LatLng(0,0);
		BitmapDescriptor green = BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
		BitmapDescriptor blue = BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
		int i=0;
		int j=0;
		markers=new HashMap<Marker, Pair<Integer,Animal>>();
		for (WebConnecter.Animal animal : animals
				.toList(true)) {
			double lat=animal.getLat();
			double lng=animal.getLng();
			if(lat!=0 && lng!=0){
				Marker marker = map.addMarker(new MarkerOptions()
					.position(new LatLng(lat, lng))
					.title(animal.toString())
					.snippet(animal.getColor() + " " + animal.getBread())
					.icon(animal.isUsers() ? green : blue));
				animal.setMapInfo(map, marker);
				markers.put(marker, Pair.create(animal.isUsers()?i:j,animal));
			}
			j++;
			if(animal.isUsers())i++;
		}
		map.setOnMarkerClickListener(new OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker marker) {
				Pair<Integer, Animal> p = markers.get(marker);
				int position=p.first;
				WebConnecter.Animal animal=p.second;
				int page=animal.isUsers()?0:1;
				mViewPager.setCurrentItem(page, true);
				PlaceholderFragment fragment = ((SectionsPagerAdapter) mViewPager.getAdapter()).getFragment(page);
				View view = fragment.getListView().getAdapter().getView(position, null, null);
//				fragment.onListItemClick(null, fragment.getListView().getChildAt(position), position,0);
				fragment.onListItemClick(null, view, position,0);
				return false;
			}
		});
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14));
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		private SparseArray<PlaceholderFragment> fragments=new SparseArray<PlaceholderFragment>();;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			PlaceholderFragment fragment=PlaceholderFragment.newInstance(position + 1);
			fragments.append(position, fragment);
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
		
	   public PlaceholderFragment getFragment(int position){
		   return this.fragments.get(position);
	   }
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends ListFragment {
		private boolean all;
		private View active = null;
		private WebConnecter.Animal activeAnimal=null; 

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			fragment.setArguments(args);
			fragment.all = sectionNumber == 2;
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			try {
				setListAdapter(new AnimalListAdapter(getActivity(),
						WebConnecter.getAnimals().toList(all)));
			} catch (IOException e) {
			} catch (JSONException e) {
			} catch (WebConnecterException e) {
			}
			WebConnecter.setList(getListView(),all);
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			WebConnecter.Animal animal=(WebConnecter.Animal) getListAdapter().getItem(position);
			if (activeAnimal != null)
				activeAnimal.defocus();
			if(!animal.focus()){
				AlertDialog.Builder dlgAlert = new AlertDialog.Builder(getActivity());

				dlgAlert.setMessage(animal.getAddress());
				dlgAlert.setTitle(animal.toString());
				dlgAlert.setPositiveButton(getResources().getString(R.string.ok), null);
				dlgAlert.setCancelable(true);
				dlgAlert.create().show();
			};
			if (active != null){
				active.setBackgroundResource(0);
				active.clearAnimation();
			};
			ValueAnimator colorAnim = ObjectAnimator.ofInt(v,
					"backgroundColor",
					getResources().getColor(android.R.color.background_light),
					Color.argb(255, 255, 255, 255));
			colorAnim.setDuration(500);
			colorAnim.setEvaluator(new ArgbEvaluator());
			colorAnim.start();
			active=v;
			activeAnimal=animal;
		}
	}
}
