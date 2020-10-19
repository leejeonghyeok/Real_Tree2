package edu.skku.treearium.Activity.Search;

import android.content.Context;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import edu.skku.treearium.R;
import edu.skku.treearium.Utils.TreesContent;

public class TreesAdapter extends RecyclerView.Adapter<TreesAdapter.TreesHolder> {

    private List<Trees> trees;
    private Context context;

    public TreesAdapter(List<Trees> trees, Context context) {
        this.trees = trees;
        this.context = context;
    }

    @NonNull
    @Override
    public TreesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, parent, false);
        return new TreesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TreesHolder holder, int position) {
        if(trees.get(position).getTreeName() != null) {
            holder.mTreeName.setText(trees.get(position).getTreeName());
        }
        if(trees.get(position).getTreeDbh() != null) {
            holder.mTreeDBH.setText(trees.get(position).getTreeDbh() + " cm");
        }
        if(trees.get(position).getTreeHeight() != null) {
            holder.mTreeHeight.setText(trees.get(position).getTreeHeight() + " m");
        }
        if(trees.get(position).getTreeSpecies() != null) {
            holder.mTreeSpecies.setText(trees.get(position).getTreeSpecies());
        }
        if(trees.get(position).getTreeNearLandMark() != null) {
            holder.mTreeLandmark.setText(trees.get(position).getTreeNearLandMark());
        }
        if(trees.get(position).getTreeLocation()!=null) {
            holder.mTreeLocation.setText(String.format("%.8f",trees.get(position).getTreeLocation().getLatitude())
                    + "     "
                    + String.format("%.8f",trees.get(position).getTreeLocation().getLongitude()));
        }
        if(trees.get(position).getTreePerson() != null) {
            holder.mPerson.setText(trees.get(position).getTreePerson());
        }

        //time
        SimpleDateFormat format1 = new SimpleDateFormat ( "yyyy년 MM월 dd일 HH시 mm분");
        if(trees.get(position).getTime()!=null) {
            String format_time1 = format1.format (1000*(Long.parseLong(trees.get(position).getTime())));
            holder.mTime.setText(format_time1);
        }

        //image
        if (trees.get(position).getTreeSpecies().equals("은행")) {
            holder.tree_image.setImageResource(R.mipmap.ginkgo2_icon_foreground);
        } else if (trees.get(position).getTreeSpecies().equals("단풍")) {
            holder.tree_image.setImageResource(R.mipmap.maple2_icon_foreground);
        } else if (trees.get(position).getTreeSpecies().equals("벚")) {
            holder.tree_image.setImageResource(R.mipmap.sakura4_icon_foreground);
        } else if(trees.get(position).getTreeSpecies().equals("메타")) {
            holder.tree_image.setImageResource(R.mipmap.meta_icon_foreground);
        } else {
            holder.tree_image.setImageResource(R.mipmap.tree_icon_foreground);
        }

        holder.expandableView.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.expandableView.getVisibility()==View.GONE){
                    TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
                    holder.expandableView.setVisibility(View.VISIBLE);
                    holder.arrowBtn.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                } else {
                    TreesContent.updateFirebase(holder.mTreeName.getText().toString(),trees.get(position).getTime());
                    holder.expandableView.setVisibility(View.GONE);
                    holder.arrowBtn.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return trees.size();
    }

    public void setFilter(List<Trees> treesList)
    {
        trees = new ArrayList<>();
        trees.addAll(treesList);
        notifyDataSetChanged();
    }

    public class TreesHolder extends RecyclerView.ViewHolder {

        TextView mTreeDBH, mTreeHeight, mTreeSpecies, mTreeLocation, mTime, mTreeLandmark, mPerson;
        EditText mTreeName;
        RelativeLayout expandableView;
        ImageView arrowBtn, tree_image;
        CardView cardView;


        public TreesHolder(@NonNull View itemView) {
            super(itemView);

            mPerson = itemView.findViewById(R.id.person);
            mTreeName = itemView.findViewById(R.id.name);
            mTreeDBH = itemView.findViewById(R.id.dbh);
            mTreeHeight  = itemView.findViewById(R.id.height);
            mTreeSpecies  = itemView.findViewById(R.id.species);
            mTreeLocation  = itemView.findViewById(R.id.location);
            mTreeLandmark = itemView.findViewById(R.id.landmark);
            mTime = itemView.findViewById(R.id.timeTree);
            expandableView = itemView.findViewById(R.id.expandableView);
            arrowBtn = itemView.findViewById(R.id.arrowBtn);
            cardView = itemView.findViewById(R.id.cardView);
            tree_image = itemView.findViewById(R.id.tree_image);
        }
    }




}
