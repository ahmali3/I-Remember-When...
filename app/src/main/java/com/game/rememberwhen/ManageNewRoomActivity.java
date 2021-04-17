/* ManageNewRoomActivity acts as a game room lobby. Host stays while player joins
 Player list gets updated while user joins using Pub-Sub events on Firebase*/
package com.game.rememberwhen;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.game.rememberwhen.utilities.Constants;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ManageNewRoomActivity extends AppCompatActivity {
    private final List playerList = new ArrayList<Player>();
    private Player player;
    private Room room;
    private TextView playersListText;
    private RecyclerView players_list_view; // Player details view dynamic creation using simple player_list_item.xml
    private Bundle b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_new_room);
        b = getIntent().getExtras();
        players_list_view = (RecyclerView) findViewById(R.id.playersListView);
        players_list_view.setLayoutManager(new LinearLayoutManager(this));
        players_list_view.addItemDecoration(new DividerItemDecoration(players_list_view.getContext(), DividerItemDecoration.VERTICAL));
        final PlayersAdapter adapter = new PlayersAdapter(playerList);
        players_list_view.setAdapter(adapter);
        room = new Room(Integer.parseInt(b.get("roomId").toString()), false);
        if (b.getSerializable("playerThis") != null) {
            player = new Gson().fromJson(b.getSerializable("playerThis").toString(), Player.class);
        } else if (b.get("player") != null) {
            // For host
            player = new Gson().fromJson(b.get("player").toString(), Player.class);
        }
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        CollectionReference collection = fStore.collection("/rooms");
        collection.document(String.valueOf(b.get("roomId"))).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            //addSnapshotListener creates Publisher-Subscriber connection to given /rooms/ROOMID path and updates when new data inserted
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(ManageNewRoomActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                }
                if (value.exists()) {
                    ArrayList<Player> playerArrayList = (ArrayList<Player>) value.toObject(PlayerDocument.class).users;
                    // Converts the FireStore data into arrayList
                    playerList.clear();
                    playerList.addAll(playerArrayList);
                    adapter.notifyDataSetChanged(); // By Default adapters work on assigned dataset so when we query firebase it takes some time so we have to tell adapter to update and display new data
                    playersListText = findViewById(R.id.playersList);
                    if (playerList.size() == 6) {
                        playersListText.setText("Players are ready: ");
                    } else {
                        playersListText.setText("Players are Being Added: ");
                    }
                }
            }
        });
        adapter.notifyDataSetChanged();
        try {
            String roomCode = String.valueOf(room.getRoomId());
            TextView displayRoomID = findViewById(R.id.textViewDisplayRoomID);
            displayRoomID.setText(roomCode);
        } catch (Exception e) {
            e.printStackTrace();
            TextView displayRoomID = findViewById(R.id.textViewDisplayRoomID);
            displayRoomID.setText("roomCode not Found");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void readyUp(View view) {
        final Intent intentHost = new Intent(this, StorytellerActivity.class);
        final Intent intentRest = new Intent(this, ListenerActivity.class);
        System.out.println("PLAYER STATUS IS : " + player.getStatus());
        if (player.getStatus().equalsIgnoreCase(Constants.KEY_PLAYER_TYPE_TELLER)) {
            Bundle b = new Bundle();
            b.putSerializable("player", player);
            b.putString("roomId", String.valueOf(this.b.get("roomId")));
            b.putInt("numPlayers", playerList.size());
            b.putSerializable("allPlayers", (Serializable) playerList);
            ArrayList<Player> exportWithoutHost = new ArrayList<>(playerList);
            System.out.println("exportWithoutHost.size() " + exportWithoutHost.size());
            System.out.println("Player Token: " + player.getToken());
            Predicate<Player> isQualified = item -> (item.getToken().equals(player.getToken())); // Predicate to filter host player from list
            exportWithoutHost.stream().filter(isQualified).forEach(p -> System.out.println(p.getName() + " : " + p.token));
            exportWithoutHost.removeIf(isQualified); // removing Host from List // otherwise will send invite to self as well.
            exportWithoutHost.remove(player);
            System.out.println("exportWithoutHost.size() " + exportWithoutHost.size());
            b.putSerializable("selectedUsersLIST", (Serializable) exportWithoutHost);
            intentHost.putExtras(b);
            startActivity(intentHost);
        } else {
            b.putSerializable("player", player);
            b.putInt("numPlayers", playerList.size());
            b.putSerializable("allPlayers", (Serializable) playerList);
            intentRest.putExtras(b);
            startActivity(intentRest);
        }
    }

    public void openRules(View view) {
        final Intent intent = new Intent(this, RulesActivity.class);
        startActivity(intent);
    }

    // Adapters are used to bind dynamic list of data with a static re-usable List or any kind of Custom List views such as player_list_item.xml into
    // Recycler or other view. Requires View Holder,
    class PlayersAdapter extends RecyclerView.Adapter<PlayersViewHolder> {
        private final List<Player> users;

        public PlayersAdapter(List<Player> playerList) {
            super();
            this.users = playerList;
        }

        @NonNull
        @Override
        public PlayersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PlayersViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull PlayersViewHolder holder, int position) {
            holder.bind(this.users.get(position));
        }

        @Override
        public int getItemCount() {
            return this.users.size();
        }
    }

    class PlayersViewHolder extends RecyclerView.ViewHolder {
        private final TextView playerName;

        public PlayersViewHolder(ViewGroup container) {
            super(LayoutInflater.from(ManageNewRoomActivity.this).inflate(R.layout.player_list_item, container, false));
            playerName = (TextView) itemView.findViewById(R.id.playerNameText);
        }

        public void bind(Player playerBinder) {
            playerName.setText(playerBinder.getName());
        }
    }
}
