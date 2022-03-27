package com.example.finalyearprojectuser.recycleviewadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalyearprojectuser.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class HorizontalViewAdapterParty extends RecyclerView.Adapter<HorizontalViewAdapterParty.ViewHolder>{

    //Vars
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> mName = new ArrayList<>();
    private ArrayList<String> mImage = new ArrayList<>();
    private ArrayList<String> mDetails = new ArrayList<>();
    private ArrayList<String> mHistory = new ArrayList<>();
    private Context mContext;

    //Setting Image/Context/Details to ArrayList
    public HorizontalViewAdapterParty(Context context, ArrayList<String> name, ArrayList<String> image, ArrayList<String> details, ArrayList<String> history){
        mName = name;
        mImage = image;
        mContext = context;
        mDetails = details;
        mHistory = history;
    }

    //Declaring the UI That We Want To Link Between Recycle View And The Pager
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_swipe_horizontal_party, parent, false);
        return new ViewHolder(view);
    }

    //Bind The Widgets Together
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Log.d(TAG, "onCreateViewHolder: called.");

        Glide.with(mContext)
                .asBitmap()
                .load(mImage.get(position))
                .into(holder.Image);

        holder.Details.setText(mDetails.get(position));
        holder.History.setText(mHistory.get(position));
        holder.Name.setText(mName.get(position));
        holder.Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on an image" + mName.get(position));
                Toast.makeText(mContext, mName.get(position), Toast.LENGTH_SHORT).show();
            }
        });

    }

    //Return Current Position Number
    @Override
    public int getItemCount() {
        return mImage.size();
    }

    //Setting Image/Details/Name To The RecycleView
    public static class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView Image;
        TextView Name;
        TextView Details;
        TextView History;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            Image = itemView.findViewById(R.id.horizontalImage);
            Name = itemView.findViewById(R.id.horizontalName);
            Details = itemView.findViewById(R.id.horizontalDetails);
            History = itemView.findViewById(R.id.horizontalHistory);

        }
    }
}
