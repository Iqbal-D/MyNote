package com.example.mynote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mynote.database.NoteDatabase;
import com.example.mynote.entiti.Note;
import com.example.mynote.style.StyleText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNote extends AppCompatActivity {

    ImageView backBtn, imageDelete, noteDelete, saveBtn, exportBtn, upOrDown;
    ImageView inputedImage;
    EditText judul, catatan;
    TextView waktu;
    ImageView imageBtn, textBold, textUnderline, textItalic, textLeft, textCenter, textRight;
    StyleText styleText;
//    String positionFirst = "";

    private String imagePath;

    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    private Note availableNote;

    private AlertDialog alertHapus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        // membuat app fullscreen, dengan taskbar on (transparent)
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        // membuat icon color taskbar menyesuaikan dengan background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        judul = findViewById(R.id.textJudul);
        catatan = findViewById(R.id.textContent);
        waktu = findViewById(R.id.textWaktu);
        inputedImage = findViewById(R.id.imageInput);
        waktu.setText(new SimpleDateFormat("EEEE, dd MMMM yy HH:mm", Locale.getDefault()).format(new Date()));

        imagePath = null;

        backBtn = findViewById(R.id.btnBack);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        saveBtn = findViewById(R.id.btnSave);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        imageBtn = findViewById(R.id.featureAddImage);
        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(CreateNote.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                }
                else{
                    selectImage();
                }
            }
        });

        if(getIntent().getBooleanExtra("viewORupdate", false)){
            availableNote = (Note) getIntent().getSerializableExtra("note");
            setViewORUpdate();
//            Toast.makeText(this, availableNote.getId(), Toast.LENGTH_SHORT).show();
        }

        imageDelete = findViewById(R.id.deleteImage);
        // pengesetan visibility, hasil percobaan di xml malah posisi berubah
        if (imagePath == null){
            imageDelete.setVisibility(View.GONE);
        }
        imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CreateNote.this, "Before : " + imagePath, Toast.LENGTH_SHORT).show();
                inputedImage.setImageBitmap(null);
                inputedImage.setVisibility(View.GONE);
                imageDelete.setVisibility(View.GONE);
                imagePath = null;
                Toast.makeText(CreateNote.this, "After : " + imagePath, Toast.LENGTH_SHORT).show();
            }
        });

        noteDelete = findViewById(R.id.btnDelete);
        if (availableNote == null){
            noteDelete.setVisibility(View.GONE);
        }
        noteDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmationDeleteDiaglog();
            }
        });

        // TextStyle digunakan untuk mengatur gaya/style text yg dipilih

        styleText = new StyleText();

        textBold = findViewById(R.id.featureTextBold);
        textBold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                styleText.formatText(catatan, "bold");
            }
        });

        textUnderline = findViewById(R.id.featureTextUndeline);
        textUnderline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                styleText.formatText(catatan, "underline");
            }
        });

        textItalic = findViewById(R.id.featureTextItalic);
        textItalic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                styleText.formatText(catatan, "italic");
            }
        });

        textLeft = findViewById(R.id.featureLeftParagraph);
        textLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                styleText.formatText(catatan, "left");
            }
        });

        textCenter = findViewById(R.id.featureCenterParagraph);
        textCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                styleText.formatText(catatan, "center");

//                if (availableNote != null){
//                    availableNote.setAlign("center");
//                }

//                positionFirst = "center";
            }
        });

        textRight = findViewById(R.id.featureRightParagraph);
        textRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                styleText.formatText(catatan, "right");

//                if (availableNote != null){
//                    availableNote.setAlign("right");
//                }
//
//                positionFirst = "right";
            }
        });

        exportBtn = findViewById(R.id.btnExport);
        if (availableNote == null) {
            exportBtn.setVisibility(View.GONE);
        }
        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(CreateNote.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
                }
                else{
                    if (catatan.getText().toString().trim().length() > 2000 || catatan.getLineCount() > 50){
                        Toast.makeText(CreateNote.this, "Max character (saat ini) untuk export adalah 2000", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        createPDF();
                    }
                }
            }
        });

        reference = FirebaseDatabase.getInstance().getReference().child("Notes");
        mAuth = FirebaseAuth.getInstance();

        upOrDown = findViewById(R.id.btnUpOrDown);
        if (availableNote == null){
            upOrDown.setVisibility(View.GONE);
        }
        upOrDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                    reference.push().setValue(availableNote)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(CreateNote.this, "Note berhasil diunggah", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull @NotNull Exception e) {
                                    Toast.makeText(CreateNote.this, "Gagal upload Note", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else{
                    Toast.makeText(CreateNote.this, "Login untuk upload Notes", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // method untuk membuat taskbar on pada fullscreen, penggunaan di atas
    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void createPDF(){
        String takeJudul = judul.getText().toString();
        String takeNotes = catatan.getText().toString();

        // membuat Objek PDF
        PdfDocument notePDF = new PdfDocument();
        // PageInfo untuk menentukan panjang lebar dan pageNumber
        PdfDocument.PageInfo notePageInfo = new PdfDocument.PageInfo.Builder(300, 900, 1).create();
        // inisiasi PageInfo ke dalam Page untuk membuat Page
        PdfDocument.Page notePage = notePDF.startPage(notePageInfo);

        // mengisi Judul
        // metode pertama dengan menggunakan Paint dan drwaText
        Paint pdfJudul = new Paint();
        int x = notePage.getCanvas().getWidth() / 2;
        int y = 25;
        pdfJudul.setTextAlign(Paint.Align.CENTER);
        notePage.getCanvas().drawText(takeJudul, x, y, pdfJudul);

        Paint pdfGambar = new Paint();
        Bitmap finalResized = null;
        if (availableNote.getImage() != null && !availableNote.getImage().trim().isEmpty()){
            Bitmap gambarPdf = BitmapFactory.decodeFile(availableNote.getImage());
            double width = gambarPdf.getWidth();
            double height = gambarPdf.getHeight();

            if (width > height){
                double perbandingan = width / height;
                double resizedWidth = 250;
                double resizedHeigh = resizedWidth / perbandingan;
//            Log.d("CEK WH", String.valueOf(resizedWidth));
//            Log.d("CEK WH", String.valueOf(resizedHeigh));
//            Log.d("CEK WH", String.valueOf(perbandingan));
                finalResized = scaleBitmap(gambarPdf, resizedWidth, resizedHeigh);
            }
            else if (width < height){
                double perbandingan = height / width;
                double resizedHeigh = 300;
                double resizedWidth = resizedHeigh / perbandingan;
//            Log.d("CEK WH", String.valueOf(resizedWidth));
//            Log.d("CEK WH", String.valueOf(resizedHeigh));
//            Log.d("CEK WH", String.valueOf(perbandingan));
                finalResized = scaleBitmap(gambarPdf, resizedWidth, resizedHeigh);
            }

            x -= finalResized.getWidth() / 2;
            y += 20;
            notePage.getCanvas().drawBitmap(finalResized, x, y, pdfGambar);
        }

        // mengisi catatan
        // metode kedua dengan menggunakan Staticlayout untuk wrap text agar tidak melebihi width dokumen
        Canvas canvas = notePage.getCanvas();
        TextPaint textPaint = new TextPaint();
        StaticLayout staticLayout = new StaticLayout(takeNotes, textPaint, canvas.getWidth() - 20, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        canvas.save();
        int textX = 10;
        int textY = y + finalResized.getHeight() + 10;
        canvas.translate(textX, textY);
        // draw atau menggambar
        staticLayout.draw(canvas);

        // finishPage karena telah dimasukan semua
        notePDF.finishPage(notePage);

        // membuat nama file terpisah
        String namaFile = judul.getText().toString()+".pdf";
        // File Path, untuk android yg saya pakai menggunakan (Environment.DIRECTORY_DOWNLOADS)
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + namaFile;
        File myFile = new File(filePath);
        // try catch untuk menyimpan file
        try {
            notePDF.writeTo(new FileOutputStream(myFile));  // simpan
            Toast.makeText(this, "Note Berhasil Tersimpan di " + filePath, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            String message = e.getMessage();
            Toast.makeText(this, "Error Export Note : " + message, Toast.LENGTH_SHORT).show();
        }

        notePDF.close();
    }

    public Bitmap scaleBitmap(Bitmap bitmap, double resizedWidth, double resizedHeigh) {

        Bitmap resized = null;
        resized = Bitmap.createScaledBitmap(bitmap, (int) resizedWidth, (int) resizedHeigh, true);

//        newbmp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return resized;
    }

    private void confirmationDeleteDiaglog(){
        if (availableNote != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNote.this);
            View view = LayoutInflater.from(this).inflate(R.layout.delete_confirmation_layout, (ViewGroup) findViewById(R.id.deleteComfirmationLayout));
            builder.setView(view);

            alertHapus = builder.create();
            if(alertHapus.getWindow() != null){
                alertHapus.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            view.findViewById(R.id.comfirmHapus).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    class DeleteNoteTask extends AsyncTask<Void, Void, Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NoteDatabase.getDatabase(getApplicationContext()).noteDao().deleteNote(availableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent intent = new Intent();
                            intent.putExtra("noteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            });

            view.findViewById(R.id.cancelHapus).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertHapus.dismiss();
                }
            });
        }
        alertHapus.show();
    }

    public void  setViewORUpdate(){
//        if(availableNote.getAlign() == "center"){
//            catatan.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//        }
//        else if (availableNote.getAlign() == "right"){
//            catatan.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
//        }
//        else catatan.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

        judul.setText(availableNote.getJudul());
        catatan.setText(availableNote.getCatatan());
        waktu.setText(availableNote.getWaktu());

//        Toast.makeText(this, availableNote.getImage(), Toast.LENGTH_SHORT).show();

        if (availableNote.getImage() != null && !availableNote.getImage().trim().isEmpty()){
            inputedImage.setImageBitmap(BitmapFactory.decodeFile(availableNote.getImage()));
            inputedImage.setVisibility(View.VISIBLE);
            imagePath = availableNote.getImage();
        }
    }

    public void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            selectImage();
        }
        else{
            Toast.makeText(this, "Ijin tidak diberikan", Toast.LENGTH_SHORT).show();
        }
    }

    private String getPathUri(Uri contentUri){
        String filepath;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null){
            filepath = contentUri.getPath();
        }
        else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filepath = cursor.getString(index);
            cursor.close();
        }
        Toast.makeText(this, filepath, Toast.LENGTH_SHORT).show();
        return filepath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                Uri selected = data.getData();
                if (selected != null){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selected);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        inputedImage.setImageBitmap(bitmap);
                        inputedImage.setVisibility(View.VISIBLE);
                        imageDelete.setVisibility(View.VISIBLE);

//                        Toast.makeText(this, selected.toString(), Toast.LENGTH_SHORT).show();

                        imagePath = getPathUri(selected);
                    }catch (Exception e){

                    }
                }
            }
        }
    }

    public void saveData(){
        if (judul.getText().toString().trim().isEmpty() && catatan.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Judul atau catatan masih kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        Toast.makeText(this, "Id note sebelumnya " + note.getId(), Toast.LENGTH_SHORT).show();
        note.setJudul(judul.getText().toString());
        note.setCatatan(catatan.getText().toString());
        note.setWaktu(waktu.getText().toString());
        note.setImage(imagePath);
//        note.setAlign(positionFirst);

        if (availableNote != null){
            note.setId(availableNote.getId());
        }

        Toast.makeText(this, "Id note baru " + note.getId(), Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, note.toString(), Toast.LENGTH_SHORT).show();

        Log.d("MU_Bite", note.getJudul() + note.getCatatan());

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void,Void,Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NoteDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid){
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveNoteTask().execute();

//        positionFirst = "";
    }
}