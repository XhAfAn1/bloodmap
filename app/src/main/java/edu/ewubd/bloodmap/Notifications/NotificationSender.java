package edu.ewubd.bloodmap.Notifications;

import android.content.Context;
import android.util.Log;
import com.google.auth.oauth2.GoogleCredentials;
import edu.ewubd.bloodmap.R;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class NotificationSender {

    private static final String FCM_V1_URL = "https://fcm.googleapis.com/v1/projects/bloodmapewu/messages:send";
    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

    public static void sendNotification(Context context, String targetToken, String title, String body, String transactionId, String senderName) {
        new Thread(() -> {
            try {
                String accessToken = getAccessToken(context);
                if (accessToken == null) {
                    Log.e("NotificationSender", "Failed to obtain Access Token.");
                    return;
                }
                sendFcmMessage(context, accessToken, targetToken, title, body, "NEW_RESPONSE", transactionId, senderName);
            } catch (Exception e) {
                Log.e("NotificationSender", "Error sending FCM notification", e);
            }
        }).start();
    }

    public static void sendPremiumBroadcast(Context context, String targetToken, String bloodGroup, String transactionId) {
        new Thread(() -> {
            try {
                String accessToken = getAccessToken(context);
                if (accessToken == null) return;
                String title = "🩸 Urgent " + bloodGroup + " Blood Needed!";
                String body = "A premium user urgently needs " + bloodGroup + " blood. Tap to respond.";
                sendFcmMessage(context, accessToken, targetToken, title, body, "PREMIUM_REQUEST", transactionId, bloodGroup);
            } catch (Exception e) {
                Log.e("NotificationSender", "Error sending premium broadcast", e);
            }
        }).start();
    }

    private static void sendFcmMessage(Context context, String accessToken, String targetToken, String title, String body, String type, String transactionId, String extra) throws Exception {
        URL url = new URL(FCM_V1_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JSONObject message = new JSONObject();
        message.put("token", targetToken);

        JSONObject notification = new JSONObject();
        notification.put("title", title);
        notification.put("body", body);
        message.put("notification", notification);

        JSONObject data = new JSONObject();
        data.put("type", type);
        data.put("transactionId", transactionId);
        data.put("senderName", extra);
        message.put("data", data);

        JSONObject root = new JSONObject();
        root.put("message", message);

        byte[] out = root.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(out);
        }

        int responseCode = conn.getResponseCode();
        Log.d("NotificationSender", "FCM Response Code: " + responseCode);

        if (responseCode != 200) {
            try (InputStream errorStream = conn.getErrorStream()) {
                if (errorStream != null) {
                    byte[] buffer = new byte[1024];
                    int read = errorStream.read(buffer);
                    Log.e("NotificationSender", "FCM Error: " + new String(buffer, 0, read));
                }
            }
        }
    }

    private static String getAccessToken(Context context) {
        try {
            // Load service_account.json from res/raw
            InputStream is = context.getResources().openRawResource(R.raw.service_account);
            GoogleCredentials credentials = GoogleCredentials.fromStream(is)
                    .createScoped(Collections.singletonList(MESSAGING_SCOPE));
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        } catch (Exception e) {
            Log.e("NotificationSender", "Error getting access token from service_account.json", e);
            return null;
        }
    }
}
