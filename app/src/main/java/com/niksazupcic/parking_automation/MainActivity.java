package com.niksazupcic.parking_automation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_SEND_SMS = 100;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;

    private FusedLocationProviderClient fusedLocationClient;

    Map<String, String> zonePhoneMap;

    Button send_button;
    Button findMe_button;
    TextInputEditText registration_textInputEditText;
    AutoCompleteTextView parkingZone_autoCompleteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FindViewsByID();
        GetSharedPreferencesRegistration();
        CheckPermissions();
        FillParkingZoneComboBox();
        MapZoneToPhoneNo();
        DefineSendButton();
        DefineFindMeButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SaveSharedPreferencesRegistration();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SaveSharedPreferencesRegistration();
    }

    private void FillParkingZoneComboBox() {
        String[] parkingZones = getResources().getStringArray(R.array.parking_zone);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                parkingZones);
        parkingZone_autoCompleteTextView.setAdapter(adapter);
    }

    private void FindViewsByID() {
        send_button = findViewById(R.id.send_button);
        findMe_button = findViewById(R.id.findMe_button);
        registration_textInputEditText = findViewById(R.id.registration_textInputEditText);
        parkingZone_autoCompleteTextView = findViewById(R.id.parkingZone_autoCompleteTextView);
    }

    private void DefineSendButton() {
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendDialog(registration_textInputEditText.getText().toString().trim(),
                        parkingZone_autoCompleteTextView.getText().toString().trim());
            }
        });
    }

    private void DefineFindMeButton() {
        findMe_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FindCurrentLocation();
            }
        });
    }

    private void FindCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this,
                    "Location could not be found!", Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        ArrayList<Address> addresses = (ArrayList<Address>) geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1);
                        Toast.makeText(MainActivity.this, "You are at: "
                                + addresses.get(0).getAddressLine(0), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, "Turn on location setting!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String FindPhoneNumber(String parkingZone){
        return zonePhoneMap.get(parkingZone);
    }

    private void CheckPermissions(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS}, PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    private void MapZoneToPhoneNo(){
        zonePhoneMap = new HashMap<>();
        String[] parkingZone = getResources().getStringArray(R.array.parking_zone);
        String[] phoneNumber = getResources().getStringArray(R.array.phone_numbers_zones);
        zonePhoneMap.put(parkingZone[0],  phoneNumber[0]);
        zonePhoneMap.put(parkingZone[1],  phoneNumber[1]);
        zonePhoneMap.put(parkingZone[2],  phoneNumber[8]);
        zonePhoneMap.put(parkingZone[3],  phoneNumber[1]);
        zonePhoneMap.put(parkingZone[4],  phoneNumber[5]);
        zonePhoneMap.put(parkingZone[5],  phoneNumber[9]);
        zonePhoneMap.put(parkingZone[6],  phoneNumber[7]);
        zonePhoneMap.put(parkingZone[7],  phoneNumber[2]);
        zonePhoneMap.put(parkingZone[8],  phoneNumber[4]);
        zonePhoneMap.put(parkingZone[9],  phoneNumber[3]);
        zonePhoneMap.put(parkingZone[10], phoneNumber[6]);
    }

    private void SendDialog(String registration, String parkingZone){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pay parking:");
        if(registration.trim().length() == 0 || parkingZone.trim().length() == 0){
            Toast.makeText(this, "Nothing to send", Toast.LENGTH_SHORT).show();
            return;
        }
        builder.setMessage( registration + "\n\n"
                + "in parking zone: "
                + parkingZone
        );
        builder.setNegativeButton("Discard", (dialog, which)->{
            //do nothing
        });
        builder.setPositiveButton("Pay", (dialog, which)->{
            SendSMS();
        });
        builder.create().show();
    }

    private void SendSMS(){
        SmsManager smsManager = SmsManager.getDefault();
        String parkingZone = parkingZone_autoCompleteTextView.getText().toString().trim();
        String phoneNumber = FindPhoneNumber(parkingZone);
        String registrationText = registration_textInputEditText.getText().toString().trim();

        //return if no phone or registration
        if(phoneNumber.toString().isEmpty()
                || phoneNumber.toString().length() == 0
                || registrationText.toString().isEmpty()
                || registrationText.toString().length() == 0
        ) return;

        smsManager.sendTextMessage(phoneNumber,
                null, registrationText, null, null);

        Toast.makeText(getApplicationContext(),
                "Parking payed for " + parkingZone + ".",
                Toast.LENGTH_LONG).show();
    }

    private void SaveSharedPreferencesRegistration(){
        SharedPreferences preferences = getApplicationContext().
                getSharedPreferences("registration", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("registrationNumber",
                registration_textInputEditText.getText().toString().trim());
        editor.commit();
    }

    private void GetSharedPreferencesRegistration(){
        SharedPreferences preferences = getApplicationContext().
                getSharedPreferences("registration", MODE_PRIVATE);
        String registrationNumber = preferences.getString("registrationNumber", "");
        if(registrationNumber.trim().length() == 0){
            registrationNumber = "ZG1234AB";
        }
        registration_textInputEditText.setText(registrationNumber);
    }
}