package edu.skku.treearium.Activity.MainPackage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import edu.skku.treearium.Activity.fragment3_fragment1;
import edu.skku.treearium.Activity.fragment3_fragment2;
import edu.skku.treearium.Activity.fragment3_fragment3;
import edu.skku.treearium.R;
import com.google.android.material.tabs.TabLayout;

import edu.skku.treearium.helpers.VerticalViewPager;
import edu.skku.treearium.helpers.ViewPagerAdapter;

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
        VerticalViewPager viewPager = view.findViewById(R.id.viewpager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());

        // add your fragments
        adapter.addFrag(new fragment3_fragment1(), "DBH");
        adapter.addFrag(new fragment3_fragment2(), "수종");
        adapter.addFrag(new fragment3_fragment3(), "Height");

        // set adapter on viewpager
        viewPager.setAdapter(adapter);
    }
}