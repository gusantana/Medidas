package com.mysticlabs.medidas;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.ClienteDAO;

public class MedidasActivity extends AppCompatActivity {

    //lista usada para recuperar os dados do DB
    private List<HashMap<String,Object>> lista_medidas;

    //lista usada para gravar os dados no DB (mais fácil!!!)
    private HashMap<String, Object> lista_aux;

    private DatabaseReference refDB;
    private ChildEventListener mChildEventListener;

    private ViewMedidasAdapter adapter;

    private String [] itens_medida;


    private String nome_cliente;
    private String tel_cliente;
    private String key_cliente;
    private String fb_email;




    //Parte de pegar as infos dos contatos pelo app do android
    // These variables are shorthand aliases for data items in Contacts-related database tables
    private static final String DATA_MIMETYPE = ContactsContract.Data.MIMETYPE;
    private static final Uri DATA_CONTENT_URI = ContactsContract.Data.CONTENT_URI;
    private static final String DATA_CONTACT_ID = ContactsContract.Data.CONTACT_ID;

    private static final String CONTACTS_ID = ContactsContract.Contacts._ID;
    private static final Uri CONTACTS_CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;

    private static final String STRUCTURED_POSTAL_CONTENT_ITEM_TYPE = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE;
    private static final String STRUCTURED_POSTAL_FORMATTED_ADDRESS = ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS;

    private static final int PICK_CONTACT_REQUEST = 0;

    //Fim da parte ds contatos







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medidas);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Medidas");
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });



        setupContentView();
        recoverMedidas();
    }


    private void setupContentView () {
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.view_medidas);
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        this.lista_medidas  = new ArrayList<>();
        this.lista_aux      = new HashMap<>();

        itens_medida = (String[]) getResources().getStringArray(R.array.medidas);

        this.adapter = new ViewMedidasAdapter();
        mRecyclerView.setAdapter (adapter);

        Intent it = getIntent();
        nome_cliente = it.getStringExtra("nome");
        tel_cliente = it.getStringExtra("telefone");
        key_cliente = it.getStringExtra("key");
        fb_email    = it.getStringExtra("fb_email");

        System.out.println ("fb_email: "+fb_email);

        TextView lbl_nome_cliente   = (TextView) findViewById(R.id.lbl_nome_cliente_medida);
        TextView lbl_tel_cliente    = (TextView) findViewById(R.id.lbl_telefone_cliente_medida);

        lbl_nome_cliente.setText (nome_cliente);
        lbl_tel_cliente.setText(tel_cliente);
    }


    private void recoverMedidas () {

        for (int i = 0; i < itens_medida.length; i++) {
            System.out.println ("lista ordenada: "+itens_medida[i]);
        }

        refDB = FirebaseDatabase.getInstance().
                getReferenceFromUrl("https://medidas-90407.firebaseio.com").
                child("costureiras").child(fb_email).child("medidas").child(key_cliente);

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                //System.out.println(dataSnapshot.getKey());
                HashMap<String, Object> item = new HashMap<>();
                if (! dataSnapshot.getKey().equals ("timestamp")) {


                    item.put(dataSnapshot.getKey(), dataSnapshot.getValue());
                    lista_aux.put(dataSnapshot.getKey(), dataSnapshot.getValue());

                    lista_medidas.add(item);
                    onMedidaAdded();

                }
                //HashMap<String, Object> mMedida = (HashMap<String, Object>) dataSnapshot.getValue();

                //System.out.println (mMedida.toString());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
//                HashMap<String, Object> item = new HashMap<>();
//                if (dataSnapshot.getValue().toString().equals("0")) {
//                    item.put(dataSnapshot.getKey(), "-1");
//                    lista_aux.put(dataSnapshot.getKey(), "-1");
                System.out.println ("i = "+(ClientesActivity.i_log++)+" onChildChanged: "+
                        dataSnapshot.toString());
//
//                }
//                System.out.println ("dados mudaram: "+dataSnapshot.getValue());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        //refDB.updateChildren();
        refDB.addChildEventListener(mChildEventListener);
    }


    private void onMedidaAdded () {
        System.out.println ("lista_medidas.size(): "+lista_medidas.size());
        System.out.println ("itens_medida.length: "+itens_medida.length);

        if (lista_medidas.size() == itens_medida.length) {
            HashMap <String, Object> aux, i_item, j_item;
            String i_nome, j_nome;
            List<HashMap<String,Object>> aux_list = new ArrayList<>();

            for (int i = 0; i < (lista_medidas.size()); i++){
                i_nome = itens_medida[i];
                for (int j = 0; j < lista_medidas.size(); j++) {
                    j_nome = lista_medidas.get (j).keySet().toArray()[0].toString();
//                    j_nome = itens_medida[j];

                    if (i_nome.equals(j_nome)) {
                        aux_list.add (lista_medidas.get(j));
                    }
                }
            }
            lista_medidas = aux_list;
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_medidas, menu);

        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_option_change_client) {
            System.out.println ("Criar a opção de escolher o novo cadastro");
            pickNewContact();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

                updateInfoContact(id, displayName, phone);
            }

            if (cursor != null) {
                cursor.close();
            }
        }
    }



    //atualiza o contato de acordo com a chave unica do cliente já cadastrado
    private void updateInfoContact (String id, String nome, String telefone) {
        HashMap<String, Object> aInfo = new HashMap<>();
        aInfo.put ("id", id);
        aInfo.put ("nome", nome);
        aInfo.put ("telefone", telefone);

        refDB.getRoot().child("costureiras").child(fb_email).child("clientes").child(key_cliente).updateChildren(aInfo);

        TextView lbl_nome_cliente   = (TextView) findViewById(R.id.lbl_nome_cliente_medida);
        TextView lbl_tel_cliente    = (TextView) findViewById(R.id.lbl_telefone_cliente_medida);

        lbl_nome_cliente.setText (nome);
        lbl_tel_cliente.setText(telefone);
    }



    public void onPause () {
        super.onPause();
        //salvar os dados na nuvem

        System.out.println (lista_aux.toString());
        refDB.getRoot().child("costureiras").child(fb_email).child("medidas").child(key_cliente)
                .updateChildren(lista_aux);

        //refDB.getRoot().child("medidas").child(key_cliente).updateChildren(lista_aux);
    }


    public void onStop () {
        super.onStop();
        refDB.removeEventListener(mChildEventListener);
    }





    public class ViewMedidasAdapter extends RecyclerView.Adapter<ViewMedidasAdapter.ViewHolder> {



        public ViewMedidasAdapter () {
            super ();
        }





        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //cria nova view
            View v = getLayoutInflater().from(parent.getContext())
                    .inflate(R.layout.medida_item, parent, false);

            final ViewMedidasAdapter.ViewHolder vh = new ViewMedidasAdapter.ViewHolder(v);

            System.out.println ("adicionando o listener");

            vh.fld_medida.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    //quando perde o foco
                        //atualiza na lista
                    lista_aux.put(vh.lbl_medida.getText().toString(), ((EditText) view).getText()
                            .toString());
                    // b é falso quando a view edittext perde o foco
                    if (!b) {
                        Toast.makeText(getApplicationContext(), "Valor de "+vh.lbl_medida.getText()
                                .toString()+" foi salvo", Toast.LENGTH_SHORT).show();
                    }

                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            HashMap<String, Object> item = lista_medidas.get(position);



            //esse foi o jeito que encontrei pra recuperar nome do item da lista
            String label        = item.keySet().toArray()[0].toString();
            String fld_value    = item.get(label).toString();

            String valor = (String) lista_aux.get(label);
            //System.out.println ("valor a atualizar: "+valor);


            holder.lbl_medida.setText(label);

            holder.fld_medida.setText (valor);

        }

        @Override
        public int getItemCount() {
            return lista_medidas.size();
        }

        protected class ViewHolder extends RecyclerView.ViewHolder {
            public TextView lbl_medida;
            public EditText fld_medida;

            public ViewHolder(View itemView) {
                super(itemView);

                lbl_medida = (TextView) itemView.findViewById(R.id.lbl_medida);
                fld_medida = (EditText) itemView.findViewById(R.id.fld_medida);

            }
        }
    }
}
