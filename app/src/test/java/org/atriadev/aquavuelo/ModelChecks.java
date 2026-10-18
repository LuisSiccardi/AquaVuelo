package org.atriadev.aquavuelo;

/** Dependency-free numerical checks; run with the JDK shipped with Android Studio. */
public final class ModelChecks {
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args){
        FlightModel.Config standard=new FlightModel.Config(2,35,3,150,9);
        FlightModel.Result r=FlightModel.simulate(standard);
        check(r.lifted && r.apogee>5 && r.apogee<150,"Default flight bounds");
        check(r.duration>r.apogeeTime && r.apogeeTime>r.burnTime,"Phase ordering");
        check(r.points.get(r.points.size()-1).height==0,"Interpolated ground contact");
        check(FlightModel.simulate(new FlightModel.Config(2,35,0,150,9)).apogee==0,"Zero pressure");
        check(FlightModel.simulate(new FlightModel.Config(2,0,3,150,9)).apogee==0,"Zero water in water-thrust model");
        FlightModel.Result higher=FlightModel.simulate(new FlightModel.Config(2,35,4,150,9));
        check(higher.apogee>r.apogee,"More pressure increases default apogee");
        FlightModel.Result finer=FlightModel.simulate(standard,.0005);
        check(Math.abs(finer.apogee-r.apogee)/r.apogee<.01,"Time step convergence <1 percent");
        for(double water:new double[]{0,1,35,80})for(double pressure:new double[]{0,.1,3,6})for(double mass:new double[]{50,500})for(double nozzle:new double[]{5,25}){
            FlightModel.Result x=FlightModel.simulate(new FlightModel.Config(.5,water,pressure,mass,nozzle));
            check(Double.isFinite(x.apogee+x.duration+x.maxSpeed),"Finite at boundaries");
            check(x.apogee>=0 && x.duration>=0,"Nonnegative at boundaries");
            double last=-1;for(FlightModel.Point p:x.points){check(p.time>last && p.height>=0,"Monotonic time / nonnegative height");last=p.time;}
        }
        boolean rejected=false;try{new FlightModel.Config(2,100,3,150,9);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"Reject invalid inputs");
        System.out.printf(java.util.Locale.US,"PASS: boundaries, phases, zero cases, pressure sensitivity, step convergence. Default: %.2f m, %.2f s, %.2f m/s%n",r.apogee,r.duration,r.maxSpeed);
    }
}
