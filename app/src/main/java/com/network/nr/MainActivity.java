package com.network.nr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.network.nr.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

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
        } catch (Exception e) {
            // Display an informative Toast message if opening is restricted (e.g. by OEM or carrier)
            Toast.makeText(this, 
                "The RadioInfo menu is restricted on this device (denied by system, OEM, or carrier).", 
                Toast.LENGTH_LONG).show();
        } finally {
            // Call finish() after launching the intent so the app closes smoothly in the background
            finish();
        }
    }
}
