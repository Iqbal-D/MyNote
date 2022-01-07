package com.example.mynote;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mynote.adapter.NoteAdapter;
import com.example.mynote.database.NoteDatabase;
import com.example.mynote.entiti.Note;
import com.example.mynote.onlinePage.AccountPage;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteListeners{
    ArrayList<Note> listnote;
    RecyclerView notesRclview;
    NoteAdapter noteAdapter;
    ImageView addNote, onlineSpace;
    TextView searchNotes;

    public static  final  int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTE = 3;

    private int noteClickedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // mencegah items terdorong naik ketika keyboard muncul
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

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

        notesRclview = findViewById(R.id.noteRecyclerview);
        notesRclview.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        listnote = new ArrayList<>();
        noteAdapter = new NoteAdapter(listnote, this);
        notesRclview.setAdapter(noteAdapter);

        showNotes(REQUEST_CODE_SHOW_NOTE, false);

        addNote = findViewById(R.id.add_note_btn);
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), CreateNote.class), REQUEST_CODE_ADD_NOTE);
            }
        });

        searchNotes = findViewById(R.id.homeTxtSearch);
        searchNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(listnote.size() != 0){
                    noteAdapter.searchNotes(s.toString());
                }
            }
        });

        onlineSpace = findViewById(R.id.homebtnUpOrDown);
        onlineSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AccountPage.class));
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


    private void showNotes(final int requestCode, final boolean noteDeleted){

        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NoteDatabase.getDatabase(getApplicationContext()).noteDao().getNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (requestCode == REQUEST_CODE_SHOW_NOTE){
                    if (listnote.size() == 0){
                        listnote.addAll(notes);
                        noteAdapter.notifyDataSetChanged();
                    }
                }
                else if (requestCode == REQUEST_CODE_ADD_NOTE){
                    listnote.add(0, notes.get(0));
                    noteAdapter.notifyItemInserted(0);
                    notesRclview.smoothScrollToPosition(0);
                }
                else if (requestCode == REQUEST_CODE_UPDATE_NOTE){
                    listnote.remove(noteClickedPosition);
                    if(noteDeleted){
                        noteAdapter.notifyItemRemoved(noteClickedPosition);
                    }
                    else{
                        listnote.add(noteClickedPosition, notes.get(noteClickedPosition));
                        noteAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }
                Log.d("MU_Bite", notes.toString());
            }
        }
        new GetNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            showNotes(REQUEST_CODE_ADD_NOTE, false);
        }
        else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK){
            if (data != null){
                showNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("noteDeleted", false));
            }
        }
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Toast.makeText(this, "Posisi " + position, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), CreateNote.class);
        intent.putExtra("viewORupdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }
}