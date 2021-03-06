package collado.fernando.todoapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import collado.fernando.todoapp.R;
import collado.fernando.todoapp.helpers.DBHelper;
import collado.fernando.todoapp.models.Task;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by Fernando on 3/02/18.
 */

public class MySection extends StatelessSection {
    private ArrayList<Task> taskList;
    private String section_date;
    private boolean expanded;
    private Context mainContext;
    private String[] TAGS;
    private SectionedRecyclerViewAdapter sectionedAdapter;

    public MySection(String section_name, ArrayList<Task> taskList,
                     SectionedRecyclerViewAdapter sectionedAdapter,
                     Context mainContext, String[] TAGS
                     ) {
        super(new SectionParameters.Builder(R.layout.task)
            .headerResourceId(R.layout.section_header)
            .build()
        );

        this.TAGS = TAGS;
        this.mainContext = mainContext;
        this.section_date = section_name.substring(8,10) + '-' + section_name.substring(5,7) + '-' + section_name.substring(0,4);;
        this.taskList = taskList;
        this.sectionedAdapter = sectionedAdapter;

        // Set all sections hidden except today's section
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String current_date = sdf.format(System.currentTimeMillis());
        this.expanded = current_date.equals(this.section_date);
    }

    private void updateView(){
        sectionedAdapter.notifyDataSetChanged();
    }

    @Override
    public int getContentItemsTotal() {
        return expanded ? taskList.size() : 0;
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new MyItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final MyItemViewHolder itemHolder = (MyItemViewHolder) holder;

        final DBHelper db = new DBHelper(itemHolder.taskName.getContext());

        itemHolder.taskName.setText(taskList.get(position).getName());
        itemHolder.taskTag.setText(taskList.get(position).getTag());
        itemHolder.taskDone.setChecked(taskList.get(position).isDone());

        itemHolder.editTask.setOnClickListener(new CompoundButton.OnClickListener() {

            @Override
            public void onClick(View view) {
            final Task task = taskList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(mainContext);
                builder.setTitle("Edit TODO");

                LayoutInflater inflater = (LayoutInflater) mainContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View alertLayout = inflater.inflate(R.layout.dialog_edit_task, null);
                final EditText input = alertLayout.findViewById(R.id.edit_task_name);
                final Spinner dropdown = alertLayout.findViewById(R.id.edit_task_tag);

                input.setText(task.getName());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(mainContext, android.R.layout.simple_spinner_dropdown_item, TAGS);

                int spinnerPosition = adapter.getPosition(task.getTag());
                dropdown.setAdapter(adapter);
                dropdown.setSelection(spinnerPosition);


                builder.setView(alertLayout);
                builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString().trim();

                        task.setName(name);
                        task.setTag(dropdown.getSelectedItem().toString());
                        db.updateTask(task);

                        dialog.dismiss();
                        updateView();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }

        });

        itemHolder.taskDone.setOnClickListener(new CompoundButton.OnClickListener() {

            @Override
            public void onClick(View view) {
                boolean done = itemHolder.taskDone.isChecked();
                taskList.get(position).setDone(done);
                db.updateTask(taskList.get(position), !done);
            }

        });

        itemHolder.removeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                db.deleteTask(taskList.get(position));
                taskList.remove(position);
                updateView();
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        final HeaderViewHolder itemHolder = (HeaderViewHolder) holder;
        itemHolder.section_date.setText(section_date);
        itemHolder.imgArrow.setImageResource(
                expanded ? R.drawable.ic_keyboard_arrow_up_black_48dp : R.drawable.ic_keyboard_arrow_down_black_48dp
        );

        itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expanded = !expanded;
                itemHolder.imgArrow.setImageResource(
                        expanded ? R.drawable.ic_keyboard_arrow_up_black_18dp : R.drawable.ic_keyboard_arrow_down_black_18dp
                );
                updateView();
            }
        });
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView section_date;
        private final View rootView;
        private ImageView imgArrow;

        public HeaderViewHolder(View itemView){
            super(itemView);
            rootView = itemView;
            section_date = itemView.findViewById(R.id.section_date);
            imgArrow = itemView.findViewById(R.id.imgArrow);
        }
    }

    class MyItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskName;
        private final TextView taskTag;
        private final CheckBox taskDone;
        private final ImageView removeBtn;
        private final ImageView editTask;

        protected MyItemViewHolder(View itemView){
            super(itemView);

            taskDone  = itemView.findViewById(R.id.cb);
            taskName  = itemView.findViewById(R.id.taskName);
            taskTag   =  itemView.findViewById(R.id.taskTag);
            editTask  = itemView.findViewById(R.id.editTask);
            removeBtn = itemView.findViewById(R.id.deleteTask);
            removeBtn.setClickable(true);
        }
    }
}
