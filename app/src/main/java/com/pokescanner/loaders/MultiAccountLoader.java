package com.pokescanner.loaders;

import com.google.android.gms.maps.model.LatLng;
import com.pokescanner.helper.MyPartition;
import com.pokescanner.objects.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Brian on 7/31/2016.
 */
public class MultiAccountLoader {
    List<LatLng> scanMap;
    List<List<LatLng>> scanMaps = new ArrayList<>();
    ArrayList<Thread> threads = new ArrayList<>();
    ArrayList<User> users;
    int SLEEP_TIME;

    public MultiAccountLoader(List<LatLng> scanMap,int SLEEP_TIME,ArrayList<User> users){
        this.scanMap = scanMap;
        this.SLEEP_TIME = SLEEP_TIME;
        this.users = users;
    }

    public void startThreads() {

        int usersNumber = users.size();
        int scanSize = (int) Math.ceil(scanMap.size()/usersNumber);

        scanMaps = MyPartition.partition(scanMap,scanSize);

        System.out.println(scanMaps.size());

        for (int i = 0;i<users.size();i++) {

            User tempUser = users.get(i);
            List<LatLng> tempMap = scanMaps.get(i);
            System.out.println(Arrays.asList(tempMap));
            threads.add(new ObjectLoaderPTC(tempUser,tempMap,SLEEP_TIME,i));
        }

        for (Thread thread: threads) {
            thread.start();
        }
    }

    public boolean cancelAllThreads() {
        while (threads != null) {
            for (Thread thread: threads) {
                try {
                    thread.interrupt();
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (threads != null) {
            if (!threads.get(0).isAlive()) {
                return true;
            }
        }
        return false;
    }
}
