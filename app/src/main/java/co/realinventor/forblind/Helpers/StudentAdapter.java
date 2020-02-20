package co.realinventor.forblind.Helpers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import co.realinventor.forblind.Admin.AdminChatActivity;
import co.realinventor.forblind.R;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
    private ArrayList<Student> students;
    private Context context;
    private final String TAG = "StudentAdapter";

    public StudentAdapter(ArrayList<Student> students, Context context){
        this.students = students;
        this.context = context;
    }

    @NonNull
    @Override
    public StudentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_user, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull StudentAdapter.ViewHolder holder, int position) {
        final Student student = students.get(position);

        holder.nameTextView.setText(student.getName());
        holder.numberTextView.setText(student.getPhone());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "User clicked : "+student.getName());
                context.startActivity(new Intent(context, AdminChatActivity.class).putExtra("student", student));
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView nameTextView;
        TextView numberTextView;
        CardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.item_name_textview);
            numberTextView = itemView.findViewById(R.id.item_number_textview);
            cardView = itemView.findViewById(R.id.item_user_card);

        }
    }

}
