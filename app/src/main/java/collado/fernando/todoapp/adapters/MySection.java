package collado.fernando.todoapp.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
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
    private SectionedRecyclerViewAdapter sectionedAdapter;

    public MySection(String section_name, ArrayList<Task> taskList,
                     SectionedRecyclerViewAdapter sectionedAdapter) {
        super(new SectionParameters.Builder(R.layout.task)
            .headerResourceId(R.layout.section_header)
            .build()
        );

        this.section_date = section_name.substring(8,10) + '-' + section_name.substring(5,7) + '-' + section_name.substring(0,4);;
        this.taskList = taskList;
        this.sectionedAdapter = sectionedAdapter;

        // Set all sections hidden except today's section
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String current_date = sdf.format(System.currentTimeMillis());
        this.expanded = current_date.equals(this.section_date);
    }

    public void updateView(){
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
        itemHolder.taskDone.setChecked(taskList.get(position).isDone());

        itemHolder.taskDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                taskList.get(position).setDone(b);
                db.updateTask(taskList.get(position), !b);
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
                expanded ? R.drawable.ic_keyboard_arrow_up_black_18dp : R.drawable.ic_keyboard_arrow_down_black_18dp
        );

        itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expanded = !expanded;
                itemHolder.imgArrow.setImageResource(
                        expanded ? R.drawable.ic_keyboard_arrow_up_black_18dp : R.drawable.ic_keyboard_arrow_down_black_18dp
                );
                sectionedAdapter.notifyDataSetChanged();
            }
        });

    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView section_date;
        private final View rootView;
        private ImageView imgArrow;

        public HeaderViewHolder(View itemView){
            super(itemView);
            rootView = itemView;
            section_date = (TextView) itemView.findViewById(R.id.section_date);
            imgArrow = (ImageView) itemView.findViewById(R.id.imgArrow);
        }
    }

    class MyItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskName;
        private final CheckBox taskDone;
        private final ImageView removeBtn;

        public MyItemViewHolder(View itemView){
            super(itemView);
            taskDone = (CheckBox)itemView.findViewById(R.id.cb);
            taskName = (TextView)itemView.findViewById(R.id.taskName);
            removeBtn = (ImageView) itemView.findViewById(R.id.deleteTask);
            removeBtn.setClickable(true);
        }
    }

}
