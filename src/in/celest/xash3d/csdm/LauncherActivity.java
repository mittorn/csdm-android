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
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import android.content.Context;
import android.util.Log;
import in.celest.xash3d.csdm.R;

public class LauncherActivity extends Activity {
	private static final int PAK_VERSION = 1;
	private static final String TAG = "CSDM_LAUNCHER";
	static EditText cmdArgs;
	static CheckBox bots;
	static SharedPreferences mPref = null;
	static String botargs;
	static Boolean isExtracting = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        if( mPref == null )
			mPref = getSharedPreferences("mod", 0);
		cmdArgs = (EditText)findViewById(R.id.cmdArgs);
		cmdArgs.setText(mPref.getString("argv","-dev 3 -log"));
		bots = (CheckBox)findViewById(R.id.bots);
		bots.setChecked( mPref.getBoolean( "bots", false ) );
		botargs = " -dll " + getFilesDir().getAbsolutePath().replace("/files","/lib/libyapb.so");
		extractPAK(this, false);
	}

    public void startXash(View view)
    {
		Intent intent = new Intent();
		intent.setAction("in.celest.xash3d.START");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		String args = cmdArgs.getText().toString();
		
		SharedPreferences.Editor editor = mPref.edit();
		editor.putString("argv", args);
		editor.putBoolean("bots", bots.isChecked() );
		editor.commit();
		editor.apply();
		if( bots.isChecked()) args += botargs;
		if(args != "") intent.putExtra("argv", args);
		intent.putExtra("gamedir", "csdm");
		intent.putExtra("gamelibdir", getFilesDir().getAbsolutePath().replace("/files","/lib"));
		intent.putExtra("pakfile", getFilesDir().getAbsolutePath() + "/extras.pak" );
		/* //Example: you can set custom env.
		String[] env = {
			"XASH3D_EXTRAS_PAK1", "",
			"XASH3D_EXTRAS_PAK2", ""
		};
		intent.putExtra("env", env );*/
		startActivity(intent);
    }
   
	public void createShortcut(View view)
	{
		String args = cmdArgs.getText().toString();
		if( bots.isChecked()) args += botargs;
		Intent intent = new Intent();
		intent.setAction("in.celest.xash3d.SHORTCUT");
		intent.putExtra( "name", "CSDM" );
		intent.putExtra("gamedir", "csdm");
		intent.putExtra( "argv", args );
		intent.putExtra( "pkgname", getPackageName() );
		/* //Example: you can set custom env.
		String[] env = {
			"XASH3D_EXTRAS_PAK1", "",
			"XASH3D_EXTRAS_PAK2", ""
		};
		intent.putExtra("env", env );*/
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

	private static int chmod(String path, int mode) {
		int ret = -1;
		try
		{
		ret = Runtime.getRuntime().exec("chmod " + Integer.toOctalString(mode) + " " + path).waitFor();
			Log.d(TAG, "chmod " + Integer.toOctalString(mode) + " " + path + ": " + ret );
		}
		catch(Exception e) 
		{
			ret = -1;
			Log.d(TAG, "chmod: Runtime not worked: " + e.toString() );
		}
		try
		{
		Class fileUtils = Class.forName("android.os.FileUtils");
		Method setPermissions = fileUtils.getMethod("setPermissions",
				String.class, int.class, int.class, int.class);
		ret = (Integer) setPermissions.invoke(null, path,
				mode, -1, -1);
		}
		catch(Exception e) 
		{
			ret = -1;
			Log.d(TAG, "chmod: FileUtils not worked: " + e.toString() );
		}
		return ret;
	}

	private static void extractFile(Context context, String path) {
			try
			{
				InputStream is = null;
				FileOutputStream os = null;
				is = context.getAssets().open(path);
				File out = new File(context.getFilesDir().getPath()+'/'+path);
				out.getParentFile().mkdirs();
				chmod( out.getParent(), 0777 );
				os = new FileOutputStream(out);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
				os.close();
				is.close();
				chmod( context.getFilesDir().getPath()+'/'+path, 0777 );
			} catch( Exception e )
		{
			Log.e( TAG, "Failed to extract file:" + e.toString() );
			e.printStackTrace();
		}
			
	}
	public static void extractPAK(Context context, Boolean force) {
		if(isExtracting)
			return;
		isExtracting = true;
		try {
		if( mPref == null )
			mPref = context.getSharedPreferences("mod", 0);
		if( mPref.getInt( "pakversion", 0 ) == PAK_VERSION && !force )
			return;
			extractFile(context, "extras.pak");
			extractFile(context, "csdm/addons/yapb/conf/lang/de_lang.cfg");
			extractFile(context, "csdm/addons/yapb/conf/lang/ru_chat.cfg");
			extractFile(context, "csdm/addons/yapb/conf/lang/ru_names.cfg");
			extractFile(context, "csdm/addons/yapb/conf/lang/en_names.cfg");
			extractFile(context, "csdm/addons/yapb/conf/lang/en_chat.cfg");
			extractFile(context, "csdm/addons/yapb/conf/lang/de_chat.cfg");
			extractFile(context, "csdm/addons/yapb/conf/lang/ru_lang.cfg");
			extractFile(context, "csdm/addons/yapb/conf/lang/chs_lang.cfg");
			extractFile(context, "csdm/addons/yapb/conf/general.cfg");
			extractFile(context, "csdm/addons/yapb/conf/chatter.cfg");
			chmod( context.getFilesDir().getPath()+"/csdm", 0777 );
			chmod( context.getFilesDir().getPath()+"/csdm/addons", 0777 );
			chmod( context.getFilesDir().getPath()+"/csdm/addons/yapb", 0777 );

			SharedPreferences.Editor editor = mPref.edit();
			editor.putInt( "pakversion", PAK_VERSION );
			editor.commit();
			editor.apply();
		} catch( Exception e )
		{
			Log.e( TAG, "Failed to extract PAK:" + e.toString() );
		}
		isExtracting = false;
	}
}
