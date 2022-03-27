package com.example.finalyearprojectuser.recycleviewadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalyearprojectuser.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class VerticalViewAdapterSenatorVote extends RecyclerView.Adapter<VerticalViewAdapterSenatorVote.ViewHolder> {

    //Vars
    private static final String TAG = "RecyclerViewAdapter";

    private int selectedPosition = -1;
    private AdapterView.OnItemClickListener onItemClickListener;
    private ArrayList<String> mName = new ArrayList<>();
    private ArrayList<String> mCheckName = new ArrayList<>();
    private ArrayList<String> mImage = new ArrayList<>();
    private ArrayList<String> mState = new ArrayList<>();
    private ArrayList<String> mParty= new ArrayList<>();
    private ArrayList<String> mPropaganda= new ArrayList<>();
    private Context mContext;
    private SharedPreferences sharedPreferences;
    private RadioButton lastCheckedRB = null;

    //Setting Image/Context/Details to ArrayList
    public VerticalViewAdapterSenatorVote(Context context, ArrayList<String> name, ArrayList<String> image, ArrayList<String> state, ArrayList<String> propaganda, ArrayList<String> party){
        mName = name;
        mImage = image;
        mContext = context;
        mState = state;
        mParty = party;
        mPropaganda = propaganda;
    }

    //Declaring the UI That We Want To Link Between Recycle View And The Pager
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_swipe_vote_candidate_list, parent, false);
        return new ViewHolder(view);
    }



    //Bind The Widgets Together
    @Override
    public void onBindViewHolder(@NonNull VerticalViewAdapterSenatorVote.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Log.d(TAG, "onCreateViewHolder: called.");

        //Clear Shared Preferences Previous Data

        Glide.with(mContext)
                .asBitmap()
                .load(mImage.get(position))
                .into(holder.Image);

        holder.Propaganda.setText(mPropaganda.get(position));
        holder.State.setText(mState.get(position));
        holder.Party.setText(mParty.get(position));
        holder.Name.setText(mName.get(position));

        //Radio button change
        holder.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(lastCheckedRB != null && lastCheckedRB != holder.radioButton){
                    lastCheckedRB.setChecked(false);
                }

                //Store the clicked radiobutton
                lastCheckedRB = holder.radioButton;
                mCheckName.clear();
                mCheckName.add(mName.get(position));

                // Storing data into SharedPreferences
                sharedPreferences = mContext.getSharedPreferences("SelectedCandidate", Context.MODE_PRIVATE);

                // Creating an Editor object to edit(write to the file)
                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                // Storing the key and its value as the data fetched from edittext
                myEdit.putString("senatorSelected", String.valueOf(mCheckName));
                myEdit.commit();

            }
        });

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
    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView Image;
        RadioButton radioButton;
        TextView Name,State, Propaganda, Party;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            radioButton = itemView.findViewById(R.id.radioButton);
            Image = itemView.findViewById(R.id.voteCandidateImage);
            Name = itemView.findViewById(R.id.voteCandidateName);
            State = itemView.findViewById(R.id.voteCandidateState);
            Propaganda = itemView.findViewById(R.id.voteCandidatePropaganda);
            Party = itemView.findViewById(R.id.voteCandidateParty);

        }

    }
}

