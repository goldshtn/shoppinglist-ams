package net.sashag.shoppinglist;

import android.content.Context;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

import java.net.MalformedURLException;

public class CloudClient {

    private static final String MOBILE_SERVICE_URL = "https://shoppyservice.azure-mobile.net";

    private static MobileServiceClient client;

    public static MobileServiceClient initClient(Context context) throws MalformedURLException {
        client = new MobileServiceClient(MOBILE_SERVICE_URL, null, context);
        return client;
    }

    public static MobileServiceClient getInstance() {
        return client;
    }

}
