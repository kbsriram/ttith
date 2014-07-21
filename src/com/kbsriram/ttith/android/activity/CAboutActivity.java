package com.kbsriram.ttith.android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.kbsriram.ttith.android.R;

public class CAboutActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        TextView tv = (TextView) findViewById(R.id.about_version_tv);
        String vcode;
        try {
            vcode = getPackageManager()
                .getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (Throwable ign) {
            vcode = "missing";
        }
        tv.setText
            (String.format
             (getResources().getString(R.string.about_version), vcode));
        // workaround bug in setting links from textview.
        ViewGroup vg = (ViewGroup) findViewById(R.id.about_content_vg);
        final int cc = vg.getChildCount();
        final MovementMethod lmm = LinkMovementMethod.getInstance();

        for (int i = 0; i < cc; i++) {
            View child = vg.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView)child).setMovementMethod(lmm);
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
