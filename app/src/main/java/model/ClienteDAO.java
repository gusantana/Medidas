package model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gustavo on 20/01/2017.
 */

public class ClienteDAO {
    protected String id;
    protected String key;
    protected HashMap<String, Object> tabMedidas = null;
    protected String nome;
    protected String telefone;
    protected String[] itens_medidas;

    protected String fb_email;

    protected Map<String, String> timestamp;

    protected DatabaseReference refDB;

    public ClienteDAO() {

    }

    public ClienteDAO(String id, String nome, String telefone, String [] medidas, String fb_email) {
        this.id             = id;
        this.nome           = nome;
        this.telefone       = telefone;
        this.itens_medidas  = medidas;
	    this.fb_email       = fb_email;

    }

    public void createNew() {
        createNewClient();
        createNewMedida();
    }



    private void createNewClient() {
        refDB = FirebaseDatabase.getInstance()
		        .getReferenceFromUrl("https://medidas-90407.firebaseio.com/").child("costureiras")
		        .child(fb_email).child("clientes");
        //pega novo id unico
        key = refDB.push().getKey();
        System.out.println("Key: " + key);
        //grava os dados do cliente
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", this.id);
        map.put("nome", this.nome);
        map.put("telefone", this.telefone);
        this.timestamp = ServerValue.TIMESTAMP;
        map.put("timestamp", this.timestamp);

        //indexado pela chave unica
        refDB.child(key).updateChildren(map);
    }



    //função que cria nova lista de medidas com os valores em -1
    private void createNewMedida() {
        refDB = refDB.getRoot().child("costureiras").child(fb_email).child("medidas");

        //grava os novos dados de medida
        HashMap<String, Object> medidas = new HashMap<>();
        //pega os itens da tabela de medidas

        // inicializa os itens da medida com -1 o que
        // significa que o valor está nulo
        for (int i = 0; i < itens_medidas.length; i++) {
            medidas.put (itens_medidas[i], "");
        }
        //usa o mesmo timestamp da criação do cliente na lista
        medidas.put("timestamp", this.timestamp);

        refDB.child(key).updateChildren(medidas);
    }




    public void setData(String[] medidas, String[] valores) {
        if (tabMedidas == null) {
            tabMedidas = new HashMap<>();
        }

        for (int i = 0; i < medidas.length; i++) {
            tabMedidas.put(medidas[i], valores[i]);
        }
    }

    public boolean sendData() {


        refDB.child(telefone).child("nome").setValue(nome);

        refDB.child(telefone).child("medidas").updateChildren(tabMedidas);

        return true;
    }
}
