/* Winner activity for end of game */
package com.game.rememberwhen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WinnerActivity extends AppCompatActivity {
    List<Player> list = new ArrayList<>();
    TextView mytxt = findViewById(R.id.mytext);
    private Button retryBtn;
    private Button quit;

    public WinnerActivity(List<Player> playerList) {
        this.list = playerList;
        showList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.winner_page);
        Collections.sort(list, Collections.reverseOrder());
        retryBtn = (Button) findViewById(R.id.retry);
        quit = (Button) findViewById(R.id.quitBtn);
        showList();
        Intent intent = new Intent(this, TurnSwitching.class);
        View.OnClickListener retryActivity = new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(intent);
            }
        };
        View.OnClickListener quitActivity = new View.OnClickListener() {
            public void onClick(View view) {
            }
        };
        retryBtn.setOnClickListener(retryActivity);
        quit.setOnClickListener(quitActivity);
    }

    private void showList() {
        for (Player p : list) {
            mytxt.append(p.toString() + "\n\n");
        }
    }
}
