package com.example.finalyearprojectuser.pdf;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearprojectuser.R;

import java.util.List;

public class PDFAdapter extends RecyclerView.Adapter<PDFAdapter.Holder> {

    private List<PDFModel> list;
    private Context context;
    ItemClickListener itemClickListener;

    //Setting PDF to ArrayList
    public PDFAdapter(List<PDFModel> list, Context context, ItemClickListener itemClickListener) {
        this.list = list;
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    //Declaring the UI That We Want To Link Between Recycle View And The Pager
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.activity_pdf_layout,parent,false);
        Holder holder = new Holder(v);
        return holder;
    }

    //Bind The Widgets Together
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        holder.pdfName.setText(list.get(position).getPdfName());
    }

    //Return Current Position Number
    @Override
    public int getItemCount() {
        return list.size();
    }

    //Setting PDF To The RecycleView
    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView pdfName;
        private ImageView imageView;

        public Holder(View itemView) {
            super(itemView);
            pdfName = itemView.findViewById(R.id.TV);
            imageView = itemView.findViewById(R.id.IV);
            imageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v,getAdapterPosition(),false);
        }
    }
}