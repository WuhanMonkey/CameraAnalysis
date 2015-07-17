package com.example.cameraanalysis;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.Liulab.cameraanalysis.R;


public class MainActivity extends Activity {
	private static final int CAMERA_REQUEST = 1888; 
	private static final int SELECT_PICTURE = 1;

	private String selectedImagePath;
    static String str_Camera_Photo_ImagePath = "";
    private static File f;
    private static int Take_Photo = 2;
    private static String str_randomnumber = "";
    static String str_Camera_Photo_ImageName = "";
    public static String str_SaveFolderName;
    private static File wallpaperDirectory;
    Bitmap bitmap;
    Bitmap analysis_bitmap;
    int storeposition = 0;
    public static GridView gridview;
    public static ImageView imageView;
    public static float concentration;
    public static int bitmapHeight;
    public static int bitmapWidth;
    private static int r_arr[] = new int[200];
    private static int g_arr[] = new int[200];
    private static int b_arr[] = new int[200];
    private static int x_offset = 0;
    private static int y_offset = 0;
    private static int first_x = 0;
    private static int first_y = 0;
    private static ArrayList<Float> x_list = new ArrayList<Float>();
    private static ArrayList<Float> y_list = new ArrayList<Float>();
    private static int cube_x = 4;
    private static int cube_y = 4;
    private static TextView t_glu;
    private static TextView t_chl;
    private static TextView t_hdl;
    private static TextView glu_warn;
    private static TextView chl_warn;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		TextView text = (TextView) findViewById(R.id.textView1);
		text.setTextColor(Color.parseColor("#FFFFFF"));
		
		this.imageView = (ImageView)this.findViewById(R.id.imageView1);
        Button photoButton = (Button) this.findViewById(R.id.photo);
        Button analysisButton = (Button) this.findViewById(R.id.Analysis);
        Button loadImageButton = (Button) this.findViewById(R.id.load);
        t_glu=new TextView(this); 
        t_glu=(TextView)findViewById(R.id.glu);
        t_glu.setTextColor(Color.parseColor("#FFFFFF"));
        t_chl=new TextView(this); 
        t_chl=(TextView)findViewById(R.id.chl);
        t_chl.setTextColor(Color.parseColor("#FFFFFF"));
        t_hdl=new TextView(this); 
        t_hdl=(TextView)findViewById(R.id.hdl);
        t_hdl.setTextColor(Color.parseColor("#FFFFFF"));
        glu_warn=new TextView(this); 
        glu_warn=(TextView)findViewById(R.id.glu_warn);
        glu_warn.setTextColor(Color.parseColor("#FFFFFF"));
        chl_warn=new TextView(this); 
        chl_warn=(TextView)findViewById(R.id.chl_warn);
        chl_warn.setTextColor(Color.parseColor("#FFFFFF"));
        
        
        photoButton.setOnClickListener(new View.OnClickListener() {
        	
            @Override
            public void onClick(View v) {
                str_SaveFolderName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AnalysisSample";
                str_randomnumber = String.valueOf(nextSessionId());
                wallpaperDirectory = new File(str_SaveFolderName);
                if (!wallpaperDirectory.exists())
                    wallpaperDirectory.mkdirs();
                str_Camera_Photo_ImageName = str_randomnumber + ".jpg";
                str_Camera_Photo_ImagePath = str_SaveFolderName + "/" + str_randomnumber + ".jpg";
                System.err.println(" str_Camera_Photo_ImagePath  "+ str_Camera_Photo_ImagePath);
                f = new File(str_Camera_Photo_ImagePath);
                startActivityForResult(new Intent(
                        MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                        MediaStore.EXTRA_OUTPUT, Uri.fromFile(f)),
                        Take_Photo);
                System.err.println("f  " + f);
            }
        });
        
        analysisButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				concentrationAnalysis(analysis_bitmap);
			}
		});
        loadImageButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), SELECT_PICTURE);			
			}
		});
        
    }
	@Override
	public void onBackPressed(){
		this.finish();
		Intent intent = new Intent(this, Directory.class);
        startActivity(intent);
	}
	// used to create random numbers
    public String nextSessionId() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        if (requestCode == Take_Photo) {
            String filePath = null;

            filePath = str_Camera_Photo_ImagePath;
            if (filePath != null) {
                Bitmap faceView = ( new_decode(new File(filePath)));
                analysis_bitmap = faceView;
                imageView.setImageBitmap(faceView);

            } else {
                bitmap = null;
            }
        }
        else if (requestCode == SELECT_PICTURE) {
            Uri selectedImageUri = data.getData();
            selectedImagePath = getPath(selectedImageUri);
            System.out.println("Image Path : " + selectedImagePath);
            Bitmap faceView = ( new_decode(new File(selectedImagePath)));
            analysis_bitmap = faceView;
            imageView.setImageBitmap(faceView);
        }
    } 
    public static void concentrationAnalysis(Bitmap analysis_bitmap){

    	int rgb[] = new int[3];
    	double glu_c[] = {2.3,4.6,7.7,13.4};      
    	double hdl_c[] = {0.42,0.83,1.32};
    	double chl_c[] = {2.98,3.64,5.09,6.14};
    	int glu_width[] = {62,62,62,62};
    	int glu_height[] = {183,206,228,249};
    	int hdl_width[] ={81,81,81};
    	int hdl_height[] = {183,228,249}; 
    	int chl_width[] ={100,100,100,100};
    	int chl_height[] = {183,206,228,249}; 
    	int glu_target_width = 145;
    	int glu_target_height = 208;
    	int hdl_target_width = 145;
    	int hdl_target_height = 230;
    	int chl_target_width = 145;
    	int chl_target_height = 249;
    	float target_s = 0;
    	float s[] = new float[glu_width.length];
    	float hsb[] = new float[3];
    	bitmapHeight = analysis_bitmap.getHeight();
    	bitmapWidth = analysis_bitmap.getWidth();
    	float result_glu =0;
    	float result_hdl =0;
    	float result_chl =0;

    	Log.i("Testing", "The bitmap Height is: " + bitmapHeight);
    	Log.i("Testing", "The bitmap Width is: " + bitmapWidth);
  
    	//GLU
    	for(int i=0; i<glu_width.length; i++){
    		rgb = cal_avg_rgb(analysis_bitmap, glu_width[i], glu_height[i], true);
    		Log.i("Testing", "The avg rgb is: "+ rgb[0] + " " + rgb[1] +" " + rgb[2]);
        	hsb = rgb2hsb(rgb);
        	Log.i("Testing", "The hsb is: "+ hsb[0] + " " + hsb[1] + " " + hsb[2]);
        	s[i] = hsb[1] * 100;
    	}
    	rgb = cal_avg_rgb(analysis_bitmap, glu_target_width, glu_target_height, true);
    	hsb = rgb2hsb(rgb);
    	target_s = hsb[1]*100;
    	
    	result_glu = linear_regression(glu_c, s, target_s);
    	
    	//HDL
    	for(int i=0; i<hdl_width.length; i++){
    		rgb = cal_avg_rgb(analysis_bitmap, hdl_width[i], hdl_height[i], true);
    		Log.i("Testing", "The avg rgb is: "+ rgb[0] + " " + rgb[1] +" " + rgb[2]);
        	hsb = rgb2hsb(rgb);
        	Log.i("Testing", "The hsb is: "+ hsb[0] + " " + hsb[1] + " " + hsb[2]);
        	s[i] = hsb[1] * 100;
    	}
    	rgb = cal_avg_rgb(analysis_bitmap, hdl_target_width, hdl_target_height, true);
    	hsb = rgb2hsb(rgb);
    	target_s = hsb[1]*100;
    	
    	result_hdl = linear_regression(hdl_c, s, target_s);
    	
    	//CHL
    	for(int i=0; i<chl_width.length; i++){
    		rgb = cal_avg_rgb(analysis_bitmap, chl_width[i], chl_height[i], true);
    		Log.i("Testing", "The avg rgb is: "+ rgb[0] + " " + rgb[1] +" " + rgb[2]);
        	hsb = rgb2hsb(rgb);
        	Log.i("Testing", "The hsb is: "+ hsb[0] + " " + hsb[1] + " " + hsb[2]);
        	s[i] = hsb[1] * 100;
    	}
    	rgb = cal_avg_rgb(analysis_bitmap, chl_target_width, chl_target_height, true);
    	Log.i("Testing", "The target chl avg rgb is: "+ rgb[0] + " " + rgb[1] +" " + rgb[2]);
    	hsb = rgb2hsb(rgb);
    	target_s = hsb[1]*100;

    	result_chl = linear_regression(chl_c, s, target_s);
    	
    	
    	if(result_glu < 0){
    		result_glu = 0;
    	}
    	if(result_hdl < 0){
    		result_hdl = 0;
    	}
    	if(result_chl < 0){
    		result_chl = 0;
    	}

		t_glu.setText("Glucose concentration: " + Float.toString(result_glu) + " mmol/L");
		if(result_glu > 5.8){
			glu_warn.setText("Your Glucose is too high!");
			glu_warn.setTextColor(Color.parseColor("#FF0000"));
		}
		else if (result_glu < 3.9){
			glu_warn.setText("Your Glucose is too low!");
			glu_warn.setTextColor(Color.parseColor("#FF0000"));
		}
		else {
			glu_warn.setText("Your Glucose is Normal!");
			glu_warn.setTextColor(Color.parseColor("#008000"));
		}
		
		t_hdl.setText("HDL concentration: " + Float.toString(result_hdl)+ " mmol/L");
		t_chl.setText("Cholesterol concentration: " + Float.toString(result_chl)+ " mmol/L");
		
		if(result_chl > 6.2){
			chl_warn.setText("Your Cholesterol is too high!");
			chl_warn.setTextColor(Color.parseColor("#FF0000"));
		}
		else {
			chl_warn.setText("Your Cholesterol is normal!");
			chl_warn.setTextColor(Color.parseColor("#008000"));
		}

    	   
    }
    
    
    public static int[] w_normalization(int[] w_rgb, int[] rgb){
    	rgb[0] = (int)(rgb[0] *(float)255/w_rgb[0]);
    	rgb[1] = (int)(rgb[1] *(float)255/w_rgb[1]);
    	rgb[2] = (int)(rgb[2] *(float)255/w_rgb[2]);
    	return rgb;
    	
    }
    
    public static int[] cal_avg_rgb(Bitmap analysis_bitmap, int x, int y, boolean ref){
    	int green = 0;
    	int blue = 0;
    	int red = 0;
    	int argb =0;
    	for(int j =y; j<y + cube_y;j++){
    		for (int i = x;i<x + cube_x;i++){
    			argb = analysis_bitmap.getPixel(i,j); 
    	    	red = red + Color.red(argb);
    	    	green =  green +Color.green(argb);
    	    	blue = blue +Color.blue(argb);
    	    	}
    	}
    	red = (int) (red/(cube_x*cube_y));
    	green = (int) (green/(cube_x*cube_y));
    	blue = (int) (blue/(cube_x*cube_y));
    	int rgb[] = new int[3];
    	rgb[0] = red;
    	rgb[1] = green;
    	rgb[2] = blue;
    	
    	return rgb;

    }
    public static float[] rgb2cmyk (int[] rgb){
    	float[] cmyk = new float[4];
    	float c = 1 - (float)rgb[0] / 255;
        float m = 1 - (float)rgb[1] / 255;
        float y = 1 - (float)rgb[2] / 255;
        float k = Math.min(Math.min(c,y), m);
        if ( k == 1 ){
        c = m = y = 0;
        }
        else{
        float s = 1 - k;
        c = ( c - k ) / s;
        m = ( m - k ) / s;
        y = ( y - k ) / s;
        }
        cmyk[0] = c;
        cmyk[1] = m;
        cmyk[2] = y;
        cmyk[3] = k;
        
    	return cmyk;
    }
    
    public static float[] rgb2cie(int[] rgb){
    	float[] cie = new float[2];
    	float x=0, y=0, z=0;
    	float X=0, Y=0;
    	x = (float) (0.4124 * rgb[0] + 0.3576*rgb[1] + 0.1805 * rgb[2]);
    	y = (float) (0.2126 * rgb[0] + 0.7152*rgb[1] + 0.0722 * rgb[2]);
    	z = (float) (0.0193 *rgb[0] + 0.1192*rgb[1] + 0.9505 *rgb[2]);
    	X = x/(x+y+z);
    	Y = y/(x+y+z);
    	cie[0] = X;
    	cie[1] = Y;    	
    	return cie;
    }
    
    public static float[] rgb2hsb(int[] rgb) {
    	   float hue, saturation, brightness;
    	   int r=0,g=0,b=0;
    	   float[] hsbvals = new float[3];
    	   r = rgb[0];
    	   g = rgb[1];
    	   b = rgb[2];
    	   int cmax = (r > g) ? r : g;
    	   if (b > cmax) cmax = b;
    	   int cmin = (r < g) ? r : g;
    	   if (b < cmin) cmin = b;
    	   	   brightness = ((float) cmax) / 255.0f;
    	   if (cmax != 0)
    	       saturation = ((float) (cmax - cmin)) / ((float) cmax);
    	   else
    	       saturation = 0;
    	   if (saturation == 0)
    	       hue = 0;
    	   else {
    	       float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
    	       float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
    	       float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
    	       if (r == cmax)
    	          hue = bluec - greenc;
    	       else if (g == cmax)
    	          hue = 2.0f + redc - bluec;
    	       else
    	          hue = 4.0f + greenc - redc;
    	          hue = hue / 6.0f;
    	          if (hue < 0)
    	             hue = hue + 1.0f;
    	        }
    	    hsbvals[0] = hue;
    	    hsbvals[1] = saturation;
    	    hsbvals[2] = brightness;
    	    return hsbvals;
 }
    
    
    public static float linear_regression(double[] concentration, float[] k, float target_k){
    	float result = 0;
    	SimpleRegression regression = new SimpleRegression();
    	for(int i = 0; i<concentration.length; i++){
    		regression.addData(concentration[i], k[i]);
    	}

    	// displays intercept of regression line
    	Log.i("Testing", "Intercept is: " + regression.getIntercept());
    	// displays slope of regression line
    	Log.i("Testing", "slope is: " + regression.getSlope());
    	// displays slope standard error
    	Log.i("Testing", "standard error is: " + regression.getSlopeStdErr());
    	
    	result = (float) (target_k - regression.getIntercept())/((float)regression.getSlope());
    	
    	return result;
    	
    	
    }
    
    public static void nearest_neighbor(float X, float Y){
    	float closest_X = 0;
    	float closest_Y = 0;
    	float temp_x = 0;
    	float temp_y = 0;
    	float temp = 0;
    	float nearest_temp =1000000;
    	int nearest_id =0;
    	int list_size = x_list.size();
    	for(int i = 0; i<list_size; i++){
    		temp_x = x_list.get(i);
    		temp_y = y_list.get(i);
    		temp = (float) (Math.pow((X-temp_x),2) + Math.pow((Y-temp_y),2));
    		temp = (float) Math.pow(temp, 1/2);
    		if(nearest_temp > temp ){
    			nearest_temp = temp;
    			nearest_id = i;
    		}
    	}
    	closest_X = x_list.get(nearest_id);
    	closest_Y = y_list.get(nearest_id);
    	x_list.clear();
    	y_list.clear();
    }
    
    public static void row_diff(int[] r_arr, int[] g_arr, int[] b_arr){
    	int r_diff = 0, g_diff=0, b_diff=0;
    	for (int i=0; i<199; i++){
    		r_diff = Math.abs(r_arr[i+1] - r_arr[0]);
    		g_diff = Math.abs(g_arr[i+1] - g_arr[0]);
    		b_diff = Math.abs(b_arr[i+1] - b_arr[0]);  		
    	}
    	//Log.i("Testing", "Here is the rgb difference r: " + Integer.toString(r_diff) + " g: " + Integer.toString(g_diff)+  " b: " + Integer.toString(b_diff) + "\n"  );
    	Log.i("Testing", "combine rgb diff: " + Integer.toString(r_diff + g_diff + b_diff) + "\n");
    }
    
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    
    public static Bitmap new_decode(File f) {

        // decode image size

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        o.inDither = false; // Disable Dithering mode

        o.inPurgeable = true; // Tell to gc that whether it needs free memory,
                                // the Bitmap can be cleared

        o.inInputShareable = true; // Which kind of reference will be used to
                                    // recover the Bitmap data after being
                                    // clear, when it will be used in the future
        try {
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // Find the correct scale value. It should be the power of 2.
        final int REQUIRED_SIZE = 300;
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 1.5 < REQUIRED_SIZE && height_tmp / 1.5 < REQUIRED_SIZE)
                break;
            width_tmp /= 1.5;
            height_tmp /= 1.5;
            scale *= 1.5;
        }

        // decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        // o2.inSampleSize=scale;
        o.inDither = false; // Disable Dithering mode

        o.inPurgeable = true; // Tell to gc that whether it needs free memory,
                                // the Bitmap can be cleared

        o.inInputShareable = true; // Which kind of reference will be used to
                                    // recover the Bitmap data after being
                                    // clear, when it will be used in the future
        // return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        try {

//          return BitmapFactory.decodeStream(new FileInputStream(f), null,
//                  null);
            Bitmap bitmap= BitmapFactory.decodeStream(new FileInputStream(f), null, null);
            System.out.println(" IW " + width_tmp);
            System.out.println("IHH " + height_tmp);           
               int iW = width_tmp;
                int iH = height_tmp;

               return Bitmap.createScaledBitmap(bitmap, iW, iH, true);

        } catch (OutOfMemoryError e) {
            // TODO: handle exception
            e.printStackTrace();
            // clearCache();

            // System.out.println("bitmap creating success");
            System.gc();
            return null;
            // System.runFinalization();
            // Runtime.getRuntime().gc();
            // System.gc();
            // decodeFile(f);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
