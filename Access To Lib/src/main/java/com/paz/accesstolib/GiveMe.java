package com.paz.accesstolib;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

public class GiveMe {
    private final Activity activity;
    private final String TAG = getClass().getSimpleName();
    private final int REQUEST_CODE = 2901;
    private final int SETTING_REQUEST_CODE = 1803;
    private GrantListener grantListener;
    private String[] permissionsForResults;
    private final MySharedPreferences msp;
    private boolean debug;

    /**
     * constructor
     *
     * @param activity      - the current activity
     * @param grantListener - GrantListener: callbacks to perform after onRequestPermissionsResult
     */
    public GiveMe(Activity activity, GrantListener grantListener) {
        this(activity);
        this.grantListener = grantListener;


    }

    /**
     * constructor
     * note! grantListener is null.
     *
     * @param activity - the current activity
     */
    public GiveMe(Activity activity) {
        this.activity = activity;
        msp = new MySharedPreferences(activity);
        debug = false;

    }

    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        logger("at onActivityResult");
        if (requestCode == SETTING_REQUEST_CODE) {
            logger("requestCode is ok, start checking");
            if (checkPermissionsList(permissionsForResults)) {
                logger("all granted");
                grantListener.onGranted(true);
            } else if (permissionsForResults != null) {
                ArrayList<String> notGranted = new ArrayList<>();
                ArrayList<String> neverAskAgain = new ArrayList<>();
                for (String permission : permissionsForResults) {
                    if (!activity.shouldShowRequestPermissionRationale(permission)) {
                        // user also CHECKED "never ask again"
                        logger("the permission " + permission + " mark as \"don't ask again\"");
                        neverAskAgain.add(permission);
                    } else {
                        // user did NOT check "never ask again"
                        logger("the permission " + permission + " not granted");
                        notGranted.add(permission);
                    }
                    if (neverAskAgain.size() > 0) {
                        logger("the permissions " + neverAskAgain + " mark as \"don't ask again\"");
                        grantListener.onNeverAskAgain(neverAskAgain.toArray(new String[0]));
                    }
                    if (notGranted.size() > 0) {
                        logger("the permissions " + notGranted + " not granted");
                        grantListener.onNotGranted(notGranted.toArray(new String[0]));
                    }
                    logger("all granted? false");
                    grantListener.onGranted(false);
                }
            }
            return true;
        }
        return false;
    }


    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        logger("at onRequestPermissionsResult");
        if (requestCode == REQUEST_CODE) {
            logger("REQUEST_CODE is ok, start checking");
            ArrayList<String> notGranted = new ArrayList<>();
            ArrayList<String> neverAskAgain = new ArrayList<>();
            boolean allGranted = true;
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allGranted = false;
                    // user rejected the permission
                    if (!activity.shouldShowRequestPermissionRationale(permission)) {
                        // user also CHECKED "never ask again"
                        logger("the permission " + permission + " mark as \"don't ask again\"");
                        neverAskAgain.add(permission);
                    } else {
                        // user did NOT check "never ask again"
                        logger("the permission " + permission + " not granted");
                        notGranted.add(permission);
                    }
                } else {
                    logger("the permission " + permission + " granted");

                }
            }
            if (neverAskAgain.size() > 0) {
                logger("the permissions " + neverAskAgain + " mark as \"don't ask again\"");
                grantListener.onNeverAskAgain(neverAskAgain.toArray(new String[0]));
            }
            if (notGranted.size() > 0) {
                logger("the permissions " + notGranted + " not granted");

                grantListener.onNotGranted(notGranted.toArray(new String[0]));
            }
            logger("all granted?  " + allGranted);
            grantListener.onGranted(allGranted);
            return true;
        }
        logger("REQUEST_CODE is not ok");

        return false;
    }


    /**
     * request the user to grant permissions from app setting
     * set permissions for check on onActivityResult method when return from app setting.
     *
     * @param permissions-   String array of permissions
     * @param msg            - String: message that will show to the user. here you can explain why you need the permissions
     * @param dialogListener - DialogListener: callback to perform on buttons click
     */
    public void askPermissionsFromSetting(String msg, String[] permissions, DialogListener dialogListener) {
        logger("at askPermissionsFromSetting");
        permissionsForResults = permissions;
        new AlertDialog.Builder(activity)
                .setTitle("Permission denied")
                .setMessage(msg)
                .setPositiveButton("RE TRY", (dialog, which) -> {
                    logger("Positive Button pressed");
                    intentToSetting();
                    if (dialogListener != null)
                        dialogListener.onPositiveButton();
                })
                .setNegativeButton("I'M SURE", (dialog, which) -> {
                    logger("Negative Button pressed");
                    if (dialogListener != null)
                        dialogListener.onNegativeButton();

                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();

    }


    /**
     * request the user to grant permissions from app setting
     * set permissions for check on onActivityResult method when return from app setting.
     *
     * @param permissions-   String array of permissions
     * @param msg            - String: message that will show to the user. here you can explain why you need the permissions
     * @param dialogListener - DialogListener: callback to perform on buttons click
     */
    public void askPermissionsFromSetting(String msg, String[] permissions, @NonNull GrantListener grantListener, DialogListener dialogListener) {
        setGrantListener(grantListener);
        askPermissionsFromSetting(msg, permissions, dialogListener);

    }


    /**
     * request permissions from the user. in the user select "don't ask me again" dialog will open.
     * if select "re-try" button the user will transfer to app setting to grant permissions
     *
     * @param permissions    - String array of permissions
     * @param grantListener  - GrantListener callbacks to perform after onRequestPermissionsResult - set as default
     * @param msg            - String: message that will show to the user. here you can explain why you need the permissions
     * @param dialogListener - DialogListener: callback to perform on buttons click
     */
    public void requestPermissionsWithForce(@NonNull String[] permissions, @NonNull GrantListener grantListener, String msg, DialogListener dialogListener) {
        setGrantListener(grantListener);
        requestPermissionsWithForce(permissions, msg, dialogListener);

    }

    /**
     * request permissions from the user. in the user select "don't ask me again" dialog will open.
     * if select "re-try" button the user will transfer to app setting to grant permissions
     *
     * @param permissions    - String array of permissions
     * @param msg            - String: message that will show to the user. here you can explain why you need the permissions
     * @param dialogListener - DialogListener: callback to perform on buttons click
     */
    public void requestPermissionsWithForce(@NonNull String[] permissions, String msg, DialogListener dialogListener) {
        logger("at requestPermissionsWithForce");
        String[] notGranted = notGrantedYetFilter(permissions);
        if (notGranted.length == 0) {
            logger("noting to ask, all granted");
            grantListener.onGranted(true);
            return;
        }

        if (shouldShowRequestPermissionRationaleForAllPermissions(notGranted) || //asked in the past but "don't ask me again" set off
                !isPermissionsListAskedBefore(notGranted)) // never asked before
            askForPermission(notGranted);
        else askPermissionsFromSetting(msg, permissions, dialogListener);

    }


    /**
     * request permissions from the user with dialog first. If the user select "agree" the grant permissions dialogs will show
     * or the user will transfer to app setting to grant permissions in case he select "don't ask me again"
     *
     * @param permissions    - String array of permissions
     * @param grantListener  - GrantListener callbacks to perform after onRequestPermissionsResult- set as default
     * @param title          - String: dialog title
     * @param msg            - String: message that will show to the user. here you can explain why you need the permissions
     * @param dialogListener - DialogListener: callback to perform on buttons click
     */
    public void requestPermissionsWithDialog(@NonNull String[] permissions, @NonNull GrantListener grantListener, String title, String msg, DialogListener dialogListener) {
        setGrantListener(grantListener);
        requestPermissionsWithDialog(permissions, title, msg, dialogListener);


    }

    /**
     * request permissions from the user with dialog first. If the user select "agree" the grant permissions dialogs will show
     * or the user will transfer to app setting to grant permissions in case he select "don't ask me again". using the default GrantListener
     *
     * @param permissions    - String array of permissions
     * @param title          - String: dialog title
     * @param msg            - String: message that will show to the user. here you can explain why you need the permissions
     * @param dialogListener - DialogListener: callback to perform on buttons click
     */
    public void requestPermissionsWithDialog(@NonNull String[] permissions, String title, String msg, DialogListener dialogListener) {
        logger("at requestPermissionsWithDialog");
        String[] notGranted = notGrantedYetFilter(permissions);
        if (notGranted.length == 0) {
            logger("noting to ask, all granted");
            grantListener.onGranted(true);
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Agree", (dialog, which) -> {
                    logger("Positive Button pressed");
                    if (shouldShowRequestPermissionRationaleForAllPermissions(notGranted) || //asked in the past but "don't ask me again" set off
                            !isPermissionsListAskedBefore(notGranted)) { // never asked before
                        askForPermission(notGranted);
                    } else { // don't ask me again
                        intentToSetting();
                        permissionsForResults = permissions;
                    }
                    if (dialogListener != null)
                        dialogListener.onPositiveButton();
                })
                .setNegativeButton("Decline", (dialog, which) -> {
                    logger("Negative Button pressed");

                    if (dialogListener != null)
                        dialogListener.onNegativeButton();

                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    /**
     * request permissions from the user.
     *
     * @param permissions   - String array of permissions
     * @param grantListener - GrantListener callbacks to perform after onRequestPermissionsResult
     */
    public void requestPermissions(@NonNull String[] permissions, @NonNull GrantListener grantListener) {
        setGrantListener(grantListener);
        requestPermissions(permissions);


    }

    /**
     * request permissions from the user. using the default GrantListener.
     * this method not handling "don't ask me again" but if permission mark with this flag you will
     * get the permission in the onNeverAskAgain method in  GrantListener callback
     *
     * @param permissions - String array of permissions
     */
    public void requestPermissions(@NonNull String[] permissions) {
        String[] notGranted = notGrantedYetFilter(permissions);
        if (notGranted.length == 0) {
            logger("noting to ask, all granted");
            grantListener.onGranted(true);
            return;
        }
        logger("request permissions");
        askForPermission(notGranted);
    }


    /**
     * @param list - string array of Permissions for example: {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_CALENDAR}
     * @return true if all the permissions already granted
     */
    private boolean checkPermissionsList(String[] list) {
        for (String s : list) {
            if (!checkSinglePermission(s)) {
                logger("the permission " + s + " not granted yet");
                return false;
            }
        }
        return true;
    }

    /**
     * @param per - string Permission for example: Manifest.permission.ACCESS_FINE_LOCATION
     * @return true if the permission already granted
     */
    private boolean checkSinglePermission(String per) {

        return (activity.checkSelfPermission(per) == PackageManager.PERMISSION_GRANTED);

    }

    /**
     * set the default GrantListener
     *
     * @param grantListener - GrantListener - set as default
     */
    public void setGrantListener(GrantListener grantListener) {
        this.grantListener = grantListener;
        logger("new GrantListener was set");
    }

    /**
     * ask for permissions from the user
     *
     * @param permissions - String array of permissions
     */
    private void askForPermission(@NonNull String[] permissions) {
        logger("at askForPermission");
        markPermissionsListAsAsked(permissions);
        activity.requestPermissions(permissions, REQUEST_CODE);
    }

    /**
     * make intent to app setting
     */
    private void intentToSetting() {
        logger("perform intent to the app setting");
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivityForResult(intent, SETTING_REQUEST_CODE);
    }

    /**
     * check if permissions asked in the past
     *
     * @param permissions - the permissions to check
     * @return - true if all asked before , false if at least one not
     */
    private boolean isPermissionsListAskedBefore(@NonNull String[] permissions) {
        for (String permission : permissions) {
            if (!isPermissionAskedBefore(permission)) {
                logger("the permission " + permission + " never asked before");
                return false;
            }
        }
        return true;
    }

    /**
     * check if permission asked in the past
     *
     * @param permission - the permission to check
     * @return - true if asked before , false if not
     */
    private boolean isPermissionAskedBefore(@NonNull String permission) {
        return msp.getBoolean(permission, false);
    }

    /**
     * mark asked permissions
     *
     * @param permissions - List of permissions to mark that asked in the past
     */
    private void markPermissionsListAsAsked(@NonNull String[] permissions) {
        for (String permission : permissions) {
            logger("mark the permission " + permission + " as asked");
            msp.putBoolean(permission, true);
        }
    }

    /**
     * check which permissions already granted
     *
     * @param permissions - the permissions that need to ask
     * @return the permissions that not granted yed
     */
    private String[] notGrantedYetFilter(@NonNull String[] permissions) {
        ArrayList<String> notGranted = new ArrayList<>();
        for (String permission : permissions) {
            if (!checkSinglePermission(permission))
                notGranted.add(permission);
        }
        logger("notGrantedYetFilter: the list after filtering: " + notGranted);
        return notGranted.toArray(new String[0]);
    }

    private boolean shouldShowRequestPermissionRationaleForAllPermissions(@NonNull String[] permissions) {
        for (String permission : permissions) {
            if (!shouldShowRequestPermissionRationale(permission)) {
                logger("at shouldShowRequestPermissionRationaleForAllPermissions\n" +
                        "\"shouldShowRequestPermissionRationale return false for permission \" " + permission);
                return false;
            }
        }
        return true;
    }

    private boolean shouldShowRequestPermissionRationale(String permission) {
        return activity.shouldShowRequestPermissionRationale(permission);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        logger("debug is set to true and should be for debugging only");
    }

    private void logger(String msg) {
        if (debug)
            Log.d(TAG, msg);
    }
}
