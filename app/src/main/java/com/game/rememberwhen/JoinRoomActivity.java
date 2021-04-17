/* Join Room Layout Activity class*/
package com.game.rememberwhen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class JoinRoomActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);
        // Get the intent that started this activity
        Intent intent = getIntent();
        Bundle b = getIntent().getExtras();
        String userName = b.get("userName").toString(); // Parsing userName from MainActivity.
        String token = b.getString("token");
        player = new Player(userName, 0, 0, "listener", token); // all players joining room will start game as listeners
    }

    // Check the code given by user is valid, connect user to open room
    // Try to find given room Id on Firebase if true transfer user to ManageNewRoomActivity as LobbyRoom

    public void findRoom(View view) {
        Toast.makeText(this, "FINDING ROOM", Toast.LENGTH_LONG).show();
        System.out.println("++++++++++++++++++++++++++++++ 1");
        /*lines below will get code from user, check if it is valid and send user to the matching room */
        final Intent intent = new Intent(this, ManageNewRoomActivity.class);
        EditText editText = findViewById(R.id.JoiningRoomID);
        final String userEnteredID = editText.getText().toString();
        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = fs.collection("/rooms");
        collectionReference.document(userEnteredID).get().addOnCompleteListener((new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    final HashMap<String, Object> roomData = (HashMap<String, Object>) document.getData();
                    if (roomData != null && !roomData.isEmpty()) {
                        // If RoomId available to join than add new player to List and update firebase
                        ArrayList<Player> arrayList = (ArrayList<Player>) roomData.get("users");
                        System.out.println("arrayList.size()" + arrayList.size());
                        if (arrayList.size() >= 6) {
                            Toast.makeText(JoinRoomActivity.this, "Room is full for ID "
                                    + userEnteredID + " Please make new room", Toast.LENGTH_LONG).show();
                            finish();

                        } else {
                            Toast.makeText(JoinRoomActivity.this, "Room ID Valid for Room: " + userEnteredID, Toast.LENGTH_LONG).show();
                            player.setRoomId(Integer.parseInt(userEnteredID));
                            player.setIndex(arrayList.size());
                            arrayList.add(player);
                            roomData.clear();
                            roomData.put("users", arrayList);
                            collectionReference.document(userEnteredID).set(roomData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(JoinRoomActivity.this, "Users updated for Room: " + userEnteredID, Toast.LENGTH_LONG).show();
                                    Bundle b = getIntent().getExtras();
//                                System.out.println(roomData);
                                    b.putString("roomId", userEnteredID);
                                    b.putSerializable("playerThis", new Gson().toJson(player, Player.class));
                                    b.putSerializable("users", new Gson().toJson(roomData));
                                    intent.putExtras(b);
                                    startActivity(intent);
                                }
                            });
                        }
                    } else {
                        Toast.makeText(JoinRoomActivity.this, "Room ID Invalid for Room: " + userEnteredID, Toast.LENGTH_LONG).show();
                    }
                }
            }
        }));
    }
}