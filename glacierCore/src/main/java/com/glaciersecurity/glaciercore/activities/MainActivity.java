/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.glaciersecurity.glaciercore.activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.support.v4n.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.glaciersecurity.glaciercore.R;
import com.glaciersecurity.glaciercore.fragments.AboutFragment;
import com.glaciersecurity.glaciercore.fragments.FaqFragment;
import com.glaciersecurity.glaciercore.fragments.GeneralSettings;
import com.glaciersecurity.glaciercore.fragments.GraphFragment;
import com.glaciersecurity.glaciercore.fragments.LogFragment;
import com.glaciersecurity.glaciercore.fragments.SendDumpFragment;
import com.glaciersecurity.glaciercore.fragments.VPNProfileList;
import com.glaciersecurity.glaciercore.views.ScreenSlidePagerAdapter;
import com.glaciersecurity.glaciercore.views.SlidingTabLayout;
import com.glaciersecurity.glaciercore.views.TabBarView;


public class MainActivity extends BaseActivity {

    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;
    private SlidingTabLayout mSlidingTabLayout;
    private TabBarView mTabs;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager(), this);

        /* Toolbar and slider should have the same elevation */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            disableToolbarElevation();
        }


        mPagerAdapter.addTab(R.string.vpn_list_title, VPNProfileList.class);

        // GOOBER - removed GRAPH tab
        // mPagerAdapter.addTab(R.string.graph, GraphFragment.class);

        mPagerAdapter.addTab(R.string.generalsettings, GeneralSettings.class);

        // GOOBER - removed FAQ tab
        // mPagerAdapter.addTab(R.string.faq, FaqFragment.class);

        if (SendDumpFragment.getLastestDump(this) != null) {
            mPagerAdapter.addTab(R.string.crashdump, SendDumpFragment.class);
        }


        if (isDirectToTV())
            mPagerAdapter.addTab(R.string.openvpn_log, LogFragment.class);

        mPagerAdapter.addTab(R.string.about, AboutFragment.class);
        mPager.setAdapter(mPagerAdapter);

        mTabs = (TabBarView) findViewById(R.id.sliding_tabs);
        mTabs.setViewPager(mPager);
    }

    private static final String FEATURE_TELEVISION = "android.hardware.type.television";
    private static final String FEATURE_LEANBACK = "android.software.leanback";

    private boolean isDirectToTV() {
        return(getPackageManager().hasSystemFeature(FEATURE_TELEVISION)
                || getPackageManager().hasSystemFeature(FEATURE_LEANBACK));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void disableToolbarElevation() {
        ActionBar toolbar = getActionBar();
        toolbar.setElevation(0);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent()!=null) {
            String page = getIntent().getStringExtra("PAGE");
            if ("graph".equals(page)) {
                mPager.setCurrentItem(1);
            }
            setIntent(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // GOOBER - Remove log info from action bar
        // getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* GOOBER - removed ability to show log
        if (item.getItemId()==R.id.show_log){
            Intent showLog = new Intent(this, LogWindow.class);
            startActivity(showLog);
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		System.out.println(data);


	}


}
