package rs.elfak.simon.diplproba;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class UserListAdapter extends ArrayAdapter<String>
{
    Context context;
    String[] name;
    String[] uname;
    ArrayList<Bitmap> img;
    String[] fon;

    public UserListAdapter(Context context, String[] name, String[] uname, ArrayList<Bitmap> img, String[] fon)
    {
        super(context, R.layout.friend_list,name);
        this.context = context;
        this.name = name;
        this.uname = uname;
        this.img = img;
        this.fon = fon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.friend_list, null, true);

        TextView tvl = (TextView)rowView.findViewById(R.id.tvl);
        TextView tvm = (TextView)rowView.findViewById(R.id.tvm);
        ImageView imv = (ImageView)rowView.findViewById(R.id.imageView);
        ImageView imaf = (ImageView)rowView.findViewById(R.id.imaf);

        tvl.setText(name[position]);
        tvm.setText(uname[position]);
        imv.setImageBitmap(img.get(position));
        if (fon[position].equals("no"))
            imaf.setImageResource(R.drawable.ic_person_add);

        return rowView;
    }
}
