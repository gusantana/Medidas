package com.mysticlabs.medidas;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookActivity;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    CallbackManager callbackManager;
    LoginButton loginButton;
	ProfileTracker profileTracker;

	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener mAuthListener;

	private String fb_name;
	private String fb_email;
	private Uri photoUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

	    mAuth = FirebaseAuth.getInstance();

		mAuthListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged (@NonNull FirebaseAuth firebaseAuth) {
				FirebaseUser user = firebaseAuth.getCurrentUser();
				if (user != null) {
					//User is signed in
					Log.d("LOG", "onAuthStateChanged: signed_in: "+user.getUid());
					fb_name = user.getDisplayName();
					fb_email = user.getEmail();
					photoUrl = user.getPhotoUrl();
					Intent intent = new Intent (LoginActivity.this, ClientesActivity.class);
					intent.putExtra("name", fb_name);
					intent.putExtra("email", fb_email);

					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					startActivity(intent);

//					loginButton.setVisibility(View.INVISIBLE);

//					System.out.println ("dados: "+name+" "+email+" "+photoUrl);
				}
				else {
					// User is signed out
					Log.d("LOG", "onAuthStateChanged: signed_out");
					//FirebaseAuth.getInstance().signOut();

					//LoginManager.getInstance().logOut();
					//mAuth.removeAuthStateListener(mAuthListener);
					//Intent intent = new Intent (LoginActivity.this, MainActivity.class);
					//startActivity(intent);
				}
			}
		};
        setupContentView();
    }

	public void onStart () {
		super.onStart();
		mAuth.addAuthStateListener(mAuthListener);
	}

	public void onStop () {
		super.onStop();
		if (mAuthListener != null) {
			mAuth.removeAuthStateListener(mAuthListener);
		}
	}

    private void setupContentView () {
        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) findViewById(R.id.login_button);

        loginButton.setReadPermissions(Arrays.asList(
		        "public_profile", "email", "user_birthday"));

        // registro de callbacks

		loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess (LoginResult loginResult) {
				//App code
				System.out.println("conseguiu conectar ao facebook");
				Log.d("BOTAO_FB", "onSucess!!");
				handleFacebookAccessToken(loginResult.getAccessToken());

/*

				GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
						new GraphRequest.GraphJSONObjectCallback() {
							@Override
							public void onCompleted (JSONObject object, GraphResponse response) {

								FacebookSdk.setIsDebugEnabled(true);
								FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

								System.out.println("AccessToken.getCurrentAccessToken() " +
										AccessToken.getCurrentAccessToken().toString());
								System.out.println ("Printando GraphRequest");
								//System.out.println(Profile.getCurrentProfile().getName());
								//System.out.println(Profile.getCurrentProfile().getProfilePictureUri(50, 50));
								System.out.println (object.toString());

								String id, name, email, gender, birthday;
//								Intent intent = new Intent(LoginActivity.this, ClientesActivity.class);

								try {
									name = object.getString("name");
									email = object.getString("email");
									gender = object.getString("gender");
//									birthday = object.getString("birthday");

//									intent.putExtra("name", name);
//									intent.putExtra("email", email);
//									intent.putExtra("gender", gender);
//									intent.putExtra("birthday", birthday);

//									startActivity(intent);
								} catch (JSONException e) {
									e.printStackTrace();

									Toast.makeText(getApplicationContext(),
											"Houve um erro ao entrar com a conta do facebook",
											Toast.LENGTH_SHORT).show();
								}
							}
						});

				Bundle parameters = new Bundle();
				parameters.putString("fields", "id,name,email,gender,birthday");
				request.setParameters(parameters);
				request.executeAsync();
*/

			}

			@Override
			public void onCancel () {
				System.out.println("Foi cancelado a ação de login no facebook");
			}

			@Override
			public void onError (FacebookException error) {
				System.out.println("Aconteceu um erro ao logar no facebook: " + error.getMessage());
			}
		});
		}



	private void handleFacebookAccessToken (AccessToken token) {
		Log.d("LOGIN_FB", "handleFacebookAccessToken:" + token);

		AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());


		mAuth.signInWithCredential(credential)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						Log.d("LOGIN_FB", "signInWithCredential:onComplete:" + task.isSuccessful());


						// If sign in fails, display a message to the user. If sign in succeeds
						// the auth state listener will be notified and logic to handle the
						// signed in user can be handled in the listener.
						if (!task.isSuccessful()) {
							Log.w("LOGIN_FB", "signInWithCredential", task.getException());
							Toast.makeText(LoginActivity.this, "Authentication failed.",
									Toast.LENGTH_SHORT).show();
						}

						// ...
					}
				});

	}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


	public void onDestroy (){
		super.onDestroy();
		//profileTracker.stopTracking();
	}

	@Override
	public void onBackPressed () {
		super.onBackPressed();
	}
}
