package abr.teleop;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class IOIOService extends AsyncTask<Void, Void, Void> {
	private static final String TAG = "CameraRobot-IOIOService";

	public static final int MESSAGE_UPDATE = 0;
	public static final int MESSAGE_CLOSE = 1;
	public static final int MESSAGE_TOAST = 2;
	public static final int MESSAGE_PASS = 3;
	public static final int MESSAGE_WRONG = 4;
	public static final int MESSAGE_DISCONNECTED = 5;

	public static final int MESSAGE_FLASH = 6;
	public static final int MESSAGE_SNAP = 7;
	public static final int MESSAGE_FOCUS = 8;

	public static final int MESSAGE_STOP = 10;
	public static final int MESSAGE_MOVE = 11;

	public static final int MESSAGE_PT_STOP = 19;
	public static final int MESSAGE_PT_MOVE = 20;	

	Boolean TASK_STATE = true;
	ServerSocket ss;
	ImageView mImageView;
	Context mContext;
	Bitmap bitmap;
	String mPassword;

	int count = 0;
	byte[] data;
	Socket s;
	BufferedWriter out;
	DataInputStream dis;
	InputStream in ;
	Handler mHandler;

	public IOIOService(Context context, Handler handler, String password) {
		mContext = context;
		mHandler = handler;
		mPassword = password;
	}

	byte[] buff;
	protected Void doInBackground(Void... params) {  

		Runnable run = new Runnable() {
			public void run() {

				try {
					byte[] message = new byte[12];
					DatagramPacket p = new DatagramPacket(message, message.length);
					DatagramSocket s = new DatagramSocket(null);
					s.setReuseAddress(true);
					s.setBroadcast(true);
					s.bind(new InetSocketAddress(21111));

					while(TASK_STATE) {
						try {		
							s.setSoTimeout(3000);
							s.receive(p);
							String text = new String(message, 0, p.getLength());

							//							Log.e("IOIO", "msg received:" + text);

							if(text.substring(0, 2).equals("MC")) 
							{
								String[] array = text.split("/");

								int speed = Integer.parseInt(array[1]);
								int steering = Integer.parseInt(array[2]);

								mHandler.obtainMessage(MESSAGE_MOVE, speed, steering).sendToTarget();								

							} 
							else if(text.substring(0, 2).equals("SS")) {
								mHandler.obtainMessage(MESSAGE_STOP).sendToTarget();
							} else if(text.substring(0, 5).equals("PT_SS")) {
								mHandler.obtainMessage(MESSAGE_PT_STOP).sendToTarget();
							} else
							{
								String[] array = text.split("/");
								if(array[0].equals("PT"))
								{
									int pan = Integer.parseInt(array[1]);
									int tilt = Integer.parseInt(array[2]);

									mHandler.obtainMessage(MESSAGE_PT_MOVE, pan, tilt).sendToTarget();									
								}
							}
						} catch (SocketException e) {
							e.printStackTrace();
						} catch (SocketTimeoutException e) {
							//e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					s.close();
					Log.e(TAG, "Kill Task");
				} catch (SocketException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(run).start();

		try {
			ss = new ServerSocket(21111);
			ss.setSoTimeout(2000);
			Log.w(TAG, "Waiting for connect");
			while(s == null && TASK_STATE) {
				try {
					s = ss.accept();
					s.setSoTimeout(2000);
				} catch (InterruptedIOException e) {
					Log.i(TAG, "Waiting for connect");
				} catch (SocketException e) {
					Log.w(TAG, e.toString());
				}
			}

			if(TASK_STATE) {
				in = s.getInputStream();
				dis = new DataInputStream(in);
				int size = dis.readInt();
				buff = new byte[size];
				dis.readFully(buff);

				if((new String(buff)).equals(mPassword)) {
					mHandler.obtainMessage(MESSAGE_PASS, s).sendToTarget();
				} else {
					mHandler.obtainMessage(MESSAGE_WRONG, s).sendToTarget();
				}
				Log.w(TAG, "Accept");
			}

		} catch (IOException e) {
			Log.w(TAG, e.toString());
		}

		while(TASK_STATE) {
			try {
				int size = dis.readInt();
				buff = new byte[size];
				dis.readFully(buff);
				String data = new String(buff);

				if(data.equals("Snap")) {
					mHandler.obtainMessage(MESSAGE_SNAP).sendToTarget();
				} else if(data.equals("LEDON") || data.equals("LEDOFF")) {
					mHandler.obtainMessage(MESSAGE_FLASH, data).sendToTarget();
				} else if(data.equals("Focus")) {
					mHandler.obtainMessage(MESSAGE_FOCUS).sendToTarget();
				} 
			} catch (EOFException e) { 
				Log.w(TAG, e.toString());
				mHandler.obtainMessage(MESSAGE_CLOSE).sendToTarget();
				break;
			} catch (SocketTimeoutException e) { 
				Log.w(TAG, e.toString());
			} catch (IOException e) { 
				Log.w(TAG, e.toString());
			} 

			if(!s.isConnected()) {
				Log.i(TAG, "Redisconnect");
				mHandler.obtainMessage(MESSAGE_DISCONNECTED).sendToTarget();
			}
		}
		try {

			ss.close();
			s.close();
			in.close();
			dis.close();
		} catch (IOException e) {
			Log.w(TAG, e.toString());
		} catch (NullPointerException e) {
			Log.w(TAG, e.toString());
		}
		Log.e(TAG, "Service was killed");
		return null;
	}

	public void killTask() {
		TASK_STATE = false;
	}
}
