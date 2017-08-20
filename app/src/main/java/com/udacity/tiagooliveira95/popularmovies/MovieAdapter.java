package com.udacity.tiagooliveira95.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 15/01/2017.
 */

class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private OnItemClickListener onItemClickListener;
    private ArrayList<Movie> movies;
    private Context context;
    private final float RATIO = 1.5f;


    interface OnItemClickListener{
        void onClick(View v, int position);
    }

    MovieAdapter(Context context,OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        this.context = context;
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.img_cover_d) ImageView poster;
        @BindView(R.id.posterNoImageMainActivity) TextView noPosterError;
        @BindView(R.id.imgContainer) RelativeLayout container;

        MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            container.setOnClickListener(this);


            /** The poster as a ratio of 1 : 1.497
             * so here i will get the display width and set the height of the poster to match the right ratio
             *
             * I think this is a faster way to get the width of the poster,
             * the second way i know to messure the view is with ViewTreeObserver
             * but i have to wait for the callback i think this is a faster way to achive the same result
             * we just need to get the screen witdh and divide by the spancount to get the poster width
             * to set the corrent ratio we multiply the width by the ratio and set the new height to the poster view
             */
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int posterWidth = displayMetrics.widthPixels / MainActivity.calculateNoOfColumns(context);
            float posterHeight = posterWidth * RATIO;
            container.getLayoutParams().height = Math.round(posterHeight);
        }

        @Override
        public void onClick(View view) {
            if(onItemClickListener != null)
                onItemClickListener.onClick(poster,getAdapterPosition());
        }
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MovieViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_element,parent,false));

    }

    @Override
    public void onBindViewHolder(final MovieViewHolder holder, int position) {
        if(movies != null) {
            Movie movie = movies.get(position);
            Picasso.with(context).load(movie.getPosterURL()).into(holder.poster,new Callback() {
                @Override
                public void onSuccess() {
                    holder.noPosterError.setVisibility(View.INVISIBLE);
                    holder.poster.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError() {
                    holder.noPosterError.setVisibility(View.VISIBLE);
                    holder.poster.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if(movies != null)
            return movies.size();
        else
            return 0;
    }

    void setMovies(ArrayList<Movie> movies){
        this.movies = movies;
        notifyDataSetChanged();
    }
}
