package edu.imtl.BlueKare.Activity.Search;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.allattentionhere.fabulousfilter.AAH_FabulousFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.imtl.BlueKare.R;
import edu.imtl.BlueKare.Utils.TreesContent;


/********************* History Page class ***********************/
public class SearchActivity extends AppCompatActivity implements AAH_FabulousFragment.Callbacks, AAH_FabulousFragment.AnimationListener{


    private EditText mSearchField;
    private ImageButton mSearchBtn;
    private RecyclerView mRecyclerView;
    private TextView mNumberTree;
    RecyclerView.LayoutManager layoutManager;

    TreesAdapter adapter;
    TreesData tData;
    List<Trees> tList = new ArrayList<>();
    private ArrayMap<String, List<String>> applied_filters = new ArrayMap<>();
    FloatingActionButton filterBtn;
    MyFabFragment dialogFrag;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchField = (EditText) findViewById(R.id.search_field);
        mSearchBtn = (ImageButton) findViewById(R.id.search_btn);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mNumberTree = (TextView) findViewById(R.id.numberoftree);


        tData = TreesContent.getTrees();
        tList = TreesContent.getTrees().getAllTrees();
        Collections.sort(tList);


        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new TreesAdapter(tList, SearchActivity.this); //allTrees -> tList
        mRecyclerView.setAdapter(adapter);


        filterBtn = (FloatingActionButton) findViewById(R.id.filterBtn);
        dialogFrag = MyFabFragment.newInstance();
        dialogFrag.setParentFab(filterBtn);
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFrag.show(getSupportFragmentManager(), dialogFrag.getTag());
            }
        });


        /******************* 검색 시 변경 ***********************/
        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = mSearchField.getText().toString();
                List<Trees> tmp = filter(tList,searchText);
                mRecyclerView.setAdapter(new TreesAdapter(filter(tList,searchText), SearchActivity.this));
                mNumberTree.setText("총 "+tmp.size() + " 그루");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSearchBtn.setOnClickListener(new OnClickListener () {
            @Override
            public void onClick(View v) {
                String searchText = mSearchField.getText().toString();
                Toast.makeText(SearchActivity.this, "통계를 위해", Toast.LENGTH_SHORT).show();
                mRecyclerView.setAdapter(new TreesAdapter(filter(tList,searchText), SearchActivity.this));
            }
        });

    }

    /******************* 검색 필터 ***********************/
    private List<Trees> filter(List<Trees> tr, String query) {
        query = query.toLowerCase();
        final List<Trees> filterModeList = new ArrayList<>();
        for (Trees model : tr) {
            final String text = model.getTreeSpecies().toLowerCase();
            final String text2 = model.getTreeNearLandMark().toLowerCase();
            if (text.contains(query) || text2.contains(query)) {
                filterModeList.add(model);
            }
        }
        return filterModeList;
    }


    @Override
    public void onResult(Object result) {
        Log.d("k9res", "onResult: " + result.toString());
        if (result.toString().equalsIgnoreCase("swiped_down")) {
            //do something or nothing
        } else {
            if (result != null) {
                ArrayMap<String, List<String>> applied_filters = (ArrayMap<String, List<String>>) result;
                if (applied_filters.size() != 0) {
                    List<Trees> filteredList = tData.getAllTrees();
                    //iterate over arraymap
                    for (Map.Entry<String, List<String>> entry : applied_filters.entrySet()) {
                        Log.d("k9res", "entry.key: " + entry.getKey());
                        switch (entry.getKey()) {
                            case "dbh":
                                filteredList = tData.getDBHFilteredtrees(entry.getValue(), filteredList);
                                //dbh로 필터 리스트 채움
                                break;
                            case "height":
                                filteredList = tData.getHeightFilteredtrees(entry.getValue(), filteredList);
                                //height로 필터 리스트 채움
                                break;
                            case "species":
                                filteredList = tData.getSpeciesFilteredtrees(entry.getValue(), filteredList);
                                //종으로 필터 리스트 채움
                                break;
                        }
                    }
                    Log.d("k9res", "new size: " + filteredList.size());
                    tList.clear(); //비우고
                    tList.addAll(filteredList);// 필터해서
                    Collections.sort(tList);//정렬
                    adapter.notifyDataSetChanged(); //나타내기


                } else {
                    tList.addAll(tData.getAllTrees()); //다 넣고
                    Collections.sort(tList);//정렬
                    adapter.notifyDataSetChanged(); //나타내기
                }
            }

            //handle result
        }
    }

    public ArrayMap<String, List<String>> getApplied_filters() {
        return applied_filters;
    }

    public TreesData gettData() {
        return tData;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (dialogFrag.isAdded()) {
            dialogFrag.dismiss();
            dialogFrag.show(getSupportFragmentManager(), dialogFrag.getTag());
        }

    }

    @Override
    public void onOpenAnimationStart() {
        Log.d("aah_animation", "onOpenAnimationStart: ");
    }

    @Override
    public void onOpenAnimationEnd() {
        Log.d("aah_animation", "onOpenAnimationEnd: ");

    }

    @Override
    public void onCloseAnimationStart() {
        Log.d("aah_animation", "onCloseAnimationStart: ");

    }

    @Override
    public void onCloseAnimationEnd() {
        Log.d("aah_animation", "onCloseAnimationEnd: ");

    }
}
