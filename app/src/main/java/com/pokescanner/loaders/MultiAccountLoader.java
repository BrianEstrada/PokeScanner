package com.pokescanner.loaders;

import com.google.android.gms.maps.model.LatLng;
import com.pokescanner.helper.MyPartition;
import com.pokescanner.objects.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brian on 7/31/2016.
 */
public class MultiAccountLoader {
    static private List<LatLng> scanMap;
    static private List<List<LatLng>> scanMaps;
    static private ArrayList<Thread> threads;
    static private ArrayList<User> users;
    static private int SLEEP_TIME;

    static public void startThreads() {
        scanMaps = new ArrayList<>();
        threads = new ArrayList<>();

        int userSize = users.size();
        double dividedValue = (float) scanMap.size()/userSize;
        int scanMapSplitSize = (int) Math.ceil(dividedValue);

        System.out.println("Divided Value:"+dividedValue);

        scanMaps = MyPartition.partition(scanMap,scanMapSplitSize);

        System.out.println("Scan Map Size: " + scanMaps.size());

        for (int i = 0;i<scanMaps.size();i++) {
            List<LatLng> tempMap = scanMaps.get(i);
            User tempUser = users.get(i);
            threads.add(new ObjectLoaderPTC(tempUser,tempMap,SLEEP_TIME,i));
        }

        for (Thread thread: threads) {
            thread.start();
        }
    }

    static public void setSleepTime(int SLEEP_TIME) {
        MultiAccountLoader.SLEEP_TIME = SLEEP_TIME;
    }

    static public void setUsers(ArrayList<User> users) {
        MultiAccountLoader.users = users;
    }

    static public void setScanMap(List<LatLng> scanMap) {
        MultiAccountLoader.scanMap = scanMap;
    }

    public static boolean areThreadsRunning() {
        if (threads==null) {
            return false;
        }
        if (threads.size() != 0) {
            if (threads.get(0).getState()== Thread.State.TERMINATED) {
                return false;
            }else {
                return true;
            }
        }else {
            return false;
        }
    }

    static public void cancelAllThreads() {
        while (threads != null) {
            for (Thread thread: threads) {
                try {
                    thread.interrupt();
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    threads = null;
                }
            }
        }
    }
}
