package guepardoapps.stopme.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

public class NavigationController {
    private Context _context;

    public NavigationController(@NonNull Context context) {
        _context = context;
    }

    public void NavigateTo(
            @NonNull Class<?> activity,
            boolean finish) {
        _context.startActivity(new Intent(_context, activity));
        if (finish) {
            ((Activity) _context).finish();
        }
    }

    public void NavigateWithData(
            @NonNull Class<?> activity,
            @NonNull Bundle data,
            boolean finish) {
        Intent navigateIntent = new Intent(_context, activity);
        navigateIntent.putExtras(data);
        _context.startActivity(navigateIntent);
        if (finish) {
            ((Activity) _context).finish();
        }
    }

    public boolean NavigateToOtherApplication(
            @NonNull String packageName,
            boolean finish) {
        PackageManager packageManager = _context.getPackageManager();
        try {
            Intent startAppIntent = packageManager.getLaunchIntentForPackage(packageName);

            if (startAppIntent == null) {
                return false;
            }

            startAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            _context.startActivity(startAppIntent);

            if (finish) {
                ((Activity) _context).finish();
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}