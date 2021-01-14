package cz.uhk.lafinder;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et = (EditText)findViewById(R.id.editText);

        Thread myThread = new Thread(new MyServerThread());
        myThread.start();
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
}