package com.example.android.mepo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.android.mepo.TeacherActivity.getList_of_courses_names;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.NumberViewHolder>{

    private static final String TAG = RecyclerViewAdapter.class.getSimpleName();

    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    final private ListItemClickListener mOnClickListener;
    private static int viewHolderCount;
    private int mNumberItems;





    /*
     * The interface that receives onClick messages.
     */
    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }



    public RecyclerViewAdapter(int numberOfItems, ListItemClickListener listener){
        mNumberItems = numberOfItems;
        mOnClickListener = listener;
        viewHolderCount = 0;
    }



    @NonNull
    @Override
    public NumberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.course_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        NumberViewHolder viewHolder = new NumberViewHolder(view);

        viewHolderCount++;
        Log.d(TAG, "onCreateViewHolder: number of ViewHolders created: "
                + viewHolderCount);

        return viewHolder;
    }



    @Override
    public void onBindViewHolder(@NonNull NumberViewHolder holder, int position) {
        Log.d(TAG, "#" + position);
        holder.bind(position);
    }


    @Override
    public int getItemCount() {
        return mNumberItems;
    }




    class NumberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        // Will display the position in the list, ie 0 through getItemCount() - 1
        TextView listItemNumberView;


        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         * @param itemView The View that you inflated in
         *                 {@link RecyclerViewAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        public NumberViewHolder(View itemView) {
            super(itemView);

            listItemNumberView = itemView.findViewById(R.id.tv_item_number);
            //viewHolderIndex = itemView.findViewById(R.id.tv_view_holder_instance);
            itemView.setOnClickListener(this);


        }


        /*
         * @param listIndex Position of the item in the list
         */
        void bind(int listIndex) {
            ArrayList<String> list_of_courses_names = getList_of_courses_names();
            listItemNumberView.setText("Course: " + list_of_courses_names.get(listIndex).toString()
                    .replaceAll("[\\[\"\\],-]",""));

        }


        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }
}
