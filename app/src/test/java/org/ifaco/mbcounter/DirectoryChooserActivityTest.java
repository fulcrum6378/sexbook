package org.ifaco.mbcounter;

import android.content.Intent;

import org.ifaco.mbcounter.dirchooser.DirectoryChooserActivity;
import org.ifaco.mbcounter.dirchooser.DirectoryChooserConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DirectoryChooserActivityTest {
    private DirectoryChooserActivity activity;
    private Intent launchIntent;

    @Before
    public void setup() {
        activity = Mockito.mock(DirectoryChooserActivity.class);
        // Robolectric doesn't support the ICS ActionBar.
        Mockito.doNothing().when(activity).setupActionBar();

        launchIntent = new Intent();
        Mockito.doReturn(launchIntent).when(activity).getIntent();
    }

    @Test
    public void testSmokeInit() {
        activity.onCreate(null);
    }

    @Test
    public void testSmokeInitWithExtras() {
        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                .newDirectoryName("my dir")
                .build();
        launchIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);
        activity.onCreate(null);
    }
}
