package be.bxl.formation.geolocateposition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {


    // initalisation du FusedLocationProviderClient, servide de géolocalisation qui utilise le gps et les réseaux
    FusedLocationProviderClient mFusedLocationClient;

    // initlialise variables
    int PERMISSION_ID = 77;

    TextView latTextView, lonTextView, altitudeTextView, accuracyTextView, speedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latTextView = findViewById(R.id.tv_lat);
        lonTextView = findViewById(R.id.tv_lon);
        altitudeTextView = findViewById(R.id.tv_altitude);
        accuracyTextView = findViewById(R.id.tv_accuracy);
        speedTextView = findViewById(R.id.tv_speed);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // recevoir dernière localisation de l'utilisateur
        getLastLocation();
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // appel méthode qui vérifie les permissions
        if (checkPermissions()) {

            // appel méthode qui vérifie si la localisation est activée
            if (isLocationEnabled()) {

                // recevoir derniere localisation

                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocation();
                        } else {
                            latTextView.setText(location.getLatitude() + "");
                            lonTextView.setText(location.getLongitude() + "");
                            altitudeTextView.setText(location.getAltitude() + "");
                            accuracyTextView.setText(location.getAccuracy() + "");
                            //TODO Vérifier pourquoi  getspeed ne renvoie rien
                            speedTextView.setText(location.getSpeed() + "");
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Activer  la localisation sur votre appareil.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // demander les permissions si elles sont manquantes
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocation() {

        // Initialisation de LocationRequest

        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(10);
        mLocationRequest.setNumUpdates(1);

        // paramètrage de  LocationRequest
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            lonTextView.setText("Longitude: " + mLastLocation.getLongitude() + "");
            latTextView.setText("Latitude: " + mLastLocation.getLatitude() + "");
        }
    };



    // méthode qui sera appélée pour demander les permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // verifie si la localisation de l'utilisateur est activée
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // vérifie permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    // si tout fonctionne correctement, renvoyer la dernière localisation de l'utilisateur
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }
}