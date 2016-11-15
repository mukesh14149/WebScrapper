package com.example.mg.webscrapper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity{
    TextView textView;
    String result;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("Result", result);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textView=(TextView)findViewById(R.id.text);

        if(savedInstanceState!=null) {
            super.onRestoreInstanceState(savedInstanceState);
            result=savedInstanceState.getString("Result");
            textView.setText(result.toString());
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isOnline())
                    new Fetch_Data(new AsyncResponse() {
                        @Override
                        public void processFinish(String output) {
                            result=output;
                            textView.setText(output);
                        }
                    }).execute();
                else
                    Snackbar.make(view, "No Internet", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public boolean isOnline(){
        ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected())
            return true;
        else
            return false;
    }



    public class Fetch_Data extends AsyncTask<Void,Void,String>{
        public AsyncResponse delegate = null;

        HttpURLConnection connection;
        InputStream inputStream;
        BufferedReader reader;
        String line;

        public Fetch_Data(AsyncResponse delegate){
            this.delegate=delegate;
        }

        public String fetchstring(){
            try {
                URL url = new URL("https://www.iiitd.ac.in/about");
                connection=(HttpURLConnection)url.openConnection();

                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                inputStream=connection.getInputStream();
                reader=new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));

                StringBuffer buffer=new StringBuffer();
                while((line=reader.readLine())!=null){
                    if(line.contains("jQuery.extend")||line.contains("var _gaq")||line.contains("//--><!]]>"))
                        buffer.append("\n");
                    else
                        buffer.append(line+"\n");
                }
                System.out.println(buffer.toString());

                if(Build.VERSION.SDK_INT>=24)
                    return Html.fromHtml(new String(buffer),Html.FROM_HTML_MODE_LEGACY).toString();
                else
                    return Html.fromHtml(new String(buffer)).toString();

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (connection != null)
                    connection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                }
            }
            return null;
        }

        @Override
        protected String doInBackground(Void... voids) {
            return fetchstring();

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            delegate.processFinish(s.toString());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
