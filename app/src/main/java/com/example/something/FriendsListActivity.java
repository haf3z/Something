package com.example.something;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.function.LongToDoubleFunction;

public class FriendsListActivity extends AppCompatActivity {

    static ArrayList<String> friendPhoneList;
    static ArrayList<Location> friendLocationList;
    static ArrayList<String> friendDataList;
    ArrayList<String> dataKeys;

    ArrayList<String> toShowList;

    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        dataKeys = new ArrayList<>();
        friendLocationList = new ArrayList<>();
        friendDataList = new ArrayList<>();
        friendPhoneList = new ArrayList<>();
        toShowList = new ArrayList<>();

        ListView friendListView = findViewById(R.id.friend_list);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, toShowList);
        friendListView.setAdapter(arrayAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("user").child(user.getPhoneNumber()).child("friendlocation");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot child: snapshot.getChildren()) {
                    String key = child.getKey();
                    dataKeys.add(key);
                }
                listLocations();
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("index", position);
                startActivity(intent);
            }
        });
    }

    private void listLocations() {
        for(String key: dataKeys) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("locations").child(key).child("location");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Location location = new Location("");
                    if(snapshot.exists()){
                        String latString =  snapshot.child("lat").getValue().toString();
                        String lonString =  snapshot.child("lon").getValue().toString();
                        double lat = Double.valueOf(latString);
                        double lon = Double.valueOf(lonString);
                        location.setLatitude(lat);
                        location.setLongitude(lon);
                    }
                    friendLocationList.add(location);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            DatabaseReference dateReference = FirebaseDatabase.getInstance().getReference().child("locations").child(key).child("date");
            dateReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String date = "";
                    if(snapshot.exists()){
                        date = snapshot.getValue().toString();
                    }
                    friendDataList.add(date);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            DatabaseReference phoneReference = FirebaseDatabase.getInstance().getReference().child("locations").child(key).child("phone");
            phoneReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String phone = "";
                    if(snapshot.exists()){
                        phone = snapshot.getValue().toString();
                    }

                    friendPhoneList.add(phone);
                    toShowList.add(phone + "\n" + friendDataList.get(friendDataList.size() - 1));
                    arrayAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        arrayAdapter.notifyDataSetChanged();
    }
}
