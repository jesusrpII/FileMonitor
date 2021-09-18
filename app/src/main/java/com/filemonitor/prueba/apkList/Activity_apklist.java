package com.filemonitor.prueba.apkList;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PermissionInfo;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.filemonitor.prueba.R;
import com.filemonitor.prueba.SpacesItemDecoration;

public class Activity_apklist extends Activity  {

    PackageManager packageManager;
    RecyclerView apkList;
    ApkAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apklist);

        packageManager = getPackageManager();
        List<PackageInfo> packageList = packageManager
                .getInstalledPackages(PackageManager.GET_PERMISSIONS);

        List<PackageInfo> userAppList = new ArrayList<PackageInfo>();

        List<PackageInfo> dangerAppList= new ArrayList<PackageInfo>();

        /*To filter out System apps*/
        for(PackageInfo pi : packageList) {
            boolean b = false;

            b = haveDangerousPerms(pi);


            if(!isSystemPackage(pi)){
                userAppList.add(pi);

                if(b){
                    dangerAppList.add(pi);
                }
            }
        }
        apkList = findViewById(R.id.apk_list);
        adapter = new ApkAdapter(this, dangerAppList, packageManager);
        apkList.setAdapter(adapter);
        apkList.setLayoutManager(new GridLayoutManager(this,1));

        apkList.addItemDecoration(new SpacesItemDecoration(3));


        CheckBox c = findViewById(R.id.apk_check);

        c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    adapter.setPackageList(userAppList);
                }
                else {
                    adapter.setPackageList(dangerAppList);
                }
            }
        });

    }

    /**
     * Return whether the given PackgeInfo represents a system package or not.
     * User-installed packages (Market or otherwise) should not be denoted as
     * system packages.
     *
     * @param pkgInfo
     * @return boolean
     */
    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true
                : false;
    }

    @SuppressLint("LongLogTag")
    private boolean haveDangerousPerms(PackageInfo pkginfo)  {

        Boolean cond = false;

        if (pkginfo.requestedPermissions != null) {
            for (String s : pkginfo.requestedPermissions) {
                try {
                    if (packageManager.getPermissionInfo(s, 0).protectionLevel == PermissionInfo.PROTECTION_DANGEROUS) {
                        cond = true;
                        break;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.i("PM.NameNotFoundException", s);
                }

            }
        }
        return cond;
    }



}



