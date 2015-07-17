package com.example.cameraanalysis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import com.Liulab.cameraanalysis.R;

public class Directory extends Activity{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		   if (android.os.Build.VERSION.SDK_INT > 9) {
			      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			      StrictMode.setThreadPolicy(policy);
			    }

		super.onCreate(savedInstanceState);
		setContentView(R.layout.directory);
		}
	
	public void toGlucose(View view){
		 Intent intent = new Intent(this, MainActivity.class);
	        startActivity(intent);
	        finish();
	}
	
	public void toUrine(View view){
		 Intent intent = new Intent(this, Urine.class);
	        startActivity(intent);
	        finish();
	}
}