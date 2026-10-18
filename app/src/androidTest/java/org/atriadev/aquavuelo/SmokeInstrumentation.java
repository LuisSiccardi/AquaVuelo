package org.atriadev.aquavuelo;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/** Device smoke checks without external test libraries. */
public final class SmokeInstrumentation extends Instrumentation {
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    @Override public void onStart(){
        Bundle report=new Bundle();
        try{
            Activity a=startActivitySync(new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            waitForIdleSync();
            AtomicReference<Throwable> uiFailure=new AtomicReference<>();
            runOnMainSync(()->{
                try{
                View root=a.getWindow().getDecorView();
                ArrayList<SeekBar> sliders=new ArrayList<>();findSliders(root,sliders);
                if(sliders.size()!=5)throw new AssertionError("Five controls expected");
                int[] baseline={15,35,30,10,4};
                for(int i=0;i<baseline.length;i++)sliders.get(i).setProgress(baseline[i]);
                capture(a,root,"preview.png");
                sliders.get(2).setProgress(0);
                if(!contains(root,"No despega"))throw new AssertionError("Zero-pressure state missing");
                sliders.get(2).setProgress(30);
                sliders.get(0).setProgress(20);
                if(!contains(root,"875 ml"))throw new AssertionError("Water volume did not update with bottle capacity");
                sliders.get(0).setProgress(15);
                if(!contains(root,"28,7 m"))throw new AssertionError("Default apogee missing");
                capture(a,root,"preview.png");
                }catch(Throwable failure){uiFailure.set(failure);}
            });
            if(uiFailure.get()!=null)throw new AssertionError("UI check failed",uiFailure.get());
            if(getTargetContext().getPackageManager().getPackageInfo(getTargetContext().getPackageName(),4096).requestedPermissions!=null)throw new AssertionError("Unexpected permissions");
            a.finish();report.putString("stream","PASS: launch, five sliders, zero pressure, cross-control volume, default recovery, no permissions.\n");finish(Activity.RESULT_OK,report);
        }catch(Throwable failure){report.putString("stream","FAIL: "+failure+"\n");finish(Activity.RESULT_CANCELED,report);}
    }
    private static void capture(Activity a,View root,String name){
        Bitmap bitmap=Bitmap.createBitmap(root.getWidth(),root.getHeight(),Bitmap.Config.ARGB_8888);
        root.draw(new Canvas(bitmap));
        try(FileOutputStream stream=new FileOutputStream(new File(a.getFilesDir(),name))){bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);}catch(Exception e){throw new RuntimeException(e);}finally{bitmap.recycle();}
    }
    private static void findSliders(View v,ArrayList<SeekBar> out){if(v instanceof SeekBar)out.add((SeekBar)v);if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)findSliders(((ViewGroup)v).getChildAt(i),out);}
    private static boolean contains(View v,String text){if(v instanceof TextView && ((TextView)v).getText().toString().contains(text))return true;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)if(contains(((ViewGroup)v).getChildAt(i),text))return true;return false;}
}
