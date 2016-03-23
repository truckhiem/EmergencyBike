package com.khiemtran.emergencybike.Application;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.khiemtran.emergencybike.Models.GarageModel;
import com.khiemtran.emergencybike.R;
import com.khiemtran.emergencybike.Utils.General;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by khiem.tran on 23/03/2016.
 */
public class EmergencyBikeApplication extends Application {

    private static EmergencyBikeApplication instance;
    private static Context mContext;

    public static synchronized EmergencyBikeApplication getInstance(){
        if(instance == null)
            instance = new EmergencyBikeApplication();
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EmergencyBikeApplication.mContext = getApplicationContext();
        loginServer();
        upload();
    }

    public Context getContext(){
        return EmergencyBikeApplication.mContext;
    }

    private void loginServer(){
        final Client mKinveyClient = new Client.Builder(getContext().getString(R.string.api_key), getContext().getString(R.string.api_secret)
                , getContext()).build();
        if(!mKinveyClient.user().isUserLoggedIn()) {
            mKinveyClient.user().login("test", "test", new KinveyClientCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    Log.e("TAG", "success to save event data");
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.e("TAG", "fail to save event data");
                }
            });
        }
    }

    public void commitToServer(GarageModel mGarageModel){
        final Client mKinveyClient = new Client.Builder(getContext().getString(R.string.api_key), getContext().getString(R.string.api_secret)
                , getContext()).build();
        AsyncAppData<GarageModel> myevents = mKinveyClient.appData(getContext().getString(R.string.kinvey_collection_machine), GarageModel.class);
        myevents.save(mGarageModel, new KinveyClientCallback<GarageModel>() {
            @Override
            public void onSuccess(GarageModel eventEntity) {
                Log.e("TAG", "success to save event data");
                i++;
                commitToServer(lstGarageModel.get(i));
                return;
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("TAG", "failed to save event data", e);
            }

        });
    }

    private void upload(){
        String strGarageJson = General.loadJSONFromAsset(this, "tiem_sua_xe.json");
        lstGarageModel = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(strGarageJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                GarageModel mGarageModel = new GarageModel();
                mGarageModel.setName(jsonObject.getString("Name"));
                mGarageModel.setAddress(jsonObject.getString("Address"));
                mGarageModel.setPhone(jsonObject.getString("Phone"));
                mGarageModel.setTag(jsonObject.getString("Tag"));
                mGarageModel.setLat(jsonObject.getLong("Lat"));
                mGarageModel.setLong(jsonObject.getLong("Long"));
                lstGarageModel.add(mGarageModel);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        commitToServer(lstGarageModel.get(0));
    }

    List<GarageModel> lstGarageModel;
    int i = 0;
}
