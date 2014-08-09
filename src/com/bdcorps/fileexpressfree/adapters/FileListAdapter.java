package com.bdcorps.fileexpressfree.adapters;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bdcorps.fileexpressfree.R;
import com.bdcorps.fileexpressfree.activity.FileListActivity;
import com.bdcorps.fileexpressfree.model.FileListEntry;
import com.bdcorps.fileexpressfree.util.Util;

public class FileListAdapter extends BaseAdapter {

	public static class ViewHolder {
		public TextView resName;
		public ImageView resIcon;
		public TextView resMeta;
	}

	private static final String TAG = FileListAdapter.class.getName();
	private SparseBooleanArray mSelectedItemsIds;
	private FileListActivity mContext;
	private List<FileListEntry> files;
	private LayoutInflater mInflater;
	
	ArrayList<FileListEntry> fileArray = new ArrayList<FileListEntry>();

	public FileListAdapter(FileListActivity context, List<FileListEntry> files) {
		super();
		mContext = context;
		this.files = files;
		mInflater = mContext.getLayoutInflater();
		mSelectedItemsIds = new SparseBooleanArray();
	}

	@Override
	public int getCount() {
		if (files == null) {
			return 0;
		} else {
			return files.size();
		}
	}

	  public void selectView(int position, boolean value)
      {
          if(value)
              mSelectedItemsIds.put(position, value);
          else
              mSelectedItemsIds.delete(position);
          
          notifyDataSetChanged();
      }
      
      public int getSelectedCount() {
          return fileArray.size();// mSelectedCount;
      } 

		public void toggleSelection(int position)
      {FileListEntry temp =(FileListEntry) getItem(position);
      if (fileArray.indexOf(temp)==-1){
			fileArray.add(temp);}else {fileArray.remove(fileArray.indexOf(temp));}
			
          selectView(position, !mSelectedItemsIds.get(position));
      }

      public void removeSelection() {
    	  fileArray=new ArrayList<FileListEntry>(); 
          mSelectedItemsIds = new SparseBooleanArray();
          notifyDataSetChanged();
      }
      
	@Override
	public Object getItem(int arg0) {

		if (files == null)
			return null;
		else
			return files.get(arg0);
	}

	public List<FileListEntry> getItems() {
		return files;
	}
	
	public ArrayList<FileListEntry> getFileArray() {
		return fileArray;
	}


	@Override
	public long getItemId(int position) {

		return position;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.explorer_item, parent,
					false);
			holder = new ViewHolder();
			holder.resName = (TextView) convertView
					.findViewById(R.id.explorer_resName);
			holder.resMeta = (TextView) convertView
					.findViewById(R.id.explorer_resMeta);
			holder.resIcon = (ImageView) convertView
					.findViewById(R.id.explorer_resIcon);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final FileListEntry currentFile = files.get(position);
		holder.resName.setText(currentFile.getName());
		holder.resIcon.setImageDrawable(Util.getIcon(mContext,
				currentFile.getPath()));
		String meta = Util.prepareMeta(currentFile, mContext);
		holder.resMeta.setText(meta);

		convertView.setBackgroundColor(mSelectedItemsIds.get(position)? 0x99ff8c00: Color.TRANSPARENT);        	
    	
		return convertView;
	}

}
