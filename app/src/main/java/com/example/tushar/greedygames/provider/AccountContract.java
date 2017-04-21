package com.example.tushar.greedygames.provider;

import com.example.tushar.greedygames.BuildConfig;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by tushar on 19/4/17.
 */

public class AccountContract {
    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID;
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_LOCATIONS = "accounts";

    public interface LocationColumns {
        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
        String TIME_STAMP = "time_stamp";
    }

    public static class Locations implements LocationColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATIONS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.bizom.account";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.bizom.account";

        public static Uri buildAccountUri(String accountId) {
            return CONTENT_URI.buildUpon().appendPath(accountId).build();
        }


        public static String getAccountId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
