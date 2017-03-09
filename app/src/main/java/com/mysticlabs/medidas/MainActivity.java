package com.mysticlabs.medidas;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		ClientesActivity.i_log = 0;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		System.out.println ("i = "+(ClientesActivity.i_log++)+": onCreate");

		if (AccessToken.getCurrentAccessToken() != null || Profile.getCurrentProfile() != null) {
			System.out.println ("Usuário já logado");
			System.out.println ("Expirado: "+AccessToken.getCurrentAccessToken().isExpired());
			// se o token ainda nao expirou:
			if (! AccessToken.getCurrentAccessToken().isExpired()) {
				Profile.fetchProfileForCurrentAccessToken();
				GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
						new GraphRequest.GraphJSONObjectCallback() {
							@Override
							public void onCompleted (JSONObject object, GraphResponse response) {

								FacebookSdk.setIsDebugEnabled(true);
								FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

								System.out.println("AccessToken.getCurrentAccessToken() " +
										AccessToken.getCurrentAccessToken().toString());

								System.out.println(Profile.getCurrentProfile().getName());
								System.out.println(Profile.getCurrentProfile().getProfilePictureUri(50, 50));

								String id, name, email, gender, birthday;


								try {
									System.out.println (object.toString());
									name = object.getString("name");
									email = object.getString("email");
									//gender = object.getString("gender");

									//birthday = object.getString("birthday");
									System.out.println ("i = "+(ClientesActivity.i_log++)+": criando activity de clientes");
									Intent intent = new Intent(MainActivity.this, ClientesActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

									intent.putExtra("name", name);
									intent.putExtra("email", email);
									//intent.putExtra("gender", gender);
									//intent.putExtra("birthday", birthday);


									startActivity(intent);
									System.out.println ("i = "+(ClientesActivity.i_log++)+": voltando para activity main");
								} catch (JSONException e) {
									e.printStackTrace();

									Toast.makeText(getApplicationContext(),
											"Houve um erro ao entrar com a conta do facebook",
											Toast.LENGTH_SHORT).show();

									Intent intent = new Intent (MainActivity.this, LoginActivity.class);
									FirebaseAuth.getInstance().signOut();
									LoginManager.getInstance().logOut();
									startActivity(intent);

								}
							}
						});

				Bundle parameters = new Bundle();
				parameters.putString("fields", "name,email");
				request.setParameters(parameters);
				request.executeAsync();

			}
		}
		else {
			System.out.println ("i = "+(ClientesActivity.i_log++)+": criando activity de login");
			Intent intent = new Intent (MainActivity.this, LoginActivity.class);
//			intent.putExtra("name", "Nilza Santana");
//			intent.putExtra("email", "gustavo_13roo@hotmail,com");
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);

			System.out.println ("i = "+(ClientesActivity.i_log++)+": voltando da activity de login");
		}
	}

	@Override
	protected void onPostResume () {
		super.onPostResume();

		if (AccessToken.getCurrentAccessToken() != null || Profile.getCurrentProfile() != null) {
			System.out.println ("i = "+(ClientesActivity.i_log++)+": usuario ja logado!");
		}
		else {
			Intent intent = new Intent (MainActivity.this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			FirebaseDatabase mRef = FirebaseDatabase.getInstance().getReference().

			startActivity(intent);
		}
		System.out.println ("i = "+(ClientesActivity.i_log++)+": resumindo activity, onResume");
	}
}
