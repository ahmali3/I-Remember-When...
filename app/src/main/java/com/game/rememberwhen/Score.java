/* Handles each players' scores */
package com.game.rememberwhen;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Score {

    private static final String TAG = "Score";
    private static String answer;
    private static String response;
    private static Player player;

    public Score() {
    }

    public static void updateScore(String room, Player player1) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("/rooms").document(room);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot docSnap = task.getResult();
                    if (docSnap.exists()) {
                        ArrayList<Player> players = ((ArrayList<Player>) docSnap.toObject(PlayerDocument.class).users);
                        int numPlayers = players.size();
                        for (int i = 0; i < numPlayers; i++) {
                            if (players.get(i).getStatus().equals("storyteller")) {
                                answer = docSnap.get("answer").toString();
                            }
                        }
                        for (int i = 0; i < numPlayers; i++) {
                            if (players.get(i).equals(player1)) {
                                player = players.get(i);
                                if (player.getStatus().equals("storyteller")) {
                                    // delay storyteller points update so that other players may process first
                                    try {
                                        TimeUnit.SECONDS.sleep(5);
                                    } catch (InterruptedException ie) {
                                        Thread.currentThread().interrupt();
                                    }
                                    // check responses of each player to calc score of storyteller
                                    for (int j = 0; j < numPlayers - 1; j++) {
                                        // modular addition makes sure to check all players before and after storyteller
                                        response = players.get((j + i) % numPlayers).getResponse();
                                        if (response == null) {
                                            break;
                                        }
                                        if (answer.equals("truth")) {
                                            switch (response) {
                                                case "truth":
                                                    docRef.update("users", FieldValue.arrayRemove((j + i) % numPlayers));
                                                    player.setScore(player.getScore() + 10);
                                                    player.setScoreDif(10);
                                                    player.setIndex(numPlayers - 1);
                                                    docRef.update("users", FieldValue.arrayUnion(player));
                                                    break;

                                                case "MakeItUp":
                                                    docRef.update("users", FieldValue.arrayRemove((j + i) % numPlayers));
                                                    player.setScore(player.getScore() - 10);
                                                    player.setScoreDif(-10);
                                                    player.setIndex(numPlayers - 1);
                                                    docRef.update("users", FieldValue.arrayUnion(player));
                                                    break;


                                            }
                                        } else {
                                            switch (response) {
                                                case "truth":
                                                    docRef.update("users", FieldValue.arrayRemove((j + i) % numPlayers));
                                                    player.setScore(player.getScore() + 20);
                                                    player.setScoreDif(20);
                                                    player.setIndex(numPlayers - 1);
                                                    docRef.update("users", FieldValue.arrayUnion(player));
                                                    break;

                                                case "MakeItUp":
                                                    docRef.update("users", FieldValue.arrayRemove((j + i) % numPlayers));
                                                    player.setScore(player.getScore() - 20);
                                                    player.setScoreDif(-20);
                                                    player.setIndex(numPlayers - 1);
                                                    docRef.update("users", FieldValue.arrayUnion(player));
                                                    break;
                                            }
                                        }
                                    }

                                } else {
                                    response = player.getResponse();
                                    if (response == null) {
                                    } else if (response.equals("")) {
                                        docRef.update("users", FieldValue.arrayRemove(i));
                                        player.setScore(player.getScore() - 10);
                                        player.setScoreDif(-10);
                                        player.setIndex(numPlayers - 1);
                                        docRef.update("users", FieldValue.arrayUnion(player));

                                    } else if (answer.equals("truth")) {
                                        switch (response) {
                                            case "truth":
                                                docRef.update("users", FieldValue.arrayRemove(i));
                                                player.setScore(player.getScore() + 10);
                                                player.setScoreDif(10);
                                                player.setIndex(numPlayers - 1);
                                                docRef.update("users", FieldValue.arrayUnion(player));
                                                break;

                                            case "MakeItUp":
                                                docRef.update("users", FieldValue.arrayRemove(i));
                                                player.setScore(player.getScore() - 10);
                                                player.setScoreDif(-10);
                                                player.setIndex(numPlayers - 1);
                                                docRef.update("users", FieldValue.arrayUnion(player));
                                                break;
                                        }
                                    } else {
                                        switch (response) {
                                            case "truth":
                                                docRef.update("users", FieldValue.arrayRemove(i));
                                                player.setScore(player.getScore() - 5);
                                                player.setScoreDif(-5);
                                                player.setIndex(numPlayers - 1);
                                                docRef.update("users", FieldValue.arrayUnion(player));
                                                break;

                                            case "MakeItUp":
                                                docRef.update("users", FieldValue.arrayRemove(i));
                                                player.setScore(player.getScore() + 10);
                                                player.setScoreDif(10);
                                                player.setIndex(numPlayers - 1);
                                                docRef.update("users", FieldValue.arrayUnion(player));
                                                break;
                                        }
                                    }
                                }
                            }

                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


    }
}