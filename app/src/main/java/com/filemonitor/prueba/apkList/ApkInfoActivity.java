package com.filemonitor.prueba.apkList;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.filemonitor.prueba.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ApkInfoActivity extends AppCompatActivity {
    TextView appLabel, packageName, version;
    TextView permissions, danpermissions, andVersion, installed, lastModify, path;
    AppData appData;
    PackageManager packageManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apk_activity);


        findViewsById();
        appData = (AppData) getIntent().getSerializableExtra("appData");
       // packageInfo = appData.getPackageInfo();
        packageManager = getPackageManager();


        setValues();


    }

    private void findViewsById() {
        appLabel = (TextView) findViewById(R.id.applabel);
        packageName = (TextView) findViewById(R.id.package_name);
        version = (TextView) findViewById(R.id.version_name);
        permissions = (TextView) findViewById(R.id.req_permission);
        andVersion = (TextView) findViewById(R.id.andversion);
        path = (TextView) findViewById(R.id.tvpath);
        installed = (TextView) findViewById(R.id.insdate);
        lastModify = (TextView) findViewById(R.id.last_modify);
        danpermissions = (TextView) findViewById(R.id.req_dpermission);

        danpermissions.setText("-");
    }

    private void setValues()  {
        // APP name
        appLabel.setText(appData.appLabel);

        // package name
        packageName.setText(appData.packageName);

        // version name
        version.setText(appData.versionName);

        // target version
        andVersion.setText(Integer
                .toString(appData.targetVersion));

        // path
        path.setText(appData.path);

        // first installation
        installed.setText(setDateFormat(appData.installedTime));

        // last modified
        lastModify.setText(setDateFormat(appData.lastModify));



        String perms ="";
        String dperms ="";
        // uses-permission

        if (appData.permissions != null)

            for ( String s : appData.permissions){
                try{
                if(packageManager.getPermissionInfo(s,0).protectionLevel == PermissionInfo.PROTECTION_DANGEROUS){
                    dperms = dperms + "- " + s + "\n";
                }
                else {
                    perms = perms + "- " + s + "\n";
                }

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                   // perms = perms + s + "\n";
                }



            }

        permissions.setText(perms);
            danpermissions.setText(dperms);
    }

    @SuppressLint("SimpleDateFormat")
    private String setDateFormat(long time) {
        Date date = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String strDate = formatter.format(date);
        return strDate;
    }

    // Convert string array to comma separated string
    private String getPermissions(String[] requestedPermissions) {
        String permission = "";
        for (int i = 0; i < requestedPermissions.length; i++) {
            permission = permission + requestedPermissions[i] + ",\n";
        }
        return permission;
    }


}

