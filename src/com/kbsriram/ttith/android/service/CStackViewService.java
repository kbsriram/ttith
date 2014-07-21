package com.kbsriram.ttith.android.service;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.format.DateFormat;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.kbsriram.ttith.android.R;
import com.kbsriram.ttith.android.data.CJSONDatabase;
import com.kbsriram.ttith.android.provider.CWidgetProvider;
import com.kbsriram.ttith.android.util.CUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import org.json.JSONException;

public class CStackViewService extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    { return new Factory(getApplicationContext(), intent); }

    public static void refreshData(Context ctx)
    {
        GregorianCalendar cal = new GregorianCalendar();
        setData(cal, getEvents(ctx.getApplicationContext(), cal));
    }

    private final static void setData
        (GregorianCalendar cal, List<CJSONDatabase.Event> events)
    {
        synchronized (s_entries) {
            s_day = cal.get(Calendar.DAY_OF_MONTH);
            s_month = cal.getDisplayName
                (Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
                .toUpperCase();

            if ((events == null) || (events.size() == 0)) {
                return;
            }

            s_entries.clear();
            s_links.clear();
            for (CJSONDatabase.Event ev: events) {
                s_entries.add
                    (Html.fromHtml
                     ("<font color=\"#a92121\"><b>"+
                      ev.getYear()+ "</b></font> - "+ev.getText()+
                      TRAILING_HACK));
                String[] links = ev.getLinks();
                if ((links != null) && (links.length > 0)) {
                    s_links.add(Uri.parse(links[0]));
                }
                else {
                    s_links.add(null);
                }
            }
        }
    }

    private final static int getItemCount()
    {
        synchronized (s_entries) {
            return s_entries.size();
        }
    }

    private final static CharSequence getItemText(int pos)
    {
        CharSequence ret = null;
        synchronized (s_entries) {
            if (s_entries.size() > 0) {
                if (pos >= s_entries.size()) { // sanity
                    pos = (pos % s_entries.size());
                }
                ret = s_entries.get(pos);
            }
        }
        if (ret == null) { ret = ""; }
        return ret;
    }

    private final static Uri getItemLink(int pos)
    {
        synchronized (s_entries) {
            if (s_entries.size() > 0) {
                if (pos >= s_entries.size()) { // sanity
                    pos = (pos % s_entries.size());
                }
                return s_links.get(pos);
            }
            else {
                return null;
            }
        }
    }


    private final static List<CJSONDatabase.Event> getEvents
        (Context ctx, GregorianCalendar cal)
    {
        int mon = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        List<CJSONDatabase.Event> ret = null;

        // skip errors, in case my db is incomplete.
        try {
            ret = CJSONDatabase.getEvents(ctx, mon, day);
        }
        catch (IOException ioe) {
            CUtils.LOGD(TAG, "Skip bad: "+mon+"/"+day, ioe);
        }
        catch (JSONException jse) {
            CUtils.LOGD(TAG, "Skip bad: "+mon+"/"+day, jse);
        }
        return ret;
    }

    private final static List<CharSequence> s_entries =
        new ArrayList<CharSequence>();
    private static int s_day = -1;
    private static String s_month = null;
    private final static List<Uri> s_links = new ArrayList<Uri>();

    public final static class Factory
        implements RemoteViewsService.RemoteViewsFactory
    {
        private Factory(Context ctx, Intent intent)
        { m_ctx = ctx; }

        // Nops
        public void onCreate() { }
        public void onDestroy() { }
        public RemoteViews getLoadingView() { return null; }
        public void onDataSetChanged() { }

        public int getCount()
        { return getItemCount(); }

        public int getViewTypeCount()
        { return 1; }

        public long getItemId(int position) { return position; }
        public boolean hasStableIds() { return true; }

        public RemoteViews getViewAt(int pos)
        {
            RemoteViews rv =
                new RemoteViews(m_ctx.getPackageName(), R.layout.widget_item);
            rv.setTextViewText(R.id.widget_item_tv, getItemText(pos));
            if (s_day > 0) {
                rv.setTextViewText
                    (R.id.widget_item_day, Integer.toString(s_day));
            }
            if (s_month != null) {
                rv.setTextViewText(R.id.widget_item_month, s_month);
            }
            Uri link = getItemLink(pos);
            if (link != null) {
                Intent fill = new Intent();
                fill.setData(link);
                rv.setOnClickFillInIntent(R.id.widget_item_base, fill);
            }
            return rv;
        }

        private final Context m_ctx;
    }
    private final static String TAG =
        CUtils.makeLogTag(CStackViewService.class);
    private final static String TRAILING_HACK =
        "<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>";
}
