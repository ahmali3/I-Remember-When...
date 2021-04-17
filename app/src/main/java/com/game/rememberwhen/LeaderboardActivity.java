/*Leaderboard activity that displays the players scores at the end of each round*/
package com.game.rememberwhen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.game.rememberwhen.utilities.FireStoreWorker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;

public class LeaderboardActivity extends AppCompatActivity {
    Bundle b;
    private ArrayList<Player> players;
    private ArrayList<Player> players2;
    private Player player;
    private String roomID;
    private TextView mytxt;
    private Button nextBtn;
    private Button quitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leaderboard);
        b = getIntent().getExtras();
        player = (Player) b.getSerializable("player");
        players2 = (ArrayList<Player>) b.getSerializable("allPlayers");
        roomID = String.valueOf(player.getRoomId());

        nextBtn = (Button) findViewById(R.id.nextRoundbutton);
        quitBtn = findViewById(R.id.quitBtn);
        mytxt = findViewById(R.id.mytext);

        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FireStoreWorker().playerQuit(roomID, player);
                startActivity(new Intent(LeaderboardActivity.this, MainActivity.class));
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("/rooms").document(b.get("roomId").toString());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot docSnap = task.getResult();
                    players = ((ArrayList<Player>) docSnap.toObject(PlayerDocument.class).users);
                    Collections.sort(players, Collections.reverseOrder());
                    showList();
                }
            }
        });

        final Intent intentHost = new Intent(this, StorytellerActivity.class);
        final Intent intentRest = new Intent(this, ListenerActivity.class);
        View.OnClickListener nextRoundActivity = new View.OnClickListener() {
            public void onClick(View view) {
                TurnSwitching.TurnSwitch(b, players, player);
                intentHost.putExtras(b);
                startActivity(intentHost);
                if (player.getStatus().equals("storyteller")) {
                    intentHost.putExtras(b);
                    startActivity(intentHost);
                } else {
                    intentRest.putExtras(b);
                    startActivity(intentRest);
                }
            }
        };

        nextBtn.setOnClickListener(nextRoundActivity);
    }

    private void showList() {
        for (Player p : players) {
            mytxt.append(p.toString() + "\n\n");
        }
        for (Player p : players2) {
            mytxt.append(p.toString() + "\n\n");
        }
    }
}
