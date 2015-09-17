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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class AbstractActivity extends  Activity {

	public View uploadActionIcon=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    WebConnecter.currentActivity=this;
	    WebConnecter.doNetwork();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			// Display the fragment as the main content.
			Intent intent = new Intent(this, SettingsActivity.class);
	        startActivity(intent);
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);

		MenuItem item = menu.findItem(R.id.action_upload);
	    uploadActionIcon = item.getActionView();
	    WebConnecter.currentActivity=this;
	    return true;
	}

	public void setIcon(int count){
		if(uploadActionIcon==null)return;
		View container=uploadActionIcon.findViewById(R.id.upload);
		if(count>0){
			TextView number=(TextView) uploadActionIcon.findViewById(R.id.upload_number);
			number.setText(String.valueOf(count));
			container.setVisibility(View.VISIBLE);
		} else {
			container.setVisibility(View.INVISIBLE);
		}
	}

	
}
