package com.example.Real_Tree.Activity.MainPackage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.Real_Tree.Activity.fragment3_fragment1;
import com.example.Real_Tree.Activity.fragment3_fragment2;
import com.example.Real_Tree.R;
import com.google.android.material.tabs.TabLayout;

import com.example.Real_Tree.helpers.ViewPagerAdapter;

public class fragment3 extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_fragment3, container, false);
        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // find views by id
        ViewPager viewPager = view.findViewById(R.id.viewpager);
        TabLayout tabLayout = view.findViewById(R.id.tablayout);

        // attach tablayout with viewpager
        tabLayout.setupWithViewPager(viewPager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());

        // add your fragments
        adapter.addFrag(new fragment3_fragment1(), "Tab1");
        adapter.addFrag(new fragment3_fragment2(), "Tab2");

        // set adapter on viewpager
        viewPager.setAdapter(adapter);
    }
}