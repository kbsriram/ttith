package com.kbsriram.ttith.android.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.RemoteViews;
import com.kbsriram.ttith.android.R;
import com.kbsriram.ttith.android.activity.CStartActivity;
import com.kbsriram.ttith.android.service.CStackViewService;
import com.kbsriram.ttith.android.service.CBackgroundService;
import com.kbsriram.ttith.android.util.CUtils;

public class CWidgetProvider
    extends AppWidgetProvider
{
    @Override
    public void onDisabled(Context ctx)
    { CUtils.uninstallUpdateWidgetAlarm(ctx);  }

    @Override
    public void onUpdate
        (Context ctx, AppWidgetManager mgr, int[] ids)
    {
        initWidgets(ctx, mgr, ids);
        CUtils.installUpdateWidgetAlarm(ctx);

        // Kick off an initial background refresh.
        CBackgroundService.asyncUpdateWidget(ctx);
    }

    @SuppressWarnings("deprecation")
    private final static void initWidgets
        (Context ctx, AppWidgetManager mgr, int[] ids)
    {
        for (int id : ids) {
            RemoteViews rv =
                new RemoteViews(ctx.getPackageName(), R.layout.widget);

            // Setup the flipper-adapter for the remote view.
            Intent intent =
                new Intent(ctx, CStackViewService.class)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);

            // When intents are compared, the extras are ignored, so
            // we need to embed the extras into the data so that the
            // extras will not be ignored.
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            // NB: deprecated after api-14; but minsdk is 12.
            rv.setRemoteAdapter(id, R.id.widget_vf, intent);
            // rv.setRemoteAdapter(R.id.widget_vf, intent);

            Intent main_intent = new Intent(Intent.ACTION_VIEW);
            PendingIntent pi = PendingIntent.getActivity
                (ctx, 0, main_intent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.widget_vf, pi);
            mgr.updateAppWidget(id, rv);
        }
    }

    // Called from background service.
    public final static void refreshWidgets(Context ctx)
    {
        AppWidgetManager mgr =
            AppWidgetManager.getInstance(ctx.getApplicationContext());
        int[] ids = mgr.getAppWidgetIds
            (new ComponentName(ctx, CWidgetProvider.class));
        if ((ids != null) && (ids.length > 0)) {
            CStackViewService.refreshData(ctx);
            mgr.notifyAppWidgetViewDataChanged(ids, R.id.widget_vf);
        }
    }

    private final static String TAG = CUtils.makeLogTag(CWidgetProvider.class);
}
