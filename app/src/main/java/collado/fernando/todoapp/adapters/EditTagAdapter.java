package collado.fernando.todoapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import collado.fernando.todoapp.R;
import collado.fernando.todoapp.helpers.DBHelper;
import collado.fernando.todoapp.models.Tag;

/**
 * Created by root on 24/02/18.
 */

public class EditTagAdapter extends RecyclerView.Adapter<EditTagAdapter.EditTagViewHolder> {
    private String[] tags;
    private Context context;

    public EditTagAdapter(Context mContext, String[] mTags){
        this.context = mContext;
        this.tags = mTags;
    }

    @Override
    public EditTagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag, parent, false);
        return new EditTagViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return tags.length;
    }

    @Override
    public void onBindViewHolder(final EditTagViewHolder holder, final int position) {
        String tagName = tags[position];
        holder.tagName.setText(tagName);

        final Context ctxt = holder.tagName.getContext();
        final DBHelper db = new DBHelper(ctxt);

        if(tagName.equals("No tag")){
            holder.deleteTag.setEnabled(false);
            holder.deleteTag.setImageDrawable(null);
            holder.editTag.setEnabled(false);
            holder.editTag.setImageDrawable(null);
        }
        else{
            holder.deleteTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.deleteTag(new Tag(tags[position]));
                    tags = db.getAllTags();
                    notifyDataSetChanged();
                }
            });

            holder.editTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ctxt);
                    builder.setTitle("Edit tag");

                    final EditText input = new EditText(ctxt);
                    input.setText(tags[position]);
                    builder.setView(input);

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String m_Text = input.getText().toString();
                            Tag tag = new Tag(m_Text);
                            db.updateTag(tag, tags[position]);
                            holder.tagName.setText(m_Text);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();

                    /*
                    db.updateTag(new Tag(tags[position]));
                    tags = db.getAllTags();
                    notifyDataSetChanged();
                    */
                }
            });
        }
    }

    public class EditTagViewHolder extends RecyclerView.ViewHolder {
        private TextView tagName;
        private ImageView editTag;
        private ImageView deleteTag;

        public EditTagViewHolder(View view){
            super(view);
            editTag = view.findViewById(R.id.editTag);
            deleteTag = view.findViewById(R.id.deleteTag);
            tagName = view.findViewById(R.id.tagName);
        }

    }

}
