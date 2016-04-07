package net.sashag.shoppinglist;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.notifications.Registration;
import com.microsoft.windowsazure.notifications.NotificationsHandler;

public class PushNotificationHandler extends NotificationsHandler {

    private static final int NOTIFICATION_ID = 42;

    @Override
    public void onRegistered(final Context context, String gcmRegistrationId) {
        super.onRegistered(context, gcmRegistrationId);

        Log.i("PushNotificationHandler", "Received GCM registration id: " + gcmRegistrationId);
        MobileServiceClient client = CloudClient.getInstance();
        Futures.addCallback(
                client.getPush().register(gcmRegistrationId, null),
                new FutureCallback<Registration>() {
                    @Override
                    public void onSuccess(Registration result) {
                        Log.i("PushNotificationHandler", "Registered successfully for push notifications, PNS handle: " + result.getPNSHandle());
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("PushNotificationHandler",
                                "Error registering for push notifications", t);
                    }
                }
        );
    }

    @Override
    public void onReceive(Context context, Bundle bundle) {
        NotificationManager manager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        PendingIntent intent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        String title = bundle.getString("title");
        String message = bundle.getString("message");

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(intent)
                .build();
        manager.notify(NOTIFICATION_ID, notification);
    }
}
