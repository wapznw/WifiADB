package maosui.com.wifiadb;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;  
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;  
import java.io.InputStreamReader;  
import java.io.LineNumberReader;  
import java.io.OutputStreamWriter;

public class MainActivity extends Activity {

    private Button enableAdb;
    private TextView logView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logView = (TextView) findViewById(R.id.logView);

        enableAdb = (Button) findViewById(R.id.enable_wifi_adb_btn);
        enableAdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableWifiAdb(!checkAdb());
                changeState();
            }
        });

        changeState();
    }
    
    void changeState(){
        boolean enabled = checkAdb();
        enableAdb.setText(enabled ? "DISABLE WIFI ADB" : "ENABLE WIFI ADB");
        logView.setText(enabled ? "当前已启用WIFI ADB" : "当前已禁用WIFI ADB");
    }

    boolean enableWifiAdb(boolean enable){
        Process process = null;
        OutputStreamWriter os = null;

        try {
            process = Runtime.getRuntime().exec("su");
            os = new OutputStreamWriter(process.getOutputStream());
            os.write("setprop service.adb.tcp.port " + (enable ? "5555" : "-1") + "\n");
            os.write("stop adbd\n");
            os.write("start adbd\n");
            os.write("exit\n");
            os.flush();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (os != null)os.close();
                if (process != null) {
                    process.destroy();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    boolean checkAdb(){
        Process process = null;
        InputStreamReader in = null;
        try {
            process = Runtime.getRuntime().exec("getprop service.adb.tcp.port");
            in = new InputStreamReader(process.getInputStream());

            LineNumberReader input = new LineNumberReader(in);

            process.waitFor();

            String line = input.readLine();
            if (line != null && !line.trim().equals("-1")){
                return true;
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(in != null){
                try {
                    in.close();
                    process.destroy();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
