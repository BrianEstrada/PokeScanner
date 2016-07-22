package com.pokescanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.auth.PtcLogin;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import okhttp3.OkHttpClient;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener {

    Button button;
    ProgressBar progressBar;
    private GoogleMap mMap;

    LocationManager locationManager;
    Location currentLocation;
    String username, password;

    SharedPreferences sharedPref;

    ArrayList<LatLng> scanMap = new ArrayList<>();
    ArrayList<CatchablePokemon> pokemons = new ArrayList<>();

    loadPokemon loader;

    final int SLEEP_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MultiDex.install(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        sharedPref = getSharedPreferences(getString(R.string.shared_key), Context.MODE_PRIVATE);

        boolean login = sharedPref.getBoolean("login", false);

        if (login) {
            username = sharedPref.getString("username", null);
            password = sharedPref.getString("password", null);
        } else {
            Toast.makeText(MapsActivity.this, "No login!", Toast.LENGTH_SHORT).show();
            logOut();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        button = (Button) findViewById(R.id.btnSearch);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PokeScan();
            }
        });
    }

    public void PokeScan() {
        progressBar.setProgress(0);
        if (mMap != null)
            mMap.clear();
        pokemons.clear();
        createScanMap(mMap.getCameraPosition().target, 5);
        new loadPokemon().execute();
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (doWeHavePermission()) {
            //Set our map stuff
            mMap.setMyLocationEnabled(true);
            mMap.setOnCameraChangeListener(this);

            //Let's find our location and set it!
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            currentLocation = locationManager.getLastKnownLocation(provider);

            //Center camera function
            centerCamera();
            startRefresher();
        }
    }

    public void centerCamera() {
        if (currentLocation != null && doWeHavePermission()) {
            LatLng target = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraPosition position = this.mMap.getCameraPosition();

            CameraPosition.Builder builder = new CameraPosition.Builder();
            builder.zoom(15);
            builder.target(target);

            this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
        }
    }

    public void showProgressbar(boolean status) {
        if (status) {
            progressBar.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
        }
    }

    public boolean isGPSEnabled() {
        LocationManager cm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return cm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void logOut() {
        getPreferences(Context.MODE_PRIVATE).edit().clear().commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void refreshMap() {
        mMap.clear();
        for (int i = 0; i < pokemons.size(); i++) {
            CatchablePokemon pokemon = pokemons.get(i);
            {

                String uri = "p" + pokemon.getPokemonId().getNumber();
                int resourceID = getResources().getIdentifier(uri, "drawable", getPackageName());

                DateTime oldDate = new DateTime(pokemon.getExpirationTimestampMs());
                Interval interval;
                if (oldDate.isAfter(new Instant())) {
                    //Find our interval
                    interval = new Interval(new Instant(), oldDate);
                    //turn our interval into MM:SS
                    DateTime dt = new DateTime(interval.toDurationMillis());
                    DateTimeFormatter fmt = DateTimeFormat.forPattern("mm:ss");
                    String timeOut = fmt.print(dt);
                    //set our location
                    LatLng position = new LatLng(pokemon.getLatitude(), pokemon.getLongitude());

                    Bitmap out = writeTextOnDrawable(resourceID,timeOut,2);

                    String name = pokemon.getPokemonId().toString();
                    name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

                    MarkerOptions pokeIcon = new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(out))
                            .position(position)
                            .title(name)
                            .snippet(timeOut);

                    mMap.addMarker(pokeIcon);

                }
            }
        }

    }

    public void createScanMap(LatLng loc, int gridsize) {
        int gridNumber = gridsize;
        //Make our grid size an odd number (evens don't have centers :P)
        if ((gridsize % 1) == 0) {
            gridNumber = gridNumber - 1;
        }
        //clear the previous scan map
        scanMap.clear();
        //Our distance number is the number we'll offset the GPS by
        //when creating a grid
        double dist = 00.002000;
        //to find the middle of the grid we need to find the middle number
        int middleNumber = ((gridNumber - 1) / 2);
        double lat = loc.latitude + (dist * -middleNumber);
        double lon = loc.longitude + (dist * -middleNumber);
        //this is the GPS offset we're going to use
        for (int i = 0; i < gridsize; i++) {
            for (int j = 0; j < gridsize; j++) {
                double newLat = (lat + (dist * i));
                double newLon = (lon + (dist * j));
                LatLng temp = new LatLng(newLat, newLon);
                scanMap.add(temp);
            }
        }
    }

    public void startRefresher() {
        Observable.interval(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        refreshMap();
                        System.out.println("Refreshing Map");
                    }
                });
    }

    public boolean doWeHavePermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
    }

    private Bitmap writeTextOnDrawable(int drawableId, String text, int scale) {
        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);
        bm = Bitmap.createScaledBitmap(bm,bm.getWidth()/scale,bm.getHeight()/scale,false);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(this, 11));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(this, 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos-convertToPixels(this,16), paint);

        return  bm;
    }

    public static int convertToPixels(Context context, int nDP) {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;

    }

    private class loadPokemon extends AsyncTask<String, List<CatchablePokemon>, String> {
        int pos = 1;

        @Override
        protected String doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo = new PtcLogin(client).login(username, password);
                PokemonGo go = new PokemonGo(authInfo, client);
                for (LatLng loc : scanMap) {
                    try {
                        go.setLongitude(loc.longitude);
                        go.setLatitude(loc.latitude);
                        Map map = new Map(go);
                        List<CatchablePokemon> catchablePokemon = map.getCatchablePokemon();
                        MapObjects objects = map.getMapObjects();
                        publishProgress(catchablePokemon);
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (RemoteServerException e) {
                        e.printStackTrace();
                    } catch (LoginFailedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (LoginFailedException e) {
                e.printStackTrace();
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            showProgressbar(false);
        }

        @Override
        protected void onPreExecute() {
            showProgressbar(true);
        }

        @Override
        protected void onProgressUpdate(List<CatchablePokemon>... objects) {
            progressBar.setProgress(pos * 4);
            if (objects.length < 1) return;
            List<CatchablePokemon> object = objects[0];
            pokemons.addAll(object);
            pos++;
        }
    }
}
