package com.t3hh4xx0r.addons;

import com.t3hh4xx0r.addons.utils.Constants;
import com.t3hh4xx0r.addons.utils.DeviceType;
import com.t3hh4xx0r.addons.utils.Downloads;
import com.t3hh4xx0r.addons.web.JSON.JSONUtils;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import com.t3hh4xx0r.R;

public class MainMenu extends PreferenceActivity  {
	PreferenceCategory mAddonsCat;
	PreferenceCategory mRomsCat;
	PreferenceCategory mNewsCat;
        PreferenceCategory mSettingsCat;

	private boolean DBG = (false || Constants.FULL_DBG);

        public static String TAG = "MainMenu";
	PreferenceScreen mNightlies;
	
	private final int CLEARCACHE = 0;
	private final int SETTINGSMENU = CLEARCACHE + 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		if(Constants.FIRST_LAUNCH)determineDevice();
		if(Constants.FIRST_LAUNCH){	
			Runnable scripts = new Runnable(){	
				@Override
				public void run() {
					// TODO Auto-generated method stub
					JSONUtils u = new JSONUtils();
					Downloads.refreshAddonsAndNightlies();
				}
				
			};
			
			Thread t = new Thread(scripts);
			t.start();		
		}
		addPreferencesFromResource(R.layout.main_menu);
		mAddonsCat = (PreferenceCategory) findPreference("addons_category");
		mRomsCat = (PreferenceCategory) findPreference("roms_category");
                mNewsCat = (PreferenceCategory) findPreference("news_category");
                mSettingsCat = (PreferenceCategory) findPreference("settings_category");

                mRomsCat.setEnabled(true);
                mNewsCat.setEnabled(true);
                mAddonsCat.setEnabled(true);
                mSettingsCat.setEnabled(true);

		if (!hasStorage(true)){
                        mNewsCat.setEnabled(false);
			mAddonsCat.setEnabled(false);
			mRomsCat.setEnabled(false);
                        if(Constants.FIRST_LAUNCH) {
			   AlertBox(getString(R.string.warning),getString(R.string.sdcard_not_mounted));
			}
		}

		if (!Constants.isDeviceDetermined()){
			mRomsCat.setEnabled(false);
			if(Constants.FIRST_LAUNCH) {
   			   AlertBox(getString(R.string.warning),"Device not supported. Disabling roms.");
			}
		}

                if(!Build.USER.equals("r2doesinc")) {
                        if(Constants.FIRST_LAUNCH) {
                           AlertBox(getString(R.string.warning),"This build was not compiled by the bot." +
		 	   " This is not an official build, please do not submit bug reports.");
                        }
                }

		try {
  	            if(!SystemProperties.get("ro.build.romversion").equals("OMFGB")) {
			    mSettingsCat.setEnabled(false);
                            if(Constants.FIRST_LAUNCH) {
                               AlertBox(getString(R.string.warning),"Rom not supported. Disabling settings.");
			    }
		    }
		} catch (Exception e) {
                    mSettingsCat.setEnabled(false);
                        if(Constants.FIRST_LAUNCH) {
                            AlertBox(getString(R.string.warning),"Rom not supported. Disabling settings.");
                        }
		}

                if(Constants.FIRST_LAUNCH)Constants.FIRST_LAUNCH = false;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuinflate = new MenuInflater(this);
		menuinflate.inflate(R.menu.main_menu, menu);
		return true;
	}	

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear_download_cache:
			Downloads.deleteDir();
			break;
		case R.id.refresh:
			Downloads.refreshAddonsAndNightlies();
			break;
		case R.id.settings_menu:
			launchSettingMenu();
			break;
		}
	    return(super.onOptionsItemSelected(item));
	}	

    private void launchSettingMenu() {    	
        Intent settings = new Intent(this, SettingsMenu.class);
        startActivity(settings);
	}

	static public boolean hasStorage(boolean requireWriteAccess) {		
	    String state = Environment.getExternalStorageState();
	    Log.v(TAG, "storage state is " + state);

	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        if (requireWriteAccess) {
	            return true;
	        } else {
	            return false;
	        }
	    } else if (!requireWriteAccess && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}

	private void determineDevice(){
		if(!DeviceType.determineDeviceDensity(getResources().getDisplayMetrics().densityDpi)){
			log("Cannot determine device density, defaultingg to Unset");
	    }
		log("Beggining to set the device");
		// We dont need to set the device script every time just once
		if(Constants.getDeviceScript() == null || !Constants.getDeviceScript().equals("")){
		    if (DeviceType.deviceDeviceEquals(DeviceType.INCREDIBLE)) {
				log("Setting device as " + DeviceType.INCREDIBLE);
			        Constants.setDeviceScript(DeviceType.INCREDIBLE_SCRIPT);
			   	DeviceType.DEVICE_TYPE = DeviceType.INCREDIBLE;     	
			}else  if (DeviceType.deviceDeviceEquals(DeviceType.ERIS)) {
				log("Setting device as " + DeviceType.ERIS);
			   	Constants.setDeviceScript(DeviceType.ERIS_SCRIPT);
			   	DeviceType.DEVICE_TYPE = DeviceType.ERIS;  	
			}else  if (DeviceType.deviceDeviceEquals(DeviceType.SHADOW)) {
				log("Setting device as " + DeviceType.SHADOW);
                                Constants.setDeviceScript(DeviceType.SHADOW_SCRIPT);
                                DeviceType.DEVICE_TYPE = DeviceType.SHADOW;
		        }else  if (DeviceType.deviceDeviceEquals(DeviceType.DROID)) {
				log("Setting device as " + DeviceType.DROID);
			   	Constants.setDeviceScript(DeviceType.DROID_SCRIPT);
			   	DeviceType.DEVICE_TYPE = DeviceType.DROID;	
			}else  if (DeviceType.deviceDeviceEquals(DeviceType.EVO)) {
				log("Setting device as " + DeviceType.EVO);
			    Constants.setDeviceScript(DeviceType.EVO_SCRIPT);
			   	DeviceType.DEVICE_TYPE = DeviceType.EVO;     	
			}else  if (DeviceType.deviceDeviceEquals(DeviceType.HERO)) {
				log("Setting device as " + DeviceType.HERO);
			   	Constants.setDeviceScript(DeviceType.HERO_SCRIPT);
			   	DeviceType.DEVICE_TYPE = DeviceType.HERO;      	
			}else  if (DeviceType.deviceDeviceEquals(DeviceType.THUNDERBOLT)) {
				log("Setting device as " + DeviceType.THUNDERBOLT);
			   	Constants.setDeviceScript(DeviceType.THUNDERBOLT_SCRIPT);
			   	DeviceType.DEVICE_TYPE = DeviceType.THUNDERBOLT;      	
			}else  if (DeviceType.deviceDeviceEquals(DeviceType.INCREDIBLE2)) {
				log("Setting device as " + DeviceType.INCREDIBLE2);
                		Constants.setDeviceScript(DeviceType.INCREDIBLE2_SCRIPT);
                		DeviceType.DEVICE_TYPE = DeviceType.INCREDIBLE2;
			}else  if (DeviceType.deviceDeviceEquals(DeviceType.FASCINATEMTD)) {
				log("Setting device as " + DeviceType.FASCINATEMTD);
				Constants.setDeviceScript(DeviceType.FASCINATEMTD_SCRIPT);
				DeviceType.DEVICE_TYPE = DeviceType.FASCINATEMTD;
			}else  if (DeviceType.deviceDeviceEquals(DeviceType.SHOWCASEMTD)) {	 
				log("Setting device as " + DeviceType.SHOWCASEMTD);
                		Constants.setDeviceScript(DeviceType.SHOWCASEMTD_SCRIPT);
                		DeviceType.DEVICE_TYPE = DeviceType.SHOWCASEMTD;
            		}else  if (DeviceType.deviceDeviceEquals(DeviceType.MESMERIZEMTD)) {
 				log("Setting device as " + DeviceType.MESMERIZEMTD);
                		Constants.setDeviceScript(DeviceType.MESMERIZEMTD_SCRIPT);
                		DeviceType.DEVICE_TYPE = DeviceType.MESMERIZEMTD;
            		}else  if (DeviceType.deviceDeviceEquals(DeviceType.VIBRANTMTD)) {
                	  	log("Setting device as " + DeviceType.VIBRANTMTD);
                		Constants.setDeviceScript(DeviceType.VIBRANTMTD_SCRIPT);
                		DeviceType.DEVICE_TYPE = DeviceType.VIBRANTMTD;
            		}else  if (DeviceType.deviceDeviceEquals(DeviceType.P999)) {
                		log("Setting device as " + DeviceType.P999);
                		Constants.setDeviceScript(DeviceType.P999_SCRIPT);
                		DeviceType.DEVICE_TYPE = DeviceType.P999;
                        }else  if (DeviceType.deviceDeviceEquals(DeviceType.ACE)) {
                                log("Setting device as " + DeviceType.ACE);
                                Constants.setDeviceScript(DeviceType.ACE_SCRIPT);
                                DeviceType.DEVICE_TYPE = DeviceType.ACE;
                        }else  if (DeviceType.deviceDeviceEquals(DeviceType.pecan)) {
                                log("Setting device as " + DeviceType.pecan);
                                Constants.setDeviceScript(DeviceType.pecan_SCRIPT);
                                DeviceType.DEVICE_TYPE = DeviceType.pecan; 
                        }
        }		
	}
	
	protected void AlertBox(String title, String mymessage) {	    
        new AlertDialog.Builder(this)
		    .setMessage(mymessage)
	     	.setTitle(title)
	     	.setCancelable(false)
	     	.setPositiveButton("OK",new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int whichButton){
		     		// Do nothing, the warning is enough.
		     	}
		     })
		     .show();
    }
	   
    private void log(String message){  
        if(DBG)Log.d(TAG, message);	   
	}
}
