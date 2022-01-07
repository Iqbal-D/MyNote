package com.example.mynote.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.MainActivity;
import com.example.mynote.Note;
import com.example.mynote.R;
import com.example.mynote.database.NoteDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class NoteAdapterFirebase extends RecyclerView.Adapter<NoteAdapterFirebase.MyViewHolder> {
    Context contex;
    ArrayList<Note> listNote;

    public NoteAdapterFirebase(Context contex, ArrayList<Note> listNote) {
        this.contex = contex;
        this.listNote = listNote;
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.online_layout_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position) {
        Note note = listNote.get(position);
        holder.judulNote.setText(note.getJudul());
        holder.waktuNote.setText(note.getWaktu());

        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFromFirebase(note);
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFromFirebase(note);
                notifyItemRemoved(holder.getAdapterPosition());
//                notifyItemRangeChanged(position, getItemCount());

                Intent intent = new Intent("Try Pass");
                LocalBroadcastManager.getInstance(contex).sendBroadcast(intent);   // send
            }
        });
    }

    @Override
    public int getItemCount() {
        return listNote.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView judulNote, waktuNote;
        ImageView download, delete;
        DatabaseReference referenceOnClik;

        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            judulNote = itemView.findViewById(R.id.onlineJudulNote);
            waktuNote = itemView.findViewById(R.id.onlineWaktuNote);
            download = itemView.findViewById(R.id.onlineDownloadNote);
            delete = itemView.findViewById(R.id.onlineDeleteNote);

            referenceOnClik = FirebaseDatabase.getInstance().getReference().child("Notes");
        }
    }

    private void saveFromFirebase(Note savingNote){
        final com.example.mynote.entiti.Note importNote = new com.example.mynote.entiti.Note();

        importNote.setJudul(savingNote.getJudul());
        importNote.setCatatan(savingNote.getCatatan());
        importNote.setWaktu(savingNote.getWaktu());

        if (savingNote.getImage() != null){
            importNote.setImage(savingNote.getImage());
        }

        Log.d("MU_Bite", savingNote.getJudul() + savingNote.getCatatan());

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void,Void,Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NoteDatabase.getDatabase(contex).noteDao().insertNote(importNote);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid){
                super.onPostExecute(aVoid);
                ((Activity)contex).finishAffinity();
                contex.startActivity(new Intent(contex, MainActivity.class));
//                        contex.startActivity(new Intent(contex, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        }
        new SaveNoteTask().execute();
    }

    private void deleteFromFirebase(Note deleteNote){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query queryDelete =  reference.child("Notes").orderByChild("judul").equalTo(deleteNote.getJudul());

        queryDelete.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    dataSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(contex, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
