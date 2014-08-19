package com.bdcorps.fileexpressfree.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Instrumentation.ActivityResult;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bdcorps.fileexpressfree.CustomToast;
import com.bdcorps.fileexpressfree.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bdcorps.fileexpressfree.activity.FileListActivity;
import com.bdcorps.fileexpressfree.adapters.FileListAdapter;
import com.bdcorps.fileexpressfree.callbacks.CancellationCallback;
import com.bdcorps.fileexpressfree.callbacks.OperationCallback;
import com.bdcorps.fileexpressfree.model.FileListEntry;
import com.bdcorps.fileexpressfree.workers.Trasher;
import com.bdcorps.fileexpressfree.workers.Unzipper;
import com.bdcorps.fileexpressfree.workers.Zipper;

public class FileActionsHelper {

	protected static final String TAG = FileActionsHelper.class.getName();
	public static int fileCount;

	public static void copyFile(ArrayList<File> filePaths2,
			FileListActivity mContext) {
		getCount(mContext);

		Util.setPasteSrcFile(filePaths2, Util.PASTE_MODE_COPY);

		if (fileCount == 1) {

CustomToast.ToastMe(
		mContext.getString(R.string.copied_toast, filePaths2.get(0)
				.getName()), mContext);

		} else {

CustomToast.ToastMe(mContext.getString(R.string.copied_multi_toast,
		String.valueOf(fileCount)), mContext);
		}
		mContext.invalidateOptionsMenu();
	}

	private static void getCount(FileListActivity mContext) {
		fileCount = ((FileListAdapter) mContext.getListView().getAdapter())
				.getSelectedCount();
	}

	public static void cutFile(ArrayList<File> filePaths2,
			final FileListActivity mContext) {
		getCount(mContext);
		Util.setPasteSrcFile(filePaths2, Util.PASTE_MODE_MOVE);
		if (fileCount == 1) {

CustomToast.ToastMe(mContext.getString(R.string.cut_toast, filePaths2.get(0)
		.getName()), mContext);

		} else {
			

CustomToast.ToastMe(mContext.getString(R.string.cut_multi_toast,
		String.valueOf(fileCount)), mContext);
		}
		mContext.invalidateOptionsMenu();
	}

	public static void showProperties(final FileListEntry file,
			final FileListActivity mContext) {
		if (mContext != null)
			new Builder(mContext)
					.setTitle(
							mContext.getString(R.string.properties_for,
									file.getName()))
					.setItems(Util.getFileProperties(file, mContext),
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							})
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									dialog.dismiss();
								}
							}).show();
	}

	public static void deleteFile(final ArrayList<FileListEntry> fileArray,
			final FileListActivity mContext,
			final OperationCallback<Void> callback) {
		getCount(mContext);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setCancelable(true);
		if (fileCount == 1) {
			builder.setMessage(mContext.getString(R.string.confirm_delete,
					fileArray.get(0).getPath()));
		} else {
			builder.setMessage(mContext.getString(
					R.string.confirm_multi_delete, String.valueOf(fileCount)));
		}
		builder.setCancelable(false)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								for (int i = 0; i < fileArray.size(); i++) {
									new Trasher(mContext, callback)
											.execute(fileArray.get(i).getPath());
								}
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();

							}
						}).setTitle(R.string.confirm);

		AlertDialog confirm = builder.create();
		if (mContext != null) {
			confirm.show();
		}
	}

	public static int[] getContextMenuOptions(File file, FileListActivity caller) {

		PreferenceHelper prefs = new PreferenceHelper(caller);

		if (Util.isProtected(file)) {
			return new int[] {};
		}
		if (Util.isSdCard(file)) {
			return new int[] { R.id.menu_props };

		} else if (file.isDirectory()) {
			if (prefs.isZipEnabled()) {
				return new int[] { R.id.menu_copy, R.id.menu_cut,
						R.id.menu_delete, R.id.menu_rename, R.id.menu_zip,
						R.id.menu_props };
			}
			return new int[] { R.id.menu_copy, R.id.menu_cut, R.id.menu_delete,
					R.id.menu_rename, R.id.menu_props };

		} else if (Util.isUnzippable(file)) {
			if (prefs.isZipEnabled()) {
				return new int[] { R.id.menu_copy, R.id.menu_cut,
						R.id.menu_delete, R.id.menu_rename, R.id.menu_zip,
						R.id.menu_unzip, R.id.menu_props };
			}
			return new int[] { R.id.menu_copy, R.id.menu_cut, R.id.menu_delete,
					R.id.menu_rename, R.id.menu_props };

		} else {
			if (prefs.isZipEnabled()) {
				return new int[] { R.id.menu_share, R.id.menu_copy,
						R.id.menu_cut, R.id.menu_delete, R.id.menu_rename,
						R.id.menu_zip, R.id.menu_props };
			}
			return new int[] { R.id.menu_share, R.id.menu_copy, R.id.menu_cut,
					R.id.menu_delete, R.id.menu_rename, R.id.menu_props };
		}
	}

	public static void rename(final File file, final FileListActivity mContext,
			final OperationCallback<Void> callback) {
		final EditText input = new EditText(mContext);
		input.setHint(mContext.getString(R.string.enter_new_name));
		input.setSingleLine();

		if (mContext != null)
			new Builder(mContext)
					.setTitle(
							mContext.getString(R.string.rename_dialog_title,
									file.getName()))
					.setView(input)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									CharSequence newName = input.getText();
									try {
										File parentFolder = file
												.getParentFile();
										if (file.renameTo(new File(
												parentFolder, newName
														.toString()))) {
											if (callback != null) {
												callback.onSuccess();
											}
											
											CustomToast.ToastMe(
													mContext.getString(
															R.string.rename_toast,
															file.getName(),
															newName), mContext);
													
											mContext.refresh();
										} else {
											if (callback != null) {
												callback.onFailure(new Exception());
											}
											if (mContext != null)
												new Builder(mContext)
														.setTitle(
																mContext.getString(R.string.error))
														.setMessage(
																mContext.getString(
																		R.string.rename_failed,
																		file.getName()))
														.show();
										}

									} catch (Exception e) {
										if (callback != null) {
											callback.onFailure(e);
										}

										Log.e(TAG,
												"Error occured while renaming path",
												e);

										if (mContext != null)
											new Builder(mContext)
													.setIcon(
															android.R.drawable.ic_dialog_alert)
													.setTitle(
															mContext.getString(R.string.error))
													.setMessage(
															mContext.getString(
																	R.string.rename_failed,
																	file.getName()))
													.show();
									}
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									dialog.dismiss();
								}
							}).show();
	}

	public static void zip(final File file, final FileListActivity mContext) {
		try {
			final File zipLoc = new PreferenceHelper(mContext)
					.getZipDestinationDir();

			if (zipLoc == null) {
				final EditText zipDestinationInput = new EditText(mContext);

				zipDestinationInput.setSingleLine();
				new Builder(mContext)
						.setTitle(
								mContext.getString(R.string.unzip_destination))
						.setView(zipDestinationInput)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {

										CharSequence destinationPath = zipDestinationInput
												.getText();
										try {
											File destination = new File(
													destinationPath.toString());
											if (destination.isFile()
													&& destination.exists()) {
												throw new FileNotFoundException();
											} else {
												promptZipFileName(file,
														mContext, destination);
											}

										} catch (Exception e) {
											Log.e(TAG, "Error zipping to path"
													+ destinationPath, e);
											new Builder(mContext)
													.setTitle(
															mContext.getString(R.string.error))
													.setMessage(
															mContext.getString(R.string.zip_failed))
													.show();
										}
									}

								})
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {

										dialog.dismiss();
									}
								}).show();
				zipDestinationInput.setText(mContext.getCurrentDir()
						.getAbsolutePath());
			} else {
				promptZipFileName(file, mContext, zipLoc);
			}

		} catch (Exception e) {
			Log.e(TAG, "Zip destination was invalid", e);
			mContext.runOnUiThread(new Runnable() {

				@Override
				public void run() {

					new Builder(mContext)
							.setTitle(mContext.getString(R.string.error))
							.setMessage(
									mContext.getString(R.string.zip_dest_invalid))
							.setPositiveButton(android.R.string.ok,
									new OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

											dialog.dismiss();
										}
									}).show();
				}
			});
		}
	}

	private static void promptZipFileName(final File file,
			final FileListActivity mContext, final File zipLoc) {
		final EditText input = new EditText(mContext);
		input.setHint(mContext.getString(R.string.enter_zip_file_name));
		input.setSingleLine();
		new Builder(mContext)
				.setTitle(
						mContext.getString(R.string.zip_dialog,
								zipLoc.getAbsolutePath()))
				.setView(input)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								String zipName = input.getText().toString();
								new Zipper(zipName, zipLoc, mContext)
										.execute(file);
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								dialog.dismiss();
							}
						}).show();
	}

	public static void unzip(final FileListActivity mContext,
			final List<File> zipFiles, final CancellationCallback callback) {
		final EditText input = new EditText(mContext);

		input.setSingleLine();
		new Builder(mContext)
				.setTitle(mContext.getString(R.string.unzip_destination))
				.setView(input)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								CharSequence destinationPath = input.getText();
								try {
									File destination = new File(destinationPath
											.toString());
									if (destination.isFile()
											&& destination.exists()) {
										throw new FileNotFoundException();
									} else {
										new Unzipper(mContext, destination)
												.execute((File[]) (new ArrayList<File>(
														zipFiles)
														.toArray(new File[0])));
									}

								} catch (Exception e) {
									Log.e(TAG, "Error unzipping to path"
											+ destinationPath, e);
									new Builder(mContext)
											.setTitle(
													mContext.getString(R.string.error))
											.setMessage(
													mContext.getString(R.string.unzip_failed_dest_file))
											.show();
								}
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								dialog.dismiss();
								if (callback != null)
									callback.onCancel();
							}
						}).show();
		input.setText(mContext.getCurrentDir().getAbsolutePath());
	}

	public static void doOperation(ArrayList<FileListEntry> fileArray,
			int action, FileListActivity mContext,
			OperationCallback<Void> callback) {
		ArrayList<File> filePaths = new ArrayList<File>();
		for (int i = 0; i < fileArray.size(); i++) {
			filePaths.add(fileArray.get(i).getPath());
		}

		FileListEntry entry = fileArray.get(0);

		File file = entry.getPath();
		switch (action) {

		case R.id.menu_cancel:
			ActivityResult result = new ActivityResult(
					Activity.RESULT_CANCELED, null);
			mContext.finish();
			break;

		case R.id.menu_copy:
			copyFile(filePaths, mContext);
			break;

		case R.id.menu_cut:
			cutFile(filePaths, mContext);
			break;

		case R.id.menu_delete:
			deleteFile(fileArray, mContext, callback);
			break;

		case R.id.menu_share:
			share(file, mContext);
			break;

		case R.id.menu_rename:
			rename(file, mContext, callback);
			break;

		case R.id.menu_zip:
			zip(file, mContext);
			break;

		case R.id.menu_unzip:
			List<File> zipFiles = new ArrayList<File>();
			zipFiles.add(file);
			unzip(mContext, zipFiles, null);
			break;

		case R.id.menu_props:
			showProperties(entry, mContext);
			break;
		default:
			break;
		}

	}

	@SuppressLint("NewApi")
	public static void rescanMedia(Activity mContext) {

		mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
				.parse("file://" + Environment.getExternalStorageDirectory())));

CustomToast.ToastMe(mContext.getString(R.string.media_rescan_started,
		String.valueOf(fileCount)), mContext);
		
		Notification noti = new Notification.Builder(mContext)
				.setContentTitle(
						mContext.getString(R.string.media_rescan_started))
				.setContentText(
						mContext.getString(R.string.media_rescan_started_desc))
				.setSmallIcon(R.drawable.ic_notif_sdcard_rescan)
				.setAutoCancel(true).build();

		NotificationManager notificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);

		// Hide the notification after its selected
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, noti);

	}

	public static void share(File file, Context mContext) {
		final Intent intent = new Intent(Intent.ACTION_SEND);

		Uri uri = Uri.fromFile(file);
		String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
		intent.setType(type);
		intent.setAction(Intent.ACTION_SEND);
		intent.setType(type == null ? "*/*" : type);
		intent.putExtra(Intent.EXTRA_STREAM, uri);

		mContext.startActivity(Intent.createChooser(intent,
				mContext.getString(R.string.share_via)));

	}}
