package com.example.cameraanalysis;

import java.io.File;

import android.app.Activity;
import android.content.Context;
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


public class Urine extends Activity {
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

    private static int cube_x = 5;
    private static int cube_y = 5;
    private static Activity thisActivity = null;
    private static TextView t_glu;
    private static TextView t_pro;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.urine);
		TextView text = (TextView) findViewById(R.id.textView1);
		text.setTextColor(Color.parseColor("#FFFFFF"));
		
		this.imageView = (ImageView)this.findViewById(R.id.imageView1);
        Button photoButton = (Button) this.findViewById(R.id.photo);
        Button analysisButton = (Button) this.findViewById(R.id.Analysis);
        Button loadImageButton = (Button) this.findViewById(R.id.load);
        thisActivity = this;
        t_glu=new TextView(this); 
        t_glu=(TextView)findViewById(R.id.glu);
        t_glu.setTextColor(Color.parseColor("#FFFFFF"));
        t_pro=new TextView(this); 
        t_pro=(TextView)findViewById(R.id.pro);
        t_pro.setTextColor(Color.parseColor("#FFFFFF"));
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
    	int w_rgb[] = new int[3];
    	int w_ref_rgb[] = new int[3];
    	float cmyk[] = new float[4];
    	float cie[] = new float[2];
    	double pro_c[] = {20,10,5,2.5,1.25,0.625,0};
    	double glu_c[] = {100,50,25,12.5,6.25,0};      
    	int glu_width[] = {38,61,83,102,123,147};
    	int glu_height[] = {201,201,201,201,201,201};
    	int pro_width[] ={38,57,73,92,109,128,147};
    	int pro_height[] = {180,180,180,180,180,180,180}; 
    	int glu_target_width = 95;
    	int glu_target_height = 237;
    	int pro_target_width = 119;
    	int pro_target_height = 237;
    	float h_glu[] = new float[glu_width.length];
    	float h_pro[] = new float[pro_width.length];
    	float k_glu[] = new float[glu_width.length];
    	float k_pro[] = new float[pro_width.length];
    	float hsb[] = new float[3];
    	float target_pro = 0;
    	float target_glu = 0;
    	float target_k_pro = 0;
    	float target_k_glu = 0;
    	float result_glu = 0;
    	float result_pro = 0;
    	int white_width =83;
    	int white_height = 236;
    	int white_ref_width = 100;
    	int white_ref_height = 170;
    	bitmapHeight = analysis_bitmap.getHeight();
    	bitmapWidth = analysis_bitmap.getWidth();

    	Log.i("Testing", "The bitmap Height is: " + bitmapHeight);
    	Log.i("Testing", "The bitmap Width is: " + bitmapWidth);
    	w_rgb = cal_avg_rgb(analysis_bitmap, white_width, white_height, true);
    	Log.i("Testing", "The avg white rgb is: "+ w_rgb[0] + " " + w_rgb[1] +" " + w_rgb[2]);
    	w_ref_rgb = cal_avg_rgb(analysis_bitmap, white_ref_width, white_ref_height, true);
    	Log.i("Testing", "The avg white ref rgb is: "+ w_ref_rgb[0] + " " + w_ref_rgb[1] +" " + w_ref_rgb[2]);

    	
    	//PRO
    	for(int i=0; i<pro_width.length; i++){
    		rgb = cal_avg_rgb(analysis_bitmap, pro_width[i], pro_height[i], true);
    		rgb = w_normalization(w_ref_rgb, rgb);
    		Log.i("Testing", "The avg rgb after adjustment is: "+ rgb[0] + " " + rgb[1] +" " + rgb[2]);
        	hsb = rgb2hsb(rgb);
        	h_pro[i]= hsb[0]*360;
        	Log.i("Testing", "The h is: "+ hsb[0]*360);
        	cmyk = rgb2cmyk(rgb);
        	k_pro[i] = cmyk[3];
    	}
    	rgb = cal_avg_rgb(analysis_bitmap, pro_target_width, pro_target_height, true);
    	rgb = w_normalization(w_ref_rgb, rgb);
    	Log.i("Testing", "The target pro avg rgb is: "+ rgb[0] + " " + rgb[1] +" " + rgb[2]);
    	hsb = rgb2hsb(rgb);
    	target_pro = hsb[0]*360;
    	cmyk = rgb2cmyk(rgb);
    	target_k_pro = cmyk[3];
    	

    	//result_pro = nearest_neighbor(pro_c, k_pro, target_k_pro);
    	result_pro = nearest_neighbor(pro_c, h_pro, target_pro);
    	float h_avg=0;
    	//GLU
    	for(int i=0; i<glu_width.length; i++){
    		//rgb = cal_avg_rgb(analysis_bitmap, glu_width[i], glu_height[i], true);
    		h_avg = h_avg(analysis_bitmap, glu_width[i], glu_height[i],w_ref_rgb);
    		h_glu[i] = h_avg*360;
    		Log.i("Testing", "The h average is: " + Float.toString(h_avg*360));
    		//Log.i("Testing", "The glu avg rgb before adjustment is: "+ rgb[0] + " " + rgb[1] +" " + rgb[2]);
    		//adjust(rgb, w_ref_rgb);
    		//Log.i("Testing", "The glu avg rgb after adjustment is: "+ rgb[0] + " " + rgb[1] +" " + rgb[2]);
    		//rgb = w_normalization(w_ref_rgb, rgb);
    		//Log.i("Testing", "The glu avg rgb after adjustment is: "+ rgb[0] + " " + rgb[1] +" " + rgb[2]);
        	//hsb = rgb2hsb(rgb);
        	//h_glu[i]= hsb[0]*360;
        	//Log.i("Testing", "The h is: "+ hsb[0]*360);
        	//cmyk = rgb2cmyk(rgb);
        	//k_glu[i] = cmyk[3];
    	}
    	//rgb = cal_avg_rgb(analysis_bitmap, glu_target_width, glu_target_height, true);
    	
    	h_avg = h_avg(analysis_bitmap, glu_target_width, glu_target_height,w_ref_rgb);
    	//Log.i("Testing", "The h average is: " + Float.toString(h_avg));
    	//rgb = w_normalization(w_ref_rgb, rgb);
    	//Log.i("Testing", "The target glu avg rgb is: "+ rgb[0] + " " + rgb[1] +" " + rgb[2]);
    	//hsb = rgb2hsb(rgb);
    	//target_glu = hsb[0]*360;
    	//cmyk = rgb2cmyk(rgb);
    	//target_k_glu = cmyk[3];
    	target_glu = h_avg *360;

    	//result_glu = nearest_neighbor(glu_c, k_glu, target_k_glu);
    	result_glu = nearest_neighbor(glu_c, h_glu, target_glu);
    	if(result_glu < 0){
    		result_glu = 0;
    	}
    	if(result_pro < 0){
    		result_pro = 0;
    	}
    	t_glu.setText("Glucose concentration: " + Float.toString(result_glu) + " mmol/L");
    	t_pro.setText("Protein concentration: " + Float.toString(result_pro) + " mmol/L");
    	
    
    }
    public static int[] w_normalization(int[] w_rgb, int[] rgb){
    	rgb[0] = (int)(rgb[0] *(float)256/w_rgb[0]);
    	rgb[1] = (int)(rgb[1] *(float)256/w_rgb[1]);
    	rgb[2] = (int)(rgb[2] *(float)256/w_rgb[2]);
    	return rgb;
    	
    }
    public static int[] adjust (int[] rgb, int[] w_ref_rgb){
    	for(int i = 0; i<3; i++){
    		rgb[i]=(int)(rgb[i]*25 - 12*w_ref_rgb[i])/13;
    	}
    	   	
    	return rgb;	
    }
    public static float h_avg(Bitmap analysis_bitmap, int x, int y, int[] w_ref_rgb){
    	int green = 0;
    	int blue = 0;
    	int red = 0;
    	int argb =0;
    	float h = 0;
    	int rgb[] = new int[3];
    	float hsb[] = new float[3];
    	for(int j =y; j<y + cube_y;j++){
    		for (int i = x;i<x + cube_x;i++){
    			argb = analysis_bitmap.getPixel(i,j); 
    	    	red = red + Color.red(argb);
    	    	green =  green +Color.green(argb);
    	    	blue = blue +Color.blue(argb);
    	    	rgb[0] = red;
    	    	rgb[1] = green;
    	    	rgb[2] = blue;
    	    	rgb = w_normalization(w_ref_rgb, rgb);
    	    	hsb = rgb2hsb(rgb);
    	    	h = h + hsb[0];
    	    	//Log.i("Testing", "The avg h is: "+ Float.toString(h));
    	    	}
    	}
    	red = (int) (red/(cube_x*cube_y));
    	green = (int) (green/(cube_x*cube_y));
    	blue = (int) (blue/(cube_x*cube_y));
    	
    	h = h/(cube_x*cube_y);
    	
    	
    	
    	return h;
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
    
    
    public static float linear_regression(double[] concentration, float[] k){
    	float result = 0;
    	SimpleRegression regression = new SimpleRegression();
    	for(int i = 0; i<k.length; i++){
    		regression.addData(concentration[i], k[i]);
    	}

    	// displays intercept of regression line
    	Log.i("Testing", "Intercept is: " + regression.getIntercept());
    	// displays slope of regression line
    	Log.i("Testing", "slope is: " + regression.getSlope());
    	// displays slope standard error
    	Log.i("Testing", "standard error is: " + regression.getSlopeStdErr());
    	return result;
    }
    
    public static float nearest_neighbor(double[] urine_c, float[] h, float target){
    	float concentration =0;
    	float[] diff_h= new float[h.length];
    	for(int i = 0; i < h.length; i++){
    		diff_h[i] = Math.abs(h[i] - target);
    	}
    	float min = 1000000;
    	int index = 0;
    	int min_index = 0;
    	int second_index = 0;
    	for(int i = 0; i <diff_h.length; i++){
    		if(min > diff_h[i]){
    			min = diff_h[i];
    			index = i;
    		}
    	}
    	min_index = index;
    	diff_h[index] = 1000001;
    	min = 1000000;
    	for(int i = 0; i <diff_h.length; i++){
    		if(min > diff_h[i]){
    			min = diff_h[i];
    			index = i;
    		}
    	}
    	second_index = index;
    	Log.i("Testing", "1st min is: " + Float.toString(h[min_index]) + " 2nd min is: " + Float.toString(h[second_index]));
    	
    	SimpleRegression regression = new SimpleRegression();
    	regression.addData(urine_c[min_index], h[min_index]);
    	regression.addData(urine_c[second_index], h[second_index]);
    	Log.i("Testing", "Intercept is: " + regression.getIntercept());
    	Log.i("Testing", "slope is: " + regression.getSlope());
    	
    	concentration = (float) (target - regression.getIntercept())/((float)regression.getSlope());
    	
    	
    	
    	return concentration;
    	
    	/**float closest_X = 0;
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
    	y_list.clear();**/
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
