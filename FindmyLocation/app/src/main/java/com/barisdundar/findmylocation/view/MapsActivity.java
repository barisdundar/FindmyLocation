package com.barisdundar.findmylocation.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.barisdundar.findmylocation.R;
import com.barisdundar.findmylocation.model.Place;
import com.barisdundar.findmylocation.roomdb.PlaceDao;
import com.barisdundar.findmylocation.roomdb.PlaceDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.barisdundar.findmylocation.databinding.ActivityMapsBinding;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ActivityResultLauncher<String> permissionLauncher;
    LocationManager locationManager;
    LocationListener locationListener;
    PlaceDatabase db;
    PlaceDao placeDao;
    Double selectedLatitude;
    Double selectedLongitude;
    Place selectedPlace;

    private CompositeDisposable compositeDisposable=new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.saveButton.setEnabled(false);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        registerLauncher();
        db=Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places")
              //  .allowMainThreadQueries()
                .build();
        placeDao=db.placeDao();
        selectedLatitude=0.0;
        selectedLongitude=0.0;


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        Intent intent=getIntent();
        String intentinfo=intent.getStringExtra("info");
        if (intentinfo.equals("new")){
            binding.saveButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.GONE);
            locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener=new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    // System.out.println("location: " + location.toString());
                    SharedPreferences sharedPreferences=MapsActivity.this.getSharedPreferences("com.barisdundar.findmylocation",MODE_PRIVATE);
                    boolean info=sharedPreferences.getBoolean("info",false);
                    if (info==false){
                        LatLng userLocation=new LatLng(location.getLatitude(),location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,17));
                        sharedPreferences.edit().putBoolean("info",true).apply();
                    }

                }

            };
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                    Snackbar.make(binding.getRoot(),"Permission Needed for Maps",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    }).show();
                }else  {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,locationListener);
                Location lastlocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

//uygulama ilk çalıştığındaki konumu alma kodları
                if (lastlocation!=null){
                    LatLng lastUserLocation=new LatLng(lastlocation.getLatitude(),lastlocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,17));
                }
            }

            //ilk açıldığında manuel oluşturulabilicek konum

     /*   LatLng home = new LatLng(41.07956605707045, 28.98107319592664);
        mMap.addMarker(new MarkerOptions().position(home).title("Marker in Home"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home,17)); */
        }
        else {
    mMap.clear();
    selectedPlace=(Place) intent.getSerializableExtra("place");
    LatLng latLng=new LatLng(selectedPlace.latitude,selectedPlace.longitude);
    mMap.addMarker(new MarkerOptions().position(latLng).title(selectedPlace.name));
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
    binding.placeNameText.setText(selectedPlace.name);

            binding.saveButton.setVisibility(View.GONE);
        }



    }
    private void registerLauncher(){
        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result){
                    if (ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,locationListener);
                        Location lastlocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

//uygulama ilk çalıştığındaki konumu alma kodları
                        if (lastlocation!=null){
                            LatLng lastUserLocation=new LatLng(lastlocation.getLatitude(),lastlocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,17));
                        }
                        mMap.setMyLocationEnabled(true); //mavi ok koyuyor
                    }
                    //permission granted

                }else {
                    //permission denied
                    Toast.makeText(MapsActivity.this,"Permission Needed",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng)  { //haritaya uzun  tıklanıldığında marker koymaya yarar
        mMap.clear();
mMap.addMarker(new MarkerOptions().position(latLng).title("Your chose"));
selectedLatitude=latLng.latitude;
selectedLongitude=latLng.longitude;
binding.saveButton.setEnabled(true);//tıklanmadan tıklanamaz
    }
    public void save(View view){
        Place place=new Place(binding.placeNameText.getText().toString(),selectedLatitude,selectedLongitude);
      //  placeDao.insert(place).subscribeOn(Schedulers.io()).subscribe();  1.yol
        // 2.yol

        compositeDisposable.add(placeDao.insert(place)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(MapsActivity.this::handleResponse));

    }
    private void handleResponse(){
        Intent intent=new Intent(MapsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    public void delete(View view){
        if (selectedPlace!=null){
            compositeDisposable.add(placeDao.delete(selectedPlace)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(MapsActivity.this::handleResponse));
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy(); //2. yolun devamı
        compositeDisposable.clear();
    }

}