package com.example.speedtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.opencensus.internal.StringUtil;

import static android.content.Context.LOCATION_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    private View mView;

    private SurfaceView mSurfaceView;

    private LatLng latLng,uploadLatLng = null;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    //Circle circle;
    private Marker marker;
    private static List<Circle> circleList = new ArrayList<>();
    //Button get = findViewById(R.id.get);

    private int curSpeed;
    private String mSpeed;
    //TextView speedTextView = findViewById(R.id.curSpeed);
    private double lat1,lon1,lat2,lon2,r = 6371000;
    private String path1 = null,path2 = null;
    private Map<String, Object> pointArray = null;

    private MapView mMapView;

    private LocationListener locationListener;
    private LocationManager locationManager;

    private Bitmap bitmapMapsattelite;

    private Dialog alartDalaog;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_home, container, false);

        mMapView = (MapView) mView.findViewById(R.id.map2);
        if (mMapView != null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }

//        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
//                .findFragmentById(R.id.map2);
//        assert mapFragment != null;
//        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET,android.Manifest.permission.WRITE_EXTERNAL_STORAGE},PackageManager.PERMISSION_GRANTED);
        } else {
            Log.e("DB", "PERMISSION GRANTED");
        }

//        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);
        ImageView currentLocation = mView.findViewById(R.id.cur_loc_btn);
        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(latLng != null){
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.setPadding(0,0,0,0);
                    }

                }
                catch (SecurityException e){
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error in screen shot", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView zoomIn = mView.findViewById(R.id.zoomIn);
        zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.moveCamera(CameraUpdateFactory.zoomIn());
            }
        });

        ImageView zoomOut = mView.findViewById(R.id.zoomOut);
        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.moveCamera(CameraUpdateFactory.zoomOut());
            }
        });

//        Button screenShotButton = mView.findViewById(R.id.takeScreenShot);
//        screenShotButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                takeScreenShot();
//            }
//        });

        return mView;
    }

    public void takeScreenShot(){

        /*getActivity().getWindow().getDecorView().findViewById(android.R.id.content).setDrawingCacheEnabled(true);
        Bitmap backBitmap = getActivity().getWindow().getDecorView().findViewById(android.R.id.content).getDrawingCache();

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                // Make a snapshot when map's done loading
                mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(Bitmap snapshot) {
                        //bitmapMapsattelite = null;
                        //bitmapMapsattelite = bitmap;
                        //imageView.setImageBitmap(bitmap);
                        //bitmapMapsattelite = null;


                        bitmapMapsattelite = Bitmap.createBitmap(
                                backBitmap.getWidth(), backBitmap.getHeight(),
                                backBitmap.getConfig());
                        Canvas canvas = new Canvas(bitmapMapsattelite);
                        canvas.drawBitmap(snapshot, new Matrix(), null);
                        canvas.drawBitmap(backBitmap, 0, 0, null);
                    }

                });
            }
        });*/


        final GoogleMap.SnapshotReadyCallback snapReadyCallback = new GoogleMap.SnapshotReadyCallback() {
            Bitmap bitmap;
            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                bitmap = snapshot;
                try {
                    //do something with your snapshot

//                    getActivity().getWindow().getDecorView().findViewById(android.R.id.content).setDrawingCacheEnabled(true);
//                    Bitmap backBitmap = getActivity().getWindow().getDecorView().findViewById(android.R.id.content).getDrawingCache();
                    Bitmap backBitmap = getBitmap();

                    bitmapMapsattelite = Bitmap.createBitmap(
                            backBitmap.getWidth(), backBitmap.getHeight(),
                            backBitmap.getConfig());
                    Canvas canvas = new Canvas(bitmapMapsattelite);
                    canvas.drawBitmap(snapshot, new Matrix(), null);
                    canvas.drawBitmap(backBitmap, 0, 0, null);
                    //getActivity().getWindow().getDecorView().findViewById(android.R.id.content).setDrawingCacheEnabled(false);
                    backBitmap = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        GoogleMap.OnMapLoadedCallback mapLoadedCallback = new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.snapshot(snapReadyCallback);
            }
        };
        mMap.setOnMapLoadedCallback(mapLoadedCallback);


        if (bitmapMapsattelite != null) {

            Date date = new Date();
            CharSequence now = android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", date);
            CharSequence charSequence = android.text.format.DateFormat.format("ss",date);

            String fileName = Environment.getExternalStorageDirectory() + "/Speed Tracker/J-" + now + ".jpg";

            File file = new File(fileName);
//        if (!dir.exists())
//            dir.mkdirs();
//        File file = new File(saveFilePath.getAbsolutePath(), fileName);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                FileOutputStream fOut = new FileOutputStream(file);
                bitmapMapsattelite.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
                fOut.flush();
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Toast.makeText(getActivity(),charSequence + "take screenshot successful", Toast.LENGTH_SHORT).show();

            bitmapMapsattelite = null;
        }
        else
            Toast.makeText(getActivity(),"take screenshot failed", Toast.LENGTH_SHORT).show();
    }
    public Bitmap getBitmap(){
        getActivity().getWindow().getDecorView().findViewById(android.R.id.content).setDrawingCacheEnabled(true);
        Bitmap backBitmap = getActivity().getWindow().getDecorView().findViewById(android.R.id.content).getDrawingCache();
        getActivity().getWindow().getDecorView().findViewById(android.R.id.content).setDrawingCacheEnabled(false);
        return backBitmap;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        takeScreenShot();
//        mMap.setMappointArray(GoogleMap.MAP_pointArray_SATELLITE);

//        LocationListener locationListener;
//        LocationManager locationManager;

        final long MIN_TIME = 1000;
        final float MIN_DIST = 0.1f;
        final TextView speedTextView = mView.findViewById(R.id.curSpeed);

        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                try {
                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);

                    curSpeed = (int) ((location.getSpeed()*3600)/1000);

                    mSpeed = "Speed = "+curSpeed+" km/h"+"\nMaxspeed = 20km/h\nDistance = __m";

                    speedTextView.setText(mSpeed);

                    lat1 = 24.9953032;
                    lon1 = 88.0791923;
                    lat2 = location.getLatitude();
                    lon2 = location.getLongitude();
                    r = 6371000;
                    double distance;

                    // d = acos( sin φ1 ⋅ sin φ2 + cos φ1 ⋅ cos φ2 ⋅ cos Δλ ) ⋅ R

//                    double d = Math.acos((Math.sin(Math.toRadians(lat1))*Math.sin(Math.toRadians(lat2)))
//                            +(Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.cos(Math.toRadians(lon2-lon1))))*r;
//
//                    if(d <= 10){
//                        mSpeed = "Speed = "+curSpeed+" km/h inside \n"+lat2+","+lon2;
//                        speedTextView.setText(mSpeed);
//                    }
//                    else {
//                        mSpeed = "Speed = "+curSpeed+" km/h outside \n"+lat2+","+lon2;
//                        speedTextView.setText(mSpeed);
//                    }


                    String[] stringLatitudeParts = String.valueOf(location.getLatitude()).split("\\.",2);
                    String[] stringLongitudeParts = String.valueOf(location.getLongitude()).split("\\.",2);
                    String newPath1 = stringLatitudeParts[0] + "_" + stringLongitudeParts[0];
                    String newPath2 = stringLatitudeParts[1].substring(0,2) + "_" + stringLongitudeParts[1].substring(0,2);

                    if(pointArray != null && newPath1.equals(path1) && newPath2.equals(path2)){
                        String key;
                        Object speed;
                        double nearDistance = 2000.0;
                        int maxSpeed = 20;

                        for (Map.Entry<String,Object> entry : pointArray.entrySet()){
                            key = entry.getKey();
                            speed = entry.getValue();
                            String latLon[] = key.split(",",2);
                            lat1 = Double.parseDouble(latLon[0]);
                            lon1 = Double.parseDouble(latLon[1]);
                            distance = Math.acos((Math.sin(Math.toRadians(lat1))*Math.sin(Math.toRadians(lat2)))
                                    +(Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.cos(Math.toRadians(lon2-lon1))))*r;

                            if (distance <= nearDistance){
                                nearDistance = distance;
                                maxSpeed = Integer.parseInt(String.valueOf(speed));

                                String nDistance = String.format("%.2f",nearDistance);

                                mSpeed = " Speed = "+curSpeed+" km/h \n Maxspeed = "+maxSpeed+" km/h \n Distance = "+nDistance +" m ";
                                TextView speedTextView = mView.findViewById(R.id.curSpeed);
                                speedTextView.setText(mSpeed);
                            }

                            if (maxSpeed < curSpeed){
                                openDialog(curSpeed,maxSpeed);
                            }
                            else {
                                closeDialog();
                            }
                        }
                    }
                    else {
                        path1 = newPath1;
                        path2 = newPath2;
                        mStore.collection("points")
                                .document(path1).collection("sub_points").document(path2)
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    assert document != null;
                                    if (document.exists()) {
                                        String key = "";
                                        Object speed;
                                        double nearDistance = 2000.0;
                                        int maxSpeed;
                                        if(circleList != null) {
                                            for (Circle circle : circleList) {
                                                circle.remove();
                                            }
                                            circleList.clear();
                                        }

                                        pointArray = null;
                                        pointArray = document.getData();
                                        assert pointArray != null;
                                        for (Map.Entry<String,Object> entry : pointArray.entrySet()){
                                            key = entry.getKey();
                                            speed = entry.getValue();
                                            String latLon[] = key.split(",",2);
                                            lat1 = Double.parseDouble(latLon[0]);
                                            lon1 = Double.parseDouble(latLon[1]);
                                            Circle circle = mMap.addCircle(new CircleOptions()
                                                    .center(new LatLng(lat1,lon1))
                                                    .radius(10).strokeColor(Color.parseColor("#FF0000"))
                                                    .fillColor(Color.parseColor("#3300FF00")).strokeWidth(2f));
                                            circleList.add(circle);
                                        }
                                        Toast.makeText(getContext(),key,Toast.LENGTH_SHORT).show();
                                    } else {
                                        for (Circle circle : circleList) {
                                            circle.remove();
                                        }
                                        circleList.clear();
                                        Toast.makeText(getContext(),"Error getting documents: "+circleList.size(),Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getContext(),"get failed with ",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
                catch (SecurityException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Toast.makeText(getContext(),"Status Changed ->"+provider,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(getContext(),"Working ->"+provider,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(getContext(),"Not Working ->"+provider,Toast.LENGTH_SHORT).show();
            }
        };

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(latLng != null)
                    uploadLatLng = latLng;
                if(marker != null)
                    marker.remove();
                if(latLng != null)
                    marker = mMap.addMarker(new MarkerOptions().position(latLng)
                            .title(String.valueOf(latLng.latitude).substring(0,10)+","+String.valueOf(latLng.longitude).substring(0,10)));
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(marker != null) {
                    marker.remove();
                    uploadLatLng = null;
                }
            }
        });


        locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);

        try {

            assert locationManager != null;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME,MIN_DIST,locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME,MIN_DIST,locationListener);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }

    }

    public void openDialog(int curSp, int maxSp) {
        if (alartDalaog != null){
            if (alartDalaog.isShowing() == false){
                alartDalaog = new Dialog(getContext()); // Context, this, etc.
                alartDalaog.setTitle("Max spedd cross");
                alartDalaog.setContentView(R.layout.dialog_demo);
                alartDalaog.setCancelable(false);
                TextView textViewCurrentSpeed = alartDalaog.findViewById(R.id.current_speed_dialog);
                TextView textViewMaxSpeed = alartDalaog.findViewById(R.id.max_speed_dialog);
                textViewCurrentSpeed.setText("Current Speed = " + curSp);
                textViewMaxSpeed.setText("Max Speed = " + maxSp);
                alartDalaog.show();
            }
        }
        else {
            alartDalaog = new Dialog(getContext()); // Context, this, etc.
            alartDalaog.setTitle("Max spedd cross");
            alartDalaog.setContentView(R.layout.dialog_demo);
            alartDalaog.setCancelable(false);
            TextView textViewCurrentSpeed = alartDalaog.findViewById(R.id.current_speed_dialog);
            TextView textViewMaxSpeed = alartDalaog.findViewById(R.id.max_speed_dialog);
            textViewCurrentSpeed.setText("Current Speed = " + curSp);
            textViewMaxSpeed.setText("Max Speed = " + maxSp);
            alartDalaog.show();
        }
    }
    public void closeDialog(){
        if (alartDalaog != null){
            if (alartDalaog.isShowing() == true){
                alartDalaog.cancel();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        if(locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if(locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
        super.onDestroyView();
    }
}
