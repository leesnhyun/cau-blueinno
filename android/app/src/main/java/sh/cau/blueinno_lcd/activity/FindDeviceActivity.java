package sh.cau.blueinno_lcd.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sh.cau.blueinno_lcd.R;
import sh.cau.blueinno_lcd.service.BLEService;

public class FindDeviceActivity extends AppCompatActivity {
    
    public static final String TAG = "FindDeviceActivity";
    private ListView mMenuList = null;
    private ArrayAdapter<String> mAdapter = null;

    private final int ENABLE_BT = 1;

    private final Messenger mMessenger;
    private Intent mServiceIntent;
    private Messenger mService = null;
    private BLEService.State mState = BLEService.State.UNKNOWN;
    private ProgressDialog mProgressDialog;
    
    public static FindDeviceActivity findDeviceActivity = null;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);

            try {
                Message msg = Message.obtain(null, BLEService.MSG_REGISTER);
                if (msg != null) {
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                    startScan();
                } else {
                    mService = null;
                }

            } catch (Exception e) {
                Log.w(TAG, "Error connecting to BLEService", e);
                mService = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private static class IncomingHandler extends Handler {
        private final WeakReference<FindDeviceActivity> mActivity;

        public IncomingHandler(FindDeviceActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            FindDeviceActivity activity = mActivity.get();
            activity.closeProgress();
            if (activity != null) {
                switch (msg.what) {
                    case BLEService.MSG_STATE_CHANGED:
                        activity.stateChanged(BLEService.State.values()[msg.arg1]);
                        break;
                    case BLEService.MSG_DEVICE_FOUND:
                        Bundle data = msg.getData();
                        if (data != null && data.containsKey(BLEService.KEY_MAC_ADDRESSES)) {
                            activity.setDevices(activity, data.getStringArray(BLEService.KEY_MAC_ADDRESSES));
                        }
                        break;
                    case BLEService.MSG_DEVICE_DATA:
/*						float temperature = msg.arg1 / 100f;
						float humidity = msg.arg2 / 100f;
						activity.mDisplay.setData(temperature, humidity);*/
                        break;
                }
            }
            super.handleMessage(msg);
        }
    }


    public FindDeviceActivity() {
        super();
        mMessenger = new Messenger(new IncomingHandler(this));
    }


    @Override
    protected void onStart() {
        super.onStart();
        bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_device);

        mServiceIntent = new Intent(this, BLEService.class);
        mMenuList = (ListView)findViewById(R.id.device_list);
        FindDeviceActivity.findDeviceActivity = this;

        //startScan();
    }


    @Override
    protected void onResume() {
        //startScan();
        super.onResume();
    }


    private void openProgress(){
        mProgressDialog = new ProgressDialog(FindDeviceActivity.this);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();
    }


    public void closeProgress(){
        if(mProgressDialog!=null&&mProgressDialog.isShowing()){
            //mProgressDialog.dismiss();
        }
    }

    private void stateChanged(BLEService.State newState) {
        boolean disconnected = mState == BLEService.State.CONNECTED;
        mState = newState;
        switch (mState) {
            case SCANNING:
//				mDeviceList.setScanning(true);
                if( mProgressDialog != null ){
                    mProgressDialog.setMessage("SCANNING");
                }
                break;
            case BLUETOOTH_OFF:
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, ENABLE_BT);
                if( mProgressDialog != null ){
                    mProgressDialog.setMessage("BLUETOOTH_OFF");
                }
                break;
            case IDLE:
                if (disconnected) {
//					FragmentTransaction tx = getFragmentManager().beginTransaction();
//					tx.replace(R.id.main_content, mDeviceList);
//					tx.commit();
                    if( mProgressDialog != null ){
                        mProgressDialog.setMessage("IDLE");
                    }
                }
//				mDeviceList.setScanning(false);
                break;
            case CONNECTED:
//				FragmentTransaction tx = getFragmentManager().beginTransaction();
//				tx.replace(R.id.main_content, mDisplay);
//				tx.commit();
                if( mProgressDialog != null ){
                    mProgressDialog.setMessage("CONNECTED");
                }
                Toast.makeText(FindDeviceActivity.this, "연결되었습니다.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startScan();
            } else {
                //The user has elected not to turn on
                //Bluetooth. There's nothing we can do
                //without it, so let's finish().
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startScan() {
        //mDeviceList.setDevices(this, null);
        //mDeviceList.setScanning(true);
        Message msg = Message.obtain(null, BLEService.MSG_START_SCAN);
        if (msg != null) {
            try {
                openProgress();
                mService.send(msg);
            } catch (RemoteException e) {
                //closeProgress();
                Log.w(TAG, "Lost connection to service", e);
                unbindService(mConnection);
            }
        }
    }

    public void setDevices(Context context, String[] devices) {
        mProgressDialog.setMessage("setDevices");
        mProgressDialog.dismiss();
        mAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);

        List<Map<String, String>> items = new ArrayList<Map<String, String>>();
        if (devices != null) {
            for (String device : devices) {
                mAdapter.add(device.toString());
            }
        }

        mMenuList.setAdapter(mAdapter);
        mMenuList.setOnItemClickListener( new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                onDeviceListFragmentInteraction(mAdapter.getItem(position).toString());
            }
        });

        mMenuList.setOnItemSelectedListener( new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                onDeviceListFragmentInteraction(mAdapter.getItem(arg2).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
    }

    public void OnDataChangeListener(byte[] val) {
        Message msg = Message.obtain(null, BLEService.MSG_DEVICE_WRITE);
        if (msg != null) {
            msg.obj = val;
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG, "Lost connection to service", e);
                unbindService(mConnection);
            }
        }
    }

    public void OnDataChangeListener(String val) {
        Message msg = Message.obtain(null, BLEService.MSG_DEVICE_WRITE);
        if (msg != null) {
            msg.obj = val;
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG, "Lost connection to service", e);
                unbindService(mConnection);
            }
        }
    }


    public void onDeviceListFragmentInteraction(String macAddress) {
        Message msg = Message.obtain(null, BLEService.MSG_DEVICE_CONNECT);
        Toast.makeText(FindDeviceActivity.this, macAddress + "로 연결", Toast.LENGTH_SHORT);
        if (msg != null) {
            msg.obj = macAddress;
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG, "Lost connection to service", e);
                unbindService(mConnection);
            }
        }

        msg = Message.obtain(null, BLEService.MSG_DEVICE_DATA);
        if (msg != null) {
            msg.obj = macAddress;
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG, "Lost connection to service", e);
                unbindService(mConnection);
            }
        }
    }

}
