package org.mariotaku.actionbarcompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mariotaku.internal.menu.MenuImpl;
import org.mariotaku.internal.menu.MenuItemImpl;
import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.InflateException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

class ActionBarCompatBase extends ActionBarCompat implements ActionBar {

	private static final String MENU_RES_NAMESPACE = "http://schemas.android.com/apk/res/android";
	private static final String MENU_ATTR_ID = "id";
	private static final String MENU_ATTR_SHOW_AS_ACTION = "showAsAction";

	private final Activity mActivity;
	private View mActionBarView, mCustomView, mHomeAsUpIndicator;
	private ImageView mIconView;
	private TextView mTitleView, mSubtitleView;
	private ViewGroup mHomeView, mTitleContainer, mCustomViewContainer, mActionMenuView;
	private final Menu mRealMenu, mActionBarMenu;

	private boolean mProgressBarIndeterminateEnabled = false;

	public ActionBarCompatBase(Activity activity) {
		mActivity = activity;
		mActionBarMenu = new MenuImpl(activity);
		mRealMenu = new MenuImpl(mActivity);
	}

	public void createActionBarMenu() {
		mActivity.onCreatePanelMenu(Window.FEATURE_OPTIONS_PANEL, mRealMenu);
		mActivity.onPrepareOptionsMenu(mRealMenu);
	}

	@Override
	public View getCustomView() {
		return mCustomView;
	}

	@Override
	public int getHeight() {
		if (mActionBarView != null) {
			mActionBarView.getHeight();
		}
		return 0;
	}

	/**
	 * Returns a {@link android.view.MenuInflater} that can read action bar
	 * metadata on pre-Honeycomb devices.
	 */
	@Override
	public MenuInflater getMenuInflater(MenuInflater superMenuInflater) {
		return new SupportMenuInflater(mActivity, superMenuInflater);
	}

	@Override
	public CharSequence getSubtitle() {
		if (mSubtitleView == null) return null;
		return mSubtitleView.getText();
	}

	@Override
	public CharSequence getTitle() {
		if (mTitleView == null) return null;
		return mTitleView.getText();
	}

	@Override
	public void setBackgroundDrawable(Drawable d) {
		if (mActionBarView != null) {
			mActionBarView.setBackgroundDrawable(d);
		}
	}

	@Override
	public void setCustomView(int resId) {
		setCustomView(mActivity.getLayoutInflater().inflate(resId, null));
	}

	@Override
	public void setCustomView(View view) {
		if (mCustomViewContainer == null) return;
		mCustomViewContainer.removeAllViews();
		mCustomViewContainer.addView(view);
		mCustomView = view;
	}

	@Override
	public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
		if (mHomeAsUpIndicator == null) return;
		mHomeAsUpIndicator.setVisibility(showHomeAsUp ? View.VISIBLE : View.INVISIBLE);
	}

	@Override
	public void setDisplayShowCustomEnabled(boolean enabled) {
		if (mCustomViewContainer == null) return;
		mCustomViewContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
	}

	@Override
	public void setDisplayShowHomeEnabled(boolean showHome) {
		if (mActionBarView == null) return;
		mHomeView.setVisibility(showHome ? View.VISIBLE : View.GONE);
	}

	@Override
	public void setDisplayShowTitleEnabled(boolean enabled) {
		if (mTitleContainer == null) return;
		mTitleContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
	}

	@Override
	public void setSubtitle(CharSequence subtitle) {
		if (mSubtitleView == null) return;
		mSubtitleView.setText(subtitle);
		mSubtitleView.setVisibility(subtitle != null ? View.VISIBLE : View.GONE);
	}

	@Override
	public void setSubtitle(int resId) {
		setSubtitle(mActivity.getString(resId));
	}

	@Override
	public void setTitle(CharSequence title) {
		if (mTitleView == null) return;
		mTitleView.setText(title);
	}

	@Override
	public void setTitle(int resId) {
		setTitle(mActivity.getString(resId));
	}

	/**
	 * Adds an action button to the compatibility action bar, using menu
	 * information from a {@link android.view.MenuItem}. If the menu item ID is
	 * <code>menu_refresh</code>, the menu item's state can be changed to show a
	 * loading spinner using
	 * {@link com.android.actionbarcompat.ActionBarHelperBase#setRefreshActionItemState(boolean)}
	 * .
	 */
	@SuppressWarnings("deprecation")
	private View addActionItemCompatFromMenuItem(final MenuItem item) {

		if (mActionMenuView == null || item == null) return null;

		// Create the button
		final ImageButton actionButton = new ImageButton(mActivity, null, R.attr.actionBarItemStyle);
		// actionButton.setVisibility(item.isVisible() ? View.VISIBLE :
		// View.GONE);
		actionButton.setLayoutParams(new ViewGroup.LayoutParams((int) mActivity.getResources().getDimension(
				R.dimen.actionbar_button_width), ViewGroup.LayoutParams.FILL_PARENT));
		actionButton.setId(item.getItemId());
		actionButton.setImageDrawable(item.getIcon());
		actionButton.setScaleType(ScaleType.CENTER);
		actionButton.setContentDescription(item.getTitle());

		actionButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (!item.isEnabled()) return;
				if (item.hasSubMenu()) {
					final PopupMenu popup = PopupMenu.getInstance(mActivity, view);
					popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

						@Override
						public boolean onMenuItemClick(MenuItem item) {
							return mActivity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, item);
						}

					});
					popup.setMenu(item.getSubMenu());
					popup.show();
				}
				mActivity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, item);
			}
		});
		actionButton.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (item.getItemId() == android.R.id.home) return false;

				final Toast t = Toast.makeText(mActivity, item.getTitle(), Toast.LENGTH_SHORT);

				final int[] screenPos = new int[2];
				final Rect displayFrame = new Rect();
				v.getLocationOnScreen(screenPos);
				v.getWindowVisibleDisplayFrame(displayFrame);

				final int width = v.getWidth();
				final int height = v.getHeight();
				final int midy = screenPos[1] + height / 2;
				final int screenWidth = mActivity.getResources().getDisplayMetrics().widthPixels;

				if (midy < displayFrame.height()) {
					// Show along the top; follow action buttons
					t.setGravity(Gravity.TOP | Gravity.RIGHT, screenWidth - screenPos[0] - width / 2, height);
				} else {
					// Show along the bottom center
					t.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, height);
				}
				t.show();
				return true;
			}
		});

		mActionMenuView.addView(actionButton);
		return actionButton;
	}

	private void clearMenuButtons() {
		if (mActionMenuView == null) return;
		mActionMenuView.removeAllViews();
	}

	private void setHomeButton() {
		// Add Home button
		final MenuItem homeItem = MenuItemImpl.createItem(mActivity, android.R.id.home);
		final PackageManager pm = mActivity.getPackageManager();
		try {
			if (mIconView != null) {
				mIconView.setImageDrawable(pm.getActivityIcon(mActivity.getComponentName()));
			}
		} catch (final NameNotFoundException e) {
			// This should not happen.
		}
		if (mHomeView != null) {
			mHomeView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					mActivity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, homeItem);
				}
			});
		}
	}

	void hideInRealMenu(Menu menu) {
		if (menu instanceof MenuImpl || menu == null) return;
		for (int i = 0; i < menu.size(); i++) {
			final MenuItem realItem = menu.getItem(i);
			if (realItem == null) {
				continue;
			}
			if (mActionBarMenu.findItem(realItem.getItemId()) != null) {
				realItem.setVisible(false);
			}
		}
	}

	void invalidateOptionsMenu() {
		clearMenuButtons();
		for (final MenuItem item : ((MenuImpl) mActionBarMenu).getMenuItems()) {
			addActionItemCompatFromMenuItem(item);
		}
		mActivity.onPrepareOptionsMenu(new SupportMenu(mActivity));
	}

	boolean requestCustomTitleView() {
		if (mActivity != null) {
			mActivity.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
			return true;
		}
		return false;
	}

	boolean setCustomTitleView() {
		mActivity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.actionbar);
		mActionBarView = mActivity.findViewById(R.id.actionbar);

		if (mActionBarView == null) return false;
		mTitleContainer = (ViewGroup) mActionBarView.findViewById(R.id.actionbar_title_view);
		mTitleView = (TextView) mTitleContainer.findViewById(R.id.actionbar_title);
		mSubtitleView = (TextView) mTitleContainer.findViewById(R.id.actionbar_subtitle);
		mHomeView = (ViewGroup) mActionBarView.findViewById(R.id.actionbar_home);
		mIconView = (ImageView) mHomeView.findViewById(R.id.actionbar_icon);
		mHomeAsUpIndicator = mHomeView.findViewById(R.id.actionbar_home_as_up_indicator);
		mActionMenuView = (ViewGroup) mActionBarView.findViewById(R.id.actionbar_menu_buttons);
		mCustomViewContainer = (ViewGroup) mActionBarView.findViewById(R.id.actionbar_custom_view_container);

		setTitle(mActivity.getTitle());

		// Add Home button
		setHomeButton();

		createActionBarMenu();
		return true;
	}

	void setProgressBarIndeterminateEnabled(boolean enabled) {
		mProgressBarIndeterminateEnabled = enabled;

	}

	void setProgressBarIndeterminateVisibility(boolean visible) {
		if (mProgressBarIndeterminateEnabled) {
			mActionBarView.findViewById(R.id.actionbar_progress_indeterminate).setVisibility(
					visible ? View.VISIBLE : View.GONE);
		}

	}

	private class SupportMenu extends MenuImpl {

		private final Context context;

		public SupportMenu(Context context) {
			super(context);
			this.context = context;
		}

		@Override
		public MenuItem findItem(int id) {
			for (final MenuItem item : ((MenuImpl) mRealMenu).getMenuItems()) {
				if (item.getItemId() == id) return new SupportMenuItem(context, id);
			}
			return null;
		}

	}

	/**
	 * A {@link android.view.MenuInflater} that reads action bar metadata.
	 */
	private class SupportMenuInflater extends MenuInflater {

		final MenuInflater mInflater;

		public SupportMenuInflater(Context context, MenuInflater inflater) {
			super(context);
			mInflater = inflater;
		}

		@Override
		public void inflate(int menuRes, Menu menu) {
			mInflater.inflate(menuRes, menu);
			// if (!(menu instanceof MenuImpl)) {
			mActionBarMenu.clear();
			mInflater.inflate(menuRes, mActionBarMenu);
			removeDuplicateMenuInActionBar(menu, menuRes);
			// }

		}

		/**
		 * Loads action bar metadata from a menu resource, storing a list of
		 * menu item IDs that should be shown on-screen (i.e. those with
		 * showAsAction set to always or ifRoom).
		 * 
		 * @param menuResId
		 */
		private void removeDuplicateMenuInActionBar(Menu menu, int menuResId) {
			XmlResourceParser parser = null;
			try {
				parser = mActivity.getResources().getXml(menuResId);
				int eventType = parser.getEventType();
				int itemId;
				int showAsAction;

				boolean eof = false;
				while (!eof) {
					switch (eventType) {
						case XmlPullParser.START_TAG:
							if (!parser.getName().equals("item")) {
								break;
							}

							itemId = parser.getAttributeResourceValue(MENU_RES_NAMESPACE, MENU_ATTR_ID, 0);
							if (itemId == 0) {
								break;
							}

							showAsAction = parser
									.getAttributeIntValue(MENU_RES_NAMESPACE, MENU_ATTR_SHOW_AS_ACTION, -1);

							final MenuItem item = menu.findItem(itemId);
							if (item == null) {
								break;
							}
							final boolean isShowAsAction = showAsAction == MenuItem.SHOW_AS_ACTION_ALWAYS
									|| showAsAction == MenuItem.SHOW_AS_ACTION_IF_ROOM
									|| showAsAction == MenuItem.SHOW_AS_ACTION_WITH_TEXT;
							if (!isShowAsAction && mActionBarMenu instanceof MenuImpl) {
								final List<MenuItem> items = ((MenuImpl) mActionBarMenu).getMenuItems();
								final List<MenuItem> items_to_remove = new ArrayList<MenuItem>();
								for (final MenuItem actionItem : items) {
									if (item.getItemId() == actionItem.getItemId()) {
										items_to_remove.add(actionItem);
									}
								}
								items.removeAll(items_to_remove);
							}
							item.setVisible(!isShowAsAction);
							break;

						case XmlPullParser.END_DOCUMENT:
							eof = true;
							break;
					}

					eventType = parser.next();
				}
			} catch (final XmlPullParserException e) {
				throw new InflateException("Error inflating menu XML", e);
			} catch (final IOException e) {
				throw new InflateException("Error inflating menu XML", e);
			} finally {
				if (parser != null) {
					parser.close();
				}
			}
			invalidateOptionsMenu();
		}
	}

	private class SupportMenuItem extends MenuItemImpl {

		private final int itemId;

		public SupportMenuItem(Context context, int itemId) {
			super(context);
			this.itemId = itemId;
		}

		@Override
		public MenuItem setEnabled(boolean enabled) {
			if (mActionBarMenu == null || mRealMenu == null) return this;
			final MenuItem item = mActionBarMenu.findItem(itemId);
			final MenuItem realItem = mRealMenu.findItem(itemId);
			if (item != null) {
				item.setEnabled(enabled);
				final View view = mActionMenuView.findViewById(itemId);
				if (view != null) {
					view.setEnabled(enabled);
				}
			}
			if (realItem != null) {
				realItem.setEnabled(enabled);
			}
			return this;
		}

		@Override
		public MenuItem setIcon(int iconRes) {
			if (mActionBarMenu == null || mRealMenu == null) return this;
			final MenuItem item = mActionBarMenu.findItem(itemId);
			final MenuItem realItem = mRealMenu.findItem(itemId);
			if (item != null) {
				item.setIcon(iconRes);
			}
			if (realItem != null) {
				realItem.setIcon(iconRes);
			}
			return this;
		}

		@Override
		public MenuItem setTitle(int titleRes) {
			if (mActionBarMenu == null || mRealMenu == null) return this;
			final MenuItem item = mActionBarMenu.findItem(itemId);
			final MenuItem realItem = mRealMenu.findItem(itemId);
			if (item != null) {
				item.setTitle(titleRes);
			}
			if (realItem != null) {
				realItem.setTitle(titleRes);
			}
			return this;
		}

		@Override
		public MenuItem setVisible(boolean visible) {
			if (mActionBarMenu == null || mRealMenu == null) return this;
			final MenuItem item = mActionBarMenu.findItem(itemId);
			final MenuItem realItem = mRealMenu.findItem(itemId);
			if (item != null) {
				item.setVisible(visible);
				final View view = mActionMenuView.findViewById(itemId);
				if (view != null) {
					view.setVisibility(visible ? View.VISIBLE : View.GONE);
				}
				if (realItem != null) {
					realItem.setVisible(false);
				}
				return this;
			}
			if (realItem != null) {
				realItem.setVisible(visible);
			}
			return this;
		}

	}
}
