package com.jocelyne.mesh.instructor.session;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jocelyne.mesh.R;
import com.jocelyne.mesh.instructor.classes.Class;

import java.util.ArrayList;

public class ClassSpinnerAdapter extends BaseAdapter {

    private ArrayList<Class> mClasses;
    private Context context;
    private LayoutInflater inflater;

    public ClassSpinnerAdapter(Context context, ArrayList<Class> list) {
        this.context = context;
        mClasses = list;
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

    protected ArrayList<Class> getClasses() {
        return mClasses;
    }

    @Override
    public int getCount() {
        return mClasses.size();
    }

    @Override
    public Object getItem(int position) {
        return getClasses().get(position);
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(getClasses().get(position).CRN);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View vi = view;
        if (vi == null)
            vi = getInflater().inflate(R.layout.class_spinner_view, null);

        Class myClass = (Class) getItem(position);

        TextView classNameTextView = vi.findViewById(R.id.class_name);
        TextView classTimeTextView = vi.findViewById(R.id.class_time);

        classNameTextView.setText(myClass.name);
        classTimeTextView.setText(myClass.startTime + " - " + myClass.endTime);

        return vi;
    }
}
