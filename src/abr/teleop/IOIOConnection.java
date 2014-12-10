package abr.teleop;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class IOIOConnection extends Activity {
	TextView txtQuality;
	EditText etxtPass;
	SeekBar barQuality;
	Button buttonOK;
	
	ArrayList<String> size;
	List<Size> previewSize;
	int cameraSize;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
		setContentView(R.layout.ioio_connection);

	    SharedPreferences settings = getSharedPreferences("Pref", 0);
	    
		etxtPass = (EditText)findViewById(R.id.etxtPass);
	    etxtPass.setText(settings.getString("Pass", ""));
	    
	    Camera mCamera = null;
    	if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD) {
	    	mCamera = Camera.open();
    	} else if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
	    	mCamera = Camera.open(0);
    	}
	    
	    Camera.Parameters params = mCamera.getParameters();
	    
	    size = new ArrayList<String>();
	    previewSize = params.getSupportedPreviewSizes();

	    mCamera.release();
	    mCamera = null;
	    
	    for(int i = 0 ; i < previewSize.size() ; i++) {
        	String str = previewSize.get(i).width + " x " + previewSize.get(i).height;
        	size.add(str);
        }		    
	    
	    final Button buttonSize = (Button)findViewById(R.id.buttonSize);
	    buttonSize.setText(size.get(settings.getInt("Size", 0)));
	    cameraSize = settings.getInt("Size", 0);
	    buttonSize.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
			    final Dialog dialogSize = new Dialog(IOIOConnection.this);
			    dialogSize.requestWindowFeature(dialogSize.getWindow().FEATURE_NO_TITLE);
			    dialogSize.setContentView(R.layout.dialog_camerasize);
			    dialogSize.setCancelable(true);
			    
			    ListView listSize = (ListView)dialogSize.findViewById(R.id.listSize);
			    listSize.setAdapter(new CustomListView(getApplicationContext(), R.layout.listview_simple_row, size));
			    listSize.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0,
							View arg1, int arg2, long arg3) {
						SharedPreferences settings = getSharedPreferences("Pref", 0);
				        SharedPreferences.Editor editor = settings.edit();
				        editor.putInt("Size", arg2);
				        editor.commit();
				        
						cameraSize = arg2;
						buttonSize.setText(size.get(arg2));
						dialogSize.cancel();
					}
			    	
			    });
			    dialogSize.show();
	    	}
	    });

	    txtQuality = (TextView)findViewById(R.id.txtQuality);
	    txtQuality.setText("Image Quality " + String.valueOf(settings.getInt("Quality", 100)) + "%");
	    	    
	    barQuality = (SeekBar)findViewById(R.id.barQuality);
	    barQuality.setProgress(settings.getInt("Quality", 100));
	    barQuality.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar,
					int progress, boolean fromUser) {
				SharedPreferences settings = getSharedPreferences("Pref", 0);
		        SharedPreferences.Editor editor = settings.edit();
		        editor.putInt("Quality", progress);
		        editor.commit();
		        
				txtQuality.setText("Image Quality " + String.valueOf(progress) + "%");
			}

			public void onStartTrackingTouch(SeekBar seekBar) { }

			public void onStopTrackingTouch(SeekBar seekBar) { }
	    });
	    
	    ImageView imageView1 = (ImageView)findViewById(R.id.imageView1);
	    imageView1.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		finish();
	    	}
	    });

	    buttonOK = (Button)findViewById(R.id.buttonOK);
	    buttonOK.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(etxtPass.getText().toString().length() != 0) {
					SharedPreferences settings = getSharedPreferences("Pref", 0);
			        SharedPreferences.Editor editor = settings.edit();
			        editor.putString("Pass", etxtPass.getText().toString());
			        editor.commit();
			        
			        Intent intent = new Intent(getApplicationContext(), IOIO.class);
					intent.putExtra("Pass", etxtPass.getText().toString());
					intent.putExtra("Size", cameraSize);
					intent.putExtra("Quality", barQuality.getProgress());
					startActivity(intent);
					
				} else {
					Toast.makeText(getApplicationContext(), "Please Insert Password", Toast.LENGTH_SHORT).show();	
				}
			}
	    });
	    
	}
}
