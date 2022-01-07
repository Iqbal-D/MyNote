package com.example.mynote.adapter;

import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.MainActivity;
import com.example.mynote.NoteListeners;
import com.example.mynote.R;
import com.example.mynote.entiti.Note;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> listnote, searchedNotes;
    private NoteListeners noteListeners;
    private Timer timer;

    public NoteAdapter(List<Note> listnote, NoteListeners noteListeners) {
        this.listnote = listnote;
        this.noteListeners = noteListeners;
        searchedNotes = listnote;
    }

    @NonNull
    @NotNull
    @Override
    public NoteAdapter.NoteViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_item_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull NoteAdapter.NoteViewHolder holder, int position) {
        Note note = listnote.get(position);
        holder.hometxtJudul.setText(note.getJudul());
        holder.hometxtWaktu.setText(note.getWaktu());

        // ingin menampilkan sedikit bagian awal catatan (50 karakter)
        String potongan = note.getCatatan();    // ambil catatan
        String potongan1 = "";                  // penampung potongan catatan

        // kondisi untuk mengecek ukuran catatan
        if (potongan.length() > 50){    // jika karakter lebih dari 50, maka potong
            for (int i=0; i<50; i++){
                potongan1 = potongan1 + potongan.charAt(i); // masukan ke variabel penampung
            }
        }
        else if (potongan.length() < 50) {     // jika kurang dari 50, maka masukan saja
            for (int i = 0; i<potongan.length(); i++) {
                potongan1 = potongan1 + potongan.charAt(i);
            }
        }
        //tampilkan yang telah dipotong
        holder.hometxtPieces.setText(potongan1);

        if(note.getImage() != null){
            holder.homeImageShow.setImageBitmap(BitmapFactory.decodeFile(note.getImage()));
            holder.imageContainer.setVisibility(View.VISIBLE);
            holder.homeImageShow.setVisibility(View.VISIBLE);
        }
        else {
            holder.homeImageShow.setVisibility(View.GONE);
            holder.imageContainer.setVisibility(View.GONE);
        }

        holder.holderNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteListeners.onNoteClicked(listnote.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listnote.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView hometxtJudul, hometxtWaktu, hometxtPieces;
        ImageView homeImageShow;
        CardView imageContainer;
        LinearLayoutCompat holderNotes;

        public NoteViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            hometxtJudul = itemView.findViewById(R.id.homeTxtJudul);
            hometxtWaktu = itemView.findViewById(R.id.homeTxtWaktu);
            hometxtPieces = itemView.findViewById(R.id.homeTxtPieces);
            homeImageShow = itemView.findViewById(R.id.homeImgView);
            holderNotes = itemView.findViewById(R.id.layoutNotes);
            imageContainer = itemView.findViewById(R.id.cardViewImageContainer);
        }
    }

    public void searchNotes(final String pencarian){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (pencarian.trim().isEmpty()){
                    listnote = searchedNotes;
                }
                else{
                    ArrayList<Note> tempNotes = new ArrayList<>();
                    for (Note note : searchedNotes){
                        if(note.getJudul().toLowerCase().contains(pencarian.toLowerCase())
                        || note.getCatatan().toLowerCase().contains(pencarian.toLowerCase())){
                            tempNotes.add(note);
                        }
                    }
                    listnote = tempNotes;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    public void cancelTimer(){
        if (timer != null){
            timer.cancel();
        }
    }
}
