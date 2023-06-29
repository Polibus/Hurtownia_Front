package com.example.analiza_pogody;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Button buttonNextActivity;
    private EditText citySelection;
    private TextView connectionInternetErrorMainActivity;
    private Spinner spinnerCities;
    private String selectedCityFromSpinner;
    private Button buttonCheckTheWeatherAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonNextActivity = findViewById(R.id.buttonCheckTheWeather);
        citySelection = findViewById(R.id.citySelection);
        connectionInternetErrorMainActivity = findViewById(R.id.connectionInternetErrorMainActivity);
        spinnerCities = findViewById(R.id.spinnerCities);
        buttonCheckTheWeatherAnalysis = findViewById(R.id.buttonCheckTheWeatherAnalysis);

        spinnerCities.setOnItemSelectedListener(this);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.cities));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCities.setAdapter(arrayAdapter);

        loadData();

        buttonNextActivity.setOnClickListener(view -> {
            citySelection = findViewById(R.id.citySelection);
            String city = citySelection.getText().toString();
            saveData(city);

            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            boolean connected = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
            if (!connected) {
                connectionInternetErrorMainActivity.setVisibility(View.VISIBLE);
            } else {
                connectionInternetErrorMainActivity.setVisibility(View.INVISIBLE);

                Intent intent = new Intent(MainActivity.this, WeatherActivity.class);

                intent.putExtra("KEY_CITY_SELECTION", city);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                MainActivity.this.startActivity(intent);
            }
        });

        buttonCheckTheWeatherAnalysis.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SelectionOfParametersForWeatherAnalysisActivity.class);

            intent.putExtra("KEY_CITY_SELECTION", selectedCityFromSpinner);
            MainActivity.this.startActivity(intent);
        });
    }

    private void saveData(String city) {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("CITY_KEY", city);
        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        String city = sharedPreferences.getString("CITY_KEY", "Tarnow");
        citySelection.setText(city);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        selectedCityFromSpinner = getResources().getStringArray(R.array.cities)[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Do nothing
    }
}