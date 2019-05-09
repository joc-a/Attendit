package com.jocelyne.mesh.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jocelyne.mesh.R;
import com.jocelyne.mesh.instructor.main.InstructorMainActivity;
import com.jocelyne.mesh.session.Instructor;
import com.jocelyne.mesh.session.SessionManager;
import com.jocelyne.mesh.session.Student;
import com.jocelyne.mesh.session.User;
import com.jocelyne.mesh.signup.SignUpActivity;
import com.jocelyne.mesh.student.StudentMainActivity;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN = 1;
    private String TAG = "LoginActivity";

    private String INSTRUCTORS_COLLECTION = "INSTRUCTORS";
    private String STUDENTS_COLLECTION = "STUDENTS";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private TextView mLoginTypeView;
    private RadioGroup mRadioGroup;
    private View mProgressView;
    private View mLoginFormView;

    private boolean isInstructor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mLoginTypeView = (TextView) findViewById(R.id.login_type);
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button signUpButton = (Button) findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSignUpPage();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_instructor:
                if (checked) {
                    isInstructor = true;
                    mLoginTypeView.setText(getString(R.string.instructor_login));
                }
                break;
            case R.id.radio_student:
                if (checked) {
                    isInstructor = false;
                    mLoginTypeView.setText(getString(R.string.student_login));
                }
                break;
        }
    }

    private void login(String email, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail: success");

                            String collectionPath = isInstructor ? INSTRUCTORS_COLLECTION : STUDENTS_COLLECTION;
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            final String id = firebaseUser.getUid();
                            FirebaseFirestore.getInstance()
                                    .collection(collectionPath)
                                    .document(id)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            User user = documentSnapshot.toObject(isInstructor ? Instructor.class : Student.class);
                                            showProgress(false);
                                            if (user == null) { // wrong account type
                                                Log.w(TAG, "signInWithEmail: failure cuz wrong account type");
                                                Snackbar.make(findViewById(R.id.linear_layout),
                                                        "Authentication failed.", Snackbar.LENGTH_SHORT).show();
                                            } else {
                                                SessionManager.Companion.getInstance(getApplicationContext())
                                                        .createLoginSession(user, id, isInstructor);
                                                Toast.makeText(LoginActivity.this, "Authentication successful!",
                                                        Toast.LENGTH_LONG).show();
                                                startUsingApp();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            showProgress(false);
                                            Log.w(TAG, "signInWithEmail: failure", e);
                                            Snackbar.make(findViewById(R.id.linear_layout),
                                                    "Authentication failed.", Snackbar.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            showProgress(false);
                            Log.w(TAG, "signInWithEmail: failure", task.getException());
                            mPasswordView.setError(getString(R.string.error_incorrect_password));
                            mPasswordView.requestFocus();
                            Snackbar.make(findViewById(R.id.linear_layout),
                                    "Authentication failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void startUsingApp() {
        Intent i;
        if (isInstructor) {
            i = new Intent(LoginActivity.this, InstructorMainActivity.class);
        } else {
            i = new Intent(LoginActivity.this, StudentMainActivity.class);
        }
        startActivity(i);
        finish();
    }

    private void goToSignUpPage() {
        Intent i = new Intent(this, SignUpActivity.class);
        startActivity(i);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check if no radio buttons are checked
        if (mRadioGroup.getCheckedRadioButtonId() == -1) {
            Snackbar.make(findViewById(R.id.linear_layout),
                    "Please choose your account type.", Snackbar.LENGTH_SHORT).show();
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            login(email, password);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginTypeView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginTypeView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginTypeView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginTypeView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}