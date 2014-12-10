package abr.teleop;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Controller extends Activity{
	private final static String TAG = "CameraRobot-Controller";
	
    public static final int MESSAGE_DATA_RECEIVE = 0;
    
    Button buttonUp, buttonDown, buttonLeft, buttonRight;
    ImageView imageView1;
    CheckBox cbFlash;
    
    RelativeLayout layout_joystick, layout_joystick_PT;
	JoyStickClass js, js_PT;
    int screenWidth, screenHeight;
    
	Boolean task_state = true;
	
	OutputStream out; 
	DataOutputStream dos;
	InputStream in;
	DataInputStream dis;
	
	Socket s;
	String ip, pass;
    
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN 
        		| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
		setContentView(R.layout.controller);
 
        Display display = getWindowManager().getDefaultDisplay(); 

        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        
		ip = getIntent().getExtras().getString("IP");
		pass = getIntent().getExtras().getString("Pass");
		
		imageView1 = (ImageView)findViewById(R.id.imageView1);
		
		layout_joystick = (RelativeLayout)findViewById(R.id.layout_joystick);
	    js = new JoyStickClass(getApplicationContext()
        		, layout_joystick, R.drawable.image_button);
	    js.setStickSize(screenHeight / 7, screenHeight / 7);
	    js.setLayoutSize(screenHeight / 2, screenHeight / 2);
	    js.setLayoutAlpha(50);
	    js.setStickAlpha(255);
	    js.setOffset((int)((screenHeight / 9) * 0.6));
//	    js.setMinimumDistance((int)((screenHeight / 9) * 0.6));
	    
	    layout_joystick.setOnTouchListener(new OnTouchListener() {
	    	long time = System.currentTimeMillis();
			public boolean onTouch(View arg0, MotionEvent arg1) {
				js.drawStick(arg1);
				if(arg1.getAction() == MotionEvent.ACTION_DOWN) {
					command();
				} else if(arg1.getAction() == MotionEvent.ACTION_MOVE) {
					if(System.currentTimeMillis() - time > 200) {
						command();
						time = System.currentTimeMillis(); 
					}
				} else if(arg1.getAction() == MotionEvent.ACTION_UP) {
					send("SS");
					send("SS");
				}
				return true;
			}
			
			public void command() 
			{	
				int pwm_steering=1500;
				int pwm_speed = 1500 - (int)(250*js.getnormY());		//reverse Y			
				
				if(pwm_speed < 1500)
					pwm_steering = 1500 + (int)(500*js.getnormX());    //reverse X
				else
					pwm_steering = 1500 - (int)(500*js.getnormX());    //reverse X
				
				send("MC/" + pwm_speed + "/" + pwm_steering);
			}
        });
	    
	    
	    //***********************************  added joystick for pan and tilt unit control   *************************************/
	    layout_joystick_PT = (RelativeLayout)findViewById(R.id.layout_joystick2);
	    js_PT = new JoyStickClass(getApplicationContext()
        		, layout_joystick_PT, R.drawable.image_button);
	    js_PT.setStickSize(screenHeight / 7, screenHeight / 7);
	    js_PT.setLayoutSize(screenHeight / 2, screenHeight / 2);
	    js_PT.setLayoutAlpha(50);
	    js_PT.setStickAlpha(255);
	    js_PT.setOffset((int)((screenHeight / 9) * 0.6));
//	    js_PT.setMinimumDistance((int)((screenHeight / 9) * 0.6));
	    
	    layout_joystick_PT.setOnTouchListener(new OnTouchListener() {
	    	long time = System.currentTimeMillis();
			public boolean onTouch(View arg0, MotionEvent arg1) {
				js_PT.drawStick(arg1);
				if(arg1.getAction() == MotionEvent.ACTION_DOWN) {
					command();
				} else if(arg1.getAction() == MotionEvent.ACTION_MOVE) {
//					if(System.currentTimeMillis() - time > 200) {
						command();
//						time = System.currentTimeMillis(); 
//					}
				} else if(arg1.getAction() == MotionEvent.ACTION_UP) {
					send("PT_SS");
					send("PT_SS");
				}
				return true;
			}
			
			public void command() 
			{				
				int pwm_pan = 1500 + (int)(500*js_PT.getnormX());
				int pwm_tilt = 1500 + (int)(500*js_PT.getnormY());
				
//				Log.e("controller", "pwm_pan: " + pwm_pan + " pwm_tilt: " + pwm_tilt);
				
				send("PT/" + pwm_pan + "/" + pwm_tilt);				
			}
        });
	    
	    //******************************************************************************************************************/
	    
	    
	    
	    Button buttonSnap = (Button)findViewById(R.id.btnPhotoSnap);
	    buttonSnap.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
				sendString("Snap");
	    	}
	    });
	    
	    Button buttonFocus = (Button)findViewById(R.id.btnAutoFocus);
	    buttonFocus.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
				sendString("Focus");
	    	}
	    });
        
        cbFlash = (CheckBox)findViewById(R.id.cbFlash);
        cbFlash.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1) {
					sendString("LEDON");
				} else {
					sendString("LEDOFF");
				}
			}
        });
		
		Runnable readThread = new Runnable() {
			Bitmap bitmap;
			
			public void run() {
				try {
					s = new Socket();
					s.connect((new InetSocketAddress(InetAddress.getByName(ip), 21111)), 5000);

					out = s.getOutputStream(); 
					dos = new DataOutputStream(out);

					in = s.getInputStream();
				    dis = new DataInputStream(in);
					sendString(pass);
					
					while(task_state) {
						try {
							int size = dis.readInt();
							final byte[] buff = new byte[size];
							dis.readFully(buff);
							runOnUiThread(new Runnable() {
					    		public void run() {
									if(buff.length > 0 && buff.length < 20) {
										if(new String(buff).equals("Snap")) {
											Toast.makeText(getApplicationContext()
													, "Take a photo"
													, Toast.LENGTH_SHORT).show();		
										} else if(new String(buff).equals("WRONG")) {
											Toast.makeText(getApplicationContext()
							    					, "Wrong Password", Toast.LENGTH_SHORT).show();
											finish();
										} else if(new String(buff).equals("ACCEPT")) {
									    	Toast.makeText(getApplicationContext()
									    			, "Connection accepted"
									    			, Toast.LENGTH_SHORT).show();	
										} else if(new String(buff).equals("NoFlash")) {
											Toast.makeText(getApplicationContext()
									    			, "Device not support flash"
									    			, Toast.LENGTH_SHORT).show();
										}								    		
						    		} else if(buff.length > 20) {
						    			bitmap = BitmapFactory.decodeByteArray(buff , 0, buff.length);
										imageView1.setImageBitmap(bitmap);
						    		}
					    		}
							});
						} catch (EOFException e) {
							runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(getApplicationContext()
											, "Connection Down"
											, Toast.LENGTH_SHORT).show();
								}
							});
							task_state = false;
							finish();
							Log.e(TAG, e.toString());
						} catch (NumberFormatException e) {
							Log.e(TAG, e.toString());
						} catch (UnknownHostException e) {
							Log.e(TAG, e.toString());
						} catch (IOException e) {
							Log.e(TAG, e.toString());
						}
					}
				} catch (NumberFormatException e) {
					Log.e(TAG, e.toString());
				} catch (UnknownHostException e) {
					Log.e(TAG, e.toString());
				} catch (IOException e) {
					Log.e(TAG, e.toString());
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext()
									, "Connection Failed"
									, Toast.LENGTH_SHORT).show();
						}
					});
					finish();
				}
			}
		};
		new Thread(readThread).start();
	}
	
	public void onPause() {
		super.onPause();

		task_state = false;
		try {
			s.close();
			out.close();
			dos.close();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		} catch (NullPointerException e) {
			Log.e(TAG, e.toString());
		}
		
		finish();
	}
	
	public void sendString(String str) {
		try {
			dos.writeInt(str.length());
			dos.write(str.getBytes());
			out.flush();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		} catch (NullPointerException e) {
			Log.e(TAG, e.toString());
		}
	}
	
	public void send(final String str) {
		new Thread(new Runnable() {
			public void run() {
				try {
//					Log.e("controller", "sending..." + str);
					
					DatagramSocket s = new DatagramSocket();
					InetAddress local = InetAddress.getByName(ip);
					DatagramPacket p = new DatagramPacket(str.getBytes(), str.getBytes().length, local, 21111);
					s.send(p);
					s.close();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
	}
}
