package com.jocelyne.mesh.instructor.hype;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jocelyne.mesh.R;
import com.jocelyne.mesh.session_management.Student;

import java.util.Locale;
import java.util.Map;

public class PresentStudentAdapter extends BaseAdapter {

    private Map<String, Student> mPresentStudents;

    private Context context;
    private LayoutInflater inflater = null;

    public PresentStudentAdapter(Context context, Map<String, Student> mPresentStudents) {
        this.context = context;
        this.mPresentStudents = mPresentStudents;
    }

    protected LayoutInflater getInflater() {
        if (inflater == null) {
            inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        return inflater;
    }

    protected Context getContext() {
        return context;
    }

    protected Map<String, Student> getPresentStudents() {
        return mPresentStudents;
    }

    @Override
    public int getCount() {
        return mPresentStudents.size();
    }

    @Override
    public Object getItem(int position) {
        return getPresentStudents().values().toArray()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View vi = convertView;

        if (vi == null)
            vi = getInflater().inflate(R.layout.present_student_cell_view, null);

        TextView presentStudentID = (TextView)vi.findViewById(R.id.present_student_id);

        Student presentStudent = (Student) getItem(position);

        presentStudentID.setText(String.format(Locale.US,"%d", presentStudent.getInstance().getUserIdentifier()));

        ImageView statusImageView = vi.findViewById(R.id.student_status);
        if (presentStudent.lost) {
            statusImageView.setImageResource(R.drawable.ic_red_circle_24dp);
        } else {
            statusImageView.setImageResource(R.drawable.ic_green_circle_24dp);
        }

        return vi;
    }
}
