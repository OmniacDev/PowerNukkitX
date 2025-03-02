package cn.nukkit.math;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public class Rotator2 {
    public double pitch;
    public double yaw;

    public static final Rotator2 ZERO = new Rotator2(0, 0);


    public Rotator2() {
        this(0, 0);
    }

    public Rotator2(double pitch) {
        this(pitch, 0);
    }

    public Rotator2(double pitch, double yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public double getPitch() {
        return this.pitch;
    }

    public double getYaw() {
        return this.yaw;
    }

    public int getFloorX() {
        return (int) Math.floor(this.pitch);
    }

    public int getFloorY() {
        return (int) Math.floor(this.yaw);
    }

    public Rotator2 add(double x) {
        return this.add(x, 0);
    }

    public Rotator2 add(double x, double y) {
        return new Rotator2(this.pitch + x, this.yaw + y);
    }

    public Rotator2 add(Rotator2 x) {
        return this.add(x.getPitch(), x.getYaw());
    }

    public Rotator2 subtract(double x) {
        return this.subtract(x, 0);
    }

    public Rotator2 subtract(double x, double y) {
        return this.add(-x, -y);
    }

    public Rotator2 subtract(Rotator2 x) {
        return this.add(-x.getPitch(), -x.getYaw());
    }

    public Rotator2 ceil() {
        return new Rotator2((int) (this.pitch + 1), (int) (this.yaw + 1));
    }

    public Rotator2 floor() {
        return new Rotator2((int) Math.floor(this.pitch), (int) Math.floor(this.yaw));
    }

    public Rotator2 round() {
        return new Rotator2(Math.round(this.pitch), Math.round(this.yaw));
    }

    public Rotator2 abs() {
        return new Rotator2(Math.abs(this.pitch), Math.abs(this.yaw));
    }

    public Rotator2 multiply(double number) {
        return new Rotator2(this.pitch * number, this.yaw * number);
    }

    public Rotator2 divide(double number) {
        return new Rotator2(this.pitch / number, this.yaw / number);
    }

    public double distance(double x) {
        return this.distance(x, 0);
    }

    public double distance(double x, double y) {
        return Math.sqrt(this.distanceSquared(x, y));
    }

    public double distance(Rotator2 vector) {
        return distance(vector.pitch, vector.yaw);
    }

    public double distanceSquared(double x) {
        return this.distanceSquared(x, 0);
    }

    public double distanceSquared(double x, double y) {
        double ex = this.pitch - x;
        double ey = this.yaw - y;
        return ey * ey + ex * ex;
    }

    public double distanceSquared(Rotator2 vector) {
        return this.distanceSquared(vector.pitch, vector.yaw);
    }

    public double length() {
        return Math.sqrt(this.lengthSquared());
    }

    public double lengthSquared() {
        return this.pitch * this.pitch + this.yaw * this.yaw;
    }

    public Rotator2 normalize() {
        double len = this.lengthSquared();
        if (len != 0) {
            return this.divide(Math.sqrt(len));
        }
        return new Rotator2(0, 0);
    }

    public double dot(Rotator2 v) {
        return this.pitch * v.pitch + this.yaw * v.yaw;
    }

    @Override
    public String toString() {
        return "Rotator2(x=" + this.pitch + ",y=" + this.yaw + ")";
    }

}
