package com.king.hashtag;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.consent.DebugGeography;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ConsentSDK {

    public abstract static class ConsentCallback {
        abstract public void onResult(boolean isRequestLocationInEeaOrUnknown);
    }

    public abstract static class ConsentStatusCallback {
        abstract public void onResult(boolean isRequestLocationInEeaOrUnknown, int isConsentPersonalized);
    }

    public abstract static class ConsentInformationCallback {
        abstract public void onResult(ConsentInformation consentInformation, ConsentStatus consentStatus);
        abstract public void onFailed(ConsentInformation consentInformation, String reason);
    }

    public abstract static class LocationIsEeaOrUnknownCallback {
        abstract public void onResult(boolean isRequestLocationInEeaOrUnknown);
    }

    private static String ads_preference = "ads_preference";
    private static String user_status = "user_status";
    private static boolean PERSONALIZED = true;
    private static boolean NON_PERSONALIZED = false;
    private static String preferences_name = "com.consentsdk";

    private Context context;
    private ConsentForm form;
    private String LOG_TAG = "ID_LOG";
    private String DEVICE_ID = "";
    private boolean DEBUG = false;
    private String privacyURL;
    private String publisherId;
    public ConsentSDK consentSDK;

    // Admob banner test id
    private static String DUMMY_BANNER = "ca-app-pub-3940256099942544/6300978111";

    private SharedPreferences settings;


    // Builder class
    public static class Builder {

        private Context context;
        private String LOG_TAG = "ID_LOG";
        private String DEVICE_ID = "";
        private boolean DEBUG = false;
        private String privacyURL;
        private String publisherId;

        // Initialize Builder
        public Builder(Context context) {
            this.context = context;
        }

        // Add test device id
        public Builder addTestDeviceId(String device_id) {
            this.DEVICE_ID = device_id;
            this.DEBUG = true;
            return this;
        }

        // Add privacy policy
        public Builder addPrivacyPolicy(String privacyURL) {
            this.privacyURL = privacyURL;
            return this;
        }

        // Add Publisher Id
        public Builder addPublisherId(String publisherId) {
            this.publisherId = publisherId;
            return this;
        }

        // Add Logcat id
        public Builder addCustomLogTag(String LOG_TAG) {
            this.LOG_TAG = LOG_TAG;
            return this;
        }

        // Build
        public ConsentSDK build() {
            if(this.DEBUG) {
                ConsentSDK consentSDK = new ConsentSDK(context, publisherId, privacyURL, true);
                consentSDK.LOG_TAG = this.LOG_TAG;
                consentSDK.DEVICE_ID = this.DEVICE_ID;
                return consentSDK;
            } else {
                return new ConsentSDK(context, publisherId, privacyURL);
            }
        }
    }


    // Initialize debug
    public ConsentSDK(Context context, String publisherId, String privacyURL, boolean DEBUG) {
        this.context = context;
        this.settings = initPreferences(context);
        this.publisherId = publisherId;
        this.privacyURL = privacyURL;
        this.DEBUG = DEBUG;
        this.consentSDK = this;
    }

    // Initialize SharedPreferences
    private static SharedPreferences initPreferences(Context context) {
        return context.getSharedPreferences(preferences_name, Context.MODE_PRIVATE);
    }

    // Initialize production
    public ConsentSDK(Context context, String publisherId, String privacyURL) {
        this.context = context;
        this.settings = context.getSharedPreferences(preferences_name, Context.MODE_PRIVATE);
        this.publisherId = publisherId;
        this.privacyURL = privacyURL;
        this.consentSDK = this;
    }

    // Consent status
    public static boolean isConsentPersonalized(Context context) {
        SharedPreferences settings = initPreferences(context);
        return settings.getBoolean(ads_preference, PERSONALIZED);
    }

    // Consent is personalized
    private void consentIsPersonalized() {
        settings.edit().putBoolean(ads_preference, PERSONALIZED).apply();
    }

    // Consent is non personalized
    private void consentIsNonPersonalized() {
        settings.edit().putBoolean(ads_preference, NON_PERSONALIZED).apply();
    }

    // Consent is within
    private void updateUserStatus(boolean status) {
        settings.edit().putBoolean(user_status, status).apply();
    }

    // Get AdRequest
    public static AdRequest getAdRequest(Context context) {
        if(isConsentPersonalized(context)) {
            return new AdRequest.Builder().build();
        } else {
            return new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, getNonPersonalizedAdsBundle())
                    .build();
        }
    }

    // Get Non Personalized Ads Bundle
    private static Bundle getNonPersonalizedAdsBundle() {
        Bundle extras = new Bundle();
        extras.putString("npa", "1");
        return extras;
    }

    // Consent information
    private void initConsentInformation(final ConsentInformationCallback callback) {
        final ConsentInformation consentInformation = ConsentInformation.getInstance(context);
        if(DEBUG) {
            if(!DEVICE_ID.isEmpty()) {
                consentInformation.addTestDevice(DEVICE_ID);
            }
            consentInformation.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        }
        String[] publisherIds = {publisherId};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                if(callback != null) {
                    callback.onResult(consentInformation, consentStatus);
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String reason) {
                callback.onFailed(consentInformation, reason);
            }
        });
    }

    // Check if the location is EEA
    public void isRequestLocationIsEeaOrUnknown(final LocationIsEeaOrUnknownCallback callback) {
        // Get Consent information
        initConsentInformation(new ConsentInformationCallback() {
            @Override
            public void onResult(ConsentInformation consentInformation, ConsentStatus consentStatus) {
                callback.onResult(consentInformation.isRequestLocationInEeaOrUnknown());
            }

            @Override
            public void onFailed(ConsentInformation consentInformation, String reason) {
                callback.onResult(false);
            }
        });
    }

    // Check the user location
    public static boolean isUserLocationWithinEea(Context context) {
        return initPreferences(context).getBoolean(user_status, false);
    }

    // Initialize Consent SDK
    public void checkConsent2(final ConsentCallback callback) {
        // Initialize consent information
        initConsentInformation(new ConsentInformationCallback() {
            @Override
            public void onResult(ConsentInformation consentInformation, ConsentStatus consentStatus) {
                if(consentInformation.isRequestLocationInEeaOrUnknown()) {
                    requestConsent(new ConsentStatusCallback() {
                        @Override
                        public void onResult(boolean isRequestLocationInEeaOrUnknown, int isConsentPersonalized) {
                            callback.onResult(isRequestLocationInEeaOrUnknown);
                        }
                    });
                }
                // Update user status
                updateUserStatus(consentInformation.isRequestLocationInEeaOrUnknown());
            }

            @Override
            public void onFailed(ConsentInformation consentInformation, String reason) {
                if(DEBUG) {
                    Log.d(LOG_TAG, "Failed to update: $reason");
                }
                // Update user status
                updateUserStatus(consentInformation.isRequestLocationInEeaOrUnknown());
            }
        });
    }

    //
    public void checkConsent(final ConsentCallback callback) {
        // Initialize consent information
        initConsentInformation(new ConsentInformationCallback() {
            @Override
            public void onResult(ConsentInformation consentInformation, ConsentStatus consentStatus) {
                // Switch consent
                switch(consentStatus) {
                    case UNKNOWN:
                        // Debugging
                        if (DEBUG) {
                            Log.d(LOG_TAG, "Unknown Consent");
                            Log.d(LOG_TAG, "User location within EEA: " + consentInformation.isRequestLocationInEeaOrUnknown());
                        }
                        // Check the user status
                        if(consentInformation.isRequestLocationInEeaOrUnknown()) {
                            requestConsent(new ConsentStatusCallback() {
                                @Override
                                public void onResult(boolean isRequestLocationInEeaOrUnknown, int isConsentPersonalized) {
                                    callback.onResult(isRequestLocationInEeaOrUnknown);
                                }
                            });
                        } else {
                            consentIsPersonalized();
                            // Callback
                            callback.onResult(consentInformation.isRequestLocationInEeaOrUnknown());
                        }
                        break;
                    case NON_PERSONALIZED:
                        consentIsNonPersonalized();
                        // Callback
                        callback.onResult(consentInformation.isRequestLocationInEeaOrUnknown());
                        break;
                    default:
                        consentIsPersonalized();
                        // Callback
                        callback.onResult(consentInformation.isRequestLocationInEeaOrUnknown());
                        break;
                }
                // Update user status
                updateUserStatus(consentInformation.isRequestLocationInEeaOrUnknown());
            }

            @Override
            public void onFailed(ConsentInformation consentInformation, String reason) {
                if(DEBUG) {
                    Log.d(LOG_TAG, "Failed to update: $reason");
                }
                // Update user status
                updateUserStatus(consentInformation.isRequestLocationInEeaOrUnknown());
            }
        });
    }

    // Request Consent
    public void requestConsent(final ConsentStatusCallback callback) {
        URL privacyUrl = null;
        try {
            privacyUrl = new URL(privacyURL);
        } catch (MalformedURLException e) {
//            e.printStackTrace();
        }
        form = new ConsentForm.Builder(context, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        if(DEBUG) {
                            Log.d(LOG_TAG, "Consent Form is loaded!");
                        }
                        form.show();
                    }

                    @Override
                    public void onConsentFormError(String reason) {
                        if(DEBUG) {
                            Log.d(LOG_TAG, "Consent Form ERROR: $reason");
                        }
                        // Callback on Error
                        if (callback != null) {
                            consentSDK.isRequestLocationIsEeaOrUnknown(new LocationIsEeaOrUnknownCallback() {
                                @Override
                                public void onResult(boolean isRequestLocationInEeaOrUnknown) {
                                    callback.onResult(isRequestLocationInEeaOrUnknown, -1);
                                }
                            });
                        }
                    }

                    @Override
                    public void onConsentFormOpened() {
                        if(DEBUG) {
                            Log.d(LOG_TAG, "Consent Form is opened!");
                        }
                    }

                    @Override
                    public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        if(DEBUG) {
                            Log.d(LOG_TAG, "Consent Form Closed!");
                        }
                        final int isConsentPersonalized;
                        // Check the consent status and save it
                        switch (consentStatus) {
                            case NON_PERSONALIZED:
                                consentIsNonPersonalized();
                                isConsentPersonalized = 0;

                                ArrayList<ActivityBanner> list = new ArrayList<ActivityBanner>();
                                list.add(new ActivityBanner(MainActivity.context,MainActivity.unitBanner));
                                list.add(new ActivityBanner(HelpActivity.context,HelpActivity.unitBanner));
                                list.add(new ActivityBanner(SettingsActivity.context,SettingsActivity.unitBanner));
                                list.add(new ActivityBanner(TagsActivity.context,TagsActivity.unitBanner));
                                refreshAds(list);

                                break;

                            default:
                                consentIsPersonalized();
                                isConsentPersonalized = 1;

                                ArrayList<ActivityBanner> list2 = new ArrayList<ActivityBanner>();
                                list2.add(new ActivityBanner(MainActivity.context,MainActivity.unitBanner));
                                list2.add(new ActivityBanner(HelpActivity.context,HelpActivity.unitBanner));
                                list2.add(new ActivityBanner(SettingsActivity.context,SettingsActivity.unitBanner));
                                list2.add(new ActivityBanner(TagsActivity.context,TagsActivity.unitBanner));
                                refreshAds(list2);

                                break;
                        }
                        // Callback
                        if(callback != null) {
                            consentSDK.isRequestLocationIsEeaOrUnknown(new LocationIsEeaOrUnknownCallback() {
                                @Override
                                public void onResult(boolean isRequestLocationInEeaOrUnknown) {
                                    callback.onResult(isRequestLocationInEeaOrUnknown, isConsentPersonalized);
                                }
                            });
                        }
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build();
        form.load();
    }


    public static void refreshAds(ArrayList<ActivityBanner> list ){

        for (int j =0;j<list.size();j++){
            try {
                if(((LinearLayout) list.get(j).getAdview()).getChildCount() > 0)
                    ((LinearLayout) list.get(j).getAdview()).removeAllViews();
                SettingsClass.admobBannerCall(list.get(j).getActivity(),list.get(j).getAdview());
            }catch (Exception ex){ }
        }
    }



    public class ActivityInterstitialAd{
    Activity activity;
    InterstitialAd interstitialAd;
    public ActivityInterstitialAd(Activity activity, InterstitialAd interstitialAd){
        this.activity=activity;
        this.interstitialAd = interstitialAd;
    }
        public InterstitialAd getInterstitialAd(){
            return this.interstitialAd;
        }

        public Activity getActivity() {
            return activity;
        }

        public void setActivity(Activity activity) {
            this.activity = activity;
        }

        public void setInterstitialAd(InterstitialAd interstitialAd) {
            this.interstitialAd = interstitialAd;
        }
    }

    public class ActivityBanner{
        Activity activity;
        LinearLayout adview;
        public ActivityBanner(Activity activity, LinearLayout adview){
            this.activity=activity;
            this.adview = adview;
        }
        public LinearLayout getAdview(){
            return this.adview;
        }

        public Activity getActivity() {
            return activity;
        }

        public void setActivity(Activity activity) {
            this.activity = activity;
        }

        public void setAdview(LinearLayout adview) {
            this.adview = adview;
        }
    }


}


