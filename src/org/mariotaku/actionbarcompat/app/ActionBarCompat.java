package org.mariotaku.actionbarcompat.app;

import android.app.Activity;
import android.os.Build;
import android.view.Menu;

public abstract class ActionBarCompat implements ActionBar {

	abstract void hideInRealMenu(Menu menu);

	abstract void invalidateOptionsMenu();

	abstract boolean requestCustomTitleView();

	abstract boolean setCustomTitleView();

	public static ActionBarCompat getInstance(Activity activity) {
		if (activity == null) return null;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			return new ActionBarCompatICS(activity);
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			return new ActionBarCompatHoneycomb(activity);
		else
			return new ActionBarCompatBase(activity);
	}

}
