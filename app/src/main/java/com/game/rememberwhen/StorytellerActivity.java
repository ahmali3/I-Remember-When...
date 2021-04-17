/* Activity used for handling the storyteller during each turn */
package com.game.rememberwhen;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.game.rememberwhen.listeners.PlayerListener;
import com.game.rememberwhen.utilities.FireStoreWorker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class StorytellerActivity extends AppCompatActivity implements PlayerListener {
    static String prompt;
    private final int REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1;
    private FirebaseDatabase database;
    private FirebaseFirestore db;
    private DocumentReference docRef;
    private Player player;
    private String roomID;
    private int numPlayers;
    private final ArrayList<Player> getSelectedUsers = new ArrayList<>();
    private ViewFlipper flipper;
    private TextView promptTextView;
    private TextView promptTextView2;
    private TextView timerTextView;
    private TextView timerTextView1;
    private Button lieButton;
    private Button truthButton;
    private Button buttonDone;
    private Button buttonMoreTime;
    private Button dsFinishBtn;
    private Button quitBtn;
    private int promptCounter = -1;
    private CountDownTimer cTimer = null;
    private int timeLeft;
    private final ArrayList<Prompt> dataset = new ArrayList<Prompt>();

    // Enable listener activity to retrieve prompt
    static String getPrompt() {
        return prompt;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        player = (Player) b.getSerializable("player");
        roomID = String.valueOf(player.getRoomId());
        numPlayers = (Integer) (b.get("numPlayers"));
        getSelectedUsers.addAll((ArrayList<Player>) b.getSerializable("selectedUsersLIST"));
        System.out.println(getSelectedUsers.toString());
        setContentView(R.layout.story_flipper);
        flipper = (ViewFlipper) findViewById(R.id.storyFlipper);
        LayoutInflater factory = LayoutInflater.from(this);
        View firstView = factory.inflate(R.layout.activity_storyteller_talk, null);
        View secondView = factory.inflate(R.layout.deliberation_storyteller, null);
        flipper.addView(firstView);
        flipper.addView(secondView);
        database = FirebaseDatabase.getInstance();
        db = FirebaseFirestore.getInstance();
        docRef = db.collection("/rooms").document(b.get("roomId").toString());

        loadUI();
        loadDataset();

        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FireStoreWorker().playerQuit(roomID, player);
                startActivity(new Intent(StorytellerActivity.this, MainActivity.class));
            }
        });

        View.OnClickListener doneListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cTimer.cancel();
                flipper.showNext();
                startTimer();
                dsFinishBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(StorytellerActivity.this, LeaderboardActivity.class);
                        intent.putExtras(b);
                        startActivity(intent);
                    }
                });
            }
        };

        // When user chooses lie/truth, screen view changes for storyteller to tell story
        View.OnClickListener truthListener = new View.OnClickListener() {
            public void onClick(View view) {
                flipper.showNext();
                buttonMoreTime.setVisibility(View.INVISIBLE);
                StorytellerActivity.this.onMultipleUsersAction(true);
                startTimer(); // begin timer on display
                // change score status
                player.setResponse("truth");
                docRef.update("answer", "truth");
                Score.updateScore(b.get("roomId").toString(), player);

                promptTextView2.setText(prompt);
                buttonDone.setOnClickListener(doneListener);
            }
        };
        View.OnClickListener lieListener = new View.OnClickListener() {
            public void onClick(View view) {
                StorytellerActivity.this.onMultipleUsersAction(true);
                flipper.showNext();
                buttonMoreTime.setVisibility(View.INVISIBLE);
                startTimer(); // begin timer on display
                // change score status
                player.setResponse("MakeItUp");
                docRef.update("answer", "MakeItUp");
                Score.updateScore(b.get("roomId").toString(), player);
                promptTextView2.setText(prompt);
                buttonDone.setOnClickListener(doneListener);
            }
        };

        lieButton.setOnClickListener(lieListener);
        truthButton.setOnClickListener(truthListener);
    }

    private void loadUI() {
        truthButton = (Button) findViewById(R.id.truthButton);
        lieButton = (Button) findViewById(R.id.lieButton);
        timerTextView = (TextView) findViewById(R.id.timerTextView);
        timerTextView1 = (TextView) findViewById(R.id.textViewTimer1);
        promptTextView2 = (TextView) findViewById(R.id.textViewPrompt2);
        buttonDone = (Button) findViewById(R.id.buttonDone);
        buttonMoreTime = (Button) findViewById(R.id.buttonMoreTime);
        dsFinishBtn = (Button) findViewById(R.id.dsFinishBtn);
        quitBtn = (Button) findViewById(R.id.quitBtn);
    }

    private void loadDataset() {
        DatabaseReference myDBRef = database.getReference().child("db").child("prompts");
        // Read from database
        myDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataset.size() != 0) {
                    dataset.clear();
                }
                for (DataSnapshot promptSnapshot : dataSnapshot.getChildren()) {
                    Prompt prompt = promptSnapshot.getValue(Prompt.class);
                    dataset.add(prompt);
                }
                randomizePrompts();
                showPrompt();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("FIREBASE", "Failed to read value.", error.toException());
            }
        });
    }

    // Randomize order of prompts for each new game room
    private void randomizePrompts() {
        Collections.shuffle(dataset);
    }

    // increment prompt number and display new prompt
    private void showPrompt() {
        promptTextView = findViewById(R.id.textViewPrompt); // put in loadUI method?
        prompt = dataset.get(++promptCounter).prompt;
        promptTextView.setText(prompt);
    }

    public void skipPrompt(View view) {
        prompt = dataset.get(++promptCounter).prompt;
        promptTextView.setText(prompt);
    }

    private void startTimer() {
        timeLeft = 120;
        cTimer = new CountDownTimer(timeLeft * 1000, 1000) {
            // update timer every second
            public void onTick(long millisUntilFinished) {
                timeLeft = (int) (millisUntilFinished / 1000);
                timerTextView1.setText(String.valueOf(timeLeft));
                timerTextView.setText(String.valueOf(timeLeft));
                // display option for More Time when time left is under 10 seconds
                if (timeLeft <= 10) {
                    buttonMoreTime.setVisibility(View.VISIBLE);
                    buttonMoreTime.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            timeLeft += 30;
                            buttonMoreTime.setVisibility(View.INVISIBLE);
                            cTimer.cancel();
                            continueTimer(timeLeft);
                        }
                    });
                }
            }

            // end storytelling phase once timer runs out
            public void onFinish() {
                if (flipper.getDisplayedChild() == 1) {
                    buttonDone.performClick();
                } else {
                    dsFinishBtn.performClick();
                }
            }
        };
        cTimer.start();
    }

    private void continueTimer(int timeLeftPlus) {
        timeLeft = timeLeftPlus;
        cTimer = new CountDownTimer(timeLeft * 1000, 1000) {
            // update timer every second
            public void onTick(long millisUntilFinished) {
                timeLeft = (int) (millisUntilFinished / 1000);
                timerTextView1.setText(String.valueOf(timeLeft));
                timerTextView.setText(String.valueOf(timeLeft));
            }

            // end storytelling phase once timer runs out
            public void onFinish() {
                if (flipper.getDisplayedChild() == 1) {
                    buttonDone.performClick();
                } else {
                    dsFinishBtn.performClick();
                }
            }
        };
        cTimer.start();
    }

    // function called when 'Rules' button pressed (onClick in .xml)
    public void openRules(View view) {
        Intent intent = new Intent(this, RulesActivity.class);
        startActivity(intent);
    }

    @Override
    public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {
        if (isMultipleUsersSelected) {
            Intent intent = new Intent(getApplicationContext(), OutgoingInvitationActivity.class);
            intent.putExtra("user", new Gson().toJson(player, Player.class));
            Bundle bundle = intent.getExtras();
            bundle.putSerializable("selectedUsersLIST", (Serializable) getSelectedUsers);
            bundle.putString("type", "video");
            bundle.putString("token", player.getToken());
            bundle.putBoolean("isMultiple", true);
            bundle.putSerializable("user", (Serializable) player);

            intent.putExtras(bundle);
            intent.putExtra("selectedUsers", new Gson().toJson(getSelectedUsers));
            intent.putExtra("type", "video");
            intent.putExtra("isMultiple", true);
            startActivity(intent);
        }
    }

    private void checkForBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                new AlertDialog.Builder(StorytellerActivity.this)
                        .setTitle("Warning")
                        .setMessage("Battery optimization is enabled. It can interrupt running background services")
                        .setPositiveButton("Disable", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                            startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATIONS);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS) {
            checkForBatteryOptimizations();
        }
    }
}
