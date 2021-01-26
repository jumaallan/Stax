package com.hover.stax.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.hover.stax.R;
import com.hover.stax.views.StaxDialog;

public class PermissionUtils {

	public static boolean has(String[] permissions, Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
			for (String permission : permissions) {
				if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean permissionsGranted(int[] grantResults) {
		for (int result : grantResults) {
			if (result != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return grantResults.length > 0;
	}

	public static boolean hasContactPermission(Context c) {
		return Build.VERSION.SDK_INT < 23 || PermissionUtils.has(new String[]{Manifest.permission.READ_CONTACTS}, c);
	}

	public static boolean hasWritePermission(Context c) {
		return Build.VERSION.SDK_INT < 23 || PermissionUtils.has(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, c);
	}

	public static boolean hasSmsPermission(Context c) {
		return Build.VERSION.SDK_INT < 23 || PermissionUtils.has(new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, c);
	}

	public static void showInformativeBasicPermissionDialog(View.OnClickListener posListener, View.OnClickListener negListener, Activity activity) {
		new StaxDialog(activity)
				.setDialogTitle(R.string.permissions_title)
				.setDialogMessage(R.string.permissions_basic_desc)
				.setPosButton(R.string.btn_ok, posListener)
				.setNegButton(R.string.btn_cancel, negListener)
				.showIt();
	}
}