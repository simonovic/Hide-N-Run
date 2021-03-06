package rs.elfak.simon.diplproba;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class NewGameFragment extends Fragment implements View.OnClickListener
{
    View v;
    Button dateBtn, timeBtn, invFr, createGame;
    EditText name, comm, numChoosenFr, address;
    static EditText time, date;
    ArrayList<User> friends;
    boolean[] chFrBool;
    static int trueCnt = 0;
    static String d, m, y, h, min;
    static boolean first = true;
    int userID, safeRad, safeTime;
    LatLng addLatLng;
    static String frList;
    SharedPreferences shPref;
    SharedPreferences.Editor editor;

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), R.style.DialogTheme, this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            /**int hh = (hourOfDay-2); // izgleda da ovo ne mora, videcemo
            if (hh < 0)
                hh += 24;*/
            String hhh = h = ""+hourOfDay;
            //h = ""+hh;
            min = ""+minute;
            /*if (hh<10)
                h = "0"+hh;*/
            if (hourOfDay<10)
                h = "0"+hourOfDay;
            if (minute<10)
                min = "0"+minute;
            if (hourOfDay<10)
                hhh = "0"+hourOfDay;
            time.setText(hhh+":"+min);
        }
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), R.style.DialogTheme, this, year, month, day);
        }
        public void onDateSet(DatePicker view, int year, int month, int day) {
            d = ""+day; m = ""+(month+1); y = ""+year;
            if (day < 10)
                d = "0"+d;
            if ((month+1) < 10)
                m = "0"+m;
            date.setText(""+d+"."+m+"."+y+".");
        }
    }

    public static class SomeDialog extends DialogFragment {
        ArrayList<User> friends;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            friends = gson.fromJson(frList, new TypeToken<ArrayList<User>>() {}.getType());
            List<String> pom = new ArrayList<String>();
            for (Iterator<User> u = friends.iterator(); u.hasNext(); )
            {
                User fr = u.next();
                pom.add(fr.getUname()+" ("+fr.getFname()+" "+fr.getLname()+")");
            }
            final CharSequence frlist[] = pom.toArray(new CharSequence[pom.size()]);
            final boolean bl[] = first ? new boolean[frlist.length] : ((MainActivity)getActivity()).getChosenFr();
            if (first) first = false;
            return new AlertDialog.Builder(getActivity(), R.style.DialogTheme)
                    .setTitle("Prijatelji:")
                    .setMultiChoiceItems(frlist, bl, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if (isChecked)
                                trueCnt++;
                            else
                                trueCnt--;
                        }
                    })
                    .setView(getActivity().getLayoutInflater().inflate(R.layout.choose_friends, null))
                    .setNegativeButton("Otkaži", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing (will close dialog)
                        }
                    })
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((MainActivity)getActivity()).setChosenFr(bl);
                            ((MainActivity)getActivity()).populateChFr();
                        }
                    })
                    .create();
        }
    }

    public static NewGameFragment newInstance() {
        NewGameFragment fragment = new NewGameFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_newgame, container, false);
        timeBtn = (Button)v.findViewById(R.id.timeBtn);
        dateBtn = (Button)v.findViewById(R.id.dateBtn);
        invFr = (Button)v.findViewById(R.id.chUser);
        createGame = (Button)v.findViewById(R.id.createGame);
        timeBtn.setOnClickListener(this);
        dateBtn.setOnClickListener(this);
        invFr.setOnClickListener(this);
        createGame.setOnClickListener(this);
        name = (EditText)v.findViewById(R.id.name);
        time = (EditText)v.findViewById(R.id.time);
        date = (EditText)v.findViewById(R.id.date);
        comm = (EditText)v.findViewById(R.id.com);
        numChoosenFr = (EditText)v.findViewById(R.id.numFr);
        address = (EditText)v.findViewById(R.id.address);
        address.setFocusable(false);
        address.setClickable(true);
        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), MapActivity.class);
                shPref = getActivity().getSharedPreferences(Constants.loginPref, Context.MODE_PRIVATE);
                editor = shPref.edit();
                editor.putString(Constants.modePref, "newGame");
                editor.commit();
                i.putExtra("mode", "newGame");
                startActivityForResult(i, 100);
            }
        });
        String frList = ((MainActivity)getActivity()).getFrResp();
        if (!frList.equals("")) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            friends = gson.fromJson(frList, new TypeToken<ArrayList<User>>(){}.getType());
        }
        numChoosenFr.setText("Broj pozvanih prijatelja: 0");
        userID = ((MainActivity)getActivity()).getUserID();

        Button safeBtn = (Button)v.findViewById(R.id.safeZone);
        safeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                safeDialog();
            }
        });
        safeRad = 25;
        safeTime = 50;
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK)
        {
            addLatLng = (LatLng)data.getExtras().get("latlng");
            Geocoder geocoder;
            List<Address> addresses = new ArrayList<Address>();
            geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(addLatLng.latitude, addLatLng.longitude, 1);
            } catch (IOException e) {}
            String address1 = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            address.setText(address1+", "+city);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.timeBtn:
                DialogFragment timeDialog = new TimePickerFragment();
                timeDialog.show(getActivity().getSupportFragmentManager(), "timePicker");
                break;
            case R.id.dateBtn:
                DialogFragment dateDialog = new DatePickerFragment();
                dateDialog.show(getActivity().getSupportFragmentManager(), "datePicker");
                break;
            case R.id.addBtn:
                Toast.makeText(getActivity().getApplicationContext(),"Nadji adresu", Toast.LENGTH_LONG).show();
                break;
            case R.id.chUser:
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                SomeDialog newFragment = new SomeDialog ();
                frList = ((MainActivity)getActivity()).getFrResp();
                if (!frList.equals(""))
                    newFragment.show(ft, "dialog");
                else
                    ((MainActivity)getActivity()).showSnackBar("Nemate prijatelje!");
                break;
            case R.id.createGame:
                createGame();
                break;
        }
    }

    private void safeDialog()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
        dialog.setTitle("Sigurna zona");
        View v = getActivity().getLayoutInflater().inflate(R.layout.safe_zone, null);
        SeekBar barRad = (SeekBar)v.findViewById(R.id.barRad);
        barRad.setProgress(safeRad-10);
        barRad.setMax(30);
        SeekBar barTime = (SeekBar)v.findViewById(R.id.barTime);
        barTime.setProgress(safeTime-20);
        barTime.setMax(60);
        final TextView t1 = (TextView)v.findViewById(R.id.t1);
        final TextView t2 = (TextView)v.findViewById(R.id.t2);
        barRad.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                safeRad = progress + 10;
                t1.setText("Radijus sigurne zone: "+safeRad+"m");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        barTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                safeTime = progress + 20;
                t2.setText("Dozvoljeno vreme u sigurnoj zoni: "+safeTime+"s");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        dialog.setView(v);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }

    private void createGame()
    {
        String net, cet, aet, det, tet;
        net = name.getText().toString();
        cet = comm.getText().toString();
        aet = address.getText().toString();
        det = date.getText().toString();
        tet = time.getText().toString();
        if (net.equals("") || tet.equals("") || det.equals("") || cet.equals("") || aet.equals("")) {
            ((MainActivity)getActivity()).showSnackBar("Morate popuniti sva polja!");
        } else if (trueCnt < 2) {
            ((MainActivity)getActivity()).showSnackBar("Potrebna su minimum dva prijatelja!");
        } else {
            JSONArray jsonarray = new JSONArray();
            for (int i = 0; i < friends.size(); i++) {
                if (chFrBool[i]) {
                    try {
                    jsonarray.put(friends.get(i).getId());
                    } catch (Exception e) {} } }
            String datetime = y+"-"+m+"-"+d+"T"+h+":"+min+":00";
            JSONObject data = new JSONObject();
            try {
                data.put("name", net);
                data.put("desc", cet);
                data.put("cID", userID);
                data.put("datetime", datetime);
                data.put("lat", addLatLng.latitude);
                data.put("lng", addLatLng.longitude);
                data.put("invFrID", jsonarray);
                data.put("safeRad", safeRad);
                data.put("safeTime", safeTime);
            } catch (JSONException e) { e.printStackTrace(); }
            first = true;
            trueCnt = 0;
            LoginActivity.socket.emit("newGameRequest", data);
        }
    }

    public void populateChoosenFr()
    {
        chFrBool = ((MainActivity)getActivity()).getChosenFr();
        //int trueCount = Arrays.deepToString((Object[])chFrBool).replaceAll("[^t]", "").length();
        numChoosenFr.setText("Broj pozvanih prijatelja: "+trueCnt);
    }
}
