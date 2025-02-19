package com.proyecto.gestionmapas;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import android.Manifest;
import android.os.Looper;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instancia de FusedLocationProviderClient, que es la API de Google para obtener ubicaciones de forma eficiente.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Gestionamos el fragmento de pantalla en el que se ubicará nuestro mapa.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Nuestro mapa será el mapa generado por Google
        mMap = googleMap;

        // Configurar tipo de mapa
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Añadir varios marcadores
        addMarkers();

        // Añadir marcador con la posición del usuario.
        addUserPositionMarker();

        // Habilitar clics en los marcadores
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Acción al seleccionar un marcador
                marker.showInfoWindow();
                return false; // Si es false, mantiene el comportamiento predeterminado
            }
        });
    }

    private void addMarkers() {
        // Coordenadas de algunos puntos
        LatLng madrid = new LatLng(40.4168, -3.7038);
        LatLng barcelona = new LatLng(41.3851, 2.1734);
        LatLng sevilla = new LatLng(37.3891, -5.9845);

        // Agregar marcadores en el mapa
        mMap.addMarker(new MarkerOptions().position(madrid).title("Madrid"));
        mMap.addMarker(new MarkerOptions().position(barcelona).title("Barcelona"));
        mMap.addMarker(new MarkerOptions().position(sevilla).title("Sevilla"));
    }

    private void addUserPositionMarker(){
        // Solicitud de permisos en tiempo de ejecución
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }

        // Listener de actualización de posición: cada 2 segundos
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        // Gestión de la respuesta del servicio de ubicación.
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        // Si todo va correctamente, posicionamos el avatar sobre nuestra posición en el mapa
                        // y hacemos un zoom medio.
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions()
                                .position(userLatLng)
                                .title("¡Aquí estás!")
                                .icon( bitmapDescriptorFromVector(MainActivity.this, R.drawable.avatar_usuario) ));

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 8));
                        fusedLocationClient.removeLocationUpdates(this); // Detener actualizaciones para ahorrar batería
                        break;
                    }
                }
            }
        };

        // Se establece la petición de ubicación, la gestión de la respuesta y se define la ejecución automática.
        // fusedLocationClient → Es una instancia de FusedLocationProviderClient, que es la API de Google para obtener ubicaciones de forma eficiente.
        // requestLocationUpdates(...) → Es el método que solicita actualizaciones de ubicación basadas en los parámetros que le pasamos.
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    /**
     * Gestión de la respuesta de los permisos solicitados en tiempo de ejecución.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @param deviceId
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Verifica si la respuesta es para el permiso de ubicación..
        if (requestCode == LOCATION_REQUEST_CODE) {
            // Comprueba si el permiso fue concedido.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el permiso fue concedido, añade el marcador del usuario.
                addUserPositionMarker();
            } else {
                // Si el permiso fue denegado, muestra un mensaje informativo al usuario.
                Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Este método transforma un .png en un bitmap
     *
     * @param context
     * @param vectorResId
     * @return
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) {
            return null;
        }

        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth()/3, vectorDrawable.getIntrinsicHeight()/3);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth()/3, vectorDrawable.getIntrinsicHeight()/3, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
