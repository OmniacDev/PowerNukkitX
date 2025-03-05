package cn.nukkit.level;

import cn.nukkit.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public class Transform extends Locator {
    public double yaw;
    public double pitch;
    public double headYaw;

    public Transform(@NotNull Level level) {
        this(0, level);
    }

    public Transform(double x, @NotNull Level level) {
        this(x, 0, level);
    }

    public Transform(double x, double y, @NotNull Level level) {
        this(x, y, 0, level);
    }

    public Transform(double x, double y, double z, @NotNull Level level) {
        this(x, y, z, 0, level);
    }

    public Transform(double x, double y, double z, double yaw, @NotNull Level level) {
        this(x, y, z, yaw, 0, level);
    }

    public Transform(double x, double y, double z, double yaw, double pitch, @NotNull Level level) {
        this(x, y, z, yaw, pitch, 0, level);
    }

    public Transform(double x, double y, double z, double yaw, double pitch, double headYaw, @NotNull Level level) {
        super(x, y, z , level);
        this.yaw = yaw;
        this.pitch = pitch;
        this.headYaw = headYaw;
    }

    public static Transform fromObject(Vector3 pos, @NotNull Level level) {
        return fromObject(pos, level, 0.0f);
    }

    public static Transform fromObject(Vector3 pos, @NotNull Level level, double yaw) {
        return fromObject(pos, level, yaw, 0.0f);
    }

    public static Transform fromObject(Vector3 pos, @NotNull Level level, double yaw, double pitch) {
        return fromObject(pos, level, yaw, pitch, 0.0f);
    }

    public static Transform fromObject(Vector3 pos, @NotNull Level level, double yaw, double pitch, double headYaw) {
        return new Transform(pos.x, pos.y, pos.z, yaw, pitch, headYaw, level);
    }

    public double getYaw() {
        return this.yaw;
    }

    public Transform setYaw(double yaw) {
        this.yaw = yaw;
        return this;
    }

    public double getPitch() {
        return this.pitch;
    }

    public Transform setPitch(double pitch) {
        this.pitch = pitch;
        return this;
    }

    public double getHeadYaw() {
        return this.headYaw;
    }

    public Transform setHeadYaw(double headYaw) {
        this.headYaw = headYaw;
        return this;
    }

    @Override
    public Transform setX(double x) {
        super.setX(x);
        return this;
    }

    @Override
    public Transform setY(double y) {
        super.setY(y);
        return this;
    }

    @Override
    public Transform setZ(double z) {
        super.setZ(z);
        return this;
    }

    @Override
    public String toString() {
        return "Location (level=" + this.getLevel().getName() + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + ", yaw=" + this.yaw + ", pitch=" + this.pitch + ", headYaw=" + this.headYaw + ")";
    }

    @Override
    @NotNull public Transform getLocation() {
        return new Transform(this.x, this.y, this.z, this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform add(double x) {
        return this.add(x, 0, 0);
    }

    @Override
    public Transform add(double x, double y) {
        return this.add(x, y, 0);
    }

    @Override
    public Transform add(double x, double y, double z) {
        return new Transform(this.x + x, this.y + y, this.z + z, this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform add(Vector3 x) {
        return new Transform(this.x + x.getX(), this.y + x.getY(), this.z + x.getZ(), this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform subtract(double x) {
        return this.subtract(x, 0, 0);
    }

    @Override
    public Transform subtract(double x, double y) {
        return this.subtract(x, y, 0);
    }

    @Override
    public Transform subtract(double x, double y, double z) {
        return this.add(-x, -y, -z);
    }

    @Override
    public Transform subtract(Vector3 x) {
        return this.add(-x.getX(), -x.getY(), -x.getZ());
    }

    @Override
    public Transform multiply(double number) {
        return new Transform(this.x * number, this.y * number, this.z * number, this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform divide(double number) {
        return new Transform(this.x / number, this.y / number, this.z / number, this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform ceil() {
        return new Transform((int) Math.ceil(this.x), (int) Math.ceil(this.y), (int) Math.ceil(this.z), this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform floor() {
        return new Transform(this.getFloorX(), this.getFloorY(), this.getFloorZ(), this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform round() {
        return new Transform(Math.round(this.x), Math.round(this.y), Math.round(this.z), this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform abs() {
        return new Transform((int) Math.abs(this.x), (int) Math.abs(this.y), (int) Math.abs(this.z), this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform clone() {
        return (Transform) super.clone();
    }
}
