package cz.uhk.lafinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private EditText et;


    public static final int REQUEST_ACCES_COARSE_LOCATION = 1;
    public static final int REQUEST_ENABLE_BLUETOOTH =11;
    private ListView devicesList;
    private Button blueButton;
    private BluetoothAdapter blue;
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et = (EditText)findViewById(R.id.editText);

        Thread myThread = new Thread(new MyServerThread());
        myThread.start();

        blue = BluetoothAdapter.getDefaultAdapter();
        devicesList = (ListView) findViewById(R.id.devicesList);
        blueButton = (Button) findViewById(R.id.blueButton);

        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        devicesList.setAdapter(listAdapter);

        checkBluetoothState();

        blueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(blue != null && blue.isEnabled()){
                    if(checkCoarseLocaetionPremision()){
                        listAdapter.clear();
                        blue.startDiscovery();
                    }
                }else{
                    checkBluetoothState();
                }

            }
        });
        checkCoarseLocaetionPremision();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(devicesFoundReciver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(devicesFoundReciver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(devicesFoundReciver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(devicesFoundReciver);
    }

    private boolean checkCoarseLocaetionPremision(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
           != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ACCES_COARSE_LOCATION);
            return false;
        }else{
            return true;
        }
    }

    private void checkBluetoothState(){
        if(blue.isEnabled()){
            if(!blue.isDiscovering()){
                blueButton.setEnabled(true);
            }
        }else{
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BLUETOOTH);
        }
    }

    class MyServerThread implements Runnable{

        Socket s;
        ServerSocket ss;
        InputStreamReader isr;
        BufferedReader br;
        Handler h = new Handler();
        String message;

        @Override
        public void run() {
            try  {

                blue = BluetoothAdapter.getDefaultAdapter();
                ss = new ServerSocket(7801);
                while (true){
                    s = ss.accept();
                    isr = new InputStreamReader(s.getInputStream());
                    br = new BufferedReader(isr);
                    message = br.readLine();

                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                        }
                    });




                    //modlimodli

                    BackgroundTask bt = new BackgroundTask();
                    bt.execute(message);

                }

            }
            catch (IOException es){
                es.printStackTrace();
            }

        }
    }

    public void send_data(View v){
        String message = et.getText().toString();
        BackgroundTask bt = new BackgroundTask();
        bt.execute(message);
    }

    class BackgroundTask extends AsyncTask<String,Void,Void>{
        private Socket s;
        private PrintWriter writer;

        @Override
        protected Void doInBackground(String... voids) {

            try {
                String message = voids[0];
                s = new Socket("192.168.0.123", 7800);
                writer = new PrintWriter(s.getOutputStream());
                writer.write(message);
                writer.flush();
                writer.close();
                s.close();

            }catch (IOException e){
                    e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == REQUEST_ENABLE_BLUETOOTH){
            checkBluetoothState();
        }
    }
    /*
    @Override
    protected void onRequestPermissionsResult(int requestCode, @NonNull String[] premissions, @NonNull int[] grandResluts){
        super.onRequestPermissionsResult(requestCode,premissions,grandResluts);
        switch (requestCode){
            case REQUEST_ACCES_COARSE_LOCATION :
                if(grandResluts.length > 0 && grandResluts)
        }
    }*/

    private final BroadcastReceiver devicesFoundReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                listAdapter.add(device.getName() + "\n" + device.getAddress() + "\n" + rssi);
            }
        }
    };


}