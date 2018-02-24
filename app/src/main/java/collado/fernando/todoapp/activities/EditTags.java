package collado.fernando.todoapp.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

import collado.fernando.todoapp.R;
import collado.fernando.todoapp.adapters.EditTagAdapter;
import collado.fernando.todoapp.helpers.DBHelper;
import collado.fernando.todoapp.models.Tag;

/**
 * Created by Fernando on 24/02/18.
 */

public class EditTags extends AppCompatActivity {

    RecyclerView recyclerView;
    EditTagAdapter adapter;
    private static String[] TAGS;
    private static  String NO_TAG = "No tag";
    private DBHelper db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_tags);

        db = new DBHelper(this);
        TAGS = db.getAllTags();

        setLayout();

        FloatingActionButton addTask = findViewById(R.id.addTag);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = EditTags.this.setCustomAlertDialog();
                builder.show();
            }
        });

    }

    public AlertDialog.Builder setCustomAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add tag");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                Tag tag = new Tag(m_Text);

                db.addTag(tag);
                ArrayList<String> temp_tags = new ArrayList<>();
                for(String s : TAGS){
                    if(!s.equals(NO_TAG)) temp_tags.add(s);
                }
                temp_tags.add(tag.getName());
                setLayout();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder;
    }

    public void setLayout(){
        TAGS = db.getAllTags();

        recyclerView = findViewById(R.id.edit_tags_recycler);
        adapter = new EditTagAdapter(this, TAGS);
        recyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(EditTags.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
    }

}
