package in.celest.xash3d.csdm;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import android.content.Context;
import android.util.Log;
import in.celest.xash3d.csdm.R;

public class LauncherActivity extends Activity {
	private static final int PAK_VERSION = 1;
	private static final String TAG = "CSDM_LAUNCHER";
	static EditText cmdArgs;
	static SharedPreferences mPref = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        if( mPref == null )
			mPref = getSharedPreferences("mod", 0);
		cmdArgs = (EditText)findViewById(R.id.cmdArgs);
		cmdArgs.setText(mPref.getString("argv","-dev 3 -log"));
		extractPAK(this, false);
	}

    public void startXash(View view)
    {
		Intent intent = new Intent();
		intent.setAction("in.celest.xash3d.START");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		SharedPreferences.Editor editor = mPref.edit();
		editor.putString("argv", cmdArgs.getText().toString());
		editor.commit();
		editor.apply();
		if(cmdArgs.length() != 0) intent.putExtra("argv", cmdArgs.getText().toString());
		intent.putExtra("gamedir", "csdm");
		intent.putExtra("gamelibdir", getFilesDir().getAbsolutePath().replace("/files","/lib"));
		intent.putExtra("pakfile", getFilesDir().getAbsolutePath() + "/extras.pak" );
		startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_launcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
		 return true;
		 }*/

        return super.onOptionsItemSelected(item);
    }

	private static int chmod(String path, int mode) throws Exception {
		Class fileUtils = Class.forName("android.os.FileUtils");
		Method setPermissions = fileUtils.getMethod("setPermissions",
				String.class, int.class, int.class, int.class);
		return (Integer) setPermissions.invoke(null, path,
				mode, -1, -1);
	}


	public static void extractPAK(Context context, Boolean force) {
		InputStream is = null;
		FileOutputStream os = null;
		try {
		if( mPref == null )
			mPref = context.getSharedPreferences("mod", 0);
		if( mPref.getInt( "pakversion", 0 ) == PAK_VERSION && !force )
			return;
			String path = context.getFilesDir().getPath()+"/extras.pak";
		
			is = context.getAssets().open("extras.pak");
			os = new FileOutputStream(path);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
			os.close();
			is.close();
			SharedPreferences.Editor editor = mPref.edit();
			editor.putInt( "pakversion", PAK_VERSION );
			editor.commit();
			editor.apply();
			chmod( path, 0644 );
		} catch( Exception e )
		{
			Log.e( TAG, "Failed to extract PAK:" + e.toString() );
		}
	}
}
