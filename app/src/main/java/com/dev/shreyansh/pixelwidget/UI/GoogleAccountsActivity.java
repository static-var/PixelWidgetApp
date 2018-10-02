package com.dev.shreyansh.pixelwidget.UI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dev.shreyansh.pixelwidget.Keys.OpenWeatherKey;
import com.dev.shreyansh.pixelwidget.R;
import com.dev.shreyansh.pixelwidget.Util.Util;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.services.calendar.CalendarScopes;

public class GoogleAccountsActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "GoogleAccountsClass";

    private GoogleSignInClient mGoogleSignInClient;

    private SignInButton signInButton;
    private Button logoutButton;

    private String name;
    private String email;

    private TextView nameTV;
    private TextView emailTV;
    private LinearLayout displayDetails;

    private Context context;

    private static final Scope CALENDAR_SCOPE= new Scope(CalendarScopes.CALENDAR);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_accounts);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);

        context = GoogleAccountsActivity.this;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(CALENDAR_SCOPE)
                .requestServerAuthCode(OpenWeatherKey.CLIENT_ID)
                .requestIdToken(OpenWeatherKey.CLIENT_ID)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        bind();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        fetchAndSetAccountDetails(account);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.enter1, R.anim.exit1);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        signInButton.setVisibility(View.VISIBLE);
                        logoutButton.setVisibility(View.INVISIBLE);
                        hideUI();
                    }
                });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            fetchAndSetAccountDetails(account);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
        }
    }

    private void bind() {
        signInButton = findViewById(R.id.sign_in_button);
        logoutButton = findViewById(R.id.sign_out_button);
        nameTV = findViewById(R.id.name);
        emailTV = findViewById(R.id.email);
        displayDetails = findViewById(R.id.display_details);
    }

    private void hideUI() {
        if(displayDetails.getVisibility()==View.VISIBLE)
            displayDetails.setVisibility(View.INVISIBLE);
    }

    private void showUI() {
        if(displayDetails.getVisibility()==View.INVISIBLE)
            displayDetails.setVisibility(View.VISIBLE);
    }

    private void fetchAndSetAccountDetails(GoogleSignInAccount account) {
        if(account!=null) {
            showUI();
            name = account.getDisplayName();
            email = account.getEmail();
            nameTV.setText(name);
            emailTV.setText(email);
            signInButton.setVisibility(View.INVISIBLE);
            logoutButton.setVisibility(View.VISIBLE);

            if (!GoogleSignIn.hasPermissions(
                    GoogleSignIn.getLastSignedInAccount(this), CALENDAR_SCOPE)) {
                final AlertDialog builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle)
                        .setCancelable(false)
                        .setTitle("No Access for Google Calendar")
                        .setMessage("Please provide access to your Google Calendar to get the most out of the widget.")
                        .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GoogleSignIn.requestPermissions(GoogleAccountsActivity.this,
                                        RC_SIGN_IN,
                                        GoogleSignIn.getLastSignedInAccount(context), CALENDAR_SCOPE);
                            }
                        })
                        .show();
                builder.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAccent));
            } else {
                /* Set Job Scheduler as soon as the user Logs in */
                Util.widgetData(context);
            }
        } else {
            hideUI();
        }
    }
}
