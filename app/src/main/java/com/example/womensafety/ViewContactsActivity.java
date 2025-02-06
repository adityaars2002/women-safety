package com.example.womensafety;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewContactsActivity extends AppCompatActivity {

    private ListView contactsListView;
    private List<String> emergencyContacts;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contacts);

        contactsListView = findViewById(R.id.contacts_list_view);
        sharedPreferences = getSharedPreferences("EmergencyContacts", MODE_PRIVATE);
        emergencyContacts = new ArrayList<>(sharedPreferences.getStringSet("contacts", new HashSet<>()));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, emergencyContacts);
        contactsListView.setAdapter(adapter);
    }
}
