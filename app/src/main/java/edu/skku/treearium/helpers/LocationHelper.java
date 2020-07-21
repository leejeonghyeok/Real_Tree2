package edu.skku.treearium.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public final class LocationHelper {
    private static final int Location_CODE = 0;
    private static final String Location_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String Location_PERMISSION2 = Manifest.permission.ACCESS_COARSE_LOCATION;

    /** Check to see we have the necessary permissions for this app. */
    public static boolean hasLocationPermission(Activity activity) {
        return ((ContextCompat.checkSelfPermission(activity, Location_PERMISSION)
                == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(activity, Location_PERMISSION2)
                == PackageManager.PERMISSION_GRANTED));
    }

    /** Check to see we have the necessary permissions for this app, and ask for them if we don't. */
    public static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity, new String[] {Location_PERMISSION}, Location_CODE);
    }

    /** Check to see if we need to show the rationale for this permission. */
    public static boolean shouldShowRequestPermissionRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, Location_PERMISSION);
    }

    /** Launch Application Setting to grant permission. */
    public static void launchPermissionSettings(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }
}
