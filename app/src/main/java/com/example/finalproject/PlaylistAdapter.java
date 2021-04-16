package com.example.finalproject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

public class PlaylistAdapter extends ArrayAdapter<Music> {
    Context context;
    int layoutResourceId;
    ArrayList<Music> data=new ArrayList<Music>();
    ArrayList<Music> filterList=new ArrayList<Music>();
    CustomFilter filter;

    public PlaylistAdapter(Context context, int layoutResourceId, ArrayList<Music> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.filterList = data;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }

    @Override
    public Music getItem(int pos) {
        // TODO Auto-generated method stub
        return data.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        // TODO Auto-generated method stub
        return data.indexOf(getItem(pos));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SongHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new SongHolder();
            holder.listTitle = (TextView)row.findViewById(R.id.listTitle);
            holder.listArtist = (TextView)row.findViewById(R.id.listArtist);
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            row.setTag(holder);
        }
        else
        {
            holder = (SongHolder)row.getTag();
        }

        Music mySong = data.get(position);
        holder.listTitle.setText(mySong.getSongTitles());
        holder.listArtist.setText(mySong.getSongArtist());
        new PlaylistAdapter.DownLoadImageTask(holder.imgIcon).execute(mySong.getImgUrl());
        return row;

    }

    static class SongHolder
    {
        ImageView imgIcon;
        TextView listTitle;
        TextView listArtist;
    }

    private class DownLoadImageTask extends AsyncTask<String,Void, Bitmap> {
        ImageView imageView;

        public DownLoadImageTask(ImageView imageView){
            this.imageView = imageView;
        }

        /*
            doInBackground(Params... params)
                Override this method to perform a computation on a background thread.
         */
        protected Bitmap doInBackground(String...urls){
            String urlOfImage = urls[0];
            Bitmap logo = null;
            try{
                InputStream is = new URL(urlOfImage).openStream();
                /*
                    decodeStream(InputStream is)
                        Decode an input stream into a bitmap.
                 */
                logo = BitmapFactory.decodeStream(is);
            }catch(Exception e){ // Catch the download exception
                e.printStackTrace();
            }
            return logo;
        }

        /*
            onPostExecute(Result result)
                Runs on the UI thread after doInBackground(Params...).
         */
        protected void onPostExecute(Bitmap result){
            imageView.setImageBitmap(result);
        }
    }

    @Override
    public Filter getFilter() {
        // TODO Auto-generated method stub
        if(filter == null)
        {
            filter=new CustomFilter();
        }

        return filter;
    }

    //INNER CLASS
    class CustomFilter extends Filter
    {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            // TODO Auto-generated method stub
            FilterResults results=new FilterResults();

            if(constraint != null && constraint.length()>0)
            {
                //CONSTARINT TO UPPER
                constraint=constraint.toString().toUpperCase();

                ArrayList<Music> filters=new ArrayList<Music>();

                //get specific items
                for(int i=0;i<filterList.size();i++)
                {
                    if(filterList.get(i).getSongTitles().toUpperCase().contains(constraint))
                    {
                        Music p=new Music(filterList.get(i).getIndex_(),filterList.get(i).getSongTitles(), filterList.get(i).getSongArtist(),filterList.get(i).getSongUrl(),filterList.get(i).getImgUrl());
                        filters.add(p);
                    }
                }

                results.count=filters.size();
                results.values=filters;

            }else
            {
                results.count=filterList.size();
                results.values=filterList;

            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // TODO Auto-generated method stub

            data=(ArrayList<Music>) results.values;
            notifyDataSetChanged();
        }
    }
}
