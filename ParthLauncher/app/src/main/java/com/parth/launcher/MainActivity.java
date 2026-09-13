package com.parth.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final int COLUMNS = 4;
    private static final int BG = Color.rgb(8,10,16);
    private static final int TEXT = Color.rgb(244,247,255);
    private static final int SECONDARY = Color.rgb(154,166,196);
    private static final int SURFACE = Color.rgb(23,27,41);
    private static final int BORDER = Color.rgb(41,48,71);
    private LinearLayout root, grid;
    private EditText search;
    private TextView greetingView, dateView, timeView;
    private final ArrayList<AppInfo> apps = new ArrayList<>();
    private SharedPreferences prefs;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        prefs = getSharedPreferences("launcher", MODE_PRIVATE);
        buildUi();
        loadApps();
        startClock();
    }

    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + .5f); }
    private TextView text(String s, float size) {
        TextView v = new TextView(this); v.setText(s); v.setTextColor(TEXT); v.setTextSize(size); return v;
    }
    private GradientDrawable rounded(int color, float radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }

    private void buildUi() {
        FrameLayout frame = new FrameLayout(this); frame.setBackgroundColor(BG);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setClipToPadding(false);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(34)); scroll.addView(root);
        frame.addView(scroll, new FrameLayout.LayoutParams(-1,-1));
        setContentView(frame);

        LinearLayout header = new LinearLayout(this); header.setOrientation(LinearLayout.VERTICAL);
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(132)));
        greetingView = text(greeting(), 15); greetingView.setTextColor(SECONDARY); header.addView(greetingView);
        timeView = text(currentTime(), 36); timeView.setTypeface(null, 1); timeView.setPadding(0,dp(3),0,0); header.addView(timeView);
        LinearLayout line = new LinearLayout(this); line.setGravity(Gravity.CENTER_VERTICAL);
        dateView = text(currentDate(), 14); dateView.setTextColor(SECONDARY); line.addView(dateView, new LinearLayout.LayoutParams(0,dp(28),1));
        TextView settings = text("⚙", 22); settings.setGravity(Gravity.CENTER); settings.setBackground(rounded(SURFACE,18));
        line.addView(settings, new LinearLayout.LayoutParams(dp(44),dp(44))); settings.setOnClickListener(v -> openSettings());
        header.addView(line);

        search = new EditText(this); search.setSingleLine(true); search.setHint(getString(com.parth.launcher.R.string.search_apps));
        search.setHintTextColor(Color.rgb(115,124,151)); search.setTextColor(TEXT); search.setTextSize(16); search.setPadding(dp(18),0,dp(12),0);
        search.setBackgroundResource(com.parth.launcher.R.drawable.search_bg); search.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_search,0,0,0);
        root.addView(search,new LinearLayout.LayoutParams(-1,dp(54)));
        search.addTextChangedListener(new TextWatcher(){ public void beforeTextChanged(CharSequence s,int a,int c,int d){} public void onTextChanged(CharSequence s,int a,int b,int c){render(s.toString());} public void afterTextChanged(Editable e){} });

        TextView label=text(getString(R.string.applications),12); label.setTextColor(Color.rgb(115,124,151)); label.setPadding(dp(4),dp(22),0,dp(10)); root.addView(label);
        grid=new LinearLayout(this); grid.setOrientation(LinearLayout.VERTICAL); root.addView(grid);
    }

    private void startClock() {
        final android.os.Handler h = new android.os.Handler();
        Runnable r = new Runnable(){ public void run(){ if (greetingView!=null){greetingView.setText(greeting());timeView.setText(currentTime());dateView.setText(currentDate());} h.postDelayed(this,30000); }};
        h.post(r);
    }
    private String greeting(){ int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY); if(hour<12)return "Good morning, Parth"; if(hour<17)return "Hello, Parth"; if(hour<21)return "Good evening, Parth"; return "Good night, Parth"; }
    private String currentTime(){ return new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date()); }
    private String currentDate(){ return new SimpleDateFormat("EEEE, d MMMM",Locale.getDefault()).format(new Date()); }

    private void loadApps(){
        new Thread(() -> {
            PackageManager pm=getPackageManager(); Intent intent=new Intent(Intent.ACTION_MAIN); intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> results=pm.queryIntentActivities(intent,PackageManager.MATCH_ALL);
            ArrayList<AppInfo> loaded=new ArrayList<>();
            for(ResolveInfo r:results){
                if(r.activityInfo==null || r.activityInfo.packageName.equals(getPackageName())) continue;
                AppInfo a=new AppInfo(); a.label=r.loadLabel(pm).toString(); a.icon=r.loadIcon(pm);
                a.intent=new Intent(Intent.ACTION_MAIN); a.intent.addCategory(Intent.CATEGORY_LAUNCHER); a.intent.setClassName(r.activityInfo.packageName,r.activityInfo.name); loaded.add(a);
            }
            loaded.sort((a,b)->a.label.compareToIgnoreCase(b.label));
            runOnUiThread(()->{apps.clear();apps.addAll(loaded);render(search==null?"":search.getText().toString());});
        }).start();
    }

    private void render(String query){
        if(grid==null)return; grid.removeAllViews(); String q=query.trim().toLowerCase(Locale.getDefault()); ArrayList<AppInfo> filtered=new ArrayList<>();
        for(AppInfo a:apps) if(a.label.toLowerCase(Locale.getDefault()).contains(q)) filtered.add(a);
        if(filtered.isEmpty()){TextView e=text(q.isEmpty()?"Loading apps…":getString(R.string.no_apps),16); e.setGravity(Gravity.CENTER); e.setPadding(0,dp(45),0,dp(45)); grid.addView(e); return;}
        LinearLayout row=null; int col=0;
        for(AppInfo a:filtered){
            if(col==0){row=new LinearLayout(this);row.setGravity(Gravity.TOP);grid.addView(row,new LinearLayout.LayoutParams(-1,dp(104)));}
            LinearLayout cell=new LinearLayout(this);cell.setOrientation(LinearLayout.VERTICAL);cell.setGravity(Gravity.CENTER);cell.setPadding(dp(3),dp(5),dp(3),dp(5));
            ImageView icon=new ImageView(this); icon.setImageDrawable(a.icon); icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            FrameLayout iconFrame=new FrameLayout(this); iconFrame.setBackgroundResource(R.drawable.icon_bg); iconFrame.setPadding(dp(8),dp(8),dp(8),dp(8)); iconFrame.addView(icon,new FrameLayout.LayoutParams(-1,-1));
            cell.addView(iconFrame,new LinearLayout.LayoutParams(dp(56),dp(56)));
            TextView n=text(a.label,11); n.setGravity(Gravity.CENTER); n.setMaxLines(1); n.setEllipsize(android.text.TextUtils.TruncateAt.END); cell.addView(n,new LinearLayout.LayoutParams(-1,dp(30)));
            row.addView(cell,new LinearLayout.LayoutParams(0,-1,1));
            cell.setOnClickListener(v->{v.animate().scaleX(.90f).scaleY(.90f).setDuration(70).withEndAction(()->{v.animate().scaleX(1).scaleY(1).setDuration(90);try{startActivity(a.intent);}catch(Exception ex){Toast.makeText(this,"Couldn't open app",Toast.LENGTH_SHORT).show();}});});
            col=(col+1)%COLUMNS;
        }
    }

    private void openSettings(){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(22),dp(18),dp(22),dp(8));
        TextView title=text("Parth Launcher",24);title.setTypeface(null,1);box.addView(title);
        TextView about=text("Personalized, offline-first home screen",13);about.setTextColor(SECONDARY);about.setPadding(0,dp(3),0,dp(18));box.addView(about);
        Switch animation=new Switch(this);animation.setText("App press animations");animation.setTextColor(TEXT);animation.setChecked(prefs.getBoolean("animations",true));box.addView(animation);animation.setOnCheckedChangeListener((b,c)->prefs.edit().putBoolean("animations",c).apply());
        TextView home=text("Set as default launcher",16);home.setTextColor(TEXT);home.setGravity(Gravity.CENTER_VERTICAL);home.setPadding(dp(4),dp(18),0,dp(18));box.addView(home);home.setOnClickListener(v->{try{startActivity(new Intent(android.provider.Settings.ACTION_HOME_SETTINGS));}catch(Exception ignored){}});
        TextView info=text("About\nParth Launcher 1.0\nNo analytics • No account • No network required",14);info.setTextColor(SECONDARY);info.setPadding(dp(4),dp(10),0,dp(10));box.addView(info);
        AlertDialog dialog=new AlertDialog.Builder(this).setView(box).setNegativeButton("Close",null).create(); dialog.show();
    }

    static class AppInfo { String label; Drawable icon; Intent intent; }
}
