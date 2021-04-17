/* Constants being stored here */
package com.game.rememberwhen.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PLAYER_TYPE_TELLER = "storyteller";

    public static final String KEY_PREFERENCE_NAME = "videoMeetingPreference";

    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";

    public static final String REMOTE_MSG_TYPE = "type";
    public static final String REMOTE_MSG_INVITATION = "invitation";
    public static final String REMOTE_MSG_MEETING_TYPE = "meetingType";
    public static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static final String REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse";
    public static final String REMOTE_MSG_INVITATION_ACCEPTED = "accepted";
    public static final String REMOTE_MSG_INVITATION_REJECTED = "rejected";
    public static final String REMOTE_MSG_INVITATION_CANCELLED = "cancelled";

    public static final String REMOTE_MSG_MEETING_ROOM = "meetingRoom";
    public static final String API_KEY_SERVER = "AAAA4-yn_xU:APA91bFai4Ao5nMaaSR5LENHc1EZufm02_5byyfZEZrt6yprHwBpTJbLFMgDI2L_r3p_kVOy6aO6EE_RXk6Z0QKY1ZSsBLP95O4fnz2pVgmgOcHVyZEbdOCT5lWD01mTcunKtK31tRN7";

    public static HashMap<String, String> getRemoteMessageHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(
                Constants.REMOTE_MSG_AUTHORIZATION,
                "key=" + API_KEY_SERVER
        );
        headers.put(Constants.REMOTE_MSG_CONTENT_TYPE, "application/json");

        return headers;
    }
}
