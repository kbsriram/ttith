package com.kbsriram.ttith.android.data;

// This code converts our daily event data json into something that's
// convenient for java to handle.

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CJSONDatabase
{
    public final static class Event
    {
        private Event(String year, String text, String[] links)
        {
            m_year = year;
            m_text = text;
            m_links = links;
        }

        public final String getYear()
        { return m_year; }
        public final String getText()
        { return m_text; }
        public final String[] getLinks()
        { return m_links; }

        private final String m_year;
        private final String m_text;
        private final String[] m_links;
    }

    public final static synchronized List<Event> getEvents
        (Context ctx, int month, int day)
        throws IOException, JSONException
    {
        if ((s_month == month) && (s_day == day)) {
            return shallowCopy(s_events);
        }

        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        InputStream in = null;
        try {
            in = ctx.getAssets().open("events/"+month+"/"+day+".json");
            if (in != null) {
                int nread;
                while ((nread = in.read(buf)) > 0) {
                    baout.write(buf, 0, nread);
                }
                in.close();
                in = null;
            }

            JSONTokener jtok = new JSONTokener
                (new String(baout.toByteArray(), "utf-8"));
            Object obj = jtok.nextValue();
            if (!(obj instanceof JSONArray)) {
                throw new JSONException("Unexpected - not an array");
            }
            JSONArray events = (JSONArray) obj;
            List<Event> nevents = new ArrayList<Event>(events.length());
            for (int i=0; i<events.length(); i++) {
                nevents.add(asEvent(events.getJSONObject(i)));
            }
            // Commit to our answer at this point.
            s_day = day;
            s_month = month;
            s_events.clear();
            s_events.addAll(nevents);
        }
        finally {
            if (in != null) {
                try { in.close(); }
                catch (IOException ign) {}
            }
        }
        return shallowCopy(s_events);
    }

    private final static Event asEvent(JSONObject data)
        throws JSONException
    {
        String year = data.getString("year");
        String text = data.getString("text");
        JSONArray jlinks = data.getJSONArray("links");
        String[] links = new String[jlinks.length()];
        for (int i=0; i<links.length; i++) {
            links[i] = jlinks.getString(i);
        }
        return new Event(year, text, links);
    }

    private final static List<Event> shallowCopy(List<Event> events)
    {
        List<Event> ret = new ArrayList<Event>(events.size());
        ret.addAll(events);
        return ret;
    }

    private final static List<Event> s_events = new ArrayList<Event>();
    private static int s_month = -1;
    private static int s_day = -1;
}
