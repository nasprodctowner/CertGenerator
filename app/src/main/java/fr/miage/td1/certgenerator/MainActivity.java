package fr.miage.td1.certgenerator;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity {

    EditText url;
    TextView listCertsTextView;
    KeyStore ks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();

        StrictMode.setThreadPolicy(policy);


        url = findViewById(R.id.URL);
        listCertsTextView = findViewById(R.id.listCerts);

        try {
            ks = getKeystore();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }


    private void print_https_cert(HttpsURLConnection con){

        StringBuilder listCerts = new StringBuilder();

        if(con!=null){

            try {

                Certificate[] certs = con.getServerCertificates();


                for(Certificate cert : certs){

                    X509Certificate x509Certificate = (X509Certificate) cert;
                    x509Certificate.getIssuerDN().getName();
                    x509Certificate.getSubjectDN().getName();

                    ks.setCertificateEntry(x509Certificate.getIssuerDN().getName(),cert);

                    listCerts.append(x509Certificate.getIssuerDN().getName());
                    listCerts.append("\n");
                    listCerts.append(x509Certificate.getSubjectDN().getName());
                    listCerts.append("\n");
                }


               listCertsTextView.setText(listCerts.toString());


            } catch (SSLPeerUnverifiedException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }

        }

    }

    private KeyStore getKeystore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        char[] chars = "1234".toCharArray();
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, chars);
        return ks;
    }

    public void getCerts(View view) {
        
        String mURL = url.getText().toString();
        URL url = null;

        try {
            url = new URL(mURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpsURLConnection urlConnection = null;
        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            urlConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        print_https_cert(urlConnection);
    }

    public void checkCert(View view) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        String tmfa = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfa);
        tmf.init(ks);

        sc.init(null,tmf.getTrustManagers(),null);

        String mURL = url.getText().toString();
        URL url = null;

        try {
            url = new URL(mURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpsURLConnection urlConnection = null;
        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(sc.getSocketFactory());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            urlConnection.connect();
            Toast.makeText(MainActivity.this,"OK !",Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Toast.makeText(MainActivity.this,"NOT OK !",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }
}
