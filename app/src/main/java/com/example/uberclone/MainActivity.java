package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    public void onClick(View view) {
        if (edtDriverOrPassenger.getText().toString().equals("Driver") || edtDriverOrPassenger.getText().toString().equals("Passenger")) {
            if (ParseUser.getCurrentUser() == null) {
                ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user != null && e == null) {
                            Toast.makeText(MainActivity.this,"Welcome user",Toast.LENGTH_SHORT).show();

                            user.put("as", edtDriverOrPassenger.getText().toString());
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        transitionToPassengerActivity();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        } else {
            Toast.makeText(MainActivity.this,"Are you a Passenger or Driver?",Toast.LENGTH_LONG).show();

        }
    }

    enum State {
        SIGNUP, LOGIN
    }

    private State state;
    private Button btnSignUp, btnOTLogIn;
    private EditText edtUsername, edtPassword, edtDriverOrPassenger;
    private RadioButton passengerRadioButton, driverRadioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if (ParseUser.getCurrentUser() != null) {
            // Intent activity

            transitionToPassengerActivity();
        }

        // Save the current Installation to Back4App
        ParseInstallation.getCurrentInstallation().saveInBackground();

        state = State.SIGNUP;
        btnSignUp = findViewById(R.id.btnSignUp);
        btnOTLogIn = findViewById(R.id.btnOTLogIn);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtDriverOrPassenger = findViewById(R.id.edtDorP);
        passengerRadioButton = findViewById(R.id.rdbPassenger);
        driverRadioButton = findViewById(R.id.rdbDriver);


        btnOTLogIn.setOnClickListener(this);



        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (state == State.SIGNUP) {

                    if (driverRadioButton.isChecked() == false && passengerRadioButton.isChecked() == false) {
                        Toast.makeText(MainActivity.this,"Are you a Passenger or Driver?",Toast.LENGTH_LONG).show();
                        return;
                    }

                    final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Signing Up");
                    progressDialog.show();

                    ParseUser appUser = new ParseUser();
                    appUser.setUsername(edtUsername.getText().toString());
                    appUser.setPassword(edtPassword.getText().toString());

                    if (driverRadioButton.isChecked()) {
                        appUser.put("as", "Driver");
                    } else if (passengerRadioButton.isChecked()) {
                        appUser.put("as", "Passenger");
                    }

                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(MainActivity.this, "Signed Up!",Toast.LENGTH_SHORT).show();
                                transitionToPassengerActivity();
                            }
                            progressDialog.dismiss();
                        }
                    });

                } else if (state == State.LOGIN) {
                    final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Logging in...");
                    progressDialog.show();

                    ParseUser.logInInBackground(edtUsername.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (user != null && e == null) {
                                Toast.makeText(MainActivity.this,"User Logged in",Toast.LENGTH_SHORT).show();
                                transitionToPassengerActivity();
                            }
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sign_up_activity,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.loginItem :

                if (state == State.SIGNUP) {
                    state = State.LOGIN;
                    btnSignUp.setText("Log In");
                    item.setTitle("Sign Up");
                } else if (state == State.LOGIN) {
                    state = State.SIGNUP;
                    btnSignUp.setText("Sign Up");
                    item.setTitle("Log In");
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void transitionToPassengerActivity() {
        if (ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().get("as").equals("Passenger")) {
                Intent intent = new Intent(MainActivity.this, PassengerActivity.class);
                startActivity(intent);
            }
        }
    }
}
