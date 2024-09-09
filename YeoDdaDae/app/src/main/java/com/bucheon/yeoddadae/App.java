package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.util.Log;

import com.skt.Tmap.TMapTapi;

public class App extends Application {

    boolean apiKeyCertified;
    String loginId;

    @Override
    public void onCreate() {
        super.onCreate();

        final String API_KEY = "iqTSQ2hMuj8E7t2sy3WYA5m73LuX4iUD5iHgwRGf";
        TMapTapi tmaptapi = new TMapTapi(this);
        tmaptapi.setOnAuthenticationListener(new TMapTapi.OnAuthenticationListenerCallback() {
            @Override
            public void SKTMapApikeySucceed() {
                Log.d(TAG, "키 인증 성공");
                apiKeyCertified = true;
            }
            @Override
            public void SKTMapApikeyFailed(String s) {
                Log.d(TAG, "키 인증 실패");
                apiKeyCertified = false;
            }
        });
        tmaptapi.setSKTMapAuthentication(API_KEY);
    }

    public boolean isApiKeyCertified() {
        return apiKeyCertified;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }
}
