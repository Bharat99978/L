package com.network.nr;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.network.nr.databinding.ActivityMainBinding;
import java.io.PrintWriter;
import java.io.StringWriter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RadioInfoShortcut";
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the button click listener to allow users to retry manually
        binding.launchButton.setOnClickListener(v -> launchRadioInfoMenu());

        // Automatically trigger the launch of RadioInfo on app open
        launchRadioInfoMenu();
    }

    private void launchRadioInfoMenu() {
        try {
            Intent intent = new Intent();
            // Explicitly target package com.android.phone and class com.android.phone.settings.RadioInfo
            intent.setClassName("com.android.phone", "com.android.phone.settings.RadioInfo");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            // Successfully launched, close the shortcut app smoothly
            finish();
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "ActivityNotFoundException: RadioInfo activity was not found on this device.", e);
            showErrorDialog(
                "Radio Info Not Found",
                "The system RadioInfo menu (com.android.phone.settings.RadioInfo) was not found on this device.\n\n"
                    + "Your device firmware or custom ROM may have removed or relocated this hidden settings menu.",
                e
            );
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: Permission denied or RadioInfo activity is restricted by OEM/carrier.", e);
            showErrorDialog(
                "Access Restricted",
                "Access to the RadioInfo menu is restricted on this device.\n\n"
                    + "The device manufacturer (OEM) or mobile network carrier has blocked third-party applications from launching this hidden system configuration screen.",
                e
            );
        } catch (Exception e) {
            Log.e(TAG, "Unexpected " + e.getClass().getSimpleName() + " while launching RadioInfo.", e);
            showErrorDialog(
                "Failed to Open Radio Info",
                "An unexpected error occurred while attempting to launch the RadioInfo screen.",
                e
            );
        }
    }

    private void showErrorDialog(String title, String explanation, Throwable throwable) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        String errorDetails = buildErrorDetails(throwable);

        String dialogMessage = explanation + "\n\n"
            + "Technical details:\n"
            + throwable.getClass().getSimpleName() + ": "
            + (throwable.getLocalizedMessage() != null ? throwable.getLocalizedMessage() : "No message provided");

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(dialogMessage)
            .setPositiveButton("Copy Error Details", null)
            .setNegativeButton("Close App", (d, which) -> finish())
            .setNeutralButton("Dismiss", null)
            .create();

        dialog.show();

        // Custom click listener on positive button to copy without dismissing the dialog immediately
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            copyToClipboard(errorDetails);
            Toast.makeText(MainActivity.this, "Error details copied to clipboard", Toast.LENGTH_SHORT).show();
        });
    }

    private String buildErrorDetails(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        sb.append("Android Version: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        sb.append("Target Component: com.android.phone/com.android.phone.settings.RadioInfo\n");
        sb.append("Exception: ").append(throwable.getClass().getName()).append("\n");
        sb.append("Message: ").append(throwable.getMessage() != null ? throwable.getMessage() : "None").append("\n");

        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        sb.append("\nStack Trace:\n").append(sw.toString());

        return sb.toString();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("RadioInfo Error Details", text);
            clipboard.setPrimaryClip(clip);
        }
    }
}
