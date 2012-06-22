package org.mariotaku.actionbarcompat;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;

public class ActionBarFragmentActivity extends FragmentActivity {

	private ActionBarCompat mActionBarCompat = ActionBarCompat.getInstance(this);
	private boolean mActionBarInitialized = false;

	@Override
	public MenuInflater getMenuInflater() {
		return mActionBarCompat.getMenuInflater(super.getMenuInflater());
	}
	
	public MenuInflater getBaseMenuInflater() {
		return super.getMenuInflater();
	}

	public ActionBar getSupportActionBar() {
		if (mActionBarCompat instanceof ActionBarCompatBase && !mActionBarInitialized) {
			mActionBarInitialized = ((ActionBarCompatBase)mActionBarCompat).setCustomTitleView();
		}
		return mActionBarCompat.getActionBar();

	}
	
	public void requestSupportWindowFeature(int featureId) {
		if (mActionBarCompat instanceof ActionBarCompatNative) {
			requestWindowFeature(featureId);
		} else {
			switch (featureId) {
				case Window.FEATURE_INDETERMINATE_PROGRESS:{			
					if (mActionBarCompat instanceof ActionBarCompatBase) {
						((ActionBarCompatBase)mActionBarCompat).setProgressBarIndeterminateEnabled(true);
					}				
				}
			}
		}
	}

	public void invalidateSupportOptionsMenu() {
		if (mActionBarCompat instanceof ActionBarCompatNative) {
			MethodsCompat.invalidateOptionsMenu(this);
		} else if (mActionBarCompat instanceof ActionBarCompatBase) {
			((ActionBarCompatBase) mActionBarCompat).invalidateOptionsMenu();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			((ActionBarCompatBase)mActionBarCompat).requestCustomTitleView();
		}
		super.onCreate(savedInstanceState);
	}

	/**
	 * Base action bar-aware implementation for
	 * {@link Activity#onCreateOptionsMenu(android.view.Menu)}.
	 * 
	 * Note: marking menu items as invisible/visible is not currently supported.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean retValue = super.onCreateOptionsMenu(menu);
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			if (mAttachedFragment != null) {
				mAttachedFragment.onCreateOptionsMenu(menu, getMenuInflater());
			}
			retValue = true;
		}
		return retValue;
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		if (mActionBarCompat instanceof ActionBarCompatBase && !mActionBarInitialized) {
			mActionBarInitialized = ((ActionBarCompatBase)mActionBarCompat).setCustomTitleView();
		}
		super.onPostCreate(savedInstanceState);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			((ActionBarCompatBase)mActionBarCompat).hideInRealMenu(menu);
		}
		return true;
	}

	@Override
	public void onTitleChanged(CharSequence title, int color) {
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			getSupportActionBar().setTitle(title);
		}
		super.onTitleChanged(title, color);
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		if (mActionBarCompat instanceof ActionBarCompatBase && !mActionBarInitialized) {
			mActionBarInitialized = ((ActionBarCompatBase)mActionBarCompat).setCustomTitleView();
		}
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		if (mActionBarCompat instanceof ActionBarCompatBase && !mActionBarInitialized) {
			mActionBarInitialized = ((ActionBarCompatBase)mActionBarCompat).setCustomTitleView();
		}
	}

	@Override
	public void setContentView(View view, LayoutParams params) {
		super.setContentView(view, params);
		if (mActionBarCompat instanceof ActionBarCompatBase && !mActionBarInitialized) {
			mActionBarInitialized = ((ActionBarCompatBase)mActionBarCompat).setCustomTitleView();
		}
	}

	public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			((ActionBarCompatBase)mActionBarCompat).setProgressBarIndeterminateVisibility(visible);
		} else {
			setProgressBarIndeterminateVisibility(visible);
		}
	}

	private Fragment mAttachedFragment;
	
	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		mAttachedFragment = fragment;
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			((ActionBarCompatBase) mActionBarCompat).createActionBarMenu();
		}
	}

}
