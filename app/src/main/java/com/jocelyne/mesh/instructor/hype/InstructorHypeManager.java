package com.jocelyne.mesh.instructor.hype;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;

import com.hypelabs.hype.Error;
import com.hypelabs.hype.Hype;
import com.hypelabs.hype.Instance;
import com.hypelabs.hype.Message;
import com.hypelabs.hype.MessageInfo;
import com.hypelabs.hype.MessageObserver;
import com.hypelabs.hype.NetworkObserver;
import com.hypelabs.hype.StateObserver;
import com.jocelyne.mesh.session_management.Student;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class InstructorHypeManager implements StateObserver, NetworkObserver, MessageObserver {

    private static final String TAG = InstructorHypeManager.class.getName();
    public static String announcement = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;

    private boolean isConfigured = false;
    private Activity activity;

    /*
    Instructor variables
     */
    private HashSet<Long> hashSet; // predefined hashset of student IDs of type Long because Instance user identifier is of type Long
    private Map<Long, Student> presentStudentsMap;

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    protected void configureHype() {
        if(isConfigured){
            return;
        }

        // The application context is used to query the user for permissions, such as using
        // the Bluetooth adapter or enabling Wi-Fi. The context must be set before anything
        // else is attempted, otherwise resulting in an exception being thrown.
        Hype.setContext(activity.getApplicationContext());

        // Adding itself as an Hype state observer makes sure that the application gets
        // notifications for lifecycle events being triggered by the Hype SDK. These
        // events include starting and stopping, as well as some error handling.
        Hype.addStateObserver(this);

        // Network observer notifications include other devices entering and leaving the
        // network. When a device is found all observers get a onHypeInstanceFound
        // notification, and when they leave onHypeInstanceLost is triggered instead.
        // This observer also gets notifications for onHypeInstanceResolved when an
        // instance is resolved.
        Hype.addNetworkObserver(this);

        // Message notifications indicate when messages are received, sent, or delivered.
        // Such callbacks are called with progress tracking indication.
        Hype.addMessageObserver(this);

        try {
            Hype.setAnnouncement("class".getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Hype.setAnnouncement(null);
            e.printStackTrace();
        }

        // App identifiers are used to segregate the network. Apps with different identifiers
        // do not communicate with each other, although they still cooperate on the network.
        Hype.setAppIdentifier("febbbe71");

        // Since Android 6.0 (API 23) Bluetooth Low Energy requires the ACCESS_COARSE_LOCATION
        // permission in order to work. The `requestPermissions()` method checks whether it's
        // necessary to ask for this permission and goes through with the request if that's the
        // case. The `requestHypeToStart()` method is called when the user replies to the permission
        // request. If the permission is denied, BLE will not work.
        OngoingClassActivity ongoingClassActivity = OngoingClassActivity.getDefaultInstance();
        ongoingClassActivity.requestPermissions(ongoingClassActivity);
        isConfigured = true;
    }

    public void requestHypeToStart() {
        // Requesting Hype to start is equivalent to requesting the device to publish
        // itself on the network and start browsing for other devices in proximity. If
        // everything goes well, the onHypeStart() observer method gets called, indicating
        // that the device is actively participating on the network.
        Hype.start();
    }

    protected void requestHypeToStop() {

        // The current release has a known issue with Bluetooth Low Energy that causes all
        // connections to drop when the SDK is stopped. This is an Android issue.
        Hype.stop();
    }

    @Override
    public void onHypeStart() {
        Log.i(TAG, "Hype started");
    }

    @Override
    public void onHypeStop(Error error) {

        String description = "";

        if (error != null) {
            description = String.format("[%s]", error.getDescription());
        }

        Log.i(TAG, String.format("Hype stopped [%s]", description));
    }

    @Override
    public void onHypeFailedStarting(Error error) {
        Log.i(TAG, String.format("Hype failed starting [%s]", error.toString()));

        final String failedMsg = error == null? "" : String.format("Suggestion: %s\nDescription: %s\nReason: %s",
                error.getSuggestion(), error.getDescription(), error.getReason());

        this.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Hype failed starting");
                builder.setMessage(failedMsg);
                builder.setPositiveButton(android.R.string.ok, null);
                builder.show();
            }
        });
    }

    @Override
    public void onHypeReady() {

        Log.i(TAG, String.format("Hype is ready"));

        requestHypeToStart();
    }

    @Override
    public void onHypeStateChange() {

        Log.i(TAG, String.format("Hype changed state to [%d] (Idle=0, Starting=1, Running=2, Stopping=3)", Hype.getState().getValue()));
    }

    @Override
    public String onHypeRequestAccessToken(int i) {
        // Access the app settings (https://hypelabs.io/apps/) to find an access token to use here.
        return "99ce298fb942cce6";
    }

    boolean shouldResolveInstance(Instance instance)
    {
        // This method can be used to decide whether an instance is interesting
        // Resolve instance only if the student ID is in predefined hashset of IDs (only if student is registered in class)
        return hashSet.contains(instance.getUserIdentifier());
    }

    @Override
    public void onHypeInstanceFound(Instance instance) {
        Log.i(TAG, String.format("Hype found instance: %s, with student ID: %s",
                instance.getStringIdentifier(), instance.getUserIdentifier()));

        if(shouldResolveInstance(instance)){
            try {
                Hype.setAnnouncement("Check-in successful!".getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Hype.setAnnouncement(null);
                e.printStackTrace();
            }
            Hype.resolve(instance);
        } else {
            try {
                Hype.setAnnouncement("class".getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Hype.setAnnouncement(null);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onHypeInstanceLost(Instance instance, Error error) {
        Log.i(TAG, String.format("Hype lost instance: %s [%s], with student ID: %s",
                instance.getStringIdentifier(), error.getDescription(), instance.getUserIdentifier()));

        removeFromResolvedInstancesMap(instance);
    }

    @Override
    public void onHypeInstanceResolved(Instance instance) {
        Log.i(TAG, String.format("Hype resolved instance: %s, with student ID: %s",
                instance.getStringIdentifier(), instance.getUserIdentifier()));

        // This device is now capable of communicating
        addToResolvedInstancesMap(instance);
    }

    @Override
    public void onHypeInstanceFailResolving(Instance instance, Error error) {
        Log.i(TAG, String.format("Hype failed resolving instance: %s [%s], with student ID: %s",
                instance.getStringIdentifier(), error.getDescription(), instance.getUserIdentifier()));
    }

    public Map<Long, Student> getPresentStudentsMap() {
        if (presentStudentsMap == null) {
            presentStudentsMap = new HashMap<>();
        }
        return presentStudentsMap;
    }

    public void addToResolvedInstancesMap(Instance instance) {
        // Instances should be strongly kept by some data structure. Their identifiers
        // are useful for keeping track of which instances are ready to communicate.
        getPresentStudentsMap().put(instance.getUserIdentifier(), null);
        // TODO send student info later after resolving

        // Notify the contact activity to refresh the UI
        OngoingClassActivity ongoingClassActivity = OngoingClassActivity.getDefaultInstance();

        if (ongoingClassActivity != null) {
            ongoingClassActivity.notifyStudentsChanged();
        }
    }

    public void removeFromResolvedInstancesMap(Instance instance) {
        // Cleaning up is always a good idea. It's not possible to communicate with instances
        // that were previously lost.
        getPresentStudentsMap().remove(instance.getUserIdentifier());

        // Notify the contact activity to refresh the UI
        OngoingClassActivity ongoingClassActivity = OngoingClassActivity.getDefaultInstance();

        if (ongoingClassActivity != null) {
            ongoingClassActivity.notifyStudentsChanged();
        }
    }

    @Override
    public void onHypeMessageReceived(Message message, Instance instance) {

    }

    @Override
    public void onHypeMessageFailedSending(MessageInfo messageInfo, Instance instance, Error error) {

    }

    @Override
    public void onHypeMessageSent(MessageInfo messageInfo, Instance instance, float v, boolean b) {

    }

    @Override
    public void onHypeMessageDelivered(MessageInfo messageInfo, Instance instance, float v, boolean b) {

    }
}
