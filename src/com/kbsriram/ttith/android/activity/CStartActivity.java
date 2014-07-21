package com.kbsriram.ttith.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
        case R.id.action_about:
            startActivity(new Intent(this, CAboutActivity.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        maybeUpdateAdapter();
    }

    @Override
    public void onDestroy()
    {
        if (m_dataloader != null) {
            m_dataloader.setParent(null);
            m_dataloader = null;
        }
        super.onDestroy();
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

        // Unless we have an existing loader in progress
        if (m_dataloader == null) {
            m_dataloader = new DataLoader(this, mon, day);
            m_dataloader.execute();
        }
    }
    private void setData(List<CJSONDatabase.Event> events, int mon, int day)
    {
        m_dataloader = null;
        m_month = mon;
        m_day = day;
        if (events != null) {
            m_events = events;
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

    private final static class DataLoader
        extends AsyncTask<Void,Void,List<CJSONDatabase.Event>>
    {
        private DataLoader(CStartActivity parent, int mon, int day)
        {
            m_parent = parent;
            m_mon = mon;
            m_day = day;
            m_ctx = parent.getApplicationContext();
        }

        @Override
        protected List<CJSONDatabase.Event> doInBackground(Void... ignore)
        {
            List<CJSONDatabase.Event> ret = null;
            try {
                ret = CJSONDatabase.getEvents(m_ctx, m_mon, m_day);
            }
            catch (IOException ioe) {
                CUtils.LOGD(TAG, "Skip bad: "+m_mon+"/"+m_day, ioe);
            }
            catch (JSONException jse) {
                CUtils.LOGD(TAG, "Skip bad: "+m_mon+"/"+m_day, jse);
            }
            return ret;
        }

        @Override
        protected void onPostExecute(List<CJSONDatabase.Event> ev)
        {
            synchronized(this) {
                if (m_parent != null) {
                    m_parent.setData(ev, m_mon, m_day);
                }
            }
        }

        private void setParent(CStartActivity parent)
        {
            synchronized(this) {
                m_parent = parent;
            }
        }

        private CStartActivity m_parent;
        private final Context m_ctx;
        private final int m_mon;
        private final int m_day;
    }

    private DataLoader m_dataloader = null;
    private int m_month = -1;
    private int m_day = 0;
    private List<CJSONDatabase.Event> m_events =
        new ArrayList<CJSONDatabase.Event>();
    private final EventAdapter m_adapter = new EventAdapter();
    private LayoutInflater m_inflater;
    private final static String TAG =
        CUtils.makeLogTag(CStartActivity.class);
}
