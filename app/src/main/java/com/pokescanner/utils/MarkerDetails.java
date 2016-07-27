package com.pokescanner.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pokescanner.R;
import com.pokescanner.objects.Gym;
import com.pokescanner.objects.PokeStop;
import com.pokescanner.objects.Pokemons;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MarkerDetails
{
    private static double markerLatitude, markerLongitude;
    private static String markerTitle;
    private static TextView tvAddress;
    private static Context context;

    public static void showMarkerDetailsDialog(final Context context, Object selectedMarkerData, Location currentLocation)
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_marker_details);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        MarkerDetails.context = context;

        TextView heading = (TextView) dialog.findViewById(R.id.tvHeading);
        ImageView iconPic = (ImageView) dialog.findViewById(R.id.ivIcon);
        tvAddress = (TextView) dialog.findViewById(R.id.tvAddress);
        TextView guardPokemon = (TextView) dialog.findViewById(R.id.tvGuardPkmn);
        TextView gymPoints = (TextView) dialog.findViewById(R.id.tvGymPoints);
        final TextView pokemonTimer = (TextView) dialog.findViewById(R.id.pokemonExpTimer);
        final TextView lureTimer = (TextView) dialog.findViewById(R.id.lureExpTimer);
        TextView luredPkmn = (TextView) dialog.findViewById(R.id.tvLuredPkmn);
        Button gmapsBtn = (Button) dialog.findViewById(R.id.gmapsBtn);
        Button directionsBtn = (Button) dialog.findViewById(R.id.directionsBtn);
        LinearLayout guardPkmnLayout = (LinearLayout) dialog.findViewById(R.id.guardPkmnLayout);
        LinearLayout pokeExpTimeLayout = (LinearLayout) dialog.findViewById(R.id.pokeExpTimeLayout);
        LinearLayout lureExpTimeLayout = (LinearLayout) dialog.findViewById(R.id.lureExpTimeLayout);
        LinearLayout luredPkmnLayout = (LinearLayout) dialog.findViewById(R.id.luredPkmnLayout);
        LinearLayout gymPointsLayout = (LinearLayout) dialog.findViewById(R.id.gymPointsLayout);
        directionsBtn.setVisibility(View.GONE);

        if(selectedMarkerData instanceof Pokemons)
        {
            final Pokemons selectedPokemon = (Pokemons) selectedMarkerData;

            markerLatitude = selectedPokemon.getLatitude();
            markerLongitude = selectedPokemon.getLongitude();

            markerTitle = selectedPokemon.getFormalName();

            iconPic.setImageBitmap(DrawableUtils.getBitmapFromView(selectedPokemon.getResourceID(context),"",context));

            guardPkmnLayout.setVisibility(View.GONE);
            lureExpTimeLayout.setVisibility(View.GONE);
            luredPkmnLayout.setVisibility(View.GONE);
            gymPointsLayout.setVisibility(View.GONE);
            Subscription pokeExpiryRefresher = Observable.interval(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            String expires = DrawableUtils.getExpireTime(selectedPokemon.getExpires());
                            pokemonTimer.setText(expires);
                        }
                    });
        }

        if(selectedMarkerData instanceof Gym)
        {
            final Gym selectedGym = (Gym) selectedMarkerData;
            markerLatitude = selectedGym.getLatitude();
            markerLongitude = selectedGym.getLongitude();
            markerTitle = selectedGym.getTitle();
            iconPic.setImageBitmap(selectedGym.getBitmap(context));
            pokeExpTimeLayout.setVisibility(View.GONE);
            lureExpTimeLayout.setVisibility(View.GONE);
            luredPkmnLayout.setVisibility(View.GONE);
            gymPoints.setText(selectedGym.getPoints() + "");
            String guardPokemonName = selectedGym.getGuardPokemonName();
            guardPokemonName = guardPokemonName.substring(0, 1).toUpperCase() + guardPokemonName.substring(1).toLowerCase();
            guardPokemon.setText(guardPokemonName);
        }

        if(selectedMarkerData instanceof PokeStop)
        {
            final PokeStop selectedPokestop = (PokeStop) selectedMarkerData;
            markerLatitude = selectedPokestop.getLatitude();
            markerLongitude = selectedPokestop.getLongitude();
            if(selectedPokestop.isHasLureInfo())
            {
                markerTitle = "Pokestop with active lure";
                luredPkmn.setText(selectedPokestop.getLuredPokemonName());
                final Subscription lureExpiryRefresher = Observable.interval(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                lureTimer.setText(selectedPokestop.getLureExpiryTime());
                            }
                        });
            }
            else
            {
                markerTitle = "Pokestop";
                lureExpTimeLayout.setVisibility(View.GONE);
                luredPkmnLayout.setVisibility(View.GONE);
            }
            iconPic.setImageBitmap(selectedPokestop.getBitmap(context));
            pokeExpTimeLayout.setVisibility(View.GONE);
            gymPointsLayout.setVisibility(View.GONE);
            guardPkmnLayout.setVisibility(View.GONE);
        }

        heading.setText(markerTitle);
        setOpenGoogleMapsButton(context, gmapsBtn);
        getAddressFromLocation(context, new GeocoderHandler());

        dialog.show();
    }

    private static void setOpenGoogleMapsButton(final Context context, Button openGoogleMapsButton)
    {
        openGoogleMapsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + markerLatitude + "," + markerLongitude + "(" + markerTitle + ")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                }
            }
        });
    }

    public static void getAddressFromLocation(final Context context, final Handler handler)
    {
        Thread thread = new Thread() {
            @Override public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try
                {
                    List<Address> list = geocoder.getFromLocation(markerLatitude, markerLongitude, 1);
                    if (list != null && list.size() > 0)
                    {
                        Address address = list.get(0);
                        result = address.getAddressLine(0) + ", " + address.getLocality();
                    }
                }
                catch (IOException e)
                {
                    Log.e("Error ", "Impossible to connect to Geocoder", e);
                }
                finally
                {
                    Message msg = Message.obtain();
                    msg.setTarget(handler);
                    if (result != null)
                    {
                        msg.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("address", result);
                        msg.setData(bundle);
                    }
                    else
                        msg.what = 0;
                    msg.sendToTarget();
                }
            }
        };
        thread.start();
    }

    private static class GeocoderHandler extends Handler
    {
        @Override
        public void handleMessage(Message message)
        {
            switch (message.what)
            {
                case 1:
                    Bundle bundle = message.getData();
                    tvAddress.setText(bundle.getString("address"));
                    break;
                default:
                    tvAddress.setText("NA");
                    getAddressFromLocation(context, new GeocoderHandler());
            }
        }
    }
}
