package co.realinventor.forblind.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import co.realinventor.forblind.Helpers.Student;
import co.realinventor.forblind.Helpers.StudentAdapter;
import co.realinventor.forblind.R;

public class AdminHomeActivity extends AppCompatActivity {
    private ArrayList<Student> students = new ArrayList<>();
    private DatabaseReference mRef;
    private final String TAG  = "AdminHomeActivity";
    private RecyclerView mRecyclerView;
    private StudentAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        mRef = FirebaseDatabase.getInstance().getReference();

        mRecyclerView = findViewById(R.id.recyclerViewPeople);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplication());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new StudentAdapter(students, this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        getData();
    }


    private void getData(){
        mRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange");
                for(DataSnapshot snap : dataSnapshot.getChildren()){
                    Student student = snap.getValue(Student.class);
                    students.add(student);
                }
                mAdapter = new StudentAdapter(students, AdminHomeActivity.this);
                mRecyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });
    }
}
