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
    }

    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SETTING_REQUEST_CODE) {
            Log.d("pttt", "requestCode: " + requestCode);
            if (checkPermissionsList(permissionsForResults))
                grantListener.onGranted(true);
            else if (permissionsForResults != null) {
                ArrayList<String> notGranted = new ArrayList<>();
                ArrayList<String> neverAskAgain = new ArrayList<>();
                for (String permission : permissionsForResults) {
                    if (!activity.shouldShowRequestPermissionRationale(permission)) {
                        // user also CHECKED "never ask again"
                        neverAskAgain.add(permission);
                    } else {
                        // user did NOT check "never ask again"
                        notGranted.add(permission);
                    }
                    if (neverAskAgain.size() > 0)
                        grantListener.onNeverAskAgain(neverAskAgain.toArray(new String[0]));
                    if (notGranted.size() > 0)
                        grantListener.onNotGranted(notGranted.toArray(new String[0]));
                    grantListener.onGranted(false);
                }
            }
            return true;
        }
        return false;
    }


    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {


        if (requestCode == REQUEST_CODE) {
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
                        neverAskAgain.add(permission);
                    } else {
                        // user did NOT check "never ask again"
                        notGranted.add(permission);
                    }
                }
            }
            if (neverAskAgain.size() > 0)
                grantListener.onNeverAskAgain(neverAskAgain.toArray(new String[0]));
            if (notGranted.size() > 0)
                grantListener.onNotGranted(notGranted.toArray(new String[0]));
            grantListener.onGranted(allGranted);
            return true;
        }

        return false;
    }


    /**
     * request the user to grant permissions from app setting
     *
     * @param msg            - String: message that will show to the user. here you can explain why you need the permissions
     * @param dialogListener - DialogListener: callback to perform on buttons click
     */
    public void askPermissionsFromSetting(String msg, DialogListener dialogListener) {
        new AlertDialog.Builder(activity)
                .setTitle("Permission denied")
                .setMessage(msg)
                .setPositiveButton("RE TRY", (dialog, which) -> {
                    intentToSetting();
                    if (dialogListener != null)
                        dialogListener.onPositiveButton();
                })
                .setNegativeButton("I'M SURE", (dialog, which) -> {
                    if (dialogListener != null)
                        dialogListener.onNegativeButton();

                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();

    }


    /**
     * request the user to grant permissions from app setting
     *
     * @param msg            - String: message that will show to the user. here you can explain why you need the permissions
     * @param dialogListener - DialogListener: callback to perform on buttons click
     */
    public void askPermissionsFromSetting(String msg, @NonNull GrantListener grantListener, DialogListener dialogListener) {
        setGrantListener(grantListener);
        askPermissionsFromSetting(msg, dialogListener);

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
        String[] notGranted = notGrantedYetFilter(permissions);
        if (notGranted.length == 0) {
            grantListener.onGranted(true);
            return;
        }

        if (shouldShowRequestPermissionRationaleForAllPermissions(notGranted) || //asked in the past but "don't ask me again" set off
                !isPermissionsListAskedBefore(notGranted)) // never asked before
            askForPermission(notGranted);
        else askPermissionsFromSetting(msg, dialogListener);

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

        String[] notGranted = notGrantedYetFilter(permissions);
        if (notGranted.length == 0) {
            grantListener.onGranted(true);
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Agree", (dialog, which) -> {
                    if (shouldShowRequestPermissionRationaleForAllPermissions(notGranted) || //asked in the past but "don't ask me again" set off
                            !isPermissionsListAskedBefore(notGranted))   // never asked before
                        askForPermission(notGranted);
                    else { // don't ask me again
                        intentToSetting();
                        permissionsForResults = permissions;
                    }
                    if (dialogListener != null)
                        dialogListener.onPositiveButton();
                })
                .setNegativeButton("Decline", (dialog, which) -> {
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
            grantListener.onGranted(true);
            return;
        }

        askForPermission(notGranted);
    }


    /**
     * @param list - string array of Permissions for example: {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_CALENDAR}
     * @return true if all the permissions already granted
     */
    private boolean checkPermissionsList(String[] list) {
        for (String s : list) {
            if (!checkSinglePermission(s))
                return false;
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
    }

    /**
     * ask for permissions from the user
     *
     * @param permissions - String array of permissions
     */
    private void askForPermission(@NonNull String[] permissions) {
        markPermissionsListAsAsked(permissions);
        activity.requestPermissions(permissions, REQUEST_CODE);
    }

    /**
     * make intent to app setting
     */
    private void intentToSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivityForResult(intent, SETTING_REQUEST_CODE);
    }

    /**
     * set permissions for check on onActivityResult method when return from app setting.
     *
     * @param permissions- String array of permissions
     * @param listener     - GrantListener - replace the existing listener - set as default
     */
    public void setPermissionsForResults(String[] permissions, GrantListener listener) {
        permissionsForResults = permissions;
    }

    /**
     * set permissions for check on onActivityResult method when return from app setting.
     * using the default  grantListener
     *
     * @param permissions - String array of permissions
     */
    public void setPermissionsForResults(String[] permissions) {
        setPermissionsForResults(permissions, grantListener);
    }

    /**
     * check if permissions asked in the past
     *
     * @param permissions - the permissions to check
     * @return - true if all asked before , false if at least one not
     */
    private boolean isPermissionsListAskedBefore(@NonNull String[] permissions) {
        for (String permission : permissions) {
            if (!isPermissionAskedBefore(permission))
                return false;
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
        return notGranted.toArray(new String[0]);
    }

    private boolean shouldShowRequestPermissionRationaleForAllPermissions(@NonNull String[] permissions) {
        for (String permission : permissions) {
            if (!shouldShowRequestPermissionRationale(permission)) {
                Log.d("pttt", "shouldShowRequestPermissionRationaleForAllPermissions: return false ");
                return false;
            }
        }
        return true;
    }

    private boolean shouldShowRequestPermissionRationale(String permission) {

        return activity.shouldShowRequestPermissionRationale(permission);
    }


}
