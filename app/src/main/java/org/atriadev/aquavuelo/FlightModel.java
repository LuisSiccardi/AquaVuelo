package org.atriadev.aquavuelo;

import java.util.ArrayList;
import java.util.List;

/** SI units internally. Water-only thrust; residual compressed-air thrust is omitted. */
public final class FlightModel {
    public static final class Config {
        public final double liters, waterPercent, pressureBar, dryGrams, nozzleMm;
        public Config(double liters, double waterPercent, double pressureBar, double dryGrams, double nozzleMm) {
            if (!Double.isFinite(liters + waterPercent + pressureBar + dryGrams + nozzleMm)
                    || liters < .5 || liters > 3 || waterPercent < 0 || waterPercent > 80
                    || pressureBar < 0 || pressureBar > 6 || dryGrams < 50 || dryGrams > 500
                    || nozzleMm < 5 || nozzleMm > 25) throw new IllegalArgumentException("Parámetros fuera de rango");
            this.liters = liters; this.waterPercent = waterPercent; this.pressureBar = pressureBar;
            this.dryGrams = dryGrams; this.nozzleMm = nozzleMm;
        }
    }
    public static final class Point {
        public final double time, height, velocity;
        Point(double time, double height, double velocity) { this.time=time; this.height=height; this.velocity=velocity; }
    }
    public static final class Result {
        public final List<Point> points = new ArrayList<>();
        public double apogee, maxSpeed, duration, burnTime, apogeeTime;
        public boolean lifted;
    }
    public static Result simulate(Config c) { return simulate(c, .001); }
    public static Result simulate(Config c, double dt) {
        if (!(dt > 0 && dt <= .01)) throw new IllegalArgumentException("Paso temporal inválido");
        final double atm=101325, rho=1000, gravity=9.80665;
        double bottle=c.liters/1000, water=bottle*c.waterPercent/100;
        double initialAir=bottle-water, p0=atm+c.pressureBar*100000;
        double airMass=p0*initialAir/(287.05*293.15);
        double baseMass=c.dryGrams/1000+airMass;
        double nozzle=Math.PI*Math.pow(c.nozzleMm/1000,2)/4;
        double bodyArea=Math.PI*.09*.09/4;
        double height=0, velocity=0, t=0;
        boolean powered=false;
        Result r=new Result(); r.points.add(new Point(0,0,0));
        int sampleEvery=Math.max(1,(int)Math.round(.02/dt));
        for(int step=0; t<60; step++) {
            double pressure=p0*Math.pow(initialAir/(bottle-water),1.4);
            double thrust=0, expelled=0;
            if(water>1e-12 && pressure>atm) {
                double jet=Math.sqrt(2*(pressure-atm)/rho);
                expelled=Math.min(water, .85*nozzle*jet*dt);
                thrust=rho*expelled*jet/dt;
                powered=true; r.burnTime=t+dt;
            }
            double mass=baseMass+rho*(water-expelled/2);
            double drag=.5*1.225*.5*bodyArea*velocity*Math.abs(velocity);
            double acceleration=(thrust-drag)/mass-gravity;
            water=Math.max(0,water-expelled);
            double previousHeight=height, previousVelocity=velocity;
            velocity+=acceleration*dt;
            height+=(previousVelocity+velocity)*.5*dt;
            t+=dt;
            if(height<=0 && !r.lifted) { height=0; velocity=0; }
            if(height>0) r.lifted=true;
            if(height>r.apogee) {r.apogee=height; r.apogeeTime=t;}
            r.maxSpeed=Math.max(r.maxSpeed,Math.abs(velocity));
            if(r.lifted && height<=0) {
                double fraction=previousHeight/(previousHeight-height);
                t=t-dt+dt*fraction;
                r.points.add(new Point(t,0,previousVelocity+(velocity-previousVelocity)*fraction));
                break;
            }
            if(step%sampleEvery==0) r.points.add(new Point(t,height,velocity));
            if(!r.lifted && thrust==0 && (powered || pressure<=atm || water<=1e-12)) break;
        }
        r.duration=r.lifted?t:0;
        if(!r.lifted) {r.maxSpeed=0; r.apogeeTime=0;}
        return r;
    }
    private FlightModel() {}
}
