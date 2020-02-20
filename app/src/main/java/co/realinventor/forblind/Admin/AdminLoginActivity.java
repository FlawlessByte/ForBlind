package co.realinventor.forblind.Admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;

import co.realinventor.forblind.R;

public class AdminLoginActivity extends AppCompatActivity {
    private TextInputEditText adminUsernameField, adminPasswordField;
    private Button adminSignInButton;
    private final String TAG = "AdminLoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        adminUsernameField = findViewById(R.id.adminUsernameInputSignIN);
        adminPasswordField = findViewById(R.id.adminPasswordInputSignIn);
        adminSignInButton = findViewById(R.id.adminButtonSignIn);

        adminSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "SIgn in button clicked");
                String username = adminUsernameField.getText().toString();
                String password = adminPasswordField.getText().toString();
                if(username.equals("admin") && password.equals("admin")){
                    //Login success
                    Log.d(TAG, "Success logging in");
                    startActivity(new Intent(AdminLoginActivity.this, AdminHomeActivity.class));
                    finish();
                }
            }
        });
    }
}
