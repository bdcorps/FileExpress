package com.bdcorps.fileexpressfree.callbacks;

import java.util.ArrayList;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.ShareActionProvider;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;

import com.bdcorps.fileexpressfree.R;
import com.bdcorps.fileexpressfree.activity.FileListActivity;
import com.bdcorps.fileexpressfree.adapters.FileListAdapter;
import com.bdcorps.fileexpressfree.model.FileListEntry;
import com.bdcorps.fileexpressfree.util.FileActionsHelper;
import com.bdcorps.fileexpressfree.util.Util;

public abstract class FileActionsCallback implements Callback {

	private FileListActivity activity;
	private ArrayList<FileListEntry> fileArray;
	static int[] allOptions = { R.id.menu_copy, R.id.menu_cut,
			R.id.menu_delete, R.id.menu_props, R.id.menu_share,
			R.id.menu_rename, R.id.menu_zip, R.id.menu_unzip };

	public FileActionsCallback(FileListActivity activity,
			ArrayList<FileListEntry> fileArray) {

		this.activity = activity;
		this.fileArray = fileArray;

	}

	@Override
	public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
		FileActionsHelper.doOperation(((FileListAdapter) activity.getListView()
				.getAdapter()).getFileArray(), item.getItemId(), activity,
				new OperationCallback<Void>() {

					@Override
					public Void onSuccess() {
						return null;
					}

					@Override
					public void onFailure(Throwable e) {

					}
				});
		mode.finish();
		return true;
	}

	@Override
	public boolean onCreateActionMode(final ActionMode actionMode, Menu menu) {
		int[] validOptions = null;
				if (((FileListAdapter) activity.getListView().getAdapter())
				.getSelectedCount() == 1) {
			validOptions = FileActionsHelper.getContextMenuOptions(
					((FileListAdapter) activity.getListView().getAdapter())
							.getFileArray().get(0).getPath(), activity);
		} else if (((FileListAdapter) activity.getListView().getAdapter())
				.getSelectedCount() > 1) {
			for (int i = 0; i < fileArray.size(); i++) {
				if (Util.isSdCard(fileArray.get(i).getPath())) {
					validOptions = new int[] { R.id.menu_props };
					break;
				} else {
					validOptions = new int[] { R.id.menu_copy, R.id.menu_cut,
							R.id.menu_delete, R.id.menu_rename, R.id.menu_props };
				}
			}
		}
		
		if (validOptions == null || validOptions.length == 0) {
			onDestroyActionMode(actionMode);
			return false;
		}
		actionMode.setTitle(activity.getString(R.string.selected_,
				String.valueOf(((FileListAdapter) activity.getListView()
						.getAdapter()).getSelectedCount())));

		MenuInflater inflater = activity.getMenuInflater();

		inflater.inflate(R.menu.context_menu, menu);

		for (int o : allOptions) {
			boolean valid = false;
			for (int v : validOptions) {
				if (o == v) {
					valid = true;
					break;
				}
			}
			if (!valid) {
				menu.removeItem(o);
			} else {
				if (o == R.id.menu_share) {
					MenuItem menuItem = menu.findItem(R.id.menu_share);
					ShareActionProvider mShareActionProvider = (ShareActionProvider) menuItem
							.getActionProvider();
					mShareActionProvider
							.setOnShareTargetSelectedListener(new OnShareTargetSelectedListener() {

								@Override
								public boolean onShareTargetSelected(
										ShareActionProvider source,
										Intent intent) {
									actionMode.finish();
									return false;
								}
							});

					final Intent intent = new Intent(Intent.ACTION_SEND);

					Uri uri = Uri.fromFile((fileArray.get(0)).getPath());
					String type = MimeTypeMap.getSingleton()
							.getMimeTypeFromExtension(
									MimeTypeMap.getFileExtensionFromUrl(uri
											.toString()));
					intent.setType(type);
					intent.setAction(Intent.ACTION_SEND);
					intent.setType(type == null ? "*/*" : type);
					intent.putExtra(Intent.EXTRA_STREAM, uri);

					mShareActionProvider.setShareIntent(intent);
				}
			}
		}
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

}
