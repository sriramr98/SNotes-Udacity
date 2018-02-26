package in.snotes.snotes.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import in.snotes.snotes.R;
import in.snotes.snotes.service.NotesService;
import in.snotes.snotes.utils.AppConstants;
import in.snotes.snotes.view.notesmain.NotesMainActivity;
import timber.log.Timber;

public class AuthActivity extends AppCompatActivity implements AuthFragment.AuthListener, LoginFragment.LoginListener, RegisterFragment.RegisterListener {

    MaterialDialog authDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frame_auth, new AuthFragment())
                    .commit();
        }
    }

    // login clicked in AuthFragment
    @Override
    public void onLoginClicked() {
        navToLogin();
    }

    // register clicked in AuthFragment
    @Override
    public void onRegisterClicked() {
        navToRegister();
    }

    private void authenticatingDialog() {
        authDialog = new MaterialDialog.Builder(this)
                .title(R.string.progress_title)
                .content(R.string.please_content)
                .progress(true, 0)
                .autoDismiss(false)
                .build();

        authDialog.show();
    }

    // logging in user with Firebase
    @Override
    public void loginUser(String email, String password) {
        authenticatingDialog();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();
                        authDialog.dismiss();
                        startSyncServiceOnLogin();
                        goToMainActivity();
                    } else {
                        authDialog.dismiss();
                        showRegisterErrorDialog(task.getException());
                        Timber.d("Login error %s", task.getException().getMessage());
                    }
                });
    }

    private void startSyncServiceOnLogin() {
        // this is called because we want to sync in users preferences from backup
        Intent i = new Intent(this, NotesService.class);
        i.setAction(NotesService.ACTION_LOGIN_SYNC);
        startService(i);
    }

    // navigating to NotesMainActivity after registering and logging in
    private void goToMainActivity() {
        Intent i = new Intent(AuthActivity.this, NotesMainActivity.class);
        startActivity(i);
        finish();
        return;
    }

    @Override
    public void navToRegister() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_auth, new RegisterFragment())
                .addToBackStack("register-fragment")
                .commit();
    }

    @Override
    public void forgotPassword() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        new MaterialDialog.Builder(this)
                .title("Enter Email ID")
                .inputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                .input(null, null, false, (dialog, input) -> {
                    if (Patterns.EMAIL_ADDRESS.matcher(input.toString()).matches()) {
                        auth.sendPasswordResetEmail(input.toString())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(this, "Check your email", Toast.LENGTH_SHORT).show();
                                    } else {
                                        showForgotPasswordError(getString(R.string.forgot_password_error_sending_mail));
                                    }
                                });
                    } else {
                        showForgotPasswordError(getString(R.string.forgot_password_incorrect_email));
                    }
                }).show();
    }

    private void showForgotPasswordError(String content) {
        new MaterialDialog.Builder(this)
                .title(getString(R.string.error))
                .content(content)
                .neutralText(getString(R.string.ok))
                .show();
    }

    @Override
    public void registerUser(String name, String email, String password) {
        authenticatingDialog();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // start the IntentService
                        authDialog.dismiss();
                        startRegistrationService(name, task.getResult().getUser().getUid());
                        goToMainActivity();
                    } else {
                        authDialog.dismiss();
                        showRegisterErrorDialog(task.getException());
                        Timber.e("Error registering user %s", task.getException().getMessage());
                    }
                });
    }

    private void showRegisterErrorDialog(Exception e) {
        String content = null;
        if (e instanceof FirebaseAuthUserCollisionException) {
            content = "Sorry. The email ID is already registered. Try logging in if it's your Email ID or check your Email";
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            content = "Please check your Email ID and Password.";
        } else if (e instanceof FirebaseAuthInvalidUserException) {
            content = "Please check your Email ID and Password.";
        }

        if (content != null) {
            new MaterialDialog.Builder(this)
                    .title("Error")
                    .content(content)
                    .neutralText(getString(R.string.ok))
                    .show();
        }

    }


    private void startRegistrationService(String name, String uid) {
        Intent i = new Intent(AuthActivity.this, NotesService.class);
        i.setAction(NotesService.ACTION_REGISTER_USER);
        i.putExtra(AppConstants.EXTRA_USER_UID, uid);
        i.putExtra(AppConstants.EXTRA_USER_NAME, name);
        startService(i);
    }

    private void navToLogin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_auth, new LoginFragment())
                .addToBackStack("login-fragment")
                .commit();
    }

    @Override
    public void userIsAlreadyRegistered() {
        navToLogin();
    }
}
