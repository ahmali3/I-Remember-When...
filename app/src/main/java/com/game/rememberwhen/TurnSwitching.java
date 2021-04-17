/* Switches players roles when turn is done */
package com.game.rememberwhen;

import android.os.Bundle;

import java.util.ArrayList;

public class TurnSwitching {

    int count = 1, maxRound = 10;

    public TurnSwitching() {
    }

    public static void TurnSwitch(Bundle b, ArrayList<Player> players, Player player) {
        //call this class when one round pass
        //find the storyteller and make it listener, assign storyteller status to the first next person
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getStatus().equals("storyteller")) {
                players.get(i).setStatus("listener");
                players.get((i + 1) % players.size()).setStatus("storyteller");
                i++;
            } else {
                players.get(i).setStatus("listener");
            }
        }
    }
}
