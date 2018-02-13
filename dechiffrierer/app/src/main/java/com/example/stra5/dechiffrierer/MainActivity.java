package com.example.stra5.dechiffrierer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;


public class MainActivity extends Activity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private ImageView imageView;
    private android.net.Uri uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonFoto = findViewById(R.id.buttonFoto);        //Button um ein Foto aufzunehmen.
        View.OnClickListener buttonFotoListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePictureIntent(false);
            }
        };
        buttonFoto.setOnClickListener(buttonFotoListener);
        imageView = findViewById(R.id.imageView); //Zeigt das aufgenommene Bild an
        Button buttonLog = findViewById(R.id.buttonLog); //button um ein Logbucheintrag zu erstellen
        View.OnClickListener buttonLogListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog();
            }
        };
        buttonLog.setOnClickListener(buttonLogListener);
        Button buttonFoto2 = findViewById(R.id.buttonFoto2);
        View.OnClickListener buttonFotoListener2 = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePictureIntent(true);
            }
        };
        buttonFoto2.setOnClickListener(buttonFotoListener2);
    }

    private void createDialog() {
        //https://stackoverflow.com/questions/10903754/input-text-dialog-android -> als Vorlage genutzt, da sehr gutes Beispiel.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);       //Ein Dialog Builder ermöglicht es einen Dialog während der Laufzeit mit Code zu erstellen.
        builder.setTitle(R.string.dialog_log_title);
        final EditText loesungswort = new EditText(this);
        loesungswort.setInputType(InputType.TYPE_CLASS_TEXT);  //bestimmt das es ein normales Textfeld ist.
        builder.setView(loesungswort);        //"OK" Button
        builder.setPositiveButton(R.string.button_log_eintragen, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss(); //Dialog ist fertig
                log(loesungswort.getText().toString()); //Logbuch Methode mit dem String des Textfeld als Lösungswort Parameter
            }
        });
        builder.setNegativeButton(R.string.button_log_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel(); //schliesst den Dialog ohne weiteres.
            }
        });
        builder.show(); //Dialog wird angezeigt, nachdem er fertig erstellt wurde.
    }

    private void log(String loesungswort) {
        Intent intent = new Intent("ch.appquest.intent.LOG");

        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) { // -> Logbuch App nicht installiert.
            Toast.makeText(this, R.string.logbuch_not_installed, Toast.LENGTH_LONG).show(); //Toast ist ein kleiner Aufgepoppert hinweis.
            return;
        }
        JSONObject json = new JSONObject();
        try {
            json.accumulate("task", "Dechiffrierer"); //JSON Zeile mit der Aufgabe
            json.accumulate("solution", loesungswort); //JSON Zeile mit dem Lösungswort
        } catch (JSONException e) {
            e.printStackTrace();
        }
        intent.putExtra("ch.appquest.logmessage", json.toString()); //gibt einen "Parameter" mit
        startActivity(intent);
    }

    private void takePictureIntent(Boolean b) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);        //Intent zum Aufnehmen eines Fotos mit einer Kamera App
        if (intent.resolveActivity(getPackageManager()) != null) { //Überprüft ob eine App vorhanden ist, die diesen Intent verarbeiten kann.
            if (!b) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, createFile());
            }
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);            //Lösst eine Activity aus, welche ein Resultat zurückgibt -> kann empfangen werden mit onActivityResult()
        }
    }

    private Uri createFile() {
        String filename = "dechiffrierer";
        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();
        String folderString = Environment.getExternalStorageDirectory() + "/dechiffrierer";
        File folder = new File(folderString);
        Boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            File file = new File(folderString, filename + ts + ".jpg");
            try {
                success = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (success) {
                uri = android.net.Uri.fromFile(file);
                return uri;
            }
        }
        return null;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap image = null;
            if (intent == null) {
                try {
                    image = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                image = (Bitmap) intent.getExtras().get("data");
            }
            if (image != null) {
//                imageView.setImageBitmap(image);
//                imageView.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);

                int[] data = new int[image.getWidth() * image.getHeight()];
                int[] newData = new int[image.getWidth() * image.getHeight()];
                image.getPixels(data, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
                int index = 0;
                for (int i : data) {
                    newData[index] = Color.rgb(Color.red(i), 0, 0);
                    index++;
                }
                imageView.setImageBitmap(Bitmap.createBitmap(newData, image.getWidth(), image.getHeight(), Bitmap.Config.RGB_565));

            }
        }
    }
}