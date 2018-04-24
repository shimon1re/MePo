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

import static com.example.android.mepo.LoginActivity.IsStudent;
import static com.example.android.mepo.StudentActivity.getList_of_student_courses_names;
import static com.example.android.mepo.StudentCourseActivity.IsCourse;
import static com.example.android.mepo.StudentCourseDetailsActivity.getListOfStudentCourseLectures;
import static com.example.android.mepo.TeacherActivity.getList_of_teacher_courses_names;

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
        int layoutIdForListItem = R.layout.recycler_list_item;
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
        TextView teacherListItemNumberView,studentListItemNumberView,studentCourseListItemNumberView;



        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         * @param itemView The View that you inflated in
         *                 {@link RecyclerViewAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        public NumberViewHolder(View itemView) {
            super(itemView);
            if(IsStudent == null){
                teacherListItemNumberView = itemView.findViewById(R.id.tv_teacher_activity_item_number);
            }
            else{
                if(IsCourse == null){
                    studentListItemNumberView = itemView.findViewById(R.id.tv_student_activity_item_number);
                }
                else{
                    studentCourseListItemNumberView = itemView.findViewById(R.id.tv_student_course_item_number);
                }

            }
            //listItemNumberView = itemView.findViewById(R.id.tv_item_number);
            //viewHolderIndex = itemView.findViewById(R.id.tv_view_holder_instance);
            itemView.setOnClickListener(this);


        }


        /*
         * @param listIndex Position of the item in the list
         */
        void bind(int listIndex) {

            if(IsStudent == null) {
                String clean;
                ArrayList<String> list_of_courses_names = getList_of_teacher_courses_names();
                clean = list_of_courses_names.get(listIndex).toString();
                clean = clean.replaceAll("[0-9]","");
                clean = clean.replaceAll("[\\[\"\\],-]", "");
                teacherListItemNumberView.setText("Course: " + clean);

            }
            else{
                if(IsCourse == null){
                    String clean;
                    ArrayList<String> list_of_courses_names = getList_of_student_courses_names();
                    clean = list_of_courses_names.get(listIndex).toString();
                    clean = clean.replaceAll("[0-9]","");
                    clean = clean.replaceAll("[\\[\"\\],-]", "");
                    studentListItemNumberView.setText("Course: " + clean);
                }
                else {
                    String clean,l_date,l_num,l_status;
                    ArrayList<String> list_of_lectures_names = getListOfStudentCourseLectures();
                    clean = list_of_lectures_names.get(listIndex).toString();
                    l_date = clean.substring(6,14);
                    l_num = clean.substring(2,3);
                    l_status = clean.substring(17,clean.length()-2);
                    //System.out.println(l_num + " "+l_date + " "+l_status+ " sub!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");
                    studentCourseListItemNumberView.setText("Lecture "+ l_num + ": "+ l_date + " " + l_status);
                }
            }

        }


        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }
}
