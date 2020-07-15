package com.example.speedtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    private View mView;

    private LatLng latLng,uploadLatLng = null;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    //Circle circle;
    private Marker marker;
    private Map<String,Circle> circleList = new HashMap<>();
    //Button get = findViewById(R.id.get);

    private int curSpeed;
    private String mSpeed;
    //TextView speedTextView = findViewById(R.id.curSpeed);
    private double lat1,lon1,lat2,lon2,r = 6371000;
    private String path1 = null,path2 = null;
    private Map<String, Object> pointArray = null;
    private Map<String, Object> pointArray1 = null;

    private MapView mMapView;

    private LocationListener locationListener;
    private LocationManager locationManager;

    private TextView textViewLat,textViewLon;
    private EditText editTextSpeed;
    private Button addDataBtn, deletePoint;
    private String speedValue;

    private Dialog alartDalaog;

    public EditMapFragment() {
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
        mView = inflater.inflate(R.layout.fragment_edit_map, container, false);
        Log.d("myTag","in EditText onCreate");


        mMapView = (MapView) mView.findViewById(R.id.map3);
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
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET},PackageManager.PERMISSION_GRANTED);
        } else {
            Log.e("DB", "PERMISSION GRANTED");
        }

        editTextSpeed = mView.findViewById(R.id.speedValue);
        addDataBtn = mView.findViewById(R.id.load);

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

        textViewLat = mView.findViewById(R.id.latTextView);
        textViewLon = mView.findViewById(R.id.lonTextView);

        deletePoint = mView.findViewById(R.id.deletePoint);
        deletePoint.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                if(uploadLatLng != null){
                    String[] stringLatitudeParts = String.valueOf(uploadLatLng.latitude).split("\\.",2);
                    String[] stringLongitudeParts = String.valueOf(uploadLatLng.longitude).split("\\.",2);
                    String path1 = stringLatitudeParts[0] + "_" + stringLongitudeParts[0];
                    String path2 = stringLatitudeParts[1].substring(0,2) + "_" + stringLongitudeParts[1].substring(0,2);
                    final String field = stringLatitudeParts[0] + "." + stringLatitudeParts[1] + ","
                            + stringLongitudeParts[0] + "." + stringLongitudeParts[1];

                    DocumentReference documentReference = mStore.collection("points")
                            .document(path1).collection("sub_points").document(path2);

                    Map<String,Object> updates = new HashMap<>();

                    updates.put(field, FieldValue.delete());

                    documentReference.set(updates, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(), "Delete point success", Toast.LENGTH_SHORT).show();
                            Log.d("myTag", "Delte field = " + field);

                            deletePoint.setVisibility(View.INVISIBLE);
//                            editTextSpeed.setText("");
                            addDataBtn.setText("Add");

                            pointArray.remove(field);

                            Circle circle = circleList.get(field);
                            circle.remove();
                            circleList.remove(field);
                        }
                    });

                }
            }
        });


        addDataBtn.setOnClickListener(new View.OnClickListener() {
            int sValue = -1;
            @Override
            public void onClick(View v) {
                final String tag = "";

                if(uploadLatLng != null){
                    String[] stringLatitudeParts = String.valueOf(uploadLatLng.latitude).split("\\.",2);
                    String[] stringLongitudeParts = String.valueOf(uploadLatLng.longitude).split("\\.",2);
                    String path1 = stringLatitudeParts[0] + "_" + stringLongitudeParts[0];
                    String path2 = stringLatitudeParts[1].substring(0,2) + "_" + stringLongitudeParts[1].substring(0,2);
                    final String field = stringLatitudeParts[0] + "." + stringLatitudeParts[1] + ","
                                            + stringLongitudeParts[0] + "." + stringLongitudeParts[1];
                    speedValue = editTextSpeed.getText().toString();
                    if(!"".equals(speedValue)){
                        sValue = Integer.parseInt(speedValue);
                    }
                    else {
                        editTextSpeed.setError("Enter Speed Value");
                        return;
                    }
                    Log.d("myTag","Field = "+ field);

                    if(sValue > 0){
                        DocumentReference documentReference = mStore.collection("points")
                                .document(path1).collection("sub_points").document(path2);

                        Map<String,Object> point = new HashMap<>();

                        point.put(field,sValue);

                        documentReference.set(point, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(),"onSuccess: user Profile is created for",Toast.LENGTH_SHORT).show();
                                Log.d("myTag","onSuccess: user Profile is created for"+field);

                                if(circleList.get(field) == null){
                                    Circle circle = mMap.addCircle(new CircleOptions()
                                            .center(uploadLatLng)
                                            .radius(10).strokeColor(Color.parseColor("#FF0000"))
                                            .fillColor(Color.parseColor("#3300FF00")).strokeWidth(2f));

                                    circleList.put(field,circle);
                                }

                                pointArray.put(field, sValue);
                                addDataBtn.setText("Update");
                                deletePoint.setVisibility(View.VISIBLE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(),"onFailure: user Profile is not created",Toast.LENGTH_SHORT).show();
                                Log.d("myTag", "onFailure: " + e.toString());
                            }
                        });

                    }
                }
            }
        });


        return mView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

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
                    double d;

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
                            String[] keys = key.split(",",2);
                            lat1 = Double.parseDouble(keys[0]);
                            lon1 = Double.parseDouble(keys[1]);
                            d = Math.acos((Math.sin(Math.toRadians(lat1))*Math.sin(Math.toRadians(lat2)))
                                    +(Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.cos(Math.toRadians(lon2-lon1))))*r;

                            if (d <= nearDistance){
                                nearDistance = d;
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
                                        int maxSpeed = 20;
                                        if(circleList != null){
                                            for (Circle circle : circleList.values()) {
                                                circle.remove();
                                            }
                                            circleList.clear();


                                            pointArray = null;
                                            pointArray = document.getData();
                                            assert pointArray != null;
                                            for (Map.Entry<String,Object> entry : pointArray.entrySet()){
                                                key = entry.getKey();
                                                speed = entry.getValue();
                                                String[] keys = key.split(",",2);
                                                lat1 = Double.parseDouble(keys[0]);
                                                lon1 = Double.parseDouble(keys[1]);
                                                Circle circle = mMap.addCircle(new CircleOptions()
                                                        .center(new LatLng(lat1,lon1))
                                                        .radius(10).strokeColor(Color.parseColor("#FF0000"))
                                                        .fillColor(Color.parseColor("#3300FF00")).strokeWidth(2f));

                                                circleList.put(key,circle);
                                                double d = Math.acos((Math.sin(Math.toRadians(lat1))*Math.sin(Math.toRadians(lat2)))
                                                        +(Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.cos(Math.toRadians(lon2-lon1))))*r;

                                                if (d <= nearDistance){
                                                    nearDistance = d;
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
                                            Toast.makeText(getContext(),key,Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        for (Circle circle : circleList.values()) {
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
            @SuppressLint("SetTextI18n")
            @Override
            public void onMapLongClick(LatLng latLng) {
                Double uploadLat,uploadLon;

                addDataBtn.setText("Add");
                addDataBtn.setVisibility(View.VISIBLE);
                deletePoint.setVisibility(View.INVISIBLE);

                editTextSpeed.setError(null);

                if(marker != null)
                    marker.remove();
                if(latLng != null){
                    uploadLat = Double.parseDouble(String.valueOf(latLng.latitude).substring(0,11));
                    uploadLon = Double.parseDouble(String.valueOf(latLng.longitude).substring(0,11));
                    uploadLatLng = new LatLng(uploadLat,uploadLon);
                    textViewLat.setText("Lat = "+ String.valueOf(uploadLatLng.latitude));
                    textViewLon.setText("Lon = "+ String.valueOf(uploadLatLng.longitude));
                    marker = mMap.addMarker(new MarkerOptions().position(latLng)
                            .title(String.valueOf(latLng.latitude).substring(0,10)+","+String.valueOf(latLng.longitude).substring(0,10)));
                }

                if (pointArray != null)
                {
                    String key;
                    Object speed;
                    Double distance;
                    Double lat1,lon1,lat2,lon2;

                    lat2 = latLng.latitude;
                    lon2 = latLng.longitude;


                    for (Map.Entry<String,Object> entry : pointArray.entrySet()){
                        key = entry.getKey();
                        speed = entry.getValue();
                        String[] keys = key.split(",",2);
                        lat1 = Double.parseDouble(keys[0]);
                        lon1 = Double.parseDouble(keys[1]);
                        distance = Math.acos((Math.sin(Math.toRadians(lat1))*Math.sin(Math.toRadians(lat2)))
                                +(Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.cos(Math.toRadians(lon2-lon1))))*r;

                        if (distance <= 10){

                            deletePoint.setVisibility(View.VISIBLE);
                            addDataBtn.setText("Update");


                            uploadLatLng = new LatLng(lat1,lon1);

                            if(uploadLatLng != null){
                                textViewLat.setText("Lat = "+ lat1);
                                textViewLon.setText("Lon = "+ lon1);
                                speedValue = speed.toString();
                                editTextSpeed.setText(speedValue);
                            }
                            else {
                                textViewLat.setText("Lat = ");
                                textViewLon.setText("Lon = ");
                            }

                            if(marker != null) {
                                marker.remove();
                                marker = mMap.addMarker(new MarkerOptions().position(uploadLatLng)
                                        .title(String.valueOf(uploadLatLng.latitude).substring(0,10)+","+String.valueOf(uploadLatLng.longitude).substring(0,10)));
                            }
                            break;
                        }
                        else {
                            textViewLat.setText("Lat = "+ String.valueOf(uploadLatLng.latitude));
                            textViewLon.setText("Lon = "+ String.valueOf(uploadLatLng.longitude));
                            editTextSpeed.setText("");
                            addDataBtn.setText("Add");
                        }
                    }
                }
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(marker != null) {
                    marker.remove();
                    uploadLatLng = null;
                    textViewLat.setText("Lat = ");
                    textViewLon.setText("Lon = ");
                    editTextSpeed.setText("");
                    addDataBtn.setText("Add");
                }
                deletePoint.setVisibility(View.INVISIBLE);
                addDataBtn.setVisibility(View.INVISIBLE);
                editTextSpeed.setError(null);
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
                alartDalaog.setTitle("Max speed cross");
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
    public void onPause() {
        if(locationManager != null) {
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
