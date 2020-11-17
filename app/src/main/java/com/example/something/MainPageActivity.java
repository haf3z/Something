package com.example.something;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MainPageActivity extends AppCompatActivity {

    private Button mLogout, mNotify, mAddNumber, mShowContact, mFriendlist;
    private EditText mNumber;
    FirebaseUser user;

    static ArrayList<String> emergencyNumbers;

    Location myLocation;

    LocationManager locationManager;
    LocationListener locationListener;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        user = FirebaseAuth.getInstance().getCurrentUser();

        OneSignal.startInit(this).init();
        OneSignal.setSubscription(true);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                FirebaseDatabase.getInstance().getReference().child("user").child(user.getPhoneNumber()).child("notificationKey").setValue(userId);
            }
        });
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);

        //new SendNotification("message 1", "heading 1", null);

        mLogout = findViewById(R.id.logout);
        mNumber = findViewById(R.id.emergency_number);
        mAddNumber = findViewById(R.id.add_number);
        mNotify = findViewById(R.id.notify);



        emergencyNumbers = new ArrayList<>();
        /*DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(user.getPhoneNumber()).child("Emergency contacts");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String number;
                    for(DataSnapshot childsnapshot : snapshot.getChildren()) {
                        number = childsnapshot.toString();
                        emergencyNumbers.add(number);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/

        mShowContact = findViewById(R.id.show_emergency_contacts);
        mShowContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ShowContactsActivity.class));
            }
        });


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                myLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        mNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


                final String key = FirebaseDatabase.getInstance().getReference().child("locations").push().getKey();
                FirebaseDatabase.getInstance().getReference().child("user").child(user.getPhoneNumber()).child("mylocation").child(key).setValue(true);

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(user.getPhoneNumber()).child("Emergency contacts");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child: snapshot.getChildren()) {
                            String phone = child.getKey();
                            FirebaseDatabase.getInstance().getReference().child("user").child(phone).child("friendlocation").child(key).setValue(true);
                            emergencyNumbers.add(phone);
                            sendNotification(phone);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                final DatabaseReference mLocationDB = FirebaseDatabase.getInstance().getReference().child("locations").child(key).child("location");
                mLocationDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        do {
                            if (myLocation != null) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("lat", myLocation.getLatitude());
                                map.put("lon", myLocation.getLongitude());
                                mLocationDB.updateChildren(map);
                                Toast.makeText(MainPageActivity.this, "Succesful!!", Toast.LENGTH_SHORT).show();
                            }
                        } while(myLocation == null);
                        myLocation = null;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                FirebaseDatabase.getInstance().getReference().child("locations").child(key).child("phone").setValue(user.getPhoneNumber());
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss-dd/MM/yyyy");
                String date = sdf.format(new Date());
                FirebaseDatabase.getInstance().getReference().child("locations").child(key).child("date").setValue(date);
            }
        });


        mAddNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user").child(user.getPhoneNumber()).child("Emergency contacts").child(mNumber.getText().toString());
                mUserDB.setValue(mNumber.getText().toString());
            }
        });




        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OneSignal.setSubscription(false);
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return;
            }
        });

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0 , 0, locationListener);
        }




        mFriendlist = findViewById(R.id.friend_location);
        mFriendlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), FriendsListActivity.class));
            }
        });
    }

    private void sendNotification(String phone) {
        DatabaseReference notificationReference = FirebaseDatabase.getInstance().getReference().child("user").child(phone).child("notificationKey");
        notificationReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    new SendNotification("Friend in Need", user.getPhoneNumber(), snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
