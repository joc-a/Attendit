package com.jocelyne.mesh.instructor.hype;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jocelyne.mesh.R;
import com.jocelyne.mesh.session.Student;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;

public class PresentStudentAdapter extends BaseAdapter {

    private Map<Long, Student> mPresentStudents;

    private Context context;
    private LayoutInflater inflater = null;

    public PresentStudentAdapter(Context context, Map<Long, Student> mPresentStudents) {
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

    protected Map<Long, Student> getPresentStudents() {
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
        TextView announcement = (TextView)vi.findViewById(R.id.hype_announcement);

        Student presentStudent = (Student) getItem(position);

        presentStudentID.setText(String.format(Locale.US,"%d", presentStudent.getInstance().getUserIdentifier()));

        try {
            announcement.setText(new String(presentStudent.getInstance().getAnnouncement(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            announcement.setText("");
        }

        return vi;
    }
}
