package com.app.didaktikapp.Activities;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentTransaction;

import com.app.didaktikapp.BBDD.SQLiteControlador;
import com.app.didaktikapp.Fragments.FragmentErrota;
import com.app.didaktikapp.Fragments.FragmentErrotaTextos;
import com.app.didaktikapp.Fragments.FragmentPuzle;
import com.app.didaktikapp.Fragments.FragmentSanMiguel;
import com.app.didaktikapp.Fragments.FragmentSanMiguelImagenes;
import com.app.didaktikapp.Fragments.FragmentTrenTexto;
import com.app.didaktikapp.Fragments.FragmentUnibertsitatea;
import com.app.didaktikapp.Fragments.FragmentZumeltzegi;
import com.app.didaktikapp.InicioActivity;
import com.app.didaktikapp.MainActivity;
import com.app.didaktikapp.Modelo.Lugar;
import com.app.didaktikapp.R;
import com.app.didaktikapp.wordsearch.features.SplashScreenActivity;
import com.app.didaktikapp.wordsearch.features.gameplay.GamePlayActivity;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;

import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener
        , FragmentSanMiguel.OnFragmentInteractionListener
        , FragmentSanMiguelImagenes.OnFragmentInteractionListener
        , FragmentZumeltzegi.OnFragmentInteractionListener
        , FragmentPuzle.OnFragmentInteractionListener {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;

    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private LocationEngine locationEngine;
    private LocationChangeListeningActivityLocationCallback callback =
            new LocationChangeListeningActivityLocationCallback(this);
    private LocationEngineRequest locationEngineRequest;


    private String UNIQUE_LAYER_ID = "landuse";
    private Layer layer;

    private Location originLocation;

    private Context context;

    private ValueAnimator animator;
    private LatLng currentPosition = new LatLng(43.035000, -2.413917);
    private GeoJsonSource geoJsonSource;

    private int idgrupo;

    private static final LatLngBounds ONIATE_BOUNDS = new LatLngBounds.Builder()
        .include(new LatLng(43.042073, -2.422996)) // Northeast
        .include(new LatLng(43.028919, -2.405703)) // Southwest
        .build();

    private ArrayList<Lugar> listaLugares;

    private List<Point> routeCoordinates;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        this.context = getApplicationContext();
        permissionsManager = new PermissionsManager(this);


        cargarListaLugares();

        // Mapbox Access token
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_map);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        ImageView ivLogo = findViewById(R.id.ivLogo);

        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });



    }

    //NUEVOOO
    private final ValueAnimator.AnimatorUpdateListener animatorUpdateListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    LatLng animatedPosition = (LatLng) valueAnimator.getAnimatedValue();
                    geoJsonSource.setGeoJson(Point.fromLngLat(animatedPosition.getLongitude(), animatedPosition.getLatitude()));
                }
            };

    private static final TypeEvaluator<LatLng> latLngEvaluator = new TypeEvaluator<LatLng>() {

        private final LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    };

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        this.mapboxMap = mapboxMap;

        geoJsonSource = new GeoJsonSource("source-id",
                Feature.fromGeometry(Point.fromLngLat(currentPosition.getLongitude(),
                        currentPosition.getLatitude())));


        mapboxMap.setCameraPosition(new CameraPosition.Builder()
                .zoom(10)
                .build());




        // Limites de Getxo
        mapboxMap.setLatLngBoundsForCameraTarget(ONIATE_BOUNDS);

        // Visualise bounds area
        showBoundsArea(mapboxMap);

        crearIconos();


        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                enableLocationComponent(style);

                Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_update, null);
                Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);

                style.addImage(("marker_icon"), mBitmap);

                style.addSource(geoJsonSource);

                initRouteCoordinates();

                // Create the LineString from the list of coordinates and then make a GeoJSON
                // FeatureCollection so we can add the line to our map as a layer.
                style.addSource(new GeoJsonSource("line-source",
                        FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(
                                LineString.fromLngLats(routeCoordinates)
                        )})));

                // The layer properties for our line. This is where we make the line dotted, set the
                // color, etc.
                style.addLayer(new LineLayer("linelayer", "line-source").withProperties(
                        PropertyFactory.lineDasharray(new Float[] {0.01f, 2f}),
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                        PropertyFactory.lineWidth(5f),
                        PropertyFactory.lineColor(Color.parseColor("#e55e5e"))

                ));

                //NUEVOOO
                style.addLayer(new SymbolLayer("layer-id", "source-id")
                        .withProperties(
                                PropertyFactory.iconImage("marker_icon"),
                                PropertyFactory.iconIgnorePlacement(true),
                                PropertyFactory.iconAllowOverlap(true)
                        ));

            }

            });

        //NUEVOOOOO
        // When the user clicks on the map, we want to animate the marker to that
        // location.
        if (animator != null && animator.isStarted()) {
            currentPosition = (LatLng) animator.getAnimatedValue();
            animator.cancel();
        }

        animator = ObjectAnimator
                .ofObject(latLngEvaluator, currentPosition, new LatLng(43.033417, -2.413917))
                .setDuration(10000);
        animator.addUpdateListener(animatorUpdateListener);
        animator.start();

        mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                IconFactory iconFactory = IconFactory.getInstance(context);
                Icon iconorojo = iconFactory.fromResource(R.drawable.pin_sinhacer);
                Icon iconoamarillo = iconFactory.fromResource(R.drawable.pin_empezado);
                Icon iconoverde = iconFactory.fromResource(R.drawable.pin_hecho);
                SQLiteControlador sql = new SQLiteControlador(getApplicationContext());

                /*
                * Por cada punto con actividades que hay en el mapa, se comprueba el estado en
                * el que está, si hecho, empezado o deshabilitado, se procede a poner el icono
                * y se determina si se abre la actividad o no*/

                //ZUMELTZEGI DORREA (1)
                if(marker.getPosition().getLatitude()==43.035000 && marker.getPosition().getLongitude()==-2.412889){
                    int estado = sql.disponibilidadActividad("ActividadZumeltzegi",idgrupo);
                    boolean mostrar = false;
                    switch (estado) {
                        case -1:
                            marker.setIcon(iconorojo);
                            break;
                        case 0:
                            marker.setIcon(iconoamarillo);
                            sql.empezarActividad("ActividadZumeltzegi",idgrupo);
                            mostrar = true;
                            break;
                        case 1:
                            marker.setIcon(iconoamarillo);
                            mostrar = true;
                            break;
                        case 2:
                            marker.setIcon(iconoverde);
                            break;

                    }
                    if (mostrar) {
                        FragmentZumeltzegi fragment = new FragmentZumeltzegi();
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right);
                        transaction.replace(R.id.fragment_frame, fragment);
                        transaction.commit();
                        transaction.addToBackStack("Fragment");
                    }

                }
                //SAN MIGUEL PARROKIA (2)
                else if(marker.getPosition().getLatitude()==43.033417 && marker.getPosition().getLongitude()==-2.413917){
                    int estado = sql.disponibilidadActividad("ActividadSanMiguel",idgrupo);
                    boolean mostrar = false;
                    switch (estado) {
                        case -1:
                            marker.setIcon(iconorojo);
                            break;
                        case 0:
                            marker.setIcon(iconoamarillo);
                            sql.empezarActividad("ActividadSanMiguel",idgrupo);
                            mostrar = true;
                            break;
                        case 1:
                            marker.setIcon(iconoamarillo);
                            mostrar = true;
                            break;
                        case 2:
                            marker.setIcon(iconoverde);
                            break;

                    }
                    if (mostrar) {
                        FragmentSanMiguel fragment = new FragmentSanMiguel();
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right);
                        transaction.replace(R.id.fragment_frame, fragment);
                        transaction.commit();
                        transaction.addToBackStack("Fragment");
                    }

                }
                //UNIBERTSITATEA (3)
                else if(marker.getPosition().getLatitude()==43.033944 && marker.getPosition().getLongitude()==-2.415361){
                    int estado = sql.disponibilidadActividad("ActividadUniversidad",idgrupo);
                    boolean mostrar = false;
                    switch (estado) {
                        case -1:
                            marker.setIcon(iconorojo);
                            break;
                        case 0:
                            marker.setIcon(iconoamarillo);
                            sql.empezarActividad("ActividadUniversidad",idgrupo);
                            mostrar = true;
                            break;
                        case 1:
                            marker.setIcon(iconoamarillo);
                            mostrar = true;
                            break;
                        case 2:
                            marker.setIcon(iconoverde);
                            break;

                    }
                    if (mostrar) {
                        FragmentUnibertsitatea fragment = new FragmentUnibertsitatea();
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_frame, fragment);
                        transaction.commit();
                        transaction.addToBackStack("Fragment");
                    }

                }
                //TRENA (4)
                else if(marker.getPosition().getLatitude()==43.033833 && marker.getPosition().getLongitude()==-2.416111){
                    int estado = sql.disponibilidadActividad("ActividadTren",idgrupo);
                    boolean mostrar = false;
                    switch (estado) {
                        case -1:
                            marker.setIcon(iconorojo);
                            break;
                        case 0:
                            marker.setIcon(iconoamarillo);
                            sql.empezarActividad("ActividadTren",idgrupo);
                            mostrar = true;
                            break;
                        case 1:
                            marker.setIcon(iconoamarillo);
                            mostrar = true;
                            break;
                        case 2:
                            marker.setIcon(iconoverde);
                            break;

                    }
                    if (mostrar) {
                        FragmentPuzle fragment = new FragmentPuzle();
                        Bundle bundle = new Bundle();
                        bundle.putInt(FragmentPuzle.ARG_IMAGEN, R.drawable.tren);
                        fragment.setArguments(bundle);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right);
                        transaction.replace(R.id.fragment_frame, fragment);
                        transaction.commit();
                        transaction.addToBackStack("Fragment");
                    }

                }
                //SAN MIGUEL ERROTA (5)
                else if(marker.getPosition().getLatitude()==43.032917 && marker.getPosition().getLongitude()==-2.415750){
                    int estado = sql.disponibilidadActividad("ActividadErrota",idgrupo);
                    boolean mostrar = false;
                    switch (estado) {
                        case -1:
                            marker.setIcon(iconorojo);
                            break;
                        case 0:
                            marker.setIcon(iconoamarillo);
                            sql.empezarActividad("ActividadErrota",idgrupo);
                            mostrar = true;
                            break;
                        case 1:
                            marker.setIcon(iconoamarillo);
                            mostrar = true;
                            break;
                        case 2:
                            marker.setIcon(iconoverde);
                            break;

                    }
                    if (mostrar) {
                        FragmentErrotaTextos fragment = new FragmentErrotaTextos();
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right);
                        transaction.replace(R.id.fragment_frame, fragment);
                        transaction.commit();
                        transaction.addToBackStack("Fragment");
                    }

                }
                //ARAOTZ ASUA (sin uso, arriba)
                else if(marker.getPosition().getLatitude()==43.009139 && marker.getPosition().getLongitude()==-2.431444){


                }
                //ARRIKRUTZEKO KOBAK (sin uso, en medio)
                else if(marker.getPosition().getLatitude()==43.000583 && marker.getPosition().getLongitude()==-2.433250){
                    marker.setIcon(iconoverde);
                    Log.i("tag","s");
                    Intent intent = new Intent(MapActivity.this, SplashScreenActivity.class);
                    intent.putExtra(GamePlayActivity.EXTRA_ROW_COUNT, 10);
                    intent.putExtra(GamePlayActivity.EXTRA_COL_COUNT, 10);
                    startActivity(intent);

                }
                //ARANTZAZUKO SANTUTEGIA (sin uso, abajo)
                else if(marker.getPosition().getLatitude()==42.979194 && marker.getPosition().getLongitude()==-2.398583){
//

                }


                return false;
            }
        });




    }

    private void showBoundsArea(MapboxMap mapboxMap) {

        //Delimitamos los limites del mapa en la pantalla -> Getxo
        PolygonOptions boundsArea = new PolygonOptions()
                .add(ONIATE_BOUNDS.getNorthWest())
                .add(ONIATE_BOUNDS.getNorthEast())
                .add(ONIATE_BOUNDS.getSouthEast())
                .add(ONIATE_BOUNDS.getSouthWest());
        // Ajusta la transparencia del area seleccionada | 0 = transparente y 1 = opaco
        boundsArea.alpha(0f);
        mapboxMap.addPolygon(boundsArea);
    }

//    public static void actualizarIconos() {
//
//    }

    private void crearIconos(){
        IconFactory iconFactory = IconFactory.getInstance(context);
        Icon iconorojo = iconFactory.fromResource(R.drawable.pin_sinhacer);
        Icon iconoamarillo = iconFactory.fromResource(R.drawable.pin_empezado);
//        Icon iconoamarillo = iconFactory.fromResource(R.drawable.yellow_marker);
        Icon iconoverde = iconFactory.fromResource(R.drawable.pin_hecho);
//        Icon iconogris = iconFactory.fromResource(R.drawable.grey_marker);


//        for(Lugar lugar : listaLugares) {
//            mapboxMap.addMarker(new MarkerOptions()
//                    .position(lugar.getCoordenadas())
//                    .title(lugar.getNombre())
//                    .setIcon(iconorojo));
//        }

        /*
        * Al cargar el mapa, comprobamos la disponibilidad de los puntos
        * para asignarle el icono correspondiente por si está o no está hecho
        *
        * He comentado el anterior for para no perder el código*/
        SQLiteControlador sql = new SQLiteControlador(getApplicationContext());
        for (int x=0;x<listaLugares.size();x++) {
            Icon icono = iconorojo;
            int dis = -1;
            switch (x) {
                case 0:
                    dis = sql.disponibilidadActividad("ActividadZumeltzegi",idgrupo);
                    if (dis==1) icono = iconoamarillo;
                    else if (dis==2) icono = iconoverde;
                    break;
                case 1:
                    dis = sql.disponibilidadActividad("ActividadSanMiguel",idgrupo);
                    if (dis==1) icono = iconoamarillo;
                    else if (dis==2) icono = iconoverde;
                    break;
                case 2:
                    dis = sql.disponibilidadActividad("ActividadUniversidad",idgrupo);
                    if (dis==1) icono = iconoamarillo;
                    else if (dis==2) icono = iconoverde;
                    break;
                case 3:
                    dis = sql.disponibilidadActividad("ActividadTren",idgrupo);
                    if (dis==1) icono = iconoamarillo;
                    else if (dis==2) icono = iconoverde;
                    break;
                case 4:
                    dis = sql.disponibilidadActividad("ActividadErrota",idgrupo);
                    if (dis==1) icono = iconoamarillo;
                    else if (dis==2) icono = iconoverde;
                    break;
            }
            Lugar lugar = listaLugares.get(x);
            mapboxMap.addMarker(new MarkerOptions()
                    .position(lugar.getCoordenadas())
                    .title(lugar.getNombre())
                    .setIcon(icono));
        }
    }

    private void cargarListaLugares(){

        this.listaLugares = new ArrayList<Lugar>();

        listaLugares.add(new Lugar(getString(R.string.nombreLugar1),new LatLng(43.035000, -2.412889)));
        listaLugares.add(new Lugar(getString(R.string.nombreLugar3),new LatLng(43.033417, -2.413917)));
        listaLugares.add(new Lugar(getString(R.string.nombreLugar2),new LatLng(43.033944, -2.415361)));
        listaLugares.add(new Lugar(getString(R.string.nombreLugar4),new LatLng(43.033833, -2.416111)));
        listaLugares.add(new Lugar(getString(R.string.nombreLugar5),new LatLng(43.032917,  -2.415750)));
        listaLugares.add(new Lugar(getString(R.string.nombreLugar6),new LatLng(42.979194,  -2.398583)));
        listaLugares.add(new Lugar(getString(R.string.nombreLugar7),new LatLng(43.009139,  -2.431444)));
        listaLugares.add(new Lugar(getString(R.string.nombreLugar8),new LatLng(43.000583, -2.433250)));

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }




    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Create and customize the LocationComponent's options
            LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(this)
                    .elevation(5)
                    .accuracyAlpha(.6f)
                    .foregroundDrawable(R.drawable.trotar)
                    .build();

        // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

        // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .locationComponentOptions(customLocationComponentOptions)
                            .build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }


    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private static class LocationChangeListeningActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<MapActivity> activityWeakReference;

        LocationChangeListeningActivityLocationCallback(MapActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            MapActivity activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }

                // Create a Toast which displays the new location's coordinates
//                Toast.makeText(activity, ""+R.string.new_location +", "+
//                        String.valueOf(result.getLastLocation().getLatitude()) + ", "+
//                        String.valueOf(result.getLastLocation().getLongitude()),
//                        Toast.LENGTH_SHORT).show();

                double latitud = result.getLastLocation().getLatitude();

                double longitud = result.getLastLocation().getLongitude();

                // Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }


                //Lanzar fragments cuando la distancia sea corta a los puntos.
//                Toast.makeText(activity,latitud+", "+longitud,Toast.LENGTH_SHORT).show();
//                Log.i("LATITUD",Double.toString(latitud));
//                Log.i("LONGITUD",Double.toString(longitud));


            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can't be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());
            MapActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static double distanciaCoord(double lat1, double lng1, double lat2, double lng2) {
        //double radioTierra = 3958.75;//en millas
        double radioTierra = 6371;//en kilómetros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double va1 = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1));
        double distancia = radioTierra * va2;

        return distancia;
    }

    private void initRouteCoordinates() {
// Create a list to store our line coordinates.
        routeCoordinates = new ArrayList<>();
        routeCoordinates.add(Point.fromLngLat(-2.412889, 43.035000));
        routeCoordinates.add(Point.fromLngLat(-2.413917, 43.033417));




    }





    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {

        super.onStop();

        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }

        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}



