/*Activity used for handling the listeners during each round*/
package com.game.rememberwhen;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.game.rememberwhen.utilities.FireStoreWorker;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class ListenerActivity<prompt> extends AppCompatActivity {

    private FirebaseFirestore db;
    private DocumentReference docRef;
    private Bundle b;
    private ViewFlipper flipper;
    private Player player;
    private String roomID;
    private int numPlayers;
    private TextView timer;
    private Button voteTrue;
    private Button voteFalse;
    private Button voteNow;
    private Button quitBtn;
    private CountDownTimer cTimer = null;
    private int timeLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = getIntent().getExtras();
        player = (Player) b.getSerializable("player");
        roomID = String.valueOf(player.getRoomId());
        numPlayers = (Integer) (b.get("numPlayers"));

        setContentView(R.layout.listener_flipper);

        flipper = (ViewFlipper) findViewById(R.id.listenerFlipper);
        LayoutInflater factory = LayoutInflater.from(this);
        View firstView = factory.inflate(R.layout.deliberation_listener, null);
        flipper.addView(firstView);

        loadUI();

        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FireStoreWorker().playerQuit(roomID, player);
                startActivity(new Intent(ListenerActivity.this, MainActivity.class));
            }
        });


        voteNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipper.showNext();
                setBtnListeners();
                startTimer();
            }
        });

        db = FirebaseFirestore.getInstance();
        docRef = db.collection("/rooms").document(b.get("roomId").toString());
    }

    //TODO import current prompt, storyteller, and when done telling the story from StorytellerActivity.java
    private void loadUI() {
        timer = findViewById(R.id.timerTextView);
        quitBtn = findViewById(R.id.quitBtn);
        voteNow = findViewById(R.id.voteBtn);
        voteTrue = findViewById(R.id.listenerVoteTrue);
        voteFalse = findViewById(R.id.listenerVoteFalse);
    }

    public void setBtnListeners() {

        voteTrue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cTimer.cancel();
                player.setResponse("truth");
                docRef.update("users", FieldValue.arrayRemove(player.getIndex()));
                player.setIndex(numPlayers - 1);
                docRef.update("users", FieldValue.arrayUnion(player));
                Score.updateScore(b.get("roomId").toString(), player);

                Intent intentLeaderboard = new Intent(ListenerActivity.this, LeaderboardActivity.class);
                intentLeaderboard.putExtras(b);
                startActivity(intentLeaderboard);
            }
        });

        voteFalse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cTimer.cancel();
                // new Score("makeItUp",0);
                player.setResponse("MakeItUp");
                docRef.update("users", FieldValue.arrayRemove(player.getIndex()));
                player.setIndex(numPlayers - 1);
                docRef.update("users", FieldValue.arrayUnion(player));
                Score.updateScore(b.get("roomId").toString(), player);

                Intent intentLeaderboard = new Intent(ListenerActivity.this, LeaderboardActivity.class);
                intentLeaderboard.putExtras(b);
                startActivity(intentLeaderboard);
            }
        });
    }

    private void startTimer() {
        timeLeft = 120;
        cTimer = new CountDownTimer(timeLeft * 1000, 1000) {
            // update timer every second
            public void onTick(long millisUntilFinished) {
                timeLeft = (int) (millisUntilFinished / 1000);
                timer.setText(String.valueOf(timeLeft));
            }

            // end storytelling phase once timer runs out
            public void onFinish() {
                player.setResponse("");
                docRef.update("users", FieldValue.arrayRemove(player.getIndex()));
                player.setIndex(numPlayers - 1);
                docRef.update("users", FieldValue.arrayUnion(player));
                Score.updateScore(b.get("roomId").toString(), player);

                Intent intentLeaderboard = new Intent(ListenerActivity.this, LeaderboardActivity.class);
                intentLeaderboard.putExtras(b);
                startActivity(intentLeaderboard);
            }
        };
        cTimer.start();
    }

    public void openRules(View view) {
        Intent intent = new Intent(this, RulesActivity.class);
        startActivity(intent);
    }
}