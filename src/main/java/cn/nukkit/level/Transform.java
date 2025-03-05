package cn.nukkit.level;

import cn.nukkit.math.Rotator2;
import cn.nukkit.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public class Transform extends Locator {
    public @NotNull Rotator2 rotation;
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

    public Transform(@NotNull Vector3 position, @NotNull Rotator2 rotation, @NotNull Level level) {
        super(position.x, position.y, position.z, level);
        this.rotation = rotation;
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

    public Transform setX(double x) {
        this.position.setX(x);
        return this;
    }

    public Transform setY(double y) {
        this.position.setY(y);
        return this;
    }

    public Transform setZ(double z) {
        this.position.setZ(z);
        return this;
    }

    @Override
    public String toString() {
        return "Location (level=" + this.getLevel().getName() + ", x=" + this.position.x + ", y=" + this.position.y + ", z=" + this.position.z + ", yaw=" + this.yaw + ", pitch=" + this.pitch + ", headYaw=" + this.headYaw + ")";
    }

    @Override
    @NotNull public Transform getTransform() {
        return new Transform(this.position.x, this.position.y, this.position.z, this.yaw, this.pitch, this.headYaw, this.level);
    }

    public Transform add(double x) {
        return this.add(x, 0, 0);
    }

    public Transform add(double x, double y) {
        return this.add(x, y, 0);
    }

    @Override
    public Transform add(double x, double y, double z) {
        return new Transform(this.position.x + x, this.position.y + y, this.position.z + z, this.yaw, this.pitch, this.headYaw, this.level);
    }

    public Transform add(Vector3 x) {
        return new Transform(this.position.x + x.getX(), this.position.y + x.getY(), this.position.z + x.getZ(), this.yaw, this.pitch, this.headYaw, this.level);
    }

    public Transform subtract(double x) {
        return this.subtract(x, 0, 0);
    }

    public Transform subtract(double x, double y) {
        return this.subtract(x, y, 0);
    }

    @Override
    public Transform subtract(double x, double y, double z) {
        return this.add(-x, -y, -z);
    }

    public Transform subtract(Vector3 x) {
        return this.add(-x.getX(), -x.getY(), -x.getZ());
    }

    public Transform multiply(double number) {
        return new Transform(this.position.x * number, this.position.y * number, this.position.z * number, this.yaw, this.pitch, this.headYaw, this.level);
    }

    public Transform divide(double number) {
        return new Transform(this.position.x / number, this.position.y / number, this.position.z / number, this.yaw, this.pitch, this.headYaw, this.level);
    }

    public Transform ceil() {
        return new Transform((int) Math.ceil(this.position.x), (int) Math.ceil(this.position.y), (int) Math.ceil(this.position.z), this.yaw, this.pitch, this.headYaw, this.level);
    }

    public Transform floor() {
        return new Transform(this.position.getFloorX(), this.position.getFloorY(), this.position.getFloorZ(), this.yaw, this.pitch, this.headYaw, this.level);
    }

    public Transform round() {
        return new Transform(Math.round(this.position.x), Math.round(this.position.y), Math.round(this.position.z), this.yaw, this.pitch, this.headYaw, this.level);
    }

    public Transform abs() {
        return new Transform((int) Math.abs(this.position.x), (int) Math.abs(this.position.y), (int) Math.abs(this.position.z), this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform clone() {
        return (Transform) super.clone();
    }
}
