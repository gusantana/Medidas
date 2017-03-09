package com.mysticlabs.medidas;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import model.ClienteDAO;

import static com.mysticlabs.medidas.R.drawable.ic_person_add_white_24dp;

public class ClientesActivity extends AppCompatActivity {

    public static int i_log;

    private ClientesFragment fragCliente;
    private MedidasFragment fragMedidas;

    public FirebaseDatabase mRef;
    public DatabaseReference mDataBaseRef;
    public ChildEventListener mChildEventListener;


    // These variables are shorthand aliases for data items in Contacts-related database tables
    private static final String DATA_MIMETYPE = ContactsContract.Data.MIMETYPE;
    private static final Uri DATA_CONTENT_URI = ContactsContract.Data.CONTENT_URI;
    private static final String DATA_CONTACT_ID = ContactsContract.Data.CONTACT_ID;

    private static final String CONTACTS_ID = ContactsContract.Contacts._ID;
    private static final Uri CONTACTS_CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;

    private static final String STRUCTURED_POSTAL_CONTENT_ITEM_TYPE = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE;
    private static final String STRUCTURED_POSTAL_FORMATTED_ADDRESS = ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS;

    private static final int PICK_CONTACT_REQUEST = 0;


    // Lista dos clientes
    private List<String> nome_clientes;
    private List<String> tel_clientes;

    //Adapter da lista de clientes;
    ViewClienteAdapter clienteAdapter = null;

    //lista de clientes a serem exibidos na tela
    List<HashMap<String, Object>> lClientes = new ArrayList<>();



    //Dados do facebook
    String fb_name, fb_email, fb_gender, fb_birthday;

    // variaveis do Firebase
    FirebaseAuth                    mAuth;
    FirebaseAuth.AuthStateListener  mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clientes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

	    recoverFacebookData();

//	    System.out.println ("Dados do usuario: "+fb_name+"\n"+fb_email+"\n"+
//	        fb_gender+"\n"+fb_birthday);

        setupFirebase ();
        setupContentView();
        recoverContactsInfo ();

    }

    private void setupFirebase() {}

    private void recoverFacebookData () {
        fb_name      = getIntent().getStringExtra("name");
        fb_email     = getIntent().getStringExtra("email");
//	    fb_gender    = getIntent().getStringExtra("gender");
//	    fb_birthday  = getIntent().getStringExtra("birthday");
        try {
            fb_email = fb_email.replace(".", ",");
        }
        catch (Exception e){
            Log.d("ClientesActivity", e.getMessage());
        }
    }

    private void pickNewContact() {
        try {
            // Cria um objeto Intent para pegar dados da database de Contatos
            Intent intent = new Intent(Intent.ACTION_PICK,
                    CONTACTS_CONTENT_URI);

            // Usar intent para iniciar a aplicação Contatos
            // Variável PICK_CONTACT_REQUEST identifica esta operação
            startActivityForResult(intent, PICK_CONTACT_REQUEST);

        } catch (Exception e) {
            // Exibir quaisquer mensagem de erro no LogCat usando Log.e()
            System.out.println(e.toString());
        }
    }




    private void recoverContactsInfo () {
        ClienteDAO clienteDAO = new ClienteDAO();
        mDataBaseRef = FirebaseDatabase.getInstance().
                getReferenceFromUrl("https://medidas-90407.firebaseio.com/").child("costureiras")
                .child(fb_email).child("clientes");

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                HashMap <String, Object> aCliente = (HashMap<String, Object>) dataSnapshot.getValue();
                aCliente.put("key", dataSnapshot.getKey());

                lClientes.add (aCliente);
                System.out.println ("valor de s: "+s);
                clienteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
	            // parâmetro s é a chave que foi alterada
	            String key_snapshot = dataSnapshot.getKey();
	            HashMap<String, Object> aux;
	            for (int i  = 0; i < lClientes.size(); i++) {
		            aux = lClientes.get(i);
		            String key = (String) aux.get("key");
		            if (key.equals(key_snapshot)) {
			            aux = (HashMap<String, Object>) dataSnapshot.getValue();
			            aux.put ("key", key_snapshot);
			            lClientes.set (i,aux);
		            }
	            }
	            clienteAdapter.notifyDataSetChanged();



                System.out.println ("onChildChanged");
                System.out.println ("dataSnapshot: "+dataSnapshot.toString());

                System.out.println ("s: "+s);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //lClientes.remove((HashMap<String, Object>) dataSnapshot.getValue());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("FIREBASE", databaseError.getMessage());
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        mDataBaseRef.addChildEventListener(mChildEventListener);


    }


    private void setupContentView () {


        //String title = (String) getResources().getString (R.string.label_toolbar_pick_cliente);
        String title = "Escolha um Cliente";
        getSupportActionBar().setTitle(title);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setImageDrawable(getResources().getDrawable(ic_person_add_white_24dp));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //      .setAction("Action", null).show();
                System.out.println ("FAB CLICADO!");
                pickNewContact ();
            }
        });



        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.view_cliente);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        this.clienteAdapter = new ViewClienteAdapter();
        mRecyclerView.setAdapter(this.clienteAdapter);

    }





    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        //garante que a chamada é o resultado da requisição de PICK_CONTACT_REQUEST
        if (resultCode == Activity.RESULT_OK
            && requestCode == PICK_CONTACT_REQUEST) {

            ContentResolver cr = getContentResolver();
            Cursor cursor = cr.query (data.getData(), null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String id = cursor.getString (cursor.getColumnIndex(CONTACTS_ID));
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID));
                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                //Uri my_contact_Uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactId));

                String phone = "phone";
                Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{contactId}, null);
                while (pCur.moveToNext()) {
                    phone = pCur.getString(
                            pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }

                System.out.println ("ID: "+id);
                System.out.println ("Display Name: "+ displayName);
                System.out.println ("TELEFONE: "+phone);

                String [] medidas = (String []) getResources().getStringArray(R.array.medidas);
                ClienteDAO clienteDAO = new ClienteDAO(id, displayName, phone, medidas, fb_email);
                clienteDAO.createNew ();
            }

            if (cursor != null) {
                cursor.close();
            }
        }
    }


    public void startAnotherActivity () {
        Intent it = new Intent (ClientesActivity.this, MedidasActivity.class);
        startActivity(it);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sair) {
            System.out.println ("i = "+(i_log++)+": saindo do firebase");
	        FirebaseAuth.getInstance().signOut();
            System.out.println ("i = "+(i_log++)+": saiu do firebase");
            System.out.println ("i = "+(i_log++)+": saindo do facebook");
            LoginManager.getInstance().logOut();
            System.out.println ("i = "+(i_log++)+": saiu do facebook");

            mDataBaseRef.removeEventListener(mChildEventListener);

	        Intent intent = new Intent (ClientesActivity.this, MainActivity.class);
            Log.d("ClientesActivity", "saindo..");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        startActivity(intent);
	        return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        //
        Log.d("LOG_ACTIVITY", "onBackPressed()");
    }


    @Override
    protected void onResume() {
        super.onResume();
        System.out.println ("i = "+(ClientesActivity.i_log++)+": resumindo activity de Clientes, onResume");

    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println ("i = "+(ClientesActivity.i_log++)+": parando activity clientes, onStop");


    }







    public class ViewClienteAdapter extends RecyclerView.Adapter<ViewClienteAdapter.ViewHolder> {



        public ViewClienteAdapter () {
            super();
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //cria nova view
            View v = LayoutInflater.from (parent.getContext())
                    .inflate(R.layout.client_item, parent, false);
            ViewClienteAdapter.ViewHolder vh = new ViewClienteAdapter.ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {

            if (lClientes != null) {
                //pega os elementos do conjunto de dados nesta posição
//                holder.lbl_nome_cliente.setText(nome_clientes.get(position).toString());
                try {
                    holder.lbl_nome_cliente.setText(lClientes.get(position).get("nome").toString());
                } catch (NullPointerException e) {
                    holder.lbl_nome_cliente.setText(getResources().getString(R.string.contato_sem_nome));
                }

                try {
                    holder.lbl_tel_cliente.setText(lClientes.get(position).get("telefone").toString());
                }catch (NullPointerException e) {
                    holder.lbl_tel_cliente.setText(getResources().getString(R.string.contato_sem_telefone));
                }


//                holder.lbl_tel_cliente.setText(tel_clientes.get(position).toString());
                holder.card_client_item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent it = new Intent(ClientesActivity.this, MedidasActivity.class);
                        it.putExtra("nome", lClientes.get(position).get("nome").toString());
                        it.putExtra("telefone", lClientes.get(position).get("telefone").toString());
                        it.putExtra("key", lClientes.get(position).get("key").toString());
	                    it.putExtra("fb_email", fb_email);

                        startActivity(it);
                        //Toast.makeText(ClientesActivity.this, "Clicou em "+nome_clientes.get(position), Toast.LENGTH_SHORT).show();

                    }
                });
            }



        }

        @Override
        public int getItemCount() {
            return lClientes.size();
        }


        protected class ViewHolder extends RecyclerView.ViewHolder {
            protected TextView lbl_nome_cliente;
            protected TextView lbl_tel_cliente;
            protected CardView card_client_item;

            public ViewHolder(View itemView) {
                super(itemView);
                this.lbl_nome_cliente   = (TextView) itemView.findViewById(R.id.lbl_nome_cliente);
                this.lbl_tel_cliente    = (TextView) itemView.findViewById(R.id.lbl_tel_cliente);
                this.card_client_item   = (CardView) itemView.findViewById(R.id.client_item);
            }
        }
    }

}
