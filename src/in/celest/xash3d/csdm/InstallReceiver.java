package in.celest.xash3d.csdm;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class InstallReceiver extends BroadcastReceiver {
private static final String TAG = "CSDM_LAUNCHER";
@Override
public void onReceive(Context context, Intent arg1) {
	Log.d( TAG, "Install received, extracting PAK" );
    LauncherActivity.extractPAK( context, true );
    }
}
