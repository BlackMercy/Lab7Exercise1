package th.ac.tu.siit.its333.lab7exercise1;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity {
    int preClick = 0;
    int time = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WeatherTask w = new WeatherTask();
        w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
    }

    public void buttonClicked(View v) {
        long currentTime = System.currentTimeMillis();
        long currentTimeMin = TimeUnit.MILLISECONDS.toMinutes(currentTime);
        int id = v.getId();
        int lastClick = id;
        WeatherTask w = new WeatherTask();
        if (preClick == lastClick) {
            if (currentTimeMin - time >= 1) {
                switch (id) {
                    case R.id.btBangkok:
                        w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
                        break;
                    case R.id.btNon:
                        w.execute("http://ict.siit.tu.ac.th/~cholwich/nonthaburi.json", "Nonthaburi Weather");
                        break;
                    case R.id.btPathum:
                        w.execute("http://ict.siit.tu.ac.th/~cholwich/pathumthani.json", "Pathumthani Weather");
                        break;
                }
                preClick = id;
            }
        } else {
            preClick = id;

            switch (id) {
                case R.id.btBangkok:
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
                    break;
                case R.id.btNon:
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/nonthaburi.json", "Nonthaburi Weather");
                    break;
                case R.id.btPathum:
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/pathumthani.json", "Pathumthani Weather");
                    break;
            }
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

    class WeatherTask extends AsyncTask<String, Void, Boolean> {
        String errorMsg = "";
        ProgressDialog pDialog;
        String title;

        double windSpeed;
        double k_temperature;
        double c_temperature;
        int humidity;
        String weatherCond;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading weather data ...");
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader reader;
            StringBuilder buffer = new StringBuilder();
            String line;
            try {
                title = params[1];
                URL u = new URL(params[0]);
                HttpURLConnection h = (HttpURLConnection) u.openConnection();
                h.setRequestMethod("GET");
                h.setDoInput(true);
                h.connect();

                int response = h.getResponseCode();
                if (response == 200) {
                    reader = new BufferedReader(new InputStreamReader(h.getInputStream()));
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    //Start parsing JSON
                    JSONObject jWeather = new JSONObject(buffer.toString());
                    JSONObject jWind = jWeather.getJSONObject("wind");
                    JSONObject jTemp = jWeather.getJSONObject("main");
                    JSONObject jHumid = jWeather.getJSONObject("main");
                    JSONArray jWeatherCond = jWeather.getJSONArray("weather");
                    JSONObject jWeatherCondObj = jWeatherCond.getJSONObject(0);
                    windSpeed = jWind.getDouble("speed");
                    k_temperature = jTemp.getDouble("temp");
                    humidity = jHumid.getInt("humidity");
                    weatherCond = jWeatherCondObj.getString("main");
                    errorMsg = "";
                    return true;
                } else {
                    errorMsg = "HTTP Error";
                }
            } catch (MalformedURLException e) {
                Log.e("WeatherTask", "URL Error");
                errorMsg = "URL Error";
            } catch (IOException e) {
                Log.e("WeatherTask", "I/O Error");
                errorMsg = "I/O Error";
            } catch (JSONException e) {
                Log.e("WeatherTask", "JSON Error");
                errorMsg = "JSON Error";
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            TextView tvTitle, tvWeather, tvWind, tvTemp, tvHumid;
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            tvTitle = (TextView) findViewById(R.id.tvTitle);
            tvWeather = (TextView) findViewById(R.id.tvWeather);
            tvWind = (TextView) findViewById(R.id.tvWind);
            tvTemp = (TextView) findViewById(R.id.tvTemp);
            tvHumid = (TextView) findViewById(R.id.tvHumid);

            c_temperature = k_temperature - 273.15;

            if (result) {
                tvTitle.setText(title);
                tvWind.setText(String.format("%.1f", windSpeed));
                tvTemp.setText(String.format("%.1f", c_temperature));
                tvHumid.setText(String.format("%d", humidity));
                tvWeather.setText(weatherCond);

            } else {
                tvTitle.setText(errorMsg);
                tvWeather.setText("");
                tvWind.setText("");
                tvTemp.setText("");
                tvHumid.setText("");
            }
        }
    }
}
