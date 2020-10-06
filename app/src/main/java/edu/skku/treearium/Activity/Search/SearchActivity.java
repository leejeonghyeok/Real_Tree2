package edu.skku.treearium.Activity.Search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.skku.treearium.R;
import edu.skku.treearium.Utils.TreesContent;

public class SearchActivity extends AppCompatActivity {

    private EditText mSearchField;
    private ImageButton mSearchBtn;
    private RecyclerView mRecyclerView;
    RecyclerView.LayoutManager layoutManager;
    TreesAdapter adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchField = (EditText) findViewById(R.id.search_field);
        mSearchBtn = (ImageButton) findViewById(R.id.search_btn);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        List<Trees> allTrees = TreesContent.getTrees();



        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        adapter = new TreesAdapter(allTrees, SearchActivity.this);
        mRecyclerView.setAdapter(adapter);

        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = mSearchField.getText().toString();
                mRecyclerView.setAdapter(new TreesAdapter(filter(allTrees,searchText), SearchActivity.this));
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
                mRecyclerView.setAdapter(new TreesAdapter(filter(allTrees,searchText), SearchActivity.this));
            }
        });
    }

    private List<Trees> filter(List<Trees> tr, String query) {
        query = query.toLowerCase();
        final List<Trees> filterModeList = new ArrayList<>();
        for (Trees model : tr) {
            final String text = model.getTreeName().toLowerCase();
            if (text.contains(query)) {
                filterModeList.add(model);
            }
        }
        return filterModeList;
    }




}
