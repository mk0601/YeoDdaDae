package com.bucheon.yeoddadae;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

public class MainFragment extends Fragment {
    boolean apiKeyCertified;
    String loginId;
    FragmentToActivityListener dataPasser;

    ImageButton toSttImgBtn;
    ImageButton toFindParkImgBtn;
    ImageButton toFindGasStationImgBtn;
    ImageButton toRateReportDiscountParkImgBtn;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageButton menubarBtn;

    public MainFragment(boolean apiKeyCertified, String loginId) {
        this.apiKeyCertified = apiKeyCertified;
        this.loginId = loginId;
    }

    @Override
    public void onAttach(@NonNull Context context) { // context가 OnDataPass 인터페이스를 구현한다면 dataPasser에 할당
        super.onAttach(context);

        if (context instanceof FragmentToActivityListener) {
            dataPasser = (FragmentToActivityListener) context;
        }
        else {
            throw new ClassCastException(context.toString() + " must implement OnDataPass");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        toSttImgBtn = view.findViewById(R.id.toSttImgBtn);
        toFindParkImgBtn = view.findViewById(R.id.toFindParkImgBtn);
        toFindGasStationImgBtn = view.findViewById(R.id.toFindGasStationImgBtn);
        toRateReportDiscountParkImgBtn = view.findViewById(R.id.toRateReportDiscountParkImgBtn);
        drawerLayout = view.findViewById(R.id.drawer_layout);
        navigationView = view.findViewById(R.id.navigation_view);
        menubarBtn = view.findViewById(R.id.menubarBtn);

        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderUsername = headerView.findViewById(R.id.nowIdTxt);
        if (loginId != null) {
            navHeaderUsername.setText(loginId);
        }
        
        menubarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_logout) {
                    dataPasser.onDataPassed("로그아웃");
                }
                else if (item.getItemId() == R.id.nav_change_password) {
                    dataPasser.onDataPassed("비밀번호 변경");
                }
                return false;
            }
        });

        toSttImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataPasser.onDataPassed("stt버튼클릭");
            }
        });

        toFindParkImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apiKeyCertified) {
                    Intent findParkIntent = new Intent(getActivity().getApplicationContext(), FindParkActivity.class);
                    findParkIntent.putExtra("loginId", loginId);
                    findParkIntent.putExtra("SortBy", 1);
                    startActivity(findParkIntent);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "API 키가 인증되지 않았습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        toFindGasStationImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apiKeyCertified) {
                    Intent findGasStationIntent = new Intent(getActivity().getApplicationContext(), FindGasStationActivity.class);
                    findGasStationIntent.putExtra("SortBy", 1);
                    startActivity(findGasStationIntent);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "API 키가 인증되지 않았습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        toRateReportDiscountParkImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apiKeyCertified) {
                    Intent AnotherReportDiscountParkIntent = new Intent(getActivity().getApplicationContext(), AnotherReportDiscountParkActivity.class);
                    AnotherReportDiscountParkIntent.putExtra("loginId", loginId);
                    startActivity(AnotherReportDiscountParkIntent);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "API 키가 인증되지 않았습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}
