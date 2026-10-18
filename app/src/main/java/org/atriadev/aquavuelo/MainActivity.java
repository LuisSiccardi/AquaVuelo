package org.atriadev.aquavuelo;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int INK=Color.rgb(16,45,64), TEAL=Color.rgb(8,127,131), MUTED=Color.rgb(86,107,120), BG=Color.rgb(244,247,248);
    private final Locale spanish=Locale.forLanguageTag("es-AR");
    private final double[] values={2,35,3,150,9};
    private final double[] minimum={.5,0,0,50,5}, increments={.1,1,.1,10,1};
    private final int[] maximumSteps={25,80,60,45,20};
    private final String[] labels={"Capacidad de la botella","Agua en la botella","Presión sobre la atmosférica","Masa del cohete vacío","Diámetro de la boquilla"};
    private TextView altitude, speed, flightTime, status;
    private final TextView[] controlValues=new TextView[5];
    private FlightChart chart;
    private FlightModel.Result result;
    private SharedPreferences prefs;
    private ValueAnimator animator;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs=getSharedPreferences("configuration",MODE_PRIVATE);
        for(int i=0;i<values.length;i++) values[i]=Math.max(minimum[i],Math.min(minimum[i]+maximumSteps[i]*increments[i],prefs.getFloat("p"+i,(float)values[i])));
        LinearLayout outer=new LinearLayout(this); outer.setOrientation(LinearLayout.VERTICAL); outer.setBackgroundColor(BG);
        outer.setOnApplyWindowInsetsListener((view,insets)-> {
            if(android.os.Build.VERSION.SDK_INT>=30){
                android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
                view.setPadding(bars.left,bars.top,bars.right,bars.bottom);
            }else view.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true);
        LinearLayout page=new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(22),dp(24),dp(22),dp(24));
        outer.addView(scroll); scroll.addView(page); setContentView(outer);
        addText(page,"ATRIADEV  /  LABORATORIO",11,TEAL,true,0);
        addText(page,"AquaVuelo",34,INK,true,5);
        addText(page,"Explorá el vuelo de un cohete de agua.",15,MUTED,false,4);

        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(18),dp(18),dp(18),dp(14));
        card.setBackground(shape(Color.WHITE,20)); add(page,card,20);
        addText(card,"ALTURA MÁXIMA ESTIMADA",11,MUTED,true,0);
        altitude=addText(card,"",40,INK,true,3); altitude.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        chart=new FlightChart(); card.addView(chart,new LinearLayout.LayoutParams(-1,dp(184)));
        LinearLayout stats=new LinearLayout(this); stats.setOrientation(LinearLayout.HORIZONTAL); add(card,stats,12);
        LinearLayout a=column(),b=column(); stats.addView(a,new LinearLayout.LayoutParams(0,-2,1)); stats.addView(b,new LinearLayout.LayoutParams(0,-2,1));
        speed=addText(a,"",21,INK,true,0); addText(a,"Velocidad máxima",12,MUTED,false,2);
        flightTime=addText(b,"",21,INK,true,0); addText(b,"Tiempo de vuelo",12,MUTED,false,2);
        status=addText(card,"",12,MUTED,false,12);
        Button play=button("Ver animación del vuelo",TEAL,Color.WHITE); add(page,play,12);
        play.setOnClickListener(v->animateFlight());
        addText(page,"Configurá tu cohete",22,INK,true,25);
        addText(page,"Mové los controles y compará los resultados.",13,MUTED,false,4);
        for(int i=0;i<values.length;i++) addControl(page,i);
        Button reset=button("Restablecer valores iniciales",Color.WHITE,TEAL); add(page,reset,14);
        reset.setOnClickListener(v->{ prefs.edit().clear().apply(); recreate(); });
        Button model=button("Cómo se calcula · Privacidad",BG,TEAL); add(page,model,8);
        model.setOnClickListener(v->showModel());
        addText(page,"SIMULACIÓN EDUCATIVA",10,TEAL,true,18);
        addText(page,"Los resultados son aproximados. Los valores del simulador no indican presiones seguras para una botella real.",12,MUTED,false,5);
        updateResult();
    }
    private void addControl(LinearLayout parent,int i) {
        LinearLayout card=column(); card.setPadding(dp(14),dp(13),dp(14),dp(8)); card.setBackground(shape(Color.WHITE,14)); add(parent,card,12);
        TextView title=addText(card,labels[i],13,INK,true,0);
        TextView value=addText(card,formatControl(i),18,TEAL,true,4);
        controlValues[i]=value;
        SeekBar seek=new SeekBar(this); seek.setMax(maximumSteps[i]); seek.setProgress((int)Math.round((values[i]-minimum[i])/increments[i]));
        seek.setMinimumHeight(dp(48)); seek.setContentDescription(labels[i]+", "+formatControl(i));
        card.addView(seek,new LinearLayout.LayoutParams(-1,dp(48)));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onStartTrackingTouch(SeekBar bar) { stopAnimation(); }
            public void onStopTrackingTouch(SeekBar bar) { save(); }
            public void onProgressChanged(SeekBar bar,int progress,boolean user) {
                values[i]=minimum[i]+increments[i]*progress; value.setText(formatControl(i));
                bar.setContentDescription(labels[i]+", "+formatControl(i)); updateResult();
            }
        });
    }
    private String f(String pattern,double v) { return String.format(spanish,pattern,v); }
    private String formatControl(int i) {
        switch(i) {
            case 0:return f("%.1f L",values[0]);
            case 1:return f("%.0f %%",values[1])+"  ·  "+f("%.0f ml",values[0]*values[1]*10);
            case 2:return f("%.1f bar",values[2]);
            case 3:return f("%.0f g",values[3]);
            default:return f("%.0f mm",values[4]);
        }
    }
    private void updateResult() {
        if(chart==null)return;
        for(int i=0;i<controlValues.length;i++)if(controlValues[i]!=null)controlValues[i].setText(formatControl(i));
        stopAnimation();
        result=FlightModel.simulate(new FlightModel.Config(values[0],values[1],values[2],values[3],values[4]));
        altitude.setText(f("%.1f m",result.apogee)); speed.setText(f("%.1f m/s",result.maxSpeed)); flightTime.setText(f("%.1f s",result.duration));
        status.setText(result.lifted?"Vuelo vertical · sin viento ni paracaídas":"No despega con esta configuración en el modelo.");
        chart.setContentDescription("Gráfica de altura respecto al tiempo. Altura máxima "+f("%.1f metros",result.apogee)+". Tiempo de vuelo "+f("%.1f segundos",result.duration));
        chart.fraction=-1; chart.invalidate();
    }
    private void animateFlight() {
        stopAnimation(); if(!result.lifted)return;
        animator=ValueAnimator.ofFloat(0,1); animator.setDuration(4500);
        animator.setInterpolator(new android.view.animation.LinearInterpolator());
        animator.addUpdateListener(a->{chart.fraction=(float)a.getAnimatedValue();chart.invalidate();}); animator.start();
    }
    private void stopAnimation(){if(animator!=null){animator.cancel();animator=null;}}
    private void save(){ SharedPreferences.Editor e=prefs.edit(); for(int i=0;i<values.length;i++)e.putFloat("p"+i,(float)values[i]);e.apply();}
    @Override protected void onPause(){super.onPause();save();stopAnimation();}
    private void showModel() {
        new AlertDialog.Builder(this).setTitle("Acerca de AquaVuelo").setMessage(
            "Modelo educativo de vuelo vertical con agua y aire comprimido. No utiliza combustibles.\n\n"+
            "Durante la expulsión del agua, la presión absoluta se calcula con P·V^1,4 constante. El caudal se estima con Bernoulli y un coeficiente de descarga de 0,85. Se integra el movimiento cada 0,001 segundos con masa variable, gravedad y resistencia del aire.\n\n"+
            "Supuestos: diámetro del cuerpo de 9 cm, coeficiente de resistencia 0,5, aire a 20 °C y presión atmosférica de 101.325 Pa. No incluye el empuje del aire residual, viento, giro, estabilidad, tubo de lanzamiento ni paracaídas. La masa vacía incluye botella, aletas y carga. El descenso es balístico.\n\n"+
            "No permite evaluar resistencia de materiales ni seguridad de un lanzamiento real. Los rangos de los controles son límites del simulador.\n\n"+
            "Privacidad: funciona sin internet. No recopila ni transmite datos, no usa anuncios ni analítica y no pide permisos. Solo guarda los ajustes en este dispositivo.\n\n"+
            "Referencia: NASA Glenn, Flight of a Water Rocket (www1.grc.nasa.gov). Implementación independiente; sin afiliación con NASA ni OpenRocket.\n\nVersión 1.0 · AtriaDev")
            .setPositiveButton("Entendido",null).show();
    }
    private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private LinearLayout column(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);return c;}
    private GradientDrawable shape(int color,int radius){GradientDrawable s=new GradientDrawable();s.setColor(color);s.setCornerRadius(dp(radius));return s;}
    private TextView addText(LinearLayout p,String text,int size,int color,boolean bold,int top){
        TextView t=new TextView(this);t.setText(text);t.setTextSize(size);t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL)); add(p,t,top);return t;
    }
    private void add(LinearLayout p,View v,int top){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(top);p.addView(v,lp);}
    private Button button(String text,int bg,int fg){Button b=new Button(this);b.setText(text);b.setAllCaps(false);b.setTextSize(15);b.setTextColor(fg);b.setMinHeight(dp(52));b.setBackground(shape(bg,14));return b;}

    private final class FlightChart extends View {
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path line=new Path(), fill=new Path();
        float fraction=-1;
        FlightChart(){super(MainActivity.this);setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);if(result==null)return;
            float left=dp(32),right=getWidth()-dp(8),top=dp(14),bottom=getHeight()-dp(27);
            double ymax=Math.max(5,Math.ceil(result.apogee/5)*5),tmax=Math.max(1,result.duration);
            paint.setTypeface(Typeface.DEFAULT);paint.setTextSize(dp(10));paint.setStrokeWidth(dp(1));
            for(int i=0;i<=2;i++){
                float y=bottom-(bottom-top)*i/2;
                paint.setColor(Color.rgb(229,237,240));canvas.drawLine(left,y,right,y,paint);
                paint.setColor(MUTED);paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText(f("%.0f",ymax*i/2),left-dp(6),y+dp(3),paint);
            }
            paint.setTextAlign(Paint.Align.LEFT);canvas.drawText("0 s",left,bottom+dp(19),paint);
            paint.setTextAlign(Paint.Align.RIGHT);canvas.drawText(f("%.1f s",tmax),right,bottom+dp(19),paint);
            if(!result.lifted){paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(dp(13));canvas.drawText("Sin despegue",(left+right)/2,(top+bottom)/2,paint);return;}
            line.reset();boolean first=true;
            for(FlightModel.Point p:result.points){float x=left+(right-left)*(float)(p.time/tmax), y=bottom-(bottom-top)*(float)(p.height/ymax);
                if(first){line.moveTo(x,y);first=false;}else line.lineTo(x,y);}
            fill.set(line);fill.lineTo(right,bottom);fill.lineTo(left,bottom);fill.close();
            paint.setColor(Color.rgb(226,245,244));paint.setStyle(Paint.Style.FILL);canvas.drawPath(fill,paint);
            paint.setColor(TEAL);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(2.5f));paint.setStrokeJoin(Paint.Join.ROUND);canvas.drawPath(line,paint);paint.setStyle(Paint.Style.FILL);
            if(result.lifted){
                double time=fraction<0?result.apogeeTime:result.duration*fraction;
                FlightModel.Point before=result.points.get(0),after=before;
                for(FlightModel.Point p:result.points){after=p;if(p.time>=time)break;before=p;}
                double blend=after.time>before.time?(time-before.time)/(after.time-before.time):0;
                double height=before.height+(after.height-before.height)*blend;
                float x=left+(right-left)*(float)(time/tmax),y=bottom-(bottom-top)*(float)(height/ymax);
                paint.setColor(Color.WHITE);canvas.drawCircle(x,y,dp(7),paint);paint.setColor(Color.rgb(223,119,85));canvas.drawCircle(x,y,dp(4),paint);
            }
        }
    }
}
