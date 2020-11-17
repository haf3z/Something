package com.example.something;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShowContactsActivity extends AppCompatActivity {


    ArrayList<String> emergencyNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_contacts);

        ListView listView = findViewById(R.id.contactListView);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        emergencyNumbers = new ArrayList<>();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, emergencyNumbers);
        listView.setAdapter(arrayAdapter);


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(user.getPhoneNumber()).child("Emergency contacts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child: snapshot.getChildren()) {
                    String phone = child.getKey();
                    Log.i("phone", phone);
                    emergencyNumbers.add(phone);
                    Log.i("list", emergencyNumbers.toString());


                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }
}
