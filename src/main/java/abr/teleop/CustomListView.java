package abr.teleop;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomListView extends ArrayAdapter<String> {
	ArrayList<String> str; 
	LayoutInflater inflater;
	int id;
    String[] items ;
    
	public CustomListView(Context context, int textViewResourceId, ArrayList<String> objects) {
		super(context, textViewResourceId, objects);
        inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        str = objects;
	}
 
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = inflater.inflate(R.layout.listview_simple_row, parent, false);
		row.setBackgroundResource(R.drawable.spinner_dropdownlist);
        parent.setBackgroundColor(Color.rgb(0xff, 0xfb, 0xd5));
		TextView textView = (TextView) row.findViewById(R.id.txt1);
		textView.setTextColor(Color.rgb(0xca, 0x25, 0x59));
		textView.setText(str.get(position));
		return row;
	}
}