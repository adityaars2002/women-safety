package com.example.womensafety;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final int SMS_PERMISSION_REQUEST = 2;
    private FusedLocationProviderClient fusedLocationClient;
    private Button sosButton, saveContactsButton, viewContactsButton;
    private EditText contactInput;
    private List<String> emergencyContacts;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sosButton = findViewById(R.id.sos_button);
        saveContactsButton = findViewById(R.id.save_contacts_button);
        viewContactsButton = findViewById(R.id.view_contacts_button);
        contactInput = findViewById(R.id.contact_input);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        sharedPreferences = getSharedPreferences("EmergencyContacts", MODE_PRIVATE);
        emergencyContacts = new ArrayList<>(sharedPreferences.getStringSet("contacts", new HashSet<>()));

        saveContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEmergencyContact();
            }
        });

        viewContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewContactsActivity.class);
                startActivity(intent);
            }
        });

        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSOS();
            }
        });
    }

    private void addEmergencyContact() {
        String contact = contactInput.getText().toString().trim();
        if (!contact.isEmpty() && emergencyContacts.size() < 5) {
            emergencyContacts.add(contact);
            saveContacts();
            Toast.makeText(this, "Contact saved!", Toast.LENGTH_SHORT).show();
            contactInput.setText("");
        } else {
            Toast.makeText(this, "Max 5 contacts allowed or empty input!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveContacts() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> contactSet = new HashSet<>(emergencyContacts);
        editor.putStringSet("contacts", contactSet);
        editor.apply();
    }

    private void sendSOS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            fetchLocationAndSendAlert();
        }
    }

    private void fetchLocationAndSendAlert() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    String locationMessage = "Emergency! My location: " +
                            "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                    sendSMSToEmergencyContacts(locationMessage);
                    Toast.makeText(MainActivity.this, "SOS Sent!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Unable to fetch location!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendSMSToEmergencyContacts(String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST);
        } else {
            for (String contact : emergencyContacts) {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + contact));
                smsIntent.putExtra("sms_body", message);
                startActivity(smsIntent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndSendAlert();
            } else {
                Toast.makeText(this, "Location permission is required for SOS!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == SMS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndSendAlert();
            } else {
                Toast.makeText(this, "SMS permission is required to send alerts!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
