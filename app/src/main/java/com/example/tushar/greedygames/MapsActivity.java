package com.example.tushar.greedygames;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.gson.Gson;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.example.tushar.greedygames.provider.AccountContract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private static final String PREF_FIRST_RUN = "pref_first_run";
    static int i = 0;
    HeatmapTileProvider mProvider;
    Handler myHandler = new Handler();
    private GoogleMap mMap;
    private ArrayList<Data> mDataList;
    private ReadFromFile mReadFromFile;
    private CrystalSeekbar mSeekbar;
    private boolean mFirstRun = true;
    private Button mPlay;
    private TextView mTime;
    private HashMap<Integer, ArrayList<LatLng>> mLocationHash;
    private TileOverlay mOverlay;
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mTime.setText("Time : " + i);
            String selection = AccountContract.LocationColumns.TIME_STAMP + "=?";
            String[] args = {"" + i};
            mSeekbar.setPosition(i);
            queryDb(selection, args);
            i++;
            if (i < 60) {
                myHandler.postDelayed(this, 1000);
            } else {
                mSeekbar.setEnabled(true);
            }
            Log.d("Maps", "   : " + i);
        }
    };
    private OnSeekbarChangeListener seekBarListener = new OnSeekbarChangeListener() {
        @Override
        public void valueChanged(Number value) {
            Log.d("seekbar", value + "");
            mTime.setText("Time : " + value);
            if (mLocationHash != null) {
                ArrayList<LatLng> list = mLocationHash.get(value.intValue());
                createHeatmap(list);
            }
        }
    };
    private View.OnClickListener mPlayListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSeekbar.setEnabled(false);
            playHeatmap();
        }
    };

    private static SharedPreferences getSharedPreferences(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean setFirstRun(Context context, boolean
            selectedBeatPosition) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putBoolean(PREF_FIRST_RUN, selectedBeatPosition).commit();
        return true;
    }

    public static boolean getFirstRun(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getBoolean(PREF_FIRST_RUN, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mPlay = (Button) findViewById(R.id.play);
        mTime = (TextView) findViewById(R.id.time);
        mPlay.setOnClickListener(mPlayListner);
        mTime.setText(" Time : 00");
        mSeekbar = (CrystalSeekbar) findViewById(R.id.seekbar);
        mSeekbar.setOnSeekbarChangeListener(seekBarListener);
        mLocationHash = new HashMap<>();
        mapFragment.getMapAsync(this);
        if (getFirstRun(this)) {
            getData();
            setFirstRun(this, false);
        } else {
            mFirstRun = false;
        }
    }

    private void queryDb(String where, String[] args) {
        List<LatLng> locationList = new ArrayList<>();
        ContentResolver contentResolver = this.getContentResolver();
        Cursor cursor = contentResolver
                .query(AccountContract.Locations.CONTENT_URI, null, where, args, null);
        mDataList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String latitude = cursor
                        .getString(cursor.getColumnIndex(AccountContract.Locations.LATITUDE));
                String longitude = cursor.getString(
                        cursor.getColumnIndex(AccountContract.LocationColumns.LONGITUDE));
                int timeStamp = cursor.getInt(
                        cursor.getColumnIndex(AccountContract.LocationColumns.TIME_STAMP));
                LatLng latLng = new LatLng(Double.parseDouble(latitude),
                        Double.parseDouble(longitude));
                locationList.add(latLng);
                if (mLocationHash.get(timeStamp) != null) {
                    ArrayList<LatLng> list = mLocationHash.get(timeStamp);
                    list.add(latLng);
                    mLocationHash.put(timeStamp, list);
                } else {
                    ArrayList<LatLng> list = new ArrayList<>();
                    list.add(latLng);
                    mLocationHash.put(timeStamp, list);
                }
            }
            createHeatmap(locationList);
        }
    }

    private void getData() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                mReadFromFile = new ReadFromFile(MapsActivity.this);
                try {
                    mReadFromFile.readFromAssets(MapsActivity.this, "log.txt");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    @Override
    public void createHeatmap() {
        mDataList = mReadFromFile.getDataList();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<LatLng> loctionList = new ArrayList<>();
                for (int i = 0; i < mDataList.size(); i++) {
                    Data data = mDataList.get(i);
                    double lat = data.getLatitude();
                    double lng = data.getLongitude();
                    loctionList.add(new LatLng(lat, lng));
                }
                createHeatmap(loctionList);
            }
        });
    }

    private void createHeatmap(List<LatLng> list) {
        if (list != null && list.size() == 0) {
            return;
        }
        if (mProvider != null) {
            mProvider.setData(list);
            mOverlay.clearTileCache();
        } else {
            mProvider = new HeatmapTileProvider.Builder()
                    .data(list)
                    .build();
            // Add a tile overlay to the map, using the heat map tile provider.
            mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        }
    }

    private void playHeatmap() {
        myHandler.post(mRunnable);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng bangalore = new LatLng(12.9716, 77.5946);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bangalore));
        if (!mFirstRun) {
            queryDb(null, null);
        }
    }
}
