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

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dligo.animaltool.WebConnecter.Animal;

public class AnimalListAdapter extends ArrayAdapter<WebConnecter.Animal> {
	private final Activity context;
	private final List<Animal> items;
	private int distance;
	
	static class ViewHolder {
	    protected TextView label;
	    protected TextView appearence;
	    protected ImageView photo;
	    protected Button button;
	    protected ImageButton address;
	}
	  
	public AnimalListAdapter(Activity context, List<Animal> items) {
		super(context, R.layout.animal_list_item, items);
		SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(context);
		distance=Integer.valueOf(preferences.getString("pref_distance", "200"));
		this.context=context;
		this.items=items;
	}
	
	  @Override
	  public View getView(int position, View itemView, ViewGroup parent) {
	    View view = null;
	    if (itemView == null) {
	      LayoutInflater inflator = context.getLayoutInflater();
	      view = inflator.inflate(R.layout.animal_list_item, null);
	      final ViewHolder viewHolder = new ViewHolder();
	      viewHolder.label = (TextView) view.findViewById(R.id.list_label);
	      viewHolder.appearence = (TextView) view.findViewById(R.id.list_appearence);
	      viewHolder.photo = (ImageView) view.findViewById(R.id.list_photo);
	      viewHolder.button = (Button) view.findViewById(R.id.list_button);
	      viewHolder.address = (ImageButton) view.findViewById(R.id.list_btn_address);
	      view.setTag(viewHolder);
	    } else {
	      view = itemView;
	    }
	    final ViewHolder holder = (ViewHolder) view.getTag();
	    final Animal item=items.get(position);
	    holder.label.setText(item.toString());
	    holder.appearence.setText(item.getColor()+" "+item.getBread());
	    holder.photo.setImageBitmap(item.getPhoto());
		holder.button.setEnabled(true);
		if(item.isReleased()) holder.button.setEnabled(false);
	    RelativeLayout.LayoutParams lp=(RelativeLayout.LayoutParams) holder.button.getLayoutParams();
	    if(item.hasAddress()){
	    	holder.address.setVisibility(View.VISIBLE);
	    	lp.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	    	lp.addRule(RelativeLayout.LEFT_OF, R.id.list_btn_address);
	    	if(!item.hasGPS()) {
	    		holder.address.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_address_nogps));
	    	} else {
	    		holder.address.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_address));
	    	}
	    } else {
	    	lp.removeRule(RelativeLayout.LEFT_OF);
	    	lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	    	holder.address.setVisibility(View.GONE);
	    };
	    holder.button.setLayoutParams(lp);
	    holder.button.setOnClickListener(null);
	    holder.button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				double lat = item.getLat();
				double lng = item.getLng();
				if(lat!=0 || lng!=0){
					int dist = GPS.get().getDistance(lat,lng);
					if(dist>distance){
						AlertDialog.Builder dlgAlert = new AlertDialog.Builder(context);
						dlgAlert.setTitle(context.getResources().getString(R.string.warning));
						dlgAlert.setMessage(String.format(context.getResources().getString(R.string.release_distance),dist,distance));
						dlgAlert.setPositiveButton(context.getResources().getString(R.string.ok), null);
						dlgAlert.setCancelable(true);
						dlgAlert.create().show();
					} else {
						AlertDialog.Builder dlgAlert = new AlertDialog.Builder(context);
						dlgAlert.setTitle(context.getResources().getString(R.string.sure));
						dlgAlert.setMessage(String.format(context.getResources().getString(R.string.release_release),item.toString()));
						dlgAlert.setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								holder.button.setEnabled(false);
								WebConnecter.release(item);
							}
						});
						dlgAlert.setNegativeButton(context.getResources().getString(R.string.cancel), null);
						dlgAlert.setCancelable(true);
						dlgAlert.create().show();
					}	
				} else {
					AlertDialog.Builder dlgAlert = new AlertDialog.Builder(context);
					dlgAlert.setMessage(item.getAddress());
					dlgAlert.setTitle(String.format(context.getResources().getString(R.string.release_give),item.toString()));
					dlgAlert.setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							holder.button.setEnabled(false);
							WebConnecter.release(item);
						}
					});
					dlgAlert.setNegativeButton(context.getResources().getString(R.string.cancel), null);
					dlgAlert.setCancelable(true);
					dlgAlert.create().show();
				}
			}
	    });
	    holder.address.setOnClickListener(null);
	    holder.address.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder dlgAlert = new AlertDialog.Builder(context);

				dlgAlert.setMessage(item.getAddress());
				dlgAlert.setTitle(item.toString());
				dlgAlert.setPositiveButton(context.getResources().getString(R.string.ok), null);
				dlgAlert.setCancelable(true);
				dlgAlert.create().show();
			}
		});
	    return view;
	  }

}
