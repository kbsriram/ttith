package com.kbsriram.ttith.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.kbsriram.ttith.android.R;
import com.kbsriram.ttith.android.data.CJSONDatabase;
import com.kbsriram.ttith.android.util.CUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import org.json.JSONException;

public class CStartActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        m_inflater = getLayoutInflater();
        ListView lv = (ListView) findViewById(R.id.main_lv);
        lv.setAdapter(m_adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick
                    (AdapterView p, View v, int pos, long id) {
                    showLink(pos);
                }
            });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        maybeUpdateAdapter();
    }

    private final void showLink(int pos)
    {
        if (m_events.size() == 0) { return; }
        if (pos >= m_events.size()) {
            pos = (pos % m_events.size());
        }
        String[] links = m_events.get(pos).getLinks();
        if ((links == null) || (links.length == 0)) {
            return;
        }
        Intent show = new Intent(Intent.ACTION_VIEW);
        show.setData(Uri.parse(links[0]));
        startActivity(show);
    }

    private final void maybeUpdateAdapter()
    {
        // Check whether our day has changed.
        GregorianCalendar cal = new GregorianCalendar();
        int mon = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        if ((mon == m_month) && (day == m_day)) {
            // okay, nothing to do.
            return;
        }

        // Set up the title.
        setTitle
            (cal.getDisplayName
             (Calendar.MONTH, Calendar.SHORT, Locale.getDefault())+ " "+day);

        String db_file = "events/"+mon+"/"+day+".json";
        List<CJSONDatabase.Event> events;
        try {
            events = CJSONDatabase.getEvents(getAssets().open(db_file));
        }
        catch (IOException ioe) {
            CUtils.LOGD(TAG, "Skip bad: "+db_file, ioe);
            return;
        }
        catch (JSONException jse) {
            CUtils.LOGD(TAG, "Skip bad: "+db_file, jse);
            return;
        }

        m_month = mon;
        m_day = day;
        if (events != null) {
            m_events.clear();
            m_events.addAll(events);
            m_adapter.notifyDataSetChanged();
        }
    }

    private final class EventAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        { return m_events.size(); }

        @Override
        public Object getItem(int pos)
        { return pos; }

        @Override
        public long getItemId(int pos)
        { return pos; }

        @Override
        public View getView(int pos, View conv, ViewGroup parent)
        {
            ViewGroup vg;
            if (conv != null) {
                vg = (ViewGroup) conv;
            }
            else {
                vg = (ViewGroup) m_inflater.inflate(R.layout.main_item, null);
            }
            TextView year =
                (TextView) (vg.findViewById(R.id.main_item_year_tv));
            TextView text =
                (TextView) (vg.findViewById(R.id.main_item_text_tv));

            if (m_events.size() == 0) {
                // woops.
                year.setText("");
                text.setText("");
            }
            else {
                if (pos >= m_events.size()) {
                    pos = (pos % m_events.size());
                }
                CJSONDatabase.Event ev = m_events.get(pos);
                year.setText(ev.getYear());
                text.setText(ev.getText());
            }
            return vg;
        }
    }

    private int m_month = -1;
    private int m_day = 0;
    private final List<CJSONDatabase.Event> m_events =
        new ArrayList<CJSONDatabase.Event>();
    private final EventAdapter m_adapter = new EventAdapter();
    private LayoutInflater m_inflater;
    private final static String TAG =
        CUtils.makeLogTag(CStartActivity.class);
}
