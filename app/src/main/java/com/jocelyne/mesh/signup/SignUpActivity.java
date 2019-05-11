package com.jocelyne.mesh.signup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jocelyne.mesh.R;
import com.jocelyne.mesh.instructor.main.InstructorMainActivity;
import com.jocelyne.mesh.session.Instructor;
import com.jocelyne.mesh.session.SessionManager;
import com.jocelyne.mesh.session.Student;
import com.jocelyne.mesh.session.User;
import com.jocelyne.mesh.student.main.StudentMainActivity;

public class SignUpActivity extends AppCompatActivity {

    public static String TAG = "SignUpActivity";

    private FirebaseFirestore db;
    private FirebaseAuth mFirebaseAuth;
    private String INSTRUCTORS_COLLECTION = "INSTRUCTORS";
    private String STUDENTS_COLLECTION = "STUDENTS";

    // UI references.
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mStudentIdView;
    private TextInputLayout mStudentIdContainer;
    private RadioGroup mRadioGroup;
    private View mProgressView;
    private View mLoginFormView;

    private boolean isInstructor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase components
        db = FirebaseFirestore.getInstance();

        mFirebaseAuth = FirebaseAuth.getInstance();

        // Set up the login form.
        mFirstNameView = (EditText) findViewById(R.id.first_name);
        mLastNameView = (EditText) findViewById(R.id.last_name);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mStudentIdView = (EditText) findViewById(R.id.student_id_et);
        mStudentIdContainer = findViewById(R.id.student_id_view);
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        Button mEmailSignUpButton = (Button) findViewById(R.id.sign_up_button);
        mEmailSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignup();
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
                    mStudentIdContainer.setVisibility(View.GONE);
                }
                break;
            case R.id.radio_student:
                if (checked) {
                    isInstructor = false;
                    mStudentIdContainer.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void createAccount(final String firstName, final String lastName,
                               final String email, String password, final String studentID) {
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail: success");

                            final FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                            final String id = firebaseUser.getUid(); // use this ID to retrieve info later
                            final User user;
                            String collectionPath;
                            if (isInstructor) {
                                collectionPath = INSTRUCTORS_COLLECTION;
                                user = new Instructor(firstName, lastName, email);
                            } else {
                                collectionPath = STUDENTS_COLLECTION;
                                user = new Student(firstName, lastName, email, studentID);
                            }

                            //using firebase ID here as a primary key instead of email cuz email format caused error
                            db.collection(collectionPath)
                                    .document(id)
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            showProgress(false);
                                            Log.d(TAG, "New user successfully registered with ID: " + id);
                                            Toast.makeText(SignUpActivity.this, "Sign-up successful.",
                                                    Toast.LENGTH_SHORT).show();
                                            SessionManager.Companion.getInstance(getApplicationContext())
                                                    .createLoginSession(user, id, isInstructor);
                                            startUsingApp();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            showProgress(false);
                                            Log.w(TAG, "Error writing new user", e);
                                            firebaseUser.delete(); // TODO add completion listener to ensure deletion
                                            Snackbar.make(findViewById(R.id.linear_layout),
                                                    "Sign-up failed. Please try again.", Snackbar.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Log.w(TAG, "createUserWithEmail: failure", task.getException());
                            showProgress(false);
                            Snackbar.make(findViewById(R.id.linear_layout),
                                    "Sign-up failed. Please try again.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void startUsingApp() {
        Intent i;
        if (isInstructor) {
            i = new Intent(SignUpActivity.this, InstructorMainActivity.class);
        } else {
            i = new Intent(SignUpActivity.this, StudentMainActivity.class);
        }
        startActivity(i);
        finish();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignup() {

        // Reset errors.
        mFirstNameView.setError(null);
        mLastNameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mStudentIdView.setError(null);

        // Store values at the time of the login attempt.
        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String studentID = isInstructor ? "-1" : mStudentIdView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check if no radio buttons are checked
        if (mRadioGroup.getCheckedRadioButtonId() == -1) {
            Snackbar.make(findViewById(R.id.linear_layout),
                    "Please choose your account type.", Snackbar.LENGTH_SHORT).show();
        }

        if (isInstructor && TextUtils.isEmpty(studentID)) {
            mStudentIdView.setError(getString(R.string.error_field_required));
            focusView = mStudentIdView;
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

        if (TextUtils.isEmpty(lastName)) {
            mLastNameView.setError(getString(R.string.error_field_required));
            focusView = mLastNameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(firstName)) {
            mFirstNameView.setError(getString(R.string.error_field_required));
            focusView = mFirstNameView;
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
            createAccount(firstName, lastName, email, password, studentID);
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
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}