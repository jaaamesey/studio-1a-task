package com.group5.charryt.ui;

import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group5.charryt.R;
import com.group5.charryt.data.Listing;
import com.group5.charryt.data.User;

import org.parceler.Parcels;


import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CreateBookingActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Listing listing;

    private DatePicker datePicker;
    private EditText descriptionEt, timeEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_booking_activity);

        // Get view refs
        datePicker = findViewById(R.id.datePicker);
        descriptionEt = findViewById(R.id.descriptionEt);
        timeEt = findViewById(R.id.timeEt);


        // Get listing from parcel in extra data
        listing = Parcels.unwrap(getIntent().getParcelableExtra("listing"));

        // Add back button to action bar (for some reason called the "home" button
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setTitle("Create Booking");

        mAuth = FirebaseAuth.getInstance();

    }
    public void Booking(View v){
        Map<String, Object> bookingInformation = new HashMap<>();

        String timeString = timeEt.getText().toString();
        String[] timeArr = timeString.split(":");

        int hours, minutes;
        try {
            if (timeArr.length != 2)
                throw new NumberFormatException();

            hours = Integer.parseInt(timeArr[0]);
            minutes = Integer.parseInt(timeArr[1]);
            if (hours < 0 || hours > 23)
                throw new NumberFormatException();
            if (minutes < 0 || minutes > 59)
                throw new NumberFormatException();
        }
        catch (NumberFormatException exception) {
            Toast.makeText(getBaseContext(), "Please provide a valid time.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<User> involvedUsers = new ArrayList<>();
        involvedUsers.add(User.getCurrentUser());
        involvedUsers.add(listing.getOwner());

        List<String> involvedUserIds = new ArrayList<>();
        involvedUserIds.add(User.getCurrentUser().getId());
        involvedUserIds.add(listing.getOwner().getId());

        bookingInformation.put("involvedUsers", involvedUsers);
        bookingInformation.put("involvedUserIds", involvedUserIds);
        bookingInformation.put("listing", listing);
        bookingInformation.put("date", new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), hours, minutes).getTime());
        bookingInformation.put("dateCreated", Calendar.getInstance().getTime());
        bookingInformation.put("description", descriptionEt.getText().toString());

        final DocumentReference newDocument = db.collection("bookings").document();

        newDocument.set(bookingInformation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getBaseContext(), "Successfully created booking.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getBaseContext(), "Error creating booking.", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // If back button pressed, close this activity.
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
