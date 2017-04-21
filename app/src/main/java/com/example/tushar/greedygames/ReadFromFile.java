package com.example.tushar.greedygames;

import com.google.gson.Gson;

import com.example.tushar.greedygames.provider.AccountContract;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by tushar on 18/4/17.
 */

public class ReadFromFile {
    private static final String TAG = "ReadFromFile";
    private ArrayList<Data> mDataList;
    private LocationListener mListener;

    public ReadFromFile(LocationListener listener) {
        mListener = listener;
    }

    public ArrayList<Data> getDataList() {
        return mDataList;
    }

    public void setDataList(ArrayList<Data> dataList) {
        mDataList = dataList;
    }

    public void readFromAssets(Context context, String filename) throws IOException {
        mDataList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open(filename)));
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        String mLine = reader.readLine();
        Log.d(TAG, "start");
        int i = 0;
        while (mLine != null) {
            String[] address = mLine.split(" ");
            Data data = new Data();
            data.setLatitude(Double.parseDouble(address[0]));
            data.setLongitude(Double.parseDouble(address[1]));
            mDataList.add(data);
            ContentValues values = new ContentValues();
            values.put(AccountContract.LocationColumns.LATITUDE, address[0]);
            values.put(AccountContract.LocationColumns.LONGITUDE, address[1]);
            String time = address[3].replace(":", ".");
            String[] times = time.split("\\.");
            int timeFinal = Integer.parseInt(times[2]);
            values.put(AccountContract.LocationColumns.TIME_STAMP, timeFinal);
            ContentProviderOperation.Builder builder = ContentProviderOperation
                    .newInsert(AccountContract.Locations.CONTENT_URI);
            builder.withValues(values);
            batch.add(builder.build());
            mLine = reader.readLine();
        }
        mListener.createHeatmap();
        applyBatch(context, batch);
        Log.d(TAG, "end");
        reader.close();
    }

    protected ContentProviderResult[] applyBatch(Context context,
            ArrayList<ContentProviderOperation> batch) {
        if (batch == null || batch.size() == 0) {
            return null;
        }
        ContentProviderResult[] results = null;
        try {
            results = context.getContentResolver()
                    .applyBatch(AccountContract.CONTENT_AUTHORITY, batch);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        return results;
    }
}
