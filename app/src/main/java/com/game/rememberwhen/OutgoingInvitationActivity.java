/* Handles the incoming video call from the host to the players that joined the game*/
package com.game.rememberwhen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.game.rememberwhen.network.ApiClient;
import com.game.rememberwhen.network.ApiService;
import com.game.rememberwhen.utilities.Constants;
import com.game.rememberwhen.utilities.PreferenceManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingInvitationActivity extends AppCompatActivity {

    Player user;
    private PreferenceManager preferenceManager;
    private String inviterToken = null;
    private String meetingRoom = null;
    private String meetingType = null;
    private TextView textFirstChar;
    private TextView textUsername;
    private int rejectionCount = 0;
    private int totalReceivers = 0;
    private final BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String TAG = "invitationResponseReceiver ";
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            Toast.makeText(context, "TYPE IS : " + type, Toast.LENGTH_SHORT).show();
            System.out.println(TAG + "TYPE IS " + type);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                    System.out.println(TAG + "ACCEPTED");
                    Toast.makeText(context, Constants.REMOTE_MSG_INVITATION_ACCEPTED, Toast.LENGTH_SHORT).show();
                    try {
                        URL serverURL = new URL("https://meet.jit.si");

                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverURL);
                        builder.setWelcomePageEnabled(false);
                        builder.setRoom(meetingRoom);
                        if (meetingType.equals("audio")) {
                            builder.setVideoMuted(true);
                        }
                        System.out.println("invitationResponseReceiver");
                        JitsiMeetActivity.launch(OutgoingInvitationActivity.this, builder.build());

                        finish();
                    } catch (Exception exception) {
                        System.out.println(exception);
                        exception.printStackTrace();
                        Toast.makeText(context, exception.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)) {
                    System.out.println(TAG + "REJECTED");
                    rejectionCount += 1;
                    if (rejectionCount == totalReceivers) {
                        Toast.makeText(context, "invitation Rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            } else {
                Toast.makeText(context, "TYPE IS NULL", Toast.LENGTH_SHORT).show();
                System.out.println(TAG + "TYPE IS NULL");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);

        preferenceManager = new PreferenceManager(getApplicationContext());

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(invitationResponseReceiver,
                        new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE));

        ImageView imageMeetingType = findViewById(R.id.imageMeetingType);
        meetingType = getIntent().getStringExtra("type");
//        meetingType = "audio";
        if (meetingType != null) {
            if (meetingType.equals("video")) {
                imageMeetingType.setImageResource(R.drawable.ic_video);
            } else {
                imageMeetingType.setImageResource(R.drawable.ic_audio);
            }
        } else {
            meetingType = "video";
            imageMeetingType.setImageResource(R.drawable.ic_video);

        }

        textFirstChar = (TextView) findViewById(R.id.textFirstChar);
        textUsername = (TextView) findViewById(R.id.textUsername);
//        textEmail = (TextView) findViewById(R.id.textEmail);
        //TODO TEST AND CHECK
        inviterToken = getIntent().getExtras().get("token").toString();
        System.out.println("++++++++++++++++++ inviterToken " + inviterToken);
        System.out.println(getIntent().getExtras());
//        user = (Player) getIntent().getExtras().getSerializable("player");
        user = (Player) getIntent().getSerializableExtra("user");
        meetingRoom = String.valueOf(user.getRoomId());
        if (user != null) {
            textFirstChar.setText(user.getName());
            textUsername.setText(String.format("%s %s", user.getName(), user.getStatus()));
//            textEmail.setText(user.getName());
        }

        ImageView imageStopInvitation = findViewById(R.id.imageStopInvitation);
        imageStopInvitation.setOnClickListener(v -> {
            if (getIntent().getBooleanExtra("isMultiple", false)) {
                Type type = new TypeToken<ArrayList<Player>>() {
                }.getType();
                ArrayList<Player> receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUsers"), type);

                receivers.remove(user);
                cancelInvitation(null, receivers);
            } else {
                if (user != null) {
                    cancelInvitation(user.token, null);
                }
            }
        });

        if (meetingType != null) {
            if (getIntent().getBooleanExtra("isMultiple", false)) {
                Type type = new TypeToken<ArrayList<Player>>() {
                }.getType();
                ArrayList<Player> receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUsers"), type);
                System.out.println("receivers");
                System.out.println(receivers);
                if (receivers != null) {
                    totalReceivers = receivers.size();
                }
                initiateMeeting(meetingType, null, receivers); // old user.token == null
            } else {
                if (user != null) {
                    totalReceivers = 1;
                    initiateMeeting(meetingType, user.token, null);
                }
            }
        }
    }

    private void initiateMeeting(String meetingType, String receiverToken, ArrayList<Player> receivers) {
        try {
            JSONArray tokens = new JSONArray();

            if (receiverToken != null) {
                tokens.put(receiverToken);
            }

            if (receivers != null && receivers.size() > 0) {
                StringBuilder userNames = new StringBuilder();
                for (int i = 0; i < receivers.size(); i++) {
                    tokens.put(receivers.get(i).token);
                    userNames.append(receivers.get(i).getName()).append("\n");
                }
                textFirstChar.setVisibility(View.GONE);
                textUsername.setText(userNames.toString());
            }

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_FIRST_NAME, preferenceManager.getString(Constants.KEY_FIRST_NAME));
            data.put(Constants.KEY_LAST_NAME, preferenceManager.getString(Constants.KEY_LAST_NAME));
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, user.getRoomId());

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);

        } catch (Exception exception) {
            exception.printStackTrace();
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        System.out.println("SEND REMOTE MESSAGE ACTUAL");
        System.out.println(remoteMessageBody);
        ApiClient.getClient().create(ApiService.class)
                .sendRemoteMessage(Constants.getRemoteMessageHeaders(), remoteMessageBody)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                                Toast.makeText(OutgoingInvitationActivity.this, "Invitation sent successfully", Toast.LENGTH_SHORT).show();
                            } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                                Toast.makeText(OutgoingInvitationActivity.this, "Invitation cancelled", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            System.out.println("----SEND RMT MSG ");
                            System.out.println(response);
                            Toast.makeText(OutgoingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        System.out.println("SEND RMT MSG FAILED------");
                        System.out.println(t.getStackTrace());
                        Toast.makeText(OutgoingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void cancelInvitation(String receiverToken, ArrayList<Player> receivers) {
        try {
            JSONArray tokens = new JSONArray();

            if (receiverToken != null) {
                tokens.put(receiverToken);
            }

            if (receivers != null && receivers.size() > 0) {
                for (Player user : receivers) {
                    tokens.put(user.token);
                }
            }

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE);
        } catch (Exception exception) {
            System.out.println(exception);
            exception.printStackTrace();
            Toast.makeText(OutgoingInvitationActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(invitationResponseReceiver);
    }
}