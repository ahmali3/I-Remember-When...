/* Main Entry point / Activity for Application */
package com.game.rememberwhen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.game.rememberwhen.utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    Player player;
    String tokenHolder;
    Bundle b; // Data Transfer utility between two android activities

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Firebase token failed", Toast.LENGTH_SHORT).show();
                }
                tokenHolder = task.getResult();
                System.out.println("TOKEN FCM : " + tokenHolder);
            }
        });
        setContentView(R.layout.activity_main);
        b = getIntent().getExtras(); // Getting default bundle
    }

    // On Click CreateRoom Method
    public void createRoom(View view) {
        // Go to CreateRoom Activity
        EditText usernameText = findViewById(R.id.editTextTextPersonName);
        String userName = usernameText.getText().toString();
        // Initial Score 0; room creator will start the game as a storyteller
        if (tokenHolder == null) {
            tokenHolder = "--Null--";
        }
        player = new Player(userName, 0, 0, 0, Constants.KEY_PLAYER_TYPE_TELLER, tokenHolder); // HOST AS Story TELLER// Initial Score 0

        final Room room = new Room();
        player.setRoomId(room.getRoomId());
        CollectionReference fireStore = FirebaseFirestore.getInstance().collection("/rooms"); // FireStore root node collection reference
        DocumentReference path = fireStore.document(String.valueOf(room.getRoomId())); // Creating Document node with RoomId
        // It looks like this
        // /rooms/RandomRoomId
        ArrayList arrayList = new ArrayList<Player>(); // [{PlayerObject},{PlayerObject}]
        HashMap hm = new HashMap<String, ArrayList<Player>>();
        // Firestore works on Key-Value so need HashMap to map {"users":[{PlayerObject},{PlayerObject}]}
        hm.put("users", arrayList);
        arrayList.add(player);
        fireStore.document(String.valueOf(room.getRoomId())).set(hm) // Saving new room with host Player
                .addOnSuccessListener(new OnSuccessListener<Void>() { // Async Wait for Firebase to respond
                    @Override
                    public void onSuccess(Void v) {
                        System.out.println("Room Created with id " + room.getRoomId());
                        Bundle b = new Bundle();
                        b.putSerializable("player", new Gson().toJson(player));
                        b.putString("roomId", String.valueOf(room.getRoomId()));
                        Intent intent = new Intent(MainActivity.this, ManageNewRoomActivity.class);
                        intent.putExtras(b);
                        startActivity(intent); // Transition to ManageNewRoomActivity.class/layout
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });

    }

    // Join Room Button Click
    public void joinRoom(View view) {
        EditText usernameText = findViewById(R.id.editTextTextPersonName);
        String userName = usernameText.getText().toString();
        if (b == null) {
            b = new Bundle();
        }
        if (tokenHolder != null) {
            b.putString("token", tokenHolder);
        }
        b.putString("userName", userName);

        Intent intent = new Intent(this, JoinRoomActivity.class);
        intent.putExtras(b);
        startActivity(intent); // Passing Username to JoinRoomActivity.class
    }
}