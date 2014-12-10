package abr.teleop;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ControllerConnection extends Activity {
	EditText etxtIP, etxtPass;
	Button buttonConnect;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
		setContentView(R.layout.controller_connection);
		SharedPreferences settings = getSharedPreferences("IPAddress", 0);
	       
		etxtIP = (EditText)findViewById(R.id.etxtIP);
		etxtIP.setText(settings.getString("IP", "192.168.1.1"));

		etxtPass = (EditText)findViewById(R.id.etxtPass);
		etxtPass.setText(settings.getString("Pass", ""));

		buttonConnect = (Button)findViewById(R.id.buttonConnect);
		buttonConnect.setEnabled(false);
		buttonConnect.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ControllerConnection.this, Controller.class);
				intent.putExtra("IP", etxtIP.getText().toString());
				intent.putExtra("Pass", etxtPass.getText().toString());
				startActivity(intent);
			}
		});
	}
	
	public void onPause() {
		super.onPause();
		SharedPreferences settings = getSharedPreferences("IPAddress", 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString("IP", etxtIP.getText().toString());
	    editor.putString("Pass", etxtPass.getText().toString());
	    editor.commit();
	}
	
	public void onResume() {
		super.onResume();
		buttonConnect.setEnabled(false);
		
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
					runOnUiThread(new Runnable() {
						public void run() {
							buttonConnect.setEnabled(true);
						}
					});
				} catch (InterruptedException e) { }
			}
		}).start();
	}
}
