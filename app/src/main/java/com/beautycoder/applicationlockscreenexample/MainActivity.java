package com.beautycoder.applicationlockscreenexample;

import androidx.lifecycle.Observer;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.beautycoder.pflockscreen.PFFLockScreenConfiguration;
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment;
import com.beautycoder.pflockscreen.security.PFResult;
import com.beautycoder.pflockscreen.security.PFSecurityManager;
import com.beautycoder.pflockscreen.viewmodels.PFPinCodeViewModel;

public class MainActivity extends AppCompatActivity {
     PFLockScreenFragment pfLockScreenFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        showLockScreenFragment();
        //PFSecurityManager.getInstance().setPinCodeHelper(new TestPFPinCodeHelperImpl());
    }

    private final PFLockScreenFragment.OnPFLockScreenCodeCreateListener mCodeCreateListener =
            new PFLockScreenFragment.OnPFLockScreenCodeCreateListener() {
                @Override
                public void onCodeCreated(String encodedCode) {
                    Toast.makeText(MainActivity.this, "Code created", Toast.LENGTH_SHORT).show();
                    PreferencesSettings.saveToPref(MainActivity.this, encodedCode);
                }

                @Override
                public void onNewCodeValidationFailed() {
                    Toast.makeText(MainActivity.this, "Code validation error", Toast.LENGTH_SHORT).show();
                }
            };

    private final PFLockScreenFragment.OnPFLockScreenLoginListener mLoginListener =
            new PFLockScreenFragment.OnPFLockScreenLoginListener() {

                @Override
                public void onCodeInputSuccessful() {
                    Toast.makeText(MainActivity.this, "Code successfull", Toast.LENGTH_SHORT).show();
                    showMainFragment();
                }

                @Override
                public void onFingerprintSuccessful() {
                    Toast.makeText(MainActivity.this, "Fingerprint successfull", Toast.LENGTH_SHORT).show();
                    showMainFragment();
                }

                @Override
                public void onPinLoginFailed() {
                    Toast.makeText(MainActivity.this, "Pin failed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFingerprintLoginFailed() {
                    Toast.makeText(MainActivity.this, "Fingerprint failed", Toast.LENGTH_SHORT).show();
                }
            };

    @Override
    protected void onStart() {
        super.onStart();
        showLockScreenFragment();
    }

    private void showLockScreenFragment() {
        new PFPinCodeViewModel().isPinCodeEncryptionKeyExist().observe(
                this,
                new Observer<PFResult<Boolean>>() {
                    @Override
                    public void onChanged(@Nullable PFResult<Boolean> result) {
                        if (result == null) {
                            return;
                        }
                        if (result.getError() != null) {
                            Toast.makeText(MainActivity.this, "Can not get pin code info", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        showLockScreenFragment(result.getResult());
                    }
                }
        );
    }

    private void showLockScreenFragment(boolean isPinExist) {

        final PFFLockScreenConfiguration.Builder builder = new PFFLockScreenConfiguration.Builder(this)
                .setTitle(isPinExist ? "Unlock with your pin code or fingerprint" : "Create Code")
                .setCodeLength(6)
                .setLeftButton("Can't remeber")
                .setNewCodeValidation(true)
                .setNewCodeValidationTitle("Please input code again")
                .setUseFingerprint(true);
        pfLockScreenFragment = new PFLockScreenFragment();

        pfLockScreenFragment.setOnLeftButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Left button pressed", Toast.LENGTH_LONG).show();
            }
        });

        builder.setMode(isPinExist
                ? PFFLockScreenConfiguration.MODE_AUTH
                : PFFLockScreenConfiguration.MODE_CREATE);

        builder.setAutoShowFingerprint(true);
        if (isPinExist) {
            pfLockScreenFragment.setEncodedPinCode(PreferencesSettings.getCode(this));
            pfLockScreenFragment.setLoginListener(mLoginListener);
        }

        pfLockScreenFragment.setConfiguration(builder.build());
        pfLockScreenFragment.setCodeCreateListener(mCodeCreateListener);
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, pfLockScreenFragment)
                .commit();
    }

    private void showMainFragment() {
        if (pfLockScreenFragment != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(pfLockScreenFragment)
                    .commit();
            pfLockScreenFragment = null;
        }

        final MainFragment fragment = new MainFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_view, fragment).commit();
    }


}
