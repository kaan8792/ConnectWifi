package com.kaan.connectwifi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.net.wifi.WifiConfiguration;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button connectButton, disconnectButton;
    EditText ssidEt, ipAdress, logoutAdress;
    WebView webView;
    String ssid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        ssidEt = (EditText)findViewById(R.id.ssidEt);
        ipAdress = (EditText)findViewById(R.id.ipAdress);
        logoutAdress=(EditText)findViewById(R.id.logoutAdress);
        connectButton = (Button)findViewById(R.id.connectButton);
        disconnectButton = (Button)findViewById(R.id.disconnectButton);
        webView = (WebView)findViewById(R.id.webView1);



        final WifiConfiguration wifiConfig = new WifiConfiguration(); //Wifi ayarlarını tutacak değişken.
        final WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE); //Telefonun wifi servisine erişmek için.


        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ssid = ssidEt.getText().toString();//Ssidyi kullanıcıdan al.
                wifiConfig.SSID = String.format("\"%s\"", ssid); //ssid yi ayarlara ekle
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); //Wifi şifresiz ise yalnızca bu gerekli.

                wifiManager.addNetwork(wifiConfig);//Düzenleyiciye ayarları ekle

                if (!wifiManager.isWifiEnabled()){  //Eğer wifi kapalıysa aç.
                    wifiManager.setWifiEnabled(true);
                    while (!wifiManager.isWifiEnabled()){
                        wifiManager.setWifiEnabled(true);
                    }
                }

                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                for( WifiConfiguration i : list ) {
                    if(i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {

                        wifiManager.disconnect();
                        wifiManager.enableNetwork(i.networkId, true);
                        wifiManager.reconnect();


                        break;
                    }
                }

                String ip = ipAdress.getText().toString(); //Kullanıcının giridiği ipyi al.
                String url = ip ;
                if (!url.startsWith("http://") && !url.startsWith("https://")){ //başında http yoksa ekle.
                    url = "http://" + url ;
                }

                final String finalUrl = url;

                final WebView webView = (WebView)findViewById(R.id.webView1);
                webView.getSettings().setJavaScriptEnabled(true);

                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        while (!isConnected(MainActivity.this)) { //Bekleme yeri
                            //Wait to connect
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        webView.loadUrl(finalUrl);
                    }
                });


            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String logoutA = logoutAdress.getText().toString();//Logout adresini al.
                if (!logoutA.startsWith("http://") && !logoutA.startsWith("https://")){ //başında http yoksa ekle.
                    logoutA = "http://" + logoutA;
                }
                final WebView webView = (WebView)findViewById(R.id.webView1);
                webView.getSettings().setJavaScriptEnabled(true);
                final String finalLogoutA = logoutA;
                webView.loadUrl(finalLogoutA);
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        wifiManager.disconnect();
                        wifiManager.setWifiEnabled(false);

                    }
                });


            }
        });

    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }

}
